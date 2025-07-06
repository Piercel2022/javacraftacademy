package com.javacraftacademy.gateway.filter;

import com.javacraftacademy.gateway.util.ResponseUtil;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Filtre de limitation de débit (Rate Limiting) pour le Gateway
 * 
 * Ce filtre implémente une stratégie de rate limiting basée sur l'algorithme
 * de fenêtre glissante utilisant Redis pour le stockage distribué.
 * 
 * @author JavaCraftAcademy Team
 * @version 1.0
 */
@Component
public class RateLimitingFilter extends AbstractGatewayFilterFactory<RateLimitingFilter.Config> {

    private final ReactiveStringRedisTemplate redisTemplate;
    private final ResponseUtil responseUtil;

    // Endpoints avec des limites spéciales
    private final List<String> strictLimitEndpoints = Arrays.asList(
            "/api/auth/login",
            "/api/auth/register",
            "/api/payments/**",
            "/api/admin/**"
    );

    private final List<String> publicEndpoints = Arrays.asList(
            "/api/courses/public",
            "/api/health",
            "/actuator/**",
            "/static/**"
    );

    public RateLimitingFilter(ReactiveStringRedisTemplate redisTemplate, ResponseUtil responseUtil) {
        super(Config.class);
        this.redisTemplate = redisTemplate;
        this.responseUtil = responseUtil;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getPath().value();
            String clientKey = generateClientKey(request);

            // Déterminer les limites selon l'endpoint
            RateLimitConfig limitConfig = getRateLimitConfig(path, config);

            return checkRateLimit(clientKey, limitConfig)
                    .flatMap(allowed -> {
                        if (allowed) {
                            return chain.filter(exchange);
                        } else {
                            return responseUtil.createRateLimitErrorResponse(
                                    exchange.getResponse(),
                                    "Limite de requêtes dépassée. Veuillez réessayer plus tard.",
                                    path,
                                    limitConfig.getWindowSizeSeconds()
                            );
                        }
                    });
        };
    }

    /**
     * Vérifie si la requête est autorisée selon les limites de débit
     *
     * @param clientKey la clé unique du client
     * @param config la configuration des limites
     * @return Mono<Boolean> true si autorisé, false sinon
     */
    private Mono<Boolean> checkRateLimit(String clientKey, RateLimitConfig config) {
        String redisKey = "rate_limit:" + clientKey;
        long currentTime = Instant.now().getEpochSecond();
        long windowStart = currentTime - config.getWindowSizeSeconds();

        return redisTemplate.opsForZSet()
                // Supprimer les entrées expirées
                .removeRangeByScore(redisKey, 0, windowStart)
                .then(redisTemplate.opsForZSet().count(redisKey, windowStart, currentTime))
                .flatMap(currentCount -> {
                    if (currentCount < config.getMaxRequests()) {
                        // Ajouter la requête actuelle
                        return redisTemplate.opsForZSet()
                                .add(redisKey, String.valueOf(currentTime), currentTime)
                                .then(redisTemplate.expire(redisKey, Duration.ofSeconds(config.getWindowSizeSeconds())))
                                .thenReturn(true);
                    } else {
                        return Mono.just(false);
                    }
                })
                .onErrorReturn(true); // En cas d'erreur Redis, permettre la requête
    }

    /**
     * Génère une clé unique pour identifier le client
     *
     * @param request la requête HTTP
     * @return la clé du client
     */
    private String generateClientKey(ServerHttpRequest request) {
        // Priorité 1: ID utilisateur authentifié
        String userId = request.getHeaders().getFirst("X-User-Id");
        if (userId != null && !userId.isEmpty()) {
            return "user:" + userId;
        }

        // Priorité 2: Adresse IP
        String clientIp = getClientIp(request);
        return "ip:" + clientIp;
    }

    /**
     * Extrait l'adresse IP réelle du client
     *
     * @param request la requête HTTP
     * @return l'adresse IP du client
     */
    private String getClientIp(ServerHttpRequest request) {
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeaders().getFirst("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddress() != null ?
                request.getRemoteAddress().getAddress().getHostAddress() :
                "unknown";
    }

    /**
     * Détermine la configuration de rate limiting selon l'endpoint
     *
     * @param path le chemin de la requête
     * @param config la configuration par défaut
     * @return la configuration de rate limiting à appliquer
     */
    private RateLimitConfig getRateLimitConfig(String path, Config config) {
        // Endpoints avec limites strictes
        if (matchesAnyPattern(path, strictLimitEndpoints)) {
            return new RateLimitConfig(5, 300); // 5 requêtes par 5 minutes
        }

        // Endpoints publics avec limites souples
        if (matchesAnyPattern(path, publicEndpoints)) {
            return new RateLimitConfig(100, 60); // 100 requêtes par minute
        }

        // Configuration par défaut
        return new RateLimitConfig(
                config.getMaxRequests(),
                config.getWindowSizeSeconds()
        );
    }

    /**
     * Vérifie si un path correspond à un des patterns
     *
     * @param path le chemin à vérifier
     * @param patterns les patterns à matcher
     * @return true si le path correspond à un pattern
     */
    private boolean matchesAnyPattern(String path, List<String> patterns) {
        return patterns.stream()
                .anyMatch(pattern -> {
                    if (pattern.endsWith("/**")) {
                        return path.startsWith(pattern.substring(0, pattern.length() - 3));
                    }
                    return path.equals(pattern) || path.startsWith(pattern + "/");
                });
    }

    /**
     * Configuration du filtre de Rate Limiting
     */
    public static class Config {
        private int maxRequests = 50; // Nombre maximum de requêtes
        private int windowSizeSeconds = 60; // Taille de la fenêtre en secondes
        private boolean enabled = true;

        public int getMaxRequests() {
            return maxRequests;
        }

        public void setMaxRequests(int maxRequests) {
            this.maxRequests = maxRequests;
        }

        public int getWindowSizeSeconds() {
            return windowSizeSeconds;
        }

        public void setWindowSizeSeconds(int windowSizeSeconds) {
            this.windowSizeSeconds = windowSizeSeconds;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    /**
     * Configuration spécifique pour les limites de débit
     */
    private static class RateLimitConfig {
        private final int maxRequests;
        private final int windowSizeSeconds;

        public RateLimitConfig(int maxRequests, int windowSizeSeconds) {
            this.maxRequests = maxRequests;
            this.windowSizeSeconds = windowSizeSeconds;
        }

        public int getMaxRequests() {
            return maxRequests;
        }

        public int getWindowSizeSeconds() {
            return windowSizeSeconds;
        }
    }
}