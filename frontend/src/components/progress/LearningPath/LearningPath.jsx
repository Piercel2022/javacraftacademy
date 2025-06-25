
import React, { useState, useEffect, useCallback } from 'react';
import { useProgress } from '../../hooks/useProgress';
import { useCourse } from '../../hooks/useCourse';
import { useAuth } from '../../hooks/useAuth';
import { useNotification } from '../../hooks/useNotification';
import ProgressBar from '../ProgressBar';
import CourseCard from '../../course/CourseCard';
import Button from '../../common/Button';
import Modal from '../../common/Modal';
import Loading from '../../common/Loading';
import styles from './LearningPath.module.css';

/**
 * Composant LearningPath - Affiche et gère les parcours d'apprentissage personnalisés
 * 
 * Ce composant central du système de progression permet aux utilisateurs de :
 * - Visualiser leur parcours d'apprentissage personnalisé
 * - Naviguer entre les différentes étapes du parcours
 * - Suivre leur progression globale
 * - Accéder aux cours recommandés
 * - Débloquer de nouveaux contenus
 * 
 * Relations avec l'application :
 * - Utilise useProgress pour récupérer les données de progression
 * - Utilise useCourse pour accéder aux informations des cours
 * - Utilise useAuth pour identifier l'utilisateur
 * - Communique avec ProgressContext pour l'état global
 * - Intègre CourseCard pour l'affichage des cours
 * - Utilise le service progressService pour les mises à jour
 * 
 * @component
 * @example
 * return (
 *   <LearningPath 
 *     userId={currentUser.id}
 *     pathId="java-fundamentals"
 *     onPathComplete={handlePathComplete}
 *   />
 * )
 */
