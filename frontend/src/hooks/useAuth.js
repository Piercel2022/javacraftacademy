import { useState, useEffect, useContext, useCallback } from 'react';
import { AuthContext } from '../context/AuthContext';
import { authService } from '../services/authService';
import { useNotification } from './useNotification';

/**
 * Hook personnalisé pour la gestion de l'authentification
 * Gère l'état de connexion, les opérations d'auth et la persistance
 */
export const useAuth = () => {
  const context = useContext(AuthContext);
  const { showNotification } = useNotification();
  
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }

  const { user, token, isAuthenticated, login: contextLogin, logout: contextLogout } = context;

  // Fonction de connexion
  const login = useCallback(async (credentials) => {
    setLoading(true);
    setError(null);
    
    try {
      const response = await authService.login(credentials);
      
      if (response.success) {
        contextLogin(response.user, response.token);
        showNotification('Connexion réussie !', 'success');
        return { success: true, user: response.user };
      } else {
        const errorMsg = response.message || 'Erreur de connexion';
        setError(errorMsg);
        showNotification(errorMsg, 'error');
        return { success: false, error: errorMsg };
      }
    } catch (err) {
      const errorMsg = err.response?.data?.message || 'Erreur de connexion';
      setError(errorMsg);
      showNotification(errorMsg, 'error');
      return { success: false, error: errorMsg };
    } finally {
      setLoading(false);
    }
  }, [contextLogin, showNotification]);

  // Fonction d'inscription
  const register = useCallback(async (userData) => {
    setLoading(true);
    setError(null);
    
    try {
      const response = await authService.register(userData);
      
      if (response.success) {
        contextLogin(response.user, response.token);
        showNotification('Inscription réussie ! Bienvenue !', 'success');
        return { success: true, user: response.user };
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
  }, [contextLogin, showNotification]);

  // Fonction de déconnexion
  const logout = useCallback(async () => {
    setLoading(true);
    
    try {
      await authService.logout();
      contextLogout();
      showNotification('Déconnexion réussie', 'info');
    } catch (err) {
      console.error('Erreur lors de la déconnexion:', err);
      // On déconnecte quand même côté client
      contextLogout();
    } finally {
      setLoading(false);
    }
  }, [contextLogout, showNotification]);

  // Fonction de réinitialisation du mot de passe
  const forgotPassword = useCallback(async (email) => {
    setLoading(true);
    setError(null);
    
    try {
      const response = await authService.forgotPassword(email);
      
      if (response.success) {
        showNotification('Email de réinitialisation envoyé !', 'success');
        return { success: true };
      } else {
        const errorMsg = response.message || 'Erreur lors de l\'envoi';
        setError(errorMsg);
        showNotification(errorMsg, 'error');
        return { success: false, error: errorMsg };
      }
    } catch (err) {
      const errorMsg = err.response?.data?.message || 'Erreur lors de l\'envoi';
      setError(errorMsg);
      showNotification(errorMsg, 'error');
      return { success: false, error: errorMsg };
    } finally {
      setLoading(false);
    }
  }, [showNotification]);

  // Fonction de réinitialisation du mot de passe avec token
  const resetPassword = useCallback(async (token, newPassword) => {
    setLoading(true);
    setError(null);
    
    try {
      const response = await authService.resetPassword(token, newPassword);
      
      if (response.success) {
        showNotification('Mot de passe réinitialisé avec succès !', 'success');
        return { success: true };
      } else {
        const errorMsg = response.message || 'Erreur lors de la réinitialisation';
        setError(errorMsg);
        showNotification(errorMsg, 'error');
        return { success: false, error: errorMsg };
      }
    } catch (err) {
      const errorMsg = err.response?.data?.message || 'Erreur lors de la réinitialisation';
      setError(errorMsg);
      showNotification(errorMsg, 'error');
      return { success: false, error: errorMsg };
    } finally {
      setLoading(false);
    }
  }, [showNotification]);

  // Fonction de mise à jour du profil
  const updateProfile = useCallback(async (profileData) => {
    setLoading(true);
    setError(null);
    
    try {
      const response = await authService.updateProfile(profileData);
      
      if (response.success) {
        contextLogin(response.user, token); // Met à jour l'utilisateur dans le context
        showNotification('Profil mis à jour avec succès !', 'success');
        return { success: true, user: response.user };
      } else {
        const errorMsg = response.message || 'Erreur lors de la mise à jour';
        setError(errorMsg);
        showNotification(errorMsg, 'error');
        return { success: false, error: errorMsg };
      }
    } catch (err) {
      const errorMsg = err.response?.data?.message || 'Erreur lors de la mise à jour';
      setError(errorMsg);
      showNotification(errorMsg, 'error');
      return { success: false, error: errorMsg };
    } finally {
      setLoading(false);
    }
  }, [contextLogin, token, showNotification]);

  // Fonction de changement de mot de passe
  const changePassword = useCallback(async (currentPassword, newPassword) => {
    setLoading(true);
    setError(null);
    
    try {
      const response = await authService.changePassword(currentPassword, newPassword);
      
      if (response.success) {
        showNotification('Mot de passe modifié avec succès !', 'success');
        return { success: true };
      } else {
        const errorMsg = response.message || 'Erreur lors du changement de mot de passe';
        setError(errorMsg);
        showNotification(errorMsg, 'error');
        return { success: false, error: errorMsg };
      }
    } catch (err) {
      const errorMsg = err.response?.data?.message || 'Erreur lors du changement de mot de passe';
      setError(errorMsg);
      showNotification(errorMsg, 'error');
      return { success: false, error: errorMsg };
    } finally {
      setLoading(false);
    }
  }, [showNotification]);

  // Vérification de la validité du token
  const checkTokenValidity = useCallback(async () => {
    if (!token) return false;
    
    try {
      const response = await authService.verifyToken(token);
      return response.valid;
    } catch (err) {
      console.error('Erreur lors de la vérification du token:', err);
      return false;
    }
  }, [token]);

  // Fonction pour rafraîchir le token
  const refreshToken = useCallback(async () => {
    try {
      const response = await authService.refreshToken();
      
      if (response.success) {
        contextLogin(response.user, response.token);
        return { success: true, token: response.token };
      } else {
        contextLogout();
        return { success: false };
      }
    } catch (err) {
      console.error('Erreur lors du rafraîchissement du token:', err);
      contextLogout();
      return { success: false };
    }
  }, [contextLogin, contextLogout]);

  // Effacer les erreurs
  const clearError = useCallback(() => {
    setError(null);
  }, []);

  // Vérification automatique du token au chargement
  useEffect(() => {
    if (isAuthenticated && token) {
      checkTokenValidity().then(isValid => {
        if (!isValid) {
          refreshToken();
        }
      });
    }
  }, [isAuthenticated, token, checkTokenValidity, refreshToken]);

  return {
    // État
    user,
    token,
    isAuthenticated,
    loading,
    error,
    
    // Actions
    login,
    register,
    logout,
    forgotPassword,
    resetPassword,
    updateProfile,
    changePassword,
    checkTokenValidity,
    refreshToken,
    clearError
  };
};