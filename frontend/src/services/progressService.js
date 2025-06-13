
// services/progressService.js
import { apiCall, endpoints } from './api';

class ProgressService {
  // Obtenir la progression globale de l'utilisateur
  async getOverallProgress() {
    try {
      const response = await apiCall.get(endpoints.progress.overall);
      return {
        success: true,
        data: response.data,
        message: 'Progression globale récupérée'
      };
    } catch (error) {
      return {
        success: false,
        error: error.response?.data?.message || 'Erreur lors de la récupération de la progression',
        status: error.response?.status
      };
    }
  }

  // Obtenir la progression de l'utilisateur (alias pour getOverallProgress)

  // Obtenir la progression d'un cours spécifique
  async getCourseProgress(courseId) {
    try {
      const response = await apiCall.get(endpoints.progress.course(courseId));
      return {
        success: true,
        data: response.data,
        message: 'Progression du cours récupérée'
      };
    } catch (error) {
      return {
        success: false,
        error: error.response?.data?.message || 'Erreur lors de la récupération de la progression du cours',
        status: error.response?.status
      };
    }
  }

  // Obtenir la progression d'une leçon spécifique
  async getLessonProgress(lessonId) {
    try {
      const response = await apiCall.get(endpoints.progress.lesson(lessonId));
      return {
        success: true,
        data: response.data,
        message: 'Progression de la leçon récupérée'
      };
    } catch (error) {
      return {
        success: false,
        error: error.response?.data?.message || 'Erreur lors de la récupération de la progression de la leçon',
        status: error.response?.status
      };
    }
  }

  // Mettre à jour la progression
  async updateProgress(progressData) {
    try {
      const response = await apiCall.post(endpoints.progress.update, progressData);
      return {
        success: true,
        data: response.data,
        message: 'Progression mise à jour'
      };
    } catch (error) {
      return {
        success: false,
        error: error.response?.data?.message || 'Erreur lors de la mise à jour de la progression',
        status: error.response?.status
      };
    }
  }

  // Marquer une leçon comme commencée
  async startLesson(lessonId, courseId) {
    try {
      const response = await apiCall.post(endpoints.progress.update, {
        lessonId,
        courseId,
        status: 'started',
        startedAt: new Date().toISOString()
      });
      return {
        success: true,
        data: response.data,
        message: 'Leçon commencée'
      };
    } catch (error) {
      return {
        success: false,
        error: error.response?.data?.message || 'Erreur lors du démarrage de la leçon',
        status: error.response?.status
      };
    }
  }

  // Marquer une leçon comme terminée
  async completeLesson(lessonId, courseId, timeSpent = 0) {
    try {
      const response = await apiCall.post(endpoints.progress.update, {
        lessonId,
        courseId,
        status: 'completed',
        completedAt: new Date().toISOString(),
        timeSpent
      });
      return {
        success: true,
        data: response.data,
        message: 'Leçon terminée'
      };
    } catch (error) {
      return {
        success: false,
        error: error.response?.data?.message || 'Erreur lors de la completion de la leçon',
        status: error.response?.status
      };
    }
  }

  // Enregistrer le temps passé sur une leçon
  async updateTimeSpent(lessonId, timeSpent) {
    try {
      const response = await apiCall.patch(endpoints.progress.lesson(lessonId), {
        timeSpent
      });
      return {
        success: true,
        data: response.data,
        message: 'Temps mis à jour'
      };
    } catch (error) {
      return {
        success: false,
        error: error.response?.data?.message || 'Erreur lors de la mise à jour du temps',
        status: error.response?.status
      };
    }
  }

  // Obtenir les statistiques de progression
  async getProgressStats() {
    try {
      const response = await apiCall.get(endpoints.progress.stats);
      return {
        success: true,
        data: response.data,
        message: 'Statistiques récupérées'
      };
    } catch (error) {
      return {
        success: false,
        error: error.response?.data?.message || 'Erreur lors de la récupération des statistiques',
        status: error.response?.status
      };
    }
  }

  // Obtenir les achievements/réalisations
  async getAchievements() {
    try {
      const response = await apiCall.get(endpoints.progress.achievements);
      return {
        success: true,
        data: response.data,
        message: 'Réalisations récupérées'
      };
    } catch (error) {
      return {
        success: false,
        error: error.response?.data?.message || 'Erreur lors de la récupération des réalisations',
        status: error.response?.status
      };
    }
  }

