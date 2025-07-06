/**
 * @fileoverview Utilitaires pour la gestion de l'authentification
 * @author JavaCraft Academy
 * @version 1.0.0
 * 
 * Ce module fournit des utilitaires pour gérer l'authentification,
 * les tokens JWT, les permissions et la sécurité des sessions.
 * 
 * Relations avec l'application:
 * - Utilisé par AuthContext pour gérer l'état d'authentification
 * - Intégré avec authService pour les opérations d'authentification
 * - Utilisé par ProtectedRoute pour contrôler l'accès aux pages
 * - Connecté avec les hooks useAuth et useLocalStorage
 * - Utilisé par les composants LoginForm et RegisterForm
 */

import { jwtDecode } from 'jwt-decode';

/**
 * Clés de stockage pour l'authentification
 */
export const AUTH_STORAGE_KEYS = {
  ACCESS_TOKEN: 'javacraft_access_token',
  REFRESH_TOKEN: 'javacraft_refresh_token',
  USER_DATA: 'javacraft_user_data',
  REMEMBER_ME: 'javacraft_remember_me',
  LAST_LOGIN: 'javacraft_last_login',
  SESSION_TIMEOUT: 'javacraft_session_timeout',
  LAST_ACTIVITY: 'javacraft_last_activity',
  LOGIN_ATTEMPTS: 'javacraft_login_attempts',
  LOCKOUT_UNTIL: 'javacraft_lockout_until',
};

/**
 * Rôles utilisateur disponibles
 */
export const USER_ROLES = {
  STUDENT: 'STUDENT',
  INSTRUCTOR: 'INSTRUCTOR',
  ADMIN: 'ADMIN',
  MODERATOR: 'MODERATOR',
};

/**
 * Permissions associées aux rôles
 */
export const PERMISSIONS = {
  // Permissions étudiant
  VIEW_COURSES: 'VIEW_COURSES',
  ENROLL_COURSE: 'ENROLL_COURSE',
  SUBMIT_EXERCISE: 'SUBMIT_EXERCISE',
  VIEW_PROGRESS: 'VIEW_PROGRESS',
  
  // Permissions instructeur
  CREATE_COURSE: 'CREATE_COURSE',
  EDIT_COURSE: 'EDIT_COURSE',
  DELETE_COURSE: 'DELETE_COURSE',
  GRADE_EXERCISE: 'GRADE_EXERCISE',
  VIEW_STUDENT_PROGRESS: 'VIEW_STUDENT_PROGRESS',
  
  // Permissions admin
  MANAGE_USERS: 'MANAGE_USERS',
  MANAGE_SYSTEM: 'MANAGE_SYSTEM',
  VIEW_ANALYTICS: 'VIEW_ANALYTICS',
  EXPORT_DATA: 'EXPORT_DATA',
  
  // Permissions modérateur
  MODERATE_CONTENT: 'MODERATE_CONTENT',
  MANAGE_DISCUSSIONS: 'MANAGE_DISCUSSIONS',
};

/**
 * Mappage des rôles aux permissions
 */
export const ROLE_PERMISSIONS = {
  [USER_ROLES.STUDENT]: [
    PERMISSIONS.VIEW_COURSES,
    PERMISSIONS.ENROLL_COURSE,
    PERMISSIONS.SUBMIT_EXERCISE,
    PERMISSIONS.VIEW_PROGRESS,
  ],
  [USER_ROLES.INSTRUCTOR]: [
    PERMISSIONS.VIEW_COURSES,
    PERMISSIONS.CREATE_COURSE,
    PERMISSIONS.EDIT_COURSE,
    PERMISSIONS.DELETE_COURSE,
    PERMISSIONS.GRADE_EXERCISE,
    PERMISSIONS.VIEW_STUDENT_PROGRESS,
  ],
  [USER_ROLES.MODERATOR]: [
    PERMISSIONS.VIEW_COURSES,
    PERMISSIONS.MODERATE_CONTENT,
    PERMISSIONS.MANAGE_DISCUSSIONS,
  ],
  [USER_ROLES.ADMIN]: Object.values(PERMISSIONS),
};

/**
 * Configuration de sécurité
 */
