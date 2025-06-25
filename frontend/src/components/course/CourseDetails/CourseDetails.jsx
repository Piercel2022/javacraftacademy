
import React, { useState, useEffect, useContext } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { AuthContext } from '../../../context/AuthContext';
import { CourseContext } from '../../../context/CourseContext';
import { ProgressContext } from '../../../context/ProgressContext';
import { NotificationContext } from '../../../context/NotificationContext';
import { useCourse } from '../../../hooks/useCourse';
import { useProgress } from '../../../hooks/useProgress';
import { courseService } from '../../../services/courseService';
import { progressService } from '../../../services/progressService';
import Button from '../../common/Button';
import Loading from '../../common/Loading';
import Modal from '../../common/Modal';
import CourseProgress from '../CourseProgress';
import { formatDate, formatDuration } from '../../../utils/formatters';
import { COURSE_DIFFICULTY_LEVELS, COURSE_STATUSES } from '../../../utils/constants';
import styles from './CourseDetails.module.css';

/**
 * Composant détaillé d'affichage des informations complètes d'un cours
 * 
 * @component
 * @description
 * Ce composant affiche toutes les informations détaillées d'un cours spécifique :
 * - Informations générales (titre, description, niveau, durée)
 * - Curriculum détaillé avec chapitres et leçons
 * - Prérequis et objectifs d'apprentissage
 * - Progression de l'utilisateur
 * - Actions d'inscription/désinscription
 * - Évaluation et commentaires
 * 
 * @features
 * - Affichage responsive des détails du cours
 * - Gestion de l'inscription/désinscription
 * - Suivi de progression en temps réel
 * - Navigation vers les leçons
 * - Système d'évaluation par étoiles
 * - Gestion des prérequis
 * - Preview des leçons
 * 
 * @relationships
 * - AuthContext : Gestion de l'authentification utilisateur
 * - CourseContext : État global des cours
 * - ProgressContext : Suivi de progression
 * - NotificationContext : Affichage des notifications
 * - courseService : API des cours
 * - progressService : API de progression
 * - CourseProgress : Composant de progression
 * - Button, Loading, Modal : Composants communs
 * 
 * @example
 * ```jsx
 * // Utilisation dans une route
 * <Route path="/course/:courseId" component={CourseDetails} />
 * 
 * // Navigation programmatique
 * navigate(`/course/${courseId}`);
 * ```
 * 
 * @param {Object} props - Propriétés du composant
 * @returns {JSX.Element} Interface détaillée du cours
 */
