// src/services/courseService.js
/**
 * Service de gestion des cours pour JavaCraft Academy
 * 
 * Ce service centralise toutes les opérations liées aux cours :
 * - Récupération des cours et de leurs détails
 * - Gestion des leçons et du contenu pédagogique
 * - Suivi de la progression des utilisateurs
 * - Gestion des exercices et évaluations
 * - Cache et optimisation des performances
 * 
 * Relations avec l'application :
 * - Utilisé par les hooks : useCourse.js
 * - Consommé par les composants : CourseCard, CourseList, CourseDetails, LessonPlayer
 * - Intègre avec : progressService.js pour le suivi de progression
 * - Communique avec : authService.js pour l'authentification des requêtes
 * - Utilise : api.js pour la configuration de base des appels API
 * 
 * @author JavaCraft Academy Team
 * @version 1.0.0
 * @since 2025-01-01
 */

import api from './api';
import { COURSE_ENDPOINTS, CACHE_KEYS, CACHE_DURATION } from '../utils/constants';
import { formatCourseData, validateCourseData } from '../utils/formatters';
import { getCacheItem, setCacheItem, removeCacheItem } from '../utils/storage';

/**
 * Cache local pour optimiser les performances
 * @private
 */
const courseCache = new Map();

/**
 * Service principal de gestion des cours
 * 
 * Fournit une interface unifiée pour toutes les opérations relatives aux cours,
 * incluant la gestion du cache, la validation des données et la gestion d'erreurs.
 */
class CourseService {
  /**
   * Récupère la liste de tous les cours disponibles
   * 
   * @param {Object} filters - Filtres à appliquer
   * @param {string} [filters.category] - Catégorie de cours
   * @param {string} [filters.level] - Niveau de difficulté (beginner, intermediate, advanced)
   * @param {string} [filters.search] - Terme de recherche
   * @param {number} [filters.page=1] - Numéro de page pour la pagination
   * @param {number} [filters.limit=10] - Nombre d'éléments par page
   * @param {boolean} [useCache=true] - Utiliser le cache local
   * @returns {Promise<Object>} Liste des cours avec métadonnées de pagination
   * @throws {Error} Erreur de récupération des données
   * 
   * @example
   * // Récupérer tous les cours
   * const courses = await courseService.getAllCourses();
   * 
   * // Récupérer les cours avec filtres
   * const filteredCourses = await courseService.getAllCourses({
   *   category: 'java-basics',
   *   level: 'beginner',
   *   search: 'variables'
   * });
   */
  async getAllCourses(filters = {}, useCache = true) {
    try {
      const cacheKey = `${CACHE_KEYS.COURSES}_${JSON.stringify(filters)}`;
      
      // Vérifier le cache si activé
      if (useCache) {
        const cachedData = getCacheItem(cacheKey);
        if (cachedData) {
          return cachedData;
        }
      }

      // Construction des paramètres de requête
      const params = new URLSearchParams();
      Object.entries(filters).forEach(([key, value]) => {
        if (value !== undefined && value !== null && value !== '') {
          params.append(key, value);
        }
      });

      const response = await api.get(`${COURSE_ENDPOINTS.COURSES}?${params.toString()}`);
      
      // Validation et formatage des données
      const validatedData = validateCourseData(response.data);
      const formattedData = {
        courses: validatedData.courses.map(formatCourseData),
        pagination: validatedData.pagination,
        filters: validatedData.appliedFilters
      };

      // Mise en cache
      if (useCache) {
        setCacheItem(cacheKey, formattedData, CACHE_DURATION.COURSES);
      }

      return formattedData;
    } catch (error) {
      console.error('Erreur lors de la récupération des cours:', error);
      throw this.handleApiError(error, 'Impossible de récupérer les cours');
    }
  }

