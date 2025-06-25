
/**
 * @fileoverview Point d'entrée pour le composant Achievement
 * 
 * Ce fichier sert de point d'entrée principal pour le composant Achievement.
 * Il exporte le composant principal ainsi que tous les sous-composants,
 * utilitaires et types associés pour faciliter l'importation dans d'autres
 * parties de l'application.
 * 
 * Usage:
 * ```javascript
 * // Import du composant principal
 * import Achievement from './components/Achievement';
 * 
 * // Import avec destructuring
 * import { Achievement, AchievementCard, achievementUtils } from './components/Achievement';
 * ```
 * 
 * @author JavaCraft Academy Team
 * @version 1.0.0
 * @since 2024-01-01
 */

// Import du composant principal
import Achievement from './Achievement';

// Import des sous-composants (si vous les créez séparément)
import AchievementCard from './components/AchievementCard';
import AchievementStats from './components/AchievementStats';
import AchievementFilters from './components/AchievementFilters';

// Import des utilitaires
import * as achievementUtils from './utils/achievementUtils';
import * as achievementTypes from './types/achievementTypes';
import * as achievementConstants from './constants/achievementConstants';

// Import des hooks personnalisés
import useAchievements from './hooks/useAchievements';
import useAchievementFilters from './hooks/useAchievementFilters';
import useAchievementProgress from './hooks/useAchievementProgress';

// Import des styles
import './Achievement.module.css';

/**
 * Composant principal Achievement avec toutes ses dépendances
 * @type {React.ComponentType}
 */
export default Achievement;

/**
 * Export nommé du composant principal pour plus de flexibilité
 * @type {React.ComponentType}
 */
export { Achievement };

/**
 * Export des sous-composants
 */
export {
  AchievementCard,
  AchievementStats,
  AchievementFilters
};

/**
 * Export des hooks personnalisés
 */
export {
  useAchievements,
  useAchievementFilters,
  useAchievementProgress
};

/**
 * Export des utilitaires
 */
export {
  achievementUtils,
  achievementTypes,
  achievementConstants
};

/**
 * Export des types pour TypeScript (si utilisé)
 */
export type {
  AchievementType,
  AchievementStatus,
  AchievementRarity,
  AchievementCategory,
  AchievementProgress,
  AchievementFilters as AchievementFiltersType
} from './types/achievementTypes';

/**
 * Configuration par défaut du composant
 */
export const defaultAchievementConfig = {
  // Configuration des filtres
  filters: {
    showLocked: true,
    showUnlocked: true,
    categories: 'all',
    rarity: 'all',
    sortBy: 'name',
    sortOrder: 'asc'
  },
  
  // Configuration de l'affichage
  display: {
    itemsPerPage: 12,
    gridColumns: 'auto',
    showProgress: true,
    showStats: true,
    showSearch: true
  },
  
  // Configuration des animations
  animations: {
    enabled: true,
    duration: 300,
    easing: 'ease-out'
  },
  
  // Configuration des notifications
  notifications: {
    showUnlockNotification: true,
    notificationDuration: 5000,
    playSound: false
  }
};

/**
 * Fonction utilitaire pour créer une instance du composant avec configuration personnalisée
 * @param {Object} config - Configuration personnalisée
 * @returns {React.ComponentType} Composant Achievement configuré
 */
export const createAchievementComponent = (config = {}) => {
  const mergedConfig = {
    ...defaultAchievementConfig,
    ...config
  };
  
  return (props) => (
    <Achievement 
      {...mergedConfig} 
      {...props} 
    />
  );
};

/**
 * Fonction pour valider les données d'achievements
 * @param {Array} achievements - Tableau des achievements à valider
 * @returns {Object} Résultat de la validation
 */
