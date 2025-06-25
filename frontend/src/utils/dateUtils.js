/**
 * @fileoverview Utilitaires de date pour l'application JavaCraft Academy
 * @author JavaCraft Academy Team
 * @version 1.0.0
 * 
 * Ce module fournit des fonctions utilitaires pour la manipulation, le formatage
 * et la validation des dates dans l'ensemble de l'application.
 * 
 * Relations avec l'application :
 * - CourseService : formatage des dates de création et mise à jour des cours
 * - ProgressService : calcul des durées d'apprentissage et dates de complétion
 * - NotificationService : gestion des timestamps et programmation
 * - UserProfile : affichage des dates d'inscription et dernière connexion
 * - Dashboard : statistiques temporelles et graphiques de progression
 * - LessonPlayer : suivi du temps passé sur les leçons
 * - Achievement : dates de déblocage des récompenses
 */

/**
 * Formats de date supportés par l'application
 * @readonly
 * @enum {string}
 */
export const DATE_FORMATS = {
  /** Format complet avec jour, mois et année */
  FULL: 'FULL',
  /** Format court avec jour/mois/année */
  SHORT: 'SHORT',
  /** Format pour les timestamps */
  TIMESTAMP: 'TIMESTAMP',
  /** Format pour l'API ISO */
  ISO: 'ISO',
  /** Format relatif (il y a 2 heures, hier, etc.) */
  RELATIVE: 'RELATIVE',
  /** Format pour les durées */
  DURATION: 'DURATION'
};

/**
 * Locales supportées pour la localisation des dates
 * @readonly
 * @enum {string}
 */
export const LOCALES = {
  FR: 'fr-FR',
  EN: 'en-US',
  ES: 'es-ES',
  DE: 'de-DE'
};

/**
 * Constantes temporelles en millisecondes
 * @readonly
 * @enum {number}
 */
export const TIME_CONSTANTS = {
  SECOND: 1000,
  MINUTE: 60 * 1000,
  HOUR: 60 * 60 * 1000,
  DAY: 24 * 60 * 60 * 1000,
  WEEK: 7 * 24 * 60 * 60 * 1000,
  MONTH: 30 * 24 * 60 * 60 * 1000,
  YEAR: 365 * 24 * 60 * 60 * 1000
};

/**
 * Obtient la date actuelle
 * Utilisé par : Dashboard, NotificationService, ProgressService
 * 
 * @returns {Date} La date actuelle
 * @example
 * const now = getCurrentDate();
 * console.log(now); // 2025-06-22T10:30:00.000Z
 */
export const getCurrentDate = () => {
  return new Date();
};

/**
 * Formate une date selon le format spécifié et la locale
 * Utilisé par : CourseCard, UserProfile, LessonPlayer, Achievement
 * 
 * @param {Date|string|number} date - La date à formater
 * @param {string} format - Le format de sortie (voir DATE_FORMATS)
 * @param {string} [locale='fr-FR'] - La locale pour la localisation
 * @returns {string} La date formatée
 * @throws {Error} Si la date est invalide
 * 
 * @example
 * formatDate(new Date(), 'FULL', 'fr-FR'); // "dimanche 22 juin 2025"
 * formatDate('2025-06-22', 'SHORT'); // "22/06/2025"
 * formatDate(Date.now(), 'RELATIVE'); // "à l'instant"
 */
export const formatDate = (date, format = DATE_FORMATS.SHORT, locale = LOCALES.FR) => {
  try {
    const dateObj = new Date(date);
    
    if (isNaN(dateObj.getTime())) {
      throw new Error('Date invalide fournie à formatDate');
    }

    switch (format) {
      case DATE_FORMATS.FULL:
        return dateObj.toLocaleDateString(locale, {
          weekday: 'long',
          year: 'numeric',
          month: 'long',
          day: 'numeric'
        });

      case DATE_FORMATS.SHORT:
        return dateObj.toLocaleDateString(locale);

      case DATE_FORMATS.TIMESTAMP:
        return dateObj.toLocaleString(locale);

      case DATE_FORMATS.ISO:
        return dateObj.toISOString();

      case DATE_FORMATS.RELATIVE:
        return getRelativeTime(dateObj, locale);

      case DATE_FORMATS.DURATION:
        return formatDuration(dateObj.getTime());

      default:
        return dateObj.toLocaleDateString(locale);
    }
  } catch (error) {
    console.error('Erreur lors du formatage de la date:', error);
    return 'Date invalide';
  }
};

