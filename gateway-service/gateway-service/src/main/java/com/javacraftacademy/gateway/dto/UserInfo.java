package com.javacraftacademy.gateway.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Data Transfer Object (DTO) pour les informations utilisateur.
 * 
 * <p>Cette classe encapsule toutes les informations d'un utilisateur authentifié
 * dans le système de passerelle. Elle contient les données personnelles, les rôles,
 * les permissions et les métadonnées d'authentification.</p>
 * 
 * <p>Utilise Jackson pour la sérialisation JSON avec exclusion des valeurs null
 * pour optimiser la taille des réponses.</p>
 * 
 * @author JavaCraft Academy
 * @version 1.0
 * @since 1.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserInfoDto implements Serializable {

    private static final long serialVersionUID = 1L;

    // ===== IDENTIFIANTS ET INFORMATIONS DE BASE =====
    
    /**
     * Identifiant unique de l'utilisateur dans le système.
     */
    @JsonProperty("userId")
    private Long userId;

    /**
     * Nom d'utilisateur unique pour l'authentification.
     */
    @NotBlank(message = "Le nom d'utilisateur est obligatoire")
    @Size(min = 3, max = 50, message = "Le nom d'utilisateur doit contenir entre 3 et 50 caractères")
    @JsonProperty("username")
    private String username;

    /**
     * Adresse email de l'utilisateur.
     */
    @Email(message = "Format d'email invalide")
    @NotBlank(message = "L'adresse email est obligatoire")
    @JsonProperty("email")
    private String email;

    // ===== INFORMATIONS PERSONNELLES =====
    
    /**
     * Prénom de l'utilisateur.
     */
    @Size(max = 100, message = "Le prénom ne peut pas dépasser 100 caractères")
    @JsonProperty("firstName")
    private String firstName;

    /**
     * Nom de famille de l'utilisateur.
     */
    @Size(max = 100, message = "Le nom de famille ne peut pas dépasser 100 caractères")
    @JsonProperty("lastName")
    private String lastName;

    /**
     * Nom d'affichage complet (généré automatiquement).
     */
    @JsonProperty("displayName")
    private String displayName;

    /**
     * Numéro de téléphone de l'utilisateur.
     */
    @Size(max = 20, message = "Le numéro de téléphone ne peut pas dépasser 20 caractères")
    @JsonProperty("phoneNumber")
    private String phoneNumber;

    /**
     * Avatar ou photo de profil (URL ou base64).
     */
    @JsonProperty("avatar")
    private String avatar;

    // ===== STATUTS ET ÉTATS =====
    
    /**
     * Indique si le compte utilisateur est actif.
     */
    @JsonProperty("isActive")
    private Boolean isActive;

    /**
     * Indique si l'email a été vérifié.
     */
    @JsonProperty("emailVerified")
    private Boolean emailVerified;

    /**
     * Indique si l'authentification à deux facteurs est activée.
     */
    @JsonProperty("twoFactorEnabled")
    private Boolean twoFactorEnabled;

    /**
     * Indique si le compte est verrouillé.
     */
    @JsonProperty("accountLocked")
    private Boolean accountLocked;

    // ===== RÔLES ET PERMISSIONS =====
    
    /**
     * Liste des rôles assignés à l'utilisateur.
     */
    @JsonProperty("roles")
    private List<String> roles;

    /**
     * Liste des permissions accordées à l'utilisateur.
     */
    @JsonProperty("permissions")
    private List<String> permissions;

    /**
     * Groupes auxquels appartient l'utilisateur.
     */
    @JsonProperty("groups")
    private List<String> groups;

    // ===== MÉTADONNÉES ET ATTRIBUTS =====
    
    /**
     * Attributs personnalisés de l'utilisateur.
     */
    @JsonProperty("customAttributes")
    private Map<String, Object> customAttributes;

    /**
     * Préférences utilisateur (langue, thème, etc.).
     */
    @JsonProperty("preferences")
    private Map<String, String> preferences;

    // ===== HORODATAGE =====
    
    /**
     * Date et heure de création du compte.
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;

    /**
     * Date et heure de la dernière mise à jour du profil.
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @JsonProperty("updatedAt")
    private LocalDateTime updatedAt;

    /**
     * Date et heure de la dernière connexion réussie.
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @JsonProperty("lastLoginAt")
    private LocalDateTime lastLoginAt;

    /**
     * Date et heure d'expiration du mot de passe.
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @JsonProperty("passwordExpiresAt")
    private LocalDateTime passwordExpiresAt;

    // ===== INFORMATIONS DE SESSION =====
    
    /**
     * Adresse IP de la dernière connexion.
     */
    @JsonProperty("lastLoginIp")
    private String lastLoginIp;

    /**
     * User-Agent du dernier navigateur utilisé.
     */
    @JsonProperty("lastUserAgent")
    private String lastUserAgent;

    /**
     * Identifiant de la session courante.
     */
    @JsonProperty("sessionId")
    private String sessionId;

    // ===== TOKENS D'AUTHENTIFICATION =====
    
    /**
     * Token d'accès JWT.
     */
    @JsonProperty("accessToken")
    private String accessToken;

    /**
     * Token de rafraîchissement.
     */
    @JsonProperty("refreshToken")
    private String refreshToken;

    /**
     * Type de token (généralement "Bearer").
     */
    @JsonProperty("tokenType")
    private String tokenType;

    /**
     * Durée de validité du token en secondes.
     */
    @JsonProperty("expiresIn")
    private Long expiresIn;

    // ===== CONSTRUCTEURS =====
    
    /**
     * Constructeur par défaut.
     */
    public UserInfoDto() {
        this.tokenType = "Bearer";
        this.isActive = true;
        this.emailVerified = false;
        this.twoFactorEnabled = false;
        this.accountLocked = false;
    }

    /**
     * Constructeur avec les informations essentielles.
     *
     * @param userId l'identifiant unique
     * @param username le nom d'utilisateur
     * @param email l'adresse email
     */
    public UserInfoDto(Long userId, String username, String email) {
        this();
        this.userId = userId;
        this.username = username;
        this.email = email;
    }

    // ===== GETTERS ET SETTERS =====
    
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
        updateDisplayName();
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
        updateDisplayName();
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Boolean getEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(Boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public Boolean getTwoFactorEnabled() {
        return twoFactorEnabled;
    }

    public void setTwoFactorEnabled(Boolean twoFactorEnabled) {
        this.twoFactorEnabled = twoFactorEnabled;
    }

    public Boolean getAccountLocked() {
        return accountLocked;
    }

    public void setAccountLocked(Boolean accountLocked) {
        this.accountLocked = accountLocked;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }

    public List<String> getGroups() {
        return groups;
    }

    public void setGroups(List<String> groups) {
        this.groups = groups;
    }

    public Map<String, Object> getCustomAttributes() {
        return customAttributes;
    }

    public void setCustomAttributes(Map<String, Object> customAttributes) {
        this.customAttributes = customAttributes;
    }

    public Map<String, String> getPreferences() {
        return preferences;
    }

    public void setPreferences(Map<String, String> preferences) {
        this.preferences = preferences;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public LocalDateTime getPasswordExpiresAt() {
        return passwordExpiresAt;
    }

    public void setPasswordExpiresAt(LocalDateTime passwordExpiresAt) {
        this.passwordExpiresAt = passwordExpiresAt;
    }

    public String getLastLoginIp() {
        return lastLoginIp;
    }

    public void setLastLoginIp(String lastLoginIp) {
        this.lastLoginIp = lastLoginIp;
    }

    public String getLastUserAgent() {
        return lastUserAgent;
    }

    public void setLastUserAgent(String lastUserAgent) {
        this.lastUserAgent = lastUserAgent;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }

    // ===== MÉTHODES UTILITAIRES =====
    
    /**
     * Met à jour automatiquement le nom d'affichage basé sur prénom et nom.
     */
    private void updateDisplayName() {
        StringBuilder display = new StringBuilder();
        
        if (firstName != null && !firstName.trim().isEmpty()) {
            display.append(firstName.trim());
        }
        
        if (lastName != null && !lastName.trim().isEmpty()) {
            if (display.length() > 0) {
                display.append(" ");
            }
            display.append(lastName.trim());
        }
        
        if (display.length() == 0 && username != null) {
            display.append(username);
        }
        
        this.displayName = display.toString();
    }

    /**
     * Vérifie si l'utilisateur possède un rôle spécifique.
     *
     * @param role le rôle à vérifier
     * @return true si l'utilisateur possède ce rôle
     */
    public boolean hasRole(String role) {
        return roles != null && roles.contains(role);
    }

    /**
     * Vérifie si l'utilisateur possède une permission spécifique.
     *
     * @param permission la permission à vérifier
     * @return true si l'utilisateur possède cette permission
     */
    public boolean hasPermission(String permission) {
        return permissions != null && permissions.contains(permission);
    }

    /**
     * Vérifie si l'utilisateur appartient à un groupe spécifique.
     *
     * @param group le groupe à vérifier
     * @return true si l'utilisateur appartient à ce groupe
     */
    public boolean belongsToGroup(String group) {
        return groups != null && groups.contains(group);
    }

    /**
     * Vérifie si le compte utilisateur peut se connecter.
     *
     * @return true si le compte est actif, non verrouillé et email vérifié
     */
    public boolean canLogin() {
        return Boolean.TRUE.equals(isActive) && 
               !Boolean.TRUE.equals(accountLocked) && 
               Boolean.TRUE.equals(emailVerified);
    }

    /**
     * Vérifie si le mot de passe a expiré.
     *
     * @return true si le mot de passe a expiré
     */
    public boolean isPasswordExpired() {
        return passwordExpiresAt != null && passwordExpiresAt.isBefore(LocalDateTime.now());
    }

    /**
     * Obtient un attribut personnalisé par sa clé.
     *
     * @param key la clé de l'attribut
     * @return la valeur de l'attribut ou null si non trouvé
     */
    public Object getCustomAttribute(String key) {
        return customAttributes != null ? customAttributes.get(key) : null;
    }

    /**
     * Définit un attribut personnalisé.
     *
     * @param key la clé de l'attribut
     * @param value la valeur de l'attribut
     */
    public void setCustomAttribute(String key, Object value) {
        if (customAttributes == null) {
            customAttributes = new java.util.HashMap<>();
        }
        customAttributes.put(key, value);
    }

    /**
     * Obtient une préférence utilisateur par sa clé.
     *
     * @param key la clé de la préférence
     * @return la valeur de la préférence ou null si non trouvée
     */
    public String getPreference(String key) {
        return preferences != null ? preferences.get(key) : null;
    }

    /**
     * Définit une préférence utilisateur.
     *
     * @param key la clé de la préférence
     * @param value la valeur de la préférence
     */
    public void setPreference(String key, String value) {
        if (preferences == null) {
            preferences = new java.util.HashMap<>();
        }
        preferences.put(key, value);
    }

    // ===== MÉTHODES STANDARDS =====
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserInfoDto that = (UserInfoDto) o;
        return Objects.equals(userId, that.userId) &&
               Objects.equals(username, that.username) &&
               Objects.equals(email, that.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, username, email);
    }

    @Override
    public String toString() {
        return "UserInfoDto{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", displayName='" + displayName + '\'' +
                ", isActive=" + isActive +
                ", emailVerified=" + emailVerified +
                ", roles=" + roles +
                ", createdAt=" + createdAt +
                ", lastLoginAt=" + lastLoginAt +
                '}';
    }
}
