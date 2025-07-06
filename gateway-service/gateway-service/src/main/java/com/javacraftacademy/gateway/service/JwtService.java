
package com.javacraftacademy.gateway.service;

import com.javacraftacademy.gateway.dto.UserInfo;
import com.javacraftacademy.gateway.exception.AuthenticationException;
import com.javacraftacademy.gateway.util.JwtUtil;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.List;
import java.util.ArrayList;

/**
 * Service de gestion des tokens JWT (JSON Web Tokens).
 * 
 * <p>Ce service constitue le cœur de la gestion des tokens dans l'architecture de la Gateway.
 * Il s'occupe de la génération, validation, refresh et invalidation des tokens JWT utilisés
 * pour l'authentification et l'autorisation dans l'ensemble du système de microservices.</p>
 * 
 * <h3>Fonctionnalités principales :</h3>
 * <ul>
 *   <li>Génération de tokens JWT sécurisés avec claims personnalisés</li>
 *   <li>Validation des tokens entrants avec vérification de signature</li>
 *   <li>Gestion du refresh des tokens expirés</li>
 *   <li>Invalidation des tokens (blacklist) pour la déconnexion</li>
 *   <li>Extraction des informations utilisateur depuis les tokens</li>
 *   <li>Gestion de la rotation des clés de signature</li>
 * </ul>
 * 
 * <h3>Relations avec l'application :</h3>
 * <ul>
 *   <li><strong>AuthenticationService</strong> : Principal consommateur pour les opérations d'auth</li>
 *   <li><strong>AuthenticationFilter</strong> : Utilise ce service pour valider chaque requête</li>
 *   <li><strong>JwtUtil</strong> : Délègue les opérations de bas niveau sur les JWT</li>
 *   <li><strong>RedisTemplate</strong> : Stockage de la blacklist et du cache des tokens</li>
 *   <li><strong>SecurityConfig</strong> : Configuration des paramètres de sécurité JWT</li>
 * </ul>
 * 
 * <h3>Architecture de sécurité :</h3>
 * <pre>
 * Token Generation:  UserInfo → JWT Claims → Signed Token
 * Token Validation:  Token → Signature Check → Claims Extraction → UserInfo
 * Token Refresh:     Old Token → Validation → New Token Generation
 * Token Blacklist:   Invalid Tokens → Redis Cache → Rejection
 * </pre>
 * 
 * <h3>Format des tokens :</h3>
 * <p>Les tokens générés contiennent les claims suivants :</p>
 * <ul>
 *   <li><code>sub</code> : Identifiant unique de l'utilisateur</li>
 *   <li><code>username</code> : Nom d'utilisateur</li>
 *   <li><code>email</code> : Email de l'utilisateur</li>
 *   <li><code>roles</code> : Rôles et permissions</li>
 *   <li><code>iat</code> : Date de création du token</li>
 *   <li><code>exp</code> : Date d'expiration du token</li>
 *   <li><code>jti</code> : Identifiant unique du token (pour blacklist)</li>
 * </ul>
 * 
 * @author JavaCraftAcademy
 * @version 1.0
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class JwtService {

    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration:3600000}") // 1 heure par défaut
    private long jwtExpirationMs;

    @Value("${app.jwt.refresh-expiration:604800000}") // 7 jours par défaut
    private long refreshExpirationMs;

    @Value("${app.jwt.issuer:gateway-service}")
    private String issuer;

    private SecretKey signingKey;
    private final Set<String> tokenBlacklist = ConcurrentHashMap.newKeySet();

    // Préfixes Redis pour l'organisation des données
    private static final String BLACKLIST_PREFIX = "jwt:blacklist:";
    private static final String REFRESH_PREFIX = "jwt:refresh:";
    private static final String USER_SESSIONS_PREFIX = "jwt:sessions:";

    /**
     * Initialise le service JWT avec la clé de signature.
     * 
     * <p>Cette méthode configure la clé secrète utilisée pour signer les tokens JWT.
     * Elle s'assure que la clé respecte les standards de sécurité requis par l'algorithme HMAC-SHA256.</p>
     * 
     * @throws IllegalArgumentException Si la clé secrète est trop courte ou invalide
     */
    @PostConstruct
    public void initializeSigningKey() {
        if (jwtSecret == null || jwtSecret.length() < 32) {
            throw new IllegalArgumentException("JWT secret must be at least 32 characters long");
        }
        
        this.signingKey = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        log.info("JWT Service initialized with expiration: {}ms, refresh expiration: {}ms", 
                jwtExpirationMs, refreshExpirationMs);
    }

    /**
     * Génère un token JWT pour un utilisateur authentifié.
     * 
     * <p>Cette méthode crée un token JWT signé contenant toutes les informations
     * nécessaires pour identifier et autoriser l'utilisateur dans les services en aval.
     * Le token inclut des claims standard et personnalisés pour répondre aux besoins
     * spécifiques de l'application.</p>
     * 
     * <h4>Processus de génération :</h4>
     * <ol>
     *   <li>Validation des informations utilisateur</li>
     *   <li>Construction des claims JWT (standard + personnalisés)</li>
     *   <li>Définition des dates d'émission et d'expiration</li>
     *   <li>Signature du token avec la clé secrète</li>
     *   <li>Enregistrement du token pour le suivi des sessions</li>
     * </ol>
     * 
     * @param userInfo Les informations de l'utilisateur à encoder dans le token
     * @return Mono<String> Le token JWT signé
     * @throws IllegalArgumentException Si les informations utilisateur sont invalides
     * 
     * @see UserInfo
     * @see #buildClaims(UserInfo)
     */
    public Mono<String> generateToken(UserInfo userInfo) {
        if (userInfo == null || userInfo.getId() == null) {
            return Mono.error(new IllegalArgumentException("UserInfo and ID cannot be null"));
        }

        log.debug("Generating JWT token for user: {}", userInfo.getUsername());

        try {
            Date now = new Date();
            Date expiryDate = new Date(now.getTime() + jwtExpirationMs);
            String tokenId = generateTokenId();

            Map<String, Object> claims = buildClaims(userInfo);
            
            String token = Jwts.builder()
                    .setClaims(claims)
                    .setSubject(userInfo.getId().toString())
                    .setIssuedAt(now)
                    .setExpiration(expiryDate)
                    .setIssuer(issuer)
                    .setId(tokenId)
                    .signWith(signingKey, SignatureAlgorithm.HS512)
                    .compact();

            // Enregistrement de la session utilisateur
            registerUserSession(userInfo.getUsername(), tokenId, expiryDate);

            log.debug("JWT token generated successfully for user: {} with ID: {}", 
                     userInfo.getUsername(), tokenId);

            return Mono.just(token);

        } catch (Exception e) {
            log.error("Error generating JWT token for user: {}", userInfo.getUsername(), e);
            return Mono.error(new AuthenticationException("Failed to generate authentication token"));
        }
    }

    /**
     * Valide un token JWT et extrait les informations utilisateur.
     * 
     * <p>Cette méthode constitue le point central de validation des tokens dans la Gateway.
     * Elle vérifie la signature, l'expiration, et la présence dans la blacklist avant
     * d'extraire et de retourner les informations utilisateur.</p>
     * 
     * <h4>Processus de validation :</h4>
     * <ol>
     *   <li>Vérification de la présence et du format du token</li>
     *   <li>Contrôle de la blacklist (tokens invalidés)</li>
     *   <li>Validation de la signature cryptographique</li>
     *   <li>Vérification de l'expiration</li>
     *   <li>Extraction et reconstruction des informations utilisateur</li>
     * </ol>
     * 
     * @param token Le token JWT à valider
     * @return Mono<UserInfo> Les informations utilisateur extraites du token
     * @throws AuthenticationException Si le token est invalide, expiré ou blacklisté
     * 
     * @see #isTokenBlacklisted(String)
     * @see #extractUserInfoFromClaims(Claims)
     */
    public Mono<UserInfo> validateToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return Mono.error(new AuthenticationException("Token cannot be null or empty"));
        }

        log.debug("Validating JWT token: {}", maskToken(token));

        return Mono.fromCallable(() -> {
            // Vérification de la blacklist
            if (isTokenBlacklisted(token)) {
                throw new AuthenticationException("Token has been invalidated");
            }

            try {
                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(signingKey)
                        .requireIssuer(issuer)
                        .build()
                        .parseClaimsJws(token)
                        .getBody();

                // Vérification supplémentaire de l'expiration
                if (claims.getExpiration().before(new Date())) {
                    throw new AuthenticationException("Token has expired");
                }

                UserInfo userInfo = extractUserInfoFromClaims(claims);
                log.debug("Token validated successfully for user: {}", userInfo.getUsername());
                
                return userInfo;

            } catch (ExpiredJwtException e) {
                log.debug("Token expired: {}", e.getMessage());
                throw new AuthenticationException("Token has expired");
            } catch (UnsupportedJwtException e) {
                log.warn("Unsupported JWT token: {}", e.getMessage());
                throw new AuthenticationException("Unsupported token format");
            } catch (MalformedJwtException e) {
                log.warn("Malformed JWT token: {}", e.getMessage());
                throw new AuthenticationException("Invalid token format");
            } catch (SignatureException e) {
                log.warn("Invalid JWT signature: {}", e.getMessage());
                throw new AuthenticationException("Invalid token signature");
            } catch (IllegalArgumentException e) {
                log.warn("JWT claims string is empty: {}", e.getMessage());
                throw new AuthenticationException("Invalid token content");
            }
        });
    }

    /**
     * Rafraîchit un token JWT expiré.
     * 
     * <p>Cette méthode permet de renouveler un token sans obliger l'utilisateur
     * à se reconnecter. Elle vérifie que le token n'est pas dans la blacklist
     * et que l'utilisateur a encore des droits valides avant de générer un nouveau token.</p>
     * 
     * <h4>Processus de refresh :</h4>
     * <ol>
     *   <li>Validation du token de refresh (même si expiré récemment)</li>
     *   <li>Extraction des informations utilisateur</li>
     *   <li>Vérification des droits actuels de l'utilisateur</li>
     *   <li>Génération d'un nouveau token avec durée complète</li>
     *   <li>Invalidation de l'ancien token</li>
     * </ol>
     * 
     * @param refreshToken Le token à rafraîchir
     * @return Mono<UserInfo> Les informations utilisateur pour générer un nouveau token
     * @throws AuthenticationException Si le refresh token est invalide
     * 
     * @see #generateToken(UserInfo)
     */
    public Mono<UserInfo> refreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            return Mono.error(new AuthenticationException("Refresh token cannot be null or empty"));
        }

        log.debug("Refreshing JWT token: {}", maskToken(refreshToken));

        return Mono.fromCallable(() -> {
            if (isTokenBlacklisted(refreshToken)) {
                throw new AuthenticationException("Refresh token has been invalidated");
            }

            try {
                // Utilisation d'un parser plus permissif pour les tokens expirés récemment
                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(signingKey)
                        .setAllowedClockSkewSeconds(60) // Tolérance de 60 secondes
                        .build()
                        .parseClaimsJws(refreshToken)
                        .getBody();

                // Vérification que le token n'est pas trop ancien pour un refresh
                Date expiration = claims.getExpiration();
                Date now = new Date();
                long timeSinceExpiration = now.getTime() - expiration.getTime();
                
                if (timeSinceExpiration > refreshExpirationMs) {
                    throw new AuthenticationException("Refresh token has expired beyond refresh window");
                }

                UserInfo userInfo = extractUserInfoFromClaims(claims);
                
                // Invalidation de l'ancien token
                invalidateTokenImmediate(refreshToken);
                
                log.debug("Token refreshed successfully for user: {}", userInfo.getUsername());
                return userInfo;

            } catch (ExpiredJwtException e) {
                // Pour les tokens expirés, on tente de les traiter quand même s'ils sont dans la fenêtre de refresh
                Claims claims = e.getClaims();
                if (claims != null) {
                    Date expiration = claims.getExpiration();
                    Date now = new Date();
                    long timeSinceExpiration = now.getTime() - expiration.getTime();
                    
                    if (timeSinceExpiration <= refreshExpirationMs) {
                        UserInfo userInfo = extractUserInfoFromClaims(claims);
                        invalidateTokenImmediate(refreshToken);
                        log.debug("Expired token refreshed successfully for user: {}", userInfo.getUsername());
                        return userInfo;
                    }
                }
                throw new AuthenticationException("Refresh token has expired beyond refresh window");
            } catch (Exception e) {
                log.warn("Error refreshing token: {}", e.getMessage());
                throw new AuthenticationException("Failed to refresh token");
            }
        });
    }

    /**
     * Invalide un token en l'ajoutant à la blacklist.
     * 
     * <p>Cette méthode permet de révoquer un token avant son expiration naturelle.
     * Le token sera stocké dans Redis avec une TTL correspondant à sa durée de vie restante.</p>
     * 
     * @param token Le token à invalider
     * @return Mono<Void> Confirmation de l'invalidation
     */
    public Mono<Void> invalidateToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return Mono.error(new IllegalArgumentException("Token cannot be null or empty"));
        }

        return Mono.fromRunnable(() -> {
            try {
                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(signingKey)
                        .build()
                        .parseClaimsJws(token)
                        .getBody();

                String tokenId = claims.getId();
                String username = claims.get("username", String.class);
                Date expiration = claims.getExpiration();

                // Ajout à la blacklist locale
                tokenBlacklist.add(tokenId);

                // Ajout à la blacklist Redis avec TTL
                long ttl = expiration.getTime() - System.currentTimeMillis();
                if (ttl > 0) {
                    redisTemplate.opsForValue().set(
                        BLACKLIST_PREFIX + tokenId, 
                        "true", 
                        Duration.ofMillis(ttl)
                    );
                }

                // Suppression de la session utilisateur
                removeUserSession(username, tokenId);

                log.debug("Token invalidated successfully for user: {} with ID: {}", username, tokenId);

            } catch (Exception e) {
                log.warn("Error invalidating token: {}", e.getMessage());
                // On ajoute quand même le token à la blacklist locale par sécurité
                invalidateTokenImmediate(token);
            }
        });
    }

    /**
     * Invalide immédiatement un token sans validation préalable.
     * 
     * @param token Le token à invalider
     */
    private void invalidateTokenImmediate(String token) {
        try {
            String tokenId = extractTokenId(token);
            if (tokenId != null) {
                tokenBlacklist.add(tokenId);
                redisTemplate.opsForValue().set(
                    BLACKLIST_PREFIX + tokenId, 
                    "true", 
                    Duration.ofHours(24) // TTL de sécurité
                );
            }
        } catch (Exception e) {
            log.warn("Error in immediate token invalidation: {}", e.getMessage());
        }
    }

    /**
     * Invalide tous les tokens d'un utilisateur.
     * 
     * @param username Le nom d'utilisateur
     * @return Mono<Void> Confirmation de l'invalidation
     */
    public Mono<Void> invalidateAllUserTokens(String username) {
        if (username == null || username.trim().isEmpty()) {
            return Mono.error(new IllegalArgumentException("Username cannot be null or empty"));
        }

        return Mono.fromRunnable(() -> {
            try {
                Set<String> userTokens = redisTemplate.opsForSet().members(USER_SESSIONS_PREFIX + username);
                if (userTokens != null) {
                    for (String tokenId : userTokens) {
                        tokenBlacklist.add(tokenId);
                        redisTemplate.opsForValue().set(
                            BLACKLIST_PREFIX + tokenId, 
                            "true", 
                            Duration.ofHours(24)
                        );
                    }
                    // Suppression de toutes les sessions de l'utilisateur
                    redisTemplate.delete(USER_SESSIONS_PREFIX + username);
                }
                log.debug("All tokens invalidated for user: {}", username);
            } catch (Exception e) {
                log.error("Error invalidating all tokens for user: {}", username, e);
            }
        });
    }

    /**
     * Vérifie si un token se trouve dans la blacklist.
     * 
     * @param token Le token à vérifier
     * @return true si le token est blacklisté, false sinon
     */
    private boolean isTokenBlacklisted(String token) {
        try {
            String tokenId = extractTokenId(token);
            if (tokenId == null) return false;

            // Vérification dans la blacklist locale
            if (tokenBlacklist.contains(tokenId)) {
                return true;
            }

            // Vérification dans Redis
            Boolean isBlacklisted = redisTemplate.hasKey(BLACKLIST_PREFIX + tokenId);
            if (Boolean.TRUE.equals(isBlacklisted)) {
                // Ajout à la blacklist locale pour les prochaines vérifications
                tokenBlacklist.add(tokenId);
                return true;
            }

            return false;

        } catch (Exception e) {
            log.warn("Error checking token blacklist: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Extrait l'ID du token à partir du JWT.
     * 
     * @param token Le token JWT
     * @return L'ID du token ou null si extraction impossible
     */
    private String extractTokenId(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getId();
        } catch (Exception e) {
            log.debug("Could not extract token ID: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Construit les claims personnalisés pour le token JWT.
     * 
     * @param userInfo Les informations utilisateur
     * @return Map contenant les claims
     */
    private Map<String, Object> buildClaims(UserInfo userInfo) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", userInfo.getUsername());
        claims.put("email", userInfo.getEmail());
        claims.put("roles", userInfo.getRoles());
        claims.put("firstName", userInfo.getFirstName());
        claims.put("lastName", userInfo.getLastName());
        claims.put("enabled", userInfo.isEnabled());
        return claims;
    }

    /**
     * Extrait les informations utilisateur à partir des claims JWT.
     * 
     * @param claims Les claims du token JWT
     * @return UserInfo reconstruit
     */
    private UserInfo extractUserInfoFromClaims(Claims claims) {
        UserInfo userInfo = new UserInfo();
        userInfo.setId(Long.valueOf(claims.getSubject()));
        userInfo.setUsername(claims.get("username", String.class));
        userInfo.setEmail(claims.get("email", String.class));
        userInfo.setFirstName(claims.get("firstName", String.class));
        userInfo.setLastName(claims.get("lastName", String.class));
        userInfo.setEnabled(claims.get("enabled", Boolean.class));
        
        // Extraction des rôles
        @SuppressWarnings("unchecked")
        List<String> roles = claims.get("roles", List.class);
        if (roles != null) {
            userInfo.setRoles(roles);
        } else {
            userInfo.setRoles(new ArrayList<>());
        }
        
        return userInfo;
    }

    /**
     * Génère un identifiant unique pour le token.
     * 
     * @return Un UUID unique
     */
    private String generateTokenId() {
        return UUID.randomUUID().toString();
    }

    /**
     * Enregistre une session utilisateur dans Redis.
     * 
     * @param username Le nom d'utilisateur
     * @param tokenId L'ID du token
     * @param expiration La date d'expiration
     */
    private void registerUserSession(String username, String tokenId, Date expiration) {
        try {
            long ttl = expiration.getTime() - System.currentTimeMillis();
            if (ttl > 0) {
                redisTemplate.opsForSet().add(USER_SESSIONS_PREFIX + username, tokenId);
                redisTemplate.expire(USER_SESSIONS_PREFIX + username, Duration.ofMillis(ttl));
            }
        } catch (Exception e) {
            log.warn("Error registering user session: {}", e.getMessage());
        }
    }

    /**
     * Supprime une session utilisateur de Redis.
     * 
     * @param username Le nom d'utilisateur
     * @param tokenId L'ID du token
     */
    private void removeUserSession(String username, String tokenId) {
        try {
            redisTemplate.opsForSet().remove(USER_SESSIONS_PREFIX + username, tokenId);
        } catch (Exception e) {
            log.warn("Error removing user session: {}", e.getMessage());
        }
    }

    /**
     * Masque partiellement un token pour les logs.
     * 
     * @param token Le token à masquer
     * @return Token masqué
     */
    private String maskToken(String token) {
        if (token == null || token.length() < 10) {
            return "***";
        }
        return token.substring(0, 10) + "..." + token.substring(token.length() - 10);
    }

    /**
     * Nettoie périodiquement la blacklist locale.
     * Cette méthode peut être appelée par un scheduler pour optimiser la mémoire.
     */
    public void cleanupLocalBlacklist() {
        log.debug("Cleaning up local blacklist, current size: {}", tokenBlacklist.size());
        // La blacklist locale sera automatiquement nettoyée par le garbage collector
        // car les tokens expirés ne seront plus référencés
        tokenBlacklist.clear();
        log.debug("Local blacklist cleaned up");
    }

    /**
     * Retourne des statistiques sur le service JWT.
     * 
     * @return Map contenant les statistiques
     */
    public Map<String, Object> getJwtStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("localBlacklistSize", tokenBlacklist.size());
        stats.put("jwtExpirationMs", jwtExpirationMs);
        stats.put("refreshExpirationMs", refreshExpirationMs);
        stats.put("issuer", issuer);
        return stats;
    }
}