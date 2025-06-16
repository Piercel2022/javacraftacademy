// authService.js
import api from './api';

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:3001';

class AuthService {
  constructor() {
    this.token = localStorage.getItem('authToken');
    this.user = JSON.parse(localStorage.getItem('user') || 'null');
  }

  // Connexion classique avec email/password
  async login(email, password) {
    try {
      const response = await api.post('/auth/login', {
        email,
        password
      });

      if (response.data.success) {
        this.setAuthData(response.data.user, response.data.token);
        return {
          success: true,
          user: response.data.user,
          token: response.data.token,
          message: 'Connexion réussie'
        };
      }

      return {
        success: false,
        message: response.data.message || 'Échec de la connexion'
      };
    } catch (error) {
      console.error('Erreur lors de la connexion:', error);
      return {
        success: false,
        message: error.response?.data?.message || 'Erreur de connexion'
      };
    }
  }

  // Inscription
  async register(userData) {
    try {
      const response = await api.post('/auth/register', userData);

      if (response.data.success) {
        this.setAuthData(response.data.user, response.data.token);
        return {
          success: true,
          user: response.data.user,
          token: response.data.token,
          message: 'Inscription réussie'
        };
      }

      return {
        success: false,
        message: response.data.message || 'Échec de l\'inscription'
      };
    } catch (error) {
      console.error('Erreur lors de l\'inscription:', error);
      return {
        success: false,
        message: error.response?.data?.message || 'Erreur d\'inscription'
      };
    }
  }

  // Connexion avec Google (redirection)
  async googleLogin() {
    try {
      // Cette méthode redirige vers le backend pour l'authentification OAuth
      const googleAuthUrl = `${API_BASE_URL}/auth/google`;
      window.location.href = googleAuthUrl;
    } catch (error) {
      console.error('Erreur connexion Google:', error);
      throw new Error('Erreur lors de la connexion Google');
    }
  }

  // Connexion avec GitHub (redirection)
  async githubLogin() {
    try {
      // Cette méthode redirige vers le backend pour l'authentification OAuth
      const githubAuthUrl = `${API_BASE_URL}/auth/github`;
      window.location.href = githubAuthUrl;
    } catch (error) {
      console.error('Erreur connexion GitHub:', error);
      throw new Error('Erreur lors de la connexion GitHub');
    }
  }

  // Gestion de callback OAuth (appelé après redirection depuis Google/GitHub)
  async handleOAuthCallback(provider, code, state) {
    try {
      const response = await api.post(`/auth/${provider}/callback`, {
        code,
        state
      });

      if (response.data.success) {
        this.setAuthData(response.data.user, response.data.token);
        return {
          success: true,
          user: response.data.user,
          token: response.data.token
        };
      }

      return {
        success: false,
        message: response.data.message || `Échec de la connexion ${provider}`
      };
    } catch (error) {
      console.error(`Erreur callback ${provider}:`, error);
      return {
        success: false,
        message: error.response?.data?.message || `Erreur de connexion ${provider}`
      };
    }
  }

  // Mot de passe oublié
  async forgotPassword(email) {
    try {
      const response = await api.post('/auth/forgot-password', { email });
      return {
        success: response.data.success,
        message: response.data.message || 'Email de réinitialisation envoyé'
      };
    } catch (error) {
      console.error('Erreur mot de passe oublié:', error);
      return {
        success: false,
        message: error.response?.data?.message || 'Erreur lors de l\'envoi de l\'email'
      };
    }
  }

  // Réinitialisation du mot de passe
  async resetPassword(token, newPassword) {
    try {
      const response = await api.post('/auth/reset-password', {
        token,
        password: newPassword
      });

      return {
        success: response.data.success,
        message: response.data.message || 'Mot de passe réinitialisé avec succès'
      };
    } catch (error) {
      console.error('Erreur réinitialisation mot de passe:', error);
      return {
        success: false,
        message: error.response?.data?.message || 'Erreur lors de la réinitialisation'
      };
    }
  }

  // Déconnexion
  async logout() {
    try {
      // Optionnel: appeler le backend pour invalider le token
      if (this.token) {
        await api.post('/auth/logout');
      }
    } catch (error) {
      console.error('Erreur lors de la déconnexion:', error);
    } finally {
      this.clearAuthData();
    }
  }

  // Actualiser le token
  async refreshToken() {
    try {
      const response = await api.post('/auth/refresh');
      
      if (response.data.success) {
        this.setAuthData(response.data.user, response.data.token);
        return response.data.token;
      }
      
      // Si le refresh échoue, déconnecter l'utilisateur
      this.clearAuthData();
      return null;
    } catch (error) {
      console.error('Erreur refresh token:', error);
      this.clearAuthData();
      return null;
    }
  }

  // Vérifier si l'utilisateur est connecté
  isAuthenticated() {
    return !!(this.token && this.user);
  }

  // Obtenir l'utilisateur actuel
  getCurrentUser() {
    return this.user;
  }

