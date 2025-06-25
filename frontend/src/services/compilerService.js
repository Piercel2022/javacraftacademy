// src/services/compilerService.js
/**
 * @fileoverview Service de compilation et d'exécution de code Java pour JavaCraft Academy
 * 
 * Ce service gère toute la logique de compilation, d'exécution et d'évaluation du code Java.
 * Il fait partie intégrante de l'écosystème d'apprentissage interactif de la plateforme.
 * 
 * Relations avec l'application :
 * - Utilisé par CodeEditor pour compiler le code en temps réel
 * - Intégré dans CodeRunner pour exécuter les programmes
 * - Connecté à LessonPlayer pour valider les exercices
 * - Interfacé avec ProgressService pour tracker les réussites
 * - Communique avec NotificationService pour les alertes
 * 
 * @author JavaCraft Academy Team
 * @version 1.0.0
 * @since 2024
 */

import { api } from './api';
import { notificationService } from './notificationService';
import { progressService } from './progressService';

/**
 * Configuration du service de compilation
 * @readonly
 * @enum {string}
 */
const COMPILER_CONFIG = {
  API_BASE_URL: process.env.REACT_APP_COMPILER_API_URL || '/api/compiler',
  TIMEOUT: parseInt(process.env.REACT_APP_COMPILER_TIMEOUT) || 30000,
  MAX_CODE_SIZE: parseInt(process.env.REACT_APP_MAX_CODE_SIZE) || 50000,
  SUPPORTED_VERSIONS: ['8', '11', '17', '21'],
  DEFAULT_VERSION: '17',
  MAX_EXECUTION_TIME: 15000,
  MAX_MEMORY_MB: 256
};

/**
 * Types de compilation supportés
 * @readonly
 * @enum {string}
 */
const COMPILATION_TYPES = {
  QUICK: 'quick',           // Compilation rapide pour validation syntaxique
  FULL: 'full',             // Compilation complète avec optimisations
  DEBUG: 'debug',           // Compilation avec informations de débogage
  EXERCISE: 'exercise'      // Compilation pour validation d'exercice
};

/**
 * États de compilation possibles
 * @readonly
 * @enum {string}
 */
const COMPILATION_STATUS = {
  PENDING: 'pending',
  COMPILING: 'compiling',
  RUNNING: 'running',
  SUCCESS: 'success',
  ERROR: 'error',
  TIMEOUT: 'timeout',
  MEMORY_EXCEEDED: 'memory_exceeded'
};

/**
 * Niveaux de sévérité des erreurs
 * @readonly
 * @enum {string}
 */
const ERROR_SEVERITY = {
  INFO: 'info',
  WARNING: 'warning',
  ERROR: 'error',
  FATAL: 'fatal'
};

/**
 * Service principal de compilation et d'exécution de code Java
 * 
 * Gère l'ensemble du processus de compilation, d'exécution et d'évaluation
 * du code Java soumis par les utilisateurs dans le cadre de leur apprentissage.
 * 
 * Fonctionnalités principales :
 * - Compilation de code Java en temps réel
 * - Exécution sécurisée dans un environnement sandbox
 * - Validation d'exercices avec tests automatisés
 * - Gestion des erreurs et diagnostics détaillés
 * - Support de multiples versions de Java
 * - Optimisations de performance et mise en cache
 * 
 * @class CompilerService
 */
class CompilerService {
  /**
   * Initialise le service de compilation
   * 
   * @constructor
   */
  constructor() {
    /**
     * Cache des résultats de compilation récents
     * @private
     * @type {Map<string, Object>}
     */
    this.compilationCache = new Map();
    
    /**
     * Tâches de compilation en cours
     * @private
     * @type {Map<string, Promise>}
     */
    this.pendingCompilations = new Map();
    
    /**
     * Configuration du service
     * @private
     * @type {Object}
     */
    this.config = { ...COMPILER_CONFIG };
    
    /**
     * Métriques de performance
     * @private
     * @type {Object}
     */
    this.metrics = {
      totalCompilations: 0,
      successfulCompilations: 0,
      averageCompileTime: 0,
      cacheHitRate: 0
    };

    this.initializeService();
  }

  /**
   * Initialise le service et ses dépendances
   * 
   * @private
   * @returns {Promise<void>}
   */
  async initializeService() {
    try {
      // Vérification de la disponibilité du service de compilation
      await this.healthCheck();
      
      // Initialisation du cache avec TTL
      this.setupCacheCleanup();
      
      console.log('CompilerService initialisé avec succès');
    } catch (error) {
      console.error('Erreur lors de l\'initialisation du CompilerService:', error);
      notificationService.showError('Service de compilation indisponible');
    }
  }

