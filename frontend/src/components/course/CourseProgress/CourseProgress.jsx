
import React, { useState, useEffect, useMemo } from 'react';
import PropTypes from 'prop-types';
import styles from './CourseProgress.module.css';
import { useProgress } from '../../../hooks/useProgress';
import { useCourse } from '../../../hooks/useCourse';
import { useAuth } from '../../../hooks/useAuth';
import { useNotification } from '../../../hooks/useNotification';
import ProgressBar from '../../progress/ProgressBar';
import Achievement from '../../progress/Achievement';
import { formatters } from '../../../utils/formatters';
import { dateUtils } from '../../../utils/dateUtils';

/**
 * CourseProgress - Composant pour afficher et gérer la progression d'un cours
 * 
 * Ce composant est responsable de :
 * - Afficher la progression globale du cours
 * - Montrer les détails des leçons complétées/en cours
 * - Gérer les achievements débloqués
 * - Calculer les statistiques de temps et performance
 * - Synchroniser la progression avec le backend
 * 
 * Relations avec l'application :
 * - Utilise ProgressContext pour l'état de progression
 * - Connecté à CourseContext pour les données du cours
 * - Intégré avec NotificationContext pour les alertes
 * - Communique avec progressService pour la persistance
 * - Utilise ProgressBar et Achievement comme composants enfants
 * 
 * @component
 * @param {Object} props - Les propriétés du composant
 * @param {number} props.courseId - ID du cours à afficher
 * @param {string} props.variant - Variante d'affichage ('full', 'compact', 'minimal')
 * @param {boolean} props.showAchievements - Afficher ou non les achievements
 * @param {boolean} props.showTimeStats - Afficher ou non les statistiques de temps
 * @param {boolean} props.interactive - Permet l'interaction (marquer leçon complétée)
 * @param {Function} props.onProgressUpdate - Callback appelé lors d'une mise à jour
 * @param {Function} props.onLessonClick - Callback appelé lors du clic sur une leçon
 * @param {string} props.className - Classes CSS additionnelles
 * 
 * @example
 * // Utilisation basique
 * <CourseProgress courseId={123} />
 * 
 * @example
 * // Utilisation avancée avec callbacks
 * <CourseProgress 
 *   courseId={123}
 *   variant="full"
 *   showAchievements={true}
 *   onProgressUpdate={handleProgressUpdate}
 *   onLessonClick={handleLessonClick}
 * />
 */
