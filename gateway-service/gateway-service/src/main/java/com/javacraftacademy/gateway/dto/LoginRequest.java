package com.javacraftacademy.gateway.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.Objects;

/**
 * Data Transfer Object (DTO) pour les requêtes d'authentification.
 * 
 * <p>Cette classe encapsule les informations nécessaires pour l'authentification
 * d'un utilisateur dans le système de passerelle. Elle est utilisée comme objet
 * de transfert de données entre le client et le service d'authentification.</p>
 * 
 * <h3>Fonctionnalités principales :</h3>
 * <ul>
 *   <li>Validation des données d'entrée (email et mot de passe)</li>
 *   <li>Sérialisation/Désérialisation JSON</li>
 *   <li>Encapsulation sécurisée des informations d'authentification</li>
 * </ul>
 * 
 * <h3>Relations avec l'application :</h3>
 * <ul>
 *   <li><strong>GatewayController</strong> : Reçoit et traite les requêtes d'authentification</li>
 *   <li><strong>AuthenticationService</strong> : Utilise ces données pour valider l'utilisateur</li>
 *   <li><strong>AuthenticationFilter</strong> : Intercepte et valide les requêtes d'authentification</li>
 *   <li><strong>JwtService</strong> : Génère les tokens JWT basés sur ces informations</li>
 *   <li><strong>UserValidationService</strong> : Valide les informations utilisateur</li>
 * </ul>
 * 
 * <h3>Flux d'utilisation :</h3>
 * <pre>
 * Client → GatewayController → AuthenticationService → UserValidationService
 *                          ↓
 *                     JwtService (génération token)
 *                          ↓
 *                     AuthResponse (réponse)
 * </pre>
 * 
 * @author JavaCraft Academy
 * @version 1.0
 * @since 1.0
 */
public class LoginRequest implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Adresse email de l'utilisateur.
     * 
     * <p>Utilisée comme identifiant unique pour l'authentification.
     * Doit respecter le format email valide et ne peut pas être vide.</p>
     */
    @JsonProperty("email")
    @NotBlank(message = "L'email ne peut pas être vide")
    @Email(message = "Format d'email invalide")
    @Size(max = 255, message = "L'email ne peut pas dépasser 255 caractères")
    private String email;
    
    /**
     * Mot de passe de l'utilisateur.
     * 
     * <p>Utilisé pour l'authentification. Doit respecter les critères
     * de sécurité définis (longueur minimale, etc.).</p>
     */
    @JsonProperty("password")
    @NotBlank(message = "Le mot de passe ne peut pas être vide")
    @Size(min = 6, max = 100, message = "Le mot de passe doit contenir entre 6 et 100 caractères")
    private String password;
    
    /**
     * Indicateur pour se souvenir de l'utilisateur.
     * 
     * <p>Optionnel. Si défini à true, le token JWT aura une durée
     * de vie prolongée pour maintenir la session utilisateur.</p>
     */
    @JsonProperty("rememberMe")
    private boolean rememberMe = false;
    
    /**
     * Constructeur par défaut.
     * 
     * <p>Nécessaire pour la désérialisation JSON par les frameworks
     * comme Jackson ou Spring Boot.</p>
     */
    public LoginRequest() {
        // Constructeur par défaut requis pour la désérialisation JSON
    }
    
    /**
     * Constructeur avec paramètres essentiels.
     * 
     * @param email l'adresse email de l'utilisateur (ne doit pas être null ou vide)
     * @param password le mot de passe de l'utilisateur (ne doit pas être null ou vide)
     * @throws IllegalArgumentException si email ou password est null ou vide
     */
    public LoginRequest(String email, String password) {
        this.setEmail(email);
        this.setPassword(password);
    }
    
    /**
     * Constructeur complet.
     * 
     * @param email l'adresse email de l'utilisateur (ne doit pas être null ou vide)
     * @param password le mot de passe de l'utilisateur (ne doit pas être null ou vide)
     * @param rememberMe indicateur pour se souvenir de l'utilisateur
     * @throws IllegalArgumentException si email ou password est null ou vide
     */
    public LoginRequest(String email, String password, boolean rememberMe) {
        this(email, password);
        this.rememberMe = rememberMe;
    }
    
    /**
     * Récupère l'adresse email de l'utilisateur.
     * 
     * @return l'adresse email, jamais null après validation
     */
    public String getEmail() {
        return email;
    }
    
    /**
     * Définit l'adresse email de l'utilisateur.
     * 
     * @param email l'adresse email à définir (ne doit pas être null ou vide)
     * @throws IllegalArgumentException si l'email est null ou vide
     */
    public void setEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("L'email ne peut pas être null ou vide");
        }
        this.email = email.trim().toLowerCase();
    }
    
    /**
     * Récupère le mot de passe de l'utilisateur.
     * 
     * @return le mot de passe, jamais null après validation
     */
    public String getPassword() {
        return password;
    }
    
    /**
     * Définit le mot de passe de l'utilisateur.
     * 
     * @param password le mot de passe à définir (ne doit pas être null ou vide)
     * @throws IllegalArgumentException si le mot de passe est null ou vide
     */
    public void setPassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Le mot de passe ne peut pas être null ou vide");
        }
        this.password = password;
    }
    
    /**
     * Vérifie si l'option "se souvenir de moi" est activée.
     * 
     * @return true si l'utilisateur souhaite être mémorisé, false sinon
     */
    public boolean isRememberMe() {
        return rememberMe;
    }
    
    /**
     * Définit l'option "se souvenir de moi".
     * 
     * @param rememberMe true pour mémoriser l'utilisateur, false sinon
     */
    public void setRememberMe(boolean rememberMe) {
        this.rememberMe = rememberMe;
    }
    
    /**
     * Valide les données de la requête de connexion.
     * 
     * <p>Vérifie que tous les champs obligatoires sont présents
     * et respectent les contraintes définies.</p>
     * 
     * @return true si les données sont valides, false sinon
     */
    public boolean isValid() {
        return email != null && !email.trim().isEmpty() && 
               email.contains("@") &&
               password != null && !password.trim().isEmpty() &&
               password.length() >= 6;
    }
    
    /**
     * Nettoie les données sensibles de l'objet.
     * 
     * <p>Utilisé pour sécuriser l'objet après utilisation,
     * en particulier pour effacer le mot de passe de la mémoire.</p>
     */
    public void clearSensitiveData() {
        this.password = null;
    }
    
    /**
     * Compare cette requête de connexion avec une autre.
     * 
     * @param obj l'objet à comparer
     * @return true si les objets sont égaux, false sinon
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        LoginRequest that = (LoginRequest) obj;
        return rememberMe == that.rememberMe &&
               Objects.equals(email, that.email) &&
               Objects.equals(password, that.password);
    }
    
    /**
     * Génère le code de hachage pour cette requête de connexion.
     * 
     * @return le code de hachage
     */
    @Override
    public int hashCode() {
        return Objects.hash(email, password, rememberMe);
    }
    
    /**
     * Représentation textuelle de la requête de connexion.
     * 
     * <p>Note : Le mot de passe est masqué pour des raisons de sécurité.</p>
     * 
     * @return une chaîne représentant cet objet
     */
    @Override
    public String toString() {
        return "LoginRequest{" +
                "email='" + email + '\'' +
                ", password='***'" +
                ", rememberMe=" + rememberMe +
                '}';
    }
}