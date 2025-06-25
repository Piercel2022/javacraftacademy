
import React, { useState, useEffect, useRef, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import { useCourse } from '../../hooks/useCourse';
import { useProgress } from '../../hooks/useProgress';
import { useNotification } from '../../hooks/useNotification';
import { courseService } from '../../services/courseService';
import { progressService } from '../../services/progressService';
import { CodeEditor } from '../editor/CodeEditor';
import { OutputPanel } from '../editor/OutputPanel';
import { Button } from '../common/Button';
import { Loading } from '../common/Loading';
import { Modal } from '../common/Modal';
import styles from './LessonPlayer.module.css';

/**
 * Composant LessonPlayer - Lecteur interactif de leçons
 * 
 * @component
 * @description
 * Composant principal pour la lecture et l'interaction avec les leçons.
 * Gère l'affichage du contenu, les exercices interactifs, la progression,
 * et l'intégration avec l'éditeur de code.
 * 
 * @features
 * - Lecture de contenu multimédia (vidéo, texte, code)
 * - Exercices interactifs avec validation en temps réel
 * - Suivi automatique de la progression
 * - Navigation entre les leçons
 * - Mode plein écran
 * - Sauvegarde automatique du progrès
 * - Support des raccourcis clavier
 * - Adaptation responsive
 * 
 * @relationships
 * - Parent: pages/Lesson, pages/CourseDetail
 * - Children: CodeEditor, OutputPanel, Button, Loading, Modal
 * - Contexts: AuthContext, CourseContext, ProgressContext, NotificationContext
 * - Services: courseService, progressService
 * - Hooks: useAuth, useCourse, useProgress, useNotification
 * 
 * @param {Object} props - Props du composant
 * @param {string} props.lessonId - ID de la leçon à afficher
 * @param {string} props.courseId - ID du cours parent
 * @param {Function} props.onComplete - Callback appelé à la fin de la leçon
 * @param {Function} props.onNext - Callback pour passer à la leçon suivante
 * @param {Function} props.onPrevious - Callback pour revenir à la leçon précédente
 * @param {boolean} props.autoPlay - Lecture automatique des vidéos
 * @param {boolean} props.showNotes - Affichage des notes
 * 
 * @returns {JSX.Element} Composant LessonPlayer
 * 
 * @example
 * ```jsx
 * <LessonPlayer
 *   lessonId="lesson-123"
 *   courseId="course-456" 
 *   onComplete={handleLessonComplete}
 *   onNext={handleNextLesson}
 *   onPrevious={handlePreviousLesson}
 *   autoPlay={true}
 *   showNotes={true}
 * />
 * ```
 * 
 * @since 1.0.0
 * @author JavaCraft Academy Team
 */
const LessonPlayer = ({
  lessonId: propLessonId,
  courseId: propCourseId,
  onComplete,
  onNext,
  onPrevious,
  autoPlay = false,
  showNotes = true
}) => {
  // Hooks et état
  const { lessonId: paramLessonId, courseId: paramCourseId } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  const { currentCourse, updateCourseProgress } = useCourse();
  const { updateLessonProgress, getLessonProgress } = useProgress();
  const { showNotification } = useNotification();

  // État local
  const [lesson, setLesson] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [currentStep, setCurrentStep] = useState(0);
  const [completedSteps, setCompletedSteps] = useState(new Set());
  const [userCode, setUserCode] = useState('');
  const [codeOutput, setCodeOutput] = useState('');
  const [isRunning, setIsRunning] = useState(false);
  const [showHints, setShowHints] = useState(false);
  const [isFullscreen, setIsFullscreen] = useState(false);
  const [notes, setNotes] = useState('');
  const [showNotesModal, setShowNotesModal] = useState(false);
  const [timeSpent, setTimeSpent] = useState(0);
  const [lastSaved, setLastSaved] = useState(null);

  // Refs
  const videoRef = useRef(null);
  const timerRef = useRef(null);
  const autoSaveRef = useRef(null);
  const containerRef = useRef(null);

  // IDs effectifs (props ou params)
  const effectiveLessonId = propLessonId || paramLessonId;
  const effectiveCourseId = propCourseId || paramCourseId;

  /**
   * Charge les données de la leçon
   * @async
   * @function loadLesson
   */
  const loadLesson = useCallback(async () => {
    if (!effectiveLessonId || !effectiveCourseId) {
      setError('ID de leçon ou de cours manquant');
      setLoading(false);
      return;
    }

    try {
      setLoading(true);
      
      // Chargement parallèle des données
      const [lessonData, progressData] = await Promise.all([
        courseService.getLesson(effectiveCourseId, effectiveLessonId),
        progressService.getLessonProgress(user.id, effectiveLessonId)
      ]);

      setLesson(lessonData);
      
      // Restaurer la progression
      if (progressData) {
        setCurrentStep(progressData.currentStep || 0);
        setCompletedSteps(new Set(progressData.completedSteps || []));
        setUserCode(progressData.userCode || lessonData.starterCode || '');
        setNotes(progressData.notes || '');
        setTimeSpent(progressData.timeSpent || 0);
      } else {
        setUserCode(lessonData.starterCode || '');
      }

      setError(null);
    } catch (err) {
      console.error('Erreur lors du chargement de la leçon:', err);
      setError('Impossible de charger la leçon');
      showNotification('Erreur lors du chargement de la leçon', 'error');
    } finally {
      setLoading(false);
    }
  }, [effectiveLessonId, effectiveCourseId, user.id, showNotification]);

  /**
   * Sauvegarde automatique du progrès
   * @async
   * @function autoSaveProgress
   */
  const autoSaveProgress = useCallback(async () => {
    if (!lesson || !user.id) return;

    try {
      const progressData = {
        userId: user.id,
        lessonId: effectiveLessonId,
        courseId: effectiveCourseId,
        currentStep,
        completedSteps: Array.from(completedSteps),
        userCode,
        notes,
        timeSpent,
        lastAccessed: new Date().toISOString()
      };

      await progressService.updateLessonProgress(progressData);
      setLastSaved(new Date());
    } catch (err) {
      console.error('Erreur lors de la sauvegarde:', err);
    }
  }, [lesson, user.id, effectiveLessonId, effectiveCourseId, currentStep, completedSteps, userCode, notes, timeSpent]);

  /**
   * Passe à l'étape suivante
   * @function nextStep
   */
  const nextStep = useCallback(() => {
    if (!lesson || currentStep >= lesson.steps.length - 1) return;

    const newStep = currentStep + 1;
    setCurrentStep(newStep);
    setCompletedSteps(prev => new Set([...prev, currentStep]));

    // Auto-scroll vers le haut
    if (containerRef.current) {
      containerRef.current.scrollTop = 0;
    }
  }, [lesson, currentStep]);

  /**
   * Revient à l'étape précédente
   * @function previousStep
   */
  const previousStep = useCallback(() => {
    if (currentStep <= 0) return;
    setCurrentStep(currentStep - 1);
  }, [currentStep]);

  /**
   * Marque l'étape comme complétée
   * @function completeStep
   * @param {number} stepIndex - Index de l'étape à marquer comme complétée
   */
  const completeStep = useCallback((stepIndex) => {
    setCompletedSteps(prev => new Set([...prev, stepIndex]));
    
    // Si c'est la dernière étape, marquer la leçon comme complétée
    if (stepIndex === lesson.steps.length - 1) {
      handleLessonComplete();
    }
  }, [lesson]);

  /**
   * Gère la completion de la leçon
   * @async
   * @function handleLessonComplete
   */
  const handleLessonComplete = useCallback(async () => {
    try {
      // Marquer toutes les étapes comme complétées
      const allSteps = new Set(Array.from({ length: lesson.steps.length }, (_, i) => i));
      setCompletedSteps(allSteps);

      // Mettre à jour la progression globale
      await updateLessonProgress(effectiveLessonId, {
        completed: true,
        completionDate: new Date().toISOString(),
        timeSpent,
        score: calculateScore()
      });

      await updateCourseProgress(effectiveCourseId);

      showNotification('Leçon terminée avec succès !', 'success');
      
      if (onComplete) {
        onComplete({
          lessonId: effectiveLessonId,
          timeSpent,
          score: calculateScore()
        });
      }
    } catch (err) {
      console.error('Erreur lors de la completion:', err);
      showNotification('Erreur lors de la sauvegarde du progrès', 'error');
    }
  }, [lesson, effectiveLessonId, effectiveCourseId, timeSpent, updateLessonProgress, updateCourseProgress, showNotification, onComplete]);

  /**
   * Calcule le score de la leçon
   * @function calculateScore
   * @returns {number} Score entre 0 et 100
   */
  const calculateScore = useCallback(() => {
    if (!lesson) return 0;
    
    const completionRate = completedSteps.size / lesson.steps.length;
    const timeBonus = Math.max(0, 1 - (timeSpent / lesson.estimatedDuration));
    
    return Math.round((completionRate * 80) + (timeBonus * 20));
  }, [lesson, completedSteps, timeSpent]);

  /**
   * Exécute le code utilisateur
   * @async
   * @function runCode
   */
  const runCode = useCallback(async () => {
    if (!userCode.trim()) {
      showNotification('Veuillez saisir du code à exécuter', 'warning');
      return;
    }

    setIsRunning(true);
    setCodeOutput('');

    try {
      // Simuler l'exécution du code (à remplacer par un vrai service)
      await new Promise(resolve => setTimeout(resolve, 1000));
      
      // Validation du code selon les critères de l'étape
      const currentStepData = lesson.steps[currentStep];
      if (currentStepData.type === 'exercise') {
        const isValid = validateCode(userCode, currentStepData.validation);
        if (isValid) {
          setCodeOutput('✅ Exercice réussi !\n' + (currentStepData.successMessage || ''));
          completeStep(currentStep);
        } else {
          setCodeOutput('❌ Code incorrect. Vérifiez votre solution.\n' + (currentStepData.errorMessage || ''));
        }
      } else {
        setCodeOutput('Code exécuté avec succès !');
      }
    } catch (err) {
      setCodeOutput('Erreur lors de l\'exécution: ' + err.message);
    } finally {
      setIsRunning(false);
    }
  }, [userCode, lesson, currentStep, completeStep, showNotification]);

  /**
   * Valide le code selon les critères définis
   * @function validateCode
   * @param {string} code - Code à valider
   * @param {Object} validation - Critères de validation
   * @returns {boolean} True si le code est valide
   */
  const validateCode = (code, validation) => {
    if (!validation) return true;

    // Validation simple basée sur des patterns
    if (validation.contains) {
      return validation.contains.every(pattern => code.includes(pattern));
    }

    if (validation.matches) {
      return new RegExp(validation.matches).test(code);
    }

    return true;
  };

  /**
   * Basculer le mode plein écran
   * @function toggleFullscreen
   */
  const toggleFullscreen = useCallback(() => {
    if (!document.fullscreenElement) {
      containerRef.current?.requestFullscreen();
      setIsFullscreen(true);
    } else {
      document.exitFullscreen();
      setIsFullscreen(false);
    }
  }, []);

  /**
   * Gestion des raccourcis clavier
   * @function handleKeyPress
   * @param {KeyboardEvent} event - Événement clavier
   */
  const handleKeyPress = useCallback((event) => {
    if (event.ctrlKey || event.metaKey) {
      switch (event.key) {
        case 'Enter':
          event.preventDefault();
          runCode();
          break;
        case 'f':
          event.preventDefault();
          toggleFullscreen();
          break;
        case 's':
          event.preventDefault();
          autoSaveProgress();
          break;
        default:
          break;
      }
    } else {
      switch (event.key) {
        case 'ArrowRight':
          if (event.altKey) {
            event.preventDefault();
            nextStep();
          }
          break;
        case 'ArrowLeft':
          if (event.altKey) {
            event.preventDefault();
            previousStep();
          }
          break;
        default:
          break;
      }
    }
  }, [runCode, toggleFullscreen, autoSaveProgress, nextStep, previousStep]);

  // Effets
  useEffect(() => {
    loadLesson();
  }, [loadLesson]);

  useEffect(() => {
    // Timer pour le temps passé
    timerRef.current = setInterval(() => {
      setTimeSpent(prev => prev + 1);
    }, 1000);

    return () => {
      if (timerRef.current) {
        clearInterval(timerRef.current);
      }
    };
  }, []);

  useEffect(() => {
    // Auto-sauvegarde toutes les 30 secondes
    autoSaveRef.current = setInterval(() => {
      autoSaveProgress();
    }, 30000);

    return () => {
      if (autoSaveRef.current) {
        clearInterval(autoSaveRef.current);
      }
    };
  }, [autoSaveProgress]);

  useEffect(() => {
    // Gestion des événements clavier
    document.addEventListener('keydown', handleKeyPress);
    
    return () => {
      document.removeEventListener('keydown', handleKeyPress);
    };
  }, [handleKeyPress]);

  useEffect(() => {
    // Gestion des changements de plein écran
    const handleFullscreenChange = () => {
      setIsFullscreen(!!document.fullscreenElement);
    };

    document.addEventListener('fullscreenchange', handleFullscreenChange);
    
    return () => {
      document.removeEventListener('fullscreenchange', handleFullscreenChange);
    };
  }, []);

  // Sauvegarde avant de quitter
  useEffect(() => {
    const handleBeforeUnload = () => {
      autoSaveProgress();
    };

    window.addEventListener('beforeunload', handleBeforeUnload);
    
    return () => {
      window.removeEventListener('beforeunload', handleBeforeUnload);
      autoSaveProgress(); // Sauvegarde finale
    };
  }, [autoSaveProgress]);

  if (loading) {
    return (
      <div className={styles.loadingContainer}>
        <Loading message="Chargement de la leçon..." />
      </div>
    );
  }

  if (error) {
    return (
      <div className={styles.errorContainer}>
        <div className={styles.errorMessage}>
          <h3>Erreur</h3>
          <p>{error}</p>
          <Button onClick={loadLesson} variant="primary">
            Réessayer
          </Button>
        </div>
      </div>
    );
  }

  if (!lesson) {
    return (
      <div className={styles.errorContainer}>
        <div className={styles.errorMessage}>
          <h3>Leçon introuvable</h3>
          <p>La leçon demandée n'existe pas ou n'est plus disponible.</p>
          <Button onClick={() => navigate(-1)} variant="secondary">
            Retour
          </Button>
        </div>
      </div>
    );
  }

  const currentStepData = lesson.steps[currentStep];
  const progressPercentage = ((completedSteps.size / lesson.steps.length) * 100).toFixed(1);

  return (
    <div 
      ref={containerRef}
      className={`${styles.lessonPlayer} ${isFullscreen ? styles.fullscreen : ''}`}
    >
      {/* En-tête */}
      <header className={styles.header}>
        <div className={styles.headerLeft}>
          <h1 className={styles.lessonTitle}>{lesson.title}</h1>
          <div className={styles.lessonMeta}>
            <span className={styles.stepIndicator}>
              Étape {currentStep + 1} sur {lesson.steps.length}
            </span>
            <span className={styles.progressText}>
              {progressPercentage}% complété
            </span>
          </div>
        </div>

        <div className={styles.headerRight}>
          <div className={styles.timeSpent}>
            {Math.floor(timeSpent / 60)}:{(timeSpent % 60).toString().padStart(2, '0')}
          </div>
          
          <div className={styles.headerActions}>
            {showNotes && (
              <Button
                onClick={() => setShowNotesModal(true)}
                variant="ghost"
                size="sm"
                title="Notes (Ctrl+N)"
              >
                📝
              </Button>
            )}
            
            <Button
              onClick={() => setShowHints(!showHints)}
              variant="ghost"
              size="sm"
              title="Indices"
              className={showHints ? styles.active : ''}
            >
              💡
            </Button>
            
            <Button
              onClick={toggleFullscreen}
              variant="ghost"
              size="sm"
              title="Plein écran (Ctrl+F)"
            >
              {isFullscreen ? '🗗' : '🗖'}
            </Button>
          </div>
        </div>
      </header>

      {/* Barre de progression */}
      <div className={styles.progressBar}>
        <div 
          className={styles.progressFill}
          style={{ width: `${progressPercentage}%` }}
        />
      </div>

      {/* Contenu principal */}
      <main className={styles.mainContent}>
        <div className={styles.contentArea}>
          {/* Zone de contenu */}
          <section className={styles.contentSection}>
            <div className={styles.stepContent}>
              <h2 className={styles.stepTitle}>{currentStepData.title}</h2>
              
              {/* Contenu vidéo */}
              {currentStepData.video && (
                <div className={styles.videoContainer}>
                  <video
                    ref={videoRef}
                    src={currentStepData.video.url}
                    controls
                    autoPlay={autoPlay}
                    className={styles.video}
                    onEnded={() => completeStep(currentStep)}
                  />
                </div>
              )}

              {/* Contenu texte */}
              <div 
                className={styles.textContent}
                dangerouslySetInnerHTML={{ __html: currentStepData.content }}
              />

              {/* Code exemple */}
              {currentStepData.exampleCode && (
                <div className={styles.exampleCode}>
                  <h4>Exemple :</h4>
                  <pre className={styles.codeBlock}>
                    <code>{currentStepData.exampleCode}</code>
                  </pre>
                </div>
              )}

              {/* Indices */}
              {showHints && currentStepData.hints && (
                <div className={styles.hints}>
                  <h4>💡 Indices :</h4>
                  <ul>
                    {currentStepData.hints.map((hint, index) => (
                      <li key={index}>{hint}</li>
                    ))}
                  </ul>
                </div>
              )}
            </div>
          </section>

          {/* Zone d'exercice */}
          {currentStepData.type === 'exercise' && (
            <section className={styles.exerciseSection}>
              <div className={styles.exerciseHeader}>
                <h3>✏️ Exercice pratique</h3>
                <div className={styles.exerciseActions}>
                  <Button
                    onClick={runCode}
                    variant="primary"
                    disabled={isRunning}
                    title="Exécuter le code (Ctrl+Enter)"
                  >
                    {isRunning ? '⏳ Exécution...' : '▶️ Exécuter'}
                  </Button>
                </div>
              </div>

              <div className={styles.codeArea}>
                <CodeEditor
                  value={userCode}
                  onChange={setUserCode}
                  language="java"
                  theme="vs-dark"
                  height="300px"
                  options={{
                    minimap: { enabled: false },
                    lineNumbers: 'on',
                    wordWrap: 'on'
                  }}
                />
              </div>

              <OutputPanel
                output={codeOutput}
                isRunning={isRunning}
                className={styles.outputPanel}
              />
            </section>
          )}
        </div>
      </main>

      {/* Navigation */}
      <footer className={styles.navigation}>
        <div className={styles.navLeft}>
          <Button
            onClick={onPrevious || previousStep}
            disabled={currentStep === 0 && !onPrevious}
            variant="secondary"
          >
            ← Précédent
          </Button>
        </div>

        <div className={styles.navCenter}>
          <div className={styles.stepDots}>
            {lesson.steps.map((_, index) => (
              <button
                key={index}
                className={`${styles.stepDot} ${
                  index === currentStep ? styles.current : ''
                } ${
                  completedSteps.has(index) ? styles.completed : ''
                }`}
                onClick={() => setCurrentStep(index)}
                title={`Étape ${index + 1}`}
              />
            ))}
          </div>
        </div>

        <div className={styles.navRight}>
          {currentStep < lesson.steps.length - 1 ? (
            <Button
              onClick={nextStep}
              disabled={!completedSteps.has(currentStep)}
              variant="primary"
            >
              Suivant →
            </Button>
          ) : (
            <Button
              onClick={onNext || (() => navigate(`/courses/${effectiveCourseId}`))}
              variant="primary"
            >
              {onNext ? 'Leçon suivante' : 'Retour au cours'} →
            </Button>
          )}
        </div>
      </footer>

      {/* Modal des notes */}
      {showNotesModal && (
        <Modal
          isOpen={showNotesModal}
          onClose={() => setShowNotesModal(false)}
          title="📝 Mes notes"
          size="large"
        >
          <div className={styles.notesModal}>
            <textarea
              value={notes}
              onChange={(e) => setNotes(e.target.value)}
              placeholder="Prenez des notes sur cette leçon..."
              className={styles.notesTextarea}
              rows={10}
            />
            <div className={styles.notesActions}>
              <Button
                onClick={() => setShowNotesModal(false)}
                variant="secondary"
              >
                Fermer
              </Button>
              <Button
                onClick={() => {
                  autoSaveProgress();
                  setShowNotesModal(false);
                  showNotification('Notes sauvegardées', 'success');
                }}
                variant="primary"
              >
                Sauvegarder
              </Button>
            </div>
          </div>
        </Modal>
      )}

      {/* Indicateur de sauvegarde */}
      {lastSaved && (
        <div className={styles.saveIndicator}>
          Sauvegardé à {lastSaved.toLocaleTimeString()}
        </div>
      )}
    </div>
  );
};

export default LessonPlayer;cd