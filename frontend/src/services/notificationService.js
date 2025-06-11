// src/services/notificationService.js
// services/notificationService.js
import { apiCall, endpoints } from './api';

class NotificationService {
  // Obtenir toutes les notifications de l'utilisateur
  async getNotifications(params = {}) {
    try {
      const queryParams = new URLSearchParams({
        page: params.page || 1,
        limit: params.limit || 20,
        type: params.type || '',
        status: params.status || '',
        ...params
      });

      const response = await apiCall.get(
        `${endpoints.notifications.list}?${queryParams}`
      );
      
      return {
        success: true,
        data: {
          notifications: response.data.notifications,
          pagination: response.data.pagination,
          unreadCount: response.data.unreadCount
        },
        message: 'Notifications récupérées'
      };
    } catch (error) {
      return {
        success: false,
        error: error.response?.data?.message || 'Erreur lors de la récupération des notifications',
        status: error.response?.status
      };
    }
  }

  // Obtenir le nombre de notifications non lues
  async getUnreadCount() {
    try {
      const response = await apiCall.get(endpoints.notifications.unreadCount);
      
      return {
        success: true,
        data: {
          count: response.data.count
        },
        message: 'Nombre de notifications non lues récupéré'
      };
    } catch (error) {
      return {
        success: false,
        error: error.response?.data?.message || 'Erreur lors de la récupération du compteur',
        status: error.response?.status
      };
    }
  }

  // Marquer une notification comme lue
  async markAsRead(notificationId) {
    try {
      const response = await apiCall.patch(
        endpoints.notifications.markRead(notificationId)
      );
      
      return {
        success: true,
        data: response.data,
        message: 'Notification marquée comme lue'
      };
    } catch (error) {
      return {
        success: false,
        error: error.response?.data?.message || 'Erreur lors du marquage comme lue',
        status: error.response?.status
      };
    }
  }

  // Marquer toutes les notifications comme lues
  async markAllAsRead() {
    try {
      const response = await apiCall.patch(endpoints.notifications.markAllRead);
      
      return {
        success: true,
        data: response.data,
        message: 'Toutes les notifications marquées comme lues'
      };
    } catch (error) {
      return {
        success: false,
        error: error.response?.data?.message || 'Erreur lors du marquage global',
        status: error.response?.status
      };
    }
  }

  // Supprimer une notification
  async deleteNotification(notificationId) {
    try {
      const response = await apiCall.delete(
        endpoints.notifications.delete(notificationId)
      );
      
      return {
        success: true,
        data: response.data,
        message: 'Notification supprimée'
      };
    } catch (error) {
      return {
        success: false,
        error: error.response?.data?.message || 'Erreur lors de la suppression',
        status: error.response?.status
      };
    }
  }

  // Supprimer toutes les notifications lues
  async deleteAllRead() {
    try {
      const response = await apiCall.delete(endpoints.notifications.deleteAllRead);
      
      return {
        success: true,
        data: response.data,
        message: 'Notifications lues supprimées'
      };
    } catch (error) {
      return {
        success: false,
        error: error.response?.data?.message || 'Erreur lors de la suppression groupée',
        status: error.response?.status
      };
    }
  }

  // Créer une notification (pour les administrateurs)
  async createNotification(notificationData) {
    try {
      const response = await apiCall.post(endpoints.notifications.create, {
        ...notificationData,
        createdAt: new Date().toISOString()
      });
      
      return {
        success: true,
        data: response.data,
        message: 'Notification créée'
      };
    } catch (error) {
      return {
        success: false,
        error: error.response?.data?.message || 'Erreur lors de la création',
        status: error.response?.status
      };
    }
  }

  // Obtenir les préférences de notification
  async getNotificationPreferences() {
    try {
      const response = await apiCall.get(endpoints.notifications.preferences);
      
      return {
        success: true,
        data: response.data,
        message: 'Préférences récupérées'
      };
    } catch (error) {
      return {
        success: false,
        error: error.response?.data?.message || 'Erreur lors de la récupération des préférences',
        status: error.response?.status
      };
    }
  }

  // Mettre à jour les préférences de notification
  async updateNotificationPreferences(preferences) {
    try {
      const response = await apiCall.put(endpoints.notifications.preferences, preferences);
      
      return {
        success: true,
        data: response.data,
        message: 'Préférences mises à jour'
      };
    } catch (error) {
      return {
        success: false,
        error: error.response?.data?.message || 'Erreur lors de la mise à jour des préférences',
        status: error.response?.status
      };
    }
  }

  // S'abonner aux notifications push
  async subscribeToPush(subscription) {
    try {
      const response = await apiCall.post(endpoints.notifications.pushSubscribe, {
        subscription,
        subscribedAt: new Date().toISOString()
      });
      
      return {
        success: true,
        data: response.data,
        message: 'Abonnement aux notifications push activé'
      };
    } catch (error) {
      return {
        success: false,
        error: error.response?.data?.message || 'Erreur lors de l\'abonnement push',
        status: error.response?.status
      };
    }
  }

