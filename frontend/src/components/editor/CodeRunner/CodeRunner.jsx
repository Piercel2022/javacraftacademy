
import React, { useState, useEffect, useCallback, useRef } from 'react';
import { Play, Square, RotateCcw, Settings, AlertCircle, CheckCircle } from 'lucide-react';
import styles from './CodeRunner.module.css';
import { useNotification } from '../../hooks/useNotification';
import { useAuth } from '../../hooks/useAuth';
import { compilerService } from '../../services/compilerService';
import { progressService } from '../../services/progressService';
import Button from '../common/Button';
import Loading from '../common/Loading';

/**
 * @fileoverview Composant CodeRunner - Exécuteur de code Java intégré
 * @author JavaCraft Academy
 * @version 1.0.0
 * 
 * @description
 * Le composant CodeRunner est responsable de l'exécution du code Java écrit par l'utilisateur.
 * Il gère la compilation, l'exécution, et l'affichage des résultats ou erreurs.
 * 
 * @features
 * - Compilation et exécution de code Java en temps réel
 * - Affichage des résultats et erreurs de compilation
 * - Gestion des timeouts d'exécution
 * - Historique des exécutions
 * - Intégration avec le système de progression
 * - Support des entrées utilisateur (System.in)
 * - Limitation des ressources système
 * 
 * @relations
 * - Utilisé par CodePlayground pour l'exécution du code
 * - Intégré dans LessonPlayer pour les exercices pratiques
 * - Communique avec compilerService pour l'exécution backend
 * - Met à jour la progression via progressService
 * - Utilise NotificationContext pour les alertes
 * - Dépend d'AuthContext pour l'authentification
 * 
 * @dependencies
 * - compilerService: Service de compilation backend
 * - progressService: Service de suivi de progression
 * - useNotification: Hook pour les notifications
 * - useAuth: Hook d'authentification
 */

/**
 * @typedef {Object} CodeRunnerProps
 * @property {string} code - Code Java à exécuter
 * @property {string} [language='java'] - Langage de programmation
 * @property {Function} onExecutionStart - Callback appelé au début de l'exécution
 * @property {Function} onExecutionComplete - Callback appelé à la fin de l'exécution
 * @property {Function} onError - Callback appelé en cas d'erreur
 * @property {Object} [exerciseContext] - Contexte de l'exercice si applicable
 * @property {boolean} [allowInput=false] - Autoriser les entrées utilisateur
 * @property {number} [timeout=30000] - Timeout d'exécution en ms
 * @property {boolean} [saveHistory=true] - Sauvegarder l'historique des exécutions
 * @property {string} [className] - Classes CSS additionnelles
 */

/**
 * @typedef {Object} ExecutionResult
 * @property {string} output - Sortie du programme
 * @property {string} error - Messages d'erreur
 * @property {number} executionTime - Temps d'exécution en ms
 * @property {number} memoryUsage - Utilisation mémoire en MB
 * @property {string} status - Statut d'exécution (success, error, timeout)
 * @property {string} timestamp - Horodatage de l'exécution
 */

/**
 * Composant CodeRunner - Exécuteur de code Java
 * 
 * @param {CodeRunnerProps} props - Propriétés du composant
 * @returns {JSX.Element} Composant CodeRunner rendu
 */