const CourseDetails = () => {
  const { courseId } = useParams();
  const navigate = useNavigate();
  
  // Contexts
  const { user, isAuthenticated } = useContext(AuthContext);
  const { courses, updateCourseEnrollment } = useContext(CourseContext);
  const { userProgress, updateProgress } = useContext(ProgressContext);
  const { showNotification } = useContext(NotificationContext);
  
  // Custom hooks
  const { 
    course, 
    loading: courseLoading, 
    error: courseError,
    fetchCourse,
    enrollInCourse,
    unenrollFromCourse 
  } = useCourse();
  
  const { 
    progress, 
    loading: progressLoading,
    calculateCompletionPercentage,
    getNextLesson
  } = useProgress();
  
  // État local
  const [showEnrollModal, setShowEnrollModal] = useState(false);
  const [showUnenrollModal, setShowUnenrollModal] = useState(false);
  const [selectedChapter, setSelectedChapter] = useState(null);
  const [userRating, setUserRating] = useState(0);
  const [userReview, setUserReview] = useState('');
  const [showReviewModal, setShowReviewModal] = useState(false);
  const [expandedChapters, setExpandedChapters] = useState(new Set());
  
  // État dérivé
  const isEnrolled = course?.enrolledStudents?.includes(user?.id);
  const canEnroll = isAuthenticated && !isEnrolled;
  const completionPercentage = calculateCompletionPercentage(courseId);
  const nextLesson = getNextLesson(courseId);
  const hasPrerequisites = course?.prerequisites?.length > 0;
  const meetsPrerequisites = hasPrerequisites ? 
    course.prerequisites.every(prereq => 
      userProgress?.completedCourses?.includes(prereq.id)
    ) : true;

  /**
   * Effet pour charger les données du cours au montage
   */
  useEffect(() => {
    if (courseId) {
      loadCourseDetails();
    }
  }, [courseId]);

  /**
   * Effet pour charger la progression utilisateur
   */
  useEffect(() => {
    if (isAuthenticated && courseId && isEnrolled) {
      loadUserProgress();
    }
  }, [isAuthenticated, courseId, isEnrolled]);

  /**
   * Charge les détails complets du cours
   * @async
   * @function
   */
  const loadCourseDetails = async () => {
    try {
      await fetchCourse(courseId);
    } catch (error) {
      showNotification('Erreur lors du chargement du cours', 'error');
    }
  };

  /**
   * Charge la progression utilisateur pour ce cours
   * @async
   * @function
   */
  const loadUserProgress = async () => {
    try {
      const progressData = await progressService.getUserProgress(user.id, courseId);
      updateProgress(courseId, progressData);
    } catch (error) {
      console.error('Erreur lors du chargement de la progression:', error);
    }
  };

  /**
   * Gère l'inscription au cours
   * @async
   * @function
   */
  const handleEnrollment = async () => {
    if (!isAuthenticated) {
      showNotification('Vous devez être connecté pour vous inscrire', 'warning');
      navigate('/login');
      return;
    }

    if (!meetsPrerequisites) {
      showNotification('Vous ne remplissez pas les prérequis pour ce cours', 'warning');
      return;
    }

    try {
      await enrollInCourse(courseId);
      updateCourseEnrollment(courseId, true);
      showNotification('Inscription réussie !', 'success');
      setShowEnrollModal(false);
    } catch (error) {
      showNotification('Erreur lors de l\'inscription', 'error');
    }
  };

  /**
   * Gère la désinscription du cours
   * @async
   * @function
   */
  const handleUnenrollment = async () => {
    try {
      await unenrollFromCourse(courseId);
      updateCourseEnrollment(courseId, false);
      showNotification('Désinscription réussie', 'success');
      setShowUnenrollModal(false);
    } catch (error) {
      showNotification('Erreur lors de la désinscription', 'error');
    }
  };

  /**
   * Démarre le cours ou continue depuis la dernière leçon
   * @function
   */
  const handleStartCourse = () => {
    if (nextLesson) {
      navigate(`/lesson/${nextLesson.id}`);
    } else if (course?.chapters?.length > 0) {
      const firstLesson = course.chapters[0]?.lessons?.[0];
      if (firstLesson) {
        navigate(`/lesson/${firstLesson.id}`);
      }
    }
  };

  /**
   * Navigue vers une leçon spécifique
   * @param {string} lessonId - ID de la leçon
   * @function
   */
  const handleLessonClick = (lessonId) => {
    navigate(`/lesson/${lessonId}`);
  };

  /**
   * Bascule l'expansion d'un chapitre
   * @param {string} chapterId - ID du chapitre
   * @function
   */
  const toggleChapter = (chapterId) => {
    const newExpanded = new Set(expandedChapters);
    if (newExpanded.has(chapterId)) {
      newExpanded.delete(chapterId);
    } else {
      newExpanded.add(chapterId);
    }
    setExpandedChapters(newExpanded);
  };

  /**
   * Soumet une évaluation du cours
   * @async
   * @function
   */
  const handleSubmitReview = async () => {
    try {
      await courseService.submitReview(courseId, {
        rating: userRating,
        review: userReview,
        userId: user.id
      });
      showNotification('Évaluation soumise avec succès', 'success');
      setShowReviewModal(false);
      setUserRating(0);
      setUserReview('');
      loadCourseDetails(); // Recharger pour afficher la nouvelle évaluation
    } catch (error) {
      showNotification('Erreur lors de la soumission', 'error');
    }
  };

  /**
   * Rendu du composant d'évaluation par étoiles
   * @param {number} rating - Note actuelle
   * @param {boolean} interactive - Si l'évaluation est interactive
   * @returns {JSX.Element}
   */
  const renderStarRating = (rating, interactive = false) => (
    <div className={styles.starRating}>
      {[1, 2, 3, 4, 5].map((star) => (
        <span
          key={star}
          className={`${styles.star} ${star <= rating ? styles.filled : ''} ${
            interactive ? styles.interactive : ''
          }`}
          onClick={interactive ? () => setUserRating(star) : undefined}
        >
          ★
        </span>
      ))}
    </div>
  );

  /**
   * Rendu du curriculum du cours
   * @returns {JSX.Element}
   */
  const renderCurriculum = () => (
    <div className={styles.curriculum}>
      <h3>Curriculum</h3>
      {course.chapters?.map((chapter) => (
        <div key={chapter.id} className={styles.chapter}>
          <div 
            className={styles.chapterHeader}
            onClick={() => toggleChapter(chapter.id)}
          >
            <h4>{chapter.title}</h4>
            <span className={styles.chapterMeta}>
              {chapter.lessons?.length} leçons • {formatDuration(chapter.totalDuration)}
            </span>
            <button className={styles.toggleButton}>
              {expandedChapters.has(chapter.id) ? '▼' : '▶'}
            </button>
          </div>
          
          {expandedChapters.has(chapter.id) && (
            <div className={styles.lessons}>
              {chapter.lessons?.map((lesson) => {
                const isCompleted = progress?.completedLessons?.includes(lesson.id);
                const isLocked = lesson.requiresPrevious && 
                  !progress?.completedLessons?.includes(lesson.previousLessonId);
                
                return (
                  <div 
                    key={lesson.id} 
                    className={`${styles.lesson} ${isCompleted ? styles.completed : ''} ${
                      isLocked ? styles.locked : ''
                    }`}
                    onClick={() => !isLocked && handleLessonClick(lesson.id)}
                  >
                    <div className={styles.lessonIcon}>
                      {isCompleted ? '✓' : isLocked ? '🔒' : '▶'}
                    </div>
                    <div className={styles.lessonContent}>
                      <h5>{lesson.title}</h5>
                      <span className={styles.lessonDuration}>
                        {formatDuration(lesson.duration)}
                      </span>
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </div>
      ))}
    </div>
  );

  // Gestion des états de chargement et d'erreur
  if (courseLoading || progressLoading) {
    return <Loading message="Chargement des détails du cours..." />;
  }

  if (courseError) {
    return (
      <div className={styles.error}>
        <h2>Erreur lors du chargement</h2>
        <p>{courseError}</p>
        <Button onClick={() => navigate('/courses')}>
          Retour aux cours
        </Button>
      </div>
    );
  }

  if (!course) {
    return (
      <div className={styles.notFound}>
        <h2>Cours non trouvé</h2>
        <Button onClick={() => navigate('/courses')}>
          Retour aux cours
        </Button>
      </div>
    );
  }

  return (
    <div className={styles.courseDetails}>
      {/* En-tête du cours */}
      <div className={styles.header}>
        <div className={styles.headerContent}>
          <div className={styles.courseInfo}>
            <h1>{course.title}</h1>
            <p className={styles.description}>{course.description}</p>
            
            <div className={styles.metadata}>
              <span className={`${styles.difficulty} ${styles[course.difficulty]}`}>
                {COURSE_DIFFICULTY_LEVELS[course.difficulty]}
              </span>
              <span className={styles.duration}>
                {formatDuration(course.totalDuration)}
              </span>
              <span className={styles.students}>
                {course.enrolledStudents?.length || 0} étudiants
              </span>
              <div className={styles.rating}>
                {renderStarRating(course.averageRating)}
                <span>({course.reviewCount} avis)</span>
              </div>
            </div>
          </div>
          
          <div className={styles.courseImage}>
            <img 
              src={course.thumbnail || '/default-course-thumbnail.jpg'} 
              alt={course.title}
            />
          </div>
        </div>
      </div>

      <div className={styles.content}>
        <div className={styles.mainContent}>
          {/* Progression si inscrit */}
          {isEnrolled && (
            <div className={styles.progressSection}>
              <CourseProgress 
                courseId={courseId}
                progress={progress}
                completionPercentage={completionPercentage}
              />
              {nextLesson && (
                <Button 
                  onClick={handleStartCourse}
                  variant="primary"
                  className={styles.continueButton}
                >
                  Continuer: {nextLesson.title}
                </Button>
              )}
            </div>
          )}

          {/* Objectifs d'apprentissage */}
          {course.learningObjectives?.length > 0 && (
            <div className={styles.objectives}>
              <h3>Ce que vous apprendrez</h3>
              <ul>
                {course.learningObjectives.map((objective, index) => (
                  <li key={index}>{objective}</li>
                ))}
              </ul>
            </div>
          )}

          {/* Prérequis */}
          {hasPrerequisites && (
            <div className={styles.prerequisites}>
              <h3>Prérequis</h3>
              <ul>
                {course.prerequisites.map((prereq) => (
                  <li 
                    key={prereq.id}
                    className={userProgress?.completedCourses?.includes(prereq.id) ? 
                      styles.completed : styles.incomplete}
                  >
                    {prereq.title}
                    {userProgress?.completedCourses?.includes(prereq.id) ? ' ✓' : ' ✗'}
                  </li>
                ))}
              </ul>
            </div>
          )}

          {/* Curriculum */}
          {renderCurriculum()}

          {/* Évaluations */}
          {course.reviews?.length > 0 && (
            <div className={styles.reviews}>
              <h3>Évaluations des étudiants</h3>
              {course.reviews.slice(0, 5).map((review) => (
                <div key={review.id} className={styles.review}>
                  <div className={styles.reviewHeader}>
                    <span className={styles.reviewerName}>{review.userName}</span>
                    {renderStarRating(review.rating)}
                    <span className={styles.reviewDate}>
                      {formatDate(review.createdAt)}
                    </span>
                  </div>
                  <p className={styles.reviewContent}>{review.content}</p>
                </div>
              ))}
            </div>
          )}
        </div>

        <div className={styles.sidebar}>
          {/* Actions d'inscription */}
          <div className={styles.enrollmentCard}>
            {!isAuthenticated ? (
              <Button 
                onClick={() => navigate('/login')}
                variant="primary"
                fullWidth
              >
                Se connecter pour s'inscrire
              </Button>
            ) : isEnrolled ? (
              <div className={styles.enrolledActions}>
                <Button 
                  onClick={handleStartCourse}
                  variant="primary"
                  fullWidth
                  disabled={!meetsPrerequisites}
                >
                  {completionPercentage > 0 ? 'Continuer le cours' : 'Commencer le cours'}
                </Button>
                <Button 
                  onClick={() => setShowUnenrollModal(true)}
                  variant="secondary"
                  fullWidth
                >
                  Se désinscrire
                </Button>
                <Button 
                  onClick={() => setShowReviewModal(true)}
                  variant="outline"
                  fullWidth
                >
                  Évaluer le cours
                </Button>
              </div>
            ) : (
              <Button 
                onClick={() => setShowEnrollModal(true)}
                variant="primary"
                fullWidth
                disabled={!meetsPrerequisites}
              >
                {meetsPrerequisites ? 'S\'inscrire au cours' : 'Prérequis requis'}
              </Button>
            )}
          </div>

          {/* Informations sur l'instructeur */}
          {course.instructor && (
            <div className={styles.instructorCard}>
              <h4>Instructeur</h4>
              <div className={styles.instructor}>
                <img 
                  src={course.instructor.avatar || '/default-avatar.jpg'} 
                  alt={course.instructor.name}
                  className={styles.instructorAvatar}
                />
                <div>
                  <h5>{course.instructor.name}</h5>
                  <p>{course.instructor.title}</p>
                  <p className={styles.instructorBio}>
                    {course.instructor.bio}
                  </p>
                </div>
              </div>
            </div>
          )}

          {/* Détails du cours */}
          <div className={styles.courseDetailsCard}>
            <h4>Détails du cours</h4>
            <div className={styles.detailsList}>
              <div className={styles.detail}>
                <span>Niveau:</span>
                <span>{COURSE_DIFFICULTY_LEVELS[course.difficulty]}</span>
              </div>
              <div className={styles.detail}>
                <span>Durée totale:</span>
                <span>{formatDuration(course.totalDuration)}</span>
              </div>
              <div className={styles.detail}>
                <span>Chapitres:</span>
                <span>{course.chapters?.length || 0}</span>
              </div>
              <div className={styles.detail}>
                <span>Leçons:</span>
                <span>{course.totalLessons || 0}</span>
              </div>
              <div className={styles.detail}>
                <span>Certificat:</span>
                <span>{course.certificateEligible ? 'Oui' : 'Non'}</span>
              </div>
              <div className={styles.detail}>
                <span>Dernière mise à jour:</span>
                <span>{formatDate(course.lastUpdated)}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Modales */}
      <Modal
        isOpen={showEnrollModal}
        onClose={() => setShowEnrollModal(false)}
        title="Confirmer l'inscription"
      >
        <p>Êtes-vous sûr de vouloir vous inscrire à ce cours ?</p>
        <div className={styles.modalActions}>
          <Button onClick={() => setShowEnrollModal(false)} variant="secondary">
            Annuler
          </Button>
          <Button onClick={handleEnrollment} variant="primary">
            S'inscrire
          </Button>
        </div>
      </Modal>

      <Modal
        isOpen={showUnenrollModal}
        onClose={() => setShowUnenrollModal(false)}
        title="Confirmer la désinscription"
      >
        <p>Êtes-vous sûr de vouloir vous désinscrire de ce cours ? Votre progression sera conservée.</p>
        <div className={styles.modalActions}>
          <Button onClick={() => setShowUnenrollModal(false)} variant="secondary">
            Annuler
          </Button>
          <Button onClick={handleUnenrollment} variant="danger">
            Se désinscrire
          </Button>
        </div>
      </Modal>

      <Modal
        isOpen={showReviewModal}
        onClose={() => setShowReviewModal(false)}
        title="Évaluer le cours"
      >
        <div className={styles.reviewForm}>
          <div className={styles.ratingSection}>
            <label>Note:</label>
            {renderStarRating(userRating, true)}
          </div>
          <div className={styles.reviewSection}>
            <label>Commentaire:</label>
            <textarea
              value={userReview}
              onChange={(e) => setUserReview(e.target.value)}
              placeholder="Partagez votre expérience avec ce cours..."
              rows={4}
            />
          </div>
          <div className={styles.modalActions}>
            <Button onClick={() => setShowReviewModal(false)} variant="secondary">
              Annuler
            </Button>
            <Button 
              onClick={handleSubmitReview} 
              variant="primary"
              disabled={userRating === 0}
            >
              Soumettre
            </Button>
          </div>
        </div>
      </Modal>
    </div>
  );
};

export default CourseDetails;