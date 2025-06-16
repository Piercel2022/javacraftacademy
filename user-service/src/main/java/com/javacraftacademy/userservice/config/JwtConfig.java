
package com.javacraftacademy.userservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration centralisée pour la gestion des tokens JWT dans l'application JavaCraft Academy.
 * 
 * <p><strong>But :</strong></p>
 * Cette classe centralise tous les paramètres de configuration nécessaires à la génération,
 * validation et gestion des tokens JWT (JSON Web Token) utilisés pour l'authentification
 * et l'autorisation dans le microservice utilisateur.
 * 
 * <p><strong>Fonctionnalités principales :</strong></p>
 * <ul>
 *   <li>Configuration de la clé secrète pour signer les tokens JWT</li>
 *   <li>Définition des durées d'expiration pour les access tokens et refresh tokens</li>
 *   <li>Configuration du format des headers HTTP pour l'authentification</li>
 *   <li>Gestion du préfixe des tokens (Bearer)</li>
 *   <li>Configuration de l'émetteur (issuer) des tokens</li>
 *   <li>Méthodes utilitaires pour la manipulation des tokens</li>
 * </ul>
 * 
 * <p><strong>Relations avec l'application :</strong></p>
 * <ul>
 *   <li><strong>JwtService :</strong> Utilise cette configuration pour générer et valider les tokens</li>
 *   <li><strong>SecurityConfig :</strong> S'appuie sur ces paramètres pour configurer la sécurité Spring</li>
 *   <li><strong>JwtAuthenticationFilter :</strong> Utilise les méthodes utilitaires pour extraire les tokens des headers</li>
 *   <li><strong>AuthController :</strong> Exploite la configuration pour les opérations d'authentification</li>
 *   <li><strong>application.yml/properties :</strong> Les valeurs peuvent être externalisées via le préfixe "app.jwt"</li>
 * </ul>
 * 
 * <p><strong>Configuration externe :</strong></p>
 * Les propriétés peuvent être surchargées via les fichiers de configuration Spring Boot :
 * <pre>
 * app:
 *   jwt:
 *     secret: "your-custom-secret-key"
 *     access-token-expiration-in-ms: 3600000  # 1 heure
 *     refresh-token-expiration-in-ms: 86400000 # 24 heures
 * </pre>
 * 
 * @author JavaCraft Academy
 * @version 1.0
 * @since 2024
 */
@Configuration
@ConfigurationProperties(prefix = "app.jwt")
public class JwtConfig {
    
    /**
     * Clé secrète utilisée pour signer et vérifier les tokens JWT.
     * Cette clé doit être gardée confidentielle et suffisamment complexe.
     */
    private String secret = "javaCraftAcademySecretKeyForJWTTokenGeneration2024";
    
    /**
     * Durée d'expiration des access tokens en millisecondes.
     * Par défaut : 86400000 ms (24 heures).
     */
    private int accessTokenExpirationInMs = 86400000; // 24 hours
    
    /**
     * Durée d'expiration des refresh tokens en millisecondes.
     * Par défaut : 604800000 ms (7 jours).
     */
    private int refreshTokenExpirationInMs = 604800000; // 7 days
    
    /**
     * Préfixe utilisé dans les headers HTTP pour identifier les tokens JWT.
     * Par défaut : "Bearer " (avec espace).
     */
    private String tokenPrefix = "Bearer ";
    
    /**
     * Nom du header HTTP contenant le token d'authentification.
     * Par défaut : "Authorization".
     */
    private String headerString = "Authorization";
    
    /**
     * Identifiant de l'émetteur (issuer) des tokens JWT.
     * Utilisé dans les claims du token pour identifier l'origine.
     */
    private String issuer = "JavaCraft Academy";

    // Getters and setters
    
    /**
     * Retourne la clé secrète utilisée pour signer les tokens.
     * 
     * @return la clé secrète JWT
     */
    public String getSecret() {
        return secret;
    }

