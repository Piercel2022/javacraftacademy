
/**
 * @fileoverview Constantes globales de l'application JavaCraft Academy
 * @description Ce fichier centralise toutes les constantes utilisées dans l'application
 * pour maintenir la cohérence et faciliter la maintenance.
 * 
 * Relations avec l'application :
 * - Utilisé par tous les composants pour les valeurs fixes
 * - Référencé dans les services API pour les endpoints
 * - Utilisé dans les validators pour les règles de validation
 * - Référencé dans les composants auth pour les états d'authentification
 * - Utilisé dans les composants course pour les niveaux et statuts
 * - Référencé dans les composants progress pour les achievements
 * 
 * @author JavaCraft Academy Team
 * @version 1.0.0
 */

// ==================== API CONFIGURATION ====================

/**
 * Configuration des endpoints API
 * @constant {Object} API_ENDPOINTS
 */
export const API_ENDPOINTS = {
  BASE_URL: process.env.REACT_APP_API_BASE_URL || 'http://localhost:3001/api',
  AUTH: {
    LOGIN: '/auth/login',
    REGISTER: '/auth/register',
    LOGOUT: '/auth/logout',
    REFRESH: '/auth/refresh',
    FORGOT_PASSWORD: '/auth/forgot-password',
    RESET_PASSWORD: '/auth/reset-password',
    VERIFY_EMAIL: '/auth/verify-email'
  },
  USERS: {
    PROFILE: '/users/profile',
    UPDATE_PROFILE: '/users/profile',
    UPLOAD_AVATAR: '/users/avatar',
    DELETE_ACCOUNT: '/users/account'
  },
  COURSES: {
    ALL: '/courses',
    BY_ID: '/courses/:id',
    ENROLL: '/courses/:id/enroll',
    UNENROLL: '/courses/:id/unenroll',
    SEARCH: '/courses/search',
    CATEGORIES: '/courses/categories'
  },
  LESSONS: {
    BY_COURSE: '/courses/:courseId/lessons',
    BY_ID: '/lessons/:id',
    COMPLETE: '/lessons/:id/complete',
    PROGRESS: '/lessons/:id/progress'
  },
  PROGRESS: {
    USER: '/progress/user',
    COURSE: '/progress/course/:courseId',
    ACHIEVEMENTS: '/progress/achievements',
    STATS: '/progress/stats'
  },
  COMPILER: {
    EXECUTE: '/compiler/execute',
    VALIDATE: '/compiler/validate'
  },
  NOTIFICATIONS: {
    ALL: '/notifications',
    MARK_READ: '/notifications/:id/read',
    MARK_ALL_READ: '/notifications/read-all'
  }
};

// ==================== AUTHENTICATION CONSTANTS ====================

/**
 * États d'authentification possibles
 * @constant {Object} AUTH_STATUS
 */
export const AUTH_STATUS = {
  IDLE: 'idle',
  LOADING: 'loading',
  AUTHENTICATED: 'authenticated',
  UNAUTHENTICATED: 'unauthenticated',
  ERROR: 'error'
};

/**
 * Rôles utilisateur disponibles
 * @constant {Object} USER_ROLES
 */
export const USER_ROLES = {
  STUDENT: 'student',
  INSTRUCTOR: 'instructor',
  ADMIN: 'admin',
  MODERATOR: 'moderator'
};

/**
 * Durée de vie des tokens (en millisecondes)
 * @constant {Object} TOKEN_DURATION
 */
export const TOKEN_DURATION = {
  ACCESS_TOKEN: 15 * 60 * 1000, // 15 minutes
  REFRESH_TOKEN: 7 * 24 * 60 * 60 * 1000, // 7 jours
  REMEMBER_ME: 30 * 24 * 60 * 60 * 1000 // 30 jours
};

// ==================== COURSE CONSTANTS ====================

/**
 * Niveaux de difficulté des cours
 * @constant {Object} COURSE_LEVELS
 */
export const COURSE_LEVELS = {
  BEGINNER: 'beginner',
  INTERMEDIATE: 'intermediate',
  ADVANCED: 'advanced',
  EXPERT: 'expert'
};

/**
 * Étiquettes des niveaux pour l'affichage
 * @constant {Object} COURSE_LEVEL_LABELS
 */
