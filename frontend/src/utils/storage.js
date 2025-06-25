/**
 * @fileoverview Utilitaires de stockage pour JavaCraft Academy
 * 
 * Ce module fournit une interface unifiée pour gérer le stockage des données
 * de l'application, incluant localStorage, sessionStorage et la gestion
 * des erreurs de stockage. Il est utilisé par tous les composants et hooks
 * qui nécessitent une persistance des données côté client.
 * 
 * Relations avec l'application :
 * - Utilisé par useAuth pour persister les tokens d'authentification
 * - Utilisé par useCourse pour le cache des données de cours
 * - Utilisé par useProgress pour sauvegarder la progression utilisateur
 * - Utilisé par useLocalStorage hook comme couche d'abstraction
 * - Utilisé par ThemeContext pour persister les préférences de thème
 * - Utilisé par les composants pour sauvegarder les états locaux
 * 
 * @author JavaCraft Academy Team
 * @version 1.0.0
 * @since 2024
 */

/**
 * Types de stockage disponibles
 * @readonly
 * @enum {string}
 */
export const STORAGE_TYPES = {
  LOCAL: 'localStorage',
  SESSION: 'sessionStorage'
};

/**
 * Clés de stockage standardisées pour l'application
 * @readonly
 * @enum {string}
 */
export const STORAGE_KEYS = {
  // Authentification
  AUTH_TOKEN: 'javacraft_auth_token',
  REFRESH_TOKEN: 'javacraft_refresh_token',
  USER_DATA: 'javacraft_user_data',
  
  // Cours et progression
  COURSE_PROGRESS: 'javacraft_course_progress',
  LESSON_STATE: 'javacraft_lesson_state',
  CODE_DRAFTS: 'javacraft_code_drafts',
  BOOKMARK_COURSES: 'javacraft_bookmarks',
  
  // Préférences utilisateur
  THEME_PREFERENCE: 'javacraft_theme',
  LANGUAGE_PREFERENCE: 'javacraft_language',
  EDITOR_SETTINGS: 'javacraft_editor_settings',
  NOTIFICATION_SETTINGS: 'javacraft_notifications',
  
  // Cache et données temporaires
  COURSE_CACHE: 'javacraft_course_cache',
  SEARCH_HISTORY: 'javacraft_search_history',
  RECENT_ACTIVITIES: 'javacraft_recent_activities',
  
  // Configuration
  APP_CONFIG: 'javacraft_app_config',
  FEATURE_FLAGS: 'javacraft_feature_flags'
};

/**
 * Configuration par défaut pour le stockage
 * @readonly
 * @type {Object}
 */
const DEFAULT_CONFIG = {
  prefix: 'javacraft_',
  enableEncryption: false,
  enableCompression: false,
  maxAge: 30 * 24 * 60 * 60 * 1000, // 30 jours en millisecondes
  version: '1.0.0'
};

/**
 * Classe principale pour la gestion du stockage
 * Fournit une interface unifiée pour localStorage et sessionStorage
 * avec gestion des erreurs, expiration et sérialisation
 */
class StorageManager {
  /**
   * Constructeur du gestionnaire de stockage
   * @param {Object} config - Configuration du stockage
   * @param {string} [config.prefix='javacraft_'] - Préfixe pour les clés
   * @param {boolean} [config.enableEncryption=false] - Activer le chiffrement
   * @param {boolean} [config.enableCompression=false] - Activer la compression
   * @param {number} [config.maxAge=2592000000] - Durée de vie par défaut (30 jours)
   */
  constructor(config = {}) {
    this.config = { ...DEFAULT_CONFIG, ...config };
    this.isSupported = this._checkStorageSupport();
  }

  /**
   * Vérifie si le stockage est supporté par le navigateur
   * @private
   * @returns {boolean} True si le stockage est supporté
   */
  _checkStorageSupport() {
    try {
      const testKey = '__storage_test__';
      localStorage.setItem(testKey, 'test');
      localStorage.removeItem(testKey);
      return true;
    } catch (error) {
      console.warn('Storage not supported:', error);
      return false;
    }
  }

