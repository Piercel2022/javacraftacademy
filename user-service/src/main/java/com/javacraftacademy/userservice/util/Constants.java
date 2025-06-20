
package com.javacraftacademy.userservice.util;

/**
 * Classe utilitaire contenant toutes les constantes utilisées dans l'application User Service.
 * 
 * <p>Cette classe centralise toutes les constantes pour maintenir la cohérence
 * et faciliter la maintenance de l'application. Elle inclut les constantes pour
 * la sécurité, les messages d'erreur, les formats de date, les validations,
 * et les configurations diverses.</p>
 * 
 * <p><b>Fonctionnalités principales :</b></p>
 * <ul>
 *   <li>Constantes de sécurité et JWT</li>
 *   <li>Messages d'erreur standardisés</li>
 *   <li>Formats de date et time</li>
 *   <li>Paramètres de validation</li>
 *   <li>Codes de statut et réponses API</li>
 * </ul>
 * 
 * @author JavaCraft Academy
 * @version 1.0
 * @since 1.0
 */
public final class Constants {

    // ===============================
    // CONSTANTES DE SÉCURITÉ ET JWT
    // ===============================
    
    /**
     * Durée de vie par défaut du token JWT en millisecondes (24 heures).
     */
    public static final long JWT_EXPIRATION_TIME = 86400000L; // 24 heures
    
    /**
     * Durée de vie du refresh token en millisecondes (7 jours).
     */
    public static final long REFRESH_TOKEN_EXPIRATION_TIME = 604800000L; // 7 jours
    
    /**
     * Préfixe pour les tokens Bearer dans les headers d'autorisation.
     */
    public static final String TOKEN_PREFIX = "Bearer ";
    
    /**
     * Nom du header d'autorisation.
     */
    public static final String HEADER_STRING = "Authorization";
    
    /**
     * Algorithme de hachage utilisé pour les mots de passe.
     */
    public static final String PASSWORD_ENCODER_STRENGTH = "12";
    
    /**
     * Longueur minimale requise pour un mot de passe.
     */
    public static final int MIN_PASSWORD_LENGTH = 8;
    
    /**
     * Longueur maximale autorisée pour un mot de passe.
     */
    public static final int MAX_PASSWORD_LENGTH = 100;

    // ===============================
    // CONSTANTES DE VALIDATION
    // ===============================
    
    /**
     * Expression régulière pour valider les adresses email.
     */
    public static final String EMAIL_REGEX = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
    