export const COURSE_LEVEL_LABELS = {
  [COURSE_LEVELS.BEGINNER]: 'Débutant',
  [COURSE_LEVELS.INTERMEDIATE]: 'Intermédiaire',
  [COURSE_LEVELS.ADVANCED]: 'Avancé',
  [COURSE_LEVELS.EXPERT]: 'Expert'
};

/**
 * Statuts des cours
 * @constant {Object} COURSE_STATUS
 */
export const COURSE_STATUS = {
  DRAFT: 'draft',
  PUBLISHED: 'published',
  ARCHIVED: 'archived',
  MAINTENANCE: 'maintenance'
};

/**
 * Types de contenu de cours
 * @constant {Object} CONTENT_TYPES
 */
export const CONTENT_TYPES = {
  VIDEO: 'video',
  TEXT: 'text',
  CODE: 'code',
  QUIZ: 'quiz',
  ASSIGNMENT: 'assignment',
  INTERACTIVE: 'interactive'
};

/**
 * Catégories de cours Java
 * @constant {Array<Object>} COURSE_CATEGORIES
 */
export const COURSE_CATEGORIES = [
  { id: 'fundamentals', name: 'Fondamentaux Java', icon: '☕' },
  { id: 'oop', name: 'Programmation Orientée Objet', icon: '🏗️' },
  { id: 'collections', name: 'Collections et Structures de Données', icon: '📚' },
  { id: 'concurrency', name: 'Programmation Concurrente', icon: '⚡' },
  { id: 'spring', name: 'Framework Spring', icon: '🌱' },
  { id: 'web', name: 'Développement Web', icon: '🌐' },
  { id: 'database', name: 'Base de Données', icon: '🗄️' },
  { id: 'testing', name: 'Tests et Qualité', icon: '🧪' },
  { id: 'algorithms', name: 'Algorithmes et Performance', icon: '🔢' },
  { id: 'design-patterns', name: 'Design Patterns', icon: '🎨' }
];

// ==================== PROGRESS CONSTANTS ====================

/**
 * États de progression des leçons
 * @constant {Object} LESSON_STATUS
 */
export const LESSON_STATUS = {
  NOT_STARTED: 'not_started',
  IN_PROGRESS: 'in_progress',
  COMPLETED: 'completed',
  LOCKED: 'locked'
};

/**
 * Types d'achievements disponibles
 * @constant {Object} ACHIEVEMENT_TYPES
 */
export const ACHIEVEMENT_TYPES = {
  COURSE_COMPLETION: 'course_completion',
  STREAK: 'streak',
  FIRST_CODE: 'first_code',
  SPEED_LEARNER: 'speed_learner',
  PERFECTIONIST: 'perfectionist',
  EXPLORER: 'explorer',
  MENTOR: 'mentor'
};

/**
 * Configuration des achievements
 * @constant {Object} ACHIEVEMENTS
 */
export const ACHIEVEMENTS = {
  [ACHIEVEMENT_TYPES.FIRST_CODE]: {
    title: 'Premier Code',
    description: 'Exécutez votre premier programme Java',
    icon: '🎯',
    points: 10
  },
  [ACHIEVEMENT_TYPES.COURSE_COMPLETION]: {
    title: 'Finisseur',
    description: 'Complétez un cours entier',
    icon: '🏆',
    points: 100
  },
  [ACHIEVEMENT_TYPES.STREAK]: {
    title: 'Régularité',
    description: 'Apprenez 7 jours consécutifs',
    icon: '🔥',
    points: 50
  },
  [ACHIEVEMENT_TYPES.SPEED_LEARNER]: {
    title: 'Apprenant Rapide',
    description: 'Complétez 10 leçons en une journée',
    icon: '⚡',
    points: 75
  },
  [ACHIEVEMENT_TYPES.PERFECTIONIST]: {
    title: 'Perfectionniste',
    description: 'Obtenez 100% à tous les quiz d\'un cours',
    icon: '💎',
    points: 150
  }
};

// ==================== UI CONSTANTS ====================

/**
 * Tailles d'écran pour le responsive design
 * @constant {Object} BREAKPOINTS
 */
export const BREAKPOINTS = {
  XS: '320px',
  SM: '576px',
  MD: '768px',
  LG: '992px',
  XL: '1200px',
  XXL: '1400px'
};