  /**
   * Récupère les détails complets d'un cours spécifique
   * 
   * @param {string|number} courseId - Identifiant unique du cours
   * @param {boolean} [includeProgress=true] - Inclure les données de progression
   * @param {boolean} [useCache=true] - Utiliser le cache local
   * @returns {Promise<Object>} Détails complets du cours
   * @throws {Error} Erreur si le cours n'existe pas ou est inaccessible
   * 
   * @example
   * const courseDetails = await courseService.getCourseById(1);
   * console.log(courseDetails.title, courseDetails.lessons.length);
   */
  async getCourseById(courseId, includeProgress = true, useCache = true) {
    try {
      if (!courseId) {
        throw new Error('ID du cours requis');
      }

      const cacheKey = `${CACHE_KEYS.COURSE_DETAILS}_${courseId}_${includeProgress}`;
      
      // Vérifier le cache
      if (useCache) {
        const cachedData = getCacheItem(cacheKey);
        if (cachedData) {
          return cachedData;
        }
      }

      const response = await api.get(
        `${COURSE_ENDPOINTS.COURSE_DETAILS}/${courseId}`,
        {
          params: { includeProgress }
        }
      );

      const courseData = formatCourseData(response.data);
      
      // Mise en cache
      if (useCache) {
        setCacheItem(cacheKey, courseData, CACHE_DURATION.COURSE_DETAILS);
      }

      return courseData;
    } catch (error) {
      console.error(`Erreur lors de la récupération du cours ${courseId}:`, error);
      throw this.handleApiError(error, 'Cours introuvable');
    }
  }

  /**
   * Récupère les leçons d'un cours avec leur contenu
   * 
   * @param {string|number} courseId - Identifiant du cours
   * @param {Object} options - Options de récupération
   * @param {boolean} [options.includeContent=false] - Inclure le contenu complet des leçons
   * @param {boolean} [options.includeExercises=true] - Inclure les exercices
   * @param {number} [options.lessonId] - ID d'une leçon spécifique
   * @returns {Promise<Array>} Liste des leçons du cours
   * 
   * @example
   * const lessons = await courseService.getCourseLessons(1, {
   *   includeContent: true,
   *   includeExercises: true
   * });
   */
  async getCourseLessons(courseId, options = {}) {
    try {
      const {
        includeContent = false,
        includeExercises = true,
        lessonId = null
      } = options;

      const endpoint = lessonId 
        ? `${COURSE_ENDPOINTS.LESSONS}/${courseId}/${lessonId}`
        : `${COURSE_ENDPOINTS.LESSONS}/${courseId}`;

      const response = await api.get(endpoint, {
        params: { includeContent, includeExercises }
      });

      return response.data.lessons || response.data;
    } catch (error) {
      console.error('Erreur lors de la récupération des leçons:', error);
      throw this.handleApiError(error, 'Impossible de récupérer les leçons');
    }
  }

  /**
   * Inscrit un utilisateur à un cours
   * 
   * @param {string|number} courseId - Identifiant du cours
   * @param {Object} enrollmentData - Données d'inscription
   * @param {string} [enrollmentData.paymentMethod] - Méthode de paiement (pour les cours payants)
   * @param {string} [enrollmentData.couponCode] - Code de réduction
   * @returns {Promise<Object>} Confirmation d'inscription avec détails
   * 
   * @example
   * const enrollment = await courseService.enrollInCourse(1, {
   *   paymentMethod: 'stripe',
   *   couponCode: 'WELCOME20'
   * });
   */
  async enrollInCourse(courseId, enrollmentData = {}) {
    try {
      const response = await api.post(
        `${COURSE_ENDPOINTS.ENROLLMENT}/${courseId}`,
        enrollmentData
      );

      // Invalider le cache des cours de l'utilisateur
      this.invalidateUserCoursesCache();

      return response.data;
    } catch (error) {
      console.error('Erreur lors de l\'inscription au cours:', error);
      throw this.handleApiError(error, 'Impossible de s\'inscrire au cours');
    }
  }

  /**
   * Récupère les cours auxquels l'utilisateur est inscrit
   * 
   * @param {Object} options - Options de récupération
   * @param {string} [options.status] - Statut des cours (active, completed, paused)
   * @param {boolean} [options.includeProgress=true] - Inclure les données de progression
   * @returns {Promise<Array>} Liste des cours de l'utilisateur
   * 
   * @example
   * const myCourses = await courseService.getUserCourses({
   *   status: 'active',
   *   includeProgress: true
   * });
   */
  async getUserCourses(options = {}) {
    try {
      const { status, includeProgress = true } = options;
      const cacheKey = `${CACHE_KEYS.USER_COURSES}_${JSON.stringify(options)}`;
      
      // Vérifier le cache
      const cachedData = getCacheItem(cacheKey);
      if (cachedData) {
        return cachedData;
      }

      const response = await api.get(COURSE_ENDPOINTS.USER_COURSES, {
        params: { status, includeProgress }
      });

      const userData = response.data.map(course => ({
        ...formatCourseData(course),
        enrollmentDate: course.enrollmentDate,
        lastAccessDate: course.lastAccessDate,
        progress: course.progress || 0,
        status: course.status || 'active'
      }));

      // Mise en cache
      setCacheItem(cacheKey, userData, CACHE_DURATION.USER_COURSES);

      return userData;
    } catch (error) {
      console.error('Erreur lors de la récupération des cours utilisateur:', error);
      throw this.handleApiError(error, 'Impossible de récupérer vos cours');
    }
  }