const LearningPath = ({ 
  userId, 
  pathId, 
  onPathComplete, 
  onCourseSelect,
  className = '',
  showPrerequisites = true,
  allowPathSwitching = true
}) => {
  // États locaux du composant
  const [selectedPath, setSelectedPath] = useState(pathId);
  const [isPathModalOpen, setIsPathModalOpen] = useState(false);
  const [expandedSteps, setExpandedSteps] = useState(new Set());
  const [hoveredStep, setHoveredStep] = useState(null);

  // Hooks personnalisés pour la gestion des données
  const { user } = useAuth();
  const { showNotification } = useNotification();
  const {
    userProgress,
    learningPath,
    availablePaths,
    recommendations,
    isLoading: progressLoading,
    updateProgress,
    unlockStep,
    completePath,
    switchPath
  } = useProgress(userId, selectedPath);

  const {
    courses,
    getCourseById,
    isLoading: coursesLoading
  } = useCourse();

  // État de chargement combiné
  const isLoading = progressLoading || coursesLoading;

  /**
   * Initialise le parcours sélectionné au montage du composant
   */
  useEffect(() => {
    if (pathId && pathId !== selectedPath) {
      setSelectedPath(pathId);
    }
  }, [pathId, selectedPath]);

  /**
   * Gère la sélection d'un cours dans le parcours
   * @param {Object} course - Le cours sélectionné
   * @param {Object} step - L'étape du parcours associée
   */
  const handleCourseSelect = useCallback((course, step) => {
    // Vérifier les prérequis si activé
    if (showPrerequisites && !step.isUnlocked) {
      showNotification({
        type: 'warning',
        title: 'Prérequis non satisfaits',
        message: `Vous devez d'abord compléter les étapes précédentes pour accéder à "${course.title}".`
      });
      return;
    }

    // Mettre à jour la progression
    updateProgress(step.id, { lastAccessed: new Date().toISOString() });

    // Notifier le parent
    if (onCourseSelect) {
      onCourseSelect(course, step);
    }
  }, [showPrerequisites, updateProgress, onCourseSelect, showNotification]);

  /**
   * Gère le changement de parcours d'apprentissage
   * @param {string} newPathId - ID du nouveau parcours
   */
  const handlePathSwitch = useCallback(async (newPathId) => {
    try {
      await switchPath(newPathId);
      setSelectedPath(newPathId);
      setIsPathModalOpen(false);
      
      showNotification({
        type: 'success',
        title: 'Parcours mis à jour',
        message: `Vous avez basculé vers le parcours "${availablePaths.find(p => p.id === newPathId)?.title}".`
      });
    } catch (error) {
      showNotification({
        type: 'error',
        title: 'Erreur',
        message: 'Impossible de changer de parcours. Veuillez réessayer.'
      });
    }
  }, [switchPath, availablePaths, showNotification]);

  /**
   * Gère l'expansion/réduction des étapes du parcours
   * @param {string} stepId - ID de l'étape à basculer
   */
  const toggleStepExpansion = useCallback((stepId) => {
    setExpandedSteps(prev => {
      const newSet = new Set(prev);
      if (newSet.has(stepId)) {
        newSet.delete(stepId);
      } else {
        newSet.add(stepId);
      }
      return newSet;
    });
  }, []);

  /**
   * Calcule le pourcentage de progression global du parcours
   * @returns {number} Pourcentage de progression (0-100)
   */
  const calculateOverallProgress = useCallback(() => {
    if (!learningPath?.steps || learningPath.steps.length === 0) {
      return 0;
    }

    const completedSteps = learningPath.steps.filter(step => step.isCompleted).length;
    return Math.round((completedSteps / learningPath.steps.length) * 100);
  }, [learningPath]);

  /**
   * Détermine si une étape peut être débloquée
   * @param {Object} step - L'étape à vérifier
   * @returns {boolean} True si l'étape peut être débloquée
   */
  const canUnlockStep = useCallback((step) => {
    if (step.isUnlocked) return false;
    
    const prerequisites = step.prerequisites || [];
    return prerequisites.every(prereqId => {
      const prereqStep = learningPath.steps.find(s => s.id === prereqId);
      return prereqStep?.isCompleted;
    });
  }, [learningPath]);

  /**
   * Gère le déblocage d'une nouvelle étape
   * @param {string} stepId - ID de l'étape à débloquer
   */
  const handleUnlockStep = useCallback(async (stepId) => {
    try {
      await unlockStep(stepId);
      showNotification({
        type: 'success',
        title: 'Nouvelle étape débloquée !',
        message: 'Félicitations ! Vous pouvez maintenant accéder à la prochaine étape.'
      });
    } catch (error) {
      showNotification({
        type: 'error',
        title: 'Erreur de déblocage',
        message: 'Impossible de débloquer cette étape. Veuillez réessayer.'
      });
    }
  }, [unlockStep, showNotification]);

  /**
   * Gère la finalisation complète d'un parcours
   */
  const handleCompleteEntirePath = useCallback(async () => {
    try {
      await completePath(selectedPath);
      
      showNotification({
        type: 'success',
        title: 'Parcours terminé !',
        message: `Félicitations ! Vous avez terminé le parcours "${learningPath.title}".`
      });

      if (onPathComplete) {
        onPathComplete(selectedPath, learningPath);
      }
    } catch (error) {
      showNotification({
        type: 'error',
        title: 'Erreur',
        message: 'Impossible de finaliser le parcours. Veuillez réessayer.'
      });
    }
  }, [completePath, selectedPath, learningPath, onPathComplete, showNotification]);

  /**
   * Rendu d'une étape individuelle du parcours
   * @param {Object} step - L'étape à afficher
   * @param {number} index - Index de l'étape
   * @returns {JSX.Element} Élément JSX de l'étape
   */
  const renderStep = useCallback((step, index) => {
    const course = getCourseById(step.courseId);
    const isExpanded = expandedSteps.has(step.id);
    const isHovered = hoveredStep === step.id;
    const canUnlock = canUnlockStep(step);

    return (
      <div
        key={step.id}
        className={`${styles.step} ${step.isCompleted ? styles.completed : ''} ${step.isUnlocked ? styles.unlocked : ''}`}
        onMouseEnter={() => setHoveredStep(step.id)}
        onMouseLeave={() => setHoveredStep(null)}
      >
        {/* Connecteur entre les étapes */}
        {index > 0 && (
          <div className={`${styles.connector} ${step.isUnlocked ? styles.activeConnector : ''}`} />
        )}

        {/* Numéro et statut de l'étape */}
        <div className={styles.stepHeader}>
          <div className={`${styles.stepNumber} ${step.isCompleted ? styles.completedNumber : ''}`}>
            {step.isCompleted ? '✓' : index + 1}
          </div>
          
          <div className={styles.stepInfo}>
            <h3 className={styles.stepTitle}>{step.title}</h3>
            <p className={styles.stepDescription}>{step.description}</p>
            
            {/* Badges de statut */}
            <div className={styles.stepBadges}>
              {step.isCompleted && <span className={styles.badge}>Terminé</span>}
              {step.isUnlocked && !step.isCompleted && <span className={styles.badge}>Disponible</span>}
              {!step.isUnlocked && canUnlock && <span className={styles.badge}>Peut être débloqué</span>}
              {step.difficulty && <span className={`${styles.badge} ${styles[step.difficulty.toLowerCase()]}`}>{step.difficulty}</span>}
            </div>
          </div>

          {/* Boutons d'action */}
          <div className={styles.stepActions}>
            {step.isUnlocked && course && (
              <Button
                variant="primary"
                size="small"
                onClick={() => handleCourseSelect(course, step)}
              >
                {step.isCompleted ? 'Revoir' : 'Commencer'}
              </Button>
            )}
            
            {!step.isUnlocked && canUnlock && (
              <Button
                variant="secondary"
                size="small"
                onClick={() => handleUnlockStep(step.id)}
              >
                Débloquer
              </Button>
            )}

            <Button
              variant="ghost"
              size="small"
              onClick={() => toggleStepExpansion(step.id)}
              className={styles.expandButton}
            >
              {isExpanded ? '−' : '+'}
            </Button>
          </div>
        </div>

        {/* Contenu étendu de l'étape */}
        {isExpanded && (
          <div className={styles.stepContent}>
            {course && (
              <div className={styles.coursePreview}>
                <CourseCard
                  course={course}
                  compact={true}
                  showProgress={true}
                  onClick={() => handleCourseSelect(course, step)}
                />
              </div>
            )}

            {/* Objectifs d'apprentissage */}
            {step.learningObjectives && step.learningObjectives.length > 0 && (
              <div className={styles.objectives}>
                <h4>Objectifs d'apprentissage :</h4>
                <ul>
                  {step.learningObjectives.map((objective, idx) => (
                    <li key={idx}>{objective}</li>
                  ))}
                </ul>
              </div>
            )}

            {/* Prérequis */}
            {showPrerequisites && step.prerequisites && step.prerequisites.length > 0 && (
              <div className={styles.prerequisites}>
                <h4>Prérequis :</h4>
                <ul>
                  {step.prerequisites.map(prereqId => {
                    const prereqStep = learningPath.steps.find(s => s.id === prereqId);
                    return prereqStep ? (
                      <li key={prereqId} className={prereqStep.isCompleted ? styles.completed : ''}>
                        {prereqStep.title} {prereqStep.isCompleted ? '✓' : ''}
                      </li>
                    ) : null;
                  })}
                </ul>
              </div>
            )}
          </div>
        )}
      </div>
    );
  }, [
    getCourseById, 
    expandedSteps, 
    hoveredStep, 
    canUnlockStep, 
    handleCourseSelect, 
    handleUnlockStep, 
    toggleStepExpansion,
    showPrerequisites,
    learningPath
  ]);

  // Affichage du composant de chargement
  if (isLoading) {
    return (
      <div className={`${styles.container} ${className}`}>
        <Loading message="Chargement de votre parcours d'apprentissage..." />
      </div>
    );
  }

  // Affichage si aucun parcours n'est trouvé
  if (!learningPath) {
    return (
      <div className={`${styles.container} ${styles.noPath} ${className}`}>
        <div className={styles.noPathContent}>
          <h2>Aucun parcours d'apprentissage</h2>
          <p>Vous n'avez pas encore de parcours d'apprentissage personnalisé.</p>
          {allowPathSwitching && availablePaths.length > 0 && (
            <Button
              variant="primary"
              onClick={() => setIsPathModalOpen(true)}
            >
              Choisir un parcours
            </Button>
          )}
        </div>
      </div>
    );
  }

  const overallProgress = calculateOverallProgress();
  const isPathComplete = overallProgress === 100;

  return (
    <div className={`${styles.container} ${className}`}>
      {/* En-tête du parcours */}
      <div className={styles.header}>
        <div className={styles.pathInfo}>
          <h1 className={styles.pathTitle}>{learningPath.title}</h1>
          <p className={styles.pathDescription}>{learningPath.description}</p>
          
          {/* Métadonnées du parcours */}
          <div className={styles.pathMeta}>
            <span className={styles.duration}>
              Durée estimée : {learningPath.estimatedDuration || 'Non spécifiée'}
            </span>
            <span className={styles.difficulty}>
              Niveau : {learningPath.difficulty || 'Intermédiaire'}
            </span>
            <span className={styles.stepCount}>
              {learningPath.steps.length} étapes
            </span>
          </div>
        </div>

        {/* Actions du parcours */}
        <div className={styles.headerActions}>
          {allowPathSwitching && (
            <Button
              variant="ghost"
              onClick={() => setIsPathModalOpen(true)}
            >
              Changer de parcours
            </Button>
          )}
          
          {isPathComplete && (
            <Button
              variant="success"
              onClick={handleCompleteEntirePath}
            >
              Finaliser le parcours
            </Button>
          )}
        </div>
      </div>

      {/* Barre de progression globale */}
      <div className={styles.progressSection}>
        <div className={styles.progressHeader}>
          <h3>Progression globale</h3>
          <span className={styles.progressText}>{overallProgress}% terminé</span>
        </div>
        <ProgressBar
          value={overallProgress}
          max={100}
          showText={false}
          className={styles.progressBar}
        />
      </div>

      {/* Liste des étapes du parcours */}
      <div className={styles.stepsContainer}>
        <h3 className={styles.stepsTitle}>Étapes du parcours</h3>
        <div className={styles.steps}>
          {learningPath.steps.map((step, index) => renderStep(step, index))}
        </div>
      </div>

      {/* Recommandations */}
      {recommendations && recommendations.length > 0 && (
        <div className={styles.recommendations}>
          <h3>Recommandations pour vous</h3>
          <div className={styles.recommendationList}>
            {recommendations.slice(0, 3).map(rec => (
              <div key={rec.id} className={styles.recommendation}>
                <h4>{rec.title}</h4>
                <p>{rec.description}</p>
                <Button
                  variant="outline"
                  size="small"
                  onClick={() => rec.action && rec.action()}
                >
                  {rec.actionText || 'En savoir plus'}
                </Button>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Modal de sélection de parcours */}
      {isPathModalOpen && (
        <Modal
          isOpen={isPathModalOpen}
          onClose={() => setIsPathModalOpen(false)}
          title="Choisir un parcours d'apprentissage"
          className={styles.pathModal}
        >
          <div className={styles.pathSelection}>
            {availablePaths.map(path => (
              <div
                key={path.id}
                className={`${styles.pathOption} ${path.id === selectedPath ? styles.selected : ''}`}
                onClick={() => handlePathSwitch(path.id)}
              >
                <h4>{path.title}</h4>
                <p>{path.description}</p>
                <div className={styles.pathOptionMeta}>
                  <span>{path.steps?.length || 0} étapes</span>
                  <span>{path.difficulty}</span>
                  <span>{path.estimatedDuration}</span>
                </div>
              </div>
            ))}
          </div>
        </Modal>
      )}
    </div>
  );
};

export default LearningPath;