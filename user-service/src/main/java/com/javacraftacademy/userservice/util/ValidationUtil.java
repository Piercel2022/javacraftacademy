
package com.javacraftacademy.userservice.util;

import java.util.Collection;
import java.util.regex.Pattern;

/**
 * Classe utilitaire pour la validation de données et paramètres.
 * 
 * <p>Cette classe fournit des méthodes de validation communes utilisées dans
 * l'application User Service. Elle permet de valider les formats, les tailles,
 * les valeurs nulles, et d'autres contraintes de données de manière centralisée.</p>
 * 
 * <p><b>Fonctionnalités principales :</b></p>
 * <ul>
 *   <li>Validation des emails et mots de passe</li>
 *   <li>Validation des chaînes de caractères (longueur, format)</li>
 *   <li>Validation des collections et objets</li>
 *   <li>Validation des valeurs numériques</li>
 *   <li>Validation personnalisée avec expressions régulières</li>
 *   <li>Messages d'erreur standardisés</li>
 * </ul>
 * 
 * @author JavaCraft Academy
 * @version 1.0
 * @since 1.0
 */
public final class ValidationUtil {

    // Expressions régulières pré-compilées pour de meilleures performances
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );
    
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^\\+?[1-9]\\d{1,14}$"
    );
    
    private static final Pattern ALPHANUMERIC_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9]+$"
    );
    
    private static final Pattern ALPHA_PATTERN = Pattern.compile(
        "^[a-zA-Z]+$"
    );
    
    private static final Pattern NUMERIC_PATTERN = Pattern.compile(
        "^\\d+$"
    );

    // Constantes pour la validation des mots de passe
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int MAX_PASSWORD_LENGTH = 128;
    private static final Pattern PASSWORD_UPPERCASE = Pattern.compile(".*[A-Z].*");
    private static final Pattern PASSWORD_LOWERCASE = Pattern.compile(".*[a-z].*");
    private static final Pattern PASSWORD_DIGIT = Pattern.compile(".*\\d.*");
    private static final Pattern PASSWORD_SPECIAL = Pattern.compile(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*");

    /**
     * Constructeur privé pour empêcher l'instanciation.
     */
    private ValidationUtil() {
        throw new UnsupportedOperationException("Classe utilitaire - ne peut pas être instanciée");
    }

    // ========== VALIDATION DES CHAÎNES DE CARACTÈRES ==========

    /**
     * Vérifie si une chaîne est null ou vide.
     *
     * @param str la chaîne à vérifier
     * @return true si la chaîne est null ou vide, false sinon
     */
    public static boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * Vérifie si une chaîne n'est ni null ni vide.
     *
     * @param str la chaîne à vérifier
     * @return true si la chaîne n'est ni null ni vide, false sinon
     */
    public static boolean isNotNullOrEmpty(String str) {
        return !isNullOrEmpty(str);
    }

    /**
     * Valide la longueur d'une chaîne.
     *
     * @param str la chaîne à valider
     * @param minLength longueur minimale
     * @param maxLength longueur maximale
     * @return true si la longueur est valide, false sinon
     */
    public static boolean isValidLength(String str, int minLength, int maxLength) {
        if (str == null) return false;
        int length = str.trim().length();
        return length >= minLength && length <= maxLength;
    }

    /**
     * Valide qu'une chaîne respecte une longueur minimale.
     *
     * @param str la chaîne à valider
     * @param minLength longueur minimale
     * @return true si la longueur est valide, false sinon
     */
    public static boolean hasMinLength(String str, int minLength) {
        return str != null && str.trim().length() >= minLength;
    }

    /**
     * Valide qu'une chaîne ne dépasse pas une longueur maximale.
     *
     * @param str la chaîne à valider
     * @param maxLength longueur maximale
     * @return true si la longueur est valide, false sinon
     */
    public static boolean hasMaxLength(String str, int maxLength) {
        return str != null && str.trim().length() <= maxLength;
    }

    // ========== VALIDATION DES EMAILS ==========

    /**
     * Valide le format d'une adresse email.
     *
     * @param email l'adresse email à valider
     * @return true si l'email est valide, false sinon
     */
    public static boolean isValidEmail(String email) {
        return isNotNullOrEmpty(email) && EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    // ========== VALIDATION DES MOTS DE PASSE ==========

    /**
     * Valide un mot de passe selon les critères de sécurité.
     * Le mot de passe doit contenir :
     * - Au moins 8 caractères
     * - Au moins une majuscule
     * - Au moins une minuscule
     * - Au moins un chiffre
     * - Au moins un caractère spécial
     *
     * @param password le mot de passe à valider
     * @return true si le mot de passe est valide, false sinon
     */
    public static boolean isValidPassword(String password) {
        if (isNullOrEmpty(password)) {
            return false;
        }
        
        return isValidLength(password, MIN_PASSWORD_LENGTH, MAX_PASSWORD_LENGTH) &&
               PASSWORD_UPPERCASE.matcher(password).matches() &&
               PASSWORD_LOWERCASE.matcher(password).matches() &&
               PASSWORD_DIGIT.matcher(password).matches() &&
               PASSWORD_SPECIAL.matcher(password).matches();
    }

    /**
     * Valide un mot de passe basique (longueur minimale uniquement).
     *
     * @param password le mot de passe à valider
     * @param minLength longueur minimale requise
     * @return true si le mot de passe est valide, false sinon
     */
    public static boolean isValidBasicPassword(String password, int minLength) {
        return hasMinLength(password, minLength);
    }

    // ========== VALIDATION DES FORMATS ==========

    /**
     * Valide un numéro de téléphone.
     *
     * @param phone le numéro de téléphone à valider
     * @return true si le numéro est valide, false sinon
     */
    public static boolean isValidPhone(String phone) {
        return isNotNullOrEmpty(phone) && PHONE_PATTERN.matcher(phone.trim()).matches();
    }

    /**
     * Valide qu'une chaîne ne contient que des caractères alphanumériques.
     *
     * @param str la chaîne à valider
     * @return true si la chaîne est alphanumérique, false sinon
     */
    public static boolean isAlphanumeric(String str) {
        return isNotNullOrEmpty(str) && ALPHANUMERIC_PATTERN.matcher(str).matches();
    }

    /**
     * Valide qu'une chaîne ne contient que des lettres.
     *
     * @param str la chaîne à valider
     * @return true si la chaîne ne contient que des lettres, false sinon
     */
    public static boolean isAlpha(String str) {
        return isNotNullOrEmpty(str) && ALPHA_PATTERN.matcher(str).matches();
    }

    /**
     * Valide qu'une chaîne ne contient que des chiffres.
     *
     * @param str la chaîne à valider
     * @return true si la chaîne ne contient que des chiffres, false sinon
     */
    public static boolean isNumeric(String str) {
        return isNotNullOrEmpty(str) && NUMERIC_PATTERN.matcher(str).matches();
    }

    /**
     * Valide une chaîne avec une expression régulière personnalisée.
     *
     * @param str la chaîne à valider
     * @param pattern le pattern regex à utiliser
     * @return true si la chaîne correspond au pattern, false sinon
     */
    public static boolean matchesPattern(String str, Pattern pattern) {
        return isNotNullOrEmpty(str) && pattern.matcher(str).matches();
    }

    /**
     * Valide une chaîne avec une expression régulière personnalisée.
     *
     * @param str la chaîne à valider
     * @param regex l'expression régulière à utiliser
     * @return true si la chaîne correspond au regex, false sinon
     */
    public static boolean matchesRegex(String str, String regex) {
        return isNotNullOrEmpty(str) && str.matches(regex);
    }

    // ========== VALIDATION DES COLLECTIONS ==========

    /**
     * Vérifie si une collection est null ou vide.
     *
     * @param collection la collection à vérifier
     * @return true si la collection est null ou vide, false sinon
     */
    public static boolean isNullOrEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    /**
     * Vérifie si une collection n'est ni null ni vide.
     *
     * @param collection la collection à vérifier
     * @return true si la collection n'est ni null ni vide, false sinon
     */
    public static boolean isNotNullOrEmpty(Collection<?> collection) {
        return !isNullOrEmpty(collection);
    }

    /**
     * Valide la taille d'une collection.
     *
     * @param collection la collection à valider
     * @param minSize taille minimale
     * @param maxSize taille maximale
     * @return true si la taille est valide, false sinon
     */
    public static boolean isValidSize(Collection<?> collection, int minSize, int maxSize) {
        if (collection == null) return false;
        int size = collection.size();
        return size >= minSize && size <= maxSize;
    }

    // ========== VALIDATION DES OBJETS ==========

    /**
     * Vérifie si un objet est null.
     *
     * @param obj l'objet à vérifier
     * @return true si l'objet est null, false sinon
     */
    public static boolean isNull(Object obj) {
        return obj == null;
    }

    /**
     * Vérifie si un objet n'est pas null.
     *
     * @param obj l'objet à vérifier
     * @return true si l'objet n'est pas null, false sinon
     */
    public static boolean isNotNull(Object obj) {
        return obj != null;
    }

    // ========== VALIDATION DES VALEURS NUMÉRIQUES ==========

    /**
     * Valide qu'un nombre est dans une plage donnée.
     *
     * @param value la valeur à valider
     * @param min valeur minimale (inclusive)
     * @param max valeur maximale (inclusive)
     * @return true si la valeur est dans la plage, false sinon
     */
    public static boolean isInRange(int value, int min, int max) {
        return value >= min && value <= max;
    }

    /**
     * Valide qu'un nombre est dans une plage donnée.
     *
     * @param value la valeur à valider
     * @param min valeur minimale (inclusive)
     * @param max valeur maximale (inclusive)
     * @return true si la valeur est dans la plage, false sinon
     */
    public static boolean isInRange(long value, long min, long max) {
        return value >= min && value <= max;
    }

    /**
     * Valide qu'un nombre est dans une plage donnée.
     *
     * @param value la valeur à valider
     * @param min valeur minimale (inclusive)
     * @param max valeur maximale (inclusive)
     * @return true si la valeur est dans la plage, false sinon
     */
    public static boolean isInRange(double value, double min, double max) {
        return value >= min && value <= max;
    }

    /**
     * Valide qu'un nombre est positif.
     *
     * @param value la valeur à valider
     * @return true si la valeur est positive, false sinon
     */
    public static boolean isPositive(int value) {
        return value > 0;
    }

    /**
     * Valide qu'un nombre est non négatif.
     *
     * @param value la valeur à valider
     * @return true si la valeur est non négative, false sinon
     */
    public static boolean isNonNegative(int value) {
        return value >= 0;
    }

    // ========== MÉTHODES DE VALIDATION AVEC EXCEPTIONS ==========

    /**
     * Valide qu'une chaîne n'est pas null ou vide, sinon lève une exception.
     *
     * @param str la chaîne à valider
     * @param fieldName le nom du champ pour le message d'erreur
     * @throws IllegalArgumentException si la chaîne est null ou vide
     */
    public static void requireNonEmpty(String str, String fieldName) {
        if (isNullOrEmpty(str)) {
            throw new IllegalArgumentException(fieldName + " ne peut pas être null ou vide");
        }
    }

    /**
     * Valide qu'un objet n'est pas null, sinon lève une exception.
     *
     * @param obj l'objet à valider
     * @param fieldName le nom du champ pour le message d'erreur
     * @throws IllegalArgumentException si l'objet est null
     */
    public static void requireNonNull(Object obj, String fieldName) {
        if (obj == null) {
            throw new IllegalArgumentException(fieldName + " ne peut pas être null");
        }
    }

    /**
     * Valide qu'un email est valide, sinon lève une exception.
     *
     * @param email l'email à valider
     * @throws IllegalArgumentException si l'email est invalide
     */
    public static void requireValidEmail(String email) {
        if (!isValidEmail(email)) {
            throw new IllegalArgumentException("Format d'email invalide: " + email);
        }
    }

    /**
     * Valide qu'un mot de passe est valide, sinon lève une exception.
     *
     * @param password le mot de passe à valider
     * @throws IllegalArgumentException si le mot de passe est invalide
     */
    public static void requireValidPassword(String password) {
        if (!isValidPassword(password)) {
            throw new IllegalArgumentException(
                "Le mot de passe doit contenir au moins " + MIN_PASSWORD_LENGTH + 
                " caractères, incluant une majuscule, une minuscule, un chiffre et un caractère spécial"
            );
        }
    }

    /**
     * Valide qu'une chaîne respecte une longueur donnée, sinon lève une exception.
     *
     * @param str la chaîne à valider
     * @param minLength longueur minimale
     * @param maxLength longueur maximale
     * @param fieldName le nom du champ pour le message d'erreur
     * @throws IllegalArgumentException si la longueur est invalide
     */
    public static void requireValidLength(String str, int minLength, int maxLength, String fieldName) {
        if (!isValidLength(str, minLength, maxLength)) {
            throw new IllegalArgumentException(
                fieldName + " doit contenir entre " + minLength + " et " + maxLength + " caractères"
            );
        }
    }

    // ========== MÉTHODES UTILITAIRES ==========

    /**
     * Nettoie et normalise une chaîne de caractères.
     *
     * @param str la chaîne à nettoyer
     * @return la chaîne nettoyée ou null si l'entrée était null
     */
    public static String sanitize(String str) {
        return str == null ? null : str.trim();
    }

    /**
     * Obtient la longueur sécurisée d'une chaîne (0 si null).
     *
     * @param str la chaîne
     * @return la longueur de la chaîne ou 0 si null
     */
    public static int safeLength(String str) {
        return str == null ? 0 : str.length();
    }

    /**
     * Obtient la taille sécurisée d'une collection (0 si null).
     *
     * @param collection la collection
     * @return la taille de la collection ou 0 si null
     */
    public static int safeSize(Collection<?> collection) {
        return collection == null ? 0 : collection.size();
    }
}