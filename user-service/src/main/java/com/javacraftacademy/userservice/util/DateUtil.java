package com.javacraftacademy.userservice.util;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * Classe utilitaire pour la gestion et manipulation des dates et heures.
 * 
 * <p>Cette classe fournit des méthodes utilitaires pour travailler avec les dates,
 * les heures, et les fuseaux horaires. Elle utilise l'API Java 8 Time pour une
 * meilleure gestion des dates et une sécurité thread-safe.</p>
 * 
 * <p><b>Fonctionnalités principales :</b></p>
 * <ul>
 *   <li>Conversion entre différents formats de date</li>
 *   <li>Calculs de différences entre dates</li>
 *   <li>Gestion des fuseaux horaires</li>
 *   <li>Validation de dates</li>
 *   <li>Formatage et parsing de dates</li>
 *   <li>Opérations arithmétiques sur les dates</li>
 * </ul>
 * 
 * @author JavaCraft Academy
 * @version 1.0
 * @since 1.0
 */
public final class DateUtil {

    // ===============================
    // FORMATTERS PRÉDÉFINIS
    // ===============================
    
    /**
     * Formatter pour le format de date standard (yyyy-MM-dd).
     */
    private static final DateTimeFormatter DATE_FORMATTER = 
        DateTimeFormatter.ofPattern(Constants.DEFAULT_DATE_FORMAT);
    
    /**
     * Formatter pour le format de date et heure complet (yyyy-MM-dd HH:mm:ss).
     */
    private static final DateTimeFormatter DATETIME_FORMATTER = 
        DateTimeFormatter.ofPattern(Constants.DEFAULT_DATETIME_FORMAT);
    
    /**
     * Formatter pour le format API ISO 8601 (yyyy-MM-ddTHH:mm:ss.SSSZ).
     */
    private static final DateTimeFormatter API_FORMATTER = 
        DateTimeFormatter.ofPattern(Constants.API_DATE_FORMAT);
    
    /**
     * Zone par défaut (UTC).
     */
    private static final ZoneId DEFAULT_ZONE = ZoneId.of(Constants.DEFAULT_TIMEZONE);

    // ===============================
    // MÉTHODES DE CRÉATION ET OBTENTION
    // ===============================
    
    /**
     * Obtient la date et heure actuelles en UTC.
     * 
     * @return LocalDateTime représentant maintenant en UTC
     */
    public static LocalDateTime now() {
        return LocalDateTime.now(DEFAULT_ZONE);
    }
    
    /**
     * Obtient la date actuelle en UTC.
     * 
     * @return LocalDate représentant aujourd'hui en UTC
     */
    public static LocalDate today() {
        return LocalDate.now(DEFAULT_ZONE);
    }
    
    /**
     * Obtient l'instant actuel.
     * 
     * @return Instant représentant maintenant
     */
    public static Instant nowInstant() {
        return Instant.now();
    }
    
