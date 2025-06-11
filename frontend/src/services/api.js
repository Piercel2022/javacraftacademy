// src/services/api.js
// services/api.js
import axios from 'axios';

// Configuration de base de l'API
const API_BASE_URL = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8080/api';

// Instance Axios principale
const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Intercepteur de requête pour ajouter le token d'authentification
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('authToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Intercepteur de réponse pour gérer les erreurs globales
api.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    // Gestion des erreurs d'authentification
    if (error.response?.status === 401) {
      localStorage.removeItem('authToken');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    
    // Gestion des erreurs serveur
    if (error.response?.status >= 500) {
      console.error('Erreur serveur:', error.response.data);
    }
    
    return Promise.reject(error);
  }
);

// Instance Axios pour les uploads de fichiers
const uploadApi = axios.create({
  baseURL: API_BASE_URL,
  timeout: 60000,
  headers: {
    'Content-Type': 'multipart/form-data',
  },
});

// Ajouter l'intercepteur d'authentification à l'API d'upload
uploadApi.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('authToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Fonctions utilitaires pour les appels API
export const apiCall = {
  get: (url, config = {}) => api.get(url, config),
  post: (url, data = {}, config = {}) => api.post(url, data, config),
  put: (url, data = {}, config = {}) => api.put(url, data, config),
  patch: (url, data = {}, config = {}) => api.patch(url, data, config),
  delete: (url, config = {}) => api.delete(url, config),
};

// Fonction pour les uploads
export const uploadCall = {
  post: (url, formData, config = {}) => uploadApi.post(url, formData, config),
  put: (url, formData, config = {}) => uploadApi.put(url, formData, config),
};

// Configuration des endpoints
export const endpoints = {
  auth: {
    login: '/auth/login',
    register: '/auth/register',
    refreshToken: '/auth/refresh',
    forgotPassword: '/auth/forgot-password',
    resetPassword: '/auth/reset-password',
    logout: '/auth/logout',
    profile: '/auth/profile',
  },
  courses: {
    list: '/courses',
    detail: (id) => `/courses/${id}`,
    enroll: (id) => `/courses/${id}/enroll`,
    unenroll: (id) => `/courses/${id}/unenroll`,
    lessons: (courseId) => `/courses/${courseId}/lessons`,
    lesson: (courseId, lessonId) => `/courses/${courseId}/lessons/${lessonId}`,
    search: '/courses/search',
    categories: '/courses/categories',
    featured: '/courses/featured',
  },
  progress: {
    course: (courseId) => `/progress/courses/${courseId}`,
    lesson: (lessonId) => `/progress/lessons/${lessonId}`,
    overall: '/progress/overall',
    achievements: '/progress/achievements',
    stats: '/progress/stats',
    update: '/progress/update',
  },
  compiler: {
    execute: '/compiler/execute',
    validate: '/compiler/validate',
    languages: '/compiler/languages',
    templates: '/compiler/templates',
  },
  notifications: {
    list: '/notifications',
    markRead: (id) => `/notifications/${id}/read`,
    markAllRead: '/notifications/mark-all-read',
    delete: (id) => `/notifications/${id}`,
    settings: '/notifications/settings',
  },
  users: {
    profile: '/users/profile',
    updateProfile: '/users/profile',
    uploadAvatar: '/users/avatar',
    changePassword: '/users/change-password',
    preferences: '/users/preferences',
    dashboard: '/users/dashboard',
  },
};

export default api;