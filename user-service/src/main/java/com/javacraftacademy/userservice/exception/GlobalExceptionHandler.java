
package com.javacraftacademy.userservice.exception;

import com.javacraftacademy.userservice.model.dto.response.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import javax.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Gestionnaire global des exceptions pour l'application User Service.
 * 
 * <p>Cette classe centralise la gestion de toutes les exceptions de l'application
 * en fournissant une réponse cohérente et standardisée pour chaque type d'erreur.
 * Elle utilise l'annotation @RestControllerAdvice pour intercepter automatiquement
 * les exceptions levées par les contrôleurs et les transformer en réponses HTTP
 * appropriées.</p>
 * 
 * <h3>Fonctionnalités principales :</h3>
 * <ul>
 *   <li>Gestion centralisée des exceptions métier (UserNotFoundException, etc.)</li>
 *   <li>Traitement des erreurs de validation des données</li>
 *   <li>Gestion des erreurs d'authentification et d'autorisation</li>
 *   <li>Logging automatique des erreurs pour le monitoring</li>
 *   <li>Standardisation du format des réponses d'erreur</li>
 * </ul>
 * 
 * <h3>Relations avec l'application :</h3>
 * <ul>
 *   <li><strong>Controllers</strong> : Intercepte les exceptions levées par AuthController, 
 *       UserController et ProfileController</li>
 *   <li><strong>Services</strong> : Traite les exceptions métier levées par AuthService, 
 *       UserService et ProfileService</li>
 *   <li><strong>Security</strong> : Gère les erreurs d'authentification JWT</li>
 *   <li><strong>Validation</strong> : Traite les erreurs de validation des DTOs</li>
 *   <li><strong>ApiResponse</strong> : Utilise le DTO de réponse standardisé</li>
 * </ul>
 * 
 * <h3>Pattern de conception :</h3>
 * <p>Implémente le pattern "Global Exception Handler" qui permet de séparer
 * la logique de gestion d'erreurs de la logique métier, améliorant ainsi
 * la maintenabilité et la cohérence de l'application.</p>
 * 
 * @author JavaCraft Academy
 * @version 1.0
 * @since 1.0
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Gère les exceptions UserNotFoundException.
     * 
     * <p>Cette méthode est invoquée automatiquement lorsqu'un utilisateur
     * demandé n'est pas trouvé dans la base de données. Elle retourne
     * une réponse HTTP 404 avec un message d'erreur approprié.</p>
     * 
     * @param ex L'exception UserNotFoundException levée
     * @param request La requête web qui a causé l'exception
     * @return ResponseEntity contenant l'ApiResponse avec le statut 404
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleUserNotFoundException(
            UserNotFoundException ex, WebRequest request) {
        
        logger.warn("Utilisateur non trouvé : {}", ex.getMessage());
        
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(false)
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * Gère les exceptions EmailAlreadyExistsException.
     * 
     * <p>Cette méthode traite les cas où un utilisateur tente de s'inscrire
     * avec une adresse email déjà utilisée. Elle retourne une réponse HTTP 409
     * (Conflict) avec un message d'erreur explicite.</p>
     * 
     * @param ex L'exception EmailAlreadyExistsException levée
     * @param request La requête web qui a causé l'exception
     * @return ResponseEntity contenant l'ApiResponse avec le statut 409
     */
    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Void>> handleEmailAlreadyExistsException(
            EmailAlreadyExistsException ex, WebRequest request) {
        
        logger.warn("Tentative d'inscription avec email existant : {}", ex.getMessage());
        
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(false)
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    /**
     * Gère les exceptions InvalidCredentialsException.
     * 
     * <p>Cette méthode traite les erreurs d'authentification lorsque
     * les identifiants fournis sont incorrects. Elle retourne une réponse
     * HTTP 401 (Unauthorized) sans révéler d'informations sensibles.</p>
     * 
     * @param ex L'exception InvalidCredentialsException levée
     * @param request La requête web qui a causé l'exception
     * @return ResponseEntity contenant l'ApiResponse avec le statut 401
     */
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidCredentialsException(
            InvalidCredentialsException ex, WebRequest request) {
        
        logger.warn("Tentative de connexion avec identifiants invalides");
        
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(false)
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    /**
     * Gère les exceptions TokenExpiredException.
     * 
     * <p>Cette méthode traite les cas où un token JWT ou refresh token
     * a expiré. Elle retourne une réponse HTTP 401 avec un message
     * indiquant que l'utilisateur doit se reconnecter.</p>
     * 
     * @param ex L'exception TokenExpiredException levée
     * @param request La requête web qui a causé l'exception
     * @return ResponseEntity contenant l'ApiResponse avec le statut 401
     */
    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<ApiResponse<Void>> handleTokenExpiredException(
            TokenExpiredException ex, WebRequest request) {
        
        logger.warn("Token expiré : {}", ex.getMessage());
        
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(false)
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    /**
     * Gère les exceptions de validation des arguments de méthode.
     * 
     * <p>Cette méthode traite les erreurs de validation automatiques
     * de Spring Boot lorsque les données d'entrée ne respectent pas
     * les contraintes définies sur les DTOs. Elle collecte tous les
     * erreurs de validation et les retourne dans un format structuré.</p>
     * 
     * @param ex L'exception MethodArgumentNotValidException levée
     * @return ResponseEntity contenant les détails des erreurs de validation
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        
        logger.warn("Erreurs de validation détectées");
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        ApiResponse<Map<String, String>> response = ApiResponse.<Map<String, String>>builder()
                .success(false)
                .message("Erreurs de validation")
                .data(errors)
                .timestamp(LocalDateTime.now())
                .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Gère les exceptions de violation de contraintes.
     * 
     * <p>Cette méthode traite les erreurs de validation au niveau
     * des contraintes de base de données ou des validations custom.
     * Elle extrait les messages d'erreur et les formate de manière
     * compréhensible pour le client.</p>
     * 
     * @param ex L'exception ConstraintViolationException levée
     * @return ResponseEntity contenant les détails des violations de contraintes
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleConstraintViolationException(
            ConstraintViolationException ex) {
        
        logger.warn("Violations de contraintes détectées");
        
        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(violation -> {
            String fieldName = violation.getPropertyPath().toString();
            String errorMessage = violation.getMessage();
            errors.put(fieldName, errorMessage);
        });
        
        ApiResponse<Map<String, String>> response = ApiResponse.<Map<String, String>>builder()
                .success(false)
                .message("Violations de contraintes")
                .data(errors)
                .timestamp(LocalDateTime.now())
                .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Gère les exceptions BadCredentialsException de Spring Security.
     * 
     * <p>Cette méthode traite les erreurs d'authentification levées
     * par Spring Security lors de tentatives de connexion avec des
     * identifiants incorrects.</p>
     * 
     * @param ex L'exception BadCredentialsException levée
     * @param request La requête web qui a causé l'exception
     * @return ResponseEntity contenant l'ApiResponse avec le statut 401
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentialsException(
            BadCredentialsException ex, WebRequest request) {
        
        logger.warn("Échec d'authentification Spring Security");
        
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(false)
                .message("Identifiants invalides")
                .timestamp(LocalDateTime.now())
                .build();
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    /**
     * Gère toutes les autres exceptions non spécifiquement traitées.
     * 
     * <p>Cette méthode sert de filet de sécurité pour capturer toutes
     * les exceptions inattendues qui ne sont pas gérées par les autres
     * handlers. Elle log l'erreur complète pour le debugging et retourne
     * une réponse générique pour ne pas exposer d'informations sensibles.</p>
     * 
     * @param ex L'exception générique levée
     * @param request La requête web qui a causé l'exception
     * @return ResponseEntity contenant une réponse d'erreur générique
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGlobalException(
            Exception ex, WebRequest request) {
        
        logger.error("Erreur inattendue : ", ex);
        
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(false)
                .message("Une erreur inattendue s'est produite")
                .timestamp(LocalDateTime.now())
                .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}