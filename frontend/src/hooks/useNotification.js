// frontend/src/hooks/useNotification.js
import { useState, useEffect, useContext, useCallback, useRef } from 'react';
import { NotificationContext } from '../context/NotificationContext';
import notificationService from '../services/notificationService';
import { useLocalStorage } from './useLocalStorage';

/**
 * Hook personnalisé pour la gestion des notifications
 * @param {Object} options - Options de configuration
 * @returns {Object} Objet contenant l'état et les méthodes de notification
 */
export const useNotification = (options = {}) => {
  const {
    autoRemove = true,
    autoRemoveDelay = 5000,
    maxNotifications = 5,
    position = 'top-right',
    enableSound = true
  } = options;

  const notificationContext = useContext(NotificationContext);
  const { getItem, setItem } = useLocalStorage();
  
  // État local
  const [notifications, setNotifications] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  
  // Références
  const timeoutsRef = useRef(new Map());
  const audioRef = useRef(null);

  /**
   * Types de notifications supportés
   */
  const NOTIFICATION_TYPES = {
    SUCCESS: 'success',
    ERROR: 'error',
    WARNING: 'warning',
    INFO: 'info',
    ACHIEVEMENT: 'achievement',
    COURSE: 'course',
    LESSON: 'lesson',
    SYSTEM: 'system'
  };

  /**
   * Génère un ID unique pour les notifications
   */
  const generateId = useCallback(() => {
    return `notification_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
  }, []);

  /**
   * Joue un son de notification
   */
  const playNotificationSound = useCallback((type = 'default') => {
    if (!enableSound) return;
    
    try {
      // Utiliser l'API Web Audio ou un simple audio HTML
      if (audioRef.current) {
        audioRef.current.currentTime = 0;
        audioRef.current.play().catch(() => {
          // Ignorer les erreurs de lecture audio
        });
      }
    } catch (err) {
      // Ignorer les erreurs audio
    }
  }, [enableSound]);

  /**
   * Ajoute une nouvelle notification
   */
  const addNotification = useCallback((notification) => {
    const id = generateId();
    const timestamp = new Date().toISOString();
    
    const newNotification = {
      id,
      timestamp,
      read: false,
      persistent: false,
      ...notification
    };

    setNotifications(prev => {
      const updated = [newNotification, ...prev];
      
      // Limiter le nombre de notifications
      if (updated.length > maxNotifications) {
        const removed = updated.splice(maxNotifications);
        // Nettoyer les timeouts des notifications supprimées
        removed.forEach(notif => {
          if (timeoutsRef.current.has(notif.id)) {
            clearTimeout(timeoutsRef.current.get(notif.id));
            timeoutsRef.current.delete(notif.id);
          }
        });
      }
      
      return updated;
    });

    // Mettre à jour le compteur non lu
    setUnreadCount(prev => prev + 1);
    
    // Jouer le son
    playNotificationSound(notification.type);
    
    // Programmer la suppression automatique
    if (autoRemove && !notification.persistent) {
      const timeout = setTimeout(() => {
        removeNotification(id);
      }, autoRemoveDelay);
      
      timeoutsRef.current.set(id, timeout);
    }
    
    // Mettre à jour le contexte global si disponible
    if (notificationContext?.addNotification) {
      notificationContext.addNotification(newNotification);
    }
    
    return id;
  }, [
    generateId,
    maxNotifications,
    autoRemove,
    autoRemoveDelay,
    playNotificationSound,
    notificationContext
  ]);

  /**
   * Affiche une notification simple
   */
  const showNotification = useCallback((message, type = NOTIFICATION_TYPES.INFO, options = {}) => {
    return addNotification({
      message,
      type,
      ...options
    });
  }, [addNotification, NOTIFICATION_TYPES]);

  /**
   * Affiche une notification de succès
   */
  const showSuccess = useCallback((message, options = {}) => {
    return showNotification(message, NOTIFICATION_TYPES.SUCCESS, {
      icon: '✅',
      ...options
    });
  }, [showNotification, NOTIFICATION_TYPES]);

  /**
   * Affiche une notification d'erreur
   */
  const showError = useCallback((message, options = {}) => {
    return showNotification(message, NOTIFICATION_TYPES.ERROR, {
      icon: '❌',
      persistent: true, // Les erreurs restent affichées
      ...options
    });
  }, [showNotification, NOTIFICATION_TYPES]);

  /**
   * Affiche une notification d'avertissement
   */
  const showWarning = useCallback((message, options = {}) => {
    return showNotification(message, NOTIFICATION_TYPES.WARNING, {
      icon: '⚠️',
      ...options
    });
  }, [showNotification, NOTIFICATION_TYPES]);

  /**
   * Affiche une notification d'information
   */
  const showInfo = useCallback((message, options = {}) => {
    return showNotification(message, NOTIFICATION_TYPES.INFO, {
      icon: 'ℹ️',
      ...options
    });
  }, [showNotification, NOTIFICATION_TYPES]);

  /**
   * Affiche une notification de réussite/achievement
   */
  const showAchievement = useCallback((title, message, options = {}) => {
    return showNotification(message, NOTIFICATION_TYPES.ACHIEVEMENT, {
      title,
      icon: '🏆',
      persistent: true,
      ...options
    });
  }, [showNotification, NOTIFICATION_TYPES]);

  /**
   * Supprime une notification
   */
  const removeNotification = useCallback((id) => {
    setNotifications(prev => prev.filter(notif => notif.id !== id));
    
    // Nettoyer le timeout si il existe
    if (timeoutsRef.current.has(id)) {
      clearTimeout(timeoutsRef.current.get(id));
      timeoutsRef.current.delete(id);
    }
    
    // Mettre à jour le contexte global si disponible
    if (notificationContext?.removeNotification) {
      notificationContext.removeNotification(id);
    }
  }, [notificationContext]);

  /**
   * Marque une notification comme lue
   */
  const markAsRead = useCallback((id) => {
    setNotifications(prev =>
      prev.map(notif => {
        if (notif.id === id && !notif.read) {
          setUnreadCount(count => Math.max(0, count - 1));
          return { ...notif, read: true };
        }
        return notif;
      })
    );
  }, []);

  /**
   * Marque toutes les notifications comme lues
   */
  const markAllAsRead = useCallback(() => {
    setNotifications(prev =>
      prev.map(notif => ({ ...notif, read: true }))
    );
    setUnreadCount(0);
  }, []);

  /**
   * Supprime toutes les notifications
   */
  const clearAll = useCallback(() => {
    // Nettoyer tous les timeouts
    timeoutsRef.current.forEach(timeout => clearTimeout(timeout));
    timeoutsRef.current.clear();
    
    setNotifications([]);
    setUnreadCount(0);
    
    if (notificationContext?.clearAll) {
      notificationContext.clearAll();
    }
  }, [notificationContext]);

  /**
   * Supprime les notifications lues
   */
  const clearRead = useCallback(() => {
    setNotifications(prev => {
      const unread = prev.filter(notif => !notif.read);
      
      // Nettoyer les timeouts des notifications supprimées
      prev.forEach(notif => {
        if (notif.read && timeoutsRef.current.has(notif.id)) {
          clearTimeout(timeoutsRef.current.get(notif.id));
          timeoutsRef.current.delete(notif.id);
        }
      });
      
      return unread;
    });
  }, []);

  /**
   * Récupère les notifications depuis le serveur
   */
  const fetchNotifications = useCallback(async (userId) => {
    if (!userId) return;
    
    setLoading(true);
    setError(null);
    
    try {
      const serverNotifications = await notificationService.getUserNotifications(userId);
      setNotifications(serverNotifications);
      
      // Calculer le nombre non lu
      const unread = serverNotifications.filter(notif => !notif.read).length;
      setUnreadCount(unread);
    } catch (err) {
      setError(err.message);
      console.error('Erreur lors du chargement des notifications:', err);
    } finally {
      setLoading(false);
    }
  }, []);

  /**
   * Envoie une notification push (si supporté)
   */
  const sendPushNotification = useCallback(async (title, body, options = {}) => {
    if (!('Notification' in window)) {
      console.warn('Ce navigateur ne supporte pas les notifications desktop');
      return false;
    }
    
    let permission = Notification.permission;
    
    if (permission === 'default') {
      permission = await Notification.requestPermission();
    }
    
    if (permission === 'granted') {
      new Notification(title, {
        body,
        icon: '/favicon.ico',
        badge: '/favicon.ico',
        ...options
      });
      return true;
    }
    
    return false;
  }, []);

  /**
   * Sauvegarde les préférences de notification
   */
  const savePreferences = useCallback((preferences) => {
    setItem('notificationPreferences', preferences);
  }, [setItem]);

  /**
   * Charge les préférences de notification
   */
  const loadPreferences = useCallback(() => {
    return getItem('notificationPreferences', {
      enableSound: true,
      enablePush: false,
      autoRemove: true,
      autoRemoveDelay: 5000
    });
  }, [getItem]);

  // Effets
  useEffect(() => {
    // Initialiser l'audio pour les notifications
    if (enableSound) {
      audioRef.current = new Audio('/sounds/notification.mp3');
      audioRef.current.volume = 0.3;
    }
    
    return () => {
      // Nettoyer tous les timeouts lors du démontage
      timeoutsRef.current.forEach(timeout => clearTimeout(timeout));
      timeoutsRef.current.clear();
    };
  }, [enableSound]);

  // Nettoyage lors du démontage du composant
  useEffect(() => {
    return () => {
      timeoutsRef.current.forEach(timeout => clearTimeout(timeout));
      timeoutsRef.current.clear();
    };
  }, []);

  return {
    // État
    notifications,
    unreadCount,
    loading,
    error,
    
    // Types de notifications
    types: NOTIFICATION_TYPES,
    
    // Méthodes d'affichage
    showNotification,
    showSuccess,
    showError,
    showWarning,
    showInfo,
    showAchievement,
    addNotification,
    
    // Méthodes de gestion
    removeNotification,
    markAsRead,
    markAllAsRead,
    clearAll,
    clearRead,
    
    // Méthodes de récupération
    fetchNotifications,
    
    // Notifications push
    sendPushNotification,
    
    // Préférences
    savePreferences,
    loadPreferences,
    
    // Propriétés calculées
    hasUnread: unreadCount > 0,
    isEmpty: notifications.length === 0,
    isLoading: loading,
    hasError: !!error
  };
};

export default useNotification;