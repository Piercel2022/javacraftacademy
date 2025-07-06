
package com.javacraftacademy.gateway.filter;

import com.javacraftacademy.gateway.service.JwtService;
import com.javacraftacademy.gateway.util.ResponseUtil;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

/**
 * Filtre d'authentification pour le Gateway
 * 
 * Ce filtre vérifie la validité des tokens JWT pour les routes protégées
 * et ajoute les informations d'authentification aux en-têtes des requêtes.
 * 
 * @author JavaCraftAcademy Team
 * @version 1.0
 */
@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    private final JwtService jwtService;
    private final ResponseUtil responseUtil;

    // Liste des endpoints publics qui ne nécessitent pas d'authentification
    private final List<String> publicEndpoints = Arrays.asList(
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/refresh",
            "/api/auth/forgot-password",
            "/api/auth/reset-password",
            "/api/courses/public",
            "/api/health",
            "/actuator/**",
            "/static/**",
            "/ws/**"
    );

    public AuthenticationFilter(JwtService jwtService, ResponseUtil responseUtil) {
        super(Config.class);
        this.jwtService = jwtService;
        this.responseUtil = responseUtil;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getPath().value();

            // Vérifier si l'endpoint est public
            if (isPublicEndpoint(path)) {
                return chain.filter(exchange);
            }

            // Extraire le token d'autorisation
            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return responseUtil.createAuthenticationErrorResponse(
                    exchange.getResponse(),
                    "Token d'authentification manquant ou invalide",
                    path
                );
            }

            String token = authHeader.substring(7);

            try {
                // Valider le token
                if (!jwtService.isTokenValid(token)) {
                    return responseUtil.createAuthenticationErrorResponse(
                        exchange.getResponse(),
                        "Token d'authentification expiré ou invalide",
                        path
                    );
                }

                // Extraire les informations de l'utilisateur
                String username = jwtService.extractUsername(token);
                String userId = jwtService.extractUserId(token);
                List<String> roles = jwtService.extractRoles(token);

                // Ajouter les informations d'authentification aux en-têtes
                ServerHttpRequest mutatedRequest = request.mutate()
                        .header("X-User-Id", userId)
                        .header("X-Username", username)
                        .header("X-User-Roles", String.join(",", roles))
                        .header("X-Auth-Token", token)
                        .build();

                // Vérifier les permissions pour les endpoints sensibles
                if (isSensitiveEndpoint(path) && !hasRequiredRole(roles, path)) {
                    return responseUtil.createAuthorizationErrorResponse(
                        exchange.getResponse(),
                        "Permissions insuffisantes pour accéder à cette ressource",
                        path
                    );
                }

                return chain.filter(exchange.mutate().request(mutatedRequest).build());

            } catch (Exception e) {
                return responseUtil.createAuthenticationErrorResponse(
                    exchange.getResponse(),
                    "Erreur lors de la validation du token: " + e.getMessage(),
                    path
                );
            }
        };
    }

    /**
     * Vérifie si un endpoint est public
     * 
     * @param path le chemin de la requête
     * @return true si l'endpoint est public
     */
    private boolean isPublicEndpoint(String path) {
        return publicEndpoints.stream()
                .anyMatch(pattern -> {
                    if (pattern.endsWith("/**")) {
                        return path.startsWith(pattern.substring(0, pattern.length() - 3));
                    }
                    return path.equals(pattern) || path.startsWith(pattern + "/");
                });
    }

    /**
     * Vérifie si un endpoint est sensible et nécessite des permissions spéciales
     * 
     * @param path le chemin de la requête
     * @return true si l'endpoint est sensible
     */
    private boolean isSensitiveEndpoint(String path) {
        return path.startsWith("/api/admin/") || 
               path.contains("/admin/") ||
               path.startsWith("/api/users/") && (path.contains("/delete") || path.contains("/admin")) ||
               path.startsWith("/api/courses/") && (path.contains("/admin") || path.contains("/manage"));
    }

    /**
     * Vérifie si l'utilisateur a les rôles requis pour accéder à un endpoint
     * 
     * @param userRoles les rôles de l'utilisateur
     * @param path le chemin de la requête
     * @return true si l'utilisateur a les permissions nécessaires
     */
    private boolean hasRequiredRole(List<String> userRoles, String path) {
        // Endpoints admin nécessitent le rôle ADMIN
        if (path.startsWith("/api/admin/") || path.contains("/admin/")) {
            return userRoles.contains("ADMIN") || userRoles.contains("SUPER_ADMIN");
        }

        // Endpoints de gestion de cours nécessitent INSTRUCTOR ou ADMIN
        if (path.startsWith("/api/courses/") && path.contains("/manage")) {
            return userRoles.contains("INSTRUCTOR") || 
                   userRoles.contains("ADMIN") || 
                   userRoles.contains("SUPER_ADMIN");
        }

        // Par défaut, tout utilisateur authentifié peut accéder
        return true;
    }

    /**
     * Configuration du filtre
     */
    public static class Config {
        private boolean enabled = true;
        private List<String> excludePaths;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public List<String> getExcludePaths() {
            return excludePaths;
        }

        public void setExcludePaths(List<String> excludePaths) {
            this.excludePaths = excludePaths;
        }
    }
}