  /**
   * Obtient l'interface de stockage appropriée
   * @private
   * @param {string} type - Type de stockage (LOCAL ou SESSION)
   * @returns {Storage} Interface de stockage
   * @throws {Error} Si le type de stockage n'est pas supporté
   */
  _getStorage(type = STORAGE_TYPES.LOCAL) {
    if (!this.isSupported) {
      throw new Error('Storage not supported in this browser');
    }

    switch (type) {
      case STORAGE_TYPES.LOCAL:
        return localStorage;
      case STORAGE_TYPES.SESSION:
        return sessionStorage;
      default:
        throw new Error(`Unsupported storage type: ${type}`);
    }
  }

  /**
   * Génère une clé complète avec préfixe
   * @private
   * @param {string} key - Clé de base
   * @returns {string} Clé avec préfixe
   */
  _getKey(key) {
    return `${this.config.prefix}${key}`;
  }

  /**
   * Sérialise une valeur pour le stockage
   * @private
   * @param {*} value - Valeur à sérialiser
   * @param {Object} options - Options de sérialisation
   * @returns {string} Valeur sérialisée
   */
  _serialize(value, options = {}) {
    const data = {
      value,
      timestamp: Date.now(),
      version: this.config.version,
      maxAge: options.maxAge || this.config.maxAge
    };

    let serialized = JSON.stringify(data);

    // Compression future (placeholder)
    if (this.config.enableCompression) {
      // TODO: Implémenter la compression
    }

    // Chiffrement future (placeholder)
    if (this.config.enableEncryption) {
      // TODO: Implémenter le chiffrement
    }

    return serialized;
  }

  /**
   * Désérialise une valeur du stockage
   * @private
   * @param {string} serialized - Valeur sérialisée
   * @returns {*} Valeur désérialisée ou null si expirée/invalide
   */
  _deserialize(serialized) {
    try {
      // Déchiffrement future (placeholder)
      if (this.config.enableEncryption) {
        // TODO: Implémenter le déchiffrement
      }

      // Décompression future (placeholder)
      if (this.config.enableCompression) {
        // TODO: Implémenter la décompression
      }

      const data = JSON.parse(serialized);

      // Vérification de l'expiration
      if (data.maxAge && data.timestamp) {
        const now = Date.now();
        const expirationTime = data.timestamp + data.maxAge;
        
        if (now > expirationTime) {
          return null; // Données expirées
        }
      }

      return data.value;
    } catch (error) {
      console.warn('Failed to deserialize storage data:', error);
      return null;
    }
  }

  /**
   * Stocke une valeur
   * @param {string} key - Clé de stockage
   * @param {*} value - Valeur à stocker
   * @param {Object} [options={}] - Options de stockage
   * @param {string} [options.type=LOCAL] - Type de stockage
   * @param {number} [options.maxAge] - Durée de vie personnalisée
   * @returns {boolean} True si le stockage a réussi
   * 
   * @example
   * // Stockage simple
   * storage.set('user_preference', { theme: 'dark' });
   * 
   * // Stockage avec expiration
   * storage.set('session_data', data, { maxAge: 3600000 }); // 1 heure
   * 
   * // Stockage en session
   * storage.set('temp_data', data, { type: STORAGE_TYPES.SESSION });
   */
  set(key, value, options = {}) {
    try {
      const storage = this._getStorage(options.type);
      const fullKey = this._getKey(key);
      const serialized = this._serialize(value, options);
      
      storage.setItem(fullKey, serialized);
      return true;
    } catch (error) {
      console.error('Failed to set storage item:', error);
      return false;
    }
  }

  /**
   * Récupère une valeur stockée
   * @param {string} key - Clé de stockage
   * @param {*} [defaultValue=null] - Valeur par défaut si non trouvée
   * @param {Object} [options={}] - Options de récupération
   * @param {string} [options.type=LOCAL] - Type de stockage
   * @returns {*} Valeur stockée ou valeur par défaut
   * 
   * @example
   * // Récupération simple
   * const theme = storage.get('theme_preference', 'light');
   * 
   * // Récupération depuis la session
   * const sessionData = storage.get('temp_data', null, { type: STORAGE_TYPES.SESSION });
   */
  get(key, defaultValue = null, options = {}) {
    try {
      const storage = this._getStorage(options.type);
      const fullKey = this._getKey(key);
      const serialized = storage.getItem(fullKey);
      
      if (serialized === null) {
        return defaultValue;
      }

      const value = this._deserialize(serialized);
      
      // Si les données sont expirées, les supprimer et retourner la valeur par défaut
      if (value === null) {
        this.remove(key, options);
        return defaultValue;
      }

      return value;
    } catch (error) {
      console.error('Failed to get storage item:', error);
      return defaultValue;
    }
  }

