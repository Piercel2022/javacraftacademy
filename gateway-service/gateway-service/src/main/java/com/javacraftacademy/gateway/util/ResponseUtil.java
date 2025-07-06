package com.javacraftacademy.gateway.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Utilitaire pour la gestion des réponses HTTP
 * 
 * Cette classe fournit des méthodes utilitaires pour créer et formater
 * des réponses HTTP standardisées pour le gateway JavaCraftAcademy.
 * 
 * @author JavaCraftAcademy Team
 * @version 1.0
 */
@Component
public class ResponseUtil {

    private final ObjectMapper objectMapper;

    public ResponseUtil(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Crée une réponse d'erreur standardisée
     * 
     * @param response la réponse HTTP
     * @param status le statut HTTP
     * @param message le message d'erreur
     * @param path le chemin de la requête
     * @return Mono<Void> pour la réponse
     */
    public Mono<Void> createErrorResponse(ServerHttpResponse response, HttpStatus status, String message, String path) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        errorResponse.put("status", status.value());
        errorResponse.put("error", status.getReasonPhrase());
        errorResponse.put("message", message);
        errorResponse.put("path", path);
        errorResponse.put("service", "gateway-service");

        return writeJsonResponse(response, status, errorResponse);
    }

    /**
     * Crée une réponse d'erreur d'authentification
     * 
     * @param response la réponse HTTP
     * @param message le message d'erreur
     * @param path le chemin de la requête
     * @return Mono<Void> pour la réponse
     */
    public Mono<Void> createAuthenticationErrorResponse(ServerHttpResponse response, String message, String path) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        errorResponse.put("status", HttpStatus.UNAUTHORIZED.value());
        errorResponse.put("error", "Unauthorized");
        errorResponse.put("message", message);
        errorResponse.put("path", path);
        errorResponse.put("service", "gateway-service");
        errorResponse.put("code", "AUTH_FAILED");