export const SECURITY_CONFIG = {
  TOKEN_EXPIRY_BUFFER: 5 * 60 * 1000, // 5 minutes en millisecondes
  SESSION_TIMEOUT: 24 * 60 * 60 * 1000, // 24 heures
  MAX_LOGIN_ATTEMPTS: 5,
  LOCKOUT_DURATION: 15 * 60 * 1000, // 15 minutes
  PASSWORD_MIN_LENGTH: 8,
  PASSWORD_REQUIREMENTS: {
    uppercase: true,
    lowercase: true,
    numbers: true,
    symbols: false,
  },
  ACTIVITY_CHECK_INTERVAL: 60 * 1000, // 1 minute
  SESSION_WARNING_TIME: 5 * 60 * 1000, // 5 minutes avant expiration
};

/**
 * Stocke le token d'accès de manière sécurisée
 * @param {string} token - Token d'accès
 * @param {boolean} remember - Si true, stocke dans localStorage, sinon sessionStorage
 */
export const setAccessToken = (token, remember = false) => {
  if (!token) return;

  const storage = remember ? localStorage : sessionStorage;
  storage.setItem(AUTH_STORAGE_KEYS.ACCESS_TOKEN, token);
  
  if (remember) {
    localStorage.setItem(AUTH_STORAGE_KEYS.REMEMBER_ME, 'true');
  }
};

/**
 * Récupère le token d'accès
 * @returns {string|null} Token d'accès ou null
 */
export const getAccessToken = () => {
  return localStorage.getItem(AUTH_STORAGE_KEYS.ACCESS_TOKEN) ||
         sessionStorage.getItem(AUTH_STORAGE_KEYS.ACCESS_TOKEN);
};

/**
 * Stocke le token de rafraîchissement
 * @param {string} token - Token de rafraîchissement
 */
export const setRefreshToken = (token) => {
  if (!token) return;
  localStorage.setItem(AUTH_STORAGE_KEYS.REFRESH_TOKEN, token);
};

/**
 * Récupère le token de rafraîchissement
 * @returns {string|null} Token de rafraîchissement ou null
 */
export const getRefreshToken = () => {
  return localStorage.getItem(AUTH_STORAGE_KEYS.REFRESH_TOKEN);
};

/**
 * Stocke les données utilisateur
 * @param {Object} userData - Données utilisateur
 */
export const setUserData = (userData) => {
  if (!userData) return;
  
  const storage = localStorage.getItem(AUTH_STORAGE_KEYS.REMEMBER_ME) 
    ? localStorage 
    : sessionStorage;
  
  storage.setItem(AUTH_STORAGE_KEYS.USER_DATA, JSON.stringify(userData));
  localStorage.setItem(AUTH_STORAGE_KEYS.LAST_LOGIN, new Date().toISOString());
};

/**
 * Récupère les données utilisateur
 * @returns {Object|null} Données utilisateur ou null
 */
export const getUserData = () => {
  const userData = localStorage.getItem(AUTH_STORAGE_KEYS.USER_DATA) ||
                   sessionStorage.getItem(AUTH_STORAGE_KEYS.USER_DATA);
  
  return userData ? JSON.parse(userData) : null;
};

/**
 * Nettoie toutes les données d'authentification
 */
export const clearAuthData = () => {
  Object.values(AUTH_STORAGE_KEYS).forEach(key => {
    localStorage.removeItem(key);
    sessionStorage.removeItem(key);
  });
};

/**
 * Vérifie si un token JWT est valide
 * @param {string} token - Token JWT à vérifier
 * @returns {boolean} True si le token est valide
 */
export const isTokenValid = (token) => {
  if (!token) return false;

  try {
    const decoded = jwtDecode(token);
    const currentTime = Date.now() / 1000;
    
    // Vérifier l'expiration avec buffer de sécurité
    const expirationTime = decoded.exp - (SECURITY_CONFIG.TOKEN_EXPIRY_BUFFER / 1000);
    
    return currentTime < expirationTime;
  } catch (error) {
    console.error('Erreur de validation du token:', error);
    return false;
  }
};

/**
 * Décode un token JWT et retourne les informations utilisateur
 * @param {string} token - Token JWT à décoder
 * @returns {Object|null} Informations utilisateur ou null
 */
export const decodeToken = (token) => {
  if (!token) return null;

  try {
    return jwtDecode(token);
  } catch (error) {
    console.error('Erreur de décodage du token:', error);
    return null;
  }
};

/**
 * Vérifie si l'utilisateur est authentifié
 * @returns {boolean} True si l'utilisateur est authentifié
 */
export const isAuthenticated = () => {
  const token = getAccessToken();
  return token && isTokenValid(token);
};

/**
 * Vérifie si l'utilisateur a un rôle spécifique
 * @param {string} role - Rôle à vérifier
 * @returns {boolean} True si l'utilisateur a le rôle
 */