/**
 * Retourne le temps relatif par rapport à maintenant
 * Utilisé par : NotificationPanel, CourseProgress, Dashboard
 * 
 * @param {Date} date - La date à comparer
 * @param {string} [locale='fr-FR'] - La locale pour la localisation
 * @returns {string} Le temps relatif formaté
 * 
 * @example
 * getRelativeTime(new Date(Date.now() - 3600000)); // "il y a 1 heure"
 * getRelativeTime(new Date(Date.now() + 86400000)); // "dans 1 jour"
 */
export const getRelativeTime = (date, locale = LOCALES.FR) => {
  const now = new Date();
  const diffMs = now.getTime() - new Date(date).getTime();
  const diffAbs = Math.abs(diffMs);
  const isPast = diffMs > 0;

  // Définition des seuils et labels selon la locale
  const timeLabels = {
    [LOCALES.FR]: {
      past: {
        seconds: 'à l\'instant',
        minute: 'il y a 1 minute',
        minutes: (n) => `il y a ${n} minutes`,
        hour: 'il y a 1 heure',
        hours: (n) => `il y a ${n} heures`,
        day: 'hier',
        days: (n) => `il y a ${n} jours`,
        week: 'il y a 1 semaine',
        weeks: (n) => `il y a ${n} semaines`,
        month: 'il y a 1 mois',
        months: (n) => `il y a ${n} mois`,
        year: 'il y a 1 an',
        years: (n) => `il y a ${n} ans`
      },
      future: {
        seconds: 'dans quelques secondes',
        minute: 'dans 1 minute',
        minutes: (n) => `dans ${n} minutes`,
        hour: 'dans 1 heure',
        hours: (n) => `dans ${n} heures`,
        day: 'demain',
        days: (n) => `dans ${n} jours`,
        week: 'dans 1 semaine',
        weeks: (n) => `dans ${n} semaines`,
        month: 'dans 1 mois',
        months: (n) => `dans ${n} mois`,
        year: 'dans 1 an',
        years: (n) => `dans ${n} ans`
      }
    }
  };

  const labels = timeLabels[locale] || timeLabels[LOCALES.FR];
  const direction = isPast ? 'past' : 'future';

  if (diffAbs < TIME_CONSTANTS.MINUTE) {
    return labels[direction].seconds;
  } else if (diffAbs < TIME_CONSTANTS.HOUR) {
    const minutes = Math.floor(diffAbs / TIME_CONSTANTS.MINUTE);
    return minutes === 1 ? labels[direction].minute : labels[direction].minutes(minutes);
  } else if (diffAbs < TIME_CONSTANTS.DAY) {
    const hours = Math.floor(diffAbs / TIME_CONSTANTS.HOUR);
    return hours === 1 ? labels[direction].hour : labels[direction].hours(hours);
  } else if (diffAbs < TIME_CONSTANTS.WEEK) {
    const days = Math.floor(diffAbs / TIME_CONSTANTS.DAY);
    return days === 1 ? labels[direction].day : labels[direction].days(days);
  } else if (diffAbs < TIME_CONSTANTS.MONTH) {
    const weeks = Math.floor(diffAbs / TIME_CONSTANTS.WEEK);
    return weeks === 1 ? labels[direction].week : labels[direction].weeks(weeks);
  } else if (diffAbs < TIME_CONSTANTS.YEAR) {
    const months = Math.floor(diffAbs / TIME_CONSTANTS.MONTH);
    return months === 1 ? labels[direction].month : labels[direction].months(months);
  } else {
    const years = Math.floor(diffAbs / TIME_CONSTANTS.YEAR);
    return years === 1 ? labels[direction].year : labels[direction].years(years);
  }
};

/**
 * Formate une durée en millisecondes en format lisible
 * Utilisé par : LessonPlayer, ProgressBar, StatsCard
 * 
 * @param {number} durationMs - Durée en millisecondes
 * @param {boolean} [detailed=false] - Affichage détaillé ou simplifié
 * @returns {string} La durée formatée
 * 
 * @example
 * formatDuration(3661000); // "1h 1m"
 * formatDuration(3661000, true); // "1 heure, 1 minute et 1 seconde"
 */
export const formatDuration = (durationMs, detailed = false) => {
  if (durationMs < 0) return '0s';

  const hours = Math.floor(durationMs / TIME_CONSTANTS.HOUR);
  const minutes = Math.floor((durationMs % TIME_CONSTANTS.HOUR) / TIME_CONSTANTS.MINUTE);
  const seconds = Math.floor((durationMs % TIME_CONSTANTS.MINUTE) / TIME_CONSTANTS.SECOND);

  if (detailed) {
    const parts = [];
    if (hours > 0) parts.push(`${hours} heure${hours > 1 ? 's' : ''}`);
    if (minutes > 0) parts.push(`${minutes} minute${minutes > 1 ? 's' : ''}`);
    if (seconds > 0) parts.push(`${seconds} seconde${seconds > 1 ? 's' : ''}`);
    
    if (parts.length === 0) return '0 seconde';
    if (parts.length === 1) return parts[0];
    if (parts.length === 2) return `${parts[0]} et ${parts[1]}`;
    return `${parts.slice(0, -1).join(', ')} et ${parts[parts.length - 1]}`;
  } else {
    if (hours > 0) return `${hours}h ${minutes}m`;
    if (minutes > 0) return `${minutes}m ${seconds}s`;
    return `${seconds}s`;
  }
};