        return writeJsonResponse(response, HttpStatus.UNAUTHORIZED, errorResponse);
    }

    /**
     * Crée une réponse d'erreur d'autorisation
     * 
     * @param response la réponse HTTP
     * @param message le message d'erreur
     * @param path le chemin de la requête
     * @return Mono<Void> pour la réponse
     */
    public Mono<Void> createAuthorizationErrorResponse(ServerHttpResponse response, String message, String path) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        errorResponse.put("status", HttpStatus.FORBIDDEN.value());
        errorResponse.put("error", "Forbidden");
        errorResponse.put("message", message);
        errorResponse.put("path", path);
        errorResponse.put("service", "gateway-service");
        errorResponse.put("code", "ACCESS_DENIED");

        return writeJsonResponse(response, HttpStatus.FORBIDDEN, errorResponse);
    }

    /**
     * Crée une réponse d'erreur de limitation de débit
     * 
     * @param response la réponse HTTP
     * @param message le message d'erreur
     * @param path le chemin de la requête
     * @param retryAfter temps d'attente avant nouvelle tentative
     * @return Mono<Void> pour la réponse
     */
    public Mono<Void> createRateLimitErrorResponse(ServerHttpResponse response, String message, String path, long retryAfter) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        errorResponse.put("status", HttpStatus.TOO_MANY_REQUESTS.value());
        errorResponse.put("error", "Too Many Requests");
        errorResponse.put("message", message);
        errorResponse.put("path", path);
        errorResponse.put("service", "gateway-service");
        errorResponse.put("code", "RATE_LIMIT_EXCEEDED");
        errorResponse.put("retryAfter", retryAfter);

        // Ajouter l'en-tête Retry-After
        response.getHeaders().add("Retry-After", String.valueOf(retryAfter));

        return writeJsonResponse(response, HttpStatus.TOO_MANY_REQUESTS, errorResponse);
    }

    /**
     * Crée une réponse d'erreur de validation
     * 
     * @param response la réponse HTTP
     * @param message le message d'erreur
     * @param path le chemin de la requête
     * @param validationErrors les erreurs de validation détaillées
     * @return Mono<Void> pour la réponse
     */
    public Mono<Void> createValidationErrorResponse(ServerHttpResponse response, String message, String path, Map<String, String> validationErrors) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
        errorResponse.put("error", "Bad Request");
        errorResponse.put("message", message);
        errorResponse.put("path", path);
        errorResponse.put("service", "gateway-service");
        errorResponse.put("code", "VALIDATION_FAILED");
        errorResponse.put("validationErrors", validationErrors);

        return writeJsonResponse(response, HttpStatus.BAD_REQUEST, errorResponse);
    }

    /**
     * Crée une réponse de succès standardisée
     * 
     * @param response la réponse HTTP
     * @param data les données à retourner
     * @param message le message de succès
     * @return Mono<Void> pour la réponse
     */
    public Mono<Void> createSuccessResponse(ServerHttpResponse response, Object data, String message) {
        Map<String, Object> successResponse = new HashMap<>();
        successResponse.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        successResponse.put("status", HttpStatus.OK.value());
        successResponse.put("message", message);
        successResponse.put("data", data);
        successResponse.put("service", "gateway-service");

        return writeJsonResponse(response, HttpStatus.OK, successResponse);
    }

    /**
     * Crée une réponse de succès pour la création d'une ressource
     * 
     * @param response la réponse HTTP
     * @param data les données créées
     * @param message le message de succès
     * @return Mono<Void> pour la réponse
     */
    public Mono<Void> createCreatedResponse(ServerHttpResponse response, Object data, String message) {
        Map<String, Object> successResponse = new HashMap<>();
        successResponse.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        successResponse.put("status", HttpStatus.CREATED.value());
        successResponse.put("message", message);
        successResponse.put("data", data);
        successResponse.put("service", "gateway-service");

        return writeJsonResponse(response, HttpStatus.CREATED, successResponse);
    }

    /**
     * Crée une réponse d'erreur interne du serveur
     * 
     * @param response la réponse HTTP
     * @param message le message d'erreur
     * @param path le chemin de la requête
     * @return Mono<Void> pour la réponse
     */
    public Mono<Void> createInternalServerErrorResponse(ServerHttpResponse response, String message, String path) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        errorResponse.put("error", "Internal Server Error");
        errorResponse.put("message", message);
        errorResponse.put("path", path);
        errorResponse.put("service", "gateway-service");
        errorResponse.put("code", "INTERNAL_ERROR");

        return writeJsonResponse(response, HttpStatus.INTERNAL_SERVER_ERROR, errorResponse);
    }

    /**
     * Crée une réponse d'erreur de service indisponible
     * 
     * @param response la réponse HTTP
     * @param serviceName le nom du service indisponible
     * @param path le chemin de la requête
     * @return Mono<Void> pour la réponse
     */
    public Mono<Void> createServiceUnavailableResponse(ServerHttpResponse response, String serviceName, String path) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        errorResponse.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        errorResponse.put("error", "Service Unavailable");
        errorResponse.put("message", "Le service " + serviceName + " est temporairement indisponible");
        errorResponse.put("path", path);
        errorResponse.put("service", "gateway-service");
        errorResponse.put("code", "SERVICE_UNAVAILABLE");
        errorResponse.put("unavailableService", serviceName);

        return writeJsonResponse(response, HttpStatus.SERVICE_UNAVAILABLE, errorResponse);
    }

    /**
     * Crée une réponse d'erreur de timeout
     * 
     * @param response la réponse HTTP
     * @param serviceName le nom du service qui a timeout
     * @param path le chemin de la requête
     * @param timeoutDuration la durée du timeout en millisecondes
     * @return Mono<Void> pour la réponse
     */
    public Mono<Void> createTimeoutErrorResponse(ServerHttpResponse response, String serviceName, String path, long timeoutDuration) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        errorResponse.put("status", HttpStatus.GATEWAY_TIMEOUT.value());
        errorResponse.put("error", "Gateway Timeout");
        errorResponse.put("message", "Le service " + serviceName + " n'a pas répondu dans les délais impartis");
        errorResponse.put("path", path);
        errorResponse.put("service", "gateway-service");
        errorResponse.put("code", "TIMEOUT_ERROR");
        errorResponse.put("timeoutService", serviceName);
        errorResponse.put("timeoutDuration", timeoutDuration);

        return writeJsonResponse(response, HttpStatus.GATEWAY_TIMEOUT, errorResponse);
    }

    /**
     * Crée une réponse d'erreur de ressource non trouvée
     * 
     * @param response la réponse HTTP
     * @param resource la ressource non trouvée
     * @param path le chemin de la requête
     * @return Mono<Void> pour la réponse
     */
    public Mono<Void> createNotFoundResponse(ServerHttpResponse response, String resource, String path) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        errorResponse.put("status", HttpStatus.NOT_FOUND.value());
        errorResponse.put("error", "Not Found");
        errorResponse.put("message", "La ressource " + resource + " n'a pas été trouvée");
        errorResponse.put("path", path);
        errorResponse.put("service", "gateway-service");
        errorResponse.put("code", "RESOURCE_NOT_FOUND");
        errorResponse.put("resource", resource);

        return writeJsonResponse(response, HttpStatus.NOT_FOUND, errorResponse);
    }

    /**
     * Crée une réponse d'erreur de conflit
     * 
     * @param response la réponse HTTP
     * @param message le message d'erreur
     * @param path le chemin de la requête
     * @return Mono<Void> pour la réponse
     */
    public Mono<Void> createConflictResponse(ServerHttpResponse response, String message, String path) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        errorResponse.put("status", HttpStatus.CONFLICT.value());
        errorResponse.put("error", "Conflict");
        errorResponse.put("message", message);
        errorResponse.put("path", path);
        errorResponse.put("service", "gateway-service");
        errorResponse.put("code", "RESOURCE_CONFLICT");

        return writeJsonResponse(response, HttpStatus.CONFLICT, errorResponse);
    }

    /**
     * Crée une réponse de circuit breaker
     * 
     * @param response la réponse HTTP
     * @param serviceName le nom du service
     * @param path le chemin de la requête
     * @return Mono<Void> pour la réponse
     */
    public Mono<Void> createCircuitBreakerResponse(ServerHttpResponse response, String serviceName, String path) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        errorResponse.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        errorResponse.put("error", "Circuit Breaker Open");
        errorResponse.put("message", "Le service " + serviceName + " est temporairement indisponible (circuit breaker ouvert)");
        errorResponse.put("path", path);
        errorResponse.put("service", "gateway-service");
        errorResponse.put("code", "CIRCUIT_BREAKER_OPEN");
        errorResponse.put("affectedService", serviceName);

        return writeJsonResponse(response, HttpStatus.SERVICE_UNAVAILABLE, errorResponse);
    }

    /**
     * Écrit une réponse JSON dans la réponse HTTP
     * 
     * @param response la réponse HTTP
     * @param status le statut HTTP
     * @param data les données à sérialiser en JSON
     * @return Mono<Void> pour la réponse
     */
    private Mono<Void> writeJsonResponse(ServerHttpResponse response, HttpStatus status, Object data) {
        response.setStatusCode(status);
        response.getHeaders().add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        response.getHeaders().add("X-Gateway-Response", "true");

        try {
            String jsonResponse = objectMapper.writeValueAsString(data);
            DataBufferFactory bufferFactory = response.bufferFactory();
            DataBuffer buffer = bufferFactory.wrap(jsonResponse.getBytes(StandardCharsets.UTF_8));
            return response.writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            // En cas d'erreur de sérialisation, retourner une réponse d'erreur simple
            String errorMessage = "{\"error\":\"Erreur de sérialisation JSON\",\"timestamp\":\"" + 
                                 LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "\"}";
            DataBufferFactory bufferFactory = response.bufferFactory();
            DataBuffer buffer = bufferFactory.wrap(errorMessage.getBytes(StandardCharsets.UTF_8));
            return response.writeWith(Mono.just(buffer));
        }
    }

    /**
     * Ajoute les en-têtes de sécurité standard
     * 
     * @param response la réponse HTTP
     */
    public void addSecurityHeaders(ServerHttpResponse response) {
        response.getHeaders().add("X-Content-Type-Options", "nosniff");
        response.getHeaders().add("X-Frame-Options", "DENY");
        response.getHeaders().add("X-XSS-Protection", "1; mode=block");
        response.getHeaders().add("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
        response.getHeaders().add("Referrer-Policy", "strict-origin-when-cross-origin");
    }

    /**
     * Ajoute les en-têtes CORS
     * 
     * @param response la réponse HTTP
     * @param origin l'origine de la requête
     */
    public void addCorsHeaders(ServerHttpResponse response, String origin) {
        response.getHeaders().add("Access-Control-Allow-Origin", origin);
        response.getHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.getHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With");
        response.getHeaders().add("Access-Control-Allow-Credentials", "true");
        response.getHeaders().add("Access-Control-Max-Age", "3600");
    }

    /**
     * Crée une réponse vide avec un statut donné
     * 
     * @param response la réponse HTTP
     * @param status le statut HTTP
     * @return Mono<Void> pour la réponse
     */
    public Mono<Void> createEmptyResponse(ServerHttpResponse response, HttpStatus status) {
        response.setStatusCode(status);
        response.getHeaders().add("X-Gateway-Response", "true");
        return response.setComplete();
    }

    /**
     * Crée une réponse de pagination
     * 
     * @param response la réponse HTTP
     * @param data les données paginées
     * @param page le numéro de page actuel
     * @param size la taille de la page
     * @param totalElements le nombre total d'éléments
     * @param totalPages le nombre total de pages
     * @return Mono<Void> pour la réponse
     */
    public Mono<Void> createPaginatedResponse(ServerHttpResponse response, Object data, int page, int size, 
                                            long totalElements, int totalPages) {
        Map<String, Object> paginatedResponse = new HashMap<>();
        paginatedResponse.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        paginatedResponse.put("status", HttpStatus.OK.value());
        paginatedResponse.put("data", data);
        paginatedResponse.put("service", "gateway-service");
        
        Map<String, Object> pagination = new HashMap<>();
        pagination.put("page", page);
        pagination.put("size", size);
        pagination.put("totalElements", totalElements);
        pagination.put("totalPages", totalPages);
        pagination.put("first", page == 0);
        pagination.put("last", page == totalPages - 1);
        pagination.put("hasNext", page < totalPages - 1);
        pagination.put("hasPrevious", page > 0);
        
        paginatedResponse.put("pagination", pagination);

        return writeJsonResponse(response, HttpStatus.OK, paginatedResponse);
    }

    /**
     * Crée une réponse avec métadonnées personnalisées
     * 
     * @param response la réponse HTTP
     * @param data les données
     * @param metadata les métadonnées personnalisées
     * @param status le statut HTTP
     * @return Mono<Void> pour la réponse
     */
    public Mono<Void> createResponseWithMetadata(ServerHttpResponse response, Object data, 
                                                Map<String, Object> metadata, HttpStatus status) {
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        responseBody.put("status", status.value());
        responseBody.put("data", data);
        responseBody.put("service", "gateway-service");
        responseBody.put("metadata", metadata);

        return writeJsonResponse(response, status, responseBody);
    }

    /**
     * Ajoute les en-têtes de cache
     * 
     * @param response la réponse HTTP
     * @param maxAge l'âge maximum en secondes
     * @param isPrivate si le cache est privé
     */
    public void addCacheHeaders(ServerHttpResponse response, long maxAge, boolean isPrivate) {
        String cacheControl = (isPrivate ? "private" : "public") + ", max-age=" + maxAge;
        response.getHeaders().add("Cache-Control", cacheControl);
        response.getHeaders().add("Vary", "Accept-Encoding, Authorization");
    }

    /**
     * Ajoute les en-têtes de no-cache
     * 
     * @param response la réponse HTTP
     */
    public void addNoCacheHeaders(ServerHttpResponse response) {
        response.getHeaders().add("Cache-Control", "no-cache, no-store, must-revalidate");
        response.getHeaders().add("Pragma", "no-cache");
        response.getHeaders().add("Expires", "0");
    }
}