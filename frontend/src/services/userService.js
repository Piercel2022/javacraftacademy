// src/services/userService.js
/**
 * @fileoverview Service de gestion des utilisateurs pour JavaCraft Academy
 * Ce service gère toutes les opérations liées aux utilisateurs incluant :
 * - Gestion du profil utilisateur (récupération, mise à jour)
 * - Upload et gestion des avatars
 * - Gestion des préférences utilisateur
 * - Statistiques et progression utilisateur
 * - Gestion des paramètres de compte
 * - Historique des activités
 * - Gestion des notifications utilisateur
 * 
 * Relations avec l'application :
 * - Utilisé par les composants : UserProfile, ProfileSettings, AvatarUpload
 * - Intégré avec AuthContext pour la gestion de l'utilisateur connecté
 * - Collabore avec progressService pour les statistiques de progression
 * - Utilise notificationService pour les notifications utilisateur
 * - Fonctionne avec courseService pour l'historique des cours
 * 
 * @author JavaCraft Academy Team
 * @version 1.0.0
 * @since 2024-01-01
 */

import api from './api';
import { handleApiError, formatResponse } from '../utils/helpers';
import { validateUserData, validateEmail, validatePassword } from '../utils/validators';
import { USER_ENDPOINTS, HTTP_STATUS } from '../utils/constants';

/**
 * Service de gestion des utilisateurs
 * Centralise toutes les opérations liées aux utilisateurs
 */
class UserService {
  /**
   * Constructeur du service utilisateur
   * Initialise les configurations et les caches
   */
  constructor() {
    this.cache = new Map();
    this.cacheTimeout = 5 * 60 * 1000; // 5 minutes
    this.maxCacheSize = 100;
    this.uploadProgress = new Map();
  }

  // ==================== GESTION DU PROFIL UTILISATEUR ====================

  /**
   * Récupère le profil de l'utilisateur connecté
   * @returns {Promise<Object>} Profil utilisateur avec statistiques
   * @throws {Error} En cas d'erreur lors de la récupération
   * 
   * @example
   * const profile = await userService.getCurrentUserProfile();
   * console.log(profile.name, profile.email);
   */
  async getCurrentUserProfile() {
    try {
      const cacheKey = 'currentUserProfile';
      const cachedProfile = this.getFromCache(cacheKey);
      
      if (cachedProfile) {
        return cachedProfile;
      }

      const response = await api.get(USER_ENDPOINTS.CURRENT_USER);
      const profileData = formatResponse(response.data);
      
      // Enrichir le profil avec des statistiques
      const enrichedProfile = await this.enrichProfileWithStats(profileData);
      
      this.setCache(cacheKey, enrichedProfile);
      return enrichedProfile;
    } catch (error) {
      throw handleApiError(error, 'Erreur lors de la récupération du profil');
    }
  }

  /**
   * Récupère le profil d'un utilisateur par son ID
   * @param {string} userId - ID de l'utilisateur
   * @returns {Promise<Object>} Profil utilisateur public
   * @throws {Error} En cas d'erreur lors de la récupération
   * 
   * @example
   * const userProfile = await userService.getUserProfile('user123');
   */
  async getUserProfile(userId) {
    try {
      if (!userId) {
        throw new Error('ID utilisateur requis');
      }

      const cacheKey = `userProfile_${userId}`;
      const cachedProfile = this.getFromCache(cacheKey);
      
      if (cachedProfile) {
        return cachedProfile;
      }

      const response = await api.get(`${USER_ENDPOINTS.USERS}/${userId}`);
      const profileData = formatResponse(response.data);
      
      this.setCache(cacheKey, profileData);
      return profileData;
    } catch (error) {
      throw handleApiError(error, 'Erreur lors de la récupération du profil utilisateur');
    }
  }

  /**
   * Met à jour le profil de l'utilisateur connecté
   * @param {Object} userData - Données à mettre à jour
   * @param {string} [userData.name] - Nom de l'utilisateur
   * @param {string} [userData.email] - Email de l'utilisateur
   * @param {string} [userData.bio] - Biographie de l'utilisateur
   * @param {string} [userData.location] - Localisation de l'utilisateur
   * @param {string} [userData.website] - Site web de l'utilisateur
   * @param {Array} [userData.skills] - Compétences de l'utilisateur
   * @returns {Promise<Object>} Profil utilisateur mis à jour
   * @throws {Error} En cas d'erreur lors de la mise à jour
   * 
   * @example
   * const updatedProfile = await userService.updateUserProfile({
   *   name: 'John Doe',
   *   bio: 'Développeur Java passionné'
   * });
   */
  async updateUserProfile(userData) {
    try {
      // Validation des données
      const validationResult = validateUserData(userData);
      if (!validationResult.isValid) {
        throw new Error(`Données invalides: ${validationResult.errors.join(', ')}`);
      }

      // Validation spécifique de l'email si fourni
      if (userData.email && !validateEmail(userData.email)) {
        throw new Error('Format d\'email invalide');
      }

      const response = await api.put(USER_ENDPOINTS.UPDATE_PROFILE, userData);
      const updatedProfile = formatResponse(response.data);
      
      // Invalider le cache
      this.invalidateCache('currentUserProfile');
      this.setCache('currentUserProfile', updatedProfile);
      
      return updatedProfile;
    } catch (error) {
      throw handleApiError(error, 'Erreur lors de la mise à jour du profil');
    }
  }