export const hasRole = (role) => {
  const userData = getUserData();
  return userData && userData.roles && userData.roles.includes(role);
};

/**
 * Vérifie si l'utilisateur a une permission spécifique
 * @param {string} permission - Permission à vérifier
 * @returns {boolean} True si l'utilisateur a la permission
 */
export const hasPermission = (permission) => {
  const userData = getUserData();
  if (!userData || !userData.roles) return false;

  return userData.roles.some(role => 
    ROLE_PERMISSIONS[role] && ROLE_PERMISSIONS[role].includes(permission)
  );
};

/**
 * Vérifie si l'utilisateur a toutes les permissions spécifiées
 * @param {string[]} permissions - Permissions à vérifier
 * @returns {boolean} True si l'utilisateur a toutes les permissions
 */
export const hasAllPermissions = (permissions) => {
  return permissions.every(permission => hasPermission(permission));
};

/**
 * Vérifie si l'utilisateur a au moins une des permissions spécifiées
 * @param {string[]} permissions - Permissions à vérifier
 * @returns {boolean} True si l'utilisateur a au moins une permission
 */
export const hasAnyPermission = (permissions) => {
  return permissions.some(permission => hasPermission(permission));
};

/**
 * Obtient toutes les permissions de l'utilisateur
 * @returns {string[]} Liste des permissions
 */
export const getUserPermissions = () => {
  const userData = getUserData();
  if (!userData || !userData.roles) return [];

  const permissions = new Set();
  userData.roles.forEach(role => {
    if (ROLE_PERMISSIONS[role]) {
      ROLE_PERMISSIONS[role].forEach(permission => permissions.add(permission));
    }
  });

  return Array.from(permissions);
};

/**
 * Vérifie si le mot de passe respecte les critères de sécurité
 * @param {string} password - Mot de passe à vérifier
 * @returns {Object} Résultat de la validation
 */
