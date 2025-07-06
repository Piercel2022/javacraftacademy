package com.javacraftacademy.gateway.exception;

/**
 * Exception spécialisée pour les erreurs d'authentification et d'autorisation
 * dans le service Gateway. Cette classe étend GatewayException pour fournir
 * une gestion spécifique des erreurs liées à la sécurité.
 * 
 * <h2>Fonctionnalités principales :</h2>
 * <ul>
 *   <li>Gestion des erreurs d'authentification JWT</li>
 *   <li>Gestion des erreurs d'autorisation (permissions insuffisantes)</li>
 *   <li>Support des différents types d'échecs de sécurité</li>
 *   <li>Intégration avec Spring Security</li>
 *   <li>Logging sécurisé (évite la fuite d'informations sensibles)</li>
 * </ul>
 * 
 * <h2>Relations avec l'application :</h2>
 * <ul>
 *   <li><strong>AuthenticationFilter</strong> : Principal lanceur de cette exception</li>
 *   <li><strong>AuthenticationService</strong> : Utilise cette exception pour signaler les échecs</li>
 *   <li><strong>JwtService</strong> : Lance cette exception en cas de token invalide</li>
 *   <li><strong>UserValidationService</strong> : Signale les problèmes de validation utilisateur</li>
 *   <li><strong>SecurityConfig</strong> : Configuration des règles de sécurité</li>
 *   <li><strong>GlobalExceptionHandler</strong> : Traite spécifiquement ces exceptions</li>
 * </ul>
 * 
 * <h2>Types d'erreurs gérées :</h2>
 * <ul>
 *   <li>Token JWT expiré ou invalide</li>
 *   <li>Utilisateur non authentifié</li>
 *   <li>Permissions insuffisantes pour la ressource</li>
 *   <li>Échec de validation des credentials</li>
 *   <li>Compte utilisateur désactivé ou bloqué</li>
 * </ul>
 * 
 * <h2>Codes d'erreur standardisés :</h2>
 * <ul>
 *   <li>INVALID_TOKEN : Token JWT invalide ou malformé</li>
 *   <li>EXPIRED_TOKEN : Token JWT expiré</li>
 *   <li>UNAUTHORIZED : Utilisateur non authentifié</li>
 *   <li>FORBIDDEN : Permissions insuffisantes</li>
 *   <li>INVALID_CREDENTIALS : Identifiants incorrects</li>
 *   <li>ACCOUNT_DISABLED : Compte utilisateur désactivé</li>
 * </ul>
 * 
 * @author JavaCraft Academy
 * @version 1.0
 * @since 1.0
 * 
 * @see GatewayException
 * @see GlobalExceptionHandler
 * @see com.javacraftacademy.gateway.filter.AuthenticationFilter
 * @see com.javacraftacademy.gateway.service.AuthenticationService
 */
public class AuthenticationException extends GatewayException {

    /**
     * Identifiant unique de sérialisation pour la compatibilité des versions.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Type d'erreur d'authentification pour une catégorisation fine.
     */
    public enum AuthErrorType {
        INVALID_TOKEN("Token invalide ou malformé"),
        EXPIRED_TOKEN("Token expiré"),
        MISSING_TOKEN("Token manquant"),
        UNAUTHORIZED("Utilisateur non authentifié"),
        FORBIDDEN("Accès interdit - permissions insuffisantes"),
        INVALID_CREDENTIALS("Identifiants incorrects"),
        ACCOUNT_DISABLED("Compte utilisateur désactivé"),
        ACCOUNT_LOCKED("Compte utilisateur verrouillé"),
        SESSION_EXPIRED("Session expirée");

        private final String description;

        AuthErrorType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Type d'erreur d'authentification spécifique.
     */
    private final AuthErrorType authErrorType;

    /**
     * Identifiant de l'utilisateur concerné (peut être null).
     */
    private final String userId;

    /**
     * Adresse IP de la requête (pour logging sécuritaire).
     */
    private final String clientIp;

