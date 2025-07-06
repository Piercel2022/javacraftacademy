package com.javacraftacademy.gateway.service;

import com.javacraftacademy.gateway.dto.UserInfo;
import com.javacraftacademy.gateway.exception.AuthenticationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * Service de validation des utilisateurs pour la Gateway API.
 * 
 * <p>Ce service est responsable de la validation et de la vérification des utilisateurs
 * dans l'architecture microservices. Il interagit avec les services d'authentification
 * externes, gère la mise en cache des informations utilisateurs et fournit des mécanismes
 * de validation robustes.</p>
 * 
 * <h3>Fonctionnalités principales :</h3>
 * <ul>
 *   <li><strong>Validation d'utilisateur</strong> : Vérifie l'existence et la validité d'un utilisateur</li>
 *   <li><strong>Mise en cache</strong> : Utilise Redis pour optimiser les performances</li>
 *   <li><strong>Communication microservices</strong> : Intègre avec le service d'authentification</li>
 *   <li><strong>Gestion d'erreurs</strong> : Mécanismes de retry et gestion des pannes</li>
 *   <li><strong>Validation des formats</strong> : Contrôle la validité des emails et usernames</li>
 * </ul>
 * 
 * <h3>Relations avec l'architecture :</h3>
 * <ul>
 *   <li><strong>AuthenticationFilter</strong> : Utilisé pour valider les utilisateurs lors de l'authentification</li>
 *   <li><strong>AuthenticationService</strong> : Collabore pour les processus d'authentification</li>
 *   <li><strong>JwtService</strong> : Fournit des informations utilisateur pour la génération de tokens</li>
 *   <li><strong>RedisConfig</strong> : Utilise la configuration Redis pour la mise en cache</li>
 *   <li><strong>Service d'authentification externe</strong> : Communique via WebClient</li>
 * </ul>
 * 
 * <h3>Configuration requise :</h3>
 * <ul>
 *   <li>URL du service d'authentification via application.yml</li>
 *   <li>Configuration Redis pour la mise en cache</li>
 *   <li>Timeouts et paramètres de retry configurables</li>
 * </ul>
 * 
 * @author JavaCraft Academy
 * @version 1.0
 * @since 1.0
 * @see AuthenticationService
 * @see JwtService
 * @see UserInfo
 */
@Service
public class UserValidationService {

    private static final Logger logger = LoggerFactory.getLogger(UserValidationService.class);
    
    // Constantes pour la mise en cache
    private static final String USER_CACHE_KEY_PREFIX = "user:validation:";
    private static final String USER_INFO_CACHE_KEY_PREFIX = "user:info:";
    private static final long CACHE_EXPIRATION_MINUTES = 15;
    private static final long USER_INFO_CACHE_EXPIRATION_HOURS = 2;
    
