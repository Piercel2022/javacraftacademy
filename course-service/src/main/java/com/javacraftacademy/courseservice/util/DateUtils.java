package com.javacraftacademy.courseservice.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Classe utilitaire pour la gestion des dates et heures dans le service de cours.
 * 
 * Cette classe fournit des méthodes pour :
 * - Le formatage et parsing de dates dans différents formats
 * - Le calcul de durées et d'intervalles de temps
 * - La gestion des fuseaux horaires
 * - La validation de dates et plages de dates
 * - La génération de rapports de statistiques temporelles
 * - La gestion des dates de début et fin de cours
 * - Le calcul de la progression dans le temps
 * 
 * Relations avec l'application :
 * - Utilisée par CourseService pour gérer les dates de début/fin des cours
 * - Utilisée par EnrollmentService pour calculer les périodes d'inscription
 * - Utilisée par LessonService pour programmer les leçons
 * - Intégrée dans les rapports et statistiques temporelles
 * - Utilisée pour la validation des données temporelles dans les DTOs
 * 
 * @author JavaCraft Academy
 * @version 1.0
 * @since 1.0
 */
@Component
public class DateUtils {

    private static final Logger logger = LoggerFactory.getLogger(DateUtils.class);

    /**
     * Formatters de dates couramment utilisés
     */
    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");
    public static final DateTimeFormatter DISPLAY_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    public static final DateTimeFormatter DISPLAY_DATETIME_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    public static final DateTimeFormatter ISO_DATETIME_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    public static final DateTimeFormatter COURSE_SCHEDULE_FORMAT = DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy 'à' HH:mm", Locale.FRENCH);

    /**
     * Fuseau horaire par défaut pour l'application
     */
    public static final ZoneId DEFAULT_ZONE = ZoneId.of("Europe/Paris");

    /**
     * Constantes pour les calculs de durée
     */
    public static final long SECONDS_IN_MINUTE = 60;
    public static final long MINUTES_IN_HOUR = 60;
    public static final long HOURS_IN_DAY = 24;
    public static final long DAYS_IN_WEEK = 7;
    public static final long DAYS_IN_MONTH = 30; // Approximation
    public static final long DAYS_IN_YEAR = 365; // Approximation

    /**
     * Obtient la date et l'heure actuelles dans le fuseau horaire par défaut.
     * 
     * @return LocalDateTime actuel
     */
    public static LocalDateTime now() {
        return LocalDateTime.now(DEFAULT_ZONE);
    }

    /**
     * Obtient la date actuelle dans le fuseau horaire par défaut.
     * 
     * @return LocalDate actuel
     */
    public static LocalDate today() {
        return LocalDate.now(DEFAULT_ZONE);
    }

    /**
     * Obtient l'heure actuelle dans le fuseau horaire par défaut.
     * 
     * @return LocalTime actuel
     */
    public static LocalTime nowTime() {
        return LocalTime.now(DEFAULT_ZONE);
    }

    /**
     * Formate une date selon le format spécifié.
     * 
     * @param date La date à formater
     * @param formatter Le formatter à utiliser
     * @return La date formatée en chaîne de caractères
     */
    public static String formatDate(LocalDate date, DateTimeFormatter formatter) {
        if (date == null) {
            return null;
        }
        return date.format(formatter);
    }

