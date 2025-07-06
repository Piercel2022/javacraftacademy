package com.javacraftacademy.gateway.config;

import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;

import java.time.Duration;

/**
 * Configuration principale du Gateway
 * 
 * Cette classe contient toutes les configurations nécessaires pour le fonctionnement
 * du gateway, incluant le circuit breaker, le rate limiting, et autres paramètres.
 * 
 * @author JavaCraftAcademy Team
 * @version 1.0
 */
@Configuration
public class GatewayConfig {

    /**
     * Configuration du Circuit Breaker par défaut
     * 
     * @return Customizer pour ReactiveResilience4JCircuitBreakerFactory
     */
    @Bean
    public Customizer<ReactiveResilience4JCircuitBreakerFactory> defaultCustomizer() {
        return factory -> factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
                .circuitBreakerConfig(CircuitBreakerConfig.custom()
                        .slidingWindowSize(10)
                        .permittedNumberOfCallsInHalfOpenState(3)
                        .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.TIME_BASED)
                        .minimumNumberOfCalls(5)
                        .waitDurationInOpenState(Duration.ofSeconds(30))
                        .failureRateThreshold(50.0f)
                        .slowCallRateThreshold(50.0f)
                        .slowCallDurationThreshold(Duration.ofSeconds(2))
                        .build())
                .timeLimiterConfig(TimeLimiterConfig.custom()
                        .timeoutDuration(Duration.ofSeconds(10))
                        .build())
                .build());
    }

    /**
     * Configuration du Rate Limiter par défaut
     * 
     * @return RedisRateLimiter configuré
     */
    @Bean
    public RedisRateLimiter redisRateLimiter() {
        return new RedisRateLimiter(10, 20, 1);
    }

    /**
     * Résolution de clé pour le rate limiting basé sur l'IP
     * 
     * @return KeyResolver basé sur l'adresse IP
     */
    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> {
            String clientIp = getClientIp(exchange);
            return Mono.just(clientIp);
        };
    }

    /**
     * Résolution de clé pour le rate limiting basé sur l'utilisateur authentifié
     * 
     * @return KeyResolver basé sur l'ID utilisateur
     */
    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> exchange.getRequest()
                .getHeaders()
                .getFirst("X-User-Id") != null ? 
                Mono.just(exchange.getRequest().getHeaders().getFirst("X-User-Id")) :
                Mono.just("anonymous");
    }

    /**
     * Résolution de clé pour le rate limiting basé sur le path de l'API
     * 
     * @return KeyResolver basé sur le path
     */
    @Bean
    public KeyResolver pathKeyResolver() {
        return exchange -> {
            String path = exchange.getRequest().getPath().value();
            // Extraire le service du path (ex: /api/users/... -> users)
            String[] pathParts = path.split("/");
            if (pathParts.length >= 3 && "api".equals(pathParts[1])) {
                return Mono.just(pathParts[2]);
            }
            return Mono.just("default");
        };
    }

    /**
     * Rate Limiter spécialisé pour les endpoints sensibles
     * 
     * @return RedisRateLimiter avec des limites plus strictes
     */
    @Bean
    public RedisRateLimiter strictRateLimiter() {
        return new RedisRateLimiter(5, 10, 1);
    }

    /**
     * Rate Limiter pour les endpoints publics
     * 
     * @return RedisRateLimiter avec des limites plus souples
     */
    @Bean
    public RedisRateLimiter publicRateLimiter() {
        return new RedisRateLimiter(50, 100, 1);
    }

    /**
     * Extrait l'adresse IP réelle du client
     * 
     * @param exchange l'échange web
     * @return l'adresse IP du client
     */
    private String getClientIp(ServerWebExchange exchange) {
        String xForwardedFor = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = exchange.getRequest().getHeaders().getFirst("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        String xOriginalForwardedFor = exchange.getRequest().getHeaders().getFirst("X-Original-Forwarded-For");
        if (xOriginalForwardedFor != null && !xOriginalForwardedFor.isEmpty()) {
            return xOriginalForwardedFor.split(",")[0].trim();
        }
        
        return exchange.getRequest().getRemoteAddress() != null ? 
               exchange.getRequest().getRemoteAddress().getAddress().getHostAddress() : 
               "unknown";
    }
}