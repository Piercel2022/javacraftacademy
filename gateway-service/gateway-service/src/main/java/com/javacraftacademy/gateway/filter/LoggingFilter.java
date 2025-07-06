package com.javacraftacademy.gateway.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Filtre de journalisation pour la Gateway API.
 * 
 * <p>Ce composant fait partie du système d'observabilité et de monitoring de la Gateway.
 * Il capture, trace et journalise toutes les interactions avec l'API Gateway pour
 * faciliter le debugging, l'audit et l'analyse de performance.</p>
 * 
 * <h3>Fonctionnalités principales :</h3>
 * <ul>
 *   <li><strong>Traçabilité des requêtes</strong> : Génère un ID unique pour chaque requête</li>
 *   <li><strong>Journalisation structurée</strong> : Log au format JSON pour faciliter l'analyse</li>
 *   <li><strong>Mesure de performance</strong> : Calcule le temps de réponse de chaque requête</li>
 *   <li><strong>Capture des métadonnées</strong> : Enregistre les en-têtes, paramètres et informations client</li>
 *   <li><strong>Logging conditionnel</strong> : Niveaux de log configurables selon l'environnement</li>
 *   <li><strong>Corrélation distribuée</strong> : Support des headers de tracing distribué</li>
 *   <li><strong>Anonymisation des données sensibles</strong> : Masque les informations confidentielles</li>
 * </ul>
 * 
 * <h3>Relations avec l'architecture :</h3>
 * <ul>
 *   <li><strong>GatewayConfig</strong> : Configuré dans la chaîne de filtres avec ordre de priorité</li>
 *   <li><strong>RequestValidationFilter</strong> : Reçoit les logs des validations effectuées</li>
 *   <li><strong>AuthenticationFilter</strong> : Enregistre les événements d'authentification</li>
 *   <li><strong>RateLimitingFilter</strong> : Trace les limitations de taux appliquées</li>
 *   <li><strong>GlobalExceptionHandler</strong> : Coordonne la journalisation des erreurs</li>
 *   <li><strong>Services backend</strong> : Transmet les IDs de corrélation aux services</li>
 *   <li><strong>Système de monitoring externe</strong> : Intégration avec ELK Stack, Splunk, etc.</li>
 * </ul>
 * 
 * <h3>Position dans la chaîne de filtres :</h3>
 * <pre>
 * 1. RequestValidationFilter - Validation des requêtes
 * 2. RateLimitingFilter - Limitation du taux de requêtes  
 * 3. AuthenticationFilter - Authentification
 * 4. LoggingFilter (ce composant) - Journalisation finale
 * 5. Service Backend
 * </pre>
 * 
 * <h3>Format des logs :</h3>
 * <p>Les logs sont structurés au format JSON pour faciliter l'indexation et la recherche :</p>
 * <pre>
 * {
 *   "timestamp": "2024-01-15T10:30:45.123Z",
 *   "requestId": "req-123e4567-e89b-12d3-a456-426614174000",
 *   "method": "POST",
 *   "uri": "/api/users",
 *   "clientIp": "192.168.1.100",
 *   "userAgent": "MyApp/1.0",
 *   "responseStatus": 201,
 *   "responseTime": 245,
 *   "requestSize": 1024,
 *   "responseSize": 512,
 *   "userId": "user123",
 *   "correlationId": "corr-456789"
 * }
 * </pre>
 * 
 * <h3>Configuration :</h3>
 * <p>Le comportement du logging peut être configuré via application.yml :</p>
 * <pre>
 * gateway:
 *   logging:
 *     enabled: true
 *     level: INFO
 *     include-headers: true
 *     include-body: false
 *     sensitive-headers:
 *       - Authorization
 *       - Cookie
 *       - X-API-Key
 *     max-body-size: 1024
 * </pre>
 * 
 * @author JavaCraft Academy
 * @version 1.0
 * @since 1.0
 * @see org.springframework.cloud.gateway.filter.GatewayFilter
 * @see com.javacraftacademy.gateway.config.GatewayConfig
 * @see org.slf4j.MDC
 */
@Component
public class LoggingFilter extends AbstractGatewayFilterFactory<LoggingFilter.Config> {

    private static final Logger logger = LoggerFactory.getLogger(LoggingFilter.class);
    private static final Logger accessLogger = LoggerFactory.getLogger("ACCESS_LOG");
    private static final Logger auditLogger = LoggerFactory.getLogger("AUDIT_LOG");
    
