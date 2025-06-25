import { useState, useEffect, useCallback, useContext } from 'react';
import { CourseContext } from '../context/CourseContext';
import courseService  from '../services/courseService';
import { useNotification } from './useNotification';
import { useAuth } from './useAuth';

/**
 * Hook personnalisé pour la gestion des cours
 * Gère le catalogue, les inscriptions, les leçons et la progression
 */
export const useCourse = (courseId = null) => {
  const context = useContext(CourseContext);
  const { showNotification } = useNotification();
  const { isAuthenticated, user } = useAuth();
  
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [currentCourse, setCurrentCourse] = useState(null);
  const [currentLesson, setCurrentLesson] = useState(null);
  const [lessons, setLessons] = useState([]);

  if (!context) {
    throw new Error('useCourse must be used within a CourseProvider');
  }

  const {
    courses,
    enrolledCourses,
    categories,
    setCourses,
    setEnrolledCourses,
    setCategories
  } = context;

  // Récupérer tous les cours
  const fetchCourses = useCallback(async (filters = {}) => {
    setLoading(true);
    setError(null);
    
    try {
      const response = await courseService.getAllCourses(filters);
      
      if (response.success) {
        setCourses(response.courses);
        return { success: true, courses: response.courses };
      } else {
        const errorMsg = response.message || 'Erreur lors du chargement des cours';
        setError(errorMsg);
        return { success: false, error: errorMsg };
      }
    } catch (err) {
      const errorMsg = err.response?.data?.message || 'Erreur lors du chargement des cours';
      setError(errorMsg);
      return { success: false, error: errorMsg };
    } finally {
      setLoading(false);
    }
  }, [setCourses]);

  // Récupérer les cours inscrits
  const fetchEnrolledCourses = useCallback(async () => {
    if (!isAuthenticated) return { success: false, error: 'Non authentifié' };
    
    setLoading(true);
    setError(null);
    
    try {
      const response = await courseService.getEnrolledCourses();
      
      if (response.success) {
        setEnrolledCourses(response.courses);
        return { success: true, courses: response.courses };
      } else {
        const errorMsg = response.message || 'Erreur lors du chargement des cours inscrits';
        setError(errorMsg);
        return { success: false, error: errorMsg };
      }
    } catch (err) {
      const errorMsg = err.response?.data?.message || 'Erreur lors du chargement des cours inscrits';
      setError(errorMsg);
      return { success: false, error: errorMsg };
    } finally {
      setLoading(false);
    }
  }, [isAuthenticated, setEnrolledCourses]);

  // Récupérer les catégories
  const fetchCategories = useCallback(async () => {
    setLoading(true);
    setError(null);
    
    try {
      const response = await courseService.getCategories();
      
      if (response.success) {
        setCategories(response.categories);
        return { success: true, categories: response.categories };
      } else {
        const errorMsg = response.message || 'Erreur lors du chargement des catégories';
        setError(errorMsg);
        return { success: false, error: errorMsg };
      }
    } catch (err) {
      const errorMsg = err.response?.data?.message || 'Erreur lors du chargement des catégories';
      setError(errorMsg);
      return { success: false, error: errorMsg };
    } finally {
      setLoading(false);
    }
  }, [setCategories]);

  // Récupérer un cours spécifique
  const fetchCourse = useCallback(async (id) => {
    setLoading(true);
    setError(null);
    
    try {
      const response = await courseService.getCourseById(id);
      
      if (response.success) {
        setCurrentCourse(response.course);
        return { success: true, course: response.course };
      } else {
        const errorMsg = response.message || 'Cours non trouvé';
        setError(errorMsg);
        return { success: false, error: errorMsg };
      }
    } catch (err) {
      const errorMsg = err.response?.data?.message || 'Erreur lors du chargement du cours';
      setError(errorMsg);
      return { success: false, error: errorMsg };
    } finally {
      setLoading(false);
    }
  }, []);

  // Récupérer les leçons d'un cours
  const fetchLessons = useCallback(async (courseId) => {
    setLoading(true);
    setError(null);
    
    try {
      const response = await courseService.getCourseLessons(courseId);
      
      if (response.success) {
        setLessons(response.lessons);
        return { success: true, lessons: response.lessons };
      } else {
        const errorMsg = response.message || 'Erreur lors du chargement des leçons';
        setError(errorMsg);
        return { success: false, error: errorMsg };
      }
    } catch (err) {
      const errorMsg = err.response?.data?.message || 'Erreur lors du chargement des leçons';
      setError(errorMsg);
      return { success: false, error: errorMsg };
    } finally {
      setLoading(false);
    }
  }, []);

  // Récupérer une leçon spécifique
  const fetchLesson = useCallback(async (courseId, lessonId) => {
    setLoading(true);
    setError(null);
    
    try {
      const response = await courseService.getLesson(courseId, lessonId);
      
      if (response.success) {
        setCurrentLesson(response.lesson);
        return { success: true, lesson: response.lesson };
      } else {
        const errorMsg = response.message || 'Leçon non trouvée';
        setError(errorMsg);
        return { success: false, error: errorMsg };
      }
    } catch (err) {
      const errorMsg = err.response?.data?.message || 'Erreur lors du chargement de la leçon';
      setError(errorMsg);
      return { success: false, error: errorMsg };
    } finally {
      setLoading(false);
    }
  }, []);

  // S'inscrire à un cours
  const enrollCourse = useCallback(async (courseId) => {
    if (!isAuthenticated) {
      showNotification('Vous devez être connecté pour vous inscrire', 'warning');
      return { success: false, error: 'Non authentifié' };
    }
    
    setLoading(true);
    setError(null);
    
    try {
      const response = await courseService.enrollCourse(courseId);
      
      if (response.success) {
        // Mettre à jour la liste des cours inscrits
        await fetchEnrolledCourses();
        showNotification('Inscription réussie !', 'success');
        return { success: true };
      } else {
        const errorMsg = response.message || 'Erreur lors de l\'inscription';
        setError(errorMsg);
        showNotification(errorMsg, 'error');
        return { success: false, error: errorMsg };
      }
    } catch (err) {
      const errorMsg = err.response?.data?.message || 'Erreur lors de l\'inscription';
      setError(errorMsg);
      showNotification(errorMsg, 'error');
      return { success: false, error: errorMsg };
    } finally {
      setLoading(false);
    }
  }, [isAuthenticated, showNotification, fetchEnrolledCourses]);

  // Se désinscrire d'un cours
  const unenrollCourse = useCallback(async (courseId) => {
    if (!isAuthenticated) return { success: false, error: 'Non authentifié' };
    
    setLoading(true);
    setError(null);
    
    try {
      const response = await courseService.unenrollCourse(courseId);
      
      if (response.success) {
        // Mettre à jour la liste des cours inscrits
        await fetchEnrolledCourses();
        showNotification('Désinscription réussie', 'info');
        return { success: true };
      } else {
        const errorMsg = response.message || 'Erreur lors de la désinscription';
        setError(errorMsg);
        showNotification(errorMsg, 'error');
        return { success: false, error: errorMsg };
      }
    } catch (err) {
      const errorMsg = err.response?.data?.message || 'Erreur lors de la désinscription';
      setError(errorMsg);
      showNotification(errorMsg, 'error');
      return { success: false, error: errorMsg };
    } finally {
      setLoading(false);
    }
  }, [isAuthenticated, showNotification, fetchEnrolledCourses]);

  // Marquer une leçon comme terminée
  const completeLesson = useCallback(async (courseId, lessonId) => {
    if (!isAuthenticated) return { success: false, error: 'Non authentifié' };
    
    setLoading(true);
    setError(null);
    
    try {
      const response = await courseService.completeLesson(courseId, lessonId);
      
      if (response.success) {
        showNotification('Leçon terminée !', 'success');
        return { success: true, progress: response.progress };
      } else {
        const errorMsg = response.message || 'Erreur lors de la validation';
        setError(errorMsg);
        return { success: false, error: errorMsg };
      }
    } catch (err) {
      const errorMsg = err.response?.data?.message || 'Erreur lors de la validation';
      setError(errorMsg);
      return { success: false, error: errorMsg };
    } finally {
      setLoading(false);
    }
  }, [isAuthenticated, showNotification]);

  // Rechercher des cours
  const searchCourses = useCallback(async (query, filters = {}) => {
    setLoading(true);
    setError(null);
    
    try {
      const response = await courseService.searchCourses(query, filters);
      
      if (response.success) {
        return { success: true, courses: response.courses };
      } else {
        const errorMsg = response.message || 'Erreur lors de la recherche';
        setError(errorMsg);
        return { success: false, error: errorMsg };
      }
    } catch (err) {
      const errorMsg = err.response?.data?.message || 'Erreur lors de la recherche';
      setError(errorMsg);
      return { success: false, error: errorMsg };
    } finally {
      setLoading(false);
    }
  }, []);

  // Obtenir les cours recommandés
  const getRecommendedCourses = useCallback(async () => {
    if (!isAuthenticated) return { success: false, courses: [] };
    
    setLoading(true);
    setError(null);
    
    try {
      const response = await courseService.getRecommendedCourses();
      
      if (response.success) {
        return { success: true, courses: response.courses };
      } else {
        return { success: false, courses: [], error: response.message };
      }
    } catch (err) {
      return { success: false, courses: [], error: 'Erreur lors du chargement des recommandations' };
    } finally {
      setLoading(false);
    }
  }, [isAuthenticated]);

  // Vérifier si l'utilisateur est inscrit à un cours
  const isEnrolled = useCallback((courseId) => {
    return enrolledCourses.some(course => course.id === courseId);
  }, [enrolledCourses]);

  // Obtenir la progression d'un cours
  const getCourseProgress = useCallback((courseId) => {
    const enrolledCourse = enrolledCourses.find(course => course.id === courseId);
    return enrolledCourse?.progress || 0;
  }, [enrolledCourses]);

  // Effacer les erreurs
  const clearError = useCallback(() => {
    setError(null);
  }, []);

  // Effet pour charger un cours spécifique si courseId est fourni
  useEffect(() => {
    if (courseId && courseId !== currentCourse?.id) {
      fetchCourse(courseId);
      fetchLessons(courseId);
    }
  }, [courseId, currentCourse?.id, fetchCourse, fetchLessons]);

  // Charger les cours inscrits à la connexion
  useEffect(() => {
    if (isAuthenticated && user) {
      fetchEnrolledCourses();
    }
  }, [isAuthenticated, user, fetchEnrolledCourses]);

  return {
    // État
    courses,
    enrolledCourses,
    categories,
    currentCourse,
    currentLesson,
    lessons,
    loading,
    error,
    
    // Actions
    fetchCourses,
    fetchEnrolledCourses,
    fetchCategories,
    fetchCourse,
    fetchLessons,
    fetchLesson,
    enrollCourse,
    unenrollCourse,
    completeLesson,
    searchCourses,
    getRecommendedCourses,
    
    // Utilitaires
    isEnrolled,
    getCourseProgress,
    clearError
  };
};