package com.javacraftacademy.gateway.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Classe utilitaire pour la gestion des tokens JWT dans le service Gateway.
 * 
 * <p>Cette classe fournit des méthodes pour :
 * <ul>
 *   <li>Générer des tokens JWT d'accès et de rafraîchissement</li>
 *   <li>Valider et parser les tokens JWT</li>
 *   <li>Extraire les informations des tokens (claims, expiration, etc.)</li>
 *   <li>Vérifier l'intégrité et la validité des tokens</li>
 * </ul>
 * 
 * <h3>Relations avec l'application :</h3>
 * <ul>
 *   <li><strong>JwtService</strong> : Utilisé par JwtService pour les opérations JWT de haut niveau</li>
 *   <li><strong>AuthenticationFilter</strong> : Appelé pour valider les tokens dans les requêtes HTTP</li>
 *   <li><strong>AuthenticationService</strong> : Utilisé pour générer des tokens lors de l'authentification</li>
 *   <li><strong>SecurityConfig</strong> : Configuration des paramètres de sécurité JWT</li>
 * </ul>
 * 
 * <h3>Configuration requise :</h3>
 * <p>Les propriétés suivantes doivent être définies dans application.yml :
 * <pre>
 * jwt:
 *   secret: your-256-bit-secret-key
 *   access-token:
 *     expiration: 3600000  # 1 heure en millisecondes
 *   refresh-token:
 *     expiration: 604800000  # 7 jours en millisecondes
 * </pre>
 * 
 * @author JavaCraft Academy
 * @version 1.0
 * @since 1.0
 */
