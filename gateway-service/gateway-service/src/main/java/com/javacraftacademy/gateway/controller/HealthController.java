package com.javacraftacademy.gateway.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Contrôleur de santé pour le Gateway
 * 
 * Ce contrôleur fournit des endpoints pour vérifier l'état de santé
 * du gateway et de ses dépendances (Redis, etc.).
 * 
 * @author JavaCraftAcademy Team
 * @version 1.0
 */
@RestController
@RequestMapping("/api/health")
public class HealthController {

    @Autowired
    private ReactiveStringRedisTemplate redisTemplate;

    /**
     * Endpoint de santé simple
     * 
     * @return statut de santé basique
     */
    @GetMapping
    public Mono<ResponseEntity<Map<String, Object>>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "gateway-service");
        response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        response.put("version", "1.0.0");
        
        return Mono.just(ResponseEntity.ok(response));
    }

    /**
     * Endpoint de santé détaillé avec vérification des dépendances
     * 
     * @return statut de santé détaillé
     */
    @GetMapping("/detailed")
    public Mono<ResponseEntity<Map<String, Object>>> detailedHealth() {
        Map<String, Object> response = new HashMap<>();
        response.put("service", "gateway-service");
        response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        response.put("version", "1.0.0");
        
        // Vérification de Redis
        return checkRedisHealth()
                .map(redisHealth -> {
                    Map<String, Object> dependencies = new HashMap<>();
                    dependencies.put("redis", redisHealth);
                    
                    response.put("dependencies", dependencies);
                    
                    // Déterminer le statut global
                    boolean allHealthy = redisHealth.get("status").equals("UP");
                    response.put("status", allHealthy ? "UP" : "DOWN");
                    
                    HttpStatus httpStatus = allHealthy ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE;
                    return ResponseEntity.status(httpStatus).body(response);
                })
                .onErrorReturn(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body(createErrorResponse("Erreur lors de la vérification de santé")));
    }

    /**
     * Endpoint de readiness pour Kubernetes
     * 
     * @return statut de disponibilité
     */
    @GetMapping("/ready")
    public Mono<ResponseEntity<Map<String, Object>>> readiness() {
        return checkRedisHealth()
                .map(redisHealth -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("ready", redisHealth.get("status").equals("UP"));
                    response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    
                    boolean isReady = (Boolean) response.get("ready");
                    HttpStatus status = isReady ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE;
                    
                    return ResponseEntity.status(status).body(response);
                })
                .onErrorReturn(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body(Map.of("ready", false, "error", "Redis connection failed")));
    }

    /**
     * Endpoint de liveness pour Kubernetes
     * 
     * @return statut de vie de l'application
     */
    @GetMapping("/live")
    public Mono<ResponseEntity<Map<String, Object>>> liveness() {
        Map<String, Object> response = new HashMap<>();
        response.put("alive", true);
        response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        return Mono.just(ResponseEntity.ok(response));
    }

    /**
     * Vérifie la santé de Redis
     * 
     * @return informations sur l'état de Redis
     */
    private Mono<Map<String, Object>> checkRedisHealth() {
        Map<String, Object> redisHealth = new HashMap<>();
        
        return redisTemplate.opsForValue()
                .set("health:check", "ping")
                .timeout(Duration.ofSeconds(5))
                .then(redisTemplate.opsForValue().get("health:check"))
                .timeout(Duration.ofSeconds(5))
                .map(value -> {
                    redisHealth.put("status", "UP");
                    redisHealth.put("details", "Connection successful");
                    redisHealth.put("responseTime", System.currentTimeMillis());
                    return redisHealth;
                })
                .onErrorReturn(Map.of(
                    "status", "DOWN",
                    "details", "Connection failed or timeout",
                    "error", "Redis unavailable"
                ));
    }

    /**
     * Crée une réponse d'erreur standardisée
     * 
     * @param message message d'erreur
     * @return réponse d'erreur
     */
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "DOWN");
        response.put("service", "gateway-service");
        response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        response.put("error", message);
        return response;
    }
}