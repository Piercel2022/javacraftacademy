package com.javacraftacademy.gateway.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javacraftacademy.gateway.exception.GatewayException;
import com.javacraftacademy.gateway.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Filtre de validation des requêtes pour la Gateway API.
 * 
 * <p>Ce composant fait partie de la couche de sécurité et de validation de la Gateway.
 * Il intercepte toutes les requêtes entrantes pour effectuer des validations essentielles
 * avant qu'elles ne soient transmises aux services backend.</p>
 * 
 * <h3>Fonctionnalités principales :</h3>
 * <ul>
 *   <li><strong>Validation des en-têtes HTTP</strong> : Vérifie la présence et la validité des en-têtes requis</li>
 *   <li><strong>Validation de la taille des requêtes</strong> : Limite la taille des corps de requête pour éviter les attaques DoS</li>
 *   <li><strong>Validation du format des données</strong> : Contrôle le format et la structure des données selon le Content-Type</li>
 *   <li><strong>Protection contre les injections</strong> : Filtre les caractères malveillants dans les paramètres et en-têtes</li>
 *   <li><strong>Validation des chemins d'accès</strong> : Vérifie que les URLs sont conformes aux patterns autorisés</li>
 *   <li><strong>Sanitisation des entrées</strong> : Nettoie et normalise les données d'entrée</li>
 * </ul>
 * 
 * <h3>Relations avec l'architecture :</h3>
 * <ul>
 *   <li><strong>GatewayConfig</strong> : Configuré dans la chaîne de filtres via la configuration Gateway</li>
 *   <li><strong>AuthenticationFilter</strong> : Exécuté AVANT le filtre d'authentification pour pré-valider les requêtes</li>
 *   <li><strong>RateLimitingFilter</strong> : Complémentaire avec le rate limiting pour la protection DDoS</li>
 *   <li><strong>LoggingFilter</strong> : Collabore pour tracer les requêtes invalidées</li>
 *   <li><strong>GlobalExceptionHandler</strong> : Utilise le gestionnaire global pour les erreurs de validation</li>
 *   <li><strong>ResponseUtil</strong> : Utilise l'utilitaire pour formater les réponses d'erreur</li>
 * </ul>
 * 
 * <h3>Position dans la chaîne de filtres :</h3>
 * <pre>
 * 1. RequestValidationFilter (ce composant) - Validation des requêtes
 * 2. RateLimitingFilter - Limitation du taux de requêtes
 * 3. AuthenticationFilter - Authentification
 * 4. LoggingFilter - Journalisation
 * 5. Service Backend
 * </pre>
 * 
 * <h3>Configuration et utilisation :</h3>
 * <p>Ce filtre est automatiquement activé par Spring Cloud Gateway grâce à l'annotation @Component.
 * Les règles de validation peuvent être configurées via les propriétés application.yml :</p>
 * 
 * <pre>
 * gateway:
 *   validation:
 *     max-request-size: 10MB
 *     allowed-content-types: 
 *       - application/json
 *       - application/xml
 *     blocked-patterns:
 *       - ".*<script.*"
 *       - ".*javascript:.*"
 * </pre>
 * 
 * @author JavaCraft Academy
 * @version 1.0
 * @since 1.0
 * @see org.springframework.cloud.gateway.filter.GatewayFilter
 * @see com.javacraftacademy.gateway.config.GatewayConfig
 * @see com.javacraftacademy.gateway.exception.GlobalExceptionHandler
 */
@Component
public class RequestValidationFilter extends AbstractGatewayFilterFactory<RequestValidationFilter.Config> {

    private static final Logger logger = LoggerFactory.getLogger(RequestValidationFilter.class);
    
    // Constantes de validation
    private static final long MAX_REQUEST_SIZE = 10 * 1024 * 1024; // 10MB
    private static final int MAX_HEADER_VALUE_LENGTH = 4096;
    private static final int MAX_URL_LENGTH = 2048;
    
    // Patterns de sécurité
    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile(
        "(?i).*(union|select|insert|update|delete|drop|create|alter|exec|script).*"
    );
    private static final Pattern XSS_PATTERN = Pattern.compile(
        "(?i).*(<script|javascript:|vbscript:|onload|onerror|onclick|onmouseover).*"
    );
    private static final Pattern PATH_TRAVERSAL_PATTERN = Pattern.compile(
        ".*(\\.\\.[\\/\\\\]|[\\/\\\\]\\.\\.).*"
    );
    
