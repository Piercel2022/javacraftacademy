
package com.javacraftacademy.gateway.controller;

import com.javacraftacademy.gateway.dto.AuthResponse;
import com.javacraftacademy.gateway.dto.LoginRequest;
import com.javacraftacademy.gateway.service.AuthenticationService;
import com.javacraftacademy.gateway.service.JwtService;
import com.javacraftaclademy.gateway.util.ResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.util.Map;

/**
 * Contrôleur principal du Gateway
 * 
 * Ce contrôleur gère l'authentification et les opérations
 * principales du gateway.
 * 
 * @author JavaCraftAcademy Team
 * @version 1.0
 */
@RestController
@RequestMapping("/api/gateway")
@CrossOrigin(origins = "*", maxAge = 3600)
public class GatewayController {

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private JwtService jwtService;

    /**
     * Endpoint d'authentification
     * 
     * @param loginRequest requête de connexion
     * @return réponse d'authentification avec token JWT
     */
    @PostMapping("/auth/login")
    public Mono<ResponseEntity<AuthResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {
        return authenticationService.authenticate(loginRequest)
                .map(authResponse -> ResponseEntity.ok(authResponse))
                .onErrorResume(throwable -> {
                    AuthResponse errorResponse = AuthResponse.builder()
                            .success(false)
                            .message("Authentification échouée: " + throwable.getMessage())
                            .build();
                    return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse));
                });
    }

    /**
     * Endpoint de rafraîchissement du token
     * 
     * @param refreshToken token de rafraîchissement
     * @return nouveau token JWT
     */
    @PostMapping("/auth/refresh")
    public Mono<ResponseEntity<AuthResponse>> refreshToken(@RequestHeader("Authorization") String refreshToken) {
        // Retirer le préfixe "Bearer " si présent
        String token = refreshToken.startsWith("Bearer ") ? 
                      refreshToken.substring(7) : refreshToken;
        
        return jwtService.refreshToken(token)
                .map(newToken -> {
                    AuthResponse response = AuthResponse.builder()
                            .success(true)
                            .token(newToken)
                            .message("Token rafraîchi avec succès")
                            .build();
                    return ResponseEntity.ok(response);
                })
                .onErrorResume(throwable -> {
                    AuthResponse errorResponse = AuthResponse.builder()
                            .success(false)
                            .message("Échec du rafraîchissement: " + throwable.getMessage())
                            .build();
                    return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse));
                });
    }

    /**
     * Endpoint de validation du token
     * 
     * @param token token JWT à valider
     * @return statut de validation
     */
    @PostMapping("/auth/validate")
    public Mono<ResponseEntity<Map<String, Object>>> validateToken(@RequestHeader("Authorization") String token) {
        // Retirer le préfixe "Bearer " si présent
        String jwtToken = token.startsWith("Bearer ") ? 
                         token.substring(7) : token;
        
        return jwtService.validateToken(jwtToken)
                .map(isValid -> {
                    if (isValid) {
                        return ResponseEntity.ok(ResponseUtil.createSuccessResponse("Token valide"));
                    } else {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body(ResponseUtil.createErrorResponse("Token invalide"));
                    }
                })
                .onErrorResume(throwable -> 
                    Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(ResponseUtil.createErrorResponse("Erreur de validation: " + throwable.getMessage())))
                );
    }

    /**
     * Endpoint de déconnexion
     * 
     * @param token token JWT à invalider
     * @return confirmation de déconnexion
     */
    @PostMapping("/auth/logout")
    public Mono<ResponseEntity<Map<String, Object>>> logout(@RequestHeader("Authorization") String token) {
        // Retirer le préfixe "Bearer " si présent
        String jwtToken = token.startsWith("Bearer ") ? 
                         token.substring(7) : token;
        
        return jwtService.invalidateToken(jwtToken)
                .map(success -> {
                    if (success) {
                        return ResponseEntity.ok(ResponseUtil.createSuccessResponse("Déconnexion réussie"));
                    } else {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(ResponseUtil.createErrorResponse("Échec de la déconnexion"));
                    }
                })
                .onErrorResume(throwable -> 
                    Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(ResponseUtil.createErrorResponse("Erreur lors de la déconnexion: " + throwable.getMessage())))
                );
    }

    /**
     * Endpoint pour obtenir les informations utilisateur à partir du token
     * 
     * @param token token JWT
     * @return informations utilisateur
     */
    @GetMapping("/auth/user-info")
    public Mono<ResponseEntity<Map<String, Object>>> getUserInfo(@RequestHeader("Authorization") String token) {
        // Retirer le préfixe "Bearer " si présent
        String jwtToken = token.startsWith("Bearer ") ? 
                         token.substring(7) : token;
        
        return jwtService.extractUserInfo(jwtToken)
                .map(userInfo -> ResponseEntity.ok(Map.of(
                    "success", true,
                    "user", userInfo,
                    "message", "Informations utilisateur récupérées"
                )))
                .onErrorResume(throwable -> 
                    Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(ResponseUtil.createErrorResponse("Token invalide ou expiré: " + throwable.getMessage())))
                );
    }

    /**
     * Endpoint de vérification des permissions
     * 
     * @param token token JWT
     * @param resource ressource demandée
     * @param action action demandée
     * @return autorisation accordée ou non
     */
    @GetMapping("/auth/check-permission")
    public Mono<ResponseEntity<Map<String, Object>>> checkPermission(
            @RequestHeader("Authorization") String token,
            @RequestParam String resource,
            @RequestParam String action) {
        
        // Retirer le préfixe "Bearer " si présent
        String jwtToken = token.startsWith("Bearer ") ? 
                         token.substring(7) : token;
        
        return authenticationService.checkPermission(jwtToken, resource, action)
                .map(hasPermission -> {
                    Map<String, Object> response = Map.of(
                        "success", true,
                        "hasPermission", hasPermission,
                        "resource", resource,
                        "action", action,
                        "message", hasPermission ? "Permission accordée" : "Permission refusée"
                    );
                    
                    HttpStatus status = hasPermission ? HttpStatus.OK : HttpStatus.FORBIDDEN;
                    return ResponseEntity.status(status).body(response);
                })
                .onErrorResume(throwable -> 
                    Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(ResponseUtil.createErrorResponse("Erreur de vérification: " + throwable.getMessage())))
                );
    }

    /**
     * Endpoint de status du gateway
     * 
     * @return informations sur le statut du gateway
     */
    @GetMapping("/status")
    public Mono<ResponseEntity<Map<String, Object>>> getGatewayStatus() {
        return Mono.just(ResponseEntity.ok(Map.of(
            "service", "gateway-service",
            "status", "RUNNING",
            "version", "1.0.0",
            "timestamp", System.currentTimeMillis(),
            "message", "Gateway opérationnel"
        )));
    }
}