/**
 * Calcule la différence entre deux dates
 * Utilisé par : ProgressService, CourseService, Achievement
 * 
 * @param {Date|string} startDate - Date de début
 * @param {Date|string} endDate - Date de fin
 * @returns {Object} Objet contenant les différences en différentes unités
 * 
 * @example
 * const diff = getDateDifference('2025-06-01', '2025-06-22');
 * // { days: 21, hours: 504, minutes: 30240, seconds: 1814400, milliseconds: 1814400000 }
 */
export const getDateDifference = (startDate, endDate) => {
  const start = new Date(startDate);
  const end = new Date(endDate);
  const diffMs = Math.abs(end.getTime() - start.getTime());

  return {
    milliseconds: diffMs,
    seconds: Math.floor(diffMs / TIME_CONSTANTS.SECOND),
    minutes: Math.floor(diffMs / TIME_CONSTANTS.MINUTE),
    hours: Math.floor(diffMs / TIME_CONSTANTS.HOUR),
    days: Math.floor(diffMs / TIME_CONSTANTS.DAY),
    weeks: Math.floor(diffMs / TIME_CONSTANTS.WEEK),
    months: Math.floor(diffMs / TIME_CONSTANTS.MONTH),
    years: Math.floor(diffMs / TIME_CONSTANTS.YEAR)
  };
};

/**
 * Valide si une chaîne ou un objet représente une date valide
 * Utilisé par : formulaires, validators.js, services API
 * 
 * @param {*} date - La valeur à valider
 * @returns {boolean} True si la date est valide, false sinon
 * 
 * @example
 * isValidDate('2025-06-22'); // true
 * isValidDate('invalid'); // false
 * isValidDate(new Date()); // true
 */
export const isValidDate = (date) => {
  try {
    const dateObj = new Date(date);
    return !isNaN(dateObj.getTime()) && dateObj.getTime() > 0;
  } catch (error) {
    return false;
  }
};

/**
 * Ajoute une durée à une date
 * Utilisé par : NotificationService, CourseService, ProgressService
 * 
 * @param {Date|string} date - Date de base
 * @param {number} amount - Quantité à ajouter
 * @param {string} unit - Unité (days, hours, minutes, seconds, etc.)
 * @returns {Date} Nouvelle date avec la durée ajoutée
 * 
 * @example
 * addToDate(new Date(), 7, 'days'); // Date dans 7 jours
 * addToDate('2025-06-22', 2, 'hours'); // 2 heures plus tard
 */
export const addToDate = (date, amount, unit) => {
  const baseDate = new Date(date);
  const result = new Date(baseDate);

  switch (unit.toLowerCase()) {
    case 'years':
    case 'year':
      result.setFullYear(result.getFullYear() + amount);
      break;
    case 'months':
    case 'month':
      result.setMonth(result.getMonth() + amount);
      break;
    case 'weeks':
    case 'week':
      result.setDate(result.getDate() + (amount * 7));
      break;
    case 'days':
    case 'day':
      result.setDate(result.getDate() + amount);
      break;
    case 'hours':
    case 'hour':
      result.setTime(result.getTime() + (amount * TIME_CONSTANTS.HOUR));
      break;
    case 'minutes':
    case 'minute':
      result.setTime(result.getTime() + (amount * TIME_CONSTANTS.MINUTE));
      break;
    case 'seconds':
    case 'second':
      result.setTime(result.getTime() + (amount * TIME_CONSTANTS.SECOND));
      break;
    default:
      console.warn(`Unité de temps non reconnue: ${unit}`);
      return baseDate;
  }

  return result;
};

/**
 * Obtient le début de la journée pour une date donnée
 * Utilisé par : Dashboard (statistiques quotidiennes), ProgressService
 * 
 * @param {Date|string} [date=new Date()] - Date de référence
 * @returns {Date} Date au début de la journée (00:00:00)
 * 
 * @example
 * getStartOfDay('2025-06-22T15:30:45'); // 2025-06-22T00:00:00
 */
export const getStartOfDay = (date = new Date()) => {
  const result = new Date(date);
  result.setHours(0, 0, 0, 0);
  return result;
};

