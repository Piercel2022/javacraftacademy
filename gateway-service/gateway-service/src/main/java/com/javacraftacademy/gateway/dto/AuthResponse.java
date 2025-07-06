package com.javacraftacademy.gateway.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Data Transfer Object (DTO) pour les réponses d'authentification.
 * 
 * <p>Cette classe encapsule toutes les informations retournées après une
 * authentification réussie ou échouée dans le système de passerelle. Elle
 * fournit les tokens d'accès, les informations utilisateur et les métadonnées
 * de la session.</p>
 * 
 * <h3>Fonctionnalités principales :</h3>
 * <ul>
 *   <li>Transport des tokens JWT d'authentification et de rafraîchissement</li>
 *   <li>Encapsulation des informations de session utilisateur</li>
 *   <li>Gestion des erreurs d'authentification</li>
 *   <li>Métadonnées de sécurité (expiration, rôles, permissions)</li>
 * </ul>
 * 
 * <h3>Relations avec l'application :</h3>
 * <ul>
 *   <li><strong>AuthenticationService</strong> : Construit et retourne cette réponse</li>
 *   <li><strong>JwtService</strong> : Fournit les tokens inclus dans la réponse</li>
 *   <li><strong>GatewayController</strong> : Retourne cette réponse au client</li>
 *   <li><strong>UserInfo</strong> : Inclut les informations utilisateur détaillées</li>
 *   <li><strong>AuthenticationFilter</strong> : Utilise les tokens pour valider les requêtes</li>
 *   <li><strong>SecurityConfig</strong> : Configure les règles de sécurité basées sur ces données</li>
 * </ul>
 * 
 * <h3>Flux d'utilisation :</h3>
 * <pre>
 * AuthenticationService → JwtService (génération tokens)
 *                      ↓
 *                 AuthResponse (construction)
 *                      ↓
 *                 GatewayController → Client
 * </pre>
 * 
 * <h3>Structure de la réponse :</h3>
 * <ul>
 *   <li>Succès : contient les tokens, informations utilisateur et métadonnées</li>
 *   <li>Échec : contient les messages d'erreur et codes de statut</li>
 * </ul>
 * 
 * @author JavaCraft Academy
 * @version 1.0
 * @since 1.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponse implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Indicateur de succès de l'authentification.
     * 
     * <p>True si l'authentification a réussi, false en cas d'échec.</p>
     */
    @JsonProperty("success")
    private boolean success;
    
    /**
     * Message descriptif du résultat de l'authentification.
     * 
     * <p>Contient un message de succès ou d'erreur à destination
     * de l'utilisateur final.</p>
     */
    @JsonProperty("message")
    private String message;
    
    /**
     * Token d'accès JWT.
     * 
     * <p>Token principal utilisé pour l'authentification des requêtes
     * API. Contient les informations d'identité et les permissions
     * de l'utilisateur.</p>
     */
    @JsonProperty("accessToken")
    private String accessToken;
    
    /**
     * Token de rafraîchissement JWT.
     * 
     * <p>Token longue durée utilisé pour renouveler l'accessToken
     * sans redemander les identifiants à l'utilisateur.</p>
     */
    @JsonProperty("refreshToken")
    private String refreshToken;
    
    /**
     * Type de token utilisé.
     * 
     * <p>Généralement "Bearer" pour les tokens JWT.</p>
     */
    @JsonProperty("tokenType")
    private String tokenType = "Bearer";
    
    /**
     * Durée de vie du token d'accès en secondes.
     * 
     * <p>Indique au client quand le token d'accès expirera.</p>
     */
    @JsonProperty("expiresIn")
    private Long expiresIn;
    
    /**
     * Timestamp d'expiration du token d'accès.
     * 
     * <p>Date et heure précises d'expiration du token.</p>
     */
    @JsonProperty("expiresAt")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime expiresAt;
    
    /**
     * Informations détaillées de l'utilisateur authentifié.
     * 
     * <p>Inclut les données personnelles, rôles et permissions
     * de l'utilisateur connecté.</p>
     */
    @JsonProperty("user")
    private UserInfo user;
    
    /**
     * Liste des permissions accordées à l'utilisateur.
     * 
     * <p>Définit les actions autorisées pour cet utilisateur
     * dans le système.</p>
     */
    @JsonProperty("permissions")
    private List<String> permissions;
    
    /**
     * Liste des rôles assignés à l'utilisateur.
     * 
     * <p>Définit les niveaux d'accès et responsabilités
     * de l'utilisateur dans le système.</p>
     */
    @JsonProperty("roles")
    private List<String> roles;
    
    /**
     * Code d'erreur en cas d'échec d'authentification.
     * 
     * <p>Code numérique ou textuel identifiant le type
     * d'erreur rencontré.</p>
     */
    @JsonProperty("errorCode")
    private String errorCode;
    
    /**
     * Timestamp de création de la réponse.
     * 
     * <p>Date et heure de génération de cette réponse
     * d'authentification.</p>
     */
    @JsonProperty("timestamp")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    
    /**
     * Identifiant de session unique.
     * 
     * <p>Permet de tracer et gérer la session utilisateur
     * à travers les différents services.</p>
     */
    @JsonProperty("sessionId")
    private String sessionId;
    
    /**
     * Constructeur par défaut.
     * 
     * <p>Initialise la réponse avec le timestamp actuel.</p>
     */
    public AuthResponse() {
        this.timestamp = LocalDateTime.now();
    }
    
    /**
     * Constructeur pour une réponse de succès.
     * 
     * @param accessToken le token d'accès JWT
     * @param refreshToken le token de rafraîchissement
     * @param expiresIn la durée de vie du token en secondes
     * @param user les informations utilisateur
     */
    public AuthResponse(String accessToken, String refreshToken, Long expiresIn, UserInfo user) {
        this();
        this.success = true;
        this.message = "Authentification réussie";
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.expiresAt = LocalDateTime.now().plusSeconds(expiresIn);
        this.user = user;
    }
    
    /**
     * Constructeur pour une réponse d'échec.
     * 
     * @param message le message d'erreur
     * @param errorCode le code d'erreur
     */
    public AuthResponse(String message, String errorCode) {
        this();
        this.success = false;
        this.message = message;
        this.errorCode = errorCode;
    }
    
    /**
     * Crée une réponse de succès avec tous les paramètres.
     * 
     * @param accessToken le token d'accès
     * @param refreshToken le token de rafraîchissement
     * @param expiresIn la durée de vie en secondes
     * @param user les informations utilisateur
     * @param roles les rôles utilisateur
     * @param permissions les permissions utilisateur
     * @param sessionId l'identifiant de session
     * @return une nouvelle instance d'AuthResponse configurée pour le succès
     */
    public static AuthResponse success(String accessToken, String refreshToken, Long expiresIn, 
                                     UserInfo user, List<String> roles, List<String> permissions, 
                                     String sessionId) {
        AuthResponse response = new AuthResponse(accessToken, refreshToken, expiresIn, user);
        response.setRoles(roles);
        response.setPermissions(permissions);
        response.setSessionId(sessionId);
        return response;
    }
    
    /**
     * Crée une réponse d'échec avec message et code d'erreur.
     * 
     * @param message le message d'erreur
     * @param errorCode le code d'erreur
     * @return une nouvelle instance d'AuthResponse configurée pour l'échec
     */
    public static AuthResponse failure(String message, String errorCode) {
        return new AuthResponse(message, errorCode);
    }
    
    /**
     * Crée une réponse d'échec avec message seulement.
     * 
     * @param message le message d'erreur
     * @return une nouvelle instance d'AuthResponse configurée pour l'échec
     */
    public static AuthResponse failure(String message) {
        return new AuthResponse(message, null);
    }
    
    /**
     * Vérifie si l'authentification a réussi.
     * 
     * @return true si succès, false sinon
     */
    public boolean isSuccess() {
        return success;
    }
    
    /**
     * Définit le statut de succès de l'authentification.
     * 
     * @param success true pour succès, false pour échec
     */
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    /**
     * Récupère le message de la réponse.
     * 
     * @return le message descriptif
     */
    public String getMessage() {
        return message;
    }
    
    /**
     * Définit le message de la réponse.
     * 
     * @param message le message à définir
     */
    public void setMessage(String message) {
        this.message = message;
    }
    
    /**
     * Récupère le token d'accès.
     * 
     * @return le token d'accès JWT
     */
    public String getAccessToken() {
        return accessToken;
    }
    
    /**
     * Définit le token d'accès.
     * 
     * @param accessToken le token d'accès à définir
     */
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
    
    /**
     * Récupère le token de rafraîchissement.
     * 
     * @return le token de rafraîchissement
     */
    public String getRefreshToken() {
        return refreshToken;
    }
    
    /**
     * Définit le token de rafraîchissement.
     * 
     * @param refreshToken le token de rafraîchissement à définir
     */
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
    
    /**
     * Récupère le type de token.
     * 
     * @return le type de token (généralement "Bearer")
     */
    public String getTokenType() {
        return tokenType;
    }
    
    /**
     * Définit le type de token.
     * 
     * @param tokenType le type de token à définir
     */
    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }
    
    /**
     * Récupère la durée de vie du token en secondes.
     * 
     * @return la durée de vie en secondes
     */
    public Long getExpiresIn() {
        return expiresIn;
    }
    
    /**
     * Définit la durée de vie du token.
     * 
     * @param expiresIn la durée de vie en secondes
     */
    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
        if (expiresIn != null) {
            this.expiresAt = LocalDateTime.now().plusSeconds(expiresIn);
        }
    }
    
    /**
     * Récupère la date d'expiration du token.
     * 
     * @return la date et heure d'expiration
     */
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
    
    /**
     * Définit la date d'expiration du token.
     * 
     * @param expiresAt la date et heure d'expiration
     */
    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
    
    /**
     * Récupère les informations utilisateur.
     * 
     * @return les informations de l'utilisateur authentifié
     */
    public UserInfo getUser() {
        return user;
    }
    
    /**
     * Définit les informations utilisateur.
     * 
     * @param user les informations utilisateur à définir
     */
    public void setUser(UserInfo user) {
        this.user = user;
    }
    
    /**
     * Récupère la liste des permissions.
     * 
     * @return la liste des permissions accordées
     */
    public List<String> getPermissions() {
        return permissions;
    }
    
    /**
     * Définit la liste des permissions.
     * 
     * @param permissions la liste des permissions à définir
     */
    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }
    
    /**
     * Récupère la liste des rôles.
     * 
     * @return la liste des rôles assignés
     */
    public List<String> getRoles() {
        return roles;
    }
    
    /**
     * Définit la liste des rôles.
     * 
     * @param roles la liste des rôles à définir
     */
    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
    
    /**
     * Récupère le code d'erreur.
     * 
     * @return le code d'erreur ou null si aucune erreur
     */
    public String getErrorCode() {
        return errorCode;
    }
    
    /**
     * Définit le code d'erreur.
     * 
     * @param errorCode le code d'erreur à définir
     */
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
    
    /**
     * Récupère le timestamp de création.
     * 
     * @return la date et heure de création de la réponse
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    /**
     * Définit le timestamp de création.
     * 
     * @param timestamp la date et heure de création
     */
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    /**
     * Récupère l'identifiant de session.
     * 
     * @return l'identifiant de session unique
     */
    public String getSessionId() {
        return sessionId;
    }
    
    /**
     * Définit l'identifiant de session.
     * 
     * @param sessionId l'identifiant de session à définir
     */
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    
    /**
     * Vérifie si le token est encore valide.
     * 
     * @return true si le token n'a pas expiré, false sinon
     */
    public boolean isTokenValid() {
        return expiresAt != null && LocalDateTime.now().isBefore(expiresAt);
    }
    
    /**
     * Nettoie les données sensibles de la réponse.
     * 
     * <p>Supprime les tokens et autres informations sensibles
     * de la mémoire pour des raisons de sécurité.</p>
     */
    public void clearSensitiveData() {
        this.accessToken = null;
        this.refreshToken = null;
        if (this.user != null) {
            this.user.clearSensitiveData();
        }
    }
    
    /**
     * Compare cette réponse d'authentification avec une autre.
     * 
     * @param obj l'objet à comparer
     * @return true si les objets sont égaux, false sinon
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        AuthResponse that = (AuthResponse) obj;
        return success == that.success &&
               Objects.equals(accessToken, that.accessToken) &&
               Objects.equals(refreshToken, that.refreshToken) &&
               Objects.equals(sessionId, that.sessionId) &&
               Objects.equals(user, that.user);
    }
    
    /**
     * Génère le code de hachage pour cette réponse.
     * 
     * @return le code de hachage
     */
    @Override
    public int hashCode() {
        return Objects.hash(success, accessToken, refreshToken, sessionId, user);
    }
    
    /**
     * Représentation textuelle de la réponse d'authentification.
     * 
     * <p>Note : Les tokens sont masqués pour des raisons de sécurité.</p>
     * 
     * @return une chaîne représentant cet objet
     */
    @Override
    public String toString() {
        return "AuthResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", accessToken='" + (accessToken != null ? "***" : null) + '\'' +
                ", refreshToken='" + (refreshToken != null ? "***" : null) + '\'' +
                ", tokenType='" + tokenType + '\'' +
                ", expiresIn=" + expiresIn +
                ", expiresAt=" + expiresAt +
                ", user=" + user +
                ", sessionId='" + sessionId + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}