  /**
   * Compile le code Java fourni
   * 
   * Cette méthode principale gère la compilation du code Java avec différents
   * modes de compilation selon le contexte d'utilisation.
   * 
   * @param {string} code - Code Java à compiler
   * @param {Object} options - Options de compilation
   * @param {string} [options.version='17'] - Version de Java à utiliser
   * @param {string} [options.type='quick'] - Type de compilation
   * @param {string} [options.className='Main'] - Nom de la classe principale
   * @param {boolean} [options.useCache=true] - Utiliser le cache si disponible
   * @param {Object} [options.exerciseContext] - Contexte d'exercice si applicable
   * @returns {Promise<Object>} Résultat de la compilation
   * 
   * @throws {Error} Si le code est invalide ou trop volumineux
   * 
   * @example
   * ```javascript
   * const result = await compilerService.compileCode(`
   *   public class HelloWorld {
   *     public static void main(String[] args) {
   *       System.out.println("Hello, World!");
   *     }
   *   }
   * `, {
   *   version: '17',
   *   type: 'full',
   *   className: 'HelloWorld'
   * });
   * 
   * if (result.success) {
   *   console.log('Compilation réussie:', result.bytecode);
   * } else {
   *   console.error('Erreurs de compilation:', result.errors);
   * }
   * ```
   */
  async compileCode(code, options = {}) {
    const startTime = Date.now();
    
    try {
      // Validation des paramètres d'entrée
      this.validateCompilationRequest(code, options);
      
      const compilationOptions = this.prepareCompilationOptions(code, options);
      const cacheKey = this.generateCacheKey(code, compilationOptions);
      
      // Vérification du cache si activé
      if (options.useCache !== false && this.compilationCache.has(cacheKey)) {
        this.updateMetrics({ cacheHit: true });
        return this.compilationCache.get(cacheKey);
      }
      
      // Éviter les compilations redondantes
      if (this.pendingCompilations.has(cacheKey)) {
        return await this.pendingCompilations.get(cacheKey);
      }
      
      // Lancement de la compilation
      const compilationPromise = this.performCompilation(code, compilationOptions);
      this.pendingCompilations.set(cacheKey, compilationPromise);
      
      const result = await compilationPromise;
      
      // Nettoyage et mise en cache
      this.pendingCompilations.delete(cacheKey);
      if (result.success && options.useCache !== false) {
        this.cacheResult(cacheKey, result);
      }
      
      // Mise à jour des métriques
      this.updateMetrics({
        compilationTime: Date.now() - startTime,
        success: result.success,
        cacheHit: false
      });
      
      return result;
      
    } catch (error) {
      this.pendingCompilations.delete(this.generateCacheKey(code, options));
      throw this.handleCompilationError(error, code, options);
    }
  }

  /**
   * Exécute le code Java compilé avec des paramètres d'entrée
   * 
   * @param {string} bytecode - Bytecode Java compilé
   * @param {Object} executionOptions - Options d'exécution
   * @param {string[]} [executionOptions.args=[]] - Arguments de ligne de commande
   * @param {string} [executionOptions.input=''] - Entrée standard
   * @param {number} [executionOptions.timeout] - Timeout personnalisé en ms
   * @param {number} [executionOptions.memoryLimit] - Limite mémoire en MB
   * @returns {Promise<Object>} Résultat de l'exécution
   * 
   * @example
   * ```javascript
   * const executionResult = await compilerService.executeCode(bytecode, {
   *   args: ['arg1', 'arg2'],
   *   input: 'Données d\'entrée\n',
   *   timeout: 10000
   * });
   * 
   * console.log('Sortie:', executionResult.output);
   * console.log('Erreurs:', executionResult.stderr);
   * ```
   */
  async executeCode(bytecode, executionOptions = {}) {
    try {
      const options = {
        args: executionOptions.args || [],
        input: executionOptions.input || '',
        timeout: executionOptions.timeout || this.config.MAX_EXECUTION_TIME,
        memoryLimit: executionOptions.memoryLimit || this.config.MAX_MEMORY_MB,
        ...executionOptions
      };

      const response = await api.post(`${this.config.API_BASE_URL}/execute`, {
        bytecode,
        options
      }, {
        timeout: options.timeout + 5000 // Marge pour la communication réseau
      });

      return this.processExecutionResult(response.data);
      
    } catch (error) {
      return this.handleExecutionError(error);
    }
  }