  /**
   * Supprime une valeur stockée
   * @param {string} key - Clé de stockage
   * @param {Object} [options={}] - Options de suppression
   * @param {string} [options.type=LOCAL] - Type de stockage
   * @returns {boolean} True si la suppression a réussi
   * 
   * @example
   * // Suppression simple
   * storage.remove('user_data');
   * 
   * // Suppression en session
   * storage.remove('temp_data', { type: STORAGE_TYPES.SESSION });
   */
  remove(key, options = {}) {
    try {
      const storage = this._getStorage(options.type);
      const fullKey = this._getKey(key);
      storage.removeItem(fullKey);
      return true;
    } catch (error) {
      console.error('Failed to remove storage item:', error);
      return false;
    }
  }

  /**
   * Vérifie si une clé existe dans le stockage
   * @param {string} key - Clé à vérifier
   * @param {Object} [options={}] - Options de vérification
   * @param {string} [options.type=LOCAL] - Type de stockage
   * @returns {boolean} True si la clé existe
   * 
   * @example
   * if (storage.has('auth_token')) {
   *   // L'utilisateur est authentifié
   * }
   */
  has(key, options = {}) {
    try {
      const storage = this._getStorage(options.type);
      const fullKey = this._getKey(key);
      return storage.getItem(fullKey) !== null;
    } catch (error) {
      console.error('Failed to check storage item:', error);
      return false;
    }
  }

  /**
   * Vide tout le stockage de l'application
   * @param {Object} [options={}] - Options de vidage
   * @param {string} [options.type=LOCAL] - Type de stockage
   * @returns {boolean} True si le vidage a réussi
   * 
   * @example
   * // Vider le localStorage de l'application
   * storage.clear();
   * 
   * // Vider le sessionStorage de l'application
   * storage.clear({ type: STORAGE_TYPES.SESSION });
   */
  clear(options = {}) {
    try {
      const storage = this._getStorage(options.type);
      const keys = [];
      
      // Collecter toutes les clés de l'application
      for (let i = 0; i < storage.length; i++) {
        const key = storage.key(i);
        if (key && key.startsWith(this.config.prefix)) {
          keys.push(key);
        }
      }
      
      // Supprimer toutes les clés de l'application
      keys.forEach(key => storage.removeItem(key));
      return true;
    } catch (error) {
      console.error('Failed to clear storage:', error);
      return false;
    }
  }

  /**
   * Obtient toutes les clés de l'application
   * @param {Object} [options={}] - Options de récupération
   * @param {string} [options.type=LOCAL] - Type de stockage
   * @returns {string[]} Tableau des clés (sans préfixe)
   * 
   * @example
   * const keys = storage.keys();
   * console.log('Clés stockées:', keys);
   */
  keys(options = {}) {
    try {
      const storage = this._getStorage(options.type);
      const keys = [];
      
      for (let i = 0; i < storage.length; i++) {
        const key = storage.key(i);
        if (key && key.startsWith(this.config.prefix)) {
          keys.push(key.substring(this.config.prefix.length));
        }
      }
      
      return keys;
    } catch (error) {
      console.error('Failed to get storage keys:', error);
      return [];
    }
  }

  /**
   * Obtient la taille approximative du stockage de l'application
   * @param {Object} [options={}] - Options de calcul
   * @param {string} [options.type=LOCAL] - Type de stockage
   * @returns {number} Taille en octets
   * 
   * @example
   * const size = storage.size();
   * console.log(`Utilisation du stockage: ${size} octets`);
   */
  size(options = {}) {
    try {
      const storage = this._getStorage(options.type);
      let totalSize = 0;
      
      for (let i = 0; i < storage.length; i++) {
        const key = storage.key(i);
        if (key && key.startsWith(this.config.prefix)) {
          const value = storage.getItem(key);
          totalSize += key.length + (value ? value.length : 0);
        }
      }
      
      return totalSize;
    } catch (error) {
      console.error('Failed to calculate storage size:', error);
      return 0;
    }
  }