  // Débloquer un achievement
  async unlockAchievement(achievementId) {
    try {
      const response = await apiCall.post(endpoints.progress.unlockAchievement, {
        achievementId,
        unlockedAt: new Date().toISOString()
      });
      return {
        success: true,
        data: response.data,
        message: 'Achievement débloqué'
      };
    } catch (error) {
      return {
        success: false,
        error: error.response?.data?.message || 'Erreur lors du déblocage de l\'achievement',
        status: error.response?.status
      };
    }
  }

  // Obtenir le parcours d'apprentissage
  async getLearningPath() {
    try {
      const response = await apiCall.get(endpoints.progress.learningPath);
      return {
        success: true,
        data: response.data,
        message: 'Parcours d\'apprentissage récupéré'
      };
    } catch (error) {
      return {
        success: false,
        error: error.response?.data?.message || 'Erreur lors de la récupération du parcours',
        status: error.response?.status
      };
    }
  }

  // Marquer un cours comme commencé
  async startCourse(courseId) {
    try {
      const response = await apiCall.post(endpoints.progress.update, {
        courseId,
        status: 'started',
        startedAt: new Date().toISOString()
      });
      return {
        success: true,
        data: response.data,
        message: 'Cours commencé'
      };
    } catch (error) {
      return {
        success: false,
        error: error.response?.data?.message || 'Erreur lors du démarrage du cours',
        status: error.response?.status
      };
    }
  }

  // Marquer un cours comme terminé
  async completeCourse(courseId) {
    try {
      const response = await apiCall.post(endpoints.progress.update, {
        courseId,
        status: 'completed',
        completedAt: new Date().toISOString()
      });
      return {
        success: true,
        data: response.data,
        message: 'Cours terminé'
      };
    } catch (error) {
      return {
        success: false,
        error: error.response?.data?.message || 'Erreur lors de la completion du cours',
        status: error.response?.status
      };
    }
  }

  // Obtenir la progression des exercices
  async getExerciseProgress(exerciseId) {
    try {
      const response = await apiCall.get(endpoints.progress.exercise(exerciseId));
      return {
        success: true,
        data: response.data,
        message: 'Progression de l\'exercice récupérée'
      };
    } catch (error) {
      return {
        success: false,
        error: error.response?.data?.message || 'Erreur lors de la récupération de la progression de l\'exercice',
        status: error.response?.status
      };
    }
  }

  // Soumettre un exercice
  async submitExercise(exerciseId, submission) {
    try {
      const response = await apiCall.post(endpoints.progress.submitExercise, {
        exerciseId,
        submission,
        submittedAt: new Date().toISOString()
      });
      return {
        success: true,
        data: response.data,
        message: 'Exercice soumis'
      };
    } catch (error) {
      return {
        success: false,
        error: error.response?.data?.message || 'Erreur lors de la soumission de l\'exercice',
        status: error.response?.status
      };
    }
  }

  // Calculer le pourcentage de progression d'un cours
  calculateCoursePercentage(progress) {
    if (!progress || !progress.lessons) return 0;
    
    const totalLessons = progress.lessons.length;
    const completedLessons = progress.lessons.filter(lesson => 
      lesson.status === 'completed'
    ).length;
    
    return totalLessons > 0 ? Math.round((completedLessons / totalLessons) * 100) : 0;
  }

  // Calculer le temps total passé
  calculateTotalTimeSpent(progress) {
    if (!progress || !progress.lessons) return 0;
    
    return progress.lessons.reduce((total, lesson) => {
      return total + (lesson.timeSpent || 0);
    }, 0);
  }

  // Vérifier si une leçon est débloquée
  isLessonUnlocked(lessonIndex, courseProgress) {
    if (lessonIndex === 0) return true; // Première leçon toujours débloquée
    
    if (!courseProgress || !courseProgress.lessons) return false;
    
    const previousLesson = courseProgress.lessons[lessonIndex - 1];
    return previousLesson && previousLesson.status === 'completed';
  }

  // Obtenir la prochaine leçon recommandée
  getNextRecommendedLesson(courseProgress) {
    if (!courseProgress || !courseProgress.lessons) return null;
    
    return courseProgress.lessons.find(lesson => 
      lesson.status === 'not_started' || lesson.status === 'started'
    );
  }
}

export default new ProgressService();