  /**
   * Compile et exécute le code en une seule opération
   * 
   * Méthode de commodité qui combine compilation et exécution.
   * Particulièrement utile pour les tests rapides et la validation d'exercices.
   * 
   * @param {string} code - Code Java à compiler et exécuter
   * @param {Object} options - Options combinées de compilation et d'exécution
   * @returns {Promise<Object>} Résultat combiné compilation + exécution
   * 
   * @example
   * ```javascript
   * const result = await compilerService.compileAndRun(`
   *   public class Calculator {
   *     public static void main(String[] args) {
   *       System.out.println(2 + 2);
   *     }
   *   }
   * `);
   * 
   * if (result.compilationSuccess && result.executionSuccess) {
   *   console.log('Résultat:', result.output);
   * }
   * ```
   */
  async compileAndRun(code, options = {}) {
    const compilationResult = await this.compileCode(code, {
      ...options,
      type: options.type || COMPILATION_TYPES.FULL
    });

    if (!compilationResult.success) {
      return {
        compilationSuccess: false,
        executionSuccess: false,
        compilationErrors: compilationResult.errors,
        warnings: compilationResult.warnings,
        executionResult: null
      };
    }

    const executionResult = await this.executeCode(
      compilationResult.bytecode,
      options.execution || {}
    );

    return {
      compilationSuccess: true,
      executionSuccess: executionResult.success,
      compilationTime: compilationResult.compilationTime,
      executionTime: executionResult.executionTime,
      output: executionResult.output,
      stderr: executionResult.stderr,
      exitCode: executionResult.exitCode,
      warnings: compilationResult.warnings,
      bytecode: compilationResult.bytecode,
      executionResult
    };
  }

  /**
   * Valide une solution d'exercice contre des tests automatisés
   * 
   * Cette méthode est cruciale pour l'évaluation automatique des exercices.
   * Elle compile le code de l'étudiant et l'exécute contre une suite de tests
   * prédéfinis pour déterminer si la solution est correcte.
   * 
   * @param {string} studentCode - Code soumis par l'étudiant
   * @param {Object} exerciseConfig - Configuration de l'exercice
   * @param {Object[]} exerciseConfig.testCases - Cas de test à exécuter
   * @param {string} exerciseConfig.expectedOutput - Sortie attendue
   * @param {Object} exerciseConfig.constraints - Contraintes d'exécution
   * @returns {Promise<Object>} Résultat détaillé de la validation
   * 
   * @example
   * ```javascript
   * const validationResult = await compilerService.validateExercise(studentCode, {
   *   testCases: [
   *     { input: "5", expectedOutput: "120" },
   *     { input: "0", expectedOutput: "1" }
   *   ],
   *   constraints: { maxExecutionTime: 5000 }
   * });
   * 
   * if (validationResult.allTestsPassed) {
   *   // Marquer l'exercice comme réussi
   *   await progressService.markExerciseCompleted(exerciseId);
   * }
   * ```
   */
  async validateExercise(studentCode, exerciseConfig) {
    try {
      const validationId = this.generateValidationId();
      
      // Compilation avec mode spécial exercice
      const compilationResult = await this.compileCode(studentCode, {
        type: COMPILATION_TYPES.EXERCISE,
        exerciseContext: exerciseConfig,
        useCache: false // Pas de cache pour les validations d'exercices
      });

      if (!compilationResult.success) {
        return {
          validationId,
          success: false,
          compilationErrors: compilationResult.errors,
          allTestsPassed: false,
          testResults: [],
          score: 0,
          feedback: 'Code non compilable. Vérifiez les erreurs de syntaxe.'
        };
      }

      // Exécution des tests
      const testResults = await this.runTestSuite(
        compilationResult.bytecode,
        exerciseConfig.testCases,
        exerciseConfig.constraints
      );

      const validationResult = this.analyzeTestResults(testResults, exerciseConfig);
      
      // Notification du résultat à l'utilisateur
      if (validationResult.allTestsPassed) {
        notificationService.showSuccess('Exercice réussi ! Félicitations 🎉');
        
        // Mise à jour automatique du progrès
        if (exerciseConfig.exerciseId) {
          await progressService.updateExerciseProgress(
            exerciseConfig.exerciseId,
            validationResult.score
          );
        }
      } else {
        notificationService.showWarning('Certains tests ont échoué. Continuez vos efforts !');
      }

      return {
        validationId,
        success: true,
        ...validationResult,
        compilationTime: compilationResult.compilationTime
      };

    } catch (error) {
      console.error('Erreur lors de la validation d\'exercice:', error);
      return {
        success: false,
        error: 'Erreur technique lors de la validation',
        allTestsPassed: false,
        score: 0
      };
    }
  }

