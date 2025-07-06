/**
 * @fileoverview Utilitaires pour la gestion des appels API
 * @author JavaCraft Academy
 * @version 1.0.0
 * 
 * Ce module fournit des utilitaires pour standardiser les appels API,
 * gérer les erreurs, et formatter les réponses dans l'application JavaCraft Academy.
 * 
 * Relations avec l'application:
 * - Utilisé par tous les services (authService, courseService, etc.)
 * - Intégré avec le système de notification pour les erreurs
 * - Connecté au contexte d'authentification pour les tokens
 * - Utilisé par les hooks personnalisés (useApi, useAuth, etc.)
 */

import { toast } from 'react-toastify';

/**
 * Configuration de base pour les appels API
 */
const API_CONFIG = {
  baseURL: process.env.REACT_APP_API_URL || 'http://localhost:8080/api',
  timeout: 30000,
  retryAttempts: 3,
  retryDelay: 1000,
};

/**
 * Types d'erreurs API standardisés
 */
export const API_ERROR_TYPES = {
  NETWORK_ERROR: 'NETWORK_ERROR',
  AUTHENTICATION_ERROR: 'AUTHENTICATION_ERROR',
  AUTHORIZATION_ERROR: 'AUTHORIZATION_ERROR',
  VALIDATION_ERROR: 'VALIDATION_ERROR',
  SERVER_ERROR: 'SERVER_ERROR',
  TIMEOUT_ERROR: 'TIMEOUT_ERROR',
  RATE_LIMIT_ERROR: 'RATE_LIMIT_ERROR',
};

/**
 * Status codes HTTP couramment utilisés
 */
export const HTTP_STATUS = {
  OK: 200,
  CREATED: 201,
  NO_CONTENT: 204,
  BAD_REQUEST: 400,
  UNAUTHORIZED: 401,
  FORBIDDEN: 403,
  NOT_FOUND: 404,
  CONFLICT: 409,
  UNPROCESSABLE_ENTITY: 422,
  TOO_MANY_REQUESTS: 429,
  INTERNAL_SERVER_ERROR: 500,
  BAD_GATEWAY: 502,
  SERVICE_UNAVAILABLE: 503,
};

/**
 * Classe pour les erreurs API personnalisées
 */
export class ApiError extends Error {
  /**
   * @param {string} message - Message d'erreur
   * @param {number} status - Code de statut HTTP
   * @param {string} type - Type d'erreur
   * @param {Object} data - Données additionnelles
   */
  constructor(message, status, type, data = null) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.type = type;
    this.data = data;
    this.timestamp = new Date().toISOString();
  }
}

/**
 * Crée les en-têtes par défaut pour les requêtes API
 * @param {string} token - Token d'authentification
 * @param {Object} customHeaders - En-têtes personnalisés
 * @returns {Object} Headers configurés
 */
export const createHeaders = (token = null, customHeaders = {}) => {
  const headers = {
    'Content-Type': 'application/json',
    'Accept': 'application/json',
    'X-Client-Version': process.env.REACT_APP_VERSION || '1.0.0',
    'X-Request-ID': generateRequestId(),
    ...customHeaders,
  };

  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  return headers;
};

/**
 * Génère un ID unique pour chaque requête
 * @returns {string} ID de requête unique
 */
