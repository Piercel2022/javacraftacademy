package com.javacraftacademy.userservice.model.dto.request;

import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * Classe de requête pour la mise à jour du profil utilisateur.
 * 
 * Cette classe encapsule les informations du profil qui peuvent être
 * modifiées par l'utilisateur. Contrairement aux données de base du compte
 * (comme l'email), ces informations sont plus flexibles et peuvent être
 * mises à jour librement par l'utilisateur.
 * 
 * Tous les champs sont optionnels, permettant des mises à jour partielles
 * du profil. Seuls les champs fournis seront mis à jour.
 * 
 * @author JavaCraft Academy
 * @version 1.0
 * @since 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfileUpdateRequest {
    
    /**
     * Prénom de l'utilisateur.
     * 
     * Si fourni, le prénom doit contenir entre 2 et 50 caractères
     * et ne peut contenir que des lettres, espaces, apostrophes et tirets.
     * Ce champ est optionnel lors de la mise à jour.
     */
    @Size(min = 2, max = 50, message = "Le prénom doit contenir entre 2 et 50 caractères")
    @Pattern(regexp = "^[a-zA-ZÀ-ÿ\\s'-]*$", message = "Le prénom ne peut contenir que des lettres, espaces, apostrophes et tirets")
    private String firstName;
    
    /**
     * Nom de famille de l'utilisateur.
     * 
     * Si fourni, le nom doit contenir entre 2 et 50 caractères
     * et ne peut contenir que des lettres, espaces, apostrophes et tirets.
     * Ce champ est optionnel lors de la mise à jour.
     */
    @Size(min = 2, max = 50, message = "Le nom doit contenir entre 2 et 50 caractères")
    @Pattern(regexp = "^[a-zA-ZÀ-ÿ\\s'-]*$", message = "Le nom ne peut contenir que des lettres, espaces, apostrophes et tirets")
    private String lastName;
    
    /**
     * Numéro de téléphone de l'utilisateur.
     * 
     * Si fourni, le numéro doit respecter un format international valide.
     * Ce champ peut être utilisé pour les notifications SMS ou la
     * vérification à deux facteurs. Il est optionnel.
     */
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Le format du numéro de téléphone n'est pas valide")
    private String phoneNumber;
    
    /**
     * Date de naissance de l'utilisateur.
     * 
     * Si fournie, la date doit être dans le passé (antérieure à aujourd'hui).
     * Cette information peut être utilisée pour calculer l'âge ou pour
     * des fonctionnalités liées à l'âge. Elle est optionnelle.
     */
    @Past(message = "La date de naissance doit être dans le passé")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;
    
    /**
     * Genre de l'utilisateur.
     * 
     * Ce champ optionnel permet à l'utilisateur de spécifier son genre.
     * Les valeurs acceptées sont généralement : HOMME, FEMME, AUTRE, PREFERE_NE_PAS_DIRE.
     * Si fourni, doit correspondre à l'une des valeurs de l'énumération Gender.
     */
    @Pattern(regexp = "^(HOMME|FEMME|AUTRE|PREFERE_NE_PAS_DIRE)$", 
             message = "Le genre doit être HOMME, FEMME, AUTRE ou PREFERE_NE_PAS_DIRE")
    private String gender;
    
    /**
     * Biographie ou description personnelle de l'utilisateur.
     * 
     * Ce champ de texte libre permet à l'utilisateur de se présenter.
     * La longueur est limitée à 500 caractères pour éviter les abus
     * et maintenir une bonne expérience utilisateur. Il est optionnel.
     */
    @Size(max = 500, message = "La biographie ne peut pas dépasser 500 caractères")
    private String bio;
    
    /**
     * Ville de résidence de l'utilisateur.
     * 
     * Ce champ optionnel permet de spécifier la ville où réside l'utilisateur.
     * Il peut être utilisé pour des fonctionnalités de géolocalisation
     * ou de recommandations basées sur la localisation.
     */
    @Size(max = 100, message = "La ville ne peut pas dépasser 100 caractères")
    @Pattern(regexp = "^[a-zA-ZÀ-ÿ\\s'-]*$", message = "La ville ne peut contenir que des lettres, espaces, apostrophes et tirets")
    private String city;
    
    /**
     * Pays de résidence de l'utilisateur.
     * 
     * Ce champ optionnel permet de spécifier le pays où réside l'utilisateur.
     * Il est recommandé d'utiliser les codes pays ISO 3166-1 alpha-2
     * (FR pour France, US pour États-Unis, etc.) pour la cohérence.
     */
    @Size(max = 100, message = "Le pays ne peut pas dépasser 100 caractères")
    @Pattern(regexp = "^[a-zA-ZÀ-ÿ\\s'-]*$", message = "Le pays ne peut contenir que des lettres, espaces, apostrophes et tirets")
    private String country;
    
    /**
     * URL de l'image de profil de l'utilisateur.
     * 
     * Si fournie, cette URL doit pointer vers une image valide.
     * L'image sera utilisée comme photo de profil de l'utilisateur.
     * Ce champ est optionnel.
     */
    @Pattern(regexp = "^(https?://).*\\.(jpg|jpeg|png|gif|bmp|webp)$", 
             message = "L'URL de l'image de profil doit être une URL valide pointant vers une image")
    private String profileImageUrl;
    
    /**
     * Préférences de notification de l'utilisateur.
     * 
     * Ce champ optionnel indique si l'utilisateur souhaite recevoir
     * des notifications par email. Il peut être modifié à tout moment
     * par l'utilisateur dans ses paramètres de profil.
     */
    private Boolean emailNotifications;
    
    /**
     * Préférences pour les notifications push.
     * 
     * Ce champ optionnel indique si l'utilisateur souhaite recevoir
     * des notifications push sur ses appareils mobiles.
     */
    private Boolean pushNotifications;
    
    /**
     * Préférences de confidentialité du profil.
     * 
     * Ce champ optionnel indique si le profil de l'utilisateur est public
     * ou privé. Un profil public peut être vu par d'autres utilisateurs,
     * tandis qu'un profil privé n'est visible que par l'utilisateur lui-même.
     */
    private Boolean isPublicProfile;
}
