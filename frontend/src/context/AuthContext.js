// src/context/AuthContext.js
import React, { createContext, useState, useEffect } from 'react';
import authService from '../services/authService';

export const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(null);
  const [isLoading, setIsLoading] = useState(true);

  // État calculé
  const isAuthenticated = Boolean(user && token);

  // Initialisation au chargement
  useEffect(() => {
    const initAuth = async () => {
      try {
        const storedUser = authService.getCurrentUser();
        const storedToken = authService.getToken();
        
        if (storedUser && storedToken) {
          setUser(storedUser);
          setToken(storedToken);
        }
      } catch (error) {
        console.error('Erreur lors de l\'initialisation de l\'authentification:', error);
      } finally {
        setIsLoading(false);
      }
    };

    initAuth();
  }, []);

  // Fonction de connexion
  const login = (userData, userToken) => {
    setUser(userData);
    setToken(userToken);
    authService.setAuthData(userData, userToken);
  };

  // Fonction de connexion pour utilisateur de développement
  const devUser = () => {
    const mockUser = {
      id: 'dev-001',
      email: 'dev@example.com',
      name: 'Développeur',
      role: 'admin',
      isDevUser: true
    };
    const mockToken = 'dev-token-' + Date.now();
    
    login(mockUser, mockToken);
  };

  // Fonction de déconnexion
  const logout = () => {
    setUser(null);
    setToken(null);
    authService.logout();
  };

  const value = {
    user,
    token,
    isAuthenticated,
    isLoading,
    login,
    devUser,
    logout
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
};