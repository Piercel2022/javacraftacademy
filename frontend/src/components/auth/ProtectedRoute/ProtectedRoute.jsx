import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import LoadingSpinner from '../ui/LoadingSpinner';
import styles from './ProtectedRoute.module.css';

const ProtectedRoute = ({ 
  children, 
  requiredRole = null, 
  redirectTo = '/login',
  requireEmailVerification = false 
}) => {
  const { user, loading, isAuthenticated } = useAuth();
  const location = useLocation();

  // Afficher le spinner pendant le chargement
  if (loading) {
    return (
      <div className={styles.loadingContainer}>
        <LoadingSpinner size="large" />
        <p className={styles.loadingText}>Vérification de l'authentification...</p>
      </div>
    );
  }

  // Rediriger si non authentifié
  if (!isAuthenticated) {
    return (
      <Navigate 
        to={redirectTo} 
        state={{ from: location }} 
        replace 
      />
    );
  }

  // Vérifier si l'email est vérifié (si requis)
  if (requireEmailVerification && user && !user.emailVerified) {
    return (
      <Navigate 
        to="/verify-email" 
        state={{ from: location }} 
        replace 
      />
    );
  }

  // Vérifier le rôle requis
  if (requiredRole && user && user.role !== requiredRole) {
    return (
      <div className={styles.accessDenied}>
        <div className={styles.accessDeniedContent}>
          <h2>Accès refusé</h2>
          <p>Vous n'avez pas les permissions nécessaires pour accéder à cette page.</p>
          <p>Rôle requis: <strong>{requiredRole}</strong></p>
          <p>Votre rôle: <strong>{user.role || 'Non défini'}</strong></p>
          <button 
            onClick={() => window.history.back()}
            className={styles.backButton}
          >
            Retour
          </button>
        </div>
      </div>
    );
  }

  // Afficher le contenu protégé
  return <>{children}</>;
};

// Composant pour les routes admin uniquement
export const AdminRoute = ({ children, ...props }) => (
  <ProtectedRoute requiredRole="admin" {...props}>
    {children}
  </ProtectedRoute>
);

// Composant pour les routes enseignant uniquement
export const TeacherRoute = ({ children, ...props }) => (
  <ProtectedRoute requiredRole="teacher" {...props}>
    {children}
  </ProtectedRoute>
);

// Composant pour les routes étudiant uniquement
export const StudentRoute = ({ children, ...props }) => (
  <ProtectedRoute requiredRole="student" {...props}>
    {children}
  </ProtectedRoute>
);

// Composant pour les routes nécessitant une vérification d'email
export const EmailVerifiedRoute = ({ children, ...props }) => (
  <ProtectedRoute requireEmailVerification={true} {...props}>
    {children}
  </ProtectedRoute>
);

export default ProtectedRoute;