export const generateRequestId = () => {
  return `req_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
};

/**
 * Détermine le type d'erreur basé sur le code de statut
 * @param {number} status - Code de statut HTTP
 * @returns {string} Type d'erreur
 */
export const getErrorType = (status) => {
  switch (status) {
    case HTTP_STATUS.UNAUTHORIZED:
      return API_ERROR_TYPES.AUTHENTICATION_ERROR;
    case HTTP_STATUS.FORBIDDEN:
      return API_ERROR_TYPES.AUTHORIZATION_ERROR;
    case HTTP_STATUS.BAD_REQUEST:
    case HTTP_STATUS.UNPROCESSABLE_ENTITY:
      return API_ERROR_TYPES.VALIDATION_ERROR;
    case HTTP_STATUS.TOO_MANY_REQUESTS:
      return API_ERROR_TYPES.RATE_LIMIT_ERROR;
    case HTTP_STATUS.INTERNAL_SERVER_ERROR:
    case HTTP_STATUS.BAD_GATEWAY:
    case HTTP_STATUS.SERVICE_UNAVAILABLE:
      return API_ERROR_TYPES.SERVER_ERROR;
    default:
      return API_ERROR_TYPES.NETWORK_ERROR;
  }
};

/**
 * Traite les erreurs de réponse API
 * @param {Response} response - Réponse de l'API
 * @param {Object} data - Données de réponse
 * @returns {ApiError} Erreur formatée
 */
export const handleApiError = async (response, data = null) => {
  let errorMessage = 'Une erreur est survenue';
  let errorData = data;

  if (data && data.message) {
    errorMessage = data.message;
  } else if (data && data.error) {
    errorMessage = data.error;
  } else {
    errorMessage = `Erreur ${response.status}: ${response.statusText}`;
  }

  const errorType = getErrorType(response.status);
  
  // Log des erreurs pour le développement
  if (process.env.NODE_ENV === 'development') {
    console.error('API Error:', {
      status: response.status,
      type: errorType,
      message: errorMessage,
      data: errorData,
      url: response.url,
    });
  }

  return new ApiError(errorMessage, response.status, errorType, errorData);
};

/**
 * Effectue une requête HTTP avec gestion d'erreur et retry
 * @param {string} url - URL de la requête
 * @param {Object} options - Options de la requête
 * @param {number} retryCount - Nombre de tentatives restantes
 * @returns {Promise<Object>} Réponse de l'API
 */
export const makeRequest = async (url, options = {}, retryCount = API_CONFIG.retryAttempts) => {
  const requestUrl = url.startsWith('http') ? url : `${API_CONFIG.baseURL}${url}`;
  const requestOptions = {
    ...options,
    headers: createHeaders(options.token, options.headers),
  };

  try {
    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), API_CONFIG.timeout);

    const response = await fetch(requestUrl, {
      ...requestOptions,
      signal: controller.signal,
    });

    clearTimeout(timeoutId);

    if (!response.ok) {
      let errorData = null;
      try {
        errorData = await response.json();
      } catch (e) {
        // Ignore JSON parsing errors for error responses
      }
      throw await handleApiError(response, errorData);
    }

    // Gérer les réponses vides (204 No Content)
    if (response.status === HTTP_STATUS.NO_CONTENT) {
      return null;
    }

    const data = await response.json();
    return data;

  } catch (error) {
    if (error.name === 'AbortError') {
      throw new ApiError('Timeout de la requête', 0, API_ERROR_TYPES.TIMEOUT_ERROR);
    }

    if (error instanceof ApiError) {
      throw error;
    }

    // Retry logic pour les erreurs réseau
    if (retryCount > 0 && shouldRetry(error)) {
      await delay(API_CONFIG.retryDelay);
      return makeRequest(url, options, retryCount - 1);
    }

    throw new ApiError(
      error.message || 'Erreur de réseau',
      0,
      API_ERROR_TYPES.NETWORK_ERROR
    );
  }
};

/**
 * Détermine si une requête doit être retentée
 * @param {Error} error - Erreur rencontrée
 * @returns {boolean} True si la requête doit être retentée
 */
const shouldRetry = (error) => {
  if (error instanceof ApiError) {
    return error.type === API_ERROR_TYPES.NETWORK_ERROR || 
           error.type === API_ERROR_TYPES.SERVER_ERROR;
  }
  return true;
};

/**
 * Utilitaire pour créer un délai
 * @param {number} ms - Délai en millisecondes
 * @returns {Promise<void>}
 */
const delay = (ms) => new Promise(resolve => setTimeout(resolve, ms));

/**
 * Méthodes HTTP convenantes
 */
export const apiUtils = {
  /**
   * Effectue une requête GET
   * @param {string} url - URL de la requête
   * @param {Object} options - Options de la requête
   * @returns {Promise<Object>} Réponse de l'API
   */
  get: (url, options = {}) => makeRequest(url, { method: 'GET', ...options }),

  /**
   * Effectue une requête POST
   * @param {string} url - URL de la requête
   * @param {Object} data - Données à envoyer
   * @param {Object} options - Options de la requête
   * @returns {Promise<Object>} Réponse de l'API
   */
  post: (url, data, options = {}) => makeRequest(url, {
    method: 'POST',
    body: JSON.stringify(data),
    ...options,
  }),

  /**
   * Effectue une requête PUT
   * @param {string} url - URL de la requête
   * @param {Object} data - Données à envoyer
   * @param {Object} options - Options de la requête
   * @returns {Promise<Object>} Réponse de l'API
   */
  put: (url, data, options = {}) => makeRequest(url, {
    method: 'PUT',
    body: JSON.stringify(data),
    ...options,
  }),

  /**
   * Effectue une requête PATCH
   * @param {string} url - URL de la requête
   * @param {Object} data - Données à envoyer
   * @param {Object} options - Options de la requête
   * @returns {Promise<Object>} Réponse de l'API
   */
  patch: (url, data, options = {}) => makeRequest(url, {
    method: 'PATCH',
    body: JSON.stringify(data),
    ...options,
  }),

  /**
   * Effectue une requête DELETE
   * @param {string} url - URL de la requête
   * @param {Object} options - Options de la requête
   * @returns {Promise<Object>} Réponse de l'API
   */
  delete: (url, options = {}) => makeRequest(url, { method: 'DELETE', ...options }),
};

/**
 * Intercepteur pour gérer les erreurs globalement
 * @param {ApiError} error - Erreur API
 * @param {Function} onError - Callback d'erreur personnalisé
 */
export const handleGlobalError = (error, onError = null) => {
  if (onError) {
    onError(error);
    return;
  }

  switch (error.type) {
    case API_ERROR_TYPES.AUTHENTICATION_ERROR:
      toast.error('Session expirée. Veuillez vous reconnecter.');
      // Rediriger vers la page de connexion
      window.location.href = '/login';
      break;
    case API_ERROR_TYPES.AUTHORIZATION_ERROR:
      toast.error('Accès non autorisé.');
      break;
    case API_ERROR_TYPES.VALIDATION_ERROR:
      toast.error(error.message || 'Données invalides.');
      break;
    case API_ERROR_TYPES.RATE_LIMIT_ERROR:
      toast.error('Trop de requêtes. Veuillez patienter.');
      break;
    case API_ERROR_TYPES.SERVER_ERROR:
      toast.error('Erreur serveur. Veuillez réessayer plus tard.');
      break;
    case API_ERROR_TYPES.TIMEOUT_ERROR:
      toast.error('Timeout de la requête. Vérifiez votre connexion.');
      break;
    default:
      toast.error('Erreur de connexion. Vérifiez votre réseau.');
  }
};

/**
 * Utilitaire pour créer des URLs avec des paramètres de requête
 * @param {string} baseUrl - URL de base
 * @param {Object} params - Paramètres de requête
 * @returns {string} URL complète
 */
export const buildUrl = (baseUrl, params = {}) => {
  const url = new URL(baseUrl, API_CONFIG.baseURL);
  
  Object.entries(params).forEach(([key, value]) => {
    if (value !== null && value !== undefined) {
      url.searchParams.append(key, value.toString());
    }
  });
  
  return url.toString();
};

/**
 * Utilitaire pour uploader des fichiers
 * @param {string} url - URL d'upload
 * @param {File} file - Fichier à uploader
 * @param {Object} options - Options d'upload
 * @param {Function} onProgress - Callback de progression
 * @returns {Promise<Object>} Réponse de l'API
 */
export const uploadFile = async (url, file, options = {}, onProgress = null) => {
  const formData = new FormData();
  formData.append('file', file);
  
  if (options.metadata) {
    formData.append('metadata', JSON.stringify(options.metadata));
  }

  const requestOptions = {
    method: 'POST',
    body: formData,
    headers: {
      'Accept': 'application/json',
      ...(options.token && { Authorization: `Bearer ${options.token}` }),
    },
  };

  if (onProgress && typeof onProgress === 'function') {
    // Note: La progression exacte nécessite XMLHttpRequest
    // Ici on simule avec fetch
    return makeRequest(url, requestOptions);
  }

  return makeRequest(url, requestOptions);
};

export default apiUtils;