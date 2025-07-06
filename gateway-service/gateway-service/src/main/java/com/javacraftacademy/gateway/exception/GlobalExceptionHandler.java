package com.javacraftacademy.gateway.exception;

import com.javacraftacademy.gateway.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Gestionnaire global des exceptions pour le service Gateway.
 * Cette classe intercepte toutes les exceptions non gérées dans l'application
 * et fournit des réponses HTTP standardisées et sécurisées.
 * 
 * <h2>Fonctionnalités principales :</h2>
 * <ul>
 *   <li>Gestion centralisée de toutes les exceptions de l'application</li>
 *   <li>Standardisation des réponses d'erreur JSON</li>
 *   <li>Logging sécurisé des erreurs avec corrélation</li>
 *   <li>Masquage des informations sensibles en production</li>
 *   <li>Support des erreurs de validation avec détails</li>
 *   <li>Gestion spécialisée des erreurs d'authentification</li>
 * </ul>
 * 
 * <h2>Relations avec l'application :</h2>
 * <ul>
 *   <li><strong>Tous les Controllers</strong> : Intercepte leurs exceptions</li>
 *   <li><strong>Tous les Filters</strong> : Traite les exceptions des filtres</li>
 *   <li><strong>Tous les Services</strong> : Gère les exceptions métier</li>
 *   <li><strong>Spring Security</strong> : Intégration avec les erreurs de sécurité</li>
 *   <li><strong>ResponseUtil</strong> : Utilise cette classe pour formater les réponses</li>
 *   <li><strong>Logging Framework</strong> : Intégration avec SLF4J/Logback</li>
 * </ul>
 * 
 * <h2>Types d'exceptions gérées :</h2>
 * <ul>
 *   <li>GatewayException et ses sous-classes</li>
 *   <li>AuthenticationException (erreurs de sécurité)</li>
 *   <li>Erreurs de validation (Bean Validation)</li>
 *   <li>Erreurs de parsing JSON</li>
 *   <li>Erreurs de routage (404)</li>
 *   <li>Erreurs génériques (500)</li>
 * </ul>
 * 
 * <h2>Format de réponse standardisé :</h2>
 * <pre>
 * {
 *   "timestamp": "2024-01-15T10:30:00",
 *   "status": 400,
 *   "error": "Bad Request",
 *   "message": "Description de l'erreur",
 *   "path": "/api/endpoint",
 *   "errorCode": "VALIDATION_ERROR",
 *   "traceId": "uuid-correlation-id",
 *   "details": { ... }
 * }
 * </pre>
 * 
 * @author JavaCraft Academy
 * @version 1.0
 * @since 1.0
 * 
 * @see GatewayException
 * @see AuthenticationException
 * @see ResponseUtil
 * @see org.springframework.web.bind.annotation.RestControllerAdvice
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final Logger securityLogger = LoggerFactory.getLogger("SECURITY");

    /**
     * Indique si l'application est en mode développement.
     * En mode développement, plus de détails sont exposés dans les réponses d'erreur.
     */
    @Value("${app.debug:false}")
    private boolean debugMode;

    /**
     * Nom de l'application pour identifier la source des erreurs.
     */
    @Value("${spring.application.name:gateway-service}")
    private String applicationName;

    /**
     * Formateur pour les timestamps dans les réponses d'erreur.
     */
    private final DateTimeFormatter timestampFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    /**
     * Gère les exceptions spécifiques au Gateway.
     * 
     * @param ex L'exception GatewayException
     * @param request La requête HTTP
     * @return Une réponse HTTP avec les détails de l'erreur
     */
    @ExceptionHandler(GatewayException.class)
    public ResponseEntity<Map<String, Object>> handleGatewayException(
            GatewayException ex, HttpServletRequest request) {
        
        String traceId = generateTraceId();
        
        // Logging de l'erreur avec corrélation
        logger.error("GatewayException [{}]: {} - Path: {}", 
                    traceId, ex.getMessage(), request.getRequestURI(), ex);
        
        Map<String, Object> errorResponse = buildErrorResponse(
            ex.getHttpStatus(),
            HttpStatus.valueOf(ex.getHttpStatus()).getReasonPhrase(),
            ex.getMessage(),
            request.getRequestURI(),
            ex.getErrorCode(),
            traceId,
            null
        );
        
        return ResponseEntity.status(ex.getHttpStatus()).body(errorResponse);
    }

    /**
     * Gère les exceptions d'authentification et d'autorisation.
     * Effectue un logging de sécurité spécialisé.
     * 
     * @param ex L'exception AuthenticationException
     * @param request La requête HTTP
     * @return Une réponse HTTP avec les détails de l'erreur de sécurité
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAuthenticationException(
            AuthenticationException ex, HttpServletRequest request) {
        
        String traceId = generateTraceId();
        
        // Logging de sécurité spécialisé
        securityLogger.warn("Authentication failed [{}]: {} - Path: {} - IP: {} - User-Agent: {}", 
                           traceId, ex.getMessage(), request.getRequestURI(), 
                           getClientIpAddress(request), request.getHeader("User-Agent"));
        
        Map<String, Object> errorResponse = buildErrorResponse(
            HttpStatus.UNAUTHORIZED.value(),
            "Unauthorized",
            debugMode ? ex.getMessage() : "Authentication failed",
            request.getRequestURI(),
            "AUTHENTICATION_ERROR",
            traceId,
            null
        );
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    /**
     * Gère les erreurs de validation des arguments de méthode.
     * 
     * @param ex L'exception MethodArgumentNotValidException
     * @param request La requête HTTP
     * @return Une réponse HTTP avec les détails des erreurs de validation
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        
        String traceId = generateTraceId();
        
        logger.warn("Validation error [{}]: {} validation errors - Path: {}", 
                   traceId, ex.getBindingResult().getErrorCount(), request.getRequestURI());
        
        Map<String, String> validationErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            validationErrors.put(fieldName, errorMessage);
        });
        
        Map<String, Object> errorResponse = buildErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Bad Request",
            "Validation failed",
            request.getRequestURI(),
            "VALIDATION_ERROR",
            traceId,
            validationErrors
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Gère les erreurs de violation de contraintes de validation.
     * 
     * @param ex L'exception ConstraintViolationException
     * @param request La requête HTTP
     * @return Une réponse HTTP avec les détails des violations de contraintes
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolationException(
            ConstraintViolationException ex, HttpServletRequest request) {
        
        String traceId = generateTraceId();
        
        logger.warn("Constraint violation [{}]: {} violations - Path: {}", 
                   traceId, ex.getConstraintViolations().size(), request.getRequestURI());
        
        Map<String, String> violationErrors = new HashMap<>();
        Set<ConstraintViolation<?>> violations = ex.getConstraintViolations();
        for (ConstraintViolation<?> violation : violations) {
            String propertyPath = violation.getPropertyPath().toString();
            String message = violation.getMessage();
            violationErrors.put(propertyPath, message);
        }
        
        Map<String, Object> errorResponse = buildErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Bad Request",
            "Constraint validation failed",
            request.getRequestURI(),
            "CONSTRAINT_VIOLATION",
            traceId,
            violationErrors
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Gère les erreurs de parsing des messages HTTP (JSON malformé).
     * 
     * @param ex L'exception HttpMessageNotReadableException
     * @param request La requête HTTP
     * @return Une réponse HTTP avec les détails de l'erreur de parsing
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        
        String traceId = generateTraceId();
        
        logger.warn("JSON parsing error [{}]: {} - Path: {}", 
                   traceId, ex.getMessage(), request.getRequestURI());
        
        Map<String, Object> errorResponse = buildErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Bad Request",
            debugMode ? ex.getMessage() : "Invalid JSON format",
            request.getRequestURI(),
            "JSON_PARSE_ERROR",
            traceId,
            null
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Gère les erreurs de type d'argument de méthode.
     * 
     * @param ex L'exception MethodArgumentTypeMismatchException
     * @param request La requête HTTP
     * @return Une réponse HTTP avec les détails de l'erreur de type
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        
        String traceId = generateTraceId();
        
        logger.warn("Type mismatch error [{}]: Parameter '{}' - Path: {}", 
                   traceId, ex.getName(), request.getRequestURI());
        
        String message = String.format("Invalid value for parameter '%s'. Expected type: %s", 
                                     ex.getName(), ex.getRequiredType().getSimpleName());
        
        Map<String, Object> errorResponse = buildErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Bad Request",
            message,
            request.getRequestURI(),
            "TYPE_MISMATCH_ERROR",
            traceId,
            null
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Gère les erreurs de statut de réponse générique.
     * 
     * @param ex L'exception ResponseStatusException
     * @param request La requête HTTP
     * @return Une réponse HTTP avec les détails de l'erreur de statut
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatusException(
            ResponseStatusException ex, HttpServletRequest request) {
        
        String traceId = generateTraceId();
        
        logger.warn("Response status error [{}]: {} - Status: {} - Path: {}", 
                   traceId, ex.getReason(), ex.getStatus(), request.getRequestURI());
        
        Map<String, Object> errorResponse = buildErrorResponse(
            ex.getStatus().value(),
            ex.getStatus().getReasonPhrase(),
            ex.getReason() != null ? ex.getReason() : ex.getStatus().getReasonPhrase(),
            request.getRequestURI(),
            "RESPONSE_STATUS_ERROR",
            traceId,
            null
        );
        
        return ResponseEntity.status(ex.getStatus()).body(errorResponse);
    }

    /**
     * Gère les erreurs 404 (handler non trouvé).
     * 
     * @param ex L'exception NoHandlerFoundException
     * @param request La requête HTTP
     * @return Une réponse HTTP avec les détails de l'erreur 404
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNoHandlerFoundException(
            NoHandlerFoundException ex, HttpServletRequest request) {
        
        String traceId = generateTraceId();
        
        logger.warn("No handler found [{}]: {} {} - Path: {}", 
                   traceId, ex.getHttpMethod(), ex.getRequestURL(), request.getRequestURI());
        
        Map<String, Object> errorResponse = buildErrorResponse(
            HttpStatus.NOT_FOUND.value(),
            "Not Found",
            String.format("No handler found for %s %s", ex.getHttpMethod(), ex.getRequestURL()),
            request.getRequestURI(),
            "NOT_FOUND_ERROR",
            traceId,
            null
        );
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * Gère toutes les autres exceptions non spécifiquement traitées.
     * 
     * @param ex L'exception générique
     * @param request La requête HTTP
     * @return Une réponse HTTP avec les détails de l'erreur générique
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(
            Exception ex, HttpServletRequest request) {
        
        String traceId = generateTraceId();
        
        logger.error("Unexpected error [{}]: {} - Path: {}", 
                    traceId, ex.getMessage(), request.getRequestURI(), ex);
        
        Map<String, Object> errorResponse = buildErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Internal Server Error",
            debugMode ? ex.getMessage() : "An unexpected error occurred",
            request.getRequestURI(),
            "INTERNAL_SERVER_ERROR",
            traceId,
            debugMode ? Map.of("exception", ex.getClass().getSimpleName()) : null
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * Construit une réponse d'erreur standardisée.
     * 
     * @param status Le code de statut HTTP
     * @param error Le message d'erreur du statut
     * @param message Le message détaillé de l'erreur
     * @param path Le chemin de la requête
     * @param errorCode Le code d'erreur spécifique
     * @param traceId L'identifiant de corrélation
     * @param details Les détails supplémentaires (optionnel)
     * @return Une map représentant la réponse d'erreur
     */
    private Map<String, Object> buildErrorResponse(int status, String error, String message, 
                                                 String path, String errorCode, String traceId, 
                                                 Map<String, ?> details) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now().format(timestampFormatter));
        errorResponse.put("status", status);
        errorResponse.put("error", error);
        errorResponse.put("message", message);
        errorResponse.put("path", path);
        errorResponse.put("errorCode", errorCode);
        errorResponse.put("traceId", traceId);
        errorResponse.put("service", applicationName);
        
        if (details != null && !details.isEmpty()) {
            errorResponse.put("details", details);
        }
        
        return errorResponse;
    }

    /**
     * Génère un identifiant de trace unique pour la corrélation des logs.
     * 
     * @return Un UUID sous forme de chaîne
     */
    private String generateTraceId() {
        return UUID.randomUUID().toString();
    }

    /**
     * Extrait l'adresse IP réelle du client en tenant compte des proxies.
     * 
     * @param request La requête HTTP
     * @return L'adresse IP du client
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}