  /**
   * Nettoie les données expirées
   * @param {Object} [options={}] - Options de nettoyage
   * @param {string} [options.type=LOCAL] - Type de stockage
   * @returns {number} Nombre d'éléments supprimés
   * 
   * @example
   * const cleaned = storage.cleanup();
   * console.log(`${cleaned} éléments expirés supprimés`);
   */
  cleanup(options = {}) {
    try {
      const storage = this._getStorage(options.type);
      const keysToRemove = [];
      
      for (let i = 0; i < storage.length; i++) {
        const key = storage.key(i);
        if (key && key.startsWith(this.config.prefix)) {
          const serialized = storage.getItem(key);
          if (serialized && this._deserialize(serialized) === null) {
            keysToRemove.push(key);
          }
        }
      }
      
      keysToRemove.forEach(key => storage.removeItem(key));
      return keysToRemove.length;
    } catch (error) {
      console.error('Failed to cleanup storage:', error);
      return 0;
    }
  }
}

// Instance globale du gestionnaire de stockage
const storage = new StorageManager();

/**
 * Fonctions utilitaires rapides pour les opérations courantes
 */

/**
 * Stocke les données d'authentification
 * @param {Object} authData - Données d'authentification
 * @param {string} authData.token - Token d'accès
 * @param {string} authData.refreshToken - Token de rafraîchissement
 * @param {Object} authData.user - Données utilisateur
 * @returns {boolean} True si le stockage a réussi
 */
export const setAuthData = (authData) => {
  const success = storage.set(STORAGE_KEYS.AUTH_TOKEN, authData.token) &&
                 storage.set(STORAGE_KEYS.REFRESH_TOKEN, authData.refreshToken) &&
                 storage.set(STORAGE_KEYS.USER_DATA, authData.user);
  return success;
};

/**
 * Récupère les données d'authentification
 * @returns {Object|null} Données d'authentification ou null
 */
export const getAuthData = () => {
  const token = storage.get(STORAGE_KEYS.AUTH_TOKEN);
  const refreshToken = storage.get(STORAGE_KEYS.REFRESH_TOKEN);
  const user = storage.get(STORAGE_KEYS.USER_DATA);
  
  if (!token) return null;
  
  return { token, refreshToken, user };
};

/**
 * Supprime toutes les données d'authentification
 * @returns {boolean} True si la suppression a réussi
 */
export const clearAuthData = () => {
  return storage.remove(STORAGE_KEYS.AUTH_TOKEN) &&
         storage.remove(STORAGE_KEYS.REFRESH_TOKEN) &&
         storage.remove(STORAGE_KEYS.USER_DATA);
};

/**
 * Stocke la progression d'un cours
 * @param {string} courseId - ID du cours
 * @param {Object} progress - Données de progression
 * @returns {boolean} True si le stockage a réussi
 */
export const setCourseProgress = (courseId, progress) => {
  const allProgress = storage.get(STORAGE_KEYS.COURSE_PROGRESS, {});
  allProgress[courseId] = {
    ...progress,
    lastUpdated: Date.now()
  };
  return storage.set(STORAGE_KEYS.COURSE_PROGRESS, allProgress);
};

/**
 * Récupère la progression d'un cours
 * @param {string} courseId - ID du cours
 * @returns {Object|null} Progression du cours ou null
 */
export const getCourseProgress = (courseId) => {
  const allProgress = storage.get(STORAGE_KEYS.COURSE_PROGRESS, {});
  return allProgress[courseId] || null;
};

/**
 * Stocke un brouillon de code
 * @param {string} lessonId - ID de la leçon
 * @param {string} code - Code à sauvegarder
 * @returns {boolean} True si le stockage a réussi
 */