    // Constantes pour les headers de tracing
    private static final String REQUEST_ID_HEADER = "X-Request-ID";
    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    private static final String TRACE_ID_HEADER = "X-Trace-ID";
    private static final String SPAN_ID_HEADER = "X-Span-ID";
    
    // Headers sensibles à masquer
    private static final String[] SENSITIVE_HEADERS = {
        "Authorization", "Cookie", "X-API-Key", "X-Auth-Token", 
        "X-Access-Token", "X-Refresh-Token", "Authentication"
    };
    
    // MDC Keys pour le contexte de logging
    private static final String MDC_REQUEST_ID = "requestId";
    private static final String MDC_CORRELATION_ID = "correlationId";
    private static final String MDC_USER_ID = "userId";
    private static final String MDC_CLIENT_IP = "clientIp";
    
    // Compteurs pour les métriques
    private static final AtomicLong requestCounter = new AtomicLong(0);
    private static final AtomicLong errorCounter = new AtomicLong(0);
    
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Constructeur par défaut initialisant la configuration.
     */
    public LoggingFilter() {
        super(Config.class);
    }

    /**
     * Applique le filtre de journalisation sur la requête et la réponse.
     * 
     * <p>Cette méthode constitue le cœur du système de logging. Elle capture
     * toutes les informations pertinentes de la requête et de la réponse,
     * mesure les performances et structure les logs pour l'analyse.</p>
     * 
     * @param config Configuration du filtre de logging
     * @return GatewayFilter configuré pour la journalisation
     */
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            if (!config.isEnabled()) {
                return chain.filter(exchange);
            }
            
            ServerHttpRequest request = exchange.getRequest();
            ServerHttpResponse response = exchange.getResponse();
            Instant startTime = Instant.now();
            
            // Génération des IDs de traçabilité
            String requestId = generateOrExtractRequestId(request);
            String correlationId = generateOrExtractCorrelationId(request);
            
            // Configuration du contexte MDC
            setupMDC(request, requestId, correlationId);
            
