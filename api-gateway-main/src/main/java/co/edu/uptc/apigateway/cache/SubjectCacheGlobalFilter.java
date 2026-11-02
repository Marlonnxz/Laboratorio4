package co.edu.uptc.apigateway.cache;

import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

@Component
public class SubjectCacheGlobalFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(SubjectCacheGlobalFilter.class);
    private static final String X_CACHE_HEADER = "X-Cache";

    private final RedisCacheService redisCacheService;

    public SubjectCacheGlobalFilter(RedisCacheService redisCacheService) {
        this.redisCacheService = redisCacheService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();

        // Only handle /api/subjects/**
        if (!path.startsWith("/api/subjects")) {
            return chain.filter(exchange);
        }

        HttpMethod method = request.getMethod();

        // 1. Handle Cache Invalidation on write requests (POST, PUT, DELETE, PATCH)
        if (method == HttpMethod.POST || method == HttpMethod.PUT || method == HttpMethod.DELETE || method == HttpMethod.PATCH) {
            return chain.filter(exchange).then(Mono.defer(() -> {
                HttpStatus status = (HttpStatus) exchange.getResponse().getStatusCode();
                if (status != null && status.is2xxSuccessful()) {
                    return redisCacheService.invalidateSubjectsCache().then();
                }
                return Mono.empty();
            }));
        }

        // 2. Handle Cache Read for GET requests
        if (method != HttpMethod.GET || !redisCacheService.isCacheEnabled()) {
            return chain.filter(exchange);
        }

        // Do not cache binary file downloads (e.g. /api/subjects/.../pdf)
        if (path.endsWith("/pdf") || path.endsWith("/pdf-url") || path.contains("/documents")) {
            return chain.filter(exchange);
        }

        String rawQuery = request.getURI().getRawQuery();
        String normalizedQuery = (rawQuery != null && !rawQuery.isBlank()) ? rawQuery : "";

        return redisCacheService.getVersion().flatMap(version -> {
            String cacheKey = "subjects:catalog:v" + version + ":" + path + "?" + normalizedQuery;

            return redisCacheService.get(cacheKey)
                    .flatMap(cachedBody -> {
                        // Cache HIT
                        ServerHttpResponse response = exchange.getResponse();
                        response.setStatusCode(HttpStatus.OK);
                        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                        response.getHeaders().set(X_CACHE_HEADER, "HIT");

                        byte[] bytes = cachedBody.getBytes(StandardCharsets.UTF_8);
                        DataBuffer buffer = response.bufferFactory().wrap(bytes);
                        return response.writeWith(Mono.just(buffer));
                    })
                    .switchIfEmpty(Mono.defer(() -> {
                        // Cache MISS - intercept downstream response and cache it
                        ServerHttpResponse originalResponse = exchange.getResponse();
                        originalResponse.getHeaders().set(X_CACHE_HEADER, "MISS");
                        DataBufferFactory bufferFactory = originalResponse.bufferFactory();

                        ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(originalResponse) {
                            @Override
                            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                                if (getStatusCode() != null && getStatusCode().is2xxSuccessful() && body instanceof Flux) {
                                    Flux<? extends DataBuffer> fluxBody = Flux.from(body);
                                    return super.writeWith(fluxBody.buffer().map(dataBuffers -> {
                                        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                                        dataBuffers.forEach(buffer -> {
                                            byte[] array = new byte[buffer.readableByteCount()];
                                            buffer.read(array);
                                            DataBufferUtils.release(buffer);
                                            try {
                                                outputStream.write(array);
                                            } catch (Exception ignored) {
                                            }
                                        });

                                        byte[] responseBytes = outputStream.toByteArray();
                                        String responseString = new String(responseBytes, StandardCharsets.UTF_8);

                                        // Asynchronously store in Redis
                                        redisCacheService.set(cacheKey, responseString).subscribe();

                                        return bufferFactory.wrap(responseBytes);
                                    }));
                                }
                                return super.writeWith(body);
                            }
                        };

                        return chain.filter(exchange.mutate().response(decoratedResponse).build());
                    }));
        });
    }

    @Override
    public int getOrder() {
        return -1; // Run after security filter (-100), but before Netty routing filter
    }
}

