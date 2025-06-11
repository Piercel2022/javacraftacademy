// src/services/compilerService.js
// services/courseService.js
import { apiCall, endpoints } from './api';

class CourseService {
  // Obtenir la liste de tous les cours
  async getAllCourses(params = {}) {
    try {
      const response = await apiCall.get(endpoints.courses.list, { params });
      return {
        success: true,
        data: response.data,
        message: 'Cours récupérés avec succès'
      };
    } catch (error) {
      return {
        success: false,
        error: error.response?.data?.message || 'Erreur lors de la récupération des cours',
        status: error.response?.status
      };
    }
  }

  // Obtenir les détails d'un cours
  async getCourseById(courseId) {
    try {
      const response = await apiCall.get(endpoints.courses.detail(courseId));
      return {
        success: true,
        data: response.data,
        message: 'Détails du cours récupérés'
      };
    } catch (error) {
      return {
        success: false,
        error: error.response?.data?.message || 'Erreur lors de la récupération du cours',
        status: error.response?.status
      };
    }
  }

  // Obtenir les cours en vedette
  async getFeaturedCourses() {
    try {
      const response = await apiCall.get(endpoints.courses.featured);
      return {
        success: true,
        data: response.data,
        message: 'Cours en vedette récupérés'
      };
    } catch (error) {
      return {
        success: false,
        error: error.response?.data?.message || 'Erreur lors de la récupération des cours en vedette',
        status: error.response?.status
      };
    }
  }

  // Rechercher des cours
  async searchCourses(searchParams) {
    try {
      const response = await apiCall.get(endpoints.courses.search, { 
        params: searchParams 
      });
      return {
        success: true,
        data: response.data,
        message: 'Recherche effectuée avec succès'
      };
    } catch (error) {
      return {
        success: false,
        error: error.response?.data?.message || 'Erreur lors de la recherche',
        status: error.response?.status
      };
    }
  }

  // Obtenir les catégories de cours
  async getCategories() {
    try {
      const response = await apiCall.get(endpoints.courses.categories);
      return {
        success: true,
        data: response.data,
        message: 'Catégories récupérées'
      };
    } catch (error) {
      return {
        success: false,
        error: error.response?.data?.message || 'Erreur lors de la récupération des catégories',
        status: error.response?.status
      };
    }
  }

  // S'inscrire à un cours
  async enrollInCourse(courseId) {
    try {
      const response = await apiCall.post(endpoints.courses.enroll(courseId));
      return {
        success: true,
        data: response.data,
        message: 'Inscription au cours réussie'
      };
    } catch (error) {
      return {
        success: false,
        error: error.response?.data?.message || 'Erreur lors de l\'inscription',
        status: error.response?.status
      };
    }
  }

  // Se désinscrire d'un cours
  async unenrollFromCourse(courseId) {
    try {
      const response = await apiCall.delete(endpoints.courses.unenroll(courseId));
      return {
        success: true,
        data: response.data,
        message: 'Désinscription du cours réussie'
      };
    } catch (error) {
      return {
        success: false,
        error: error.response?.data?.message || 'Erreur lors de la désinscription',
        status: error.response?.status
      };
    }
  }

  // Obtenir les leçons d'un cours
  async getCourseLessons(courseId) {
    try {
      const response = await apiCall.get(endpoints.courses.lessons(courseId));
      return {
        success: true,
        data: response.data,
        message: 'Leçons récupérées'
      };
    } catch (error) {
      return {
        success: false,
        error: error.response?.data?.message || 'Erreur lors de la récupération des leçons',
        status: error.response?.status
      };
    }
  }

  // Obtenir une leçon spécifique
  async getLesson(courseId, lessonId) {
    try {
      const response = await apiCall.get(endpoints.courses.lesson(courseId, lessonId));
      return {
        success: true,
        data: response.data,
        message: 'Leçon récupérée'
      };
    } catch (error) {
      return {
        success: false,
        error: error.response?.data?.message || 'Erreur lors de la récupération de la leçon',
        status: error.response?.status
      };
    }
  }

  // Marquer une leçon comme terminée
  async completeLesson(courseId, lessonId) {
    try {
      const response = await apiCall.post(
        endpoints.courses.lesson(courseId, lessonId) + '/complete'
      );
      return {
        success: true,
        data: response.data,
        message: 'Leçon marquée comme terminée'
      };
    } catch (error) {
      return {
        success: false,
        error: error.response?.data?.message || 'Erreur lors de la completion de la leçon',
        status: error.response?.status
      };
    }
  }

