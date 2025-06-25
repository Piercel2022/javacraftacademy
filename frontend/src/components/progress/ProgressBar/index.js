
/**
 * index.js - Point d'entrée du composant ProgressBar
 * 
 * Ce fichier sert de point d'entrée principal pour le composant ProgressBar
 * dans l'architecture de JavaCraft Academy. Il facilite les imports et 
 * peut exporter des utilitaires supplémentaires liés au composant.
 * 
 * Relations avec l'application :
 * - Permet l'import simplifié depuis d'autres composants
 * - Centralise les exports du composant et ses utilitaires
 * - Facilite la maintenance et les modifications futures
 * 
 * @fileoverview Point d'entrée du composant ProgressBar
 * @version 1.0.0
 * @author JavaCraft Academy Team
 */

// Import du composant principal
import ProgressBar from './ProgressBar.jsx';

// Import des utilitaires et hooks spécifiques au composant (si nécessaires)
// Ces imports peuvent être ajoutés selon les besoins futurs
// import { useProgressBar } from './hooks/useProgressBar';
// import { calculateProgress, formatProgressLabel } from './utils/progressUtils';

/**
 * Utilitaires pour le composant ProgressBar
 * Ces fonctions peuvent être utilisées par d'autres composants
 * pour interagir avec ou calculer des valeurs de progression
 */

/**
 * Calcule le pourcentage de progression entre deux valeurs
 * @param {number} current - Valeur actuelle
 * @param {number} total - Valeur totale
 * @param {number} precision - Nombre de décimales (défaut: 2)
 * @returns {number} Pourcentage de progression (0-100)
 * 
 * @example
 * calculateProgressPercentage(75, 100); // Returns 75
 * calculateProgressPercentage(1, 3, 1); // Returns 33.3
 */
export const calculateProgressPercentage = (current, total, precision = 2) => {
  if (total === 0 || total < 0 || current < 0) {
    return 0;
  }
  
  const percentage = (current / total) * 100;
  return Math.min(100, Math.max(0, Number(percentage.toFixed(precision))));
};

/**
 * Formate le label de progression avec différents formats disponibles
 * @param {number} current - Valeur actuelle
 * @param {number} total - Valeur totale
 * @param {string} format - Format d'affichage ('percentage', 'fraction', 'both')
 * @returns {string} Label formaté
 * 
 * @example
 * formatProgressLabel(75, 100, 'percentage'); // Returns "75%"
 * formatProgressLabel(3, 10, 'fraction'); // Returns "3/10"
 * formatProgressLabel(5, 8, 'both'); // Returns "5/8 (62.5%)"
 */
export const formatProgressLabel = (current, total, format = 'percentage') => {
  const percentage = calculateProgressPercentage(current, total);
  
  switch (format) {
    case 'percentage':
      return `${percentage}%`;
    case 'fraction':
      return `${current}/${total}`;
    case 'both':
      return `${current}/${total} (${percentage}%)`;
    default:
      return `${percentage}%`;
  }
};

/**
 * Détermine la couleur de la barre de progression selon le pourcentage
 * @param {number} percentage - Pourcentage de progression (0-100)
 * @param {Object} colorThresholds - Seuils de couleur personnalisés
 * @returns {string} Classe CSS ou couleur correspondante
 * 
 * @example
 * getProgressColor(25); // Returns 'danger'
 * getProgressColor(60); // Returns 'warning'
 * getProgressColor(85); // Returns 'success'
 */
export const getProgressColor = (percentage, colorThresholds = {}) => {
  const thresholds = {
    danger: 33,
    warning: 66,
    success: 100,
    ...colorThresholds
  };
  
  if (percentage < thresholds.danger) {
    return 'danger';
  } else if (percentage < thresholds.warning) {
    return 'warning';
  } else {
    return 'success';
  }
};

/**
 * Valide les props du composant ProgressBar
 * @param {Object} props - Props à valider
 * @returns {Object} Objet contenant isValid et errors
 * 
 * @example
 * validateProgressProps({ value: 50, max: 100 }); 
 * // Returns { isValid: true, errors: [] }
 */
export const validateProgressProps = (props) => {
  const errors = [];
  const { value, max, min = 0 } = props;
  
  if (typeof value !== 'number' || isNaN(value)) {
    errors.push('La valeur doit être un nombre valide');
  }
  
  if (typeof max !== 'number' || isNaN(max) || max <= 0) {
    errors.push('La valeur maximale doit être un nombre positif');
  }
  
  if (typeof min !== 'number' || isNaN(min) || min < 0) {
    errors.push('La valeur minimale doit être un nombre positif ou zéro');
  }
  
  if (value < min) {
    errors.push('La valeur ne peut pas être inférieure au minimum');
  }
  
  if (value > max) {
    errors.push('La valeur ne peut pas être supérieure au maximum');
  }
  
  if (min >= max) {
    errors.push('La valeur minimale doit être inférieure à la valeur maximale');
  }
  
  return {
    isValid: errors.length === 0,
    errors
  };
};

/**
 * Crée des étapes de progression pour une barre de progression segmentée
 * @param {number} totalSteps - Nombre total d'étapes
 * @param {number} currentStep - Étape actuelle (0-based)
 * @returns {Array} Tableau d'objets représentant les étapes
 * 
 * @example
 * createProgressSteps(5, 2);
 * // Returns array with 5 steps, first 3 completed
 */
export const createProgressSteps = (totalSteps, currentStep) => {
  return Array.from({ length: totalSteps }, (_, index) => ({
    id: index,
    completed: index <= currentStep,
    active: index === currentStep,
    label: `Étape ${index + 1}`
  }));
};

/**
 * Animation helpers pour les transitions de progression
 */
export const progressAnimations = {
  /**
   * Configuration d'animation par défaut
   */
  defaultConfig: {
    duration: 300,
    easing: 'ease-out',
    delay: 0
  },
  
  /**
   * Génère les styles d'animation CSS
   * @param {number} duration - Durée en millisecondes
   * @param {string} easing - Fonction d'easing
   * @returns {Object} Styles CSS
   */
  getTransitionStyles: (duration = 300, easing = 'ease-out') => ({
    transition: `all ${duration}ms ${easing}`,
    willChange: 'width, background-color'
  })
};

/**
 * Constantes utiles pour le composant ProgressBar
 */
export const PROGRESS_CONSTANTS = {
  DEFAULT_MAX: 100,
  DEFAULT_MIN: 0,
  DEFAULT_PRECISION: 2,
  ANIMATION_DURATION: 300,
  COLOR_THEMES: {
    default: {
      danger: '#dc3545',
      warning: '#ffc107',
      success: '#28a745',
      info: '#17a2b8'
    },
    dark: {
      danger: '#e74c3c',
      warning: '#f39c12',
      success: '#2ecc71',
      info: '#3498db'
    }
  }
};

// Export principal du composant
export default ProgressBar;

// Export nommé pour compatibilité
export { ProgressBar };

/**
 * Export groupé de tous les utilitaires
 * Pratique pour importer tous les utilitaires en une fois
 */
export const ProgressBarUtils = {
  calculateProgressPercentage,
  formatProgressLabel,
  getProgressColor,
  validateProgressProps,
  createProgressSteps,
  progressAnimations,
  PROGRESS_CONSTANTS
};