/**
 * @fileoverview Utilitaires de formatage pour JavaCraft Academy
 * 
 * Ce module contient toutes les fonctions utilitaires pour le formatage des données
 * dans l'application JavaCraft Academy. Il gère le formatage des dates, des nombres,
 * des textes, des durées, et d'autres types de données spécifiques à la plateforme.
 * 
 * Relations avec l'application :
 * - Utilisé par tous les composants pour l'affichage formaté des données
 * - Intégré dans CourseCard, CourseProgress, UserProfile, StatsCard
 * - Utilisé par LessonPlayer pour le formatage du temps
 * - Employé dans Dashboard pour les statistiques
 * - Connecté aux services API pour le formatage des réponses
 * 
 * @version 1.0.0
 * @author JavaCraft Academy Team
 * @since 2024-01-01
 */

/**
 * Formate une date en format lisible français
 * 
 * @param {Date|string|number} date - La date à formater
 * @param {Object} options - Options de formatage
 * @param {string} options.format - Format de sortie ('short', 'medium', 'long', 'full')
 * @param {boolean} options.includeTime - Inclure l'heure dans le formatage
 * @param {string} options.locale - Locale à utiliser (par défaut 'fr-FR')
 * @returns {string} Date formatée
 * 
 * @example
 * // Utilisation dans CourseCard pour afficher la date de création
 * const createdDate = formatDate(course.createdAt, { format: 'medium' });
 * 
 * @example
 * // Utilisation dans Dashboard pour les statistiques
 * const lastActivity = formatDate(user.lastActivity, { 
 *   format: 'short', 
 *   includeTime: true 
 * });
 */
export const formatDate = (date, options = {}) => {
  const {
    format = 'medium',
    includeTime = false,
    locale = 'fr-FR'
  } = options;

  if (!date) return '';

  const dateObj = new Date(date);
  
  if (isNaN(dateObj.getTime())) {
    console.warn('Date invalide fournie à formatDate:', date);
    return '';
  }

  const formatOptions = {
    short: { 
      day: 'numeric', 
      month: 'short', 
      year: 'numeric' 
    },
    medium: { 
      day: 'numeric', 
      month: 'long', 
      year: 'numeric' 
    },
    long: { 
      weekday: 'long', 
      day: 'numeric', 
      month: 'long', 
      year: 'numeric' 
    },
    full: { 
      weekday: 'long', 
      day: 'numeric', 
      month: 'long', 
      year: 'numeric' 
    }
  };

  const baseOptions = formatOptions[format] || formatOptions.medium;
  
  if (includeTime) {
    baseOptions.hour = '2-digit';
    baseOptions.minute = '2-digit';
  }

  try {
    return dateObj.toLocaleDateString(locale, baseOptions);
  } catch (error) {
    console.error('Erreur lors du formatage de la date:', error);
    return dateObj.toLocaleDateString();
  }
};

/**
 * Formate une durée en format lisible (heures, minutes, secondes)
 * 
 * @param {number} seconds - Durée en secondes
 * @param {Object} options - Options de formatage
 * @param {boolean} options.short - Format court (h:m:s vs heures complètes)
 * @param {boolean} options.hideSeconds - Masquer les secondes
 * @returns {string} Durée formatée
 * 
 * @example
 * // Utilisation dans LessonPlayer pour afficher la durée de la vidéo
 * const videoDuration = formatDuration(course.duration, { short: true });
 * 
 * @example
 * // Utilisation dans CourseProgress pour le temps passé
 * const timeSpent = formatDuration(progress.timeSpent, { hideSeconds: true });
 */