export const validateAchievements = (achievements) => {
  const errors = [];
  const warnings = [];
  
  if (!Array.isArray(achievements)) {
    errors.push('Les achievements doivent être un tableau');
    return { isValid: false, errors, warnings };
  }
  
  achievements.forEach((achievement, index) => {
    // Validation des champs obligatoires
    if (!achievement.id) {
      errors.push(`Achievement à l'index ${index}: ID manquant`);
    }
    
    if (!achievement.name) {
      errors.push(`Achievement à l'index ${index}: Nom manquant`);
    }
    
    if (!achievement.description) {
      warnings.push(`Achievement à l'index ${index}: Description manquante`);
    }
    
    // Validation des types
    if (achievement.points && typeof achievement.points !== 'number') {
      errors.push(`Achievement à l'index ${index}: Les points doivent être un nombre`);
    }
    
    if (achievement.rarity && !['common', 'rare', 'epic', 'legendary'].includes(achievement.rarity)) {
      warnings.push(`Achievement à l'index ${index}: Rareté invalide`);
    }
    
    // Validation des dates
    if (achievement.unlockedAt && !(achievement.unlockedAt instanceof Date) && isNaN(Date.parse(achievement.unlockedAt))) {
      errors.push(`Achievement à l'index ${index}: Date de déblocage invalide`);
    }
  });
  
  return {
    isValid: errors.length === 0,
    errors,
    warnings
  };
};

/**
 * Fonction pour transformer les données d'achievements depuis une API
 * @param {Array} rawData - Données brutes depuis l'API
 * @returns {Array} Données transformées pour le composant
 */
export const transformAchievementData = (rawData) => {
  if (!Array.isArray(rawData)) {
    console.warn('transformAchievementData: Les données doivent être un tableau');
    return [];
  }
  
  return rawData.map(item => ({
    id: item.id || item._id || `achievement_${Date.now()}_${Math.random()}`,
    name: item.name || item.title || 'Achievement sans nom',
    description: item.description || item.desc || '',
    icon: item.icon || item.emoji || '🏆',
    points: parseInt(item.points || item.score || 0),
    rarity: item.rarity || item.level || 'common',
    category: item.category || item.type || 'general',
    isUnlocked: Boolean(item.isUnlocked || item.unlocked || item.achieved),
    unlockedAt: item.unlockedAt ? new Date(item.unlockedAt) : null,
    progress: {
      current: parseInt(item.progress?.current || item.currentProgress || 0),
      total: parseInt(item.progress?.total || item.totalProgress || item.maxProgress || 1)
    },
    requirements: item.requirements || item.conditions || [],
    tags: Array.isArray(item.tags) ? item.tags : [],
    createdAt: item.createdAt ? new Date(item.createdAt) : new Date(),
    updatedAt: item.updatedAt ? new Date(item.updatedAt) : new Date()
  }));
};

/**
 * Hook personnalisé pour l'intégration avec des APIs externes
 * @param {string} apiUrl - URL de l'API
 * @param {Object} options - Options pour la requête
 * @returns {Object} État de la requête et données
 */
export const useAchievementAPI = (apiUrl, options = {}) => {
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [achievements, setAchievements] = useState([]);
  
  useEffect(() => {
    const fetchAchievements = async () => {
      try {
        setLoading(true);
        setError(null);
        
        const response = await fetch(apiUrl, {
          method: 'GET',
          headers: {
            'Content-Type': 'application/json',
            ...options.headers
          },
          ...options
        });
        
        if (!response.ok) {
          throw new Error(`Erreur HTTP: ${response.status}`);
        }
        
        const data = await response.json();
        const transformedData = transformAchievementData(data);
        const validation = validateAchievements(transformedData);
        
        if (!validation.isValid) {
          console.warn('Données d\'achievements invalides:', validation.errors);
        }
        
        if (validation.warnings.length > 0) {
          console.warn('Avertissements:', validation.warnings);
        }
        
        setAchievements(transformedData);
      } catch (err) {
        setError(err.message);
        console.error('Erreur lors du chargement des achievements:', err);
      } finally {
        setLoading(false);
      }
    };
    
    if (apiUrl) {
      fetchAchievements();
    }
  }, [apiUrl, JSON.stringify(options)]);
  
  return { loading, error, achievements, refetch: () => fetchAchievements() };
};

/**
 * Constantes pour les événements personnalisés
 */
export const ACHIEVEMENT_EVENTS = {
  UNLOCKED: 'achievement:unlocked',
  PROGRESS_UPDATED: 'achievement:progress:updated',
  FILTER_CHANGED: 'achievement:filter:changed',
  LOADED: 'achievement:loaded',
  ERROR: 'achievement:error'
};

/**
 * Version du composant
 */
export const VERSION = '1.0.0';

/**
 * Informations sur le package
 */
export const PACKAGE_INFO = {
  name: '@javacraft/achievement-component',
  version: VERSION,
  description: 'Composant React pour afficher et gérer les achievements',
  author: 'JavaCraft Academy Team',
  license: 'MIT'
};