  /**
   * Analyse statique du code pour détecter les problèmes potentiels
   * 
   * Effectue une analyse statique approfondie du code Java pour identifier
   * les problèmes de style, de performance et de bonnes pratiques.
   * 
   * @param {string} code - Code Java à analyser
   * @param {Object} analysisOptions - Options d'analyse
   * @param {string[]} [analysisOptions.rules] - Règles d'analyse à appliquer
   * @param {string} [analysisOptions.level='medium'] - Niveau d'analyse
   * @returns {Promise<Object>} Rapport d'analyse détaillé
   * 
   * @example
   * ```javascript
   * const analysis = await compilerService.analyzeCode(code, {
   *   rules: ['naming-conventions', 'performance', 'security'],
   *   level: 'strict'
   * });
   * 
   * analysis.suggestions.forEach(suggestion => {
   *   console.log(`${suggestion.type}: ${suggestion.message}`);
   * });
   * ```
   */
  async analyzeCode(code, analysisOptions = {}) {
    try {
      const response = await api.post(`${this.config.API_BASE_URL}/analyze`, {
        code,
        options: {
          rules: analysisOptions.rules || ['all'],
          level: analysisOptions.level || 'medium',
          includeMetrics: true,
          ...analysisOptions
        }
      });

      return this.processAnalysisResult(response.data);
      
    } catch (error) {
      console.error('Erreur lors de l\'analyse de code:', error);
      return {
        success: false,
        error: 'Analyse indisponible',
        suggestions: [],
        metrics: {}
      };
    }
  }

  /**
   * Formate le code Java selon les conventions standard
   * 
   * @param {string} code - Code Java à formater
   * @param {Object} formatOptions - Options de formatage
   * @returns {Promise<string>} Code formaté
   */
  async formatCode(code, formatOptions = {}) {
    try {
      const response = await api.post(`${this.config.API_BASE_URL}/format`, {
        code,
        options: {
          indentSize: formatOptions.indentSize || 4,
          maxLineLength: formatOptions.maxLineLength || 120,
          insertFinalNewline: formatOptions.insertFinalNewline !== false,
          ...formatOptions
        }
      });

      return response.data.formattedCode;
      
    } catch (error) {
      console.error('Erreur lors du formatage:', error);
      return code; // Retourner le code original en cas d'erreur
    }
  }

  /**
   * Obtient des suggestions d'auto-complétion pour le code
   * 
   * @param {string} code - Code Java actuel
   * @param {number} cursorPosition - Position du curseur
   * @param {Object} context - Contexte d'auto-complétion
   * @returns {Promise<Array>} Liste des suggestions
   */
  async getAutocompleteSuggestions(code, cursorPosition, context = {}) {
    try {
      const response = await api.post(`${this.config.API_BASE_URL}/autocomplete`, {
        code,
        cursorPosition,
        context
      });

      return response.data.suggestions || [];
      
    } catch (error) {
      console.error('Erreur lors de l\'auto-complétion:', error);
      return [];
    }
  }

  /**
   * Valide les paramètres de compilation
   * 
   * @private
   * @param {string} code - Code à valider
   * @param {Object} options - Options à valider
   * @throws {Error} Si les paramètres sont invalides
   */
  validateCompilationRequest(code, options) {
    if (!code || typeof code !== 'string') {
      throw new Error('Le code Java est requis et doit être une chaîne de caractères');
    }

    if (code.length > this.config.MAX_CODE_SIZE) {
      throw new Error(`Le code est trop volumineux (max: ${this.config.MAX_CODE_SIZE} caractères)`);
    }

    if (options.version && !this.config.SUPPORTED_VERSIONS.includes(options.version)) {
      throw new Error(`Version Java non supportée: ${options.version}`);
    }

    if (options.type && !Object.values(COMPILATION_TYPES).includes(options.type)) {
      throw new Error(`Type de compilation invalide: ${options.type}`);
    }
  }

