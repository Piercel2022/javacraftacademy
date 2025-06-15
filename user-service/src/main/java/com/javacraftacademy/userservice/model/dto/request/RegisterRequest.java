package com.javacraftacademy.userservice.model.dto.request;

import com.javacraftacademy.userservice.validation.ValidEmail;
import com.javacraftacademy.userservice.validation.ValidPassword;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Classe de requête pour l'inscription d'un nouvel utilisateur.
 * 
 * Cette classe encapsule toutes les données nécessaires pour créer un nouveau
 * compte utilisateur dans l'application. Elle comprend les informations personnelles
 * de base ainsi que les identifiants de connexion avec leurs validations respectives.
 * 
 * @author JavaCraft Academy
 * @version 1.0
 * @since 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    
    /**
     * Prénom de l'utilisateur.
     * 
     * Le prénom est obligatoire et doit contenir entre 2 et 50 caractères.
     * Il peut contenir des lettres, espaces, apostrophes et tirets.
     */
    @NotBlank(message = "Le prénom est obligatoire")
    @Size(min = 2, max = 50, message = "Le prénom doit contenir entre 2 et 50 caractères")
    @Pattern(regexp = "^[a-zA-ZÀ-ÿ\\s'-]+$", message = "Le prénom ne peut contenir que des lettres, espaces, apostrophes et tirets")
    private String firstName;
    
    /**
     * Nom de famille de l'utilisateur.
     * 
     * Le nom est obligatoire et doit contenir entre 2 et 50 caractères.
     * Il peut contenir des lettres, espaces, apostrophes et tirets.
     */
    @NotBlank(message = "Le nom est obligatoire")
    @Size(min = 2, max = 50, message = "Le nom doit contenir entre 2 et 50 caractères")
    @Pattern(regexp = "^[a-zA-ZÀ-ÿ\\s'-]+$", message = "Le nom ne peut contenir que des lettres, espaces, apostrophes et tirets")
    private String lastName;
    
    /**
     * Adresse email de l'utilisateur.
     * 
     * L'email sert d'identifiant unique et doit respecter le format d'email valide.
     * Il sera utilisé pour la connexion et les communications avec l'utilisateur.
     */
    @NotBlank(message = "L'email est obligatoire")
    @ValidEmail(message = "Le format de l'email n'est pas valide")
    private String email;
    
    /**
     * Mot de passe choisi par l'utilisateur.
     * 
     * Le mot de passe doit respecter les critères de sécurité définis
     * par l'annotation @ValidPassword (longueur, complexité, etc.).
     */
    @NotBlank(message = "Le mot de passe est obligatoire")
    @ValidPassword(message = "Le mot de passe ne respecte pas les critères de sécurité")
    private String password;
    
    /**
     * Confirmation du mot de passe.
     * 
     * Ce champ doit être identique au mot de passe pour confirmer que
     * l'utilisateur a bien saisi le mot de passe souhaité.
     * La validation de correspondance est généralement effectuée au niveau du service.
     */
    @NotBlank(message = "La confirmation du mot de passe est obligatoire")
    private String confirmPassword;
    
    /**
     * Numéro de téléphone de l'utilisateur (optionnel).
     * 
     * Si fourni, le numéro doit respecter un format international valide.
     * Ce champ peut être utilisé pour la vérification à deux facteurs
     * ou les notifications SMS.
     */
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Le format du numéro de téléphone n'est pas valide")
    private String phoneNumber;
    
    /**
     * Acceptation des conditions d'utilisation.
     * 
     * L'utilisateur doit obligatoirement accepter les conditions d'utilisation
     * pour pouvoir créer un compte. Ce champ doit être à true.
     */
    @NotBlank(message = "L'acceptation des conditions d'utilisation est obligatoire")
    private boolean acceptTerms;
    
    /**
     * Consentement pour recevoir des emails promotionnels.
     * 
     * Ce champ optionnel indique si l'utilisateur souhaite recevoir
     * des newsletters et autres communications marketing.
     * Par défaut, cette option est désactivée (false).
     */
    private boolean acceptMarketing = false;
}