const CodeRunner = ({
  code = '',
  language = 'java',
  onExecutionStart,
  onExecutionComplete,
  onError,
  exerciseContext,
  allowInput = false,
  timeout = 30000,
  saveHistory = true,
  className = ''
}) => {
  // États du composant
  const [isRunning, setIsRunning] = useState(false);
  const [output, setOutput] = useState('');
  const [error, setError] = useState('');
  const [executionTime, setExecutionTime] = useState(0);
  const [memoryUsage, setMemoryUsage] = useState(0);
  const [userInput, setUserInput] = useState('');
  const [waitingForInput, setWaitingForInput] = useState(false);
  const [executionHistory, setExecutionHistory] = useState([]);
  const [settings, setSettings] = useState({
    autoSave: true,
    showStats: true,
    enableOptimizations: false
  });

  // Hooks
  const { showNotification } = useNotification();
  const { user } = useAuth();
  const executionRef = useRef(null);
  const inputRef = useRef(null);

  /**
   * Valide le code avant l'exécution
   * @param {string} codeToValidate - Code à valider
   * @returns {Object} Résultat de la validation
   */
  const validateCode = useCallback((codeToValidate) => {
    const errors = [];
    const warnings = [];

    // Vérifications de base
    if (!codeToValidate.trim()) {
      errors.push('Le code ne peut pas être vide');
    }

    // Vérification de la classe principale
    if (language === 'java' && !codeToValidate.includes('public static void main')) {
      warnings.push('Aucune méthode main trouvée');
    }

    // Vérification des imports dangereux
    const dangerousImports = ['java.io.File', 'java.lang.Runtime', 'java.lang.System.exit'];
    dangerousImports.forEach(imp => {
      if (codeToValidate.includes(imp)) {
        errors.push(`Import non autorisé: ${imp}`);
      }
    });

    return { isValid: errors.length === 0, errors, warnings };
  }, [language]);

  /**
   * Exécute le code Java
   * @async
   * @param {string} [inputData=''] - Données d'entrée pour le programme
   */
  const executeCode = useCallback(async (inputData = '') => {
    if (!code.trim()) {
      showNotification('Le code ne peut pas être vide', 'warning');
      return;
    }

    // Validation du code
    const validation = validateCode(code);
    if (!validation.isValid) {
      setError(validation.errors.join('\n'));
      if (onError) onError(validation.errors);
      return;
    }

    // Affichage des avertissements
    if (validation.warnings.length > 0) {
      showNotification(validation.warnings.join('\n'), 'warning');
    }

    setIsRunning(true);
    setOutput('');
    setError('');
    setWaitingForInput(false);
    
    const startTime = performance.now();
    
    if (onExecutionStart) onExecutionStart();

    try {
      // Création de la requête d'exécution
      const executionRequest = {
        code,
        language,
        input: inputData,
        timeout,
        userId: user?.id,
        exerciseId: exerciseContext?.id,
        settings: {
          memoryLimit: 128, // MB
          cpuTimeLimit: timeout / 1000, // seconds
          enableOptimizations: settings.enableOptimizations
        }
      };

      // Exécution via le service de compilation
      executionRef.current = compilerService.executeCode(executionRequest);
      const result = await executionRef.current;

      const endTime = performance.now();
      const execTime = Math.round(endTime - startTime);

      // Traitement du résultat
      if (result.success) {
        setOutput(result.output || 'Programme exécuté avec succès (aucune sortie)');
        setExecutionTime(result.executionTime || execTime);
        setMemoryUsage(result.memoryUsage || 0);

        // Vérification si le programme attend une entrée
        if (result.waitingForInput) {
          setWaitingForInput(true);
          if (inputRef.current) {
            inputRef.current.focus();
          }
          return;
        }

        // Notification de succès
        showNotification('Code exécuté avec succès!', 'success');

        // Mise à jour de la progression si dans un contexte d'exercice
        if (exerciseContext && user) {
          try {
            await progressService.updateExerciseProgress(
              user.id,
              exerciseContext.id,
              {
                completed: true,
                executionTime: execTime,
                code: code,
                attempts: (exerciseContext.attempts || 0) + 1
              }
            );
          } catch (progressError) {
            console.warn('Erreur lors de la mise à jour de la progression:', progressError);
          }
        }
      } else {
        // Gestion des erreurs
        const errorMessage = result.error || 'Erreur inconnue lors de l\'exécution';
        setError(errorMessage);
        showNotification('Erreur lors de l\'exécution', 'error');
        
        if (onError) onError([errorMessage]);
      }

      // Sauvegarde dans l'historique
      if (saveHistory) {
        const executionRecord = {
          id: Date.now(),
          code,
          output: result.output || '',
          error: result.error || '',
          executionTime: execTime,
          memoryUsage: result.memoryUsage || 0,
          status: result.success ? 'success' : 'error',
          timestamp: new Date().toISOString(),
          exerciseId: exerciseContext?.id
        };

        setExecutionHistory(prev => [executionRecord, ...prev.slice(0, 9)]); // Garder les 10 dernières
      }

      if (onExecutionComplete) {
        onExecutionComplete({
          success: result.success,
          output: result.output,
          error: result.error,
          executionTime: execTime,
          memoryUsage: result.memoryUsage
        });
      }

    } catch (err) {
      const endTime = performance.now();
      const execTime = Math.round(endTime - startTime);
      
      let errorMessage = 'Erreur lors de l\'exécution du code';
      
      if (err.name === 'AbortError') {
        errorMessage = 'Exécution annulée';
      } else if (err.message.includes('timeout')) {
        errorMessage = `Timeout d'exécution (${timeout}ms)`;
      } else if (err.message) {
        errorMessage = err.message;
      }

      setError(errorMessage);
      setExecutionTime(execTime);
      showNotification(errorMessage, 'error');
      
      if (onError) onError([errorMessage]);
      if (onExecutionComplete) {
        onExecutionComplete({
          success: false,
          error: errorMessage,
          executionTime: execTime
        });
      }
    } finally {
      setIsRunning(false);
      executionRef.current = null;
    }
  }, [code, language, timeout, user, exerciseContext, settings, showNotification, onExecutionStart, onExecutionComplete, onError, validateCode, saveHistory]);

  /**
   * Arrête l'exécution en cours
   */
  const stopExecution = useCallback(() => {
    if (executionRef.current) {
      executionRef.current.abort();
      setIsRunning(false);
      setWaitingForInput(false);
      showNotification('Exécution arrêtée', 'info');
    }
  }, [showNotification]);

  /**
   * Efface la sortie et les erreurs
   */
  const clearOutput = useCallback(() => {
    setOutput('');
    setError('');
    setExecutionTime(0);
    setMemoryUsage(0);
    setWaitingForInput(false);
  }, []);

  /**
   * Soumet l'entrée utilisateur au programme en cours d'exécution
   */
  const submitInput = useCallback(async () => {
    if (!waitingForInput || !userInput.trim()) return;

    try {
      await compilerService.sendInput(userInput);
      setUserInput('');
      setWaitingForInput(false);
    } catch (err) {
      showNotification('Erreur lors de l\'envoi de l\'entrée', 'error');
    }
  }, [waitingForInput, userInput, showNotification]);

  /**
   * Gère la soumission d'entrée via Enter
   */
  const handleInputKeyPress = useCallback((e) => {
    if (e.key === 'Enter') {
      submitInput();
    }
  }, [submitInput]);

  /**
   * Formate le temps d'exécution pour l'affichage
   * @param {number} time - Temps en millisecondes
   * @returns {string} Temps formaté
   */
  const formatExecutionTime = (time) => {
    if (time < 1000) {
      return `${time}ms`;
    }
    return `${(time / 1000).toFixed(2)}s`;
  };

  /**
   * Formate l'utilisation mémoire pour l'affichage
   * @param {number} memory - Mémoire en MB
   * @returns {string} Mémoire formatée
   */
  const formatMemoryUsage = (memory) => {
    if (memory < 1) {
      return `${Math.round(memory * 1024)}KB`;
    }
    return `${memory.toFixed(1)}MB`;
  };

  // Nettoyage lors du démontage
  useEffect(() => {
    return () => {
      if (executionRef.current) {
        executionRef.current.abort();
      }
    };
  }, []);

  return (
    <div className={`${styles.codeRunner} ${className}`}>
      {/* Barre de contrôle */}
      <div className={styles.controlBar}>
        <div className={styles.mainControls}>
          <Button
            onClick={() => executeCode()}
            disabled={isRunning || !code.trim()}
            className={styles.runButton}
            variant="primary"
          >
            {isRunning ? (
              <>
                <Loading size="small" />
                En cours...
              </>
            ) : (
              <>
                <Play size={16} />
                Exécuter
              </>
            )}
          </Button>

          {isRunning && (
            <Button
              onClick={stopExecution}
              className={styles.stopButton}
              variant="secondary"
            >
              <Square size={16} />
              Arrêter
            </Button>
          )}

          <Button
            onClick={clearOutput}
            className={styles.clearButton}
            variant="outline"
            disabled={isRunning}
          >
            <RotateCcw size={16} />
            Effacer
          </Button>
        </div>

        <div className={styles.settingsControls}>
          <Button
            className={styles.settingsButton}
            variant="ghost"
            title="Paramètres d'exécution"
          >
            <Settings size={16} />
          </Button>
        </div>
      </div>

      {/* Zone d'entrée utilisateur */}
      {(allowInput && waitingForInput) && (
        <div className={styles.inputSection}>
          <label className={styles.inputLabel}>
            Le programme attend votre saisie:
          </label>
          <div className={styles.inputGroup}>
            <input
              ref={inputRef}
              type="text"
              value={userInput}
              onChange={(e) => setUserInput(e.target.value)}
              onKeyPress={handleInputKeyPress}
              className={styles.userInput}
              placeholder="Tapez votre entrée et appuyez sur Entrée"
              disabled={isRunning}
            />
            <Button
              onClick={submitInput}
              disabled={!userInput.trim() || isRunning}
              variant="primary"
              size="small"
            >
              Envoyer
            </Button>
          </div>
        </div>
      )}

      {/* Zone de sortie */}
      <div className={styles.outputSection}>
        {/* Statistiques d'exécution */}
        {settings.showStats && (executionTime > 0 || memoryUsage > 0) && (
          <div className={styles.stats}>
            <div className={styles.statItem}>
              <span className={styles.statLabel}>Temps:</span>
              <span className={styles.statValue}>{formatExecutionTime(executionTime)}</span>
            </div>
            {memoryUsage > 0 && (
              <div className={styles.statItem}>
                <span className={styles.statLabel}>Mémoire:</span>
                <span className={styles.statValue}>{formatMemoryUsage(memoryUsage)}</span>
              </div>
            )}
          </div>
        )}

        {/* Sortie du programme */}
        {output && (
          <div className={styles.outputContainer}>
            <div className={styles.outputHeader}>
              <CheckCircle size={16} className={styles.successIcon} />
              <span>Sortie du programme</span>
            </div>
            <pre className={styles.output}>{output}</pre>
          </div>
        )}

        {/* Erreurs */}
        {error && (
          <div className={styles.errorContainer}>
            <div className={styles.errorHeader}>
              <AlertCircle size={16} className={styles.errorIcon} />
              <span>Erreur</span>
            </div>
            <pre className={styles.error}>{error}</pre>
          </div>
        )}

        {/* État d'attente */}
        {isRunning && !output && !error && (
          <div className={styles.loadingContainer}>
            <Loading />
            <span>Exécution en cours...</span>
          </div>
        )}

        {/* Message par défaut */}
        {!isRunning && !output && !error && (
          <div className={styles.defaultMessage}>
            <Play size={24} className={styles.defaultIcon} />
            <p>Cliquez sur "Exécuter" pour lancer votre code Java</p>
            {exerciseContext && (
              <p className={styles.exerciseHint}>
                Exercice: {exerciseContext.title}
              </p>
            )}
          </div>
        )}
      </div>

      {/* Historique des exécutions (masqué par défaut) */}
      {executionHistory.length > 0 && (
        <details className={styles.historySection}>
          <summary className={styles.historySummary}>
            Historique des exécutions ({executionHistory.length})
          </summary>
          <div className={styles.historyList}>
            {executionHistory.map((record) => (
              <div key={record.id} className={styles.historyItem}>
                <div className={styles.historyHeader}>
                  <span className={`${styles.historyStatus} ${styles[record.status]}`}>
                    {record.status === 'success' ? <CheckCircle size={12} /> : <AlertCircle size={12} />}
                  </span>
                  <span className={styles.historyTime}>
                    {new Date(record.timestamp).toLocaleTimeString()}
                  </span>
                  <span className={styles.historyDuration}>
                    {formatExecutionTime(record.executionTime)}
                  </span>
                </div>
                {record.output && (
                  <pre className={styles.historyOutput}>{record.output}</pre>
                )}
                {record.error && (
                  <pre className={styles.historyError}>{record.error}</pre>
                )}
              </div>
            ))}
          </div>
        </details>
      )}
    </div>
  );
};

export default CodeRunner;