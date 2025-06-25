/**
 * @fileoverview Fichier des fonctions de validation pour l'application JavaCraft Academy
 * @description Ce fichier contient toutes les fonctions de validation utilisées dans l'application
 * Relations : Utilisé principalement par les formulaires, hooks et services de l'application
 * @author JavaCraft Academy Team
 * @version 1.0.0
 */

/**
 * Configuration des expressions régulières pour les validations
 */
const REGEX_PATTERNS = {
  EMAIL: /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/,
  PASSWORD_STRONG: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]/,
  PHONE: /^\+?[1-9]\d{1,14}$/,
  USERNAME: /^[a-zA-Z0-9_.-]{3,20}$/,
  URL: /^https?:\/\/(www\.)?[-a-zA-Z0-9@:%._\+~#=]{1,256}\.[a-zA-Z0-9()]{1,6}\b([-a-zA-Z0-9()@:%_\+.~#?&//=]*)$/,
  JAVA_CLASS_NAME: /^[A-Z][a-zA-Z0-9_]*$/,
  JAVA_VARIABLE_NAME: /^[a-z_$][a-zA-Z0-9_$]*$/,
  SLUG: /^[a-z0-9]+(?:-[a-z0-9]+)*$/
};

/**
 * Messages d'erreur par défaut
 */
const ERROR_MESSAGES = {
  REQUIRED: 'Ce champ est obligatoire',
  EMAIL_INVALID: 'Adresse e-mail invalide',
  PASSWORD_TOO_SHORT: 'Le mot de passe doit contenir au moins 8 caractères',
  PASSWORD_TOO_WEAK: 'Le mot de passe doit contenir au moins une majuscule, une minuscule, un chiffre et un caractère spécial',
  USERNAME_INVALID: 'Le nom d\'utilisateur doit contenir entre 3 et 20 caractères alphanumériques',
  PHONE_INVALID: 'Numéro de téléphone invalide',
  URL_INVALID: 'URL invalide',
  FILE_TOO_LARGE: 'Le fichier est trop volumineux',
  FILE_TYPE_INVALID: 'Type de fichier non autorisé',
  JAVA_CLASS_NAME_INVALID: 'Le nom de classe doit commencer par une majuscule et contenir uniquement des lettres, chiffres et underscores',
  JAVA_VARIABLE_NAME_INVALID: 'Le nom de variable doit commencer par une minuscule et contenir uniquement des lettres, chiffres, underscores et $'
};

/**
 * Classe pour gérer les résultats de validation
 */
class ValidationResult {
  constructor(isValid = true, errors = []) {
    this.isValid = isValid;
    this.errors = Array.isArray(errors) ? errors : [errors];
  }

  addError(error) {
    this.errors.push(error);
    this.isValid = false;
    return this;
  }

  merge(otherResult) {
    if (!otherResult.isValid) {
      this.isValid = false;
      this.errors.push(...otherResult.errors);
    }
    return this;
  }

  getFirstError() {
    return this.errors.length > 0 ? this.errors[0] : null;
  }
}

/**
 * Valide si une valeur n'est pas vide
 * @param {*} value - La valeur à valider
 * @param {string} fieldName - Le nom du champ (optionnel)
 * @returns {ValidationResult} Résultat de la validation
 * @example
 * validateRequired("test") // {isValid: true, errors: []}
 * validateRequired("") // {isValid: false, errors: ["Ce champ est obligatoire"]}
 */
export const validateRequired = (value, fieldName = '') => {
  const isEmpty = value === null || value === undefined || 
    (typeof value === 'string' && value.trim() === '') ||
    (Array.isArray(value) && value.length === 0);
  
  if (isEmpty) {
    const message = fieldName 
      ? `${fieldName} est obligatoire`
      : ERROR_MESSAGES.REQUIRED;
    return new ValidationResult(false, [message]);
  }
  
  return new ValidationResult();
};

/**
 * Valide une adresse e-mail
 * @param {string} email - L'adresse e-mail à valider
 * @returns {ValidationResult} Résultat de la validation
 * @example
 * validateEmail("user@example.com") // {isValid: true, errors: []}
 * validateEmail("invalid-email") // {isValid: false, errors: ["Adresse e-mail invalide"]}
 */