/**
 * Thèmes disponibles
 * @constant {Object} THEMES
 */
export const THEMES = {
  LIGHT: 'light',
  DARK: 'dark',
  AUTO: 'auto'
};

/**
 * Types de notifications
 * @constant {Object} NOTIFICATION_TYPES
 */
export const NOTIFICATION_TYPES = {
  SUCCESS: 'success',
  ERROR: 'error',
  WARNING: 'warning',
  INFO: 'info'
};

/**
 * Durées d'affichage des notifications (en millisecondes)
 * @constant {Object} NOTIFICATION_DURATION
 */
export const NOTIFICATION_DURATION = {
  SHORT: 3000,
  MEDIUM: 5000,
  LONG: 8000,
  PERSISTENT: 0
};

// ==================== CODE EDITOR CONSTANTS ====================

/**
 * Langages supportés par l'éditeur de code
 * @constant {Object} SUPPORTED_LANGUAGES
 */
export const SUPPORTED_LANGUAGES = {
  JAVA: 'java',
  JAVASCRIPT: 'javascript',
  HTML: 'html',
  CSS: 'css',
  SQL: 'sql'
};

/**
 * Thèmes de l'éditeur de code
 * @constant {Object} EDITOR_THEMES
 */
export const EDITOR_THEMES = {
  VS_LIGHT: 'vs-light',
  VS_DARK: 'vs-dark',
  HIGH_CONTRAST: 'hc-black'
};

/**
 * Tailles de police pour l'éditeur
 * @constant {Array<number>} EDITOR_FONT_SIZES
 */
export const EDITOR_FONT_SIZES = [10, 12, 14, 16, 18, 20, 22, 24];

// ==================== VALIDATION CONSTANTS ====================

/**
 * Règles de validation pour les mots de passe
 * @constant {Object} PASSWORD_RULES
 */
export const PASSWORD_RULES = {
  MIN_LENGTH: 8,
  MAX_LENGTH: 128,
  REQUIRE_UPPERCASE: true,
  REQUIRE_LOWERCASE: true,
  REQUIRE_NUMBERS: true,
  REQUIRE_SPECIAL_CHARS: true,
  SPECIAL_CHARS: '!@#$%^&*()_+-=[]{}|;:,.<>?'
};

/**
 * Règles de validation pour les emails
 * @constant {Object} EMAIL_RULES
 */
export const EMAIL_RULES = {
  MAX_LENGTH: 254,
  REGEX: /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/
};

/**
 * Règles de validation pour les noms d'utilisateur
 * @constant {Object} USERNAME_RULES
 */
export const USERNAME_RULES = {
  MIN_LENGTH: 3,
  MAX_LENGTH: 30,
  REGEX: /^[a-zA-Z0-9_-]+$/,
  RESERVED_NAMES: ['admin', 'root', 'system', 'support', 'help']
};

// ==================== FILE UPLOAD CONSTANTS ====================

/**
 * Configuration pour l'upload de fichiers
 * @constant {Object} FILE_UPLOAD
 */
export const FILE_UPLOAD = {
  MAX_SIZE: 5 * 1024 * 1024, // 5MB
  ALLOWED_IMAGE_TYPES: ['image/jpeg', 'image/png', 'image/gif', 'image/webp'],
  ALLOWED_DOCUMENT_TYPES: ['application/pdf', 'text/plain', '.java'],
  AVATAR_MAX_SIZE: 2 * 1024 * 1024 // 2MB pour les avatars
};

// ==================== PAGINATION CONSTANTS ====================

/**
 * Configuration de la pagination
 * @constant {Object} PAGINATION
 */
export const PAGINATION = {
  DEFAULT_PAGE_SIZE: 10,
  PAGE_SIZE_OPTIONS: [5, 10, 20, 50],
  MAX_PAGE_SIZE: 100
};

// ==================== ERROR MESSAGES ====================

/**
 * Messages d'erreur standardisés
 * @constant {Object} ERROR_MESSAGES
 */
