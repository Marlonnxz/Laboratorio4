package co.edu.uptc.apigateway.security;

import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class AuthenticationAndAuthorizationFilter implements GlobalFilter, Ordered {

    private static final List<String> PUBLIC_PATH_PREFIXES = List.of(
            "/api/auth/",
            "/actuator/",
            "/swagger-ui",
            "/v3/api-docs",
            "/webjars/"
    );

    private static final Pattern STUDENT_DETAIL_PATTERN = Pattern.compile("^/api/students/(\\d+)/(?:detail|detalle)$");

    private final JwtValidator jwtValidator;

    @Value("${gateway.security.enabled:true}")
    private boolean securityEnabled;

    public AuthenticationAndAuthorizationFilter(JwtValidator jwtValidator) {
        this.jwtValidator = jwtValidator;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (!securityEnabled) {
            return chain.filter(exchange);
        }

        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();

        // 1. Skip preflight CORS requests
        if (request.getMethod() == HttpMethod.OPTIONS) {
            return chain.filter(exchange);
        }

        // 2. Skip public endpoints
        for (String publicPrefix : PUBLIC_PATH_PREFIXES) {
            if (path.startsWith(publicPrefix)) {
                return chain.filter(exchange);
            }
        }

        // 3. Extract Authorization header
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return onError(exchange, HttpStatus.UNAUTHORIZED, "Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7).trim();
        Claims claims = jwtValidator.validateAndExtract(token);
        if (claims == null) {
            return onError(exchange, HttpStatus.UNAUTHORIZED, "Invalid or expired JWT token");
        }

        String role = claims.get("role", String.class);
        String username = claims.getSubject();
        Object studentIdObj = claims.get("studentId");
        Long tokenStudentId = null;
        if (studentIdObj instanceof Number num) {
            tokenStudentId = num.longValue();
        }

        // 4. Role-based authorization rules (RBAC)

        // Rule A: Modifying subjects catalog requires ADMIN
        if (path.startsWith("/api/subjects")) {
            HttpMethod method = request.getMethod();
            if (method == HttpMethod.POST || method == HttpMethod.PUT || method == HttpMethod.DELETE || method == HttpMethod.PATCH) {
                if (!"ADMIN".equalsIgnoreCase(role)) {
                    return onError(exchange, HttpStatus.FORBIDDEN, "Access denied: ADMIN role required to create, update, or delete subjects");
                }
            }
        }

        // Rule B: Student Detail requires ADMIN or student accessing their own record
        Matcher detailMatcher = STUDENT_DETAIL_PATTERN.matcher(path);
        if (detailMatcher.matches()) {
            Long requestedStudentId = Long.parseLong(detailMatcher.group(1));
            if ("ESTUDIANTE".equalsIgnoreCase(role)) {
                if (tokenStudentId == null || !tokenStudentId.equals(requestedStudentId)) {
                    return onError(exchange, HttpStatus.FORBIDDEN, "Access denied: Students can only view their own detail profile (requested: " + requestedStudentId + ", token: " + tokenStudentId + ")");
                }
            }
        }

        // 5. Mutate request to pass authenticated user metadata downstream
        ServerHttpRequest mutatedRequest = request.mutate()
                .header("X-User-Username", username != null ? username : "")
                .header("X-User-Role", role != null ? role : "")
                .header("X-Student-Id", tokenStudentId != null ? tokenStudentId.toString() : "")
                .build();

        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    private Mono<Void> onError(ServerWebExchange exchange, HttpStatus status, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String json = String.format(
                "{\"status\":%d,\"error\":\"%s\",\"message\":\"%s\",\"timestamp\":\"%s\"}",
                status.value(),
                status.getReasonPhrase(),
                message,
                LocalDateTime.now()
        );

        DataBuffer buffer = response.bufferFactory().wrap(json.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -100; // Run early, before proxy routing
    }
}