export const validateEmail = (email) => {
  const result = new ValidationResult();
  
  if (!email || typeof email !== 'string') {
    return result.addError(ERROR_MESSAGES.EMAIL_INVALID);
  }
  
  const trimmedEmail = email.trim().toLowerCase();
  
  if (!REGEX_PATTERNS.EMAIL.test(trimmedEmail)) {
    return result.addError(ERROR_MESSAGES.EMAIL_INVALID);
  }
  
  // Vérifications supplémentaires
  if (trimmedEmail.length > 254) {
    return result.addError('Adresse e-mail trop longue');
  }
  
  const localPart = trimmedEmail.split('@')[0];
  if (localPart.length > 64) {
    return result.addError('Partie locale de l\'e-mail trop longue');
  }
  
  return result;
};

/**
 * Valide un mot de passe
 * @param {string} password - Le mot de passe à valider
 * @param {Object} options - Options de validation
 * @param {number} options.minLength - Longueur minimale (défaut: 8)
 * @param {boolean} options.requireStrong - Exiger un mot de passe fort (défaut: true)
 * @returns {ValidationResult} Résultat de la validation
 * @example
 * validatePassword("Password123!") // {isValid: true, errors: []}
 * validatePassword("weak") // {isValid: false, errors: [...]}
 */
export const validatePassword = (password, options = {}) => {
  const { minLength = 8, requireStrong = true } = options;
  const result = new ValidationResult();
  
  if (!password || typeof password !== 'string') {
    return result.addError('Mot de passe requis');
  }
  
  if (password.length < minLength) {
    return result.addError(`Le mot de passe doit contenir au moins ${minLength} caractères`);
  }
  
  if (requireStrong && !REGEX_PATTERNS.PASSWORD_STRONG.test(password)) {
    return result.addError(ERROR_MESSAGES.PASSWORD_TOO_WEAK);
  }
  
  // Vérifications supplémentaires de sécurité
  if (password.length > 128) {
    return result.addError('Mot de passe trop long (maximum 128 caractères)');
  }
  
  return result;
};

/**
 * Valide la correspondance de deux mots de passe
 * @param {string} password - Le mot de passe principal
 * @param {string} confirmPassword - La confirmation du mot de passe
 * @returns {ValidationResult} Résultat de la validation
 * @example
 * validatePasswordMatch("pass123", "pass123") // {isValid: true, errors: []}
 * validatePasswordMatch("pass123", "pass456") // {isValid: false, errors: [...]}
 */
export const validatePasswordMatch = (password, confirmPassword) => {
  const result = new ValidationResult();
  
  if (password !== confirmPassword) {
    return result.addError('Les mots de passe ne correspondent pas');
  }
  
  return result;
};

/**
 * Valide un nom d'utilisateur
 * @param {string} username - Le nom d'utilisateur à valider
 * @returns {ValidationResult} Résultat de la validation
 * @example
 * validateUsername("john_doe") // {isValid: true, errors: []}
 * validateUsername("jo") // {isValid: false, errors: [...]}
 */
export const validateUsername = (username) => {
  const result = new ValidationResult();
  
  if (!username || typeof username !== 'string') {
    return result.addError('Nom d\'utilisateur requis');
  }
  
  const trimmedUsername = username.trim();
  
  if (!REGEX_PATTERNS.USERNAME.test(trimmedUsername)) {
    return result.addError(ERROR_MESSAGES.USERNAME_INVALID);
  }
  
  // Mots réservés à éviter
  const reservedWords = ['admin', 'root', 'system', 'api', 'www', 'null', 'undefined'];
  if (reservedWords.includes(trimmedUsername.toLowerCase())) {
    return result.addError('Ce nom d\'utilisateur est réservé');
  }
  
  return result;
};

/**
 * Valide un numéro de téléphone
 * @param {string} phone - Le numéro de téléphone à valider
 * @returns {ValidationResult} Résultat de la validation
 * @example
 * validatePhone("+33123456789") // {isValid: true, errors: []}
 * validatePhone("invalid") // {isValid: false, errors: [...]}
 */
export const validatePhone = (phone) => {
  const result = new ValidationResult();
  
  if (!phone || typeof phone !== 'string') {
    return result.addError(ERROR_MESSAGES.PHONE_INVALID);
  }
  
  const cleanPhone = phone.replace(/[\s()-]/g, '');
  
  if (!REGEX_PATTERNS.PHONE.test(cleanPhone)) {
    return result.addError(ERROR_MESSAGES.PHONE_INVALID);
  }
  
  return result;
};