    /**
     * Expression régulière pour valider les mots de passe forts.
     * Doit contenir au moins : 1 majuscule, 1 minuscule, 1 chiffre, 1 caractère spécial.
     */
    public static final String PASSWORD_REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]";
    
    /**
     * Longueur minimale pour le nom d'utilisateur.
     */
    public static final int MIN_USERNAME_LENGTH = 3;
    
    /**
     * Longueur maximale pour le nom d'utilisateur.
     */
    public static final int MAX_USERNAME_LENGTH = 50;
    
    /**
     * Longueur minimale pour le prénom et nom.
     */
    public static final int MIN_NAME_LENGTH = 2;
    
    /**
     * Longueur maximale pour le prénom et nom.
     */
    public static final int MAX_NAME_LENGTH = 50;

    // ===============================
    // MESSAGES D'ERREUR
    // ===============================
    
    /**
     * Message d'erreur pour utilisateur non trouvé.
     */
    public static final String USER_NOT_FOUND = "Utilisateur non trouvé";
    
    /**
     * Message d'erreur pour email déjà existant.
     */
    public static final String EMAIL_ALREADY_EXISTS = "Cette adresse email est déjà utilisée";
    
    /**
     * Message d'erreur pour nom d'utilisateur déjà existant.
     */
    public static final String USERNAME_ALREADY_EXISTS = "Ce nom d'utilisateur est déjà pris";
    
    /**
     * Message d'erreur pour identifiants invalides.
     */
    public static final String INVALID_CREDENTIALS = "Email ou mot de passe incorrect";
    
    /**
     * Message d'erreur pour token expiré.
     */
    public static final String TOKEN_EXPIRED = "Token expiré";
    
    /**
     * Message d'erreur pour token invalide.
     */
    public static final String INVALID_TOKEN = "Token invalide";
    
    /**
     * Message d'erreur pour accès non autorisé.
     */
    public static final String ACCESS_DENIED = "Accès refusé";
    
    /**
     * Message d'erreur pour email invalide.
     */
    public static final String INVALID_EMAIL = "Format d'email invalide";
    
    /**
     * Message d'erreur pour mot de passe faible.
     */
    public static final String WEAK_PASSWORD = "Le mot de passe doit contenir au moins 8 caractères, une majuscule, une minuscule, un chiffre et un caractère spécial";

    // ===============================
    // MESSAGES DE SUCCÈS
    // ===============================
    
    /**
     * Message de succès pour inscription.
     */
    public static final String REGISTRATION_SUCCESS = "Inscription réussie";
    
    /**
     * Message de succès pour connexion.
     */
    public static final String LOGIN_SUCCESS = "Connexion réussie";
    
    /**
     * Message de succès pour mise à jour de profil.
     */
    public static final String PROFILE_UPDATED = "Profil mis à jour avec succès";
    
    /**
     * Message de succès pour réinitialisation de mot de passe.
     */
    public static final String PASSWORD_RESET_SUCCESS = "Mot de passe réinitialisé avec succès";

    // ===============================
    // FORMATS DE DATE ET HEURE
    // ===============================
    
    /**
     * Format de date par défaut (yyyy-MM-dd).
     */
    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    
    /**
     * Format de date et heure complet (yyyy-MM-dd HH:mm:ss).
     */
    public static final String DEFAULT_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    
    /**
     * Format de date pour les APIs (ISO 8601).
     */
    public static final String API_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    
    /**
     * Fuseau horaire par défaut.
     */
    public static final String DEFAULT_TIMEZONE = "UTC";

    // ===============================
    // RÔLES ET PERMISSIONS
    // ===============================
    
    /**
     * Rôle utilisateur standard.
     */
    public static final String ROLE_USER = "ROLE_USER";
    
    /**
     * Rôle administrateur.
     */
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    
    /**
     * Rôle modérateur.
     */
    public static final String ROLE_MODERATOR = "ROLE_MODERATOR";
    
    /**
     * Rôle instructeur.
     */
    public static final String ROLE_INSTRUCTOR = "ROLE_INSTRUCTOR";

    // ===============================
    // CODES DE STATUT HTTP
    // ===============================
    
    /**
     * Code de statut HTTP pour succès (200).
     */
    public static final int HTTP_OK = 200;
    
    /**
     * Code de statut HTTP pour création réussie (201).
     */
    public static final int HTTP_CREATED = 201;
    
    /**
     * Code de statut HTTP pour requête incorrecte (400).
     */
    public static final int HTTP_BAD_REQUEST = 400;
    
    /**
     * Code de statut HTTP pour non autorisé (401).
     */
    public static final int HTTP_UNAUTHORIZED = 401;
    
    /**
     * Code de statut HTTP pour interdit (403).
     */
    public static final int HTTP_FORBIDDEN = 403;
    
    /**
     * Code de statut HTTP pour non trouvé (404).
     */
    public static final int HTTP_NOT_FOUND = 404;
    
    /**
     * Code de statut HTTP pour conflit (409).
     */
    public static final int HTTP_CONFLICT = 409;
    
    /**
     * Code de statut HTTP pour erreur serveur (500).
     */
    public static final int HTTP_INTERNAL_ERROR = 500;

    // ===============================
    // CONFIGURATION EMAIL
    // ===============================
    
    /**
     * Sujet d'email de bienvenue.
     */
    public static final String WELCOME_EMAIL_SUBJECT = "Bienvenue sur JavaCraft Academy";
    
    /**
     * Sujet d'email de réinitialisation de mot de passe.
     */
    public static final String PASSWORD_RESET_EMAIL_SUBJECT = "Réinitialisation de votre mot de passe";
    
    /**
     * Adresse email d'expédition par défaut.
     */
    public static final String DEFAULT_FROM_EMAIL = "noreply@javacraftacademy.com";

    // ===============================
    // PAGINATION
    // ===============================
    
    /**
     * Taille de page par défaut pour la pagination.
     */
    public static final int DEFAULT_PAGE_SIZE = 20;
    
    /**
     * Taille maximale de page autorisée.
     */
    public static final int MAX_PAGE_SIZE = 100;
    
    /**
     * Numéro de page par défaut.
     */
    public static final int DEFAULT_PAGE_NUMBER = 0;

    // ===============================
    // CONSTRUCTEUR PRIVÉ
    // ===============================
    
    /**
     * Constructeur privé pour empêcher l'instanciation de cette classe utilitaire.
     * 
     * @throws UnsupportedOperationException si une tentative d'instanciation est faite
     */
    private Constants() {
        throw new UnsupportedOperationException("Cette classe utilitaire ne peut pas être instanciée");
    }
}