  /**
   * Met à jour la progression d'une leçon
   * 
   * @param {string|number} courseId - Identifiant du cours
   * @param {string|number} lessonId - Identifiant de la leçon
   * @param {Object} progressData - Données de progression
   * @param {number} progressData.progress - Pourcentage de progression (0-100)
   * @param {boolean} [progressData.completed=false] - Leçon terminée
   * @param {number} [progressData.timeSpent] - Temps passé en secondes
   * @returns {Promise<Object>} Données de progression mises à jour
   * 
   * @example
   * await courseService.updateLessonProgress(1, 5, {
   *   progress: 100,
   *   completed: true,
   *   timeSpent: 1800
   * });
   */
  async updateLessonProgress(courseId, lessonId, progressData) {
    try {
      const response = await api.put(
        `${COURSE_ENDPOINTS.PROGRESS}/${courseId}/${lessonId}`,
        {
          ...progressData,
          timestamp: new Date().toISOString()
        }
      );

      // Invalider les caches liés à la progression
      this.invalidateProgressCache(courseId);

      return response.data;
    } catch (error) {
      console.error('Erreur lors de la mise à jour de la progression:', error);
      throw this.handleApiError(error, 'Impossible de sauvegarder la progression');
    }
  }

  /**
   * Recherche des cours par mots-clés
   * 
   * @param {string} query - Terme de recherche
   * @param {Object} options - Options de recherche
   * @param {Array<string>} [options.fields] - Champs à rechercher
   * @param {number} [options.limit=20] - Nombre de résultats maximum
   * @param {boolean} [options.fuzzy=true] - Recherche approximative
   * @returns {Promise<Array>} Résultats de recherche avec score de pertinence
   * 
   * @example
   * const results = await courseService.searchCourses('java collections', {
   *   fields: ['title', 'description', 'tags'],
   *   limit: 10
   * });
   */
  async searchCourses(query, options = {}) {
    try {
      if (!query || query.trim().length < 2) {
        return [];
      }

      const {
        fields = ['title', 'description', 'tags'],
        limit = 20,
        fuzzy = true
      } = options;

      const response = await api.get(COURSE_ENDPOINTS.SEARCH, {
        params: {
          q: query.trim(),
          fields: fields.join(','),
          limit,
          fuzzy
        }
      });

      return response.data.results.map(result => ({
        ...formatCourseData(result.course),
        relevanceScore: result.score,
        matchedFields: result.matchedFields
      }));
    } catch (error) {
      console.error('Erreur lors de la recherche:', error);
      throw this.handleApiError(error, 'Erreur de recherche');
    }
  }

  /**
   * Récupère les catégories de cours disponibles
   * 
   * @param {boolean} [includeStats=false] - Inclure les statistiques par catégorie
   * @returns {Promise<Array>} Liste des catégories
   * 
   * @example
   * const categories = await courseService.getCategories(true);
   */
  async getCategories(includeStats = false) {
    try {
      const cacheKey = `${CACHE_KEYS.CATEGORIES}_${includeStats}`;
      
      // Vérifier le cache
      const cachedData = getCacheItem(cacheKey);
      if (cachedData) {
        return cachedData;
      }

      const response = await api.get(COURSE_ENDPOINTS.CATEGORIES, {
        params: { includeStats }
      });

      const categories = response.data;
      
      // Mise en cache longue durée
      setCacheItem(cacheKey, categories, CACHE_DURATION.CATEGORIES);

      return categories;
    } catch (error) {
      console.error('Erreur lors de la récupération des catégories:', error);
      throw this.handleApiError(error, 'Impossible de récupérer les catégories');
    }
  }

  /**
   * Récupère les cours recommandés pour un utilisateur
   * 
   * @param {Object} options - Options de recommandation
   * @param {number} [options.limit=5] - Nombre de recommandations
   * @param {string} [options.based_on='progress'] - Base de recommandation
   * @returns {Promise<Array>} Liste des cours recommandés
   * 
   * @example
   * const recommendations = await courseService.getRecommendations({
   *   limit: 10,
   *   based_on: 'progress'
   * });
   */
  async getRecommendations(options = {}) {
    try {
      const { limit = 5, based_on = 'progress' } = options;
      
      const response = await api.get(COURSE_ENDPOINTS.RECOMMENDATIONS, {
        params: { limit, based_on }
      });

      return response.data.map(item => ({
        ...formatCourseData(item.course),
        recommendationReason: item.reason,
        score: item.score
      }));
    } catch (error) {
      console.error('Erreur lors de la récupération des recommandations:', error);
      // Retourner un tableau vide en cas d'erreur pour les recommandations
      return [];
    }
  }

