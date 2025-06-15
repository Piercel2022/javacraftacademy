package com.javacraftacademy.userservice.model.dto.request;

import com.javacraftacademy.userservice.validation.ValidEmail;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Classe de requête pour l'authentification des utilisateurs.
 * 
 * Cette classe encapsule les données nécessaires pour qu'un utilisateur
 * puisse se connecter à l'application. Elle contient l'email et le mot de passe
 * avec leurs validations respectives.
 * 
 * @author JavaCraft Academy
 * @version 1.0
 * @since 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    
    /**
     * Adresse email de l'utilisateur.
     * 
     * L'email doit respecter le format d'email valide et ne peut pas être vide.
     * Il sert d'identifiant unique pour l'utilisateur lors de la connexion.
     */
    @NotBlank(message = "L'email est obligatoire")
    @ValidEmail(message = "Le format de l'email n'est pas valide")
    private String email;
    
    /**
     * Mot de passe de l'utilisateur.
     * 
     * Le mot de passe ne peut pas être vide et doit contenir au minimum 6 caractères
     * pour des raisons de sécurité de base.
     */
    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 6, message = "Le mot de passe doit contenir au moins 6 caractères")
    private String password;
    
    /**
     * Indicateur pour se souvenir de l'utilisateur.
     * 
     * Si cette option est activée, le token de rafraîchissement aura une durée
     * de vie plus longue, permettant à l'utilisateur de rester connecté plus longtemps.
     * Par défaut, cette option est désactivée (false).
     */
    private boolean rememberMe = false;
}
