
/**
 * @fileoverview Fichier des fonctions utilitaires pour l'application JavaCraft Academy
 * @description Ce fichier contient des fonctions d'aide réutilisables dans toute l'application
 * Relations : Utilisé par tous les composants, pages, services et hooks de l'application
 * @author JavaCraft Academy Team
 * @version 1.0.0
 */

/**
 * Formate une chaîne de caractères en supprimant les espaces et en convertissant en minuscules
 * @param {string} str - La chaîne à formater
 * @returns {string} La chaîne formatée
 * @example
 * formatString("  Hello World  ") // "hello world"
 */
export const formatString = (str) => {
  if (!str || typeof str !== 'string') return '';
  return str.trim().toLowerCase();
};

/**
 * Capitalise la première lettre d'une chaîne
 * @param {string} str - La chaîne à capitaliser
 * @returns {string} La chaîne avec la première lettre en majuscule
 * @example
 * capitalize("hello world") // "Hello world"
 */
export const capitalize = (str) => {
  if (!str || typeof str !== 'string') return '';
  return str.charAt(0).toUpperCase() + str.slice(1).toLowerCase();
};

/**
 * Convertit une chaîne en format title case
 * @param {string} str - La chaîne à convertir
 * @returns {string} La chaîne en title case
 * @example
 * toTitleCase("hello world") // "Hello World"
 */
export const toTitleCase = (str) => {
  if (!str || typeof str !== 'string') return '';
  return str.replace(/\w\S*/g, (txt) => 
    txt.charAt(0).toUpperCase() + txt.substr(1).toLowerCase()
  );
};

/**
 * Génère un identifiant unique basé sur timestamp et nombre aléatoire
 * @returns {string} Identifiant unique
 * @example
 * generateId() // "1640995200000-abc123"
 */
export const generateId = () => {
  return `${Date.now()}-${Math.random().toString(36).substr(2, 6)}`;
};

/**
 * Génère un slug URL-friendly à partir d'une chaîne
 * @param {string} str - La chaîne à convertir en slug
 * @returns {string} Le slug généré
 * @example
 * generateSlug("Introduction to Java Programming") // "introduction-to-java-programming"
 */
export const generateSlug = (str) => {
  if (!str || typeof str !== 'string') return '';
  return str
    .toLowerCase()
    .trim()
    .replace(/[^\w\s-]/g, '') // Supprime les caractères spéciaux
    .replace(/[\s_-]+/g, '-') // Remplace espaces et underscores par des tirets
    .replace(/^-+|-+$/g, ''); // Supprime les tirets en début/fin
};

/**
 * Debounce une fonction pour éviter les appels trop fréquents
 * @param {Function} func - La fonction à debouncer
 * @param {number} wait - Le délai d'attente en millisecondes
 * @returns {Function} La fonction debouncée
 * @example
 * const debouncedSearch = debounce(searchFunction, 300);
 */
export const debounce = (func, wait) => {
  let timeout;
  return function executedFunction(...args) {
    const later = () => {
      clearTimeout(timeout);
      func(...args);
    };
    clearTimeout(timeout);
    timeout = setTimeout(later, wait);
  };
};

/**
 * Throttle une fonction pour limiter le nombre d'appels
 * @param {Function} func - La fonction à throttler
 * @param {number} limit - La limite en millisecondes
 * @returns {Function} La fonction throttlée
 * @example
 * const throttledScroll = throttle(onScrollFunction, 100);
 */
export const throttle = (func, limit) => {
  let inThrottle;
  return function executedFunction(...args) {
    if (!inThrottle) {
      func.apply(this, args);
      inThrottle = true;
      setTimeout(() => inThrottle = false, limit);
    }
  };
};

/**
 * Clone profond d'un objet ou tableau
 * @param {*} obj - L'objet à cloner
 * @returns {*} La copie profonde de l'objet
 * @example
 * const cloned = deepClone({a: {b: 1}});
 */
export const deepClone = (obj) => {
  if (obj === null || typeof obj !== 'object') return obj;
  if (obj instanceof Date) return new Date(obj.getTime());
  if (obj instanceof Array) return obj.map(item => deepClone(item));
  if (typeof obj === 'object') {
    const copy = {};
    Object.keys(obj).forEach(key => {
      copy[key] = deepClone(obj[key]);
    });
    return copy;
  }
};

/**
 * Fusionne deux objets de manière profonde
 * @param {Object} target - L'objet cible
 * @param {Object} source - L'objet source
 * @returns {Object} L'objet fusionné
 * @example
 * const merged = deepMerge({a: 1}, {b: 2}); // {a: 1, b: 2}
 */
