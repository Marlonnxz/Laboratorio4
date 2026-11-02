package co.edu.uptc.apigateway.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
public class RedisCacheService {

    private static final Logger log = LoggerFactory.getLogger(RedisCacheService.class);
    private static final String VERSION_KEY = "subjects:cache:version";

    private final ReactiveStringRedisTemplate redisTemplate;

    @Value("${gateway.cache.enabled:true}")
    private boolean cacheEnabled;

    @Value("${gateway.cache.ttl-seconds:180}")
    private long ttlSeconds;

    public RedisCacheService(ReactiveStringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean isCacheEnabled() {
        return cacheEnabled;
    }

    public Mono<String> get(String key) {
        if (!cacheEnabled) {
            return Mono.empty();
        }

        return redisTemplate.opsForValue().get(key)
                .timeout(Duration.ofMillis(800))
                .onErrorResume(e -> {
                    log.warn("Redis GET failed for key {}: {}. Falling back to downstream service.", key, e.getMessage());
                    return Mono.empty();
                });
    }

    public Mono<Boolean> set(String key, String value) {
        if (!cacheEnabled) {
            return Mono.just(false);
        }

        return redisTemplate.opsForValue().set(key, value, Duration.ofSeconds(ttlSeconds))
                .timeout(Duration.ofMillis(800))
                .onErrorResume(e -> {
                    log.warn("Redis SET failed for key {}: {}", key, e.getMessage());
                    return Mono.just(false);
                });
    }

    public Mono<Long> getVersion() {
        if (!cacheEnabled) {
            return Mono.just(1L);
        }

        return redisTemplate.opsForValue().get(VERSION_KEY)
                .map(val -> {
                    try {
                        return Long.parseLong(val);
                    } catch (NumberFormatException e) {
                        return 1L;
                    }
                })
                .defaultIfEmpty(1L)
                .timeout(Duration.ofMillis(800))
                .onErrorResume(e -> {
                    log.warn("Redis getVersion failed: {}. Using default version 1.", e.getMessage());
                    return Mono.just(1L);
                });
    }

    public Mono<Long> invalidateSubjectsCache() {
        if (!cacheEnabled) {
            return Mono.just(0L);
        }

        log.info("Invalidating subjects cache: incrementing cache version in Redis");
        return redisTemplate.opsForValue().increment(VERSION_KEY)
                .timeout(Duration.ofMillis(800))
                .doOnSuccess(newVer -> log.info("Subjects cache invalidated. New version: {}", newVer))
                .onErrorResume(e -> {
                    log.warn("Redis invalidateSubjectsCache failed: {}", e.getMessage());
                    return Mono.just(0L);
                });
    }
}