export const ERROR_MESSAGES = {
  NETWORK_ERROR: 'Erreur de connexion. Veuillez vérifier votre connexion internet.',
  UNAUTHORIZED: 'Vous devez vous connecter pour accéder à cette ressource.',
  FORBIDDEN: 'Vous n\'avez pas les permissions nécessaires.',
  NOT_FOUND: 'La ressource demandée n\'a pas été trouvée.',
  SERVER_ERROR: 'Erreur serveur. Veuillez réessayer plus tard.',
  VALIDATION_ERROR: 'Les données fournies ne sont pas valides.',
  SESSION_EXPIRED: 'Votre session a expiré. Veuillez vous reconnecter.'
};

// ==================== SUCCESS MESSAGES ====================

/**
 * Messages de succès standardisés
 * @constant {Object} SUCCESS_MESSAGES
 */
export const SUCCESS_MESSAGES = {
  LOGIN_SUCCESS: 'Connexion réussie ! Bienvenue sur JavaCraft Academy.',
  REGISTER_SUCCESS: 'Inscription réussie ! Vérifiez votre email pour activer votre compte.',
  PROFILE_UPDATED: 'Votre profil a été mis à jour avec succès.',
  PASSWORD_CHANGED: 'Votre mot de passe a été modifié avec succès.',
  COURSE_ENROLLED: 'Vous êtes maintenant inscrit à ce cours !',
  LESSON_COMPLETED: 'Félicitations ! Vous avez terminé cette leçon.',
  CODE_EXECUTED: 'Code exécuté avec succès !'
};

// ==================== STORAGE KEYS ====================

/**
 * Clés pour le stockage local et de session
 * @constant {Object} STORAGE_KEYS
 */
export const STORAGE_KEYS = {
  ACCESS_TOKEN: 'jca_access_token',
  REFRESH_TOKEN: 'jca_refresh_token',
  USER_DATA: 'jca_user_data',
  THEME: 'jca_theme',
  LANGUAGE: 'jca_language',
  EDITOR_SETTINGS: 'jca_editor_settings',
  COURSE_PROGRESS: 'jca_course_progress',
  LAST_VISITED_COURSE: 'jca_last_course'
};

// ==================== EXPORT DEFAULT ====================

/**
 * Export par défaut des constantes principales
 * Utilisé pour un accès rapide aux constantes les plus courantes
 */
export default {
  API_ENDPOINTS,
  AUTH_STATUS,
  COURSE_LEVELS,
  NOTIFICATION_TYPES,
  THEMES,
  ERROR_MESSAGES,
  SUCCESS_MESSAGES
};

// ==================== CACHE CONFIGURATION ====================
/**
 * Clés de cache pour le stockage des données
 * @constant {Object} CACHE_KEYS
 */
export const CACHE_KEYS = {
  COURSES: 'courses',
  COURSE_DETAILS: 'course_details',
  USER_COURSES: 'user_courses',
  COURSE_CATEGORIES: 'course_categories',
  COURSE_SEARCH: 'course_search',
  COURSE_LESSONS: 'course_lessons',
  COURSE_PROGRESS: 'course_progress',
  POPULAR_COURSES: 'popular_courses',
  FEATURED_COURSES: 'featured_courses'
};

/**
 * Durées de cache (en millisecondes)
 * @constant {Object} CACHE_DURATION
 */
export const CACHE_DURATION = {
  SHORT: 5 * 60 * 1000,    // 5 minutes
  MEDIUM: 15 * 60 * 1000,  // 15 minutes
  LONG: 60 * 60 * 1000,    // 1 heure
  VERY_LONG: 24 * 60 * 60 * 1000 // 24 heures
};

// ==================== COURSE API ENDPOINTS ====================
/**
 * Endpoints spécifiques aux cours
 * @constant {Object} COURSE_ENDPOINTS
 */
export const COURSE_ENDPOINTS = {
  ALL: '/courses',
  BY_ID: '/courses/:id',
  SEARCH: '/courses/search',
  CATEGORIES: '/courses/categories',
  ENROLL: '/courses/:id/enroll',
  UNENROLL: '/courses/:id/unenroll',
  LESSONS: '/courses/:courseId/lessons',
  PROGRESS: '/courses/:courseId/progress',
  POPULAR: '/courses/popular',
  FEATURED: '/courses/featured',
  RECOMMENDATIONS: '/courses/recommendations',
  REVIEWS: '/courses/:id/reviews'
};