    /**
     * Définit la clé secrète pour signer les tokens.
     * 
     * @param secret la nouvelle clé secrète
     */
    public void setSecret(String secret) {
        this.secret = secret;
    }

    /**
     * Retourne la durée d'expiration des access tokens en millisecondes.
     * 
     * @return durée d'expiration en ms
     */
    public int getAccessTokenExpirationInMs() {
        return accessTokenExpirationInMs;
    }

    /**
     * Définit la durée d'expiration des access tokens.
     * 
     * @param accessTokenExpirationInMs durée en millisecondes
     */
    public void setAccessTokenExpirationInMs(int accessTokenExpirationInMs) {
        this.accessTokenExpirationInMs = accessTokenExpirationInMs;
    }

    /**
     * Retourne la durée d'expiration des refresh tokens en millisecondes.
     * 
     * @return durée d'expiration en ms
     */
    public int getRefreshTokenExpirationInMs() {
        return refreshTokenExpirationInMs;
    }

    /**
     * Définit la durée d'expiration des refresh tokens.
     * 
     * @param refreshTokenExpirationInMs durée en millisecondes
     */
    public void setRefreshTokenExpirationInMs(int refreshTokenExpirationInMs) {
        this.refreshTokenExpirationInMs = refreshTokenExpirationInMs;
    }

    /**
     * Retourne le préfixe utilisé pour les tokens dans les headers HTTP.
     * 
     * @return le préfixe du token (ex: "Bearer ")
     */
    public String getTokenPrefix() {
        return tokenPrefix;
    }

    /**
     * Définit le préfixe pour les tokens.
     * 
     * @param tokenPrefix le nouveau préfixe
     */
    public void setTokenPrefix(String tokenPrefix) {
        this.tokenPrefix = tokenPrefix;
    }

    /**
     * Retourne le nom du header HTTP contenant le token.
     * 
     * @return nom du header (ex: "Authorization")
     */
    public String getHeaderString() {
        return headerString;
    }

    /**
     * Définit le nom du header HTTP pour les tokens.
     * 
     * @param headerString nom du header
     */
    public void setHeaderString(String headerString) {
        this.headerString = headerString;
    }

    /**
     * Retourne l'identifiant de l'émetteur des tokens.
     * 
     * @return l'issuer des tokens JWT
     */
    public String getIssuer() {
        return issuer;
    }

    /**
     * Définit l'émetteur des tokens JWT.
     * 
     * @param issuer le nouvel émetteur
     */
    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    // Utility methods
    
    /**
     * Convertit la durée d'expiration des access tokens de millisecondes en secondes.
     * Utile pour les bibliothèques qui requièrent la durée en secondes.
     * 
     * @return durée d'expiration en secondes
     */
    public long getAccessTokenExpirationInSeconds() {
        return accessTokenExpirationInMs / 1000;
    }

    /**
     * Convertit la durée d'expiration des refresh tokens de millisecondes en secondes.
     * Utile pour les bibliothèques qui requièrent la durée en secondes.
     * 
     * @return durée d'expiration en secondes
     */
    public long getRefreshTokenExpirationInSeconds() {
        return refreshTokenExpirationInMs / 1000;
    }

    /**
     * Vérifie si un token contient le préfixe valide.
     * 
     * @param token le token à vérifier
     * @return true si le token a le bon préfixe, false sinon
     */
    public boolean isTokenPrefixValid(String token) {
        return token != null && token.startsWith(tokenPrefix);
    }

    /**
     * Extrait le token JWT pur à partir du header d'autorisation.
     * Supprime le préfixe "Bearer " pour ne garder que le token.
     * 
     * @param authHeader la valeur complète du header Authorization
     * @return le token JWT sans préfixe, ou null si le header est invalide
     * 
     * @example
     * <pre>
     * String header = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...";
     * String token = jwtConfig.extractTokenFromHeader(header);
     * // token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
     * </pre>
     */
    public String extractTokenFromHeader(String authHeader) {
        if (isTokenPrefixValid(authHeader)) {
            return authHeader.substring(tokenPrefix.length());
        }
        return null;
    }
}