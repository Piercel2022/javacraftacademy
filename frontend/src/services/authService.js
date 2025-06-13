// src/services/authService.js
/** const authService = {
  // Récupérer l'utilisateur actuel depuis le localStorage
  getCurrentUser: () => {
    try {
      const user = localStorage.getItem('user');
      return user ? JSON.parse(user) : null;
    } catch (error) {
      console.error('Erreur lors de la récupération de l\'utilisateur:', error);
      return null;
    }
  },

  // Récupérer le token depuis le localStorage
  getToken: () => {
    try {
      return localStorage.getItem('token');
    } catch (error) {
      console.error('Erreur lors de la récupération du token:', error);
      return null;
    }
  },

  // Sauvegarder les données d'authentification
  setAuthData: (user, token) => {
    try {
      localStorage.setItem('user', JSON.stringify(user));
      localStorage.setItem('token', token);
    } catch (error) {
      console.error('Erreur lors de la sauvegarde des données d\'authentification:', error);
    }
  },

  // Connexion
  login: async (credentials) => {
    try {
      // Simulation d'un appel API - remplacez par votre vraie API
      const response = await fetch('/api/auth/login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(credentials),
      });

      const data = await response.json();
      
      if (response.ok) {
        return {
          success: true,
          user: data.user,
          token: data.token,
        };
      } else {
        return {
          success: false,
          message: data.message || 'Erreur de connexion',
        };
      }
    } catch (error) {
      console.error('Erreur lors de la connexion:', error);
      return {
        success: false,
        message: 'Erreur de connexion',
      };
    }
  },

  // Inscription
  register: async (userData) => {
    try {
      const response = await fetch('/api/auth/register', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(userData),
      });

      const data = await response.json();
      
      if (response.ok) {
        return {
          success: true,
          user: data.user,
          token: data.token,
        };
      } else {
        return {
          success: false,
          message: data.message || 'Erreur lors de l\'inscription',
        };
      }
    } catch (error) {
      console.error('Erreur lors de l\'inscription:', error);
      return {
        success: false,
        message: 'Erreur lors de l\'inscription',
      };
    }
  },

  // Déconnexion
  logout: async () => {
    try {
      // Optionnel : appel API pour invalider le token côté serveur
      // await fetch('/api/auth/logout', { method: 'POST' });
      
      localStorage.removeItem('user');
      localStorage.removeItem('token');
    } catch (error) {
      console.error('Erreur lors de la déconnexion:', error);
    }
  },

  // Mot de passe oublié
  forgotPassword: async (email) => {
    try {
      const response = await fetch('/api/auth/forgot-password', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ email }),
      });

      const data = await response.json();
      
      return {
        success: response.ok,
        message: data.message,
      };
    } catch (error) {
      console.error('Erreur lors de l\'envoi de l\'email:', error);
      return {
        success: false,
        message: 'Erreur lors de l\'envoi de l\'email',
      };
    }
  },

  // Mise à jour du profil
  updateProfile: async (profileData) => {
    try {
      const token = authService.getToken();
      const response = await fetch('/api/auth/profile', {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`,
        },
        body: JSON.stringify(profileData),
      });

      const data = await response.json();
      
      if (response.ok) {
        return {
          success: true,
          user: data.user,
        };
      } else {
        return {
          success: false,
          message: data.message || 'Erreur lors de la mise à jour',
        };
      }
    } catch (error) {
      console.error('Erreur lors de la mise à jour du profil:', error);
      return {
        success: false,
        message: 'Erreur lors de la mise à jour du profil',
      };
    }
  },

  // Vérification de la validité du token
  verifyToken: async (token) => {
    try {
      const response = await fetch('/api/auth/verify-token', {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`,
        },
      });

      return {
        valid: response.ok,
      };
    } catch (error) {
      console.error('Erreur lors de la vérification du token:', error);
      return {
        valid: false,
      };
    }
  },

  // Rafraîchir le token
  refreshToken: async () => {
    try {
      const token = authService.getToken();
      const response = await fetch('/api/auth/refresh-token', {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`,
        },
      });

      const data = await response.json();
      
      if (response.ok) {
        return {
          success: true,
          user: data.user,
          token: data.token,
        };
      } else {
        return {
          success: false,
        };
      }
    } catch (error) {
      console.error('Erreur lors du rafraîchissement du token:', error);
      return {
        success: false,
      };
    }
  },
};

export default authService;

*/
 // src/services/authService.js (version de simulation pour test)
const authService = {
  getCurrentUser: () => {
    try {
      const user = localStorage.getItem('user');
      return user ? JSON.parse(user) : null;
    } catch (error) {
      console.error('Erreur lors de la récupération de l\'utilisateur:', error);
      return null;
    }
  },

  getToken: () => {
    try {
      return localStorage.getItem('token');
    } catch (error) {
      console.error('Erreur lors de la récupération du token:', error);
      return null;
    }
  },

  setAuthData: (user, token) => {
    try {
      localStorage.setItem('user', JSON.stringify(user));
      localStorage.setItem('token', token);
    } catch (error) {
      console.error('Erreur lors de la sauvegarde des données d\'authentification:', error);
    }
  },

  // Simulation de connexion
  login: async (credentials) => {
    // Simulation d'un délai réseau
    await new Promise(resolve => setTimeout(resolve, 1000));
    
    // Simulation de validation simple
    if (credentials.email && credentials.password) {
      const mockUser = {
        id: 1,
        email: credentials.email,
        name: 'Utilisateur Test',
        avatar: null,
      };
      const mockToken = 'mock-jwt-token-' + Date.now();
      
      return {
        success: true,
        user: mockUser,
        token: mockToken,
      };
    } else {
      return {
        success: false,
        message: 'Email ou mot de passe incorrect',
      };
    }
  },

  register: async (userData) => {
    await new Promise(resolve => setTimeout(resolve, 1000));
    
    if (userData.email && userData.password && userData.name) {
      const mockUser = {
        id: 2,
        email: userData.email,
        name: userData.name,
        avatar: null,
      };
      const mockToken = 'mock-jwt-token-' + Date.now();
      
      return {
        success: true,
        user: mockUser,
        token: mockToken,
      };
    } else {
      return {
        success: false,
        message: 'Données d\'inscription invalides',
      };
    }
  },

  logout: async () => {
    localStorage.removeItem('user');
    localStorage.removeItem('token');
  },

  forgotPassword: async (email) => {
    await new Promise(resolve => setTimeout(resolve, 1000));
    
    return {
      success: true,
      message: 'Email de réinitialisation envoyé (simulation)',
    };
  },

  updateProfile: async (profileData) => {
    await new Promise(resolve => setTimeout(resolve, 1000));
    
    const currentUser = authService.getCurrentUser();
    if (currentUser) {
      const updatedUser = { ...currentUser, ...profileData };
      return {
        success: true,
        user: updatedUser,
      };
    } else {
      return {
        success: false,
        message: 'Utilisateur non trouvé',
      };
    }
  },

  verifyToken: async (token) => {
    await new Promise(resolve => setTimeout(resolve, 500));
    
    return {
      valid: token && token.startsWith('mock-jwt-token'),
    };
  },

  refreshToken: async () => {
    await new Promise(resolve => setTimeout(resolve, 500));
    
    const currentUser = authService.getCurrentUser();
    if (currentUser) {
      const newToken = 'mock-jwt-token-refreshed-' + Date.now();
      return {
        success: true,
        user: currentUser,
        token: newToken,
      };
    } else {
      return {
        success: false,
      };
    }
  },
};

export default authService;
 