package com.javacraftacademy.gateway.exception;

/**
 * Exception personnalisée pour les erreurs spécifiques au service Gateway.
 * Cette classe représente la hiérarchie d'exceptions racine pour toutes les erreurs
 * liées aux opérations de passerelle dans l'architecture microservices.
 * 
 * <h2>Fonctionnalités principales :</h2>
 * <ul>
 *   <li>Gestion centralisée des erreurs de passerelle</li>
 *   <li>Support des codes d'erreur HTTP personnalisés</li>
 *   <li>Traçabilité complète des erreurs avec stack trace</li>
 *   <li>Intégration avec le système de logging global</li>
 *   <li>Support des erreurs chaînées (cause)</li>
 * </ul>
 * 
 * <h2>Relations avec l'application :</h2>
 * <ul>
 *   <li><strong>GlobalExceptionHandler</strong> : Intercepte et traite ces exceptions</li>
 *   <li><strong>AuthenticationFilter</strong> : Lance cette exception en cas d'erreur de routage</li>
 *   <li><strong>RateLimitingFilter</strong> : Utilise cette exception pour signaler les limites dépassées</li>
 *   <li><strong>GatewayController</strong> : Peut propager ces exceptions vers le handler global</li>
 *   <li><strong>Services</strong> : Tous les services peuvent lever cette exception</li>
 * </ul>
 * 
 * <h2>Cas d'usage typiques :</h2>
 * <ul>
 *   <li>Erreurs de routage vers les microservices</li>
 *   <li>Échecs de communication avec les services downstream</li>
 *   <li>Erreurs de configuration de la passerelle</li>
 *   <li>Problèmes de connectivité réseau</li>
 *   <li>Timeouts des requêtes</li>
 * </ul>
 * 
 * @author JavaCraft Academy
 * @version 1.0
 * @since 1.0
 * 
 * @see GlobalExceptionHandler
 * @see AuthenticationException
 * @see org.springframework.web.server.ResponseStatusException
 */
public class GatewayException extends RuntimeException {

    /**
     * Identifiant unique de sérialisation pour la compatibilité des versions.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Code d'erreur HTTP associé à cette exception.
     * Par défaut, utilise le code 500 (Internal Server Error).
     */
    private final int httpStatus;

    /**
     * Code d'erreur métier spécifique à l'application.
     * Permet une identification précise du type d'erreur.
     */
    private final String errorCode;

    /**
     * Timestamp de création de l'exception pour le debugging.
     */
    private final long timestamp;

    /**
     * Constructeur par défaut.
     * Crée une exception avec un message générique et un code d'erreur 500.
     */
    public GatewayException() {
        this("Une erreur interne s'est produite dans la passerelle", 500, "GATEWAY_ERROR");
    }

    /**
     * Constructeur avec message personnalisé.
     * 
     * @param message Le message d'erreur descriptif
     */
    public GatewayException(String message) {
        this(message, 500, "GATEWAY_ERROR");
    }

    /**
     * Constructeur avec message et cause.
     * Permet de chaîner les exceptions pour une meilleure traçabilité.
     * 
     * @param message Le message d'erreur descriptif
     * @param cause L'exception causale (peut être null)
     */
    public GatewayException(String message, Throwable cause) {
        this(message, cause, 500, "GATEWAY_ERROR");
    }

    /**
     * Constructeur avec message et code d'erreur HTTP.
     * 
     * @param message Le message d'erreur descriptif
     * @param httpStatus Le code de statut HTTP à retourner
     */
    public GatewayException(String message, int httpStatus) {
        this(message, httpStatus, "GATEWAY_ERROR");
    }

    /**
     * Constructeur complet avec message, code HTTP et code métier.
     * 
     * @param message Le message d'erreur descriptif
     * @param httpStatus Le code de statut HTTP à retourner
     * @param errorCode Le code d'erreur métier spécifique
     */
    public GatewayException(String message, int httpStatus, String errorCode) {
        super(message);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * Constructeur complet avec cause.
     * 
     * @param message Le message d'erreur descriptif
     * @param cause L'exception causale
     * @param httpStatus Le code de statut HTTP à retourner
     * @param errorCode Le code d'erreur métier spécifique
     */
    public GatewayException(String message, Throwable cause, int httpStatus, String errorCode) {
        super(message, cause);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * Retourne le code de statut HTTP associé à cette exception.
     * 
     * @return Le code de statut HTTP (par exemple: 400, 404, 500)
     */
    public int getHttpStatus() {
        return httpStatus;
    }

    /**
     * Retourne le code d'erreur métier spécifique.
     * 
     * @return Le code d'erreur métier sous forme de chaîne
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * Retourne le timestamp de création de l'exception.
     * 
     * @return Le timestamp en millisecondes depuis l'époque Unix
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Méthode utilitaire pour créer une exception de service indisponible.
     * 
     * @param serviceName Le nom du service qui n'est pas disponible
     * @return Une nouvelle instance de GatewayException configurée
     */
    public static GatewayException serviceUnavailable(String serviceName) {
        return new GatewayException(
            String.format("Le service '%s' n'est pas disponible", serviceName),
            503,
            "SERVICE_UNAVAILABLE"
        );
    }

    /**
     * Méthode utilitaire pour créer une exception de timeout.
     * 
     * @param serviceName Le nom du service qui a eu un timeout
     * @param timeoutMs Le timeout en millisecondes
     * @return Une nouvelle instance de GatewayException configurée
     */
    public static GatewayException timeout(String serviceName, long timeoutMs) {
        return new GatewayException(
            String.format("Timeout lors de l'appel au service '%s' (timeout: %dms)", serviceName, timeoutMs),
            504,
            "GATEWAY_TIMEOUT"
        );
    }

    /**
     * Méthode utilitaire pour créer une exception de routage.
     * 
     * @param path Le chemin qui n'a pas pu être routé
     * @return Une nouvelle instance de GatewayException configurée
     */
    public static GatewayException routingError(String path) {
        return new GatewayException(
            String.format("Impossible de router la requête vers le chemin '%s'", path),
            404,
            "ROUTING_ERROR"
        );
    }

    /**
     * Méthode utilitaire pour créer une exception de configuration.
     * 
     * @param configKey La clé de configuration manquante ou invalide
     * @return Une nouvelle instance de GatewayException configurée
     */
    public static GatewayException configurationError(String configKey) {
        return new GatewayException(
            String.format("Erreur de configuration pour la clé '%s'", configKey),
            500,
            "CONFIGURATION_ERROR"
        );
    }

    /**
     * Redéfinition de toString pour un affichage détaillé de l'exception.
     * Inclut le code d'erreur, le statut HTTP et le timestamp.
     * 
     * @return Une représentation textuelle complète de l'exception
     */
    @Override
    public String toString() {
        return String.format(
            "GatewayException{message='%s', httpStatus=%d, errorCode='%s', timestamp=%d}",
            getMessage(),
            httpStatus,
            errorCode,
            timestamp
        );
    }
}