export const deepMerge = (target, source) => {
  const result = deepClone(target);
  
  for (const key in source) {
    if (source.hasOwnProperty(key)) {
      if (source[key] && typeof source[key] === 'object' && !Array.isArray(source[key])) {
        result[key] = deepMerge(result[key] || {}, source[key]);
      } else {
        result[key] = source[key];
      }
    }
  }
  
  return result;
};

/**
 * Formate la taille d'un fichier en format lisible
 * @param {number} bytes - La taille en bytes
 * @param {number} decimals - Le nombre de décimales (défaut: 2)
 * @returns {string} La taille formatée
 * @example
 * formatFileSize(1024) // "1.00 KB"
 */
export const formatFileSize = (bytes, decimals = 2) => {
  if (bytes === 0) return '0 Bytes';
  
  const k = 1024;
  const dm = decimals < 0 ? 0 : decimals;
  const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB'];
  
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  
  return parseFloat((bytes / Math.pow(k, i)).toFixed(dm)) + ' ' + sizes[i];
};

/**
 * Calcule le pourcentage de progression
 * @param {number} current - La valeur actuelle
 * @param {number} total - La valeur totale
 * @returns {number} Le pourcentage (0-100)
 * @example
 * calculateProgress(5, 10) // 50
 */
export const calculateProgress = (current, total) => {
  if (!total || total <= 0) return 0;
  const percentage = Math.round((current / total) * 100);
  return Math.min(100, Math.max(0, percentage));
};

/**
 * Formate une durée en secondes en format lisible
 * @param {number} seconds - La durée en secondes
 * @returns {string} La durée formatée (HH:MM:SS ou MM:SS)
 * @example
 * formatDuration(3661) // "01:01:01"
 * formatDuration(61) // "01:01"
 */