/**
 * Valide une URL
 * @param {string} url - L'URL à valider
 * @returns {ValidationResult} Résultat de la validation
 * @example
 * validateUrl("https://example.com") // {isValid: true, errors: []}
 * validateUrl("invalid-url") // {isValid: false, errors: [...]}
 */
export const validateUrl = (url) => {
  const result = new ValidationResult();
  
  if (!url || typeof url !== 'string') {
    return result.addError(ERROR_MESSAGES.URL_INVALID);
  }
  
  if (!REGEX_PATTERNS.URL.test(url.trim())) {
    return result.addError(ERROR_MESSAGES.URL_INVALID);
  }
  
  return result;
};

/**
 * Valide la longueur d'une chaîne
 * @param {string} value - La valeur à valider
 * @param {number} min - Longueur minimale
 * @param {number} max - Longueur maximale
 * @param {string} fieldName - Nom du champ
 * @returns {ValidationResult} Résultat de la validation
 * @example
 * validateLength("Hello", 3, 10) // {isValid: true, errors: []}
 * validateLength("Hi", 3, 10) // {isValid: false, errors: [...]}
 */
export const validateLength = (value, min, max, fieldName = 'Ce champ') => {
  const result = new ValidationResult();
  
  if (!value || typeof value !== 'string') {
    return result.addError(`${fieldName} est requis`);
  }
  
  const length = value.trim().length;
  
  if (min && length < min) {
    return result.addError(`${fieldName} doit contenir au moins ${min} caractères`);
  }
  
  if (max && length > max) {
    return result.addError(`${fieldName} ne peut pas dépasser ${max} caractères`);
  }
  
  return result;
};

/**
 * Valide un fichier
 * @param {File} file - Le fichier à valider
 * @param {Object} options - Options de validation
 * @param {number} options.maxSize - Taille maximale en bytes
 * @param {string[]} options.allowedTypes - Types MIME autorisés
 * @param {string[]} options.allowedExtensions - Extensions autorisées
 * @returns {ValidationResult} Résultat de la validation
 * @example
 * validateFile(file, {maxSize: 5*1024*1024, allowedTypes: ['image/jpeg', 'image/png']})
 */
export const validateFile = (file, options = {}) => {
  const result = new ValidationResult();
  
  if (!file || !(file instanceof File)) {
    return result.addError('Fichier requis');
  }
  
  const { maxSize, allowedTypes, allowedExtensions } = options;
  
  // Validation de la taille
  if (maxSize && file.size > maxSize) {
    const sizeMB = Math.round(maxSize / (1024 * 1024));
    return result.addError(`${ERROR_MESSAGES.FILE_TOO_LARGE} (maximum ${sizeMB}MB)`);
  }
  
  // Validation du type MIME
  if (allowedTypes && !allowedTypes.includes(file.type)) {
    return result.addError(`${ERROR_MESSAGES.FILE_TYPE_INVALID}. Types autorisés: ${allowedTypes.join(', ')}`);
  }
  
  // Validation de l'extension
  if (allowedExtensions) {
    const fileExtension = file.name.split('.').pop().toLowerCase();
    if (!allowedExtensions.includes(fileExtension)) {
      return result.addError(`Extension de fichier non autorisée. Extensions autorisées: ${allowedExtensions.join(', ')}`);
    }
  }
  
  return result;
};

/**
 * Valide un nom de classe Java
 * @param {string} className - Le nom de classe à valider
 * @returns {ValidationResult} Résultat de la validation
 * @example
 * validateJavaClassName("MyClass") // {isValid: true, errors: []}
 * validateJavaClassName("myClass") // {isValid: false, errors: [...]}
 */
export const validateJavaClassName = (className) => {
  const result = new ValidationResult();
  
  if (!className || typeof className !== 'string') {
    return result.addError('Nom de classe requis');
  }
  
  const trimmedName = className.trim();
  
  if (!REGEX_PATTERNS.JAVA_CLASS_NAME.test(trimmedName)) {
    return result.addError(ERROR_MESSAGES.JAVA_CLASS_NAME_INVALID);
  }
  
  // Vérifier que ce n'est pas un mot-clé Java
  const javaKeywords = [
    'abstract', 'boolean', 'break', 'byte', 'case', 'catch', 'char', 'class',
    'const', 'continue', 'default', 'do', 'double', 'else', 'enum', 'extends',
    'final', 'finally', 'float', 'for', 'goto', 'if', 'implements', 'import',
    'instanceof', 'int', 'interface', 'long', 'native', 'new', 'package',
    'private', 'protected', 'public', 'return', 'short', 'static', 'strictfp',
    'super', 'switch', 'synchronized', 'this', 'throw', 'throws', 'transient',
    'try', 'void', 'volatile', 'while'
  ];
  
  if (javaKeywords.includes(trimmedName.toLowerCase())) {
    return result.addError('Le nom de classe ne peut pas être un mot-clé Java');
  }
  
  return result;
};