/**
 * Obtient la fin de la journée pour une date donnée
 * Utilisé par : Dashboard (statistiques quotidiennes), ProgressService
 * 
 * @param {Date|string} [date=new Date()] - Date de référence
 * @returns {Date} Date à la fin de la journée (23:59:59.999)
 * 
 * @example
 * getEndOfDay('2025-06-22T15:30:45'); // 2025-06-22T23:59:59.999
 */
export const getEndOfDay = (date = new Date()) => {
  const result = new Date(date);
  result.setHours(23, 59, 59, 999);
  return result;
};

/**
 * Vérifie si deux dates correspondent au même jour
 * Utilisé par : Dashboard, ProgressService, NotificationService
 * 
 * @param {Date|string} date1 - Première date
 * @param {Date|string} date2 - Deuxième date
 * @returns {boolean} True si les dates sont le même jour
 * 
 * @example
 * isSameDay('2025-06-22T10:00:00', '2025-06-22T18:30:00'); // true
 * isSameDay('2025-06-22', '2025-06-23'); // false
 */
export const isSameDay = (date1, date2) => {
  const d1 = new Date(date1);
  const d2 = new Date(date2);
  
  return d1.getFullYear() === d2.getFullYear() &&
         d1.getMonth() === d2.getMonth() &&
         d1.getDate() === d2.getDate();
};

/**
 * Obtient une plage de dates pour les statistiques
 * Utilisé par : Dashboard, Progress, StatsCard
 * 
 * @param {string} period - Période ('today', 'week', 'month', 'year')
 * @param {Date|string} [referenceDate=new Date()] - Date de référence
 * @returns {Object} Objet avec les dates de début et fin
 * 
 * @example
 * getDateRange('week'); // { start: Date, end: Date } pour cette semaine
 * getDateRange('month', '2025-06-15'); // Plage pour juin 2025
 */
export const getDateRange = (period, referenceDate = new Date()) => {
  const refDate = new Date(referenceDate);
  const start = new Date(refDate);
  const end = new Date(refDate);

  switch (period.toLowerCase()) {
    case 'today':
      return {
        start: getStartOfDay(start),
        end: getEndOfDay(end)
      };

    case 'week':
      const dayOfWeek = start.getDay();
      const diffToMonday = dayOfWeek === 0 ? -6 : 1 - dayOfWeek;
      start.setDate(start.getDate() + diffToMonday);
      end.setDate(start.getDate() + 6);
      return {
        start: getStartOfDay(start),
        end: getEndOfDay(end)
      };

    case 'month':
      start.setDate(1);
      end.setMonth(end.getMonth() + 1, 0);
      return {
        start: getStartOfDay(start),
        end: getEndOfDay(end)
      };

    case 'year':
      start.setMonth(0, 1);
      end.setMonth(11, 31);
      return {
        start: getStartOfDay(start),
        end: getEndOfDay(end)
      };

    default:
      console.warn(`Période non reconnue: ${period}`);
      return {
        start: getStartOfDay(refDate),
        end: getEndOfDay(refDate)
      };
  }
};

/**
 * Convertit une date en timestamp Unix
 * Utilisé par : API services, cache système, notifications
 * 
 * @param {Date|string} [date=new Date()] - Date à convertir
 * @returns {number} Timestamp Unix en secondes
 * 
 * @example
 * toUnixTimestamp(new Date()); // 1719057600
 * toUnixTimestamp('2025-06-22'); // 1719014400
 */
export const toUnixTimestamp = (date = new Date()) => {
  return Math.floor(new Date(date).getTime() / 1000);
};

/**
 * Convertit un timestamp Unix en objet Date
 * Utilisé par : API services, données en cache
 * 
 * @param {number} timestamp - Timestamp Unix en secondes
 * @returns {Date} Objet Date correspondant
 * 
 * @example
 * fromUnixTimestamp(1719057600); // Date object pour 2025-06-22
 */
export const fromUnixTimestamp = (timestamp) => {
  return new Date(timestamp * 1000);
};

/**
 * Objet par défaut exporté contenant toutes les fonctions utilitaires
 * Utilisé pour l'importation globale : import dateUtils from './dateUtils'
 */
const dateUtils = {
  // Constantes
  DATE_FORMATS,
  LOCALES,
  TIME_CONSTANTS,
  
  // Fonctions principales
  getCurrentDate,
  formatDate,
  getRelativeTime,
  formatDuration,
  getDateDifference,
  isValidDate,
  addToDate,
  getStartOfDay,
  getEndOfDay,
  isSameDay,
  getDateRange,
  toUnixTimestamp,
  fromUnixTimestamp
};

export default dateUtils;
