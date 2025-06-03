// frontend/src/hooks/useProgress.js
import { useState, useEffect, useContext, useCallback } from 'react';
import { ProgressContext } from '../context/ProgressContext';
import { progressService } from '../services/progressService';
import { useNotification } from './useNotification';

/**
 * Hook personnalisé pour la gestion de la progression des cours
 * @param {string} courseId - ID du cours (optionnel)
 * @param {string} userId - ID de l'utilisateur (optionnel)
 * @returns {Object} Objet contenant l'état et les méthodes de progression
 */
export const useProgress = (courseId = null, userId = null) => {
  const progressContext = useContext(ProgressContext);
  const { showNotification } = useNotification();
  
  // État local
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [progress, setProgress] = useState(null);
  const [courseProgress, setCourseProgress] = useState({});
  const [achievements, setAchievements] = useState([]);
  const [statistics, setStatistics] = useState({
    totalCourses: 0,
    completedCourses: 0,
    totalLessons: 0,
    completedLessons: 0,
    totalTimeSpent: 0,
    streak: 0
  });

  /**
   * Récupère la progression globale de l'utilisateur
   */
  const fetchUserProgress = useCallback(async (targetUserId = userId) => {
    if (!targetUserId) return;
    
    setLoading(true);
    setError(null);
    
    try {
      const userProgress = await progressService.getUserProgress(targetUserId);
      setProgress(userProgress);
      setStatistics(userProgress.statistics || statistics);
      setAchievements(userProgress.achievements || []);
    } catch (err) {
      setError(err.message);
      showNotification('Erreur lors du chargement de la progression', 'error');
    } finally {
      setLoading(false);
    }
  }, [userId, showNotification]);

  /**
   * Récupère la progression d'un cours spécifique
   */
  const fetchCourseProgress = useCallback(async (targetCourseId = courseId, targetUserId = userId) => {
    if (!targetCourseId || !targetUserId) return;
    
    setLoading(true);
    setError(null);
    
    try {
      const progress = await progressService.getCourseProgress(targetUserId, targetCourseId);
      setCourseProgress(prev => ({
        ...prev,
        [targetCourseId]: progress
      }));
    } catch (err) {
      setError(err.message);
      showNotification('Erreur lors du chargement de la progression du cours', 'error');
    } finally {
      setLoading(false);
    }
  }, [courseId, userId, showNotification]);

  /**
   * Met à jour la progression d'une leçon
   */
  const updateLessonProgress = useCallback(async (lessonId, progressData) => {
    if (!userId || !courseId) {
      setError('ID utilisateur et cours requis');
      return false;
    }
    
    setLoading(true);
    setError(null);
    
    try {
      const updatedProgress = await progressService.updateLessonProgress(
        userId,
        courseId,
        lessonId,
        progressData
      );
      
      // Mettre à jour l'état local
      setCourseProgress(prev => ({
        ...prev,
        [courseId]: updatedProgress
      }));
      
      // Mettre à jour le contexte global si disponible
      if (progressContext?.updateProgress) {
        progressContext.updateProgress(courseId, updatedProgress);
      }
      
      showNotification('Progression mise à jour avec succès', 'success');
      return true;
    } catch (err) {
      setError(err.message);
      showNotification('Erreur lors de la mise à jour de la progression', 'error');
      return false;
    } finally {
      setLoading(false);
    }
  }, [userId, courseId, progressContext, showNotification]);

  /**
   * Marque une leçon comme terminée
   */
  const completeLesson = useCallback(async (lessonId, timeSpent = 0, score = null) => {
    return await updateLessonProgress(lessonId, {
      completed: true,
      completedAt: new Date().toISOString(),
      timeSpent,
      score
    });
  }, [updateLessonProgress]);

  /**
   * Marque un cours comme terminé
   */
  const completeCourse = useCallback(async (targetCourseId = courseId) => {
    if (!userId || !targetCourseId) {
      setError('ID utilisateur et cours requis');
      return false;
    }
    
    setLoading(true);
    setError(null);
    
    try {
      const result = await progressService.completeCourse(userId, targetCourseId);
      
      // Mettre à jour les statistiques
      setStatistics(prev => ({
        ...prev,
        completedCourses: prev.completedCourses + 1
      }));
      
      // Vérifier les nouveaux achievements
      if (result.newAchievements && result.newAchievements.length > 0) {
        setAchievements(prev => [...prev, ...result.newAchievements]);
        result.newAchievements.forEach(achievement => {
          showNotification(`Nouveau succès débloqué: ${achievement.name}!`, 'success');
        });
      }
      
      showNotification('Félicitations! Cours terminé avec succès!', 'success');
      return true;
    } catch (err) {
      setError(err.message);
      showNotification('Erreur lors de la finalisation du cours', 'error');
      return false;
    } finally {
      setLoading(false);
    }
  }, [userId, courseId, showNotification]);

  /**
   * Calcule le pourcentage de progression d'un cours
   */
  const getCourseProgressPercentage = useCallback((targetCourseId = courseId) => {
    if (!targetCourseId || !courseProgress[targetCourseId]) {
      return 0;
    }
    
    const progress = courseProgress[targetCourseId];
    const totalLessons = progress.totalLessons || 0;
    const completedLessons = progress.completedLessons || 0;
    
    return totalLessons > 0 ? Math.round((completedLessons / totalLessons) * 100) : 0;
  }, [courseId, courseProgress]);

  /**
   * Obtient la progression globale en pourcentage
   */
  const getOverallProgressPercentage = useCallback(() => {
    const { totalCourses, completedCourses } = statistics;
    return totalCourses > 0 ? Math.round((completedCourses / totalCourses) * 100) : 0;
  }, [statistics]);

  /**
   * Vérifie si une leçon est terminée
   */
  const isLessonCompleted = useCallback((lessonId, targetCourseId = courseId) => {
    if (!targetCourseId || !courseProgress[targetCourseId]) {
      return false;
    }
    
    const lessons = courseProgress[targetCourseId].lessons || {};
    return lessons[lessonId]?.completed || false;
  }, [courseId, courseProgress]);

  /**
   * Obtient les détails de progression d'une leçon
   */
  const getLessonProgress = useCallback((lessonId, targetCourseId = courseId) => {
    if (!targetCourseId || !courseProgress[targetCourseId]) {
      return null;
    }
    
    const lessons = courseProgress[targetCourseId].lessons || {};
    return lessons[lessonId] || null;
  }, [courseId, courseProgress]);

  /**
   * Réinitialise la progression d'un cours
   */
  const resetCourseProgress = useCallback(async (targetCourseId = courseId) => {
    if (!userId || !targetCourseId) {
      setError('ID utilisateur et cours requis');
      return false;
    }
    
    setLoading(true);
    setError(null);
    
    try {
      await progressService.resetCourse(userId, targetCourseId);
      
      // Supprimer de l'état local
      setCourseProgress(prev => {
        const updated = { ...prev };
        delete updated[targetCourseId];
        return updated;
      });
      
      showNotification('Progression du cours réinitialisée', 'info');
      return true;
    } catch (err) {
      setError(err.message);
      showNotification('Erreur lors de la réinitialisation', 'error');
      return false;
    } finally {
      setLoading(false);
    }
  }, [userId, courseId, showNotification]);

  // Effets
  useEffect(() => {
    if (userId) {
      fetchUserProgress();
    }
  }, [userId, fetchUserProgress]);

  useEffect(() => {
    if (courseId && userId) {
      fetchCourseProgress();
    }
  }, [courseId, userId, fetchCourseProgress]);

  return {
    // État
    loading,
    error,
    progress,
    courseProgress,
    achievements,
    statistics,
    
    // Méthodes de récupération
    fetchUserProgress,
    fetchCourseProgress,
    
    // Méthodes de mise à jour
    updateLessonProgress,
    completeLesson,
    completeCourse,
    resetCourseProgress,
    
    // Méthodes utilitaires
    getCourseProgressPercentage,
    getOverallProgressPercentage,
    isLessonCompleted,
    getLessonProgress,
    
    // Propriétés calculées
    isLoading: loading,
    hasError: !!error,
    currentCourseProgress: courseProgress[courseId] || null,
    overallProgress: getOverallProgressPercentage()
  };
};

export default useProgress;