export const saveCodeDraft = (lessonId, code) => {
  const drafts = storage.get(STORAGE_KEYS.CODE_DRAFTS, {});
  drafts[lessonId] = {
    code,
    timestamp: Date.now()
  };
  return storage.set(STORAGE_KEYS.CODE_DRAFTS, drafts);
};

/**
 * Récupère un brouillon de code
 * @param {string} lessonId - ID de la leçon
 * @returns {string|null} Code sauvegardé ou null
 */
export const getCodeDraft = (lessonId) => {
  const drafts = storage.get(STORAGE_KEYS.CODE_DRAFTS, {});
  const draft = drafts[lessonId];
  return draft ? draft.code : null;
};

/**
 * Stocke les préférences utilisateur
 * @param {Object} preferences - Préférences à stocker
 * @returns {boolean} True si le stockage a réussi
 */
export const setUserPreferences = (preferences) => {
  let success = true;
  
  if (preferences.theme) {
    success &= storage.set(STORAGE_KEYS.THEME_PREFERENCE, preferences.theme);
  }
  
  if (preferences.language) {
    success &= storage.set(STORAGE_KEYS.LANGUAGE_PREFERENCE, preferences.language);
  }
  
  if (preferences.editor) {
    success &= storage.set(STORAGE_KEYS.EDITOR_SETTINGS, preferences.editor);
  }
  
  if (preferences.notifications) {
    success &= storage.set(STORAGE_KEYS.NOTIFICATION_SETTINGS, preferences.notifications);
  }
  
  return Boolean(success);
};

/**
 * Récupère les préférences utilisateur
 * @returns {Object} Préférences utilisateur
 */
export const getUserPreferences = () => {
  return {
    theme: storage.get(STORAGE_KEYS.THEME_PREFERENCE, 'light'),
    language: storage.get(STORAGE_KEYS.LANGUAGE_PREFERENCE, 'fr'),
    editor: storage.get(STORAGE_KEYS.EDITOR_SETTINGS, {}),
    notifications: storage.get(STORAGE_KEYS.NOTIFICATION_SETTINGS, {})
  };
};
/**
 * Cache management functions for courseService
 * Add these functions to your storage.js file
 */

/**
 * Stocke un élément dans le cache
 * @param {string} key - Clé de cache
 * @param {*} value - Valeur à mettre en cache
 * @param {Object} [options={}] - Options de cache
 * @param {number} [options.maxAge] - Durée de vie en millisecondes
 * @returns {boolean} True si le stockage a réussi
 */
export const setCacheItem = (key, value, options = {}) => {
  // Utiliser un préfixe spécifique pour les éléments de cache
  const cacheKey = `cache_${key}`;
  return storage.set(cacheKey, value, {
    ...options,
    maxAge: options.maxAge || 15 * 60 * 1000 // 15 minutes par défaut
  });
};

/**
 * Récupère un élément du cache
 * @param {string} key - Clé de cache
 * @param {*} [defaultValue=null] - Valeur par défaut
 * @returns {*} Valeur en cache ou valeur par défaut
 */
export const getCacheItem = (key, defaultValue = null) => {
  const cacheKey = `cache_${key}`;
  return storage.get(cacheKey, defaultValue);
};

/**
 * Supprime un élément du cache
 * @param {string} key - Clé de cache
 * @returns {boolean} True si la suppression a réussi
 */
export const removeCacheItem = (key) => {
  const cacheKey = `cache_${key}`;
  return storage.remove(cacheKey);
};

/**
 * Nettoie tous les éléments de cache expirés
 * @returns {number} Nombre d'éléments supprimés
 */
export const cleanupCache = () => {
  const keys = storage.keys();
  let cleaned = 0;
  
  keys.forEach(key => {
    if (key.startsWith('cache_')) {
      const value = storage.get(key);
      if (value === null) {
        storage.remove(key);
        cleaned++;
      }
    }
  });
  
  return cleaned;
};

/**
 * Vérifie si un élément existe dans le cache
 * @param {string} key - Clé de cache
 * @returns {boolean} True si l'élément existe et n'est pas expiré
 */
export const hasCacheItem = (key) => {
  const cacheKey = `cache_${key}`;
  return storage.get(cacheKey) !== null;
};

// Export de l'instance principale et des utilitaires
export default storage;
export { StorageManager};