    // Patterns de validation
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );
    private static final Pattern USERNAME_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._-]{3,20}$"
    );

    @Autowired
    private WebClient.Builder webClientBuilder;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Value("${gateway.auth-service.url:http://auth-service:8081}")
    private String authServiceUrl;
    
    @Value("${gateway.validation.timeout:5000}")
    private int validationTimeout;
    
    @Value("${gateway.validation.retry.max-attempts:3}")
    private int maxRetryAttempts;

    /**
     * Valide l'existence et la validité d'un utilisateur.
     * 
     * <p>Cette méthode vérifie d'abord le cache Redis pour optimiser les performances.
     * Si l'information n'est pas en cache, elle interroge le service d'authentification
     * externe et met en cache le résultat.</p>
     * 
     * <h4>Processus de validation :</h4>
     * <ol>
     *   <li>Validation du format de l'identifiant utilisateur</li>
     *   <li>Vérification du cache Redis</li>
     *   <li>Si absent du cache : appel au service d'authentification</li>
     *   <li>Mise en cache du résultat</li>
     *   <li>Retour du statut de validation</li>
     * </ol>
     * 
     * @param userIdentifier l'identifiant de l'utilisateur (email ou username)
     * @return Mono&lt;Boolean&gt; true si l'utilisateur est valide, false sinon
     * @throws AuthenticationException si une erreur de validation critique survient
     * @throws IllegalArgumentException si l'identifiant fourni est invalide
     * 
     * @since 1.0
     */
    public Mono<Boolean> validateUser(String userIdentifier) {
        logger.debug("Début de validation pour l'utilisateur: {}", userIdentifier);
        
        if (userIdentifier == null || userIdentifier.trim().isEmpty()) {
            logger.warn("Tentative de validation avec un identifiant vide");
            return Mono.error(new IllegalArgumentException("L'identifiant utilisateur ne peut pas être vide"));
        }
        
        // Validation du format
        if (!isValidUserIdentifier(userIdentifier)) {
            logger.warn("Format d'identifiant invalide: {}", userIdentifier);
            return Mono.just(false);
        }
        
        String cacheKey = USER_CACHE_KEY_PREFIX + userIdentifier;
        
        // Vérification du cache
        return getCachedValidation(cacheKey)
            .switchIfEmpty(validateUserFromService(userIdentifier)
                .doOnNext(isValid -> cacheValidationResult(cacheKey, isValid))
                .doOnError(error -> logger.error("Erreur lors de la validation de l'utilisateur {}: {}", 
                    userIdentifier, error.getMessage()))
            );
    }
    
    /**
     * Récupère les informations détaillées d'un utilisateur.
     * 
     * <p>Cette méthode fournit des informations complètes sur un utilisateur validé,
     * incluant ses rôles, permissions et métadonnées. Elle utilise également le
     * système de cache pour optimiser les performances.</p>
     * 
     * <h4>Informations récupérées :</h4>
     * <ul>
     *   <li>Identifiant unique de l'utilisateur</li>
     *   <li>Email et nom d'utilisateur</li>
     *   <li>Rôles et permissions</li>
     *   <li>Statut du compte (actif, suspendu, etc.)</li>
     *   <li>Métadonnées (dernière connexion, etc.)</li>
     * </ul>
     * 
     * @param userIdentifier l'identifiant de l'utilisateur
     * @return Mono&lt;UserInfo&gt; les informations complètes de l'utilisateur
     * @throws AuthenticationException si l'utilisateur n'existe pas ou n'est pas accessible
     * 
     * @since 1.0
     */
    public Mono<UserInfo> getUserInfo(String userIdentifier) {
        logger.debug("Récupération des informations pour l'utilisateur: {}", userIdentifier);
        
        if (userIdentifier == null || userIdentifier.trim().isEmpty()) {
            return Mono.error(new IllegalArgumentException("L'identifiant utilisateur ne peut pas être vide"));
        }
        
        String cacheKey = USER_INFO_CACHE_KEY_PREFIX + userIdentifier;
        
        return getCachedUserInfo(cacheKey)
            .switchIfEmpty(fetchUserInfoFromService(userIdentifier)
                .doOnNext(userInfo -> cacheUserInfo(cacheKey, userInfo))
                .doOnError(error -> logger.error("Erreur lors de la récupération des infos utilisateur {}: {}", 
                    userIdentifier, error.getMessage()))
            );
    }
    
    /**
     * Invalide le cache de validation pour un utilisateur spécifique.
     * 
     * <p>Cette méthode est utilisée lors de modifications d'informations utilisateur
     * pour s'assurer que les données en cache restent cohérentes.</p>
     * 
     * @param userIdentifier l'identifiant de l'utilisateur
     * @return Mono&lt;Void&gt; indicateur de completion
     * 
     * @since 1.0
     */
    public Mono<Void> invalidateUserCache(String userIdentifier) {
        logger.debug("Invalidation du cache pour l'utilisateur: {}", userIdentifier);
        
        return Mono.fromRunnable(() -> {
            String validationCacheKey = USER_CACHE_KEY_PREFIX + userIdentifier;
            String infoCacheKey = USER_INFO_CACHE_KEY_PREFIX + userIdentifier;
            
            redisTemplate.delete(validationCacheKey);
            redisTemplate.delete(infoCacheKey);
            
            logger.info("Cache invalidé pour l'utilisateur: {}", userIdentifier);
        });
    }
    
    /**
     * Vérifie si un utilisateur possède un rôle spécifique.
     * 
     * <p>Cette méthode est utilisée par les filtres d'autorisation pour contrôler
     * l'accès aux ressources en fonction des rôles utilisateur.</p>
     * 
     * @param userIdentifier l'identifiant de l'utilisateur
     * @param role le rôle à vérifier
     * @return Mono&lt;Boolean&gt; true si l'utilisateur possède le rôle, false sinon
     * 
     * @since 1.0
     */
    public Mono<Boolean> hasRole(String userIdentifier, String role) {
        logger.debug("Vérification du rôle '{}' pour l'utilisateur: {}", role, userIdentifier);
        
        return getUserInfo(userIdentifier)
            .map(userInfo -> userInfo.getRoles().contains(role))
            .onErrorReturn(false);
    }
    
    /**
     * Effectue une validation batch de plusieurs utilisateurs.
     * 
     * <p>Cette méthode optimise la validation de plusieurs utilisateurs en parallèle,
     * utile pour les opérations de validation en masse.</p>
     * 
     * @param userIdentifiers liste des identifiants à valider
     * @return Mono&lt;java.util.Map&lt;String, Boolean&gt;&gt; map des résultats de validation
     * 
     * @since 1.0
     */
    public Mono<java.util.Map<String, Boolean>> validateUsers(java.util.List<String> userIdentifiers) {
        logger.debug("Validation batch de {} utilisateurs", userIdentifiers.size());
        
        return reactor.core.publisher.Flux.fromIterable(userIdentifiers)
            .flatMap(identifier -> validateUser(identifier)
                .map(isValid -> new java.util.AbstractMap.SimpleEntry<>(identifier, isValid))
                .onErrorReturn(new java.util.AbstractMap.SimpleEntry<>(identifier, false))
            )
            .collectMap(
                java.util.Map.Entry::getKey,
                java.util.Map.Entry::getValue
            );
    }
    
    // ========== Méthodes privées ==========
    
    /**
     * Valide le format d'un identifiant utilisateur.
     * 
     * @param userIdentifier l'identifiant à valider
     * @return true si le format est valide
     */
    private boolean isValidUserIdentifier(String userIdentifier) {
        return EMAIL_PATTERN.matcher(userIdentifier).matches() || 
               USERNAME_PATTERN.matcher(userIdentifier).matches();
    }
    
    /**
     * Récupère le résultat de validation depuis le cache.
     * 
     * @param cacheKey clé de cache
     * @return Mono avec le résultat si présent en cache
     */
    private Mono<Boolean> getCachedValidation(String cacheKey) {
        return Mono.fromCallable(() -> {
            Boolean cached = (Boolean) redisTemplate.opsForValue().get(cacheKey);
            return cached;
        }).onErrorReturn(null);
    }
    
    /**
     * Met en cache le résultat de validation.
     * 
     * @param cacheKey clé de cache
     * @param isValid résultat de validation
     */
    private void cacheValidationResult(String cacheKey, Boolean isValid) {
        try {
            redisTemplate.opsForValue().set(cacheKey, isValid, CACHE_EXPIRATION_MINUTES, TimeUnit.MINUTES);
            logger.debug("Résultat de validation mis en cache: {}", cacheKey);
        } catch (Exception e) {
            logger.warn("Erreur lors de la mise en cache: {}", e.getMessage());
        }
    }
    
    /**
     * Valide un utilisateur via le service d'authentification externe.
     * 
     * @param userIdentifier identifiant de l'utilisateur
     * @return Mono avec le résultat de validation
     */
    private Mono<Boolean> validateUserFromService(String userIdentifier) {
        WebClient webClient = webClientBuilder.build();
        
        return webClient.get()
            .uri(authServiceUrl + "/api/users/validate/{userIdentifier}", userIdentifier)
            .retrieve()
            .bodyToMono(Boolean.class)
            .timeout(Duration.ofMillis(validationTimeout))
            .retryWhen(Retry.backoff(maxRetryAttempts, Duration.ofMillis(500))
                .filter(throwable -> !(throwable instanceof WebClientResponseException.NotFound)))
            .onErrorReturn(WebClientResponseException.NotFound.class, false)
            .doOnSuccess(result -> logger.debug("Validation depuis le service: {} -> {}", userIdentifier, result));
    }
    
    /**
     * Récupère les informations utilisateur depuis le cache.
     * 
     * @param cacheKey clé de cache
     * @return Mono avec les informations si présentes en cache
     */
    private Mono<UserInfo> getCachedUserInfo(String cacheKey) {
        return Mono.fromCallable(() -> {
            UserInfo cached = (UserInfo) redisTemplate.opsForValue().get(cacheKey);
            return cached;
        }).onErrorReturn(null);
    }
    
    /**
     * Met en cache les informations utilisateur.
     * 
     * @param cacheKey clé de cache
     * @param userInfo informations utilisateur
     */
    private void cacheUserInfo(String cacheKey, UserInfo userInfo) {
        try {
            redisTemplate.opsForValue().set(cacheKey, userInfo, USER_INFO_CACHE_EXPIRATION_HOURS, TimeUnit.HOURS);
            logger.debug("Informations utilisateur mises en cache: {}", cacheKey);
        } catch (Exception e) {
            logger.warn("Erreur lors de la mise en cache des infos utilisateur: {}", e.getMessage());
        }
    }
    
    /**
     * Récupère les informations utilisateur depuis le service externe.
     * 
     * @param userIdentifier identifiant de l'utilisateur
     * @return Mono avec les informations utilisateur
     */
    private Mono<UserInfo> fetchUserInfoFromService(String userIdentifier) {
        WebClient webClient = webClientBuilder.build();
        
        return webClient.get()
            .uri(authServiceUrl + "/api/users/info/{userIdentifier}", userIdentifier)
            .retrieve()
            .onStatus(HttpStatus::is4xxClientError, 
                response -> Mono.error(new AuthenticationException("Utilisateur non trouvé: " + userIdentifier)))
            .bodyToMono(UserInfo.class)
            .timeout(Duration.ofMillis(validationTimeout))
            .retryWhen(Retry.backoff(maxRetryAttempts, Duration.ofMillis(500))
                .filter(throwable -> !(throwable instanceof AuthenticationException)))
            .doOnSuccess(userInfo -> logger.debug("Informations récupérées depuis le service pour: {}", userIdentifier));
    }
}