  /**
   * Soumet une évaluation pour un cours
   * 
   * @param {string|number} courseId - Identifiant du cours
   * @param {Object} reviewData - Données d'évaluation
   * @param {number} reviewData.rating - Note de 1 à 5
   * @param {string} [reviewData.comment] - Commentaire textuel
   * @param {Array<string>} [reviewData.tags] - Tags d'évaluation
   * @returns {Promise<Object>} Évaluation créée
   * 
   * @example
   * await courseService.submitReview(1, {
   *   rating: 5,
   *   comment: 'Excellent cours, très bien structuré !',
   *   tags: ['clear', 'practical', 'engaging']
   * });
   */
  async submitReview(courseId, reviewData) {
    try {
      const response = await api.post(
        `${COURSE_ENDPOINTS.REVIEWS}/${courseId}`,
        {
          ...reviewData,
          createdAt: new Date().toISOString()
        }
      );

      // Invalider le cache des détails du cours
      removeCacheItem(`${CACHE_KEYS.COURSE_DETAILS}_${courseId}_true`);
      removeCacheItem(`${CACHE_KEYS.COURSE_DETAILS}_${courseId}_false`);

      return response.data;
    } catch (error) {
      console.error('Erreur lors de la soumission de l\'évaluation:', error);
      throw this.handleApiError(error, 'Impossible de soumettre l\'évaluation');
    }
  }

  /**
   * Gère les erreurs d'API et les transforme en erreurs utilisateur
   * 
   * @private
   * @param {Error} error - Erreur d'origine
   * @param {string} defaultMessage - Message par défaut
   * @returns {Error} Erreur formatée
   */
  handleApiError(error, defaultMessage) {
    if (error.response) {
      const { status, data } = error.response;
      
      switch (status) {
        case 404:
          return new Error('Ressource introuvable');
        case 403:
          return new Error('Accès non autorisé');
        case 401:
          return new Error('Authentification requise');
        case 429:
          return new Error('Trop de requêtes, veuillez patienter');
        case 500:
          return new Error('Erreur serveur, veuillez réessayer plus tard');
        default:
          return new Error(data?.message || defaultMessage);
      }
    } else if (error.request) {
      return new Error('Problème de connexion réseau');
    } else {
      return new Error(defaultMessage);
    }
  }

  /**
   * Invalide le cache des cours utilisateur
   * 
   * @private
   */
  invalidateUserCoursesCache() {
    const keysToRemove = [];
    for (const key of courseCache.keys()) {
      if (key.includes(CACHE_KEYS.USER_COURSES)) {
        keysToRemove.push(key);
      }
    }
    keysToRemove.forEach(key => removeCacheItem(key));
  }

  /**
   * Invalide le cache de progression pour un cours
   * 
   * @private
   * @param {string|number} courseId - Identifiant du cours
   */
  invalidateProgressCache(courseId) {
    removeCacheItem(`${CACHE_KEYS.COURSE_DETAILS}_${courseId}_true`);
    removeCacheItem(`${CACHE_KEYS.USER_COURSES}_${JSON.stringify({})}`);
  }

  /**
   * Nettoie tout le cache du service
   * 
   * @public
   */
  clearCache() {
    courseCache.clear();
    // Nettoyer aussi le localStorage des cours
    Object.values(CACHE_KEYS).forEach(key => {
      if (key.includes('COURSE') || key.includes('CATEGORIES')) {
        removeCacheItem(key);
      }
    });
  }

  /**
   * Précharge les données essentielles
   * 
   * @public
   * @returns {Promise<void>}
   */
  async preloadEssentialData() {
    try {
      await Promise.all([
        this.getCategories(true),
        this.getAllCourses({ limit: 12 }) // Précharger les premiers cours
      ]);
    } catch (error) {
      console.warn('Erreur lors du préchargement:', error);
    }
  }
}

// Export d'une instance unique (singleton)
const courseService = new CourseService();

export default courseService;

/**
 * Export des méthodes individuelles pour faciliter les tests
 */
export {
  CourseService
};