/**
 * Valide un nom de variable Java
 * @param {string} variableName - Le nom de variable à valider
 * @returns {ValidationResult} Résultat de la validation
 * @example
 * validateJavaVariableName("myVariable") // {isValid: true, errors: []}
 * validateJavaVariableName("MyVariable") // {isValid: false, errors: [...]}
 */
export const validateJavaVariableName = (variableName) => {
  const result = new ValidationResult();
  
  if (!variableName || typeof variableName !== 'string') {
    return result.addError('Nom de variable requis');
  }
  
  const trimmedName = variableName.trim();
  
  if (!REGEX_PATTERNS.JAVA_VARIABLE_NAME.test(trimmedName)) {
    return result.addError(ERROR_MESSAGES.JAVA_VARIABLE_NAME_INVALID);
  }
  
  // Vérifier que ce n'est pas un mot-clé Java
  const javaKeywords = [
    'abstract', 'boolean', 'break', 'byte', 'case', 'catch', 'char', 'class',
    'const', 'continue', 'default', 'do', 'double', 'else', 'enum', 'extends',
    'final', 'finally', 'float', 'for', 'goto', 'if', 'implements', 'import',
    'instanceof', 'int', 'interface', 'long', 'native', 'new', 'package',
    'private', 'protected', 'public', 'return', 'short', 'static', 'strictfp',
    'super', 'switch', 'synchronized', 'this', 'throw', 'throws', 'transient',
    'try', 'void', 'volatile', 'while'
  ];
  
  if (javaKeywords.includes(trimmedName.toLowerCase())) {
    return result.addError('Le nom de variable ne peut pas être un mot-clé Java');
  }
  
  return result;
};

/**
 * Valide un slug (URL-friendly)
 * @param {string} slug - Le slug à valider
 * @returns {ValidationResult} Résultat de la validation
 * @example
 * validateSlug("mon-article-blog") // {isValid: true, errors: []}
 * validateSlug("Mon Article!") // {isValid: false, errors: [...]}
 */
export const validateSlug = (slug) => {
  const result = new ValidationResult();
  
  if (!slug || typeof slug !== 'string') {
    return result.addError('Slug requis');
  }
  
  const trimmedSlug = slug.trim();
  
  if (!REGEX_PATTERNS.SLUG.test(trimmedSlug)) {
    return result.addError('Le slug ne peut contenir que des lettres minuscules, des chiffres et des tirets');
  }
  
  if (trimmedSlug.length < 3) {
    return result.addError('Le slug doit contenir au moins 3 caractères');
  }
  
  if (trimmedSlug.length > 100) {
    return result.addError('Le slug ne peut pas dépasser 100 caractères');
  }
  
  return result;
};

/**
 * Valide un âge
 * @param {number} age - L'âge à valider
 * @param {Object} options - Options de validation
 * @param {number} options.min - Âge minimum (défaut: 0)
 * @param {number} options.max - Âge maximum (défaut: 150)
 * @returns {ValidationResult} Résultat de la validation
 * @example
 * validateAge(25) // {isValid: true, errors: []}
 * validateAge(-5) // {isValid: false, errors: [...]}
 */
export const validateAge = (age, options = {}) => {
  const { min = 0, max = 150 } = options;
  const result = new ValidationResult();
  
  if (age === null || age === undefined) {
    return result.addError('Âge requis');
  }
  
  const numericAge = Number(age);
  
  if (isNaN(numericAge) || !Number.isInteger(numericAge)) {
    return result.addError('L\'âge doit être un nombre entier');
  }
  
  if (numericAge < min) {
    return result.addError(`L'âge doit être d'au moins ${min} ans`);
  }
  
  if (numericAge > max) {
    return result.addError(`L'âge ne peut pas dépasser ${max} ans`);
  }
  
  return result;
};

