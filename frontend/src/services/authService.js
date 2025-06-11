// src/services/authService.js
// services/authService.js
import { apiCall, endpoints } from './api';

class AuthService {
  constructor() {
    this.token = localStorage.getItem('authToken');
    this.user = JSON.parse(localStorage.getItem('user') || 'null');
  }

  // Connexion utilisateur
  async login(credentials) {
    try {
      const response = await apiCall.post(endpoints.auth.login, credentials);
      const { token, user, refreshToken } = response.data;
      
      // Stocker les informations d'authentification
      this.setAuthData(token, user, refreshToken);
      
      return {
        success: true,
        data: { token, user, refreshToken },
        message: 'Connexion réussie'
      };
    } catch (error) {
      return {
        success: false,
        error: error.response?.data?.message || 'Erreur lors de la connexion',
        status: error.response?.status
      };
    }
  }

  // Inscription utilisateur
  async register(userData) {
    try {
      const response = await apiCall.post(endpoints.auth.register, userData);
      const { token, user, refreshToken } = response.data;
      
      // Stocker les informations d'authentification
      this.setAuthData(token, user, refreshToken);
      
      return {
        success: true,
        data: { token, user, refreshToken },
        message: 'Inscription réussie'
      };
    } catch (error) {
      return {
        success: false,
        error: error.response?.data?.message || 'Erreur lors de l\'inscription',
        status: error.response?.status
      };
    }
  }

  // Déconnexion
  async logout() {
    try {
      await apiCall.post(endpoints.auth.logout);
    } catch (error) {
      console.error('Erreur lors de la déconnexion:', error);
    } finally {
      this.clearAuthData();
      return { success: true, message: 'Déconnexion réussie' };
    }
  }

  // Mot de passe oublié
  async forgotPassword(email) {
    try {
      const response = await apiCall.post(endpoints.auth.forgotPassword, { email });
      return {
        success: true,
        data: response.data,
        message: 'Email de récupération envoyé'
      };
    } catch (error) {
      return {
        success: false,
        error: error.response?.data?.message || 'Erreur lors de l\'envoi de l\'email',
        status: error.response?.status
      };
    }
  }

  // Réinitialiser le mot de passe
  async resetPassword(token, newPassword) {
    try {
      const response = await apiCall.post(endpoints.auth.resetPassword, {
        token,
        password: newPassword
      });
      return {
        success: true,
        data: response.data,
        message: 'Mot de passe réinitialisé avec succès'
      };
    } catch (error) {
      return {
        success: false,
        error: error.response?.data?.message || 'Erreur lors de la réinitialisation',
        status: error.response?.status
      };
    }
  }

  // Rafraîchir le token
  async refreshToken() {
    try {
      const refreshToken = localStorage.getItem('refreshToken');
      if (!refreshToken) {
        throw new Error('Aucun refresh token disponible');
      }

      const response = await apiCall.post(endpoints.auth.refreshToken, {
        refreshToken
      });
      
      const { token, user } = response.data;
      this.setAuthData(token, user, refreshToken);
      
      return {
        success: true,
        data: { token, user },
        message: 'Token rafraîchi'
      };
    } catch (error) {
      this.clearAuthData();
      return {
        success: false,
        error: error.response?.data?.message || 'Erreur lors du rafraîchissement',
        status: error.response?.status
      };
    }
  }

  // Obtenir le profil utilisateur
  async getProfile() {
    try {
      const response = await apiCall.get(endpoints.auth.profile);
      const user = response.data;
      
      // Mettre à jour les données utilisateur en local
      this.user = user;
      localStorage.setItem('user', JSON.stringify(user));
      
      return {
        success: true,
        data: user,
        message: 'Profil récupéré'
      };
    } catch (error) {
      return {
        success: false,
        error: error.response?.data?.message || 'Erreur lors de la récupération du profil',
        status: error.response?.status
      };
    }
  }

  // Mettre à jour le profil utilisateur
  async updateProfile(profileData) {
    try {
      const response = await apiCall.put(endpoints.auth.profile, profileData);
      const user = response.data;
      
      // Mettre à jour les données utilisateur en local
      this.user = user;
      localStorage.setItem('user', JSON.stringify(user));
      
      return {
        success: true,
        data: user,
        message: 'Profil mis à jour'
      };
    } catch (error) {
      return {
        success: false,
        error: error.response?.data?.message || 'Erreur lors de la mise à jour',
        status: error.response?.status
      };
    }
  }

  // Vérifier si l'utilisateur est authentifié
  isAuthenticated() {
    return !!(this.token && this.user);
  }

  // Obtenir le token actuel
  getToken() {
    return this.token;
  }

  // Obtenir l'utilisateur actuel
  getCurrentUser() {
    return this.user;
  }

  // Vérifier si l'utilisateur a un rôle spécifique
  hasRole(role) {
    return this.user?.roles?.includes(role) || false;
  }

  // Vérifier si l'utilisateur a une permission spécifique
  hasPermission(permission) {
    return this.user?.permissions?.includes(permission) || false;
  }

  // Stocker les données d'authentification
  setAuthData(token, user, refreshToken) {
    this.token = token;
    this.user = user;
    
    localStorage.setItem('authToken', token);
    localStorage.setItem('user', JSON.stringify(user));
    if (refreshToken) {
      localStorage.setItem('refreshToken', refreshToken);
    }
  }

  // Effacer les données d'authentification
  clearAuthData() {
    this.token = null;
    this.user = null;
    
    localStorage.removeItem('authToken');
    localStorage.removeItem('user');
    localStorage.removeItem('refreshToken');
  }

  // Vérifier la validité du token
  isTokenValid() {
    if (!this.token) return false;
    
    try {
      const payload = JSON.parse(atob(this.token.split('.')[1]));
      const currentTime = Date.now() / 1000;
      return payload.exp > currentTime;
    } catch (error) {
      return false;
    }
  }

  // Obtenir les informations du token
  getTokenInfo() {
    if (!this.token) return null;
    
    try {
      const payload = JSON.parse(atob(this.token.split('.')[1]));
      return {
        userId: payload.sub,
        email: payload.email,
        roles: payload.roles,
        exp: payload.exp,
        iat: payload.iat
      };
    } catch (error) {
      return null;
    }
  }
}

// Exporter une instance unique
const authService = new AuthService();
export default authService;