  // Obtenir les cours de l'utilisateur
  async getMyCourses() {
    try {
      const response = await apiCall.get('/users/courses');
      return {
        success: true,
        data: response.data,
        message: 'Cours de l\'utilisateur récupérés'
      };
    } catch (error) {
      return {
        success: false,
        error: error.response?.data?.message || 'Erreur lors de la récupération des cours',
        status: error.response?.status
      };
    }
  }

  // Obtenir les cours recommandés
  async getRecommendedCourses() {
    try {
      const response = await apiCall.get('/courses/recommended');
      return {
        success: true,
        data: response.data,
        message: 'Cours recommandés récupérés'
      };
    } catch (error) {
      return {
        success: false,
        error: error.response?.data?.message || 'Erreur lors de la récupération des recommandations',
        status: error.response?.status
      };
    }
  }

  // Évaluer un cours
  async rateCourse(courseId, rating, review = '') {
    try {
      const response = await apiCall.post(`/courses/${courseId}/rating`, {
        rating,
        review
      });
      return {
        success: true,
        data: response.data,
        message: 'Évaluation enregistrée'
      };
    } catch (error) {
      return {
        success: false,
        error: error.response?.data?.message || 'Erreur lors de l\'évaluation',
        status: error.response?.status
      };
    }
  }

  // Obtenir les évaluations d'un cours
  async getCourseRatings(courseId) {
    try {
      const response = await apiCall.get(`/courses/${courseId}/ratings`);
      return {
        success: true,
        data: response.data,
        message: 'Évaluations récupérées'
      };
    } catch (error) {
      return {
        success: false,
        error: error.response?.data?.message || 'Erreur lors de la récupération des évaluations',
        status: error.response?.status
      };
    }
  }

  // Ajouter un cours aux favoris
  async addToFavorites(courseId) {
    try {
      const response = await apiCall.post(`/courses/${courseId}/favorite`);
      return {
        success: true,
        data: response.data,
        message: 'Cours ajouté aux favoris'
      };
    } catch (error) {
      return {
        success: false,
        error: error.response?.data?.message || 'Erreur lors de l\'ajout aux favoris',
        status: error.response?.status
      };
    }
  }

  // Retirer un cours des favoris
  async removeFromFavorites(courseId) {
    try {
      const response = await apiCall.delete(`/courses/${courseId}/favorite`);
      return {
        success: true,
        data: response.data,
        message: 'Cours retiré des favoris'
      };
    } catch (error) {
      return {
        success: false,
        error: error.response?.data?.message || 'Erreur lors de la suppression des favoris',
        status: error.response?.status
      };
    }
  }

  // Obtenir les cours favoris
  async getFavoriteCourses() {
    try {
      const response = await apiCall.get('/users/favorites');
      return {
        success: true,
        data: response.data,
        message: 'Cours favoris récupérés'
      };
    } catch (error) {
      return {
        success: false,
        error: error.response?.data?.message || 'Erreur lors de la récupération des favoris',
        status: error.response?.status
      };
    }
  }

  // Obtenir les statistiques d'un cours
  async getCourseStats(courseId) {
    try {
      const response = await apiCall.get(`/courses/${courseId}/stats`);
      return {
        success: true,
        data: response.data,
        message: 'Statistiques du cours récupérées'
      };
    } catch (error) {
      return {
        success: false,
        error: error.response?.data?.message || 'Erreur lors de la récupération des statistiques',
        status: error.response?.status
      };
    }
  }

  // Soumettre un exercice
  async submitExercise(courseId, lessonId, exerciseId, solution) {
    try {
      const response = await apiCall.post(
        `/courses/${courseId}/lessons/${lessonId}/exercises/${exerciseId}/submit`,
        { solution }
      );
      return {
        success: true,
        data: response.data,
        message: 'Exercice soumis avec succès'
      };
    } catch (error) {
      return {
        success: false,
        error: error.response?.data?.message || 'Erreur lors de la soumission',
        status: error.response?.status
      };
    }
  }

  // Obtenir les certificats de l'utilisateur
  async getCertificates() {
    try {
      const response = await apiCall.get('/users/certificates');
      return {
        success: true,
        data: response.data,
        message: 'Certificats récupérés'
      };
    } catch (error) {
      return {
        success: false,
        error: error.response?.data?.message || 'Erreur lors de la récupération des certificats',
        status: error.response?.status
      };
    }
  }

  // Télécharger un certificat
  async downloadCertificate(courseId) {
    try {
      const response = await apiCall.get(`/courses/${courseId}/certificate`, {
        responseType: 'blob'
      });
      return {
        success: true,
        data: response.data,
        message: 'Certificat téléchargé'
      };
    } catch (error) {
      return {
        success: false,
        error: error.response?.data?.message || 'Erreur lors du téléchargement',
        status: error.response?.status
      };
    }
  }
}

// Exporter une instance unique
const courseService = new CourseService();
export default courseService;