package com.javacraftacademy.userservice.model.dto.request;

import com.javacraftacademy.userservice.validation.ValidEmail;
import com.javacraftacademy.userservice.validation.ValidPassword;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Classe de requête pour la réinitialisation du mot de passe.
 * 
 * Cette classe peut être utilisée dans deux contextes :
 * 1. Demande de réinitialisation : seul l'email est requis
 * 2. Confirmation de réinitialisation : token, nouveau mot de passe et confirmation sont requis
 * 
 * Le processus de réinitialisation suit généralement ces étapes :
 * - L'utilisateur saisit son email
 * - Un token de réinitialisation est envoyé par email
 * - L'utilisateur utilise le token pour définir un nouveau mot de passe
 * 
 * @author JavaCraft Academy
 * @version 1.0
 * @since 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetRequest {
    
    /**
     * Adresse email de l'utilisateur demandant la réinitialisation.
     * 
     * L'email est utilisé pour identifier l'utilisateur et lui envoyer
     * le lien de réinitialisation. Il doit respecter le format d'email valide.
     * Ce champ est obligatoire pour initier le processus de réinitialisation.
     */
    @ValidEmail(message = "Le format de l'email n'est pas valide")
    private String email;
    
    /**
     * Token de réinitialisation du mot de passe.
     * 
     * Ce token sécurisé est généré par le système et envoyé à l'utilisateur
     * par email. Il permet de vérifier que la demande de réinitialisation
     * est légitime et n'a pas expiré. Ce champ est requis lors de la
     * confirmation de réinitialisation.
     */
    private String token;
    
    /**
     * Nouveau mot de passe choisi par l'utilisateur.
     * 
     * Le nouveau mot de passe doit respecter les critères de sécurité
     * définis par l'annotation @ValidPassword. Ce champ est requis
     * lors de la confirmation de réinitialisation.
     */
    @ValidPassword(message = "Le nouveau mot de passe ne respecte pas les critères de sécurité")
    private String newPassword;
    
    /**
     * Confirmation du nouveau mot de passe.
     * 
     * Ce champ doit être identique au nouveau mot de passe pour confirmer
     * que l'utilisateur a bien saisi le mot de passe souhaité.
     * La validation de correspondance est effectuée au niveau du service.
     * Ce champ est requis lors de la confirmation de réinitialisation.
     */
    private String confirmPassword;
    
    /**
     * Constructeur pour créer une demande de réinitialisation avec seulement l'email.
     * 
     * @param email L'adresse email de l'utilisateur
     */
    public PasswordResetRequest(String email) {
        this.email = email;
    }
    
    /**
     * Constructeur pour créer une confirmation de réinitialisation.
     * 
     * @param token Le token de réinitialisation
     * @param newPassword Le nouveau mot de passe
     * @param confirmPassword La confirmation du nouveau mot de passe
     */
    public PasswordResetRequest(String token, String newPassword, String confirmPassword) {
        this.token = token;
        this.newPassword = newPassword;
        this.confirmPassword = confirmPassword;
    }
    
    /**
     * Vérifie si cette requête est une demande de réinitialisation.
     * 
     * @return true si c'est une demande de réinitialisation (seul l'email est fourni)
     */
    public boolean isResetRequest() {
        return email != null && !email.trim().isEmpty() && token == null;
    }
    
    /**
     * Vérifie si cette requête est une confirmation de réinitialisation.
     * 
     * @return true si c'est une confirmation (token et nouveaux mots de passe fournis)
     */
    public boolean isResetConfirmation() {
        return token != null && !token.trim().isEmpty() && 
               newPassword != null && !newPassword.trim().isEmpty();
    }
}