  // Se désabonner des notifications push
  async unsubscribeFromPush(subscriptionId) {
    try {
      const response = await apiCall.delete(
        endpoints.notifications.pushUnsubscribe(subscriptionId)
      );
      
      return {
        success: true,
        data: response.data,
        message: 'Désabonnement des notifications push effectué'
      };
    } catch (error) {
      return {
        success: false,
        error: error.response?.data?.message || 'Erreur lors du désabonnement push',
        status: error.response?.status
      };
    }
  }

  // Obtenir les statistiques de notifications
  async getNotificationStats(period = '7d') {
    try {
      const response = await apiCall.get(
        `${endpoints.notifications.stats}?period=${period}`
      );
      
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

  // Tester une notification push
  async testPushNotification(message = 'Test de notification') {
    try {
      const response = await apiCall.post(endpoints.notifications.testPush, {
        message,
        timestamp: new Date().toISOString()
      });
      
      return {
        success: true,
        data: response.data,
        message: 'Notification test envoyée'
      };
    } catch (error) {
      return {
        success: false,
        error: error.response?.data?.message || 'Erreur lors de l\'envoi du test',
        status: error.response?.status
      };
    }
  }

  // Obtenir l'historique des notifications
  async getNotificationHistory(params = {}) {
    try {
      const queryParams = new URLSearchParams({
        startDate: params.startDate || '',
        endDate: params.endDate || '',
        type: params.type || '',
        page: params.page || 1,
        limit: params.limit || 50,
        ...params
      });

      const response = await apiCall.get(
        `${endpoints.notifications.history}?${queryParams}`
      );
      
      return {
        success: true,
        data: response.data,
        message: 'Historique récupéré'
      };
    } catch (error) {
      return {
        success: false,
        error: error.response?.data?.message || 'Erreur lors de la récupération de l\'historique',
        status: error.response?.status
      };
    }
  }

  // Archiver une notification
  async archiveNotification(notificationId) {
    try {
      const response = await apiCall.patch(
        endpoints.notifications.archive(notificationId)
      );
      
      return {
        success: true,
        data: response.data,
        message: 'Notification archivée'
      };
    } catch (error) {
      return {
        success: false,
        error: error.response?.data?.message || 'Erreur lors de l\'archivage',
        status: error.response?.status
      };
    }
  }

  // Restaurer une notification archivée
  async restoreNotification(notificationId) {
    try {
      const response = await apiCall.patch(
        endpoints.notifications.restore(notificationId)
      );
      
      return {
        success: true,
        data: response.data,
        message: 'Notification restaurée'
      };
    } catch (error) {
      return {
        success: false,
        error: error.response?.data?.message || 'Erreur lors de la restauration',
        status: error.response?.status
      };
    }
  }

  // Vérifier les permissions de notification du navigateur
  async checkNotificationPermission() {
    try {
      if (!('Notification' in window)) {
        return {
          success: false,
          error: 'Les notifications ne sont pas supportées par ce navigateur',
          permission: 'unsupported'
        };
      }

      const permission = Notification.permission;
      
      return {
        success: true,
        data: {
          permission: permission,
          supported: true
        },
        message: `Permission de notification: ${permission}`
      };
    } catch (error) {
      return {
        success: false,
        error: 'Erreur lors de la vérification des permissions',
        permission: 'error'
      };
    }
  }

  // Demander la permission pour les notifications
  async requestNotificationPermission() {
    try {
      if (!('Notification' in window)) {
        return {
          success: false,
          error: 'Les notifications ne sont pas supportées',
          permission: 'unsupported'
        };
      }

      const permission = await Notification.requestPermission();
      
      return {
        success: permission === 'granted',
        data: {
          permission: permission
        },
        message: permission === 'granted' 
          ? 'Permission accordée pour les notifications' 
          : 'Permission refusée pour les notifications'
      };
    } catch (error) {
      return {
        success: false,
        error: 'Erreur lors de la demande de permission',
        permission: 'error'
      };
    }
  }

  // Afficher une notification locale
  showLocalNotification(title, options = {}) {
    try {
      if (Notification.permission !== 'granted') {
        return {
          success: false,
          error: 'Permission de notification non accordée'
        };
      }

      const notification = new Notification(title, {
        icon: options.icon || '/favicon.ico',
        body: options.body || '',
        badge: options.badge || '/favicon.ico',
        tag: options.tag || 'default',
        requireInteraction: options.requireInteraction || false,
        silent: options.silent || false,
        ...options
      });

      // Événements de notification
      notification.onclick = options.onClick || function() {
        window.focus();
        notification.close();
      };

      notification.onclose = options.onClose || function() {
        console.log('Notification fermée');
      };

      notification.onerror = options.onError || function(error) {
        console.error('Erreur de notification:', error);
      };

      return {
        success: true,
        data: {
          notification: notification
        },
        message: 'Notification affichée'
      };
    } catch (error) {
      return {
        success: false,
        error: 'Erreur lors de l\'affichage de la notification'
      };
    }
  }
}

// Créer une instance unique du service
const notificationService = new NotificationService();

export default notificationService;