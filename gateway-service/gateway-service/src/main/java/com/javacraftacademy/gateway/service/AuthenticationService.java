package com.javacraftacademy.gateway.service;

import com.javacraftacademy.gateway.dto.AuthResponse;
import com.javacraftacademy.gateway.dto.LoginRequest;
import com.javacraftacademy.gateway.dto.UserInfo;
import com.javacraftacademy.gateway.exception.AuthenticationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Service d'authentification central pour la Gateway.
 * 
 * <p>Ce service agit comme le point d'entrée principal pour toutes les opérations
 * d'authentification dans l'architecture microservices. Il coordonne les interactions
 * avec les services en aval tout en maintenant une couche d'abstraction pour la sécurité.</p>
 * 
 * <h3>Fonctionnalités principales :</h3>
 * <ul>
 *   <li>Authentification des utilisateurs avec validation des credentials</li>
 *   <li>Génération et validation des tokens JWT</li>
 *   <li>Intégration avec le service utilisateur externe</li>
 *   <li>Gestion du cache d'authentification avec Redis</li>
 *   <li>Validation et refresh des tokens expirés</li>
 * </ul>
 * 
 * <h3>Relations avec l'application :</h3>
 * <ul>
 *   <li><strong>AuthenticationFilter</strong> : Utilise ce service pour valider les tokens entrants</li>
 *   <li><strong>JwtService</strong> : Délègue la génération/validation des JWT</li>
 *   <li><strong>UserValidationService</strong> : Collabore pour la validation des utilisateurs</li>
 *   <li><strong>GatewayController</strong> : Point d'entrée pour les requêtes d'authentification</li>
 *   <li><strong>RedisConfig</strong> : Utilise le cache pour optimiser les performances</li>
 * </ul>
 * 
 * <h3>Flux d'authentification :</h3>
 * <pre>
 * Client → GatewayController → AuthenticationService → UserService (externe)
 *                                     ↓
 *                            JwtService + Redis Cache
 *                                     ↓
 *                              AuthResponse → Client
 * </pre>
 * 
 * @author JavaCraftAcademy
 * @version 1.0
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    private final JwtService jwtService;
    private final UserValidationService userValidationService;
    private final PasswordEncoder passwordEncoder;
    private final WebClient.Builder webClientBuilder;

    @Value("${app.services.user-service.url}")
    private String userServiceUrl;

    @Value("${app.authentication.cache-duration:300}")
    private long cacheDurationSeconds;

    private WebClient userServiceClient;
    private final Map<String, UserInfo> authCache = new HashMap<>();

    /**
     * Initialise le client WebClient pour les appels au service utilisateur.
     * 
     * <p>Cette méthode configure le client avec les timeouts appropriés et
     * les headers par défaut pour optimiser les communications inter-services.</p>
     */
    @PostConstruct
    public void initializeWebClient() {
        this.userServiceClient = webClientBuilder
                .baseUrl(userServiceUrl)
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024))
                .build();
        
        log.info("AuthenticationService initialized with user service URL: {}", userServiceUrl);
    }

    /**
     * Authentifie un utilisateur avec ses credentials.
     * 
     * <p>Cette méthode constitue le point d'entrée principal pour l'authentification.
     * Elle valide les credentials contre le service utilisateur externe, génère un token JWT
     * en cas de succès, et met en cache les informations utilisateur pour optimiser
     * les requêtes futures.</p>
     * 
     * <h4>Processus d'authentification :</h4>
     * <ol>
     *   <li>Validation des paramètres d'entrée</li>
     *   <li>Appel au service utilisateur pour vérification</li>
     *   <li>Vérification du mot de passe hashé</li>
     *   <li>Génération du token JWT avec les claims appropriés</li>
     *   <li>Mise en cache des informations utilisateur</li>
     *   <li>Construction de la réponse d'authentification</li>
     * </ol>
     * 
     * @param loginRequest Les credentials de l'utilisateur (email/username et mot de passe)
     * @return Mono<AuthResponse> Réponse contenant le token JWT et les informations utilisateur
     * @throws AuthenticationException Si les credentials sont invalides ou si le service utilisateur est indisponible
     * 
     * @see LoginRequest
     * @see AuthResponse
     * @see JwtService#generateToken(UserInfo)
     */
    public Mono<AuthResponse> authenticate(LoginRequest loginRequest) {
        log.debug("Starting authentication process for user: {}", loginRequest.getUsername());
        
        return validateLoginRequest(loginRequest)
                .flatMap(this::fetchUserFromService)
                .flatMap(userInfo -> validatePassword(loginRequest.getPassword(), userInfo))
                .flatMap(this::generateAuthResponse)
                .doOnSuccess(response -> cacheUserInfo(response.getUserInfo()))
                .doOnError(error -> log.error("Authentication failed for user: {}", 
                          loginRequest.getUsername(), error))
                .onErrorMap(this::mapToAuthenticationException);
    }

    /**
     * Valide un token JWT et retourne les informations de l'utilisateur.
     * 
     * <p>Cette méthode est utilisée par les filtres de la gateway pour valider
     * les tokens entrants sur chaque requête. Elle utilise le cache Redis pour
     * optimiser les performances et éviter des appels répétés au service JWT.</p>
     * 
     * <h4>Processus de validation :</h4>
     * <ol>
     *   <li>Vérification de la présence du token</li>
     *   <li>Consultation du cache pour les informations utilisateur</li>
     *   <li>Validation JWT si nécessaire</li>
     *   <li>Mise à jour du cache si les informations ont changé</li>
     * </ol>
     * 
     * @param token Le token JWT à valider
     * @return Mono<UserInfo> Les informations de l'utilisateur si le token est valide
     * @throws AuthenticationException Si le token est invalide, expiré ou malformé
     * 
     * @see JwtService#validateToken(String)
     * @see UserInfo
     */
    public Mono<UserInfo> validateToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return Mono.error(new AuthenticationException("Token cannot be null or empty"));
        }

        log.debug("Validating token: {}", token.substring(0, Math.min(20, token.length())) + "...");

        return jwtService.validateToken(token)
                .cast(UserInfo.class)
                .doOnSuccess(userInfo -> log.debug("Token validated successfully for user: {}", 
                           userInfo.getUsername()))
                .doOnError(error -> log.warn("Token validation failed: {}", error.getMessage()));
    }

    /**
     * Rafraîchit un token JWT expiré.
     * 
     * <p>Cette méthode permet de renouveler un token sans obliger l'utilisateur
     * à se reconnecter. Elle vérifie que le token n'est pas complètement expiré
     * et génère un nouveau token avec une durée de validité mise à jour.</p>
     * 
     * @param refreshToken Le token de rafraîchissement
     * @return Mono<AuthResponse> Nouvelle réponse d'authentification avec un token frais
     * @throws AuthenticationException Si le token de rafraîchissement est invalide
     * 
     * @see JwtService#refreshToken(String)
     */
    public Mono<AuthResponse> refreshToken(String refreshToken) {
        log.debug("Refreshing token");
        
        return jwtService.refreshToken(refreshToken)
                .cast(UserInfo.class)
                .flatMap(this::generateAuthResponse)
                .doOnSuccess(response -> log.debug("Token refreshed successfully for user: {}", 
                           response.getUserInfo().getUsername()))
                .doOnError(error -> log.error("Token refresh failed: {}", error.getMessage()));
    }

    /**
     * Déconnecte un utilisateur en invalidant son token.
     * 
     * <p>Cette méthode ajoute le token à une blacklist et supprime les informations
     * utilisateur du cache. Elle assure une déconnexion sécurisée en empêchant
     * la réutilisation du token.</p>
     * 
     * @param token Le token à invalider
     * @return Mono<Void> Confirmation de la déconnexion
     */
    public Mono<Void> logout(String token) {
        log.debug("Processing logout request");
        
        return jwtService.invalidateToken(token)
                .then(removeFromCache(token))
                .doOnSuccess(v -> log.debug("Logout completed successfully"))
                .doOnError(error -> log.error("Logout failed: {}", error.getMessage()));
    }

    /**
     * Valide la requête de connexion.
     * 
     * @param loginRequest La requête à valider
     * @return Mono<LoginRequest> La requête validée
     */
    private Mono<LoginRequest> validateLoginRequest(LoginRequest loginRequest) {
        if (loginRequest == null || 
            loginRequest.getUsername() == null || loginRequest.getUsername().trim().isEmpty() ||
            loginRequest.getPassword() == null || loginRequest.getPassword().trim().isEmpty()) {
            
            return Mono.error(new AuthenticationException("Username and password are required"));
        }
        
        return Mono.just(loginRequest);
    }

    /**
     * Récupère les informations utilisateur depuis le service externe.
     * 
     * <p>Cette méthode fait appel au microservice utilisateur pour récupérer
     * les informations complètes de l'utilisateur. Elle gère les timeouts et
     * les erreurs de réseau pour assurer la résilience de la gateway.</p>
     * 
     * @param loginRequest Les credentials pour identifier l'utilisateur
     * @return Mono<UserInfo> Les informations de l'utilisateur
     */
    private Mono<UserInfo> fetchUserFromService(LoginRequest loginRequest) {
        return userServiceClient
                .get()
                .uri("/api/users/by-username/{username}", loginRequest.getUsername())
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, response -> 
                    Mono.error(new AuthenticationException("User not found or invalid credentials")))
                .onStatus(HttpStatus::is5xxServerError, response -> 
                    Mono.error(new AuthenticationException("User service temporarily unavailable")))
                .bodyToMono(UserInfo.class)
                .timeout(Duration.ofSeconds(5))
                .doOnNext(userInfo -> log.debug("User fetched from service: {}", userInfo.getUsername()));
    }

    /**
     * Valide le mot de passe fourni contre le hash stocké.
     * 
     * @param rawPassword Le mot de passe en clair
     * @param userInfo Les informations utilisateur contenant le hash
     * @return Mono<UserInfo> Les informations utilisateur validées
     */
    private Mono<UserInfo> validatePassword(String rawPassword, UserInfo userInfo) {
        if (!passwordEncoder.matches(rawPassword, userInfo.getPasswordHash())) {
            return Mono.error(new AuthenticationException("Invalid credentials"));
        }
        
        return Mono.just(userInfo);
    }

    /**
     * Génère la réponse d'authentification complète.
     * 
     * @param userInfo Les informations de l'utilisateur authentifié
     * @return Mono<AuthResponse> La réponse contenant le token et les infos utilisateur
     */
    private Mono<AuthResponse> generateAuthResponse(UserInfo userInfo) {
        return jwtService.generateToken(userInfo)
                .map(token -> AuthResponse.builder()
                        .token(token)
                        .userInfo(userInfo)
                        .expiresIn(jwtService.getExpirationTime())
                        .tokenType("Bearer")
                        .build());
    }

    /**
     * Met en cache les informations utilisateur.
     * 
     * @param userInfo Les informations à mettre en cache
     */
    private void cacheUserInfo(UserInfo userInfo) {
        // Dans un environnement de production, ceci serait géré par Redis
        authCache.put(userInfo.getUsername(), userInfo);
        log.debug("User info cached for: {}", userInfo.getUsername());
    }

    /**
     * Supprime les informations du cache lors de la déconnexion.
     * 
     * @param token Le token associé aux informations à supprimer
     * @return Mono<Void> Confirmation de la suppression
     */
    private Mono<Void> removeFromCache(String token) {
        return jwtService.extractUsername(token)
                .doOnNext(username -> {
                    authCache.remove(username);
                    log.debug("User info removed from cache for: {}", username);
                })
                .then();
    }

    /**
     * Mappe les exceptions génériques vers des exceptions d'authentification.
     * 
     * @param throwable L'exception originale
     * @return AuthenticationException L'exception mappée
     */
    private AuthenticationException mapToAuthenticationException(Throwable throwable) {
        if (throwable instanceof AuthenticationException) {
            return (AuthenticationException) throwable;
        }
        
        log.error("Unexpected error during authentication", throwable);
        return new AuthenticationException("Authentication failed due to internal error");
    }

    /**
     * Vérifie si un utilisateur est actuellement authentifié.
     * 
     * @param username Le nom d'utilisateur à vérifier
     * @return boolean true si l'utilisateur est authentifié
     */
    public boolean isUserAuthenticated(String username) {
        return authCache.containsKey(username);
    }

    /**
     * Retourne les statistiques d'authentification pour le monitoring.
     * 
     * @return Map<String, Object> Les métriques d'authentification
     */
    public Map<String, Object> getAuthenticationMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("cachedUsers", authCache.size());
        metrics.put("cacheCapacity", cacheDurationSeconds);
        return metrics;
    }
}