  /**
   * Prépare les options de compilation avec les valeurs par défaut
   * 
   * @private
   * @param {string} code - Code à compiler
   * @param {Object} options - Options utilisateur
   * @returns {Object} Options de compilation préparées
   */
  prepareCompilationOptions(code, options) {
    return {
      version: options.version || this.config.DEFAULT_VERSION,
      type: options.type || COMPILATION_TYPES.QUICK,
      className: options.className || this.extractClassName(code),
      optimizationLevel: options.optimizationLevel || 'O1',
      debugInfo: options.type === COMPILATION_TYPES.DEBUG,
      warningsAsErrors: options.warningsAsErrors || false,
      ...options
    };
  }

  /**
   * Extrait le nom de la classe principale du code Java
   * 
   * @private
   * @param {string} code - Code Java
   * @returns {string} Nom de la classe principale
   */
  extractClassName(code) {
    const classMatch = code.match(/public\s+class\s+(\w+)/);
    return classMatch ? classMatch[1] : 'Main';
  }

  /**
   * Génère une clé de cache unique pour la compilation
   * 
   * @private
   * @param {string} code - Code Java
   * @param {Object} options - Options de compilation
   * @returns {string} Clé de cache
   */
  generateCacheKey(code, options) {
    const hash = this.generateHash(code + JSON.stringify(options));
    return `compile_${hash}`;
  }

  /**
   * Génère un hash simple pour les clés de cache
   * 
   * @private
   * @param {string} str - Chaîne à hasher
   * @returns {string} Hash généré
   */
  generateHash(str) {
    let hash = 0;
    for (let i = 0; i < str.length; i++) {
      const char = str.charCodeAt(i);
      hash = ((hash << 5) - hash) + char;
      hash = hash & hash; // Conversion en 32bit
    }
    return Math.abs(hash).toString(36);
  }

  /**
   * Effectue la compilation proprement dite
   * 
   * @private
   * @param {string} code - Code à compiler
   * @param {Object} options - Options de compilation
   * @returns {Promise<Object>} Résultat de compilation
   */
  async performCompilation(code, options) {
    const response = await api.post(`${this.config.API_BASE_URL}/compile`, {
      code,
      options
    }, {
      timeout: this.config.TIMEOUT
    });

    return this.processCompilationResult(response.data);
  }

  /**
   * Traite le résultat brut de compilation
   * 
   * @private
   * @param {Object} rawResult - Résultat brut du serveur
   * @returns {Object} Résultat traité et normalisé
   */
  processCompilationResult(rawResult) {
    return {
      success: rawResult.success,
      bytecode: rawResult.bytecode,
      errors: this.normalizeErrors(rawResult.errors || []),
      warnings: this.normalizeWarnings(rawResult.warnings || []),
      compilationTime: rawResult.compilationTime,
      memoryUsed: rawResult.memoryUsed,
      status: rawResult.status || COMPILATION_STATUS.SUCCESS,
      metadata: rawResult.metadata || {}
    };
  }

  /**
   * Normalise les erreurs de compilation
   * 
   * @private
   * @param {Array} errors - Erreurs brutes
   * @returns {Array} Erreurs normalisées
   */
  normalizeErrors(errors) {
    return errors.map(error => ({
      line: error.line,
      column: error.column,
      message: error.message,
      severity: error.severity || ERROR_SEVERITY.ERROR,
      code: error.code,
      suggestion: error.suggestion
    }));
  }

  /**
   * Normalise les avertissements de compilation
   * 
   * @private
   * @param {Array} warnings - Avertissements bruts
   * @returns {Array} Avertissements normalisés
   */
  normalizeWarnings(warnings) {
    return warnings.map(warning => ({
      line: warning.line,
      column: warning.column,
      message: warning.message,
      severity: ERROR_SEVERITY.WARNING,
      code: warning.code,
      suggestion: warning.suggestion
    }));
  }

  /**
   * Traite le résultat d'exécution
   * 
   * @private
   * @param {Object} rawResult - Résultat brut d'exécution
   * @returns {Object} Résultat traité
   */
  processExecutionResult(rawResult) {
    return {
      success: rawResult.exitCode === 0,
      output: rawResult.stdout || '',
      stderr: rawResult.stderr || '',
      exitCode: rawResult.exitCode,
      executionTime: rawResult.executionTime,
      memoryUsed: rawResult.memoryUsed,
      status: rawResult.status || COMPILATION_STATUS.SUCCESS
    };
  }