  // ==================== GESTION DES AVATARS ====================

  /**
   * Upload un nouvel avatar pour l'utilisateur
   * @param {File} avatarFile - Fichier image de l'avatar
   * @param {Function} [onProgress] - Callback pour suivre la progression
   * @returns {Promise<Object>} URL de l'avatar uploadé
   * @throws {Error} En cas d'erreur lors de l'upload
   * 
   * @example
   * const result = await userService.uploadAvatar(file, (progress) => {
   *   console.log(`Upload: ${progress}%`);
   * });
   */
  async uploadAvatar(avatarFile, onProgress) {
    try {
      // Validation du fichier
      if (!avatarFile) {
        throw new Error('Fichier avatar requis');
      }

      if (!this.isValidImageFile(avatarFile)) {
        throw new Error('Format de fichier invalide. Utilisez JPG, PNG ou WebP');
      }

      if (avatarFile.size > 5 * 1024 * 1024) { // 5MB max
        throw new Error('Fichier trop volumineux. Taille maximale: 5MB');
      }

      const formData = new FormData();
      formData.append('avatar', avatarFile);

      const uploadId = Date.now().toString();
      this.uploadProgress.set(uploadId, 0);

      const response = await api.post(USER_ENDPOINTS.UPLOAD_AVATAR, formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
        onUploadProgress: (progressEvent) => {
          const progress = Math.round(
            (progressEvent.loaded * 100) / progressEvent.total
          );
          this.uploadProgress.set(uploadId, progress);
          
          if (onProgress) {
            onProgress(progress);
          }
        },
      });

      this.uploadProgress.delete(uploadId);
      
      const result = formatResponse(response.data);
      
      // Invalider le cache du profil
      this.invalidateCache('currentUserProfile');
      
      return result;
    } catch (error) {
      throw handleApiError(error, 'Erreur lors de l\'upload de l\'avatar');
    }
  }

  /**
   * Supprime l'avatar de l'utilisateur
   * @returns {Promise<Object>} Confirmation de suppression
   * @throws {Error} En cas d'erreur lors de la suppression
   * 
   * @example
   * await userService.deleteAvatar();
   */
  async deleteAvatar() {
    try {
      const response = await api.delete(USER_ENDPOINTS.DELETE_AVATAR);
      const result = formatResponse(response.data);
      
      // Invalider le cache du profil
      this.invalidateCache('currentUserProfile');
      
      return result;
    } catch (error) {
      throw handleApiError(error, 'Erreur lors de la suppression de l\'avatar');
    }
  }

  // ==================== GESTION DES PRÉFÉRENCES ====================

  /**
   * Récupère les préférences de l'utilisateur
   * @returns {Promise<Object>} Préférences utilisateur
   * @throws {Error} En cas d'erreur lors de la récupération
   * 
   * @example
   * const preferences = await userService.getUserPreferences();
   * console.log(preferences.theme, preferences.language);
   */
  async getUserPreferences() {
    try {
      const cacheKey = 'userPreferences';
      const cachedPreferences = this.getFromCache(cacheKey);
      
      if (cachedPreferences) {
        return cachedPreferences;
      }

      const response = await api.get(USER_ENDPOINTS.PREFERENCES);
      const preferences = formatResponse(response.data);
      
      this.setCache(cacheKey, preferences);
      return preferences;
    } catch (error) {
      throw handleApiError(error, 'Erreur lors de la récupération des préférences');
    }
  }

  /**
   * Met à jour les préférences de l'utilisateur
   * @param {Object} preferences - Nouvelles préférences
   * @param {string} [preferences.theme] - Thème de l'interface (light/dark)
   * @param {string} [preferences.language] - Langue de l'interface
   * @param {boolean} [preferences.emailNotifications] - Notifications par email
   * @param {boolean} [preferences.pushNotifications] - Notifications push
   * @param {Object} [preferences.editorSettings] - Paramètres de l'éditeur
   * @returns {Promise<Object>} Préférences mises à jour
   * @throws {Error} En cas d'erreur lors de la mise à jour
   * 
   * @example
   * const updatedPrefs = await userService.updateUserPreferences({
   *   theme: 'dark',
   *   emailNotifications: true
   * });
   */
  async updateUserPreferences(preferences) {
    try {
      const response = await api.put(USER_ENDPOINTS.UPDATE_PREFERENCES, preferences);
      const updatedPreferences = formatResponse(response.data);
      
      // Mettre à jour le cache
      this.setCache('userPreferences', updatedPreferences);
      
      return updatedPreferences;
    } catch (error) {
      throw handleApiError(error, 'Erreur lors de la mise à jour des préférences');
    }
  }

  // ==================== STATISTIQUES ET PROGRESSION ====================

  /**
   * Récupère les statistiques détaillées de l'utilisateur
   * @param {string} [period='all'] - Période des statistiques (week, month, year, all)
   * @returns {Promise<Object>} Statistiques utilisateur
   * @throws {Error} En cas d'erreur lors de la récupération
   * 
   * @example
   * const stats = await userService.getUserStats('month');
   * console.log(stats.coursesCompleted, stats.totalStudyTime);
   */
  async getUserStats(period = 'all') {
    try {
      const cacheKey = `userStats_${period}`;
      const cachedStats = this.getFromCache(cacheKey);
      
      if (cachedStats) {
        return cachedStats;
      }

      const response = await api.get(`${USER_ENDPOINTS.STATS}?period=${period}`);
      const stats = formatResponse(response.data);
      
      this.setCache(cacheKey, stats, 2 * 60 * 1000); // Cache 2 minutes
      return stats;
    } catch (error) {
      throw handleApiError(error, 'Erreur lors de la récupération des statistiques');
    }
  }

  /**
   * Récupère l'historique des activités de l'utilisateur
   * @param {Object} options - Options de récupération
   * @param {number} [options.page=1] - Page à récupérer
   * @param {number} [options.limit=20] - Nombre d'éléments par page
   * @param {string} [options.type] - Type d'activité à filtrer
   * @returns {Promise<Object>} Historique des activités
   * @throws {Error} En cas d'erreur lors de la récupération
   * 
   * @example
   * const activities = await userService.getUserActivities({
   *   page: 1,
   *   limit: 10,
   *   type: 'course_completion'
   * });
   */
  async getUserActivities(options = {}) {
    try {
      const { page = 1, limit = 20, type } = options;
      const params = new URLSearchParams({
        page: page.toString(),
        limit: limit.toString(),
        ...(type && { type })
      });

      const response = await api.get(`${USER_ENDPOINTS.ACTIVITIES}?${params}`);
      return formatResponse(response.data);
    } catch (error) {
      throw handleApiError(error, 'Erreur lors de la récupération des activités');
    }
  }

  // ==================== GESTION DU COMPTE ====================

  /**
   * Change le mot de passe de l'utilisateur
   * @param {Object} passwordData - Données du mot de passe
   * @param {string} passwordData.currentPassword - Mot de passe actuel
   * @param {string} passwordData.newPassword - Nouveau mot de passe
   * @param {string} passwordData.confirmPassword - Confirmation du nouveau mot de passe
   * @returns {Promise<Object>} Confirmation du changement
   * @throws {Error} En cas d'erreur lors du changement
   * 
   * @example
   * await userService.changePassword({
   *   currentPassword: 'oldPassword',
   *   newPassword: 'newPassword123',
   *   confirmPassword: 'newPassword123'
   * });
   */
  async changePassword(passwordData) {
    try {
      const { currentPassword, newPassword, confirmPassword } = passwordData;

      // Validations
      if (!currentPassword || !newPassword || !confirmPassword) {
        throw new Error('Tous les champs sont requis');
      }

      if (newPassword !== confirmPassword) {
        throw new Error('Les mots de passe ne correspondent pas');
      }

      if (!validatePassword(newPassword)) {
        throw new Error('Le nouveau mot de passe ne respecte pas les critères de sécurité');
      }

      const response = await api.put(USER_ENDPOINTS.CHANGE_PASSWORD, {
        currentPassword,
        newPassword
      });

      return formatResponse(response.data);
    } catch (error) {
      throw handleApiError(error, 'Erreur lors du changement de mot de passe');
    }
  }

  /**
   * Supprime le compte de l'utilisateur
   * @param {string} password - Mot de passe de confirmation
   * @param {string} reason - Raison de la suppression
   * @returns {Promise<Object>} Confirmation de suppression
   * @throws {Error} En cas d'erreur lors de la suppression
   * 
   * @example
   * await userService.deleteAccount('password123', 'No longer need the service');
   */
  async deleteAccount(password, reason) {
    try {
      if (!password) {
        throw new Error('Mot de passe requis pour la suppression');
      }

      const response = await api.delete(USER_ENDPOINTS.DELETE_ACCOUNT, {
        data: { password, reason }
      });

      // Nettoyer le cache
      this.clearCache();
      
      return formatResponse(response.data);
    } catch (error) {
      throw handleApiError(error, 'Erreur lors de la suppression du compte');
    }
  }

  // ==================== GESTION DES NOTIFICATIONS ====================

  /**
   * Récupère les paramètres de notification de l'utilisateur
   * @returns {Promise<Object>} Paramètres de notification
   * @throws {Error} En cas d'erreur lors de la récupération
   * 
   * @example
   * const notificationSettings = await userService.getNotificationSettings();
   */
  async getNotificationSettings() {
    try {
      const cacheKey = 'notificationSettings';
      const cachedSettings = this.getFromCache(cacheKey);
      
      if (cachedSettings) {
        return cachedSettings;
      }

      const response = await api.get(USER_ENDPOINTS.NOTIFICATION_SETTINGS);
      const settings = formatResponse(response.data);
      
      this.setCache(cacheKey, settings);
      return settings;
    } catch (error) {
      throw handleApiError(error, 'Erreur lors de la récupération des paramètres de notification');
    }
  }

  /**
   * Met à jour les paramètres de notification
   * @param {Object} settings - Nouveaux paramètres
   * @param {boolean} [settings.emailNotifications] - Notifications email
   * @param {boolean} [settings.pushNotifications] - Notifications push
   * @param {boolean} [settings.courseReminders] - Rappels de cours
   * @param {boolean} [settings.achievementNotifications] - Notifications de réussites
   * @returns {Promise<Object>} Paramètres mis à jour
   * @throws {Error} En cas d'erreur lors de la mise à jour
   * 
   * @example
   * const updatedSettings = await userService.updateNotificationSettings({
   *   emailNotifications: true,
   *   pushNotifications: false
   * });
   */
  async updateNotificationSettings(settings) {
    try {
      const response = await api.put(USER_ENDPOINTS.UPDATE_NOTIFICATION_SETTINGS, settings);
      const updatedSettings = formatResponse(response.data);
      
      // Mettre à jour le cache
      this.setCache('notificationSettings', updatedSettings);
      
      return updatedSettings;
    } catch (error) {
      throw handleApiError(error, 'Erreur lors de la mise à jour des paramètres de notification');
    }
  }

  // ==================== MÉTHODES UTILITAIRES ====================

  /**
   * Enrichit le profil utilisateur avec des statistiques
   * @private
   * @param {Object} profile - Profil de base
   * @returns {Promise<Object>} Profil enrichi
   */
  async enrichProfileWithStats(profile) {
    try {
      const [stats, preferences] = await Promise.all([
        this.getUserStats().catch(() => ({})),
        this.getUserPreferences().catch(() => ({}))
      ]);

      return {
        ...profile,
        stats,
        preferences,
        lastUpdated: new Date().toISOString()
      };
    } catch (error) {
      // Retourner le profil de base si l'enrichissement échoue
      return profile;
    }
  }

  /**
   * Valide si un fichier est une image valide
   * @private
   * @param {File} file - Fichier à valider
   * @returns {boolean} True si le fichier est valide
   */
  isValidImageFile(file) {
    const allowedTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/webp'];
    return allowedTypes.includes(file.type);
  }

  /**
   * Récupère une valeur du cache
   * @private
   * @param {string} key - Clé du cache
   * @returns {*} Valeur mise en cache ou null
   */
  getFromCache(key) {
    const item = this.cache.get(key);
    if (!item) return null;

    if (Date.now() > item.expiry) {
      this.cache.delete(key);
      return null;
    }

    return item.value;
  }

  /**
   * Met une valeur en cache
   * @private
   * @param {string} key - Clé du cache
   * @param {*} value - Valeur à mettre en cache
   * @param {number} [timeout] - Timeout personnalisé
   */
  setCache(key, value, timeout = this.cacheTimeout) {
    // Nettoyer le cache si trop plein
    if (this.cache.size >= this.maxCacheSize) {
      const firstKey = this.cache.keys().next().value;
      this.cache.delete(firstKey);
    }

    this.cache.set(key, {
      value,
      expiry: Date.now() + timeout
    });
  }

  /**
   * Invalide une entrée du cache
   * @private
   * @param {string} key - Clé à invalider
   */
  invalidateCache(key) {
    this.cache.delete(key);
  }

  /**
   * Nettoie tout le cache
   * @private
   */
  clearCache() {
    this.cache.clear();
  }

  /**
   * Récupère la progression d'upload
   * @param {string} uploadId - ID de l'upload
   * @returns {number} Progression en pourcentage
   */
  getUploadProgress(uploadId) {
    return this.uploadProgress.get(uploadId) || 0;
  }
}

// Instance singleton du service
const userService = new UserService();

export default userService;