@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    /**
     * Clé secrète pour signer les tokens JWT.
     * Configurée via application.yml
     */
    @Value("${jwt.secret}")
    private String jwtSecret;

    /**
     * Durée d'expiration des tokens d'accès en millisecondes.
     * Par défaut : 1 heure (3600000 ms)
     */
    @Value("${jwt.access-token.expiration:3600000}")
    private long accessTokenExpiration;

    /**
     * Durée d'expiration des tokens de rafraîchissement en millisecondes.
     * Par défaut : 7 jours (604800000 ms)
     */
    @Value("${jwt.refresh-token.expiration:604800000}")
    private long refreshTokenExpiration;

    // Claims personnalisés
    public static final String CLAIM_USER_ID = "userId";
    public static final String CLAIM_USERNAME = "username";
    public static final String CLAIM_AUTHORITIES = "authorities";
    public static final String CLAIM_TOKEN_TYPE = "tokenType";
    public static final String CLAIM_TENANT_ID = "tenantId";
    public static final String CLAIM_SESSION_ID = "sessionId";

    // Types de tokens
    public static final String TOKEN_TYPE_ACCESS = "ACCESS";
    public static final String TOKEN_TYPE_REFRESH = "REFRESH";

    /**
     * Génère un token d'accès JWT pour un utilisateur authentifié.
     * 
     * @param userId Identifiant unique de l'utilisateur
     * @param username Nom d'utilisateur
     * @param authorities Liste des autorités/rôles de l'utilisateur
     * @param tenantId Identifiant du tenant (optionnel, peut être null)
     * @param sessionId Identifiant de session
     * @return Token JWT d'accès signé
     * @throws IllegalArgumentException si les paramètres obligatoires sont null ou vides
     */
    public String generateAccessToken(String userId, String username, String authorities, 
                                    String tenantId, String sessionId) {
        validateRequiredParams(userId, username, sessionId);
        
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_USER_ID, userId);
        claims.put(CLAIM_USERNAME, username);
        claims.put(CLAIM_AUTHORITIES, authorities);
        claims.put(CLAIM_TOKEN_TYPE, TOKEN_TYPE_ACCESS);
        claims.put(CLAIM_SESSION_ID, sessionId);
        
        if (tenantId != null && !tenantId.trim().isEmpty()) {
            claims.put(CLAIM_TENANT_ID, tenantId);
        }
        
        return createToken(claims, username, accessTokenExpiration);
    }

    /**
     * Génère un token de rafraîchissement JWT.
     * 
     * @param userId Identifiant unique de l'utilisateur
     * @param username Nom d'utilisateur
     * @param sessionId Identifiant de session
     * @return Token JWT de rafraîchissement signé
     * @throws IllegalArgumentException si les paramètres obligatoires sont null ou vides
     */
    public String generateRefreshToken(String userId, String username, String sessionId) {
        validateRequiredParams(userId, username, sessionId);
        
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_USER_ID, userId);
        claims.put(CLAIM_USERNAME, username);
        claims.put(CLAIM_TOKEN_TYPE, TOKEN_TYPE_REFRESH);
        claims.put(CLAIM_SESSION_ID, sessionId);
        
        return createToken(claims, username, refreshTokenExpiration);
    }

    /**
     * Extrait le nom d'utilisateur du token JWT.
     * 
     * @param token Token JWT à analyser
     * @return Nom d'utilisateur extrait du token
     * @throws JwtException si le token est invalide
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extrait l'identifiant utilisateur du token JWT.
     * 
     * @param token Token JWT à analyser
     * @return Identifiant utilisateur ou null si non présent
     */
    public String extractUserId(String token) {
        return extractClaim(token, claims -> claims.get(CLAIM_USER_ID, String.class));
    }

    /**
     * Extrait les autorités/rôles du token JWT.
     * 
     * @param token Token JWT à analyser
     * @return Chaîne des autorités ou null si non présente
     */
    public String extractAuthorities(String token) {
        return extractClaim(token, claims -> claims.get(CLAIM_AUTHORITIES, String.class));
    }

    /**
     * Extrait l'identifiant de session du token JWT.
     * 
     * @param token Token JWT à analyser
     * @return Identifiant de session ou null si non présent
     */
    public String extractSessionId(String token) {
        return extractClaim(token, claims -> claims.get(CLAIM_SESSION_ID, String.class));
    }

    /**
     * Extrait l'identifiant du tenant du token JWT.
     * 
     * @param token Token JWT à analyser
     * @return Identifiant du tenant ou null si non présent
     */
    public String extractTenantId(String token) {
        return extractClaim(token, claims -> claims.get(CLAIM_TENANT_ID, String.class));
    }

    /**
     * Extrait le type de token (ACCESS ou REFRESH).
     * 
     * @param token Token JWT à analyser
     * @return Type de token ou null si non présent
     */
    public String extractTokenType(String token) {
        return extractClaim(token, claims -> claims.get(CLAIM_TOKEN_TYPE, String.class));
    }

    /**
     * Extrait la date d'expiration du token JWT.
     * 
     * @param token Token JWT à analyser
     * @return Date d'expiration du token
     * @throws JwtException si le token est invalide
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extrait la date d'émission du token JWT.
     * 
     * @param token Token JWT à analyser
     * @return Date d'émission du token
     * @throws JwtException si le token est invalide
     */
    public Date extractIssuedAt(String token) {
        return extractClaim(token, Claims::getIssuedAt);
    }

    /**
     * Extrait un claim spécifique du token JWT.
     * 
     * @param <T> Type du claim à extraire
     * @param token Token JWT à analyser
     * @param claimsResolver Fonction pour extraire le claim des claims
     * @return Valeur du claim extrait
     * @throws JwtException si le token est invalide
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Vérifie si le token JWT est expiré.
     * 
     * @param token Token JWT à vérifier
     * @return true si le token est expiré, false sinon
     */
    public boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (JwtException e) {
            logger.warn("Erreur lors de la vérification de l'expiration du token: {}", e.getMessage());
            return true; // Considérer comme expiré si erreur
        }
    }

    /**
     * Valide un token JWT pour un utilisateur spécifique.
     * 
     * @param token Token JWT à valider
     * @param username Nom d'utilisateur attendu
     * @return true si le token est valide pour cet utilisateur, false sinon
     */
    public boolean validateToken(String token, String username) {
        try {
            final String extractedUsername = extractUsername(token);
            return (extractedUsername.equals(username) && !isTokenExpired(token));
        } catch (JwtException e) {
            logger.warn("Validation du token échouée pour l'utilisateur {}: {}", username, e.getMessage());
            return false;
        }
    }

    /**
     * Valide un token JWT de manière générale (structure, signature, expiration).
     * 
     * @param token Token JWT à valider
     * @return true si le token est valide, false sinon
     */
    public boolean isValidToken(String token) {
        try {
            extractAllClaims(token);
            return !isTokenExpired(token);
        } catch (JwtException e) {
            logger.warn("Token invalide: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Vérifie si un token est un token d'accès.
     * 
     * @param token Token JWT à vérifier
     * @return true si c'est un token d'accès, false sinon
     */
    public boolean isAccessToken(String token) {
        try {
            String tokenType = extractTokenType(token);
            return TOKEN_TYPE_ACCESS.equals(tokenType);
        } catch (JwtException e) {
            logger.warn("Erreur lors de la vérification du type de token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Vérifie si un token est un token de rafraîchissement.
     * 
     * @param token Token JWT à vérifier
     * @return true si c'est un token de rafraîchissement, false sinon
     */
    public boolean isRefreshToken(String token) {
        try {
            String tokenType = extractTokenType(token);
            return TOKEN_TYPE_REFRESH.equals(tokenType);
        } catch (JwtException e) {
            logger.warn("Erreur lors de la vérification du type de token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Calcule le temps restant avant expiration du token en secondes.
     * 
     * @param token Token JWT à analyser
     * @return Nombre de secondes avant expiration, 0 si expiré ou en cas d'erreur
     */
    public long getTimeToExpiration(String token) {
        try {
            Date expiration = extractExpiration(token);
            long timeToExpiration = expiration.getTime() - System.currentTimeMillis();
            return Math.max(0, timeToExpiration / 1000); // Retourner 0 si négatif
        } catch (JwtException e) {
            logger.warn("Erreur lors du calcul du temps d'expiration: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * Extrait tous les claims du token JWT.
     * 
     * @param token Token JWT à analyser
     * @return Claims du token
     * @throws JwtException si le token est invalide, expiré ou mal signé
     */
    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            logger.warn("Token JWT expiré: {}", e.getMessage());
            throw new JwtException("Token expiré", e);
        } catch (UnsupportedJwtException e) {
            logger.error("Token JWT non supporté: {}", e.getMessage());
            throw new JwtException("Format de token non supporté", e);
        } catch (MalformedJwtException e) {
            logger.error("Token JWT malformé: {}", e.getMessage());
            throw new JwtException("Token malformé", e);
        } catch (SignatureException e) {
            logger.error("Signature JWT invalide: {}", e.getMessage());
            throw new JwtException("Signature invalide", e);
        } catch (IllegalArgumentException e) {
            logger.error("Token JWT vide ou null: {}", e.getMessage());
            throw new JwtException("Token vide ou invalide", e);
        } catch (Exception e) {
            logger.error("Erreur inattendue lors du parsing du token: {}", e.getMessage());
            throw new JwtException("Erreur de traitement du token", e);
        }
    }

    /**
     * Crée un token JWT avec les claims spécifiés.
     * 
     * @param claims Claims à inclure dans le token
     * @param subject Sujet du token (généralement le nom d'utilisateur)
     * @param expiration Durée d'expiration en millisecondes
     * @return Token JWT signé
     */
    private String createToken(Map<String, Object> claims, String subject, long expiration) {
        Instant now = Instant.now();
        Instant expiryDate = now.plus(expiration, ChronoUnit.MILLIS);
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiryDate))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Obtient la clé de signature pour les tokens JWT.
     * 
     * @return Clé secrète pour signer/vérifier les tokens
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Valide les paramètres obligatoires pour la génération de tokens.
     * 
     * @param userId Identifiant utilisateur
     * @param username Nom d'utilisateur
     * @param sessionId Identifiant de session
     * @throws IllegalArgumentException si un paramètre obligatoire est null ou vide
     */
    private void validateRequiredParams(String userId, String username, String sessionId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("L'identifiant utilisateur ne peut pas être null ou vide");
        }
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom d'utilisateur ne peut pas être null ou vide");
        }
        if (sessionId == null || sessionId.trim().isEmpty()) {
            throw new IllegalArgumentException("L'identifiant de session ne peut pas être null ou vide");
        }
    }
}