    /**
     * Constructeur par défaut pour erreur d'authentification générique.
     */
    public AuthenticationException() {
        this("Erreur d'authentification", AuthErrorType.UNAUTHORIZED);
    }

    /**
     * Constructeur avec message personnalisé.
     * 
     * @param message Le message d'erreur descriptif
     */
    public AuthenticationException(String message) {
        this(message, AuthErrorType.UNAUTHORIZED);
    }

    /**
     * Constructeur avec message et type d'erreur.
     * 
     * @param message Le message d'erreur descriptif
     * @param authErrorType Le type spécifique d'erreur d'authentification
     */
    public AuthenticationException(String message, AuthErrorType authErrorType) {
        super(message, getHttpStatusForAuthError(authErrorType), getErrorCodeForAuthError(authErrorType));
        this.authErrorType = authErrorType;
        this.userId = null;
        this.clientIp = null;
    }

    /**
     * Constructeur avec message, type d'erreur et informations contextuelles.
     * 
     * @param message Le message d'erreur descriptif
     * @param authErrorType Le type spécifique d'erreur d'authentification
     * @param userId L'identifiant de l'utilisateur concerné (peut être null)
     * @param clientIp L'adresse IP du client (peut être null)
     */
    public AuthenticationException(String message, AuthErrorType authErrorType, String userId, String clientIp) {
        super(message, getHttpStatusForAuthError(authErrorType), getErrorCodeForAuthError(authErrorType));
        this.authErrorType = authErrorType;
        this.userId = userId;
        this.clientIp = clientIp;
    }

    /**
     * Constructeur avec cause et informations contextuelles.
     * 
     * @param message Le message d'erreur descriptif
     * @param cause L'exception causale
     * @param authErrorType Le type spécifique d'erreur d'authentification
     * @param userId L'identifiant de l'utilisateur concerné (peut être null)
     * @param clientIp L'adresse IP du client (peut être null)
     */
    public AuthenticationException(String message, Throwable cause, AuthErrorType authErrorType, 
                                 String userId, String clientIp) {
        super(message, cause, getHttpStatusForAuthError(authErrorType), getErrorCodeForAuthError(authErrorType));
        this.authErrorType = authErrorType;
        this.userId = userId;
        this.clientIp = clientIp;
    }

    /**
     * Retourne le type d'erreur d'authentification.
     * 
     * @return Le type d'erreur d'authentification
     */
    public AuthErrorType getAuthErrorType() {
        return authErrorType;
    }

    /**
     * Retourne l'identifiant de l'utilisateur concerné.
     * 
     * @return L'identifiant utilisateur ou null si non disponible
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Retourne l'adresse IP du client.
     * 
     * @return L'adresse IP du client ou null si non disponible
     */
    public String getClientIp() {
        return clientIp;
    }

    /**
     * Méthode utilitaire pour créer une exception de token invalide.
     * 
     * @param tokenType Le type de token (JWT, Bearer, etc.)
     * @return Une nouvelle instance d'AuthenticationException configurée
     */
    public static AuthenticationException invalidToken(String tokenType) {
        return new AuthenticationException(
            String.format("Token %s invalide ou malformé", tokenType),
            AuthErrorType.INVALID_TOKEN
        );
    }

    /**
     * Méthode utilitaire pour créer une exception de token expiré.
     * 
     * @param expiredAt La date/heure d'expiration du token
     * @return Une nouvelle instance d'AuthenticationException configurée
     */
    public static AuthenticationException expiredToken(String expiredAt) {
        return new AuthenticationException(
            String.format("Token expiré le %s", expiredAt),
            AuthErrorType.EXPIRED_TOKEN
        );
    }

    /**
     * Méthode utilitaire pour créer une exception de token manquant.
     * 
     * @return Une nouvelle instance d'AuthenticationException configurée
     */
    public static AuthenticationException missingToken() {
        return new AuthenticationException(
            "Token d'authentification manquant dans la requête",
            AuthErrorType.MISSING_TOKEN
        );
    }