  /**
   * Exécute une suite de tests pour la validation d'exercice
   * 
   * @private
   * @param {string} bytecode - Bytecode à tester
   * @param {Array} testCases - Cas de test
   * @param {Object} constraints - Contraintes d'exécution
   * @returns {Promise<Array>} Résultats des tests
   */
  async runTestSuite(bytecode, testCases, constraints = {}) {
    const testResults = [];

    for (let i = 0; i < testCases.length; i++) {
      const testCase = testCases[i];
      
      try {
        const executionResult = await this.executeCode(bytecode, {
          input: testCase.input || '',
          args: testCase.args || [],
          timeout: constraints.maxExecutionTime || this.config.MAX_EXECUTION_TIME,
          memoryLimit: constraints.maxMemory || this.config.MAX_MEMORY_MB
        });

        const testResult = {
          testIndex: i,
          passed: this.compareOutput(executionResult.output, testCase.expectedOutput),
          actualOutput: executionResult.output,
          expectedOutput: testCase.expectedOutput,
          executionTime: executionResult.executionTime,
          memoryUsed: executionResult.memoryUsed,
          error: executionResult.success ? null : executionResult.stderr
        };

        testResults.push(testResult);

      } catch (error) {
        testResults.push({
          testIndex: i,
          passed: false,
          error: error.message,
          actualOutput: '',
          expectedOutput: testCase.expectedOutput,
          executionTime: 0,
          memoryUsed: 0
        });
      }
    }

    return testResults;
  }

  /**
   * Compare la sortie actuelle avec la sortie attendue
   * 
   * @private
   * @param {string} actual - Sortie actuelle
   * @param {string} expected - Sortie attendue
   * @returns {boolean} True si les sorties correspondent
   */
  compareOutput(actual, expected) {
    // Normalisation des fins de ligne et espaces
    const normalizeOutput = (str) => 
      str.toString().trim().replace(/\r\n/g, '\n').replace(/\s+$/gm, '');
    
    return normalizeOutput(actual) === normalizeOutput(expected);
  }

  /**
   * Analyse les résultats de tests et génère un rapport
   * 
   * @private
   * @param {Array} testResults - Résultats des tests
   * @param {Object} exerciseConfig - Configuration de l'exercice
   * @returns {Object} Analyse des résultats
   */
  analyzeTestResults(testResults, exerciseConfig) {
    const passedTests = testResults.filter(result => result.passed);
    const failedTests = testResults.filter(result => !result.passed);
    
    const totalTests = testResults.length;
    const passedCount = passedTests.length;
    const score = totalTests > 0 ? Math.round((passedCount / totalTests) * 100) : 0;

    const feedback = this.generateFeedback(testResults, exerciseConfig);

    return {
      allTestsPassed: passedCount === totalTests,
      testResults,
      totalTests,
      passedTests: passedCount,
      failedTests: failedTests.length,
      score,
      feedback,
      suggestions: this.generateSuggestions(failedTests)
    };
  }

  /**
   * Génère un feedback personnalisé basé sur les résultats
   * 
   * @private
   * @param {Array} testResults - Résultats des tests
   * @param {Object} exerciseConfig - Configuration de l'exercice
   * @returns {string} Feedback généré
   */
  generateFeedback(testResults, exerciseConfig) {
    const passedCount = testResults.filter(r => r.passed).length;
    const totalCount = testResults.length;

    if (passedCount === totalCount) {
      return '🎉 Excellent ! Tous les tests sont passés. Votre solution est correcte.';
    } else if (passedCount > totalCount / 2) {
      return `👍 Bon travail ! ${passedCount}/${totalCount} tests passés. Quelques ajustements sont nécessaires.`;
    } else if (passedCount > 0) {
      return `💪 Vous êtes sur la bonne voie ! ${passedCount}/${totalCount} tests passés. Continuez vos efforts.`;
    } else {
      return '🤔 Aucun test n\'est passé. Vérifiez votre logique et réessayez.';
    }
  }

