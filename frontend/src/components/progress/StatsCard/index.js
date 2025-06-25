
/**
 * Point d'entrée pour le composant StatsCard
 * 
 * Ce fichier centralise les exports du composant StatsCard et de ses utilitaires
 * pour faciliter l'importation dans d'autres parties de l'application.
 * 
 * Architecture de l'export :
 * - Export par défaut : Composant StatsCard principal
 * - Exports nommés : Utilitaires et types connexes
 * 
 * Relations avec l'application :
 * - Importé dans Dashboard.jsx : `import StatsCard from '@/components/progress/StatsCard'`
 * - Importé dans Progress.jsx : `import { StatsCard, formatters } from '@/components/progress/StatsCard'`
 * - Utilisé dans UserProfile.jsx pour afficher les statistiques utilisateur
 * - Intégré dans les layouts de reporting et analytics
 * 
 * @fileoverview Export principal du composant StatsCard
 * @author JavaCraft Academy
 * @version 1.0.0
 */

import StatsCard from './StatsCard';

// Réexport du composant principal
export default StatsCard;

// Export nommé pour la compatibilité
export { default as StatsCard } from './StatsCard';

/**
 * Formateurs prédéfinis pour les valeurs courantes de JavaCraft Academy
 * 
 * Ces formateurs standardisent l'affichage des métriques les plus courantes
 * de la plateforme d'apprentissage.
 */
export const formatters = {
  /**
   * Formate le temps en heures et minutes
   * @param {number} minutes - Nombre de minutes
   * @returns {string} Temps formaté (ex: "2h 30m")
   */
  duration: (minutes) => {
    if (minutes < 60) {
      return `${minutes}m`;
    }
    const hours = Math.floor(minutes / 60);
    const remainingMinutes = minutes % 60;
    return remainingMinutes > 0 ? `${hours}h ${remainingMinutes}m` : `${hours}h`;
  },

  /**
   * Formate un pourcentage avec une décimale
   * @param {number} value - Valeur entre 0 et 100
   * @returns {string} Pourcentage formaté (ex: "85.5%")
   */
  percentage: (value) => {
    return `${value.toFixed(1)}%`;
  },

  /**
   * Formate un nombre avec séparateurs de milliers
   * @param {number} value - Nombre à formater
   * @returns {string} Nombre formaté (ex: "1,234")
   */
  number: (value) => {
    return value.toLocaleString('fr-FR');
  },

  /**
   * Formate un score avec notation sur 100
   * @param {number} score - Score entre 0 et 100
   * @returns {string} Score formaté (ex: "85/100")
   */
  score: (score) => {
    return `${Math.round(score)}/100`;
  },

  /**
   * Formate un niveau avec préfixe
   * @param {number} level - Niveau numérique
   * @returns {string} Niveau formaté (ex: "Niveau 5")
   */
  level: (level) => {
    return `Niveau ${level}`;
  },

  /**
   * Formate les points d'expérience avec abréviation
   * @param {number} xp - Points d'expérience
   * @returns {string} XP formaté (ex: "1.2K XP")
   */
  experience: (xp) => {
    if (xp >= 1000000) {
      return `${(xp / 1000000).toFixed(1)}M XP`;
    } else if (xp >= 1000) {
      return `${(xp / 1000).toFixed(1)}K XP`;
    }
    return `${xp} XP`;
  },

  /**
   * Formate une streak (série de jours consécutifs)
   * @param {number} days - Nombre de jours
   * @returns {string} Streak formatée (ex: "15 jours")
   */
  streak: (days) => {
    return days === 1 ? '1 jour' : `${days} jours`;
  },

  /**
   * Formate une date relative (il y a X jours)
   * @param {Date|string} date - Date à formater
   * @returns {string} Date relative (ex: "il y a 3 jours")
   */
  relativeDate: (date) => {
    const now = new Date();
    const targetDate = new Date(date);
    const diffTime = Math.abs(now - targetDate);
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    
    if (diffDays === 0) return "Aujourd'hui";
    if (diffDays === 1) return "Hier";
    if (diffDays < 7) return `il y a ${diffDays} jours`;
    if (diffDays < 30) {
      const weeks = Math.floor(diffDays / 7);
      return weeks === 1 ? 'il y a 1 semaine' : `il y a ${weeks} semaines`;
    }
    if (diffDays < 365) {
      const months = Math.floor(diffDays / 30);
      return months === 1 ? 'il y a 1 mois' : `il y a ${months} mois`;
    }
    const years = Math.floor(diffDays / 365);
    return years === 1 ? 'il y a 1 an' : `il y a ${years} ans`;
  },

  /**
   * Formate une devise en euros
   * @param {number} amount - Montant en euros
   * @returns {string} Montant formaté (ex: "29,99 €")
   */
  currency: (amount) => {
    return `${amount.toFixed(2).replace('.', ',')} €`;
  },

  /**
   * Formate un rang ou classement
   * @param {number} rank - Position dans le classement
   * @returns {string} Rang formaté (ex: "3ème")
   */
  rank: (rank) => {
    if (rank === 1) return '1er';
    return `${rank}ème`;
  }
};

/**
 * Types prédéfinis de statistiques pour JavaCraft Academy
 * 
 * Ces constantes facilitent la création de StatsCard avec des configurations
 * cohérentes pour les différents types de métriques.
 */
export const statTypes = {
  // Métriques de progression
  PROGRESS: {
    icon: 'TrendingUp',
    color: 'blue',
    formatter: formatters.percentage
  },
  
  // Métriques de temps
  TIME_SPENT: {
    icon: 'Clock',
    color: 'green',
    formatter: formatters.duration
  },
  
  // Métriques d'expérience
  EXPERIENCE: {
    icon: 'Star',
    color: 'yellow',
    formatter: formatters.experience
  },
  
  // Métriques de niveau
  LEVEL: {
    icon: 'Award',
    color: 'purple',
    formatter: formatters.level
  },
  
  // Métriques de streak
  STREAK: {
    icon: 'Flame',
    color: 'orange',
    formatter: formatters.streak
  },
  
  // Métriques de score
  SCORE: {
    icon: 'Target',
    color: 'red',
    formatter: formatters.score
  },
  
  // Métriques numériques générales
  COUNT: {
    icon: 'Hash',
    color: 'gray',
    formatter: formatters.number
  },
  
  // Métriques de classement
  RANK: {
    icon: 'Trophy',
    color: 'gold',
    formatter: formatters.rank
  }
};

/**
 * Fonction utilitaire pour créer rapidement une StatsCard avec des paramètres prédéfinis
 * 
 * @param {string} type - Type de statistique (clé de statTypes)
 * @param {number} value - Valeur à afficher
 * @param {string} title - Titre de la carte
 * @param {Object} overrides - Propriétés à surcharger
 * @returns {Object} Props pour le composant StatsCard
 */
export const createStatCard = (type, value, title, overrides = {}) => {
  const statType = statTypes[type];
  if (!statType) {
    console.warn(`Type de statistique inconnu: ${type}`);
    return { value, title, ...overrides };
  }
  
  return {
    value: statType.formatter ? statType.formatter(value) : value,
    title,
    icon: statType.icon,
    color: statType.color,
    ...overrides
  };
};

/**
 * Constantes pour les couleurs de thème
 */
export const colors = {
  blue: '#3b82f6',
  green: '#10b981',
  yellow: '#f59e0b',
  purple: '#8b5cf6',
  orange: '#f97316',
  red: '#ef4444',
  gray: '#6b7280',
  gold: '#fbbf24'
};