    /**
     * Méthode utilitaire pour créer une exception d'accès interdit.
     * 
     * @param resource La ressource à laquelle l'accès est refusé
     * @param userId L'identifiant de l'utilisateur
     * @return Une nouvelle instance d'AuthenticationException configurée
     */
    public static AuthenticationException forbidden(String resource, String userId) {
        return new AuthenticationException(
            String.format("Accès interdit à la ressource '%s'", resource),
            AuthErrorType.FORBIDDEN,
            userId,
            null
        );
    }

    /**
     * Méthode utilitaire pour créer une exception d'identifiants invalides.
     * 
     * @param username Le nom d'utilisateur (pour logging)
     * @param clientIp L'adresse IP du client
     * @return Une nouvelle instance d'AuthenticationException configurée
     */
    public static AuthenticationException invalidCredentials(String username, String clientIp) {
        return new AuthenticationException(
            "Identifiants incorrects",
            AuthErrorType.INVALID_CREDENTIALS,
            username,
            clientIp
        );
    }

    /**
     * Méthode utilitaire pour créer une exception de compte désactivé.
     * 
     * @param userId L'identifiant de l'utilisateur
     * @return Une nouvelle instance d'AuthenticationException configurée
     */
    public static AuthenticationException accountDisabled(String userId) {
        return new AuthenticationException(
            "Compte utilisateur désactivé",
            AuthErrorType.ACCOUNT_DISABLED,
            userId,
            null
        );
    }

    /**
     * Détermine le code de statut HTTP approprié selon le type d'erreur.
     * 
     * @param authErrorType Le type d'erreur d'authentification
     * @return Le code de statut HTTP correspondant
     */
    private static int getHttpStatusForAuthError(AuthErrorType authErrorType) {
        switch (authErrorType) {
            case FORBIDDEN:
                return 403; // Forbidden
            case INVALID_TOKEN:
            case EXPIRED_TOKEN:
            case MISSING_TOKEN:
            case UNAUTHORIZED:
            case INVALID_CREDENTIALS:
            case ACCOUNT_DISABLED:
            case ACCOUNT_LOCKED:
            case SESSION_EXPIRED:
            default:
                return 401; // Unauthorized
        }
    }

    /**
     * Détermine le code d'erreur métier selon le type d'erreur.
     * 
     * @param authErrorType Le type d'erreur d'authentification
     * @return Le code d'erreur métier correspondant
     */
    private static String getErrorCodeForAuthError(AuthErrorType authErrorType) {
        return "AUTH_" + authErrorType.name();
    }

    /**
     * Vérifie si cette exception nécessite un logging de sécurité.
     * 
     * @return true si l'exception doit être loggée pour sécurité
     */
    public boolean requiresSecurityLogging() {
        return authErrorType == AuthErrorType.INVALID_CREDENTIALS ||
               authErrorType == AuthErrorType.ACCOUNT_LOCKED ||
               authErrorType == AuthErrorType.INVALID_TOKEN;
    }

    /**
     * Génère un message sécurisé pour les logs (évite la fuite d'informations).
     * 
     * @return Un message approprié pour les logs de sécurité
     */
    public String getSecureLogMessage() {
        StringBuilder logMessage = new StringBuilder();
        logMessage.append("Échec d'authentification: ").append(authErrorType.getDescription());
        
        if (userId != null) {
            // Masquer partiellement l'ID utilisateur pour la sécurité
            String maskedUserId = userId.length() > 3 ? 
                userId.substring(0, 3) + "***" : "***";
            logMessage.append(" [User: ").append(maskedUserId).append("]");
        }
        
        if (clientIp != null) {
            logMessage.append(" [IP: ").append(clientIp).append("]");
        }
        
        return logMessage.toString();
    }

    /**
     * Redéfinition de toString pour un affichage sécurisé de l'exception.
     * Évite l'exposition d'informations sensibles.
     * 
     * @return Une représentation textuelle sécurisée de l'exception
     */
    @Override
    public String toString() {
        return String.format(
            "AuthenticationException{type=%s, httpStatus=%d, errorCode='%s', timestamp=%d}",
            authErrorType.name(),
            getHttpStatus(),
            getErrorCode(),
            getTimestamp()
        );
    }
}