export const formatDuration = (seconds, options = {}) => {
  const { short = false, hideSeconds = false } = options;
  
  if (typeof seconds !== 'number' || seconds < 0) {
    return short ? '0:00' : '0 minute';
  }

  const hours = Math.floor(seconds / 3600);
  const minutes = Math.floor((seconds % 3600) / 60);
  const secs = Math.floor(seconds % 60);

  if (short) {
    if (hours > 0) {
      return hideSeconds 
        ? `${hours}:${minutes.toString().padStart(2, '0')}`
        : `${hours}:${minutes.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
    }
    return hideSeconds 
      ? `${minutes}:${secs.toString().padStart(2, '0')}`
      : `${minutes}:${secs.toString().padStart(2, '0')}`;
  }

  const parts = [];
  if (hours > 0) parts.push(`${hours} heure${hours > 1 ? 's' : ''}`);
  if (minutes > 0) parts.push(`${minutes} minute${minutes > 1 ? 's' : ''}`);
  if (!hideSeconds && secs > 0) parts.push(`${secs} seconde${secs > 1 ? 's' : ''}`);

  return parts.length > 0 ? parts.join(' ') : '0 seconde';
};

/**
 * Formate un nombre avec séparateurs de milliers
 * 
 * @param {number} number - Nombre à formater
 * @param {Object} options - Options de formatage
 * @param {string} options.locale - Locale à utiliser
 * @param {number} options.decimals - Nombre de décimales
 * @param {string} options.currency - Code de devise pour formatage monétaire
 * @returns {string} Nombre formaté
 * 
 * @example
 * // Utilisation dans StatsCard pour afficher le nombre d'étudiants
 * const studentCount = formatNumber(course.studentsCount);
 * 
 * @example
 * // Utilisation pour le prix des cours
 * const coursePrice = formatNumber(course.price, { currency: 'EUR' });
 */
export const formatNumber = (number, options = {}) => {
  const {
    locale = 'fr-FR',
    decimals = 0,
    currency = null
  } = options;

  if (typeof number !== 'number' || isNaN(number)) {
    return '0';
  }

  const formatOptions = {
    minimumFractionDigits: decimals,
    maximumFractionDigits: decimals
  };

  if (currency) {
    formatOptions.style = 'currency';
    formatOptions.currency = currency;
  }

  try {
    return new Intl.NumberFormat(locale, formatOptions).format(number);
  } catch (error) {
    console.error('Erreur lors du formatage du nombre:', error);
    return number.toString();
  }
};

/**
 * Formate un pourcentage avec précision
 * 
 * @param {number} value - Valeur à convertir en pourcentage (0-1 ou 0-100)
 * @param {Object} options - Options de formatage
 * @param {boolean} options.isDecimal - Si la valeur est décimale (0-1) ou déjà en pourcentage (0-100)
 * @param {number} options.decimals - Nombre de décimales
 * @returns {string} Pourcentage formaté
 * 
 * @example
 * // Utilisation dans CourseProgress pour le pourcentage de completion
 * const completionRate = formatPercentage(progress.completion, { isDecimal: true });
 * 
 * @example
 * // Utilisation dans Achievement pour le taux de réussite
 * const successRate = formatPercentage(user.successRate, { decimals: 1 });
 */
export const formatPercentage = (value, options = {}) => {
  const { isDecimal = true, decimals = 0 } = options;
  
  if (typeof value !== 'number' || isNaN(value)) {
    return '0%';
  }

  const percentage = isDecimal ? value * 100 : value;
  const clampedPercentage = Math.max(0, Math.min(100, percentage));
  
  return `${clampedPercentage.toFixed(decimals)}%`;
};

/**
 * Formate un texte en tronquant avec des points de suspension
 * 
 * @param {string} text - Texte à tronquer
 * @param {number} maxLength - Longueur maximale
 * @param {Object} options - Options de formatage
 * @param {string} options.ellipsis - Caractère d'ellipse à utiliser
 * @param {boolean} options.wordBoundary - Respecter les limites de mots
 * @returns {string} Texte tronqué
 * 
 * @example
 * // Utilisation dans CourseCard pour la description
 * const shortDescription = truncateText(course.description, 150);
 * 
 * @example
 * // Utilisation dans NotificationPanel pour les messages
 * const shortMessage = truncateText(notification.message, 100, { wordBoundary: true });
 */
export const truncateText = (text, maxLength, options = {}) => {
  const { ellipsis = '...', wordBoundary = false } = options;
  
  if (typeof text !== 'string' || text.length <= maxLength) {
    return text || '';
  }

  if (wordBoundary) {
    const truncated = text.slice(0, maxLength);
    const lastSpace = truncated.lastIndexOf(' ');
    return lastSpace > 0 
      ? truncated.slice(0, lastSpace) + ellipsis
      : truncated + ellipsis;
  }

  return text.slice(0, maxLength) + ellipsis;
};

/**
 * Formate une taille de fichier en unités lisibles
 * 
 * @param {number} bytes - Taille en bytes
 * @param {Object} options - Options de formatage
 * @param {number} options.decimals - Nombre de décimales
 * @param {boolean} options.binary - Utiliser les unités binaires (1024) ou décimales (1000)
 * @returns {string} Taille formatée
 * 
 * @example
 * // Utilisation dans AvatarUpload pour la taille du fichier
 * const fileSize = formatFileSize(file.size);
 * 
 * @example
 * // Utilisation dans CodeEditor pour la taille du code
 * const codeSize = formatFileSize(codeContent.length, { decimals: 1 });
 */
export const formatFileSize = (bytes, options = {}) => {
  const { decimals = 1, binary = true } = options;
  
  if (typeof bytes !== 'number' || bytes < 0) {
    return '0 B';
  }

  const base = binary ? 1024 : 1000;
  const units = binary 
    ? ['B', 'KiB', 'MiB', 'GiB', 'TiB']
    : ['B', 'KB', 'MB', 'GB', 'TB'];

  if (bytes === 0) return '0 B';

  const i = Math.floor(Math.log(bytes) / Math.log(base));
  const size = bytes / Math.pow(base, i);
  
  return `${size.toFixed(decimals)} ${units[i]}`;
};

/**
 * Formate le nom d'un utilisateur (prénom + nom)
 * 
 * @param {Object} user - Objet utilisateur
 * @param {string} user.firstName - Prénom
 * @param {string} user.lastName - Nom
 * @param {Object} options - Options de formatage
 * @param {string} options.format - Format ('full', 'initials', 'firstLast', 'lastFirst')
 * @returns {string} Nom formaté
 * 
 * @example
 * // Utilisation dans Header pour afficher le nom de l'utilisateur
 * const userName = formatUserName(currentUser, { format: 'full' });
 * 
 * @example
 * // Utilisation dans CourseCard pour l'instructeur
 * const instructorName = formatUserName(course.instructor, { format: 'firstLast' });
 */
export const formatUserName = (user, options = {}) => {
  const { format = 'full' } = options;
  
  if (!user || typeof user !== 'object') {
    return 'Utilisateur anonyme';
  }

  const { firstName = '', lastName = '' } = user;
  
  switch (format) {
    case 'initials':
      return `${firstName.charAt(0)}${lastName.charAt(0)}`.toUpperCase();
    case 'firstLast':
      return `${firstName} ${lastName}`.trim();
    case 'lastFirst':
      return `${lastName}, ${firstName}`.trim();
    case 'full':
    default:
      return `${firstName} ${lastName}`.trim() || 'Utilisateur';
  }
};

/**
 * Formate un niveau de difficulté avec des indicateurs visuels
 * 
 * @param {string|number} level - Niveau de difficulté
 * @param {Object} options - Options de formatage
 * @param {boolean} options.showIcon - Afficher l'icône
 * @param {boolean} options.showColor - Retourner la couleur associée
 * @returns {string|Object} Niveau formaté ou objet avec niveau et couleur
 * 
 * @example
 * // Utilisation dans CourseCard pour afficher le niveau
 * const courseLevel = formatDifficultyLevel(course.level);
 * 
 * @example
 * // Utilisation dans LessonPlayer pour le niveau de la leçon
 * const lessonLevel = formatDifficultyLevel(lesson.difficulty, { showIcon: true });
 */
export const formatDifficultyLevel = (level, options = {}) => {
  const { showIcon = false, showColor = false } = options;
  
  const levelMap = {
    1: { text: 'Débutant', icon: '🟢', color: '#4CAF50' },
    2: { text: 'Intermédiaire', icon: '🟡', color: '#FF9800' },
    3: { text: 'Avancé', icon: '🔴', color: '#F44336' },
    beginner: { text: 'Débutant', icon: '🟢', color: '#4CAF50' },
    intermediate: { text: 'Intermédiaire', icon: '🟡', color: '#FF9800' },
    advanced: { text: 'Avancé', icon: '🔴', color: '#F44336' }
  };

  const levelData = levelMap[level] || levelMap[1];
  
  if (showColor) {
    return {
      text: showIcon ? `${levelData.icon} ${levelData.text}` : levelData.text,
      color: levelData.color
    };
  }
  
  return showIcon ? `${levelData.icon} ${levelData.text}` : levelData.text;
};

/**
 * Formate un score ou une note
 * 
 * @param {number} score - Score à formater
 * @param {number} maxScore - Score maximum
 * @param {Object} options - Options de formatage
 * @param {boolean} options.showPercentage - Afficher en pourcentage
 * @param {boolean} options.showFraction - Afficher sous forme de fraction
 * @returns {string} Score formaté
 * 
 * @example
 * // Utilisation dans Achievement pour les scores
 * const examScore = formatScore(user.examScore, 100, { showPercentage: true });
 * 
 * @example
 * // Utilisation dans Progress pour les quiz
 * const quizResult = formatScore(quiz.score, quiz.maxScore, { showFraction: true });
 */
export const formatScore = (score, maxScore, options = {}) => {
  const { showPercentage = false, showFraction = false } = options;
  
  if (typeof score !== 'number' || typeof maxScore !== 'number' || maxScore === 0) {
    return '0';
  }

  const clampedScore = Math.max(0, Math.min(maxScore, score));
  
  if (showPercentage) {
    const percentage = (clampedScore / maxScore) * 100;
    return `${percentage.toFixed(1)}%`;
  }
  
  if (showFraction) {
    return `${clampedScore}/${maxScore}`;
  }
  
  return clampedScore.toString();
};

/**
 * Formate une date relative (il y a X temps)
 * 
 * @param {Date|string|number} date - Date à formater
 * @param {Object} options - Options de formatage
 * @param {string} options.locale - Locale à utiliser
 * @param {boolean} options.short - Format court
 * @returns {string} Date relative formatée
 * 
 * @example
 * // Utilisation dans NotificationPanel pour l'heure des notifications
 * const notificationTime = formatRelativeTime(notification.createdAt);
 * 
 * @example
 * // Utilisation dans CourseCard pour la dernière mise à jour
 * const lastUpdate = formatRelativeTime(course.updatedAt, { short: true });
 */
export const formatRelativeTime = (date, options = {}) => {
  const { locale = 'fr-FR', short = false } = options;
  
  if (!date) return '';
  
  const dateObj = new Date(date);
  const now = new Date();
  const diffInSeconds = Math.floor((now - dateObj) / 1000);
  
  if (diffInSeconds < 60) {
    return short ? 'maintenant' : 'il y a quelques secondes';
  }
  
  const diffInMinutes = Math.floor(diffInSeconds / 60);
  if (diffInMinutes < 60) {
    return short ? `${diffInMinutes}m` : `il y a ${diffInMinutes} minute${diffInMinutes > 1 ? 's' : ''}`;
  }
  
  const diffInHours = Math.floor(diffInMinutes / 60);
  if (diffInHours < 24) {
    return short ? `${diffInHours}h` : `il y a ${diffInHours} heure${diffInHours > 1 ? 's' : ''}`;
  }
  
  const diffInDays = Math.floor(diffInHours / 24);
  if (diffInDays < 7) {
    return short ? `${diffInDays}j` : `il y a ${diffInDays} jour${diffInDays > 1 ? 's' : ''}`;
  }
  
  const diffInWeeks = Math.floor(diffInDays / 7);
  if (diffInWeeks < 4) {
    return short ? `${diffInWeeks}sem` : `il y a ${diffInWeeks} semaine${diffInWeeks > 1 ? 's' : ''}`;
  }
  
  // Pour les dates plus anciennes, retourner la date formatée
  return formatDate(dateObj, { format: 'short' });
};

/**
 * Formate les tags/étiquettes d'un cours
 * 
 * @param {Array} tags - Liste des tags
 * @param {Object} options - Options de formatage
 * @param {number} options.maxTags - Nombre maximum de tags à afficher
 * @param {string} options.separator - Séparateur entre les tags
 * @returns {string} Tags formatés
 * 
 * @example
 * // Utilisation dans CourseCard pour les tags du cours
 * const courseTags = formatTags(course.tags, { maxTags: 3 });
 * 
 * @example
 * // Utilisation dans CourseDetails pour tous les tags
 * const allTags = formatTags(course.tags, { separator: ' • ' });
 */
export const formatTags = (tags, options = {}) => {
  const { maxTags = 5, separator = ', ' } = options;
  
  if (!Array.isArray(tags) || tags.length === 0) {
    return '';
  }
  
  const displayTags = tags.slice(0, maxTags);
  const remainingCount = tags.length - maxTags;
  
  let formatted = displayTags.join(separator);
  
  if (remainingCount > 0) {
    formatted += ` (+${remainingCount} autre${remainingCount > 1 ? 's' : ''})`;
  }
  
  return formatted;
};

// Export par défaut pour faciliter l'importation
export default {
  formatDate,
  formatDuration,
  formatNumber,
  formatPercentage,
  truncateText,
  formatFileSize,
  formatUserName,
  formatDifficultyLevel,
  formatScore,
  formatRelativeTime,
  formatTags
};