    /**
     * Crée un LocalDateTime à partir d'un timestamp Unix.
     * 
     * @param timestamp timestamp Unix en millisecondes
     * @return LocalDateTime correspondant au timestamp
     */
    public static LocalDateTime fromTimestamp(long timestamp) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), DEFAULT_ZONE);
    }
    
    /**
     * Convertit un LocalDateTime en timestamp Unix.
     * 
     * @param dateTime LocalDateTime à convertir
     * @return timestamp Unix en millisecondes
     */
    public static long toTimestamp(LocalDateTime dateTime) {
        return dateTime.atZone(DEFAULT_ZONE).toInstant().toEpochMilli();
    }

    // ===============================
    // MÉTHODES DE FORMATAGE
    // ===============================
    
    /**
     * Formate une date au format standard (yyyy-MM-dd).
     * 
     * @param date LocalDate à formater
     * @return String représentant la date formatée
     * @throws IllegalArgumentException si la date est null
     */
    public static String formatDate(LocalDate date) {
        validateNotNull(date, "La date ne peut pas être null");
        return date.format(DATE_FORMATTER);
    }
    
    /**
     * Formate une date et heure au format standard (yyyy-MM-dd HH:mm:ss).
     * 
     * @param dateTime LocalDateTime à formater
     * @return String représentant la date et heure formatées
     * @throws IllegalArgumentException si dateTime est null
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        validateNotNull(dateTime, "La date et heure ne peuvent pas être null");
        return dateTime.format(DATETIME_FORMATTER);
    }
    
    /**
     * Formate une date et heure au format API ISO 8601.
     * 
     * @param dateTime LocalDateTime à formater
     * @return String représentant la date et heure formatées pour l'API
     * @throws IllegalArgumentException si dateTime est null
     */
    public static String formatForApi(LocalDateTime dateTime) {
        validateNotNull(dateTime, "La date et heure ne peuvent pas être null");
        return dateTime.atZone(DEFAULT_ZONE).format(API_FORMATTER);
    }
    
    /**
     * Formate une date avec un pattern personnalisé.
     * 
     * @param date LocalDate à formater
     * @param pattern pattern de formatage
     * @return String représentant la date formatée
     * @throws IllegalArgumentException si date ou pattern est null
     */
    public static String formatWithPattern(LocalDate date, String pattern) {
        validateNotNull(date, "La date ne peut pas être null");
        validateNotNull(pattern, "Le pattern ne peut pas être null");
        return date.format(DateTimeFormatter.ofPattern(pattern));
    }

    // ===============================
    // MÉTHODES DE PARSING
    // ===============================
    
    /**
     * Parse une chaîne de date au format standard (yyyy-MM-dd).
     * 
     * @param dateString chaîne représentant la date
     * @return LocalDate parsée
     * @throws IllegalArgumentException si dateString est null ou invalide
     */
    public static LocalDate parseDate(String dateString) {
        validateNotNull(dateString, "La chaîne de date ne peut pas être null");
        try {
            return LocalDate.parse(dateString, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Format de date invalide: " + dateString, e);
        }
    }
    
    /**
     * Parse une chaîne de date et heure au format standard (yyyy-MM-dd HH:mm:ss).
     * 
     * @param dateTimeString chaîne représentant la date et heure
     * @return LocalDateTime parsée
     * @throws IllegalArgumentException si dateTimeString est null ou invalide
     */
    public static LocalDateTime parseDateTime(String dateTimeString) {
        validateNotNull(dateTimeString, "La chaîne de date et heure ne peut pas être null");
        try {
            return LocalDateTime.parse(dateTimeString, DATETIME_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Format de date et heure invalide: " + dateTimeString, e);
        }
    }
    
    /**
     * Parse une chaîne de date avec un pattern personnalisé.
     * 
     * @param dateString chaîne à parser
     * @param pattern pattern de parsing
     * @return LocalDate parsée
     * @throws IllegalArgumentException si dateString, pattern est null ou invalide
     */
    public static LocalDate parseDateWithPattern(String dateString, String pattern) {
        validateNotNull(dateString, "La chaîne de date ne peut pas être null");
        validateNotNull(pattern, "Le pattern ne peut pas être null");
        try {
            return LocalDate.parse(dateString, DateTimeFormatter.ofPattern(pattern));
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Format de date invalide: " + dateString, e);
        }
    }

    // ===============================
    // MÉTHODES DE CALCUL
    // ===============================
    
    /**
     * Calcule la différence en jours entre deux dates.
     * 
     * @param startDate date de début
     * @param endDate date de fin
     * @return nombre de jours entre les deux dates (positif si endDate > startDate)
     * @throws IllegalArgumentException si une des dates est null
     */
    public static long daysBetween(LocalDate startDate, LocalDate endDate) {
        validateNotNull(startDate, "La date de début ne peut pas être null");
        validateNotNull(endDate, "La date de fin ne peut pas être null");
        return ChronoUnit.DAYS.between(startDate, endDate);
    }
    
    /**
     * Calcule la différence en heures entre deux dates et heures.
     * 
     * @param startDateTime date et heure de début
     * @param endDateTime date et heure de fin
     * @return nombre d'heures entre les deux dates et heures
     * @throws IllegalArgumentException si une des dates est null
     */
    public static long hoursBetween(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        validateNotNull(startDateTime, "La date et heure de début ne peuvent pas être null");
        validateNotNull(endDateTime, "La date et heure de fin ne peuvent pas être null");
        return ChronoUnit.HOURS.between(startDateTime, endDateTime);
    }
    
    /**
     * Calcule la différence en minutes entre deux dates et heures.
     * 
     * @param startDateTime date et heure de début
     * @param endDateTime date et heure de fin
     * @return nombre de minutes entre les deux dates et heures
     * @throws IllegalArgumentException si une des dates est null
     */
    public static long minutesBetween(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        validateNotNull(startDateTime, "La date et heure de début ne peuvent pas être null");
        validateNotNull(endDateTime, "La date et heure de fin ne peuvent pas être null");
        return ChronoUnit.MINUTES.between(startDateTime, endDateTime);
    }

    // ===============================
    // MÉTHODES D'OPÉRATION
    // ===============================
    
    /**
     * Ajoute des jours à une date.
     * 
     * @param date date de base
     * @param days nombre de jours à ajouter (peut être négatif)
     * @return nouvelle LocalDate avec les jours ajoutés
     * @throws IllegalArgumentException si date est null
     */
    public static LocalDate addDays(LocalDate date, long days) {
        validateNotNull(date, "La date ne peut pas être null");
        return date.plusDays(days);
    }
    
    /**
     * Ajoute des heures à une date et heure.
     * 
     * @param dateTime date et heure de base
     * @param hours nombre d'heures à ajouter (peut être négatif)
     * @return nouvelle LocalDateTime avec les heures ajoutées
     * @throws IllegalArgumentException si dateTime est null
     */
    public static LocalDateTime addHours(LocalDateTime dateTime, long hours) {
        validateNotNull(dateTime, "La date et heure ne peuvent pas être null");
        return dateTime.plusHours(hours);
    }
    
    /**
     * Ajoute des minutes à une date et heure.
     * 
     * @param dateTime date et heure de base
     * @param minutes nombre de minutes à ajouter (peut être négatif)
     * @return nouvelle LocalDateTime avec les minutes ajoutées
     * @throws IllegalArgumentException si dateTime est null
     */
    public static LocalDateTime addMinutes(LocalDateTime dateTime, long minutes) {
        validateNotNull(dateTime, "La date et heure ne peuvent pas être null");
        return dateTime.plusMinutes(minutes);
    }

    // ===============================
    // MÉTHODES DE VALIDATION
    // ===============================
    
    /**
     * Vérifie si une date est dans le passé.
     * 
     * @param date date à vérifier
     * @return true si la date est dans le passé, false sinon
     * @throws IllegalArgumentException si date est null
     */
    public static boolean isInPast(LocalDate date) {
        validateNotNull(date, "La date ne peut pas être null");
        return date.isBefore(today());
    }
    
    /**
     * Vérifie si une date et heure est dans le passé.
     * 
     * @param dateTime date et heure à vérifier
     * @return true si la date et heure est dans le passé, false sinon
     * @throws IllegalArgumentException si dateTime est null
     */
    public static boolean isInPast(LocalDateTime dateTime) {
        validateNotNull(dateTime, "La date et heure ne peuvent pas être null");
        return dateTime.isBefore(now());
    }
    
    /**
     * Vérifie si une date est dans le futur.
     * 
     * @param date date à vérifier
     * @return true si la date est dans le futur, false sinon
     * @throws IllegalArgumentException si date est null
     */
    public static boolean isInFuture(LocalDate date) {
        validateNotNull(date, "La date ne peut pas être null");
        return date.isAfter(today());
    }
    
    /**
     * Vérifie si une date et heure est dans le futur.
     * 
     * @param dateTime date et heure à vérifier
     * @return true si la date et heure est dans le futur, false sinon
     * @throws IllegalArgumentException si dateTime est null
     */
    public static boolean isInFuture(LocalDateTime dateTime) {
        validateNotNull(dateTime, "La date et heure ne peuvent pas être null");
        return dateTime.isAfter(now());
    }
    
    /**
     * Vérifie si une chaîne de date est valide selon un format.
     * 
     * @param dateString chaîne à valider
     * @param pattern format à utiliser pour la validation
     * @return true si la chaîne est une date valide, false sinon
     */
    public static boolean isValidDate(String dateString, String pattern) {
        if (dateString == null || pattern == null) {
            return false;
        }
        try {
            LocalDate.parse(dateString, DateTimeFormatter.ofPattern(pattern));
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    // ===============================
    // MÉTHODES DE CONVERSION
    // ===============================
    
    /**
     * Convertit un java.util.Date en LocalDateTime.
     * 
     * @param date Date à convertir
     * @return LocalDateTime équivalent
     * @throws IllegalArgumentException si date est null
     */
    public static LocalDateTime fromDate(Date date) {
        validateNotNull(date, "La date ne peut pas être null");
        return LocalDateTime.ofInstant(date.toInstant(), DEFAULT_ZONE);
    }
    
    /**
     * Convertit un LocalDateTime en java.util.Date.
     * 
     * @param dateTime LocalDateTime à convertir
     * @return Date équivalent
     * @throws IllegalArgumentException si dateTime est null
     */
    public static Date toDate(LocalDateTime dateTime) {
        validateNotNull(dateTime, "La date et heure ne peuvent pas être null");
        return Date.from(dateTime.atZone(DEFAULT_ZONE).toInstant());
    }

    // ===============================
    // MÉTHODES UTILITAIRES
    // ===============================
    
    /**
     * Obtient le début de la journée pour une date donnée (00:00:00).
     * 
     * @param date date pour laquelle obtenir le début de journée
     * @return LocalDateTime représentant le début de la journée
     * @throws IllegalArgumentException si date est null
     */
    public static LocalDateTime startOfDay(LocalDate date) {
        validateNotNull(date, "La date ne peut pas être null");
        return date.atStartOfDay();
    }
    
    /**
     * Obtient la fin de la journée pour une date donnée (23:59:59.999).
     * 
     * @param date date pour laquelle obtenir la fin de journée
     * @return LocalDateTime représentant la fin de la journée
     * @throws IllegalArgumentException si date est null
     */
    public static LocalDateTime endOfDay(LocalDate date) {
        validateNotNull(date, "La date ne peut pas être null");
        return date.atTime(23, 59, 59, 999_999_999);
    }
    
    /**
     * Vérifie qu'un objet n'est pas null.
     * 
     * @param object objet à vérifier
     * @param message message d'erreur si l'objet est null
     * @throws IllegalArgumentException si l'objet est null
     */
    private static void validateNotNull(Object object, String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }
    }

    // ===============================
    // CONSTRUCTEUR PRIVÉ
    // ===============================
    
    /**
     * Constructeur privé pour empêcher l'instanciation de cette classe utilitaire.
     * 
     * @throws UnsupportedOperationException si une tentative d'instanciation est faite
     */
    private DateUtil() {
        throw new UnsupportedOperationException("Cette classe utilitaire ne peut pas être instanciée");
    }
}