const CourseProgress = ({
  courseId,
  variant = 'full',
  showAchievements = true,
  showTimeStats = true,
  interactive = false,
  onProgressUpdate,
  onLessonClick,
  className = ''
}) => {
  // States locaux
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);
  const [expandedSections, setExpandedSections] = useState(new Set());

  // Hooks personnalisés
  const { user } = useAuth();
  const { showNotification } = useNotification();
  const { 
    progress, 
    updateLessonProgress, 
    calculateCourseProgress,
    getTimeSpent,
    isLoading: progressLoading 
  } = useProgress();
  const { 
    getCourseById, 
    isLoading: courseLoading 
  } = useCourse();

  // Données du cours et progression
  const course = useMemo(() => getCourseById(courseId), [courseId, getCourseById]);
  const courseProgress = useMemo(() => 
    calculateCourseProgress(courseId), 
    [courseId, calculateCourseProgress, progress]
  );

  /**
   * Effet pour charger les données initiales
   */
  useEffect(() => {
    const loadData = async () => {
      try {
        setIsLoading(true);
        setError(null);
        
        if (!course) {
          throw new Error('Cours non trouvé');
        }

        // Données déjà disponibles via les hooks
        setIsLoading(false);
      } catch (err) {
        setError(err.message);
        setIsLoading(false);
        showNotification('Erreur lors du chargement de la progression', 'error');
      }
    };

    loadData();
  }, [courseId, course, showNotification]);

  /**
   * Calcule les statistiques détaillées du cours
   * @returns {Object} Statistiques calculées
   */
  const calculateStats = useMemo(() => {
    if (!course || !courseProgress) return null;

    const totalLessons = course.lessons?.length || 0;
    const completedLessons = courseProgress.completedLessons || 0;
    const totalTime = getTimeSpent(courseId);
    const averageTimePerLesson = totalTime && completedLessons > 0 
      ? totalTime / completedLessons 
      : 0;

    return {
      totalLessons,
      completedLessons,
      remainingLessons: totalLessons - completedLessons,
      completionPercentage: courseProgress.percentage || 0,
      totalTimeSpent: totalTime,
      averageTimePerLesson,
      lastActivity: courseProgress.lastActivity,
      streak: courseProgress.streak || 0,
      achievements: courseProgress.achievements || []
    };
  }, [course, courseProgress, getTimeSpent, courseId]);

  /**
   * Gère le clic sur une leçon
   * @param {Object} lesson - Données de la leçon
   * @param {number} lessonIndex - Index de la leçon
   */
  const handleLessonClick = (lesson, lessonIndex) => {
    if (onLessonClick) {
      onLessonClick(lesson, lessonIndex);
    }
  };

  /**
   * Marque une leçon comme complétée
   * @param {number} lessonId - ID de la leçon
   */
  const handleMarkCompleted = async (lessonId) => {
    if (!interactive) return;

    try {
      await updateLessonProgress(courseId, lessonId, {
        completed: true,
        completedAt: new Date().toISOString(),
        timeSpent: 0 // Sera calculé par le service
      });

      showNotification('Leçon marquée comme complétée !', 'success');
      
      if (onProgressUpdate) {
        onProgressUpdate(calculateCourseProgress(courseId));
      }
    } catch (error) {
      showNotification('Erreur lors de la mise à jour', 'error');
    }
  };

  /**
   * Toggle l'expansion d'une section
   * @param {string} sectionId - ID de la section
   */
  const toggleSection = (sectionId) => {
    const newExpanded = new Set(expandedSections);
    if (newExpanded.has(sectionId)) {
      newExpanded.delete(sectionId);
    } else {
      newExpanded.add(sectionId);
    }
    setExpandedSections(newExpanded);
  };

  /**
   * Rendu d'une leçon individuelle
   * @param {Object} lesson - Données de la leçon
   * @param {number} index - Index de la leçon
   * @returns {JSX.Element}
   */
  const renderLesson = (lesson, index) => {
    const lessonProgress = progress[courseId]?.lessons?.[lesson.id];
    const isCompleted = lessonProgress?.completed || false;
    const isCurrent = lessonProgress?.current || false;
    const timeSpent = lessonProgress?.timeSpent || 0;

    return (
      <div 
        key={lesson.id}
        className={`${styles.lesson} ${isCompleted ? styles.completed : ''} ${isCurrent ? styles.current : ''}`}
        onClick={() => handleLessonClick(lesson, index)}
      >
        <div className={styles.lessonHeader}>
          <div className={styles.lessonIcon}>
            {isCompleted ? '✓' : isCurrent ? '▶' : index + 1}
          </div>
          <div className={styles.lessonInfo}>
            <h4 className={styles.lessonTitle}>{lesson.title}</h4>
            <p className={styles.lessonDuration}>
              {formatters.formatDuration(lesson.estimatedDuration)}
              {timeSpent > 0 && ` • ${formatters.formatDuration(timeSpent)} passé`}
            </p>
          </div>
          {interactive && !isCompleted && (
            <button 
              className={styles.markCompleteBtn}
              onClick={(e) => {
                e.stopPropagation();
                handleMarkCompleted(lesson.id);
              }}
            >
              Marquer complété
            </button>
          )}
        </div>
        
        {lesson.description && variant === 'full' && (
          <p className={styles.lessonDescription}>{lesson.description}</p>
        )}
      </div>
    );
  };

  /**
   * Rendu de la section des achievements
   * @returns {JSX.Element|null}
   */
  const renderAchievements = () => {
    if (!showAchievements || !calculateStats?.achievements?.length) return null;

    return (
      <div className={styles.achievementsSection}>
        <h3 className={styles.sectionTitle}>Réussites débloquées</h3>
        <div className={styles.achievementsList}>
          {calculateStats.achievements.map(achievement => (
            <Achievement 
              key={achievement.id}
              achievement={achievement}
              size="small"
            />
          ))}
        </div>
      </div>
    );
  };

  /**
   * Rendu des statistiques de temps
   * @returns {JSX.Element|null}
   */
  const renderTimeStats = () => {
    if (!showTimeStats || !calculateStats) return null;

    return (
      <div className={styles.timeStats}>
        <div className={styles.statItem}>
          <span className={styles.statLabel}>Temps total</span>
          <span className={styles.statValue}>
            {formatters.formatDuration(calculateStats.totalTimeSpent)}
          </span>
        </div>
        <div className={styles.statItem}>
          <span className={styles.statLabel}>Temps moyen/leçon</span>
          <span className={styles.statValue}>
            {formatters.formatDuration(calculateStats.averageTimePerLesson)}
          </span>
        </div>
        <div className={styles.statItem}>
          <span className={styles.statLabel}>Dernière activité</span>
          <span className={styles.statValue}>
            {calculateStats.lastActivity 
              ? dateUtils.formatRelative(calculateStats.lastActivity)
              : 'Jamais'
            }
          </span>
        </div>
      </div>
    );
  };

  // États de chargement et d'erreur
  if (isLoading || progressLoading || courseLoading) {
    return (
      <div className={`${styles.courseProgress} ${styles.loading} ${className}`}>
        <div className={styles.loadingSpinner}>Chargement...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className={`${styles.courseProgress} ${styles.error} ${className}`}>
        <div className={styles.errorMessage}>
          <h3>Erreur</h3>
          <p>{error}</p>
        </div>
      </div>
    );
  }

  if (!course || !calculateStats) {
    return (
      <div className={`${styles.courseProgress} ${styles.noData} ${className}`}>
        <p>Aucune donnée de progression disponible</p>
      </div>
    );
  }

  return (
    <div className={`${styles.courseProgress} ${styles[variant]} ${className}`}>
      {/* En-tête avec progression globale */}
      <div className={styles.header}>
        <div className={styles.courseInfo}>
          <h2 className={styles.courseTitle}>{course.title}</h2>
          <p className={styles.progressSummary}>
            {calculateStats.completedLessons} sur {calculateStats.totalLessons} leçons complétées
          </p>
        </div>
        
        <ProgressBar 
          current={calculateStats.completedLessons}
          total={calculateStats.totalLessons}
          percentage={calculateStats.completionPercentage}
          showPercentage={true}
          size={variant === 'minimal' ? 'small' : 'medium'}
          animated={true}
        />
      </div>

      {/* Statistiques de temps */}
      {variant !== 'minimal' && renderTimeStats()}

      {/* Liste des leçons */}
      {variant === 'full' && (
        <div className={styles.lessonsSection}>
          <div className={styles.sectionHeader}>
            <h3 className={styles.sectionTitle}>Progression des leçons</h3>
            <span className={styles.lessonCount}>
              {calculateStats.remainingLessons} restantes
            </span>
          </div>
          
          <div className={styles.lessonsList}>
            {course.lessons?.map((lesson, index) => renderLesson(lesson, index))}
          </div>
        </div>
      )}

      {/* Achievements */}
      {variant === 'full' && renderAchievements()}

      {/* Streak info */}
      {variant !== 'minimal' && calculateStats.streak > 0 && (
        <div className={styles.streakInfo}>
          <span className={styles.streakIcon}>🔥</span>
          <span className={styles.streakText}>
            Série de {calculateStats.streak} jour{calculateStats.streak > 1 ? 's' : ''}
          </span>
        </div>
      )}
    </div>
  );
};

CourseProgress.propTypes = {
  courseId: PropTypes.number.isRequired,
  variant: PropTypes.oneOf(['full', 'compact', 'minimal']),
  showAchievements: PropTypes.bool,
  showTimeStats: PropTypes.bool,
  interactive: PropTypes.bool,
  onProgressUpdate: PropTypes.func,
  onLessonClick: PropTypes.func,
  className: PropTypes.string
};

export default CourseProgress;