  // Obtenir le token actuel
  getToken() {
    return this.token;
  }

  // Vérifier si le token est expiré
  isTokenExpired() {
    if (!this.token) return true;
    
    try {
      const payload = JSON.parse(atob(this.token.split('.')[1]));
      const currentTime = Date.now() / 1000;
      return payload.exp < currentTime;
    } catch (error) {
      return true;
    }
  }

  // Mettre à jour les données d'authentification
  setAuthData(user, token) {
    this.user = user;
    this.token = token;
    localStorage.setItem('user', JSON.stringify(user));
    localStorage.setItem('authToken', token);
    
    // Configurer le header Authorization pour les futures requêtes
    if (token) {
      api.defaults.headers.common['Authorization'] = `Bearer ${token}`;
    }
  }

  // Effacer les données d'authentification
  clearAuthData() {
    this.user = null;
    this.token = null;
    localStorage.removeItem('user');
    localStorage.removeItem('authToken');
    delete api.defaults.headers.common['Authorization'];
  }

  // Initialiser l'authentification au démarrage de l'app
  initializeAuth() {
    if (this.token && !this.isTokenExpired()) {
      api.defaults.headers.common['Authorization'] = `Bearer ${this.token}`;
      return true;
    } else if (this.token && this.isTokenExpired()) {
      // Essayer de refresh le token
      return this.refreshToken();
    }
    return false;
  }

  // Mettre à jour le profil utilisateur
  async updateProfile(profileData) {
    try {
      const response = await api.put('/auth/profile', profileData);
      
      if (response.data.success) {
        this.user = { ...this.user, ...response.data.user };
        localStorage.setItem('user', JSON.stringify(this.user));
        return {
          success: true,
          user: this.user,
          message: 'Profil mis à jour avec succès'
        };
      }

      return {
        success: false,
        message: response.data.message || 'Erreur lors de la mise à jour'
      };
    } catch (error) {
      console.error('Erreur mise à jour profil:', error);
      return {
        success: false,
        message: error.response?.data?.message || 'Erreur lors de la mise à jour du profil'
      };
    }
  }

  // Changer le mot de passe
  async changePassword(currentPassword, newPassword) {
    try {
      const response = await api.put('/auth/change-password', {
        currentPassword,
        newPassword
      });

      return {
        success: response.data.success,
        message: response.data.message || 'Mot de passe modifié avec succès'
      };
    } catch (error) {
      console.error('Erreur changement mot de passe:', error);
      return {
        success: false,
        message: error.response?.data?.message || 'Erreur lors du changement de mot de passe'
      };
    }
  }

  // Supprimer le compte utilisateur
  async deleteAccount(password) {
    try {
      const response = await api.delete('/auth/account', {
        data: { password }
      });

      if (response.data.success) {
        this.clearAuthData();
        return {
          success: true,
          message: 'Compte supprimé avec succès'
        };
      }

      return {
        success: false,
        message: response.data.message || 'Erreur lors de la suppression du compte'
      };
    } catch (error) {
      console.error('Erreur suppression compte:', error);
      return {
        success: false,
        message: error.response?.data?.message || 'Erreur lors de la suppression du compte'
      };
    }
  }

  // Vérifier l'email (si système de vérification par email)
  async verifyEmail(token) {
    try {
      const response = await api.post('/auth/verify-email', { token });
      
      if (response.data.success && response.data.user) {
        // Mettre à jour les données utilisateur avec le statut vérifié
        this.user = { ...this.user, ...response.data.user };
        localStorage.setItem('user', JSON.stringify(this.user));
      }

      return {
        success: response.data.success,
        message: response.data.message || 'Email vérifié avec succès'
      };
    } catch (error) {
      console.error('Erreur vérification email:', error);
      return {
        success: false,
        message: error.response?.data?.message || 'Erreur lors de la vérification de l\'email'
      };
    }
  }

  // Renvoyer l'email de vérification
  async resendVerificationEmail() {
    try {
      const response = await api.post('/auth/resend-verification');
      return {
        success: response.data.success,
        message: response.data.message || 'Email de vérification renvoyé'
      };
    } catch (error) {
      console.error('Erreur renvoi email vérification:', error);
      return {
        success: false,
        message: error.response?.data?.message || 'Erreur lors du renvoi de l\'email'
      };
    }
  }

  // Obtenir les informations de session
  getSessionInfo() {
    return {
      isAuthenticated: this.isAuthenticated(),
      user: this.getCurrentUser(),
      token: this.getToken(),
      isTokenExpired: this.isTokenExpired()
    };
  }

  // Middleware pour vérifier l'auth avant certaines actions
  requireAuth() {
    if (!this.isAuthenticated()) {
      throw new Error('Authentification requise');
    }
    
    if (this.isTokenExpired()) {
      throw new Error('Session expirée, veuillez vous reconnecter');
    }
    
    return true;
  }
}

// Créer une instance unique du service
const authService = new AuthService();

export default authService;