  /**
   * Génère des suggestions basées sur les tests échoués
   * 
   * @private
   * @param {Array} failedTests - Tests qui ont échoué
   * @returns {Array} Liste de suggestions
   */
  generateSuggestions(failedTests) {
    const suggestions = [];

    failedTests.forEach(test => {
      if (test.error) {
        if (test.error.includes('timeout')) {
          suggestions.push({
            type: 'performance',
            message: 'Votre code semble prendre trop de temps à s\'exécuter. Vérifiez s\'il n\'y a pas de boucles infinies.',
            priority: 'high'
          });
        } else if (test.error.includes('memory')) {
          suggestions.push({
            type: 'memory',
            message: 'Votre code utilise trop de mémoire. Optimisez l\'utilisation des structures de données.',
            priority: 'high'
          });
        } else if (test.error.includes('Exception')) {
          suggestions.push({
            type: 'runtime',
            message: 'Une exception s\'est produite. Vérifiez la gestion des cas limites et des erreurs.',
            priority: 'medium'
          });
        }
      } else if (!test.passed) {
        suggestions.push({
          type: 'logic',
          message: `Test ${test.testIndex + 1}: Sortie attendue "${test.expectedOutput}" mais obtenu "${test.actualOutput}". Vérifiez votre logique.`,
          priority: 'medium'
        });
      }
    });

    return suggestions;
  }

  /**
   * Traite le résultat d'analyse de code
   * 
   * @private
   * @param {Object} rawResult - Résultat brut d'analyse
   * @returns {Object} Résultat traité
   */
  processAnalysisResult(rawResult) {
    return {
      success: rawResult.success,
      suggestions: rawResult.suggestions || [],
      metrics: {
        complexity: rawResult.complexity || 0,
        maintainability: rawResult.maintainability || 0,
        codeSmells: rawResult.codeSmells || 0,
        duplications: rawResult.duplications || 0,
        ...rawResult.metrics
      },
      qualityGate: rawResult.qualityGate || 'unknown',
      technicalDebt: rawResult.technicalDebt || '0min'
    };
  }

  /**
   * Met en cache un résultat de compilation
   * 
   * @private
   * @param {string} cacheKey - Clé de cache
   * @param {Object} result - Résultat à mettre en cache
   */
  cacheResult(cacheKey, result) {
    // Ajouter un timestamp pour la gestion TTL
    const cacheEntry = {
      ...result,
      timestamp: Date.now()
    };
    
    this.compilationCache.set(cacheKey, cacheEntry);
    
    // Limiter la taille du cache
    if (this.compilationCache.size > 100) {
      const oldestKey = this.compilationCache.keys().next().value;
      this.compilationCache.delete(oldestKey);
    }
  }

  /**
   * Configure le nettoyage automatique du cache
   * 
   * @private
   */
  setupCacheCleanup() {
    // Nettoyage du cache toutes les 10 minutes
    setInterval(() => {
      const now = Date.now();
      const maxAge = 30 * 60 * 1000; // 30 minutes

      for (const [key, entry] of this.compilationCache.entries()) {
        if (now - entry.timestamp > maxAge) {
          this.compilationCache.delete(key);
        }
      }
    }, 10 * 60 * 1000);
  }

  /**
   * Met à jour les métriques de performance
   * 
   * @private
   * @param {Object} data - Données de métrique
   */
  updateMetrics(data) {
    this.metrics.totalCompilations++;
    
    if (data.success) {
      this.metrics.successfulCompilations++;
    }
    
    if (data.compilationTime) {
      // Calcul de la moyenne mobile
      const currentAverage = this.metrics.averageCompileTime;
      const totalCompilations = this.metrics.totalCompilations;
      this.metrics.averageCompileTime = 
        (currentAverage * (totalCompilations - 1) + data.compilationTime) / totalCompilations;
    }
    
    if (data.cacheHit !== undefined) {
      const cacheHits = data.cacheHit ? 1 : 0;
      this.metrics.cacheHitRate = 
        (this.metrics.cacheHitRate * (this.metrics.totalCompilations - 1) + cacheHits) / 
        this.metrics.totalCompilations;
    }
  }