    /**
     * Formate une date-heure selon le format spécifié.
     * 
     * @param dateTime La date-heure à formater
     * @param formatter Le formatter à utiliser
     * @return La date-heure formatée en chaîne de caractères
     */
    public static String formatDateTime(LocalDateTime dateTime, DateTimeFormatter formatter) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(formatter);
    }

    /**
     * Formate une date pour l'affichage utilisateur (format français).
     * 
     * @param date La date à formater
     * @return La date formatée pour l'affichage
     */
    public static String formatForDisplay(LocalDate date) {
        return formatDate(date, DISPLAY_DATE_FORMAT);
    }

    /**
     * Formate une date-heure pour l'affichage utilisateur (format français).
     * 
     * @param dateTime La date-heure à formater
     * @return La date-heure formatée pour l'affichage
     */
    public static String formatForDisplay(LocalDateTime dateTime) {
        return formatDateTime(dateTime, DISPLAY_DATETIME_FORMAT);
    }

    /**
     * Formate une date-heure pour l'affichage des horaires de cours.
     * 
     * @param dateTime La date-heure du cours
     * @return La date-heure formatée en français
     */
    public static String formatCourseSchedule(LocalDateTime dateTime) {
        return formatDateTime(dateTime, COURSE_SCHEDULE_FORMAT);
    }

    /**
     * Parse une chaîne de date selon le format spécifié.
     * 
     * @param dateString La chaîne à parser
     * @param formatter Le formatter à utiliser
     * @return La date parsée
     * @throws DateTimeParseException Si le parsing échoue
     */
    public static LocalDate parseDate(String dateString, DateTimeFormatter formatter) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }
        return LocalDate.parse(dateString.trim(), formatter);
    }

    /**
     * Parse une chaîne de date-heure selon le format spécifié.
     * 
     * @param dateTimeString La chaîne à parser
     * @param formatter Le formatter à utiliser
     * @return La date-heure parsée
     * @throws DateTimeParseException Si le parsing échoue
     */
    public static LocalDateTime parseDateTime(String dateTimeString, DateTimeFormatter formatter) {
        if (dateTimeString == null || dateTimeString.trim().isEmpty()) {
            return null;
        }
        return LocalDateTime.parse(dateTimeString.trim(), formatter);
    }

    /**
     * Parse une date au format ISO (yyyy-MM-dd).
     * 
     * @param dateString La chaîne de date au format ISO
     * @return La date parsée
     */
    public static LocalDate parseIsoDate(String dateString) {
        return parseDate(dateString, DATE_FORMAT);
    }

    /**
     * Parse une date-heure au format ISO.
     * 
     * @param dateTimeString La chaîne de date-heure au format ISO
     * @return La date-heure parsée
     */
    public static LocalDateTime parseIsoDateTime(String dateTimeString) {
        return parseDateTime(dateTimeString, ISO_DATETIME_FORMAT);
    }

    /**
     * Parse une date au format d'affichage français (dd/MM/yyyy).
     * 
     * @param dateString La chaîne de date au format français
     * @return La date parsée
     */
    public static LocalDate parseDisplayDate(String dateString) {
        return parseDate(dateString, DISPLAY_DATE_FORMAT);
    }

    /**
     * Calcule la durée entre deux dates-heures.
     * 
     * @param startDateTime Date-heure de début
     * @param endDateTime Date-heure de fin
     * @return La durée entre les deux dates
     */
    public static Duration calculateDuration(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        if (startDateTime == null || endDateTime == null) {
            return Duration.ZERO;
        }
        return Duration.between(startDateTime, endDateTime);
    }

    /**
     * Calcule le nombre de jours entre deux dates.
     * 
     * @param startDate Date de début
     * @param endDate Date de fin
     * @return Le nombre de jours entre les deux dates
     */
    public static long daysBetween(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(startDate, endDate);
    }

    /**
     * Calcule le nombre d'heures entre deux dates-heures.
     * 
     * @param startDateTime Date-heure de début
     * @param endDateTime Date-heure de fin
     * @return Le nombre d'heures entre les deux dates
     */
    public static long hoursBetween(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        if (startDateTime == null || endDateTime == null) {
            return 0;
        }
        return ChronoUnit.HOURS.between(startDateTime, endDateTime);
    }

    /**
     * Calcule le nombre de minutes entre deux dates-heures.
     * 
     * @param startDateTime Date-heure de début
     * @param endDateTime Date-heure de fin
     * @return Le nombre de minutes entre les deux dates
     */
    public static long minutesBetween(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        if (startDateTime == null || endDateTime == null) {
            return 0;
        }
        return ChronoUnit.MINUTES.between(startDateTime, endDateTime);
    }

    /**
     * Vérifie si une date est comprise dans une plage de dates.
     * 
     * @param date La date à vérifier
     * @param startDate Date de début de la plage
     * @param endDate Date de fin de la plage
     * @return true si la date est dans la plage, false sinon
     */
    public static boolean isDateInRange(LocalDate date, LocalDate startDate, LocalDate endDate) {
        if (date == null || startDate == null || endDate == null) {
            return false;
        }
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }

    /**
     * Vérifie si une date-heure est comprise dans une plage de dates-heures.
     * 
     * @param dateTime La date-heure à vérifier
     * @param startDateTime Date-heure de début de la plage
     * @param endDateTime Date-heure de fin de la plage
     * @return true si la date-heure est dans la plage, false sinon
     */
    public static boolean isDateTimeInRange(LocalDateTime dateTime, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        if (dateTime == null || startDateTime == null || endDateTime == null) {
            return false;
        }
        return !dateTime.isBefore(startDateTime) && !dateTime.isAfter(endDateTime);
    }

    /**
     * Vérifie si une date est dans le futur.
     * 
     * @param date La date à vérifier
     * @return true si la date est dans le futur, false sinon
     */
    public static boolean isFutureDate(LocalDate date) {
        if (date == null) {
            return false;
        }
        return date.isAfter(today());
    }

    /**
     * Vérifie si une date-heure est dans le futur.
     * 
     * @param dateTime La date-heure à vérifier
     * @return true si la date-heure est dans le futur, false sinon
     */
    public static boolean isFutureDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return false;
        }
        return dateTime.isAfter(now());
    }

    /**
     * Vérifie si une date est dans le passé.
     * 
     * @param date La date à vérifier
     * @return true si la date est dans le passé, false sinon
     */
    public static boolean isPastDate(LocalDate date) {
        if (date == null) {
            return false;
        }
        return date.isBefore(today());
    }

    /**
     * Vérifie si une date-heure est dans le passé.
     * 
     * @param dateTime La date-heure à vérifier
     * @return true si la date-heure est dans le passé, false sinon
     */
    public static boolean isPastDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return false;
        }
        return dateTime.isBefore(now());
    }

    /**
     * Obtient le début de la journée pour une date donnée.
     * 
     * @param date La date
     * @return LocalDateTime représentant le début de la journée (00:00:00)
     */
    public static LocalDateTime startOfDay(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.atStartOfDay();
    }

    /**
     * Obtient la fin de la journée pour une date donnée.
     * 
     * @param date La date
     * @return LocalDateTime représentant la fin de la journée (23:59:59.999999999)
     */
    public static LocalDateTime endOfDay(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.atTime(LocalTime.MAX);
    }

    /**
     * Obtient le début de la semaine (lundi) pour une date donnée.
     * 
     * @param date La date
     * @return LocalDate représentant le début de la semaine
     */
    public static LocalDate startOfWeek(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    /**
     * Obtient la fin de la semaine (dimanche) pour une date donnée.
     * 
     * @param date La date
     * @return LocalDate représentant la fin de la semaine
     */
    public static LocalDate endOfWeek(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
    }

    /**
     * Obtient le début du mois pour une date donnée.
     * 
     * @param date La date
     * @return LocalDate représentant le premier jour du mois
     */
    public static LocalDate startOfMonth(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.with(TemporalAdjusters.firstDayOfMonth());
    }

    /**
     * Obtient la fin du mois pour une date donnée.
     * 
     * @param date La date
     * @return LocalDate représentant le dernier jour du mois
     */
    public static LocalDate endOfMonth(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.with(TemporalAdjusters.lastDayOfMonth());
    }

    /**
     * Ajoute un nombre de jours à une date.
     * 
     * @param date La date de base
     * @param days Le nombre de jours à ajouter (peut être négatif)
     * @return La nouvelle date
     */
    public static LocalDate addDays(LocalDate date, long days) {
        if (date == null) {
            return null;
        }
        return date.plusDays(days);
    }

    /**
     * Ajoute un nombre d'heures à une date-heure.
     * 
     * @param dateTime La date-heure de base
     * @param hours Le nombre d'heures à ajouter (peut être négatif)
     * @return La nouvelle date-heure
     */
    public static LocalDateTime addHours(LocalDateTime dateTime, long hours) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.plusHours(hours);
    }

    /**
     * Ajoute un nombre de minutes à une date-heure.
     * 
     * @param dateTime La date-heure de base
     * @param minutes Le nombre de minutes à ajouter (peut être négatif)
     * @return La nouvelle date-heure
     */
    public static LocalDateTime addMinutes(LocalDateTime dateTime, long minutes) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.plusMinutes(minutes);
    }

    /**
     * Calcule l'âge en années à partir d'une date de naissance.
     * 
     * @param birthDate La date de naissance
     * @return L'âge en années
     */
    public static int calculateAge(LocalDate birthDate) {
        if (birthDate == null) {
            return 0;
        }
        return Period.between(birthDate, today()).getYears();
    }

    /**
     * Génère une liste de dates entre deux dates (incluses).
     * 
     * @param startDate Date de début
     * @param endDate Date de fin
     * @return Liste des dates entre les deux dates
     */
    public static List<LocalDate> getDatesBetween(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            return Collections.emptyList();
        }
        
        return startDate.datesUntil(endDate.plusDays(1))
                       .collect(Collectors.toList());
    }

    /**
     * Obtient les jours ouvrables entre deux dates (excluant weekends).
     * 
     * @param startDate Date de début
     * @param endDate Date de fin
     * @return Liste des jours ouvrables
     */
    public static List<LocalDate> getWorkingDaysBetween(LocalDate startDate, LocalDate endDate) {
        return getDatesBetween(startDate, endDate).stream()
                .filter(date -> date.getDayOfWeek() != DayOfWeek.SATURDAY && 
                               date.getDayOfWeek() != DayOfWeek.SUNDAY)
                .collect(Collectors.toList());
    }

    /**
     * Convertit une LocalDateTime en timestamp Unix (millisecondes).
     * 
     * @param dateTime La date-heure à convertir
     * @return Le timestamp Unix en millisecondes
     */
    public static long toUnixTimestamp(LocalDateTime dateTime) {
        if (dateTime == null) {
            return 0;
        }
        return dateTime.atZone(DEFAULT_ZONE).toInstant().toEpochMilli();
    }

    /**
     * Convertit un timestamp Unix en LocalDateTime.
     * 
     * @param timestamp Le timestamp Unix en millisecondes
     * @return La date-heure correspondante
     */
    public static LocalDateTime fromUnixTimestamp(long timestamp) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), DEFAULT_ZONE);
    }

    /**
     * Formate une durée en format lisible (ex: "2h 30min").
     * 
     * @param duration La durée à formater
     * @return La durée formatée en chaîne lisible
     */
    public static String formatDuration(Duration duration) {
        if (duration == null || duration.isZero()) {
            return "0min";
        }
        
        long hours = duration.toHours();
        long minutes = duration.toMinutes() % 60;
        
        StringBuilder sb = new StringBuilder();
        if (hours > 0) {
            sb.append(hours).append("h");
            if (minutes > 0) {
                sb.append(" ");
            }
        }
        if (minutes > 0) {
            sb.append(minutes).append("min");
        }
        
        return sb.toString();
    }

    /**
     * Valide qu'une date de début est antérieure à une date de fin.
     * 
     * @param startDate Date de début
     * @param endDate Date de fin
     * @return true si la validation réussit, false sinon
     */
    public static boolean validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return false;
        }
        return !startDate.isAfter(endDate);
    }

    /**
     * Valide qu'une date-heure de début est antérieure à une date-heure de fin.
     * 
     * @param startDateTime Date-heure de début
     * @param endDateTime Date-heure de fin
     * @return true si la validation réussit, false sinon
     */
    public static boolean validateDateTimeRange(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        if (startDateTime == null || endDateTime == null) {
            return false;
        }
        return !startDateTime.isAfter(endDateTime);
    }

    /**
     * Calcule le pourcentage de progression entre deux dates par rapport à la date actuelle.
     * 
     * @param startDate Date de début
     * @param endDate Date de fin
     * @return Le pourcentage de progression (0-100)
     */
    public static double calculateProgressPercentage(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            return 0.0;
        }
        
        LocalDate currentDate = today();
        if (currentDate.isBefore(startDate)) {
            return 0.0;
        }
        if (currentDate.isAfter(endDate)) {
            return 100.0;
        }
        
        long totalDays = daysBetween(startDate, endDate);
        long elapsedDays = daysBetween(startDate, currentDate);
        
        if (totalDays == 0) {
            return 100.0;
        }
        
        return (double) elapsedDays / totalDays * 100.0;
    }

    /**
     * Obtient le prochain jour ouvrable après une date donnée.
     * 
     * @param date La date de référence
     * @return Le prochain jour ouvrable
     */
    public static LocalDate getNextWorkingDay(LocalDate date) {
        if (date == null) {
            return null;
        }
        
        LocalDate nextDay = date.plusDays(1);
        while (nextDay.getDayOfWeek() == DayOfWeek.SATURDAY || 
               nextDay.getDayOfWeek() == DayOfWeek.SUNDAY) {
            nextDay = nextDay.plusDays(1);
        }
        return nextDay;
    }

    /**
     * Obtient le jour ouvrable précédent avant une date donnée.
     * 
     * @param date La date de référence
     * @return Le jour ouvrable précédent
     */
    public static LocalDate getPreviousWorkingDay(LocalDate date) {
        if (date == null) {
            return null;
        }
        
        LocalDate previousDay = date.minusDays(1);
        while (previousDay.getDayOfWeek() == DayOfWeek.SATURDAY || 
               previousDay.getDayOfWeek() == DayOfWeek.SUNDAY) {
            previousDay = previousDay.minusDays(1);
        }
        return previousDay;
    }

    /**
     * Crée un résumé temporel pour les rapports.
     * 
     * @param startDate Date de début de la période
     * @param endDate Date de fin de la période
     * @return Map contenant les statistiques temporelles
     */
    public static Map<String, Object> createTemporalSummary(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> summary = new HashMap<>();
        
        if (startDate == null || endDate == null) {
            return summary;
        }
        
        summary.put("startDate", formatForDisplay(startDate));
        summary.put("endDate", formatForDisplay(endDate));
        summary.put("totalDays", daysBetween(startDate, endDate));
        summary.put("workingDays", getWorkingDaysBetween(startDate, endDate).size());
        summary.put("progressPercentage", Math.round(calculateProgressPercentage(startDate, endDate) * 100.0) / 100.0);
        summary.put("isActive", isDateInRange(today(), startDate, endDate));
        summary.put("isPast", today().isAfter(endDate));
        summary.put("isFuture", today().isBefore(startDate));
        
        return summary;
    }

    /**
     * Parse une date de manière flexible en essayant plusieurs formats.
     * 
     * @param dateString La chaîne de date à parser
     * @return La date parsée ou null si aucun format ne fonctionne
     */
    public static LocalDate parseFlexibleDate(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }
        
        DateTimeFormatter[] formats = {
            DATE_FORMAT,
            DISPLAY_DATE_FORMAT,
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd"),
            DateTimeFormatter.ofPattern("dd/MM/yy")
        };
        
        for (DateTimeFormatter format : formats) {
            try {
                return parseDate(dateString, format);
            } catch (DateTimeParseException e) {
                logger.debug("Failed to parse date '{}' with format {}", dateString, format);
            }
        }
        
        logger.warn("Unable to parse date string: {}", dateString);
        return null;
    }

    /**
     * Convertit une date en différents formats dans une Map.
     * 
     * @param date La date à convertir
     * @return Map contenant la date dans différents formats
     */
    public static Map<String, String> dateToFormats(LocalDate date) {
        Map<String, String> formats = new HashMap<>();
        
        if (date != null) {
            formats.put("iso", formatDate(date, DATE_FORMAT));
            formats.put("display", formatForDisplay(date));
            formats.put("dayOfWeek", date.getDayOfWeek().name());
            formats.put("dayOfMonth", String.valueOf(date.getDayOfMonth()));
            formats.put("month", date.getMonth().name());
            formats.put("year", String.valueOf(date.getYear()));
        }
        
        return formats;
    }
}