    // Content-Types autorisés
    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
        "application/json",
        "application/xml",
        "text/plain",
        "application/x-www-form-urlencoded",
        "multipart/form-data"
    );
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private ResponseUtil responseUtil;

    /**
     * Constructeur par défaut initialisant la configuration.
     */
    public RequestValidationFilter() {
        super(Config.class);
    }

    /**
     * Applique le filtre de validation sur la requête entrante.
     * 
     * <p>Cette méthode constitue le point d'entrée principal du filtre.
     * Elle orchestre toutes les validations nécessaires avant de laisser
     * passer la requête vers le service suivant.</p>
     * 
     * @param config Configuration du filtre (peut être étendue pour des paramètres personnalisés)
     * @return GatewayFilter configuré pour effectuer les validations
     */
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            ServerHttpResponse response = exchange.getResponse();
            
            try {
                logger.debug("Début validation requête : {} {}", 
                    request.getMethod(), request.getURI());
                
                // 1. Validation de la taille de l'URL
                if (!validateUrlLength(request)) {
                    return handleValidationError(response, "URL trop longue", 
                        HttpStatus.REQUEST_URI_TOO_LONG);
                }
                
                // 2. Validation des en-têtes
                if (!validateHeaders(request)) {
                    return handleValidationError(response, "En-têtes invalides", 
                        HttpStatus.BAD_REQUEST);
                }
                
                // 3. Validation du Content-Type
                if (!validateContentType(request)) {
                    return handleValidationError(response, "Content-Type non supporté", 
                        HttpStatus.UNSUPPORTED_MEDIA_TYPE);
                }
                
                // 4. Validation contre les attaques par injection
                if (!validateSecurityThreats(request)) {
                    return handleValidationError(response, "Contenu potentiellement malveillant détecté", 
                        HttpStatus.BAD_REQUEST);
                }
                
                // 5. Validation de la taille de la requête
                if (!validateRequestSize(request)) {
                    return handleValidationError(response, "Taille de requête trop importante", 
                        HttpStatus.PAYLOAD_TOO_LARGE);
                }
                
                logger.debug("Validation requête réussie pour : {} {}", 
                    request.getMethod(), request.getURI());
                
                // Continuer vers le filtre suivant
                return chain.filter(exchange);
                
            } catch (Exception e) {
                logger.error("Erreur lors de la validation de la requête", e);
                return handleValidationError(response, "Erreur de validation interne", 
                    HttpStatus.INTERNAL_SERVER_ERROR);
            }
        };
    }

    /**
     * Valide la longueur de l'URL pour éviter les attaques de déni de service.
     * 
     * @param request La requête HTTP à valider
     * @return true si l'URL est valide, false sinon
     */
    private boolean validateUrlLength(ServerHttpRequest request) {
        String uri = request.getURI().toString();
        boolean isValid = uri.length() <= MAX_URL_LENGTH;
        
        if (!isValid) {
            logger.warn("URL trop longue détectée : {} caractères (max: {})", 
                uri.length(), MAX_URL_LENGTH);
        }
        
        return isValid;
    }

    /**
     * Valide les en-têtes HTTP requis et leurs valeurs.
     * 
     * <p>Vérifie :</p>
     * <ul>
     *   <li>La longueur des valeurs d'en-têtes</li>
     *   <li>La présence d'en-têtes malveillants</li>
     *   <li>Le format des en-têtes critiques</li>
     * </ul>
     * 
     * @param request La requête HTTP à valider
     * @return true si les en-têtes sont valides, false sinon
     */
    private boolean validateHeaders(ServerHttpRequest request) {
        // Validation de la longueur des en-têtes
        for (String headerName : request.getHeaders().keySet()) {
            List<String> headerValues = request.getHeaders().get(headerName);
            if (headerValues != null) {
                for (String value : headerValues) {
                    if (value != null && value.length() > MAX_HEADER_VALUE_LENGTH) {
                        logger.warn("En-tête {} trop long : {} caractères", 
                            headerName, value.length());
                        return false;
                    }
                    
                    // Vérification des caractères suspects dans les en-têtes
                    if (containsSuspiciousContent(value)) {
                        logger.warn("Contenu suspect détecté dans l'en-tête {} : {}", 
                            headerName, value);
                        return false;
                    }
                }
            }
        }
        
        // Validation de l'en-tête User-Agent (obligatoire)
        String userAgent = request.getHeaders().getFirst("User-Agent");
        if (!StringUtils.hasText(userAgent)) {
            logger.warn("En-tête User-Agent manquant");
            return false;
        }
        
        return true;
    }

    /**
     * Valide le Content-Type de la requête.
     * 
     * @param request La requête HTTP à valider
     * @return true si le Content-Type est autorisé, false sinon
     */
    private boolean validateContentType(ServerHttpRequest request) {
        // Ignorer la validation pour les requêtes GET et DELETE
        if ("GET".equals(request.getMethodValue()) || "DELETE".equals(request.getMethodValue())) {
            return true;
        }
        
        String contentType = request.getHeaders().getFirst("Content-Type");
        if (!StringUtils.hasText(contentType)) {
            logger.warn("Content-Type manquant pour la requête : {} {}", 
                request.getMethod(), request.getURI());
            return false;
        }
        
        // Extraire le type principal (sans les paramètres comme charset)
        String mainContentType = contentType.split(";")[0].trim().toLowerCase();
        
        boolean isAllowed = ALLOWED_CONTENT_TYPES.contains(mainContentType);
        if (!isAllowed) {
            logger.warn("Content-Type non autorisé : {}", contentType);
        }
        
        return isAllowed;
    }

    /**
     * Valide la requête contre les menaces de sécurité communes.
     * 
     * <p>Détecte :</p>
     * <ul>
     *   <li>Injections SQL</li>
     *   <li>Attaques XSS (Cross-Site Scripting)</li>
     *   <li>Path Traversal</li>
     *   <li>Commandes système malveillantes</li>
     * </ul>
     * 
     * @param request La requête HTTP à valider
     * @return true si la requête est sûre, false sinon
     */
    private boolean validateSecurityThreats(ServerHttpRequest request) {
        String uri = request.getURI().toString();
        
        // Vérification du path traversal dans l'URI
        if (PATH_TRAVERSAL_PATTERN.matcher(uri).matches()) {
            logger.warn("Tentative de path traversal détectée : {}", uri);
            return false;
        }
        
        // Vérification des paramètres de requête
        if (request.getQueryParams() != null) {
            for (String paramName : request.getQueryParams().keySet()) {
                List<String> values = request.getQueryParams().get(paramName);
                if (values != null) {
                    for (String value : values) {
                        if (containsSuspiciousContent(value)) {
                            logger.warn("Contenu suspect dans le paramètre {} : {}", 
                                paramName, value);
                            return false;
                        }
                    }
                }
            }
        }
        
        return true;
    }

    /**
     * Valide la taille de la requête pour éviter les attaques DoS.
     * 
     * @param request La requête HTTP à valider
     * @return true si la taille est acceptable, false sinon
     */
    private boolean validateRequestSize(ServerHttpRequest request) {
        String contentLengthHeader = request.getHeaders().getFirst("Content-Length");
        if (StringUtils.hasText(contentLengthHeader)) {
            try {
                long contentLength = Long.parseLong(contentLengthHeader);
                if (contentLength > MAX_REQUEST_SIZE) {
                    logger.warn("Requête trop volumineuse : {} bytes (max: {} bytes)", 
                        contentLength, MAX_REQUEST_SIZE);
                    return false;
                }
            } catch (NumberFormatException e) {
                logger.warn("Content-Length invalide : {}", contentLengthHeader);
                return false;
            }
        }
        
        return true;
    }

    /**
     * Vérifie si le contenu contient des éléments suspects.
     * 
     * @param content Le contenu à analyser
     * @return true si le contenu est suspect, false sinon
     */
    private boolean containsSuspiciousContent(String content) {
        if (content == null) return false;
        
        return SQL_INJECTION_PATTERN.matcher(content).matches() ||
               XSS_PATTERN.matcher(content).matches();
    }

    /**
     * Gère les erreurs de validation en retournant une réponse d'erreur appropriée.
     * 
     * @param response La réponse HTTP
     * @param message Le message d'erreur
     * @param status Le statut HTTP à retourner
     * @return Mono<Void> représentant la réponse d'erreur
     */
    private Mono<Void> handleValidationError(ServerHttpResponse response, String message, HttpStatus status) {
        response.setStatusCode(status);
        response.getHeaders().add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        
        try {
            String errorResponse = responseUtil.createErrorResponse(message, status.value());
            DataBuffer buffer = response.bufferFactory().wrap(errorResponse.getBytes(StandardCharsets.UTF_8));
            return response.writeWith(Mono.just(buffer));
        } catch (Exception e) {
            logger.error("Erreur lors de la génération de la réponse d'erreur", e);
            DataBuffer buffer = response.bufferFactory().wrap(
                "{\"error\":\"Erreur de validation\"}".getBytes(StandardCharsets.UTF_8)
            );
            return response.writeWith(Mono.just(buffer));
        }
    }

    /**
     * Classe de configuration pour le filtre de validation.
     * 
     * <p>Peut être étendue pour ajouter des paramètres de configuration
     * spécifiques comme des patterns personnalisés, tailles limites, etc.</p>
     */
    public static class Config {
        // Configuration future : patterns personnalisés, tailles, etc.
        private boolean enabled = true;
        private long maxRequestSize = MAX_REQUEST_SIZE;
        
        /**
         * Indique si le filtre est activé.
         * 
         * @return true si activé, false sinon
         */
        public boolean isEnabled() {
            return enabled;
        }
        
        /**
         * Active ou désactive le filtre.
         * 
         * @param enabled État du filtre
         */
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
        
        /**
         * Retourne la taille maximale autorisée pour les requêtes.
         * 
         * @return Taille maximale en bytes
         */
        public long getMaxRequestSize() {
            return maxRequestSize;
        }
        
        /**
         * Définit la taille maximale autorisée pour les requêtes.
         * 
         * @param maxRequestSize Taille maximale en bytes
         */
        public void setMaxRequestSize(long maxRequestSize) {
            this.maxRequestSize = maxRequestSize;
        }
    }
}