/**
 * Valide une date
 * @param {string|Date} date - La date à valider
 * @param {Object} options - Options de validation
 * @param {Date} options.minDate - Date minimale
 * @param {Date} options.maxDate - Date maximale
 * @param {boolean} options.allowFuture - Autoriser les dates futures (défaut: true)
 * @returns {ValidationResult} Résultat de la validation
 * @example
 * validateDate("2023-12-25") // {isValid: true, errors: []}
 * validateDate("invalid-date") // {isValid: false, errors: [...]}
 */
export const validateDate = (date, options = {}) => {
  const { minDate, maxDate, allowFuture = true } = options;
  const result = new ValidationResult();
  
  if (!date) {
    return result.addError('Date requise');
  }
  
  const dateObj = new Date(date);
  
  if (isNaN(dateObj.getTime())) {
    return result.addError('Date invalide');
  }
  
  const now = new Date();
  
  if (!allowFuture && dateObj > now) {
    return result.addError('La date ne peut pas être dans le futur');
  }
  
  if (minDate && dateObj < minDate) {
    return result.addError(`La date ne peut pas être antérieure au ${minDate.toLocaleDateString()}`);
  }
  
  if (maxDate && dateObj > maxDate) {
    return result.addError(`La date ne peut pas être postérieure au ${maxDate.toLocaleDateString()}`);
  }
  
  return result;
};

/**
 * Fonction utilitaire pour valider un objet complet
 * @param {Object} data - Les données à valider
 * @param {Object} rules - Les règles de validation
 * @returns {ValidationResult} Résultat de la validation
 * @example
 * const rules = {
 *   email: [(value) => validateEmail(value)],
 *   password: [(value) => validatePassword(value)]
 * };
 * validateObject({email: "test@example.com", password: "Pass123!"}, rules)
 */
export const validateObject = (data, rules) => {
  const result = new ValidationResult();
  
  if (!data || typeof data !== 'object') {
    return result.addError('Données invalides');
  }
  
  for (const [field, validators] of Object.entries(rules)) {
    if (!Array.isArray(validators)) {
      continue;
    }
    
    const fieldValue = data[field];
    
    for (const validator of validators) {
      if (typeof validator === 'function') {
        const fieldResult = validator(fieldValue);
        if (!fieldResult.isValid) {
          result.merge(fieldResult);
          break; // Arrêter à la première erreur pour ce champ
        }
      }
    }
  }
  
  return result;
};

/**
 * Fonction utilitaire pour créer un validateur personnalisé
 * @param {Function} validatorFn - Fonction de validation personnalisée
 * @param {string} errorMessage - Message d'erreur par défaut
 * @returns {Function} Fonction de validation
 * @example
 * const validateCustom = createValidator(
 *   (value) => value.includes('test'),
 *   'La valeur doit contenir "test"'
 * );
 */
export const createValidator = (validatorFn, errorMessage) => {
  return (value) => {
    const result = new ValidationResult();
    
    try {
      const isValid = validatorFn(value);
      if (!isValid) {
        result.addError(errorMessage);
      }
    } catch (error) {
      result.addError('Erreur de validation');
    }
    
    return result;
  };
};

/**
 * Fonction utilitaire pour combiner plusieurs validateurs
 * @param {...Function} validators - Les validateurs à combiner
 * @returns {Function} Fonction de validation combinée
 * @example
 * const combinedValidator = combineValidators(
 *   validateRequired,
 *   validateEmail
 * );
 */
export const combineValidators = (...validators) => {
  return (value) => {
    const result = new ValidationResult();
    
    for (const validator of validators) {
      if (typeof validator === 'function') {
        const validationResult = validator(value);
        result.merge(validationResult);
        
        if (!validationResult.isValid) {
          break; // Arrêter à la première erreur
        }
      }
    }
    
    return result;
  };
};

// Export de la classe ValidationResult pour utilisation externe
export { ValidationResult };

// Export par défaut d'un objet contenant toutes les fonctions
export default {
  validateRequired,
  validateEmail,
  validatePassword,
  validatePasswordMatch,
  validateUsername,
  validatePhone,
  validateUrl,
  validateLength,
  validateFile,
  validateJavaClassName,
  validateJavaVariableName,
  validateSlug,
  validateAge,
  validateDate,
  validateObject,
  createValidator,
  combineValidators,
  ValidationResult,
  REGEX_PATTERNS,
  ERROR_MESSAGES
};