export const formatDuration = (seconds) => {
  if (!seconds || seconds < 0) return '00:00';
  
  const hours = Math.floor(seconds / 3600);
  const minutes = Math.floor((seconds % 3600) / 60);
  const secs = Math.floor(seconds % 60);
  
  if (hours > 0) {
    return `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
  }
  
  return `${minutes.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
};

/**
 * Extrait les initiales d'un nom complet
 * @param {string} fullName - Le nom complet
 * @param {number} maxInitials - Nombre maximum d'initiales (défaut: 2)
 * @returns {string} Les initiales
 * @example
 * getInitials("John Doe Smith") // "JD"
 * getInitials("John Doe Smith", 3) // "JDS"
 */
export const getInitials = (fullName, maxInitials = 2) => {
  if (!fullName || typeof fullName !== 'string') return '';
  
  return fullName
    .split(' ')
    .filter(name => name.length > 0)
    .slice(0, maxInitials)
    .map(name => name.charAt(0).toUpperCase())
    .join('');
};

/**
 * Génère une couleur aléatoire en format hexadécimal
 * @returns {string} Couleur hexadécimale
 * @example
 * generateRandomColor() // "#a3c4f7"
 */
export const generateRandomColor = () => {
  return '#' + Math.floor(Math.random() * 16777215).toString(16).padStart(6, '0');
};

/**
 * Génère une couleur basée sur une chaîne (pour des avatars cohérents)
 * @param {string} str - La chaîne de base
 * @returns {string} Couleur hexadécimale
 * @example
 * generateColorFromString("john@example.com") // "#4a90e2"
 */
export const generateColorFromString = (str) => {
  if (!str) return generateRandomColor();
  
  let hash = 0;
  for (let i = 0; i < str.length; i++) {
    hash = str.charCodeAt(i) + ((hash << 5) - hash);
  }
  
  const hue = Math.abs(hash) % 360;
  return `hsl(${hue}, 65%, 50%)`;
};

/**
 * Tronque un texte à une longueur donnée
 * @param {string} text - Le texte à tronquer
 * @param {number} length - La longueur maximale
 * @param {string} suffix - Le suffixe à ajouter (défaut: "...")
 * @returns {string} Le texte tronqué
 * @example
 * truncateText("Lorem ipsum dolor sit amet", 10) // "Lorem ipsu..."
 */
export const truncateText = (text, length, suffix = '...') => {
  if (!text || typeof text !== 'string') return '';
  if (text.length <= length) return text;
  
  return text.slice(0, length).trim() + suffix;
};

/**
 * Convertit les paramètres d'URL en objet
 * @param {string} search - La chaîne de requête (window.location.search)
 * @returns {Object} Objet contenant les paramètres
 * @example
 * parseUrlParams("?page=1&sort=name") // {page: "1", sort: "name"}
 */
export const parseUrlParams = (search) => {
  if (!search) return {};
  
  const params = new URLSearchParams(search.startsWith('?') ? search.slice(1) : search);
  const result = {};
  
  for (const [key, value] of params.entries()) {
    result[key] = value;
  }
  
  return result;
};

/**
 * Convertit un objet en paramètres d'URL
 * @param {Object} params - L'objet à convertir
 * @returns {string} La chaîne de paramètres
 * @example
 * objectToUrlParams({page: 1, sort: "name"}) // "page=1&sort=name"
 */
export const objectToUrlParams = (params) => {
  if (!params || typeof params !== 'object') return '';
  
  return Object.entries(params)
    .filter(([key, value]) => value !== null && value !== undefined && value !== '')
    .map(([key, value]) => `${encodeURIComponent(key)}=${encodeURIComponent(value)}`)
    .join('&');
};

/**
 * Détecte le type de navigateur
 * @returns {string} Le nom du navigateur
 * @example
 * detectBrowser() // "chrome", "firefox", "safari", "edge", "opera", "unknown"
 */
export const detectBrowser = () => {
  const userAgent = navigator.userAgent.toLowerCase();
  
  if (userAgent.includes('chrome') && !userAgent.includes('edg')) return 'chrome';
  if (userAgent.includes('firefox')) return 'firefox';
  if (userAgent.includes('safari') && !userAgent.includes('chrome')) return 'safari';
  if (userAgent.includes('edg')) return 'edge';
  if (userAgent.includes('opera')) return 'opera';
  
  return 'unknown';
};

/**
 * Détecte si l'utilisateur est sur mobile
 * @returns {boolean} True si mobile, false sinon
 * @example
 * isMobile() // true/false
 */
export const isMobile = () => {
  return /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent);
};

/**
 * Copie du texte dans le presse-papiers
 * @param {string} text - Le texte à copier
 * @returns {Promise<boolean>} Promise qui résout avec le succès de l'opération
 * @example
 * copyToClipboard("Hello World").then(success => console.log(success));
 */
export const copyToClipboard = async (text) => {
  try {
    if (navigator.clipboard && window.isSecureContext) {
      await navigator.clipboard.writeText(text);
      return true;
    } else {
      // Fallback pour les navigateurs plus anciens
      const textArea = document.createElement('textarea');
      textArea.value = text;
      textArea.style.position = 'fixed';
      textArea.style.left = '-999999px';
      textArea.style.top = '-999999px';
      document.body.appendChild(textArea);
      textArea.focus();
      textArea.select();
      const result = document.execCommand('copy');
      document.body.removeChild(textArea);
      return result;
    }
  } catch (error) {
    console.error('Erreur lors de la copie:', error);
    return false;
  }
};

/**
 * Valide et nettoie le code Java pour l'éditeur
 * @param {string} code - Le code à nettoyer
 * @returns {string} Le code nettoyé
 * @example
 * sanitizeJavaCode("public class Test { }") // Code nettoyé
 */
export const sanitizeJavaCode = (code) => {
  if (!code || typeof code !== 'string') return '';
  
  // Supprime les caractères dangereux tout en préservant la syntaxe Java
  return code
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/&(?!lt;|gt;|amp;|quot;|#39;)/g, '&amp;');
};

/**
 * Retourne la configuration par défaut pour l'éditeur de code
 * @returns {Object} Configuration de l'éditeur
 */
export const getDefaultEditorConfig = () => ({
  theme: 'vs-dark',
  language: 'java',
  fontSize: 14,
  wordWrap: 'on',
  lineNumbers: 'on',
  minimap: { enabled: false },
  scrollBeyondLastLine: false,
  automaticLayout: true,
  tabSize: 4,
  insertSpaces: true,
  formatOnPaste: true,
  formatOnType: true
});

/**
 * Relations avec l'application :
 * - Utilisé par tous les composants pour le formatage et la validation
 * - AuthContext utilise generateId() pour les tokens
 * - CourseContext utilise calculateProgress() pour la progression
 * - LessonPlayer utilise formatDuration() pour les vidéos
 * - CodeEditor utilise sanitizeJavaCode() et getDefaultEditorConfig()
 * - UserProfile utilise getInitials() et generateColorFromString() pour les avatars
 * - Tous les formulaires utilisent debounce() pour l'optimisation
 * - Navigation utilise parseUrlParams() et objectToUrlParams()
 * - Services API utilisent deepClone() et deepMerge()
 * - Composants responsive utilisent isMobile()
 * - NotificationService utilise copyToClipboard()
 */