  /**
   * Génère un ID unique pour les validations
   * 
   * @private
   * @returns {string} ID de validation unique
   */
  generateValidationId() {
    return `validation_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
  }

  /**
   * Gère les erreurs de compilation
   * 
   * @private
   * @param {Error} error - Erreur à traiter
   * @param {string} code - Code qui a causé l'erreur
   * @param {Object} options - Options de compilation
   * @returns {Error} Erreur enrichie
   */
  handleCompilationError(error, code, options) {
    console.error('Erreur de compilation:', error);
    
    let userFriendlyMessage = 'Erreur lors de la compilation';
    
    if (error.code === 'TIMEOUT') {
      userFriendlyMessage = 'La compilation a pris trop de temps';
    } else if (error.code === 'NETWORK_ERROR') {
      userFriendlyMessage = 'Erreur de connexion au service de compilation';
    } else if (error.message.includes('syntax')) {
      userFriendlyMessage = 'Erreur de syntaxe dans votre code Java';
    }
    
    const enrichedError = new Error(userFriendlyMessage);
    enrichedError.originalError = error;
    enrichedError.code = code;
    enrichedError.options = options;
    
    return enrichedError;
  }

  /**
   * Gère les erreurs d'exécution
   * 
   * @private
   * @param {Error} error - Erreur d'exécution
   * @returns {Object} Résultat d'erreur formaté
   */
  handleExecutionError(error) {
    console.error('Erreur d\'exécution:', error);
    
    let status = COMPILATION_STATUS.ERROR;
    let message = 'Erreur lors de l\'exécution';
    
    if (error.code === 'TIMEOUT') {
      status = COMPILATION_STATUS.TIMEOUT;
      message = 'L\'exécution a dépassé le temps limite';
    } else if (error.message.includes('memory')) {
      status = COMPILATION_STATUS.MEMORY_EXCEEDED;
      message = 'Limite de mémoire dépassée';
    }
    
    return {
      success: false,
      output: '',
      stderr: message,
      exitCode: -1,
      executionTime: 0,
      memoryUsed: 0,
      status,
      error: error.message
    };
  }

  /**
   * Vérifie la santé du service de compilation
   * 
   * @private
   * @returns {Promise<boolean>} True si le service fonctionne
   */
  async healthCheck() {
    try {
      const response = await api.get(`${this.config.API_BASE_URL}/health`, {
        timeout: 5000
      });
      
      return response.data.status === 'healthy';
    } catch (error) {
      console.warn('Service de compilation non disponible:', error.message);
      return false;
    }
  }

  /**
   * Obtient les métriques actuelles du service
   * 
   * @returns {Object} Métriques de performance
   */
  getMetrics() {
    return { ...this.metrics };
  }

  /**
   * Réinitialise les métriques
   * 
   * @returns {void}
   */
  resetMetrics() {
    this.metrics = {
      totalCompilations: 0,
      successfulCompilations: 0,
      averageCompileTime: 0,
      cacheHitRate: 0
    };
  }

  /**
   * Vide le cache de compilation
   * 
   * @returns {void}
   */
  clearCache() {
    this.compilationCache.clear();
    this.pendingCompilations.clear();
  }

  /**
   * Obtient des informations sur l'état du cache
   * 
   * @returns {Object} Informations sur le cache
   */
  getCacheInfo() {
    return {
      size: this.compilationCache.size,
      pendingCompilations: this.pendingCompilations.size,
      maxSize: 100
    };
  }

  /**
   * Teste la connectivité avec le serveur de compilation
   * 
   * @returns {Promise<Object>} Résultat du test de connectivité
   */
  async testConnectivity() {
    const startTime = Date.now();
    
    try {
      const isHealthy = await this.healthCheck();
      const responseTime = Date.now() - startTime;
      
      return {
        success: isHealthy,
        responseTime,
        status: isHealthy ? 'connected' : 'disconnected',
        serverInfo: isHealthy ? await this.getServerInfo() : null
      };
    } catch (error) {
      return {
        success: false,
        responseTime: Date.now() - startTime,
        status: 'error',
        error: error.message
      };
    }
  }

  /**
   * Récupère les informations du serveur de compilation
   * 
   * @private
   * @returns {Promise<Object>} Informations du serveur
   */
  async getServerInfo() {
    try {
      const response = await api.get(`${this.config.API_BASE_URL}/info`);
      return response.data;
    } catch (error) {
      return null;
    }
  }

  /**
   * Configure les options du service
   * 
   * @param {Object} newConfig - Nouvelle configuration
   * @returns {void}
   */
  updateConfig(newConfig) {
    this.config = { ...this.config, ...newConfig };
  }

  /**
   * Obtient la configuration actuelle
   * 
   * @returns {Object} Configuration actuelle
   */
  getConfig() {
    return { ...this.config };
  }
}

// Instance singleton du service
const compilerService = new CompilerService();

// Exportation des constantes utiles
export {
  compilerService as default,
  COMPILATION_TYPES,
  COMPILATION_STATUS,
  ERROR_SEVERITY,
  COMPILER_CONFIG
};