export const validatePassword = (password) => {
  const requirements = SECURITY_CONFIG.PASSWORD_REQUIREMENTS;
  const errors = [];

  if (!password) {
    return { isValid: false, errors: ['Le mot de passe est requis'] };
  }

  if (password.length < SECURITY_CONFIG.PASSWORD_MIN_LENGTH) {
    errors.push(`Le mot de passe doit contenir au moins ${SECURITY_CONFIG.PASSWORD_MIN_LENGTH} caractères`);
  }

  if (requirements.uppercase && !/[A-Z]/.test(password)) {
    errors.push('Le mot de passe doit contenir au moins une lettre majuscule');
  }

  if (requirements.lowercase && !/[a-z]/.test(password)) {
    errors.push('Le mot de passe doit contenir au moins une lettre minuscule');
  }

  if (requirements.numbers && !/\d/.test(password)) {
    errors.push('Le mot de passe doit contenir au moins un chiffre');
  }

  if (requirements.symbols && !/[!@#$%^&*(),.?":{}|<>]/.test(password)) {
    errors.push('Le mot de passe doit contenir au moins un symbole');
  }

  return {
    isValid: errors.length === 0,
    errors,
  };
};

/**
 * Génère un mot de passe sécurisé
 * @param {number} length - Longueur du mot de passe
 * @returns {string} Mot de passe généré
 */
export const generateSecurePassword = (length = 12) => {
  const uppercase = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ';
  const lowercase = 'abcdefghijklmnopqrstuvwxyz';
  const numbers = '0123456789';
  const symbols = '!@#$%^&*(),.?":{}|<>';
  
  let charset = '';
  let password = '';
  
  // Garantir qu'au moins un caractère de chaque type requis est présent
  if (SECURITY_CONFIG.PASSWORD_REQUIREMENTS.uppercase) {
    charset += uppercase;
    password += uppercase.charAt(Math.floor(Math.random() * uppercase.length));
  }
  
  if (SECURITY_CONFIG.PASSWORD_REQUIREMENTS.lowercase) {
    charset += lowercase;
    password += lowercase.charAt(Math.floor(Math.random() * lowercase.length));
  }
  
  if (SECURITY_CONFIG.PASSWORD_REQUIREMENTS.numbers) {
    charset += numbers;
    password += numbers.charAt(Math.floor(Math.random() * numbers.length));
  }
  
  if (SECURITY_CONFIG.PASSWORD_REQUIREMENTS.symbols) {
    charset += symbols;
    password += symbols.charAt(Math.floor(Math.random() * symbols.length));
  }
  
  // Compléter avec des caractères aléatoires
  for (let i = password.length; i < length; i++) {
    password += charset.charAt(Math.floor(Math.random() * charset.length));
  }
  
  // Mélanger les caractères
  return password.split('').sort(() => Math.random() - 0.5).join('');
};

/**
 * Vérifie si une session a expiré
 * @returns {boolean} True si la session a expiré
 */
export const isSessionExpired = () => {
  const lastActivity = localStorage.getItem(AUTH_STORAGE_KEYS.LAST_ACTIVITY);
  if (!lastActivity) return true;

  const lastActivityTime = new Date(lastActivity).getTime();
  const currentTime = Date.now();
  const sessionTimeout = SECURITY_CONFIG.SESSION_TIMEOUT;

  return (currentTime - lastActivityTime) > sessionTimeout;
};

/**
 * Obtient le temps restant avant expiration de la session
 * @returns {number} Temps restant en millisecondes
 */
export const getSessionTimeRemaining = () => {
  const lastActivity = localStorage.getItem(AUTH_STORAGE_KEYS.LAST_ACTIVITY);
  if (!lastActivity) return 0;

  const lastActivityTime = new Date(lastActivity).getTime();
  const currentTime = Date.now();
  const sessionTimeout = SECURITY_CONFIG.SESSION_TIMEOUT;
  const timeRemaining = sessionTimeout - (currentTime - lastActivityTime);

  return Math.max(0, timeRemaining);
};

/**
 * Met à jour le timestamp de dernière activité
 */
export const updateLastActivity = () => {
  if (isAuthenticated()) {
    localStorage.setItem(AUTH_STORAGE_KEYS.LAST_ACTIVITY, new Date().toISOString());
  }
};

/**
 * Vérifie si l'utilisateur est verrouillé après trop de tentatives de connexion
 * @returns {boolean} True si l'utilisateur est verrouillé
 */
export const isUserLocked = () => {
  const lockoutUntil = localStorage.getItem(AUTH_STORAGE_KEYS.LOCKOUT_UNTIL);
  if (!lockoutUntil) return false;

  const lockoutTime = new Date(lockoutUntil).getTime();
  const currentTime = Date.now();

  return currentTime < lockoutTime;
};

/**
 * Incrémente le compteur de tentatives de connexion
 * @param {string} identifier - Identifiant de l'utilisateur (email, nom d'utilisateur)
 */
export const incrementLoginAttempts = (identifier) => {
  const key = `${AUTH_STORAGE_KEYS.LOGIN_ATTEMPTS}_${identifier}`;
  const attempts = parseInt(localStorage.getItem(key) || '0', 10);
  const newAttempts = attempts + 1;
  
  localStorage.setItem(key, newAttempts.toString());
  
  // Verrouiller l'utilisateur si le maximum est atteint
  if (newAttempts >= SECURITY_CONFIG.MAX_LOGIN_ATTEMPTS) {
    const lockoutUntil = new Date(Date.now() + SECURITY_CONFIG.LOCKOUT_DURATION);
    localStorage.setItem(AUTH_STORAGE_KEYS.LOCKOUT_UNTIL, lockoutUntil.toISOString());
  }
};

/**
 * Réinitialise le compteur de tentatives de connexion
 * @param {string} identifier - Identifiant de l'utilisateur
 */
export const resetLoginAttempts = (identifier) => {
  const key = `${AUTH_STORAGE_KEYS.LOGIN_ATTEMPTS}_${identifier}`;
  localStorage.removeItem(key);
  localStorage.removeItem(AUTH_STORAGE_KEYS.LOCKOUT_UNTIL);
};

/**
 * Obtient le nombre de tentatives de connexion restantes
 * @param {string} identifier - Identifiant de l'utilisateur
 * @returns {number} Nombre de tentatives restantes
 */
export const getRemainingLoginAttempts = (identifier) => {
  const key = `${AUTH_STORAGE_KEYS.LOGIN_ATTEMPTS}_${identifier}`;
  const attempts = parseInt(localStorage.getItem(key) || '0', 10);
  return Math.max(0, SECURITY_CONFIG.MAX_LOGIN_ATTEMPTS - attempts);
};

/**
 * Obtient le temps restant avant déblocage
 * @returns {number} Temps restant en millisecondes
 */
export const getLockoutTimeRemaining = () => {
  const lockoutUntil = localStorage.getItem(AUTH_STORAGE_KEYS.LOCKOUT_UNTIL);
  if (!lockoutUntil) return 0;

  const lockoutTime = new Date(lockoutUntil).getTime();
  const currentTime = Date.now();
  const timeRemaining = lockoutTime - currentTime;

  return Math.max(0, timeRemaining);
};

/**
 * Formate le temps restant en format lisible
 * @param {number} milliseconds - Temps en millisecondes
 * @returns {string} Temps formaté
 */
export const formatTimeRemaining = (milliseconds) => {
  const seconds = Math.floor(milliseconds / 1000);
  const minutes = Math.floor(seconds / 60);
  const hours = Math.floor(minutes / 60);

  if (hours > 0) {
    return `${hours}h ${minutes % 60}m`;
  } else if (minutes > 0) {
    return `${minutes}m ${seconds % 60}s`;
  } else {
    return `${seconds}s`;
  }
};

/**
 * Vérifie si l'utilisateur doit être averti de l'expiration de session
 * @returns {boolean} True si l'utilisateur doit être averti
 */
export const shouldShowSessionWarning = () => {
  const timeRemaining = getSessionTimeRemaining();
  return timeRemaining > 0 && timeRemaining <= SECURITY_CONFIG.SESSION_WARNING_TIME;
};

/**
 * Prolonge la session utilisateur
 */
export const extendSession = () => {
  if (isAuthenticated()) {
    updateLastActivity();
    return true;
  }
  return false;
};

/**
 * Obtient les informations de sécurité de la session
 * @returns {Object} Informations de sécurité
 */
export const getSecurityInfo = () => {
  const userData = getUserData();
  const token = getAccessToken();
  const decodedToken = decodeToken(token);
  
  return {
    isAuthenticated: isAuthenticated(),
    isSessionExpired: isSessionExpired(),
    sessionTimeRemaining: getSessionTimeRemaining(),
    shouldShowWarning: shouldShowSessionWarning(),
    userRoles: userData?.roles || [],
    userPermissions: getUserPermissions(),
    tokenExpiry: decodedToken?.exp ? new Date(decodedToken.exp * 1000) : null,
    lastActivity: localStorage.getItem(AUTH_STORAGE_KEYS.LAST_ACTIVITY),
    isLocked: isUserLocked(),
    lockoutTimeRemaining: getLockoutTimeRemaining(),
  };
};

/**
 * Nettoie les données expirées
 */
export const cleanupExpiredData = () => {
  // Nettoyer les tentatives de connexion expirées
  Object.keys(localStorage).forEach(key => {
    if (key.startsWith(AUTH_STORAGE_KEYS.LOGIN_ATTEMPTS)) {
      // Supprimer les tentatives anciennes (plus de 24h)
      const timestamp = localStorage.getItem(`${key}_timestamp`);
      if (timestamp && (Date.now() - new Date(timestamp).getTime()) > 24 * 60 * 60 * 1000) {
        localStorage.removeItem(key);
        localStorage.removeItem(`${key}_timestamp`);
      }
    }
  });

  // Nettoyer le verrouillage expiré
  if (!isUserLocked()) {
    localStorage.removeItem(AUTH_STORAGE_KEYS.LOCKOUT_UNTIL);
  }
};

/**
 * Initialise le système d'authentification
 */
export const initializeAuth = () => {
  // Nettoyer les données expirées
  cleanupExpiredData();
  
  // Mettre à jour la dernière activité si authentifié
  if (isAuthenticated()) {
    updateLastActivity();
  }
  
  // Configurer le nettoyage automatique
  setInterval(cleanupExpiredData, 60 * 60 * 1000); // Toutes les heures
};

// Exporter les utilitaires par défaut
export default {
  AUTH_STORAGE_KEYS,
  USER_ROLES,
  PERMISSIONS,
  ROLE_PERMISSIONS,
  SECURITY_CONFIG,
  setAccessToken,
  getAccessToken,
  setRefreshToken,
  getRefreshToken,
  setUserData,
  getUserData,
  clearAuthData,
  isTokenValid,
  decodeToken,
  isAuthenticated,
  hasRole,
  hasPermission,
  hasAllPermissions,
  hasAnyPermission,
  getUserPermissions,
  validatePassword,
  generateSecurePassword,
  isSessionExpired,
  getSessionTimeRemaining,
  updateLastActivity,
  isUserLocked,
  incrementLoginAttempts,
  resetLoginAttempts,
  getRemainingLoginAttempts,
  getLockoutTimeRemaining,
  formatTimeRemaining,
  shouldShowSessionWarning,
  extendSession,
  getSecurityInfo,
  cleanupExpiredData,
  initializeAuth,
};