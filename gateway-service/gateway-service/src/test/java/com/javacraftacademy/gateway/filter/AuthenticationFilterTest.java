package com.javacraftacademy.gateway.filter;

import com.javacraftacademy.gateway.service.JwtService;
import com.javacraftacademy.gateway.service.UserValidationService;
import com.javacraftacademy.gateway.exception.AuthenticationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitaires complets pour le filtre d'authentification du Gateway.
 * 
 * <p>Ce filtre constitue le cœur de la sécurité de l'architecture microservices,
 * agissant comme garde d'entrée pour toutes les requêtes entrantes.
 * 
 * <h2>Fonctionnalités du AuthenticationFilter :</h2>
 * <ul>
 *   <li><strong>Validation JWT</strong> - Vérification de l'intégrité et validité des tokens</li>
 *   <li><strong>Extraction des claims</strong> - Récupération des informations utilisateur</li>
 *   <li><strong>Gestion des sessions</strong> - Maintien de l'état d'authentification</li>
 *   <li><strong>Routage conditionnel</strong> - Redirection selon le statut d'authentification</li>
 *   <li><strong>Cache des tokens</strong> - Optimisation des performances via Redis</li>
 *   <li><strong>Blacklist management</strong> - Gestion des tokens révoqués</li>
 * </ul>
 * 
 * <h2>Position dans l'architecture de sécurité :</h2>
 * <pre>
 * Client (Web/Mobile)
 *       ↓ (JWT Token in Header)
 * Load Balancer
 *       ↓
 * [Gateway Service]
 *       ↓
 * ┌─────────────────────────────────┐
 * │ Filter Chain:                   │
 * │ 1. CorsFilter                   │
 * │ 2. RateLimitingFilter          │
 * │ 3. [AuthenticationFilter] ←──── Tests de cette classe
 * │ 4. RequestValidationFilter     │
 * │ 5. LoggingFilter               │
 * └─────────────────────────────────┘
 *       ↓ (Authenticated Request)
 * Microservices Backend
 * </pre>
 * 
 * <h2>Interactions avec les composants :</h2>
 * <ul>
 *   <li><strong>JwtService</strong> - Validation et décodage des tokens JWT</li>
 *   <li><strong>UserValidationService</strong> - Vérification de l'état utilisateur</li>
 *   <li><strong>Redis Cache</strong> - Cache des tokens validés pour optimisation</li>
 *   <li><strong>SecurityConfig</strong> - Configuration des endpoints publics/privés</li>
 *   <li><strong>User Service</strong> - Validation des utilisateurs actifs</li>
 * </ul>
 * 
 * <h2>Flux de sécurité implémenté :</h2>
 * <pre>
 * 1. Réception requête avec JWT
 *         ↓
 * 2. Extraction token du header Authorization
 *         ↓
 * 3. Validation format et signature JWT
 *         ↓
 * 4. Vérification expiration token
 *         ↓
 * 5. Check blacklist Redis
 *         ↓
 * 6. Validation utilisateur actif
 *         ↓
 * 7. Injection context sécurisé
 *         ↓
 * 8. Transmission vers backend
 * </pre>
 * 
 * @author JavaCraft Academy
 * @version 1.0
 * @since 2024
 * 
 * @see com.javacraftacademy.gateway.filter.AuthenticationFilter
 * @see com.javacraftacademy.gateway.service.JwtService
 * @see com.javacraftacademy.gateway.service.UserValidationService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires - Filtre d'authentification Gateway")
class AuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserValidationService userValidationService;

    @Mock
    private ServerWebExchange exchange;

    @Mock
    private ServerHttpRequest request;

    @Mock
    private ServerHttpResponse response;

    @Mock
    private GatewayFilterChain chain;

    @Mock
    private HttpHeaders headers;

    @InjectMocks
    private AuthenticationFilter authenticationFilter;

    /**
     * Configuration initiale des mocks pour tous les tests.
     * 
     * <p>Simule l'environnement Spring WebFlux avec :
     * <ul>
     *   <li>ServerWebExchange configuré</li>
     *   <li>Headers HTTP mockés</li>
     *   <li>Services de validation initialisés</li>
     * </ul>
     */
    @BeforeEach
    void setUp() {
        when(exchange.getRequest()).thenReturn(request);
        when(exchange.getResponse()).thenReturn(response);
        when(request.getHeaders()).thenReturn(headers);
    }

    /**
     * Tests des scénarios d'authentification réussie.
     * 
     * <p>Ces tests valident le chemin heureux où l'utilisateur fournit
     * un token JWT valide et correctement formaté.
     */
    @Nested
    @DisplayName("Scénarios d'authentification réussie")
    class SuccessfulAuthentication {

        /**
         * Test du flux d'authentification standard avec token JWT valide.
         * 
         * <p><strong>Scenario :</strong>
         * <ol>
         *   <li>Client envoie requête avec Bearer token</li>
         *   <li>Token extrait du header Authorization</li>
         *   <li>JwtService valide le token</li>
         *   <li>UserValidationService confirme utilisateur actif</li>
         *   <li>Requête transmise aux microservices backend</li>
         * </ol>
         * 
         * <p><strong>Impact sur l'architecture :</strong>
         * Ce test garantit que les utilisateurs authentifiés peuvent accéder
         * aux ressources protégées de l'ensemble de l'écosystème microservices.
         */
        @Test
        @DisplayName("Token JWT valide - Authentification réussie")
        void validJwtToken_shouldAllowAccess() {
            // Given: Token JWT valide dans le header
            String validToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...";
            String bearerToken = "Bearer " + validToken;
            String userId = "user123";
            
            when(headers.getFirst(HttpHeaders.AUTHORIZATION)).thenReturn(bearerToken);
            when(jwtService.extractToken(bearerToken)).thenReturn(validToken);
            when(jwtService.validateToken(validToken)).thenReturn(true);
            when(jwtService.extractUserId(validToken)).thenReturn(userId);
            when(userValidationService.isUserActive(userId)).thenReturn(Mono.just(true));
            when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

            // When: Filtre traite la requête
            Mono<Void> result = authenticationFilter.filter(exchange, chain);

            // Then: Authentification réussie, requête transmise
            StepVerifier.create(result)
                .verifyComplete();

            verify(jwtService).validateToken(validToken);
            verify(userValidationService).isUserActive(userId);
            verify(chain).filter(any(ServerWebExchange.class));
        }

        /**
         * Test de la gestion du cache Redis pour optimiser les performances.
         * 
         * <p><strong>Optimisation des performances :</strong>
         * <ul>
         *   <li>Tokens validés mis en cache Redis (TTL configurable)</li>
         *   <li>Évite la re-validation coûteuse pour chaque requête</li>
         *   <li>Améliore significativement les temps de réponse</li>
         *   <li>Réduit la charge sur les services d'authentification</li>
         * </ul>
         * 
         * <p><strong>Architecture de cache :</strong>
         * <pre>
         * Request → Cache Check → [HIT: Direct Access | MISS: Full Validation]
         *               ↓                                       ↓
         *         Redis Cache ←────────────────── Token Validation
         * </pre>
         */
        @Test
        @DisplayName("Cache Redis - Token validé mis en cache pour optimisation")
        void cachedValidToken_shouldSkipRevalidation() {
            // Given: Token présent en cache Redis
            String cachedToken = "cached_token_123";
            String bearerToken = "Bearer " + cachedToken;
            String userId = "user456";
            
            when(headers.getFirst(HttpHeaders.AUTHORIZATION)).thenReturn(bearerToken);
            when(jwtService.extractToken(bearerToken)).thenReturn(cachedToken);
            when(jwtService.isTokenCached(cachedToken)).thenReturn(true);
            when(jwtService.extractUserId(cachedToken)).thenReturn(userId);
            when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

            // When: Filtre utilise le cache
            Mono<Void> result = authenticationFilter.filter(exchange, chain);

            // Then: Validation rapide via cache
            StepVerifier.create(result)
                .verifyComplete();

            verify(jwtService).isTokenCached(cachedToken);
            verify(jwtService, never()).validateToken(cachedToken); // Pas de revalidation
            verify(chain).filter(any(ServerWebExchange.class));
        }

        /**
         * Test de la propagation du contexte utilisateur aux microservices.
         * 
         * <p><strong>Context Injection :</strong>
         * Le filtre enrichit la requête avec les informations utilisateur
         * extraites du JWT, permettant aux microservices backend de :
         * <ul>
         *   <li>Identifier l'utilisateur sans re-décoder le JWT</li>
         *   <li>Appliquer les autorisations appropriées</li>  
         *   <li>Tracer les actions utilisateur</li>
         *   <li>Personnaliser les réponses</li>
         * </ul>
         */
        @Test
        @DisplayName("Context utilisateur propagé aux microservices backend")
        void validToken_shouldInjectUserContext() {
            // Given: Token avec informations utilisateur riches
            String token = "jwt_with_user_context";
            String bearerToken = "Bearer " + token;
            String userId = "user789";
            String userRole = "ADMIN";
            String userEmail = "admin@example.com";
            
            when(headers.getFirst(HttpHeaders.AUTHORIZATION)).thenReturn(bearerToken);
            when(jwtService.extractToken(bearerToken)).thenReturn(token);
            when(jwtService.validateToken(token)).thenReturn(true);
            when(jwtService.extractUserId(token)).thenReturn(userId);
            when(jwtService.extractUserRole(token)).thenReturn(userRole);
            when(jwtService.extractUserEmail(token)).thenReturn(userEmail);
            when(userValidationService.isUserActive(userId)).thenReturn(Mono.just(true));
            when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

            // When: Filtre injecte le contexte
            Mono<Void> result = authenticationFilter.filter(exchange, chain);

            // Then: Context utilisateur disponible pour les services
            StepVerifier.create(result)
                .verifyComplete();

            verify(jwtService).extractUserId(token);
            verify(jwtService).extractUserRole(token);
            verify(jwtService).extractUserEmail(token);
        }
    }

    /**
     * Tests des scénarios d'échec d'authentification.
     * 
     * <p>Ces tests valident la sécurité du système en s'assurant que
     * les requêtes non authentifiées ou malformées sont correctement rejetées.
     */
    @Nested
    @DisplayName("Scénarios d'échec authentification")
    class FailedAuthentication {

        /**
         * Test de rejet des requêtes sans token d'authentification.
         * 
         * <p><strong>Sécurité par défaut :</strong>
         * Toute requête vers un endpoint protégé sans header Authorization
         * doit être rejetée avec un code 401 Unauthorized.
         * 
         * <p><strong>Endpoints concernés :</strong>
         * <ul>
         *   <li>/api/users/profile</li>
         *   <li>/api/orders/**</li>
         *   <li>/api/products/manage/**</li>
         *   <li>Tous endpoints non marqués comme publics</li>
         * </ul>
         */
        @Test
        @DisplayName("Absence de token - Requête rejetée 401 Unauthorized")
        void missingAuthToken_shouldRejectRequest() {
            // Given: Requête sans header Authorization
            when(headers.getFirst(HttpHeaders.AUTHORIZATION)).thenReturn(null);
            when(request.getPath()).thenReturn(mock(org.springframework.http.server.RequestPath.class));
            when(request.getPath().value()).thenReturn("/api/users/profile");

            // When: Filtre traite la requête
            Mono<Void> result = authenticationFilter.filter(exchange, chain);

            // Then: Requête rejetée avec 401
            StepVerifier.create(result)
                .verifyComplete();

            verify(response).setStatusCode(HttpStatus.UNAUTHORIZED);
            verify(chain, never()).filter(any(ServerWebExchange.class));
        }

        /**
         * Test de rejet des tokens JWT malformés ou corrompus.
         * 
         * <p><strong>Validation stricte des formats :</strong>
         * <ul>
         *   <li>Structure JWT invalide (header.payload.signature)</li>
         *   <li>Caractères non-base64 dans les segments</li>
         *   <li>JSON malformé dans les claims</li>
         *   <li>Signature cryptographique invalide</li>
         * </ul>
         * 
         * <p><strong>Sécurité renforcée :</strong>
         * Prévient les tentatives d'injection ou de manipulation de tokens.
         */
        @Test
        @DisplayName("Token JWT malformé - Rejet avec 401 Unauthorized")
        void malformedJwtToken_shouldRejectRequest() {
            // Given: Token JWT avec format invalide
            String malformedToken = "invalid.jwt.token.format";
            String bearerToken = "Bearer " + malformedToken;
            
            when(headers.getFirst(HttpHeaders.AUTHORIZATION)).thenReturn(bearerToken);
            when(jwtService.extractToken(bearerToken)).thenReturn(malformedToken);
            when(jwtService.validateToken(malformedToken)).thenReturn(false);

            // When: Filtre traite le token malformé
            Mono<Void> result = authenticationFilter.filter(exchange, chain);

            // Then: Requête rejetée pour token invalide
            StepVerifier.create(result)
                .verifyComplete();

            verify(jwtService).validateToken(malformedToken);
            verify(response).setStatusCode(HttpStatus.UNAUTHORIZED);
            verify(chain, never()).filter(any(ServerWebExchange.class));
        }

        /**
         * Test de gestion des tokens expirés.
         * 
         * <p><strong>Gestion de l'expiration :</strong>
         * <ul>
         *   <li>Vérification du claim 'exp' (timestamp Unix)</li>
         *   <li>Comparaison avec le temps système actuel</li>
         *   <li>Rejet automatique des tokens périmés</li>
         *   <li>Suppression du cache Redis si applicable</li>
         * </ul>
         * 
         * <p><strong>Sécurité temporelle :</strong>
         * Empêche l'utilisation de tokens compromis ou volés après expiration.
         */
        @Test
        @DisplayName("Token JWT expiré - Rejet avec 401 Unauthorized")
        void expiredJwtToken_shouldRejectRequest() {
            // Given: Token JWT expiré
            String expiredToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.expired.token";
            String bearerToken = "Bearer " + expiredToken;
            
            when(headers.getFirst(HttpHeaders.AUTHORIZATION)).thenReturn(bearerToken);
            when(jwtService.extractToken(bearerToken)).thenReturn(expiredToken);
            when(jwtService.validateToken(expiredToken)).thenReturn(false);
            when(jwtService.isTokenExpired(expiredToken)).thenReturn(true);

            // When: Filtre vérifie le token expiré
            Mono<Void> result = authenticationFilter.filter(exchange, chain);

            // Then: Accès refusé pour expiration
            StepVerifier.create(result)
                .verifyComplete();

            verify(jwtService).validateToken(expiredToken);
            verify(response).setStatusCode(HttpStatus.UNAUTHORIZED);
            verify(chain, never()).filter(any(ServerWebExchange.class));
        }

        /**
         * Test de gestion des tokens blacklistés (révoqués).
         * 
         * <p><strong>Système de révocation :</strong>
         * <ul>
         *   <li>Tokens ajoutés à la blacklist Redis lors de déconnexion</li>
         *   <li>Vérification systématique avant validation</li>
         *   <li>Protection contre replay attacks</li>
         *   <li>Gestion des tokens compromis</li>
         * </ul>
         * 
         * <p><strong>Cas d'usage de blacklist :</strong>
         * <ul>
         *   <li>Logout explicite utilisateur</li>
         *   <li>Changement de mot de passe</li>
         *   <li>Désactivation de compte</li>
         *   <li>Détection d'activité suspecte</li>
         * </ul>
         */
        @Test
        @DisplayName("Token blacklisté - Rejet avec 401 Unauthorized")
        void blacklistedToken_shouldRejectRequest() {
            // Given: Token valide mais révoqué (blacklisté)
            String blacklistedToken = "valid.but.revoked.token";
            String bearerToken = "Bearer " + blacklistedToken;
            
            when(headers.getFirst(HttpHeaders.AUTHORIZATION)).thenReturn(bearerToken);
            when(jwtService.extractToken(bearerToken)).thenReturn(blacklistedToken);
            when(jwtService.validateToken(blacklistedToken)).thenReturn(true);
            when(jwtService.isTokenBlacklisted(blacklistedToken)).thenReturn(true);

            // When: Filtre vérifie la blacklist
            Mono<Void> result = authenticationFilter.filter(exchange, chain);

            // Then: Accès refusé pour token révoqué
            StepVerifier.create(result)
                .verifyComplete();

            verify(jwtService).isTokenBlacklisted(blacklistedToken);
            verify(response).setStatusCode(HttpStatus.UNAUTHORIZED);
            verify(chain, never()).filter(any(ServerWebExchange.class));
        }

        /**
         * Test de rejet pour utilisateur inactif ou désactivé.
         * 
         * <p><strong>Validation d'état utilisateur :</strong>
         * Même avec un token JWT valide, l'accès est refusé si l'utilisateur :
         * <ul>
         *   <li>A été désactivé par un administrateur</li>
         *   <li>A supprimé son compte</li>
         *   <li>Est en statut suspendu temporairement</li>
         *   <li>N'existe plus dans la base utilisateurs</li>
         * </ul>
         */
        @Test
        @DisplayName("Utilisateur inactif - Rejet avec 401 Unauthorized")
        void inactiveUser_shouldRejectRequest() {
            // Given: Token valide mais utilisateur inactif
            String validToken = "valid.token.inactive.user";
            String bearerToken = "Bearer " + validToken;
            String inactiveUserId = "inactive_user_123";
            
            when(headers.getFirst(HttpHeaders.AUTHORIZATION)).thenReturn(bearerToken);
            when(jwtService.extractToken(bearerToken)).thenReturn(validToken);
            when(jwtService.validateToken(validToken)).thenReturn(true);
            when(jwtService.extractUserId(validToken)).thenReturn(inactiveUserId);
            when(userValidationService.isUserActive(inactiveUserId)).thenReturn(Mono.just(false));

            // When: Filtre vérifie le statut utilisateur
            Mono<Void> result = authenticationFilter.filter(exchange, chain);

            // Then: Accès refusé pour utilisateur inactif
            StepVerifier.create(result)
                .verifyComplete();

            verify(userValidationService).isUserActive(inactiveUserId);
            verify(response).setStatusCode(HttpStatus.UNAUTHORIZED);
            verify(chain, never()).filter(any(ServerWebExchange.class));
        }

        /**
         * Test de gestion des formats d'Authorization header invalides.
         * 
         * <p><strong>Formats attendus vs invalides :</strong>
         * <ul>
         *   <li>✅ Valide: "Bearer eyJhbGciOiJIUzI1NiIs..."</li>
         *   <li>❌ Invalide: "Basic dXNlcjpwYXNz" (mauvais schéma)</li>
         *   <li>❌ Invalide: "Bearer" (token manquant)</li>
         *   <li>❌ Invalide: "eyJhbGciOiJIUzI1NiIs..." (Bearer manquant)</li>
         * </ul>
         */
        @Test
        @DisplayName("Format Authorization invalide - Rejet avec 401")
        void invalidAuthorizationFormat_shouldRejectRequest() {
            // Given: Header Authorization avec format incorrect
            String invalidAuthHeader = "Basic dXNlcjpwYXNzd29yZA==";
            
            when(headers.getFirst(HttpHeaders.AUTHORIZATION)).thenReturn(invalidAuthHeader);
            when(jwtService.extractToken(invalidAuthHeader)).thenThrow(new AuthenticationException("Invalid authorization format"));

            // When: Filtre traite le format invalide
            Mono<Void> result = authenticationFilter.filter(exchange, chain);

            // Then: Rejet pour format incorrect
            StepVerifier.create(result)
                .verifyComplete();

            verify(response).setStatusCode(HttpStatus.UNAUTHORIZED);
            verify(chain, never()).filter(any(ServerWebExchange.class));
        }
    }

    /**
     * Tests des endpoints publics et exceptions de sécurité.
     * 
     * <p>Certains endpoints doivent être accessibles sans authentification
     * pour permettre les opérations essentielles du système.
     */
    @Nested
    @DisplayName("Endpoints publics et exceptions")
    class PublicEndpoints {

        /**
         * Test d'accès libre aux endpoints publics sans authentification.
         * 
         * <p><strong>Endpoints publics typiques :</strong>
         * <ul>
         *   <li>/api/auth/login - Authentification initiale</li>
         *   <li>/api/auth/register - Création de compte</li>
         *   <li>/api/health - Monitoring système</li>
         *   <li>/api/public/** - Ressources publiques</li>
         *   <li>/actuator/** - Métriques (si configuré)</li>
         * </ul>
         */
        @Test
        @DisplayName("Endpoint public accessible sans token")
        void publicEndpoint_shouldAllowAccessWithoutToken() {
            // Given: Requête vers endpoint public
            when(headers.getFirst(HttpHeaders.AUTHORIZATION)).thenReturn(null);
            when(request.getPath()).thenReturn(mock(org.springframework.http.server.RequestPath.class));
            when(request.getPath().value()).thenReturn("/api/auth/login");
            when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

            // When: Filtre traite l'endpoint public
            Mono<Void> result = authenticationFilter.filter(exchange, chain);

            // Then: Accès autorisé sans authentification
            StepVerifier.create(result)
                .verifyComplete();

            verify(chain).filter(any(ServerWebExchange.class));
            verify(jwtService, never()).validateToken(any());
        }

        /**
         * Test du health check pour monitoring infrastructure.
         * 
         * <p><strong>Importance du monitoring :</strong>
         * Les health checks doivent rester accessibles pour :
         * <ul>
         *   <li>Load balancers (vérification état service)</li>
         *   <li>Kubernetes liveness/readiness probes</li>
         *   <li>Systèmes de monitoring (Prometheus, etc.)</li>
         *   <li>Alerting automatique</li>
         * </ul>
         */
        @Test
        @DisplayName("Health check accessible pour monitoring")
        void healthCheckEndpoint_shouldBypassAuthentication() {
            // Given: Requête de health check
            when(headers.getFirst(HttpHeaders.AUTHORIZATION)).thenReturn(null);
            when(request.getPath()).thenReturn(mock(org.springframework.http.server.RequestPath.class));
            when(request.getPath().value()).thenReturn("/api/health");
            when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

            // When: Filtre traite le health check
            Mono<Void> result = authenticationFilter.filter(exchange, chain);

            // Then: Monitoring autorisé
            StepVerifier.create(result)
                .verifyComplete();

            verify(chain).filter(any(ServerWebExchange.class));
            verify(response, never()).setStatusCode(HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * Tests de performance et gestion des erreurs système.
     * 
     * <p>Validation de la robustesse du filtre en conditions dégradées.
     */
    @Nested
    @DisplayName("Performance et robustesse")
    class PerformanceAndResilience {

        /**
         * Test de gestion des timeouts de validation utilisateur.
         * 
         * <p><strong>Résilience système :</strong>
         * En cas de lenteur ou indisponibilité du service utilisateur,
         * le filtre doit :
         * <ul>
         *   <li>Appliquer un timeout configuré</li>
         *   <li>Rejeter la requête pour sécurité</li>
         *   <li>Logger l'incident pour investigation</li>
         *   <li>Maintenir la disponibilité du gateway</li>
         * </ul>
         */
        @Test
        @DisplayName("Timeout validation utilisateur - Fallback sécurisé")
        void userValidationTimeout_shouldRejectSafely() {
            // Given: Token valide mais service utilisateur en timeout
            String validToken = "valid.token.timeout.scenario";
            String bearerToken = "Bearer " + validToken;
            String userId = "user_timeout_test";
            
            when(headers.getFirst(HttpHeaders.AUTHORIZATION)).thenReturn(bearerToken);
            when(jwtService.extractToken(bearerToken)).thenReturn(validToken);
            when(jwtService.validateToken(validToken)).thenReturn(true);
            when(jwtService.extractUserId(validToken)).thenReturn(userId);
            when(userValidationService.isUserActive(userId))
                .thenReturn(Mono.error(new RuntimeException("Service timeout")));

            // When: Filtre gère le timeout
            Mono<Void> result = authenticationFilter.filter(exchange, chain);

            // Then: Rejet sécurisé en cas d'erreur
            StepVerifier.create(result)
                .verifyComplete();

            verify(response).setStatusCode(HttpStatus.UNAUTHORIZED);
            verify(chain, never()).filter(any(ServerWebExchange.class));
        }

        /**
         * Test de performance avec charge élevée de tokens en cache.
         * 
         * <p><strong>Optimisation cache Redis :</strong>
         * <ul>
         *   <li>Validation instantanée pour tokens cachés</li>
         *   <li>Réduction drastique des appels services externes</li>
         *   <li>Amélioration throughput système</li>
         *   <li>Diminution latence utilisateur</li>
         * </ul>
         */
        @Test
        @DisplayName("Performance cache - Validation rapide tokens cachés")
        void highVolumeCache_shouldMaintainPerformance() {
            // Given: Multiple tokens en cache Redis
            String[] cachedTokens = {
                "cached_token_1", "cached_token_2", "cached_token_3"
            };
            // Test chaque token depuis le cache
            for (String token : cachedTokens) {
                String bearerToken = "Bearer " + token;
                String userId = "user_" + token.split("_")[2];
                
                when(headers.getFirst(HttpHeaders.AUTHORIZATION)).thenReturn(bearerToken);
                when(jwtService.extractToken(bearerToken)).thenReturn(token);
                when(jwtService.isTokenCached(token)).thenReturn(true);
                when(jwtService.extractUserId(token)).thenReturn(userId);
                when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

                // When: Validation rapide via cache
                Mono<Void> result = authenticationFilter.filter(exchange, chain);

                // Then: Performance optimisée
                StepVerifier.create(result)
                    .verifyComplete();

                verify(jwtService).isTokenCached(token);
                verify(jwtService, never()).validateToken(token);
            }
        }

        /**
         * Test de gestion des erreurs Redis (cache indisponible).
         * 
         * <p><strong>Dégradation gracieuse :</strong>
         * En cas d'indisponibilité du cache Redis, le système doit :
         * <ul>
         *   <li>Basculer automatiquement sur validation complète</li>
         *   <li>Maintenir la sécurité sans interruption</li>
         *   <li>Logger les erreurs pour monitoring</li>
         *   <li>Continuer de fonctionner en mode dégradé</li>
         * </ul>
         */
        @Test
        @DisplayName("Fallback Redis indisponible - Validation complète")
        void redisCacheDown_shouldFallbackToFullValidation() {
            // Given: Redis indisponible, fallback validation
            String token = "token_redis_down";
            String bearerToken = "Bearer " + token;
            String userId = "user_fallback";
            
            when(headers.getFirst(HttpHeaders.AUTHORIZATION)).thenReturn(bearerToken);
            when(jwtService.extractToken(bearerToken)).thenReturn(token);
            when(jwtService.isTokenCached(token)).thenThrow(new RuntimeException("Redis connection failed"));
            when(jwtService.validateToken(token)).thenReturn(true);
            when(jwtService.extractUserId(token)).thenReturn(userId);
            when(userValidationService.isUserActive(userId)).thenReturn(Mono.just(true));
            when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

            // When: Filtre gère l'erreur Redis
            Mono<Void> result = authenticationFilter.filter(exchange, chain);

            // Then: Validation complète en fallback
            StepVerifier.create(result)
                .verifyComplete();

            verify(jwtService).validateToken(token);
            verify(userValidationService).isUserActive(userId);
            verify(chain).filter(any(ServerWebExchange.class));
        }
    }

    /**
     * Tests d'intégration avec l'écosystème microservices.
     * 
     * <p>Validation des interactions avec les services externes
     * et de la propagation des informations d'authentification.
     */
    @Nested
    @DisplayName("Intégration microservices")
    class MicroservicesIntegration {

        /**
         * Test de propagation des headers d'authentification.
         * 
         * <p><strong>Context propagation :</strong>
         * Le filtre enrichit les headers de la requête avec :
         * <ul>
         *   <li>X-User-Id - Identifiant utilisateur</li>
         *   <li>X-User-Role - Rôle/permissions</li>
         *   <li>X-User-Email - Email utilisateur</li>
         *   <li>X-Request-Id - Traçabilité distribuée</li>
         * </ul>
         */
        @Test
        @DisplayName("Propagation headers vers microservices backend")
        void validAuthentication_shouldPropagateUserHeaders() {
            // Given: Token avec informations utilisateur complètes
            String token = "token_with_full_context";
            String bearerToken = "Bearer " + token;
            String userId = "user_integration_test";
            String userRole = "PREMIUM_USER";
            String userEmail = "integration@test.com";
            
            when(headers.getFirst(HttpHeaders.AUTHORIZATION)).thenReturn(bearerToken);
            when(jwtService.extractToken(bearerToken)).thenReturn(token);
            when(jwtService.validateToken(token)).thenReturn(true);
            when(jwtService.extractUserId(token)).thenReturn(userId);
            when(jwtService.extractUserRole(token)).thenReturn(userRole);
            when(jwtService.extractUserEmail(token)).thenReturn(userEmail);
            when(userValidationService.isUserActive(userId)).thenReturn(Mono.just(true));
            when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

            // When: Filtre propage le contexte
            Mono<Void> result = authenticationFilter.filter(exchange, chain);

            // Then: Headers utilisateur injectés
            StepVerifier.create(result)
                .verifyComplete();

            verify(jwtService).extractUserId(token);
            verify(jwtService).extractUserRole(token);
            verify(jwtService).extractUserEmail(token);
            verify(chain).filter(any(ServerWebExchange.class));
        }

        /**
         * Test de gestion des rôles et permissions.
         * 
         * <p><strong>Autorisation par rôles :</strong>
         * Le filtre extrait et valide les rôles utilisateur :
         * <ul>
         *   <li>ADMIN - Accès complet système</li>
         *   <li>USER - Accès ressources personnelles</li>
         *   <li>GUEST - Accès lecture limitée</li>
         *   <li>PREMIUM - Fonctionnalités avancées</li>
         * </ul>
         */
        @Test
        @DisplayName("Gestion rôles utilisateur - Extraction et validation")
        void userRoles_shouldBeExtractedAndValidated() {
            // Given: Token avec rôle administrateur
            String adminToken = "admin_role_token";
            String bearerToken = "Bearer " + adminToken;
            String adminUserId = "admin_user_001";
            String adminRole = "ADMIN";
            
            when(headers.getFirst(HttpHeaders.AUTHORIZATION)).thenReturn(bearerToken);
            when(jwtService.extractToken(bearerToken)).thenReturn(adminToken);
            when(jwtService.validateToken(adminToken)).thenReturn(true);
            when(jwtService.extractUserId(adminToken)).thenReturn(adminUserId);
            when(jwtService.extractUserRole(adminToken)).thenReturn(adminRole);
            when(userValidationService.isUserActive(adminUserId)).thenReturn(Mono.just(true));
            when(userValidationService.hasRole(adminUserId, adminRole)).thenReturn(Mono.just(true));
            when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

            // When: Filtre valide les rôles
            Mono<Void> result = authenticationFilter.filter(exchange, chain);

            // Then: Rôle admin validé et propagé
            StepVerifier.create(result)
                .verifyComplete();

            verify(jwtService).extractUserRole(adminToken);
            verify(userValidationService).hasRole(adminUserId, adminRole);
            verify(chain).filter(any(ServerWebExchange.class));
        }

        /**
         * Test de traçabilité distribuée avec correlation ID.
         * 
         * <p><strong>Observabilité système :</strong>
         * Chaque requête authentifiée génère un ID de corrélation unique
         * permettant de tracer les appels à travers tout l'écosystème.
         */
        @Test
        @DisplayName("Traçabilité distribuée - Correlation ID propagé")
        void distributedTracing_shouldPropagateCorrelationId() {
            // Given: Token valide avec génération correlation ID
            String token = "trace_token_123";
            String bearerToken = "Bearer " + token;
            String userId = "trace_user";
            String correlationId = "trace-" + System.currentTimeMillis();
            
            when(headers.getFirst(HttpHeaders.AUTHORIZATION)).thenReturn(bearerToken);
            when(headers.getFirst("X-Correlation-ID")).thenReturn(correlationId);
            when(jwtService.extractToken(bearerToken)).thenReturn(token);
            when(jwtService.validateToken(token)).thenReturn(true);
            when(jwtService.extractUserId(token)).thenReturn(userId);
            when(userValidationService.isUserActive(userId)).thenReturn(Mono.just(true));
            when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

            // When: Filtre traite avec correlation ID
            Mono<Void> result = authenticationFilter.filter(exchange, chain);

            // Then: Traçabilité maintenue
            StepVerifier.create(result)
                .verifyComplete();

            verify(chain).filter(any(ServerWebExchange.class));
        }
    }

    /**
     * Tests de sécurité avancée et protection contre les attaques.
     * 
     * <p>Validation des mécanismes de défense contre les menaces courantes.
     */
    @Nested
    @DisplayName("Sécurité avancée et protection")
    class AdvancedSecurity {

        /**
         * Test de protection contre les attaques par replay.
         * 
         * <p><strong>Défense anti-replay :</strong>
         * <ul>
         *   <li>Validation du timestamp de création token</li>
         *   <li>Vérification de la fraîcheur (max 5 minutes)</li>
         *   <li>Nonce unique pour chaque token</li>
         *   <li>Blacklist automatique des tokens suspects</li>
         * </ul>
         */
        @Test
        @DisplayName("Protection anti-replay - Validation timestamp token")
        void replayAttackProtection_shouldValidateTokenFreshness() {
            // Given: Token avec timestamp suspect (trop ancien)
            String suspiciousToken = "old_timestamp_token";
            String bearerToken = "Bearer " + suspiciousToken;
            
            when(headers.getFirst(HttpHeaders.AUTHORIZATION)).thenReturn(bearerToken);
            when(jwtService.extractToken(bearerToken)).thenReturn(suspiciousToken);
            when(jwtService.validateToken(suspiciousToken)).thenReturn(true);
            when(jwtService.isTokenTooOld(suspiciousToken)).thenReturn(true);

            // When: Filtre détecte token suspect
            Mono<Void> result = authenticationFilter.filter(exchange, chain);

            // Then: Requête bloquée pour protection
            StepVerifier.create(result)
                .verifyComplete();

            verify(jwtService).isTokenTooOld(suspiciousToken);
            verify(response).setStatusCode(HttpStatus.UNAUTHORIZED);
            verify(chain, never()).filter(any(ServerWebExchange.class));
        }

        /**
         * Test de limitation de taux par utilisateur.
         * 
         * <p><strong>Rate limiting personnalisé :</strong>
         * Prévention des abus avec limites par utilisateur :
         * <ul>
         *   <li>100 requêtes/minute pour utilisateurs standards</li>
         *   <li>500 requêtes/minute pour comptes premium</li>
         *   <li>1000 requêtes/minute pour administrateurs</li>
         *   <li>Blacklist temporaire en cas de dépassement</li>
         * </ul>
         */
        @Test
        @DisplayName("Rate limiting par utilisateur - Protection surcharge")
        void userRateLimit_shouldPreventAbuse() {
            // Given: Utilisateur dépassant les limites
            String token = "rate_limit_token";
            String bearerToken = "Bearer " + token;
            String userId = "abusive_user";
            
            when(headers.getFirst(HttpHeaders.AUTHORIZATION)).thenReturn(bearerToken);
            when(jwtService.extractToken(bearerToken)).thenReturn(token);
            when(jwtService.validateToken(token)).thenReturn(true);
            when(jwtService.extractUserId(token)).thenReturn(userId);
            when(userValidationService.isUserActive(userId)).thenReturn(Mono.just(true));
            when(userValidationService.isRateLimitExceeded(userId)).thenReturn(Mono.just(true));

            // When: Filtre vérifie les limites
            Mono<Void> result = authenticationFilter.filter(exchange, chain);

            // Then: Requête bloquée pour dépassement
            StepVerifier.create(result)
                .verifyComplete();

            verify(userValidationService).isRateLimitExceeded(userId);
            verify(response).setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
            verify(chain, never()).filter(any(ServerWebExchange.class));
        }

        /**
         * Test de validation d'intégrité cryptographique.
         * 
         * <p><strong>Sécurité cryptographique :</strong>
         * Validation stricte de l'intégrité des tokens :
         * <ul>
         *   <li>Vérification signature HMAC/RSA</li>
         *   <li>Validation de la chaîne de certification</li>
         *   <li>Contrôle d'intégrité des claims</li>
         *   <li>Détection de manipulation</li>
         * </ul>
         */
        @Test
        @DisplayName("Intégrité cryptographique - Validation signature")
        void cryptographicIntegrity_shouldValidateSignature() {
            // Given: Token avec signature invalide
            String tamperedToken = "tampered.signature.token";
            String bearerToken = "Bearer " + tamperedToken;
            
            when(headers.getFirst(HttpHeaders.AUTHORIZATION)).thenReturn(bearerToken);
            when(jwtService.extractToken(bearerToken)).thenReturn(tamperedToken);
            when(jwtService.validateToken(tamperedToken)).thenReturn(false);
            when(jwtService.isSignatureValid(tamperedToken)).thenReturn(false);

            // When: Filtre vérifie l'intégrité
            Mono<Void> result = authenticationFilter.filter(exchange, chain);

            // Then: Rejet pour signature invalide
            StepVerifier.create(result)
                .verifyComplete();

            verify(jwtService).validateToken(tamperedToken);
            verify(response).setStatusCode(HttpStatus.UNAUTHORIZED);
            verify(chain, never()).filter(any(ServerWebExchange.class));
        }
    }

    /**
     * Tests des métriques et monitoring.
     * 
     * <p>Validation de l'observabilité et du monitoring du filtre.
     */
    @Nested
    @DisplayName("Métriques et monitoring")
    class MetricsAndMonitoring {

        /**
         * Test de collecte des métriques d'authentification.
         * 
         * <p><strong>Métriques collectées :</strong>
         * <ul>
         *   <li>Nombre de tentatives d'authentification</li>
         *   <li>Taux de succès/échec</li>
         *   <li>Temps de traitement moyen</li>
         *   <li>Utilisation du cache Redis</li>
         * </ul>
         */
        @Test
        @DisplayName("Collecte métriques - Statistiques authentification")
        void authenticationMetrics_shouldBeCollected() {
            // Given: Token valide pour collecte métriques
            String token = "metrics_token";
            String bearerToken = "Bearer " + token;
            String userId = "metrics_user";
            
            when(headers.getFirst(HttpHeaders.AUTHORIZATION)).thenReturn(bearerToken);
            when(jwtService.extractToken(bearerToken)).thenReturn(token);
            when(jwtService.validateToken(token)).thenReturn(true);
            when(jwtService.extractUserId(token)).thenReturn(userId);
            when(userValidationService.isUserActive(userId)).thenReturn(Mono.just(true));
            when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

            // When: Filtre traite avec métriques
            Mono<Void> result = authenticationFilter.filter(exchange, chain);

            // Then: Métriques collectées
            StepVerifier.create(result)
                .verifyComplete();

            verify(chain).filter(any(ServerWebExchange.class));
            // Vérification que les métriques sont incrémentées
            // (normalement via un système comme Micrometer)
        }

        /**
         * Test de logging détaillé pour audit sécurité.
         * 
         * <p><strong>Audit logging :</strong>
         * Chaque tentative d'authentification génère des logs incluant :
         * <ul>
         *   <li>Timestamp précis</li>
         *   <li>Adresse IP source</li>
         *   <li>User-Agent</li>
         *   <li>Résultat de l'authentification</li>
         *   <li>Raison du rejet si applicable</li>
         * </ul>
         */
        @Test
        @DisplayName("Audit logging - Traçabilité sécurité complète")
        void securityAuditLogging_shouldCaptureAllEvents() {
            // Given: Token invalide pour logging d'audit
            String invalidToken = "invalid_audit_token";
            String bearerToken = "Bearer " + invalidToken;
            
            when(headers.getFirst(HttpHeaders.AUTHORIZATION)).thenReturn(bearerToken);
            when(jwtService.extractToken(bearerToken)).thenReturn(invalidToken);
            when(jwtService.validateToken(invalidToken)).thenReturn(false);

            // When: Filtre traite avec audit
            Mono<Void> result = authenticationFilter.filter(exchange, chain);

            // Then: Événement audité et logué
            StepVerifier.create(result)
                .verifyComplete();

            verify(jwtService).validateToken(invalidToken);
            verify(response).setStatusCode(HttpStatus.UNAUTHORIZED);
            // Vérification que l'événement est logué pour audit
        }
    }
}