            try {
                // Logging de la requête entrante
                logIncomingRequest(request, requestId, correlationId, config);
                
                // Ajout des headers de tracing à la requête
                ServerHttpRequest mutatedRequest = addTracingHeaders(request, requestId, correlationId);
                
                // Incrémentation du compteur de requêtes
                long requestNumber = requestCounter.incrementAndGet();
                
                // Poursuite de la chaîne de filtres avec mesure du temps
                return chain.filter(exchange.mutate().request(mutatedRequest).build())
                    .doFinally(signalType -> {
                        try {
                            // Calcul du temps de réponse
                            Duration responseTime = Duration.between(startTime, Instant.now());
                            
                            // Logging de la réponse
                            logOutgoingResponse(request, response, requestId, correlationId, 
                                responseTime, requestNumber, config);
                            
                            // Logging d'audit si nécessaire
                            if (shouldAuditLog(request, response)) {
                                logAuditEvent(request, response, requestId, correlationId, responseTime);
                            }
                            
                            // Mise à jour des métriques
                            updateMetrics(response);
                            
                        } finally {
                            // Nettoyage du contexte MDC
                            clearMDC();
                        }
                    })
                    .doOnError(error -> {
                        // Logging des erreurs
                        Duration responseTime = Duration.between(startTime, Instant.now());
                        logError(request, error, requestId, correlationId, responseTime);
                        errorCounter.incrementAndGet();
                        clearMDC();
                    });
                    
            } catch (Exception e) {
                logger.error("Erreur dans le filtre de logging pour la requête {}", requestId, e);
                clearMDC();
                return chain.filter(exchange);
            }
        };
    }

    /**
     * Génère ou extrait l'ID de requête des headers.
     * 
     * @param request La requête HTTP
     * @return L'ID de requête unique
     */
    private String generateOrExtractRequestId(ServerHttpRequest request) {
        String existingRequestId = request.getHeaders().getFirst(REQUEST_ID_HEADER);
        return StringUtils.hasText(existingRequestId) ? existingRequestId : 
            "req-" + UUID.randomUUID().toString();
    }

    /**
     * Génère ou extrait l'ID de corrélation des headers.
     * 
     * @param request La requête HTTP
     * @return L'ID de corrélation
     */
    private String generateOrExtractCorrelationId(ServerHttpRequest request) {
        String existingCorrelationId = request.getHeaders().getFirst(CORRELATION_ID_HEADER);
        return StringUtils.hasText(existingCorrelationId) ? existingCorrelationId : 
            "corr-" + UUID.randomUUID().toString();
    }

    /**
     * Configure le contexte MDC (Mapped Diagnostic Context) pour les logs.
     * 
     * @param request La requête HTTP
     * @param requestId L'ID de la requête
     * @param correlationId L'ID de corrélation
     */
    private void setupMDC(ServerHttpRequest request, String requestId, String correlationId) {
        MDC.put(MDC_REQUEST_ID, requestId);
        MDC.put(MDC_CORRELATION_ID, correlationId);
        MDC.put(MDC_CLIENT_IP, getClientIp(request));
        
        // Extraction de l'ID utilisateur si disponible
        String userId = extractUserId(request);
        if (StringUtils.hasText(userId)) {
            MDC.put(MDC_USER_ID, userId);
        }
    }

    /**
     * Nettoie le contexte MDC après traitement de la requête.
     */
    private void clearMDC() {
        MDC.remove(MDC_REQUEST_ID);
        MDC.remove(MDC_CORRELATION_ID);
        MDC.remove(MDC_CLIENT_IP);
        MDC.remove(MDC_USER_ID);
    }

    /**
     * Ajoute les headers de tracing à la requête sortante.
     * 
     * @param request La requête originale
     * @param requestId L'ID de la requête
     * @param correlationId L'ID de corrélation
     * @return La requête modifiée avec les headers de tracing
     */
    private ServerHttpRequest addTracingHeaders(ServerHttpRequest request, String requestId, String correlationId) {
        return request.mutate()
            .header(REQUEST_ID_HEADER, requestId)
            .header(CORRELATION_ID_HEADER, correlationId)
            .build();
    }

    /**
     * Journalise la requête entrante.
     * 
     * @param request La requête HTTP
     * @param requestId L'ID de la requête
     * @param correlationId L'ID de corrélation
     * @param config La configuration du filtre
     */
    private void logIncomingRequest(ServerHttpRequest request, String requestId, String correlationId, Config config) {
        try {
            Map<String, Object> logData = new HashMap<>();
            logData.put("type", "REQUEST");
            logData.put("timestamp", Instant.now().toString());
            logData.put("requestId", requestId);
            logData.put("correlationId", correlationId);
            logData.put("method", request.getMethod().toString());
            logData.put("uri", request.getURI().toString());
            logData.put("path", request.getPath().toString());
            logData.put("clientIp", getClientIp(request));
            logData.put("userAgent", request.getHeaders().getFirst(HttpHeaders.USER_AGENT));
            logData.put("contentType", request.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE));
            logData.put("contentLength", request.getHeaders().getContentLength());
            
            // Ajout des paramètres de requête
            if (!request.getQueryParams().isEmpty()) {
                logData.put("queryParams", request.getQueryParams().toSingleValueMap());
            }
            
            // Ajout des headers si configuré
            if (config.isIncludeHeaders()) {
                logData.put("headers", sanitizeHeaders(request.getHeaders()));
            }
            
            // Extraction de l'ID utilisateur
            String userId = extractUserId(request);
            if (StringUtils.hasText(userId)) {
                logData.put("userId", userId);
            }
            
            String jsonLog = objectMapper.writeValueAsString(logData);
            accessLogger.info(jsonLog);
            
        } catch (Exception e) {
            logger.warn("Erreur lors de la journalisation de la requête entrante {}", requestId, e);
        }
    }

    /**
     * Journalise la réponse sortante.
     * 
     * @param request La requête HTTP originale
     * @param response La réponse HTTP
     * @param requestId L'ID de la requête
     * @param correlationId L'ID de corrélation
     * @param responseTime Le temps de réponse
     * @param requestNumber Le numéro de la requête
     * @param config La configuration du filtre
     */
    private void logOutgoingResponse(ServerHttpRequest request, ServerHttpResponse response, 
            String requestId, String correlationId, Duration responseTime, long requestNumber, Config config) {
        try {
            Map<String, Object> logData = new HashMap<>();
            logData.put("type", "RESPONSE");
            logData.put("timestamp", Instant.now().toString());
            logData.put("requestId", requestId);
            logData.put("correlationId", correlationId);
            logData.put("requestNumber", requestNumber);
            logData.put("method", request.getMethod().toString());
            logData.put("uri", request.getURI().toString());
            logData.put("statusCode", response.getStatusCode() != null ? response.getStatusCode().value() : null);
            logData.put("responseTimeMs", responseTime.toMillis());
            logData.put("clientIp", getClientIp(request));
            
            // Ajout des headers de réponse si configuré
            if (config.isIncludeHeaders()) {
                logData.put("responseHeaders", sanitizeHeaders(response.getHeaders()));
            }
            
            String jsonLog = objectMapper.writeValueAsString(logData);
            accessLogger.info(jsonLog);
            
        } catch (Exception e) {
            logger.warn("Erreur lors de la journalisation de la réponse {}", requestId, e);
        }
    }

    /**
     * Journalise un événement d'audit.
     * 
     * @param request La requête HTTP
     * @param response La réponse HTTP
     * @param requestId L'ID de la requête
     * @param correlationId L'ID de corrélation
     * @param responseTime Le temps de réponse
     */
    private void logAuditEvent(ServerHttpRequest request, ServerHttpResponse response, 
            String requestId, String correlationId, Duration responseTime) {
        try {
            Map<String, Object> auditData = new HashMap<>();
            auditData.put("type", "AUDIT");
            auditData.put("timestamp", Instant.now().toString());
            auditData.put("requestId", requestId);
            auditData.put("correlationId", correlationId);
            auditData.put("method", request.getMethod().toString());
            auditData.put("uri", request.getURI().toString());
            auditData.put("clientIp", getClientIp(request));
            auditData.put("statusCode", response.getStatusCode() != null ? response.getStatusCode().value() : null);
            auditData.put("responseTimeMs", responseTime.toMillis());
            
            String userId = extractUserId(request);
            if (StringUtils.hasText(userId)) {
                auditData.put("userId", userId);
            }
            
            String jsonLog = objectMapper.writeValueAsString(auditData);
            auditLogger.info(jsonLog);
            
        } catch (Exception e) {
            logger.warn("Erreur lors de la journalisation d'audit {}", requestId, e);
        }
    }

    /**
     * Journalise une erreur.
     * 
     * @param request La requête HTTP
     * @param error L'erreur survenue
     * @param requestId L'ID de la requête
     * @param correlationId L'ID de corrélation
     * @param responseTime Le temps de traitement
     */
    private void logError(ServerHttpRequest request, Throwable error, 
            String requestId, String correlationId, Duration responseTime) {
        try {
            Map<String, Object> errorData = new HashMap<>();
            errorData.put("type", "ERROR");
            errorData.put("timestamp", Instant.now().toString());
            errorData.put("requestId", requestId);
            errorData.put("correlationId", correlationId);
            errorData.put("method", request.getMethod().toString());
            errorData.put("uri", request.getURI().toString());
            errorData.put("clientIp", getClientIp(request));
            errorData.put("errorClass", error.getClass().getSimpleName());
            errorData.put("errorMessage", error.getMessage());
            errorData.put("responseTimeMs", responseTime.toMillis());
            
            String jsonLog = objectMapper.writeValueAsString(errorData);
            logger.error(jsonLog, error);
            
        } catch (Exception e) {
            logger.error("Erreur lors de la journalisation d'erreur pour la requête {}", requestId, e);
        }
    }

    /**
     * Détermine si un événement doit être audité.
     * 
     * @param request La requête HTTP
     * @param response La réponse HTTP
     * @return true si l'événement doit être audité
     */
    private boolean shouldAuditLog(ServerHttpRequest request, ServerHttpResponse response) {
        // Auditer les opérations de modification (POST, PUT, DELETE)
        String method = request.getMethod().toString();
        if ("POST".equals(method) || "PUT".equals(method) || "DELETE".equals(method)) {
            return true;
        }
        
        // Auditer les erreurs client et serveur
        if (response.getStatusCode() != null) {
            int statusCode = response.getStatusCode().value();
            return statusCode >= 400;
        }
        
        return false;
    }

    /**
     * Met à jour les métriques basées sur la réponse.
     * 
     * @param response La réponse HTTP
     */
    private void updateMetrics(ServerHttpResponse response) {
        // Cette méthode peut être étendue pour intégrer avec des systèmes de métriques
        // comme Micrometer, Prometheus, etc.
        if (response.getStatusCode() != null && response.getStatusCode().is4xxClientError()) {
            // Incrémenter compteur d'erreurs client
        } else if (response.getStatusCode() != null && response.getStatusCode().is5xxServerError()) {
            // Incrémenter compteur d'erreurs serveur
        }
    }

    /**
     * Extrait l'adresse IP du client.
     * 
     * @param request La requête HTTP
     * @return L'adresse IP du client
     */
    private String getClientIp(ServerHttpRequest request) {
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (StringUtils.hasText(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeaders().getFirst("X-Real-IP");
        if (StringUtils.hasText(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddress() != null ? 
            request.getRemoteAddress().getAddress().getHostAddress() : "unknown";
    }

    /**
     * Extrait l'ID utilisateur de la requête.
     * 
     * @param request La requête HTTP
     * @return L'ID utilisateur ou null si non disponible
     */
    private String extractUserId(Request request) {
        // Tentative d'extraction depuis un header personnalisé
        String userId = request.getHeaders().getFirst("X-User-ID");
        if (StringUtils.hasText(userId)) {
            return userId;
        }
        
        // Tentative d'extraction depuis le token JWT (si présent)
        String authorization = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(authorization) && authorization.startsWith("Bearer ")) {
            // Ici, vous pourriez décoder le JWT pour extraire l'ID utilisateur
            // Cette implémentation dépend de votre système d'authentification
            return extractUserIdFromJWT(authorization.substring(7));
        }
        
        return null;
    }

    /**
     * Extrait l'ID utilisateur d'un token JWT.
     * 
     * @param jwtToken Le token JWT
     * @return L'ID utilisateur ou null
     */
    private String extractUserIdFromJWT(String jwtToken) {
        // Implémentation simplifiée - à adapter selon votre bibliothèque JWT
        try {
            // Exemple avec une bibliothèque JWT comme jjwt
            // Claims claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(jwtToken).getBody();
            // return claims.getSubject();
            return null; // Placeholder
        } catch (Exception e) {
            logger.debug("Impossible d'extraire l'ID utilisateur du JWT", e);
            return null;
        }
    }

    /**
     * Sanitise les headers en masquant les valeurs sensibles.
     * 
     * @param headers Les headers HTTP
     * @return Les headers sanitisés
     */
    private Map<String, String> sanitizeHeaders(HttpHeaders headers) {
        Map<String, String> sanitizedHeaders = new HashMap<>();
        
        headers.forEach((name, values) -> {
            if (isSensitiveHeader(name)) {
                sanitizedHeaders.put(name, "***MASKED***");
            } else {
                sanitizedHeaders.put(name, String.join(", ", values));
            }
        });
        
        return sanitizedHeaders;
    }

    /**
     * Vérifie si un header est sensible et doit être masqué.
     * 
     * @param headerName Le nom du header
     * @return true si le header est sensible
     */
    private boolean isSensitiveHeader(String headerName) {
        for (String sensitiveHeader : SENSITIVE_HEADERS) {
            if (sensitiveHeader.equalsIgnoreCase(headerName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Classe de configuration pour le filtre de logging.
     */
    public static class Config {
        private boolean enabled = true;
        private String level = "INFO";
        private boolean includeHeaders = true;
        private boolean includeBody = false;
        private int maxBodySize = 1024;
        private String[] sensitiveHeaders = SENSITIVE_HEADERS;

        /**
         * @return true si le logging est activé
         */
        public boolean isEnabled() {
            return enabled;
        }

        /**
         * Active ou désactive le logging.
         * 
         * @param enabled true pour activer
         */
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        /**
         * @return le niveau de log
         */
        public String getLevel() {
            return level;
        }

        /**
         * Définit le niveau de log.
         * 
         * @param level le niveau de log (DEBUG, INFO, WARN, ERROR)
         */
        public void setLevel(String level) {
            this.level = level;
        }

        /**
         * @return true si les headers doivent être inclus
         */
        public boolean isIncludeHeaders() {
            return includeHeaders;
        }

        /**
         * Définit si les headers doivent être inclus dans les logs.
         * 
         * @param includeHeaders true pour inclure les headers
         */
        public void setIncludeHeaders(boolean includeHeaders) {
            this.includeHeaders = includeHeaders;
        }

        /**
         * @return true si le body doit être inclus
         */
        public boolean isIncludeBody() {
            return includeBody;
        }

        /**
         * Définit si le body doit être inclus dans les logs.
         * 
         * @param includeBody true pour inclure le body
         */
        public void setIncludeBody(boolean includeBody) {
            this.includeBody = includeBody;
        }

        /**
         * @return la taille maximale du body à logger
         */
        public int getMaxBodySize() {
            return maxBodySize;
        }

        /**
         * Définit la taille maximale du body à inclure dans les logs.
         * 
         * @param maxBodySize la taille maximale en octets
         */
        public void setMaxBodySize(int maxBodySize) {
            this.maxBodySize = maxBodySize;
        }

        /**
         * @return la liste des headers sensibles
         */
        public String[] getSensitiveHeaders() {
            return sensitiveHeaders;
        }

        /**
         * Définit la liste des headers à masquer.
         * 
         * @param sensitiveHeaders les headers sensibles
         */
        public void setSensitiveHeaders(String[] sensitiveHeaders) {
            this.sensitiveHeaders = sensitiveHeaders;
        }
    }
}