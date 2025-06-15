import React, { useState, useContext, useEffect } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { AuthContext } from '../../context/AuthContext';
import { NotificationContext } from '../../context/NotificationContext';
import LoginForm from '../../components/auth/LoginForm';
import Button from '../../components/common/Button';
import Loading from '../../components/common/Loading';
import styles from './Login.module.css';

const Login = () => {
  const [isLoading, setIsLoading] = useState(false);
  const [rememberMe, setRememberMe] = useState(false);
  const [showForgotPassword, setShowForgotPassword] = useState(false);
  
  const { login, isAuthenticated, error: authError } = useContext(AuthContext);
  const { showNotification } = useContext(NotificationContext);
  const navigate = useNavigate();
  const location = useLocation();

  // Redirection si déjà connecté
  useEffect(() => {
    if (isAuthenticated) {
      const from = location.state?.from?.pathname || '/dashboard';
      navigate(from, { replace: true });
    }
  }, [isAuthenticated, navigate, location]);

  // Gestion de la soumission du formulaire
  const handleLogin = async (credentials) => {
    setIsLoading(true);
    try {
      await login(credentials, rememberMe);
      showNotification('Connexion réussie !', 'success');
      
      // Redirection vers la page demandée ou dashboard
      const from = location.state?.from?.pathname || '/dashboard';
      navigate(from, { replace: true });
    } catch (error) {
      showNotification(
        error.message || 'Erreur lors de la connexion',
        'error'
      );
    } finally {
      setIsLoading(false);
    }
  };

  // Gestion de la connexion avec Google (OAuth)
  const handleGoogleLogin = () => {
    // Redirection vers l'endpoint OAuth Google
    window.location.href = `${process.env.REACT_APP_API_URL}/auth/google`;
  };

  // Gestion de la connexion avec GitHub (OAuth)
  const handleGithubLogin = () => {
    // Redirection vers l'endpoint OAuth GitHub
    window.location.href = `${process.env.REACT_APP_API_URL}/auth/github`;
  };

  if (isLoading) {
    return (
      <div className={styles.loadingContainer}>
        <Loading />
      </div>
    );
  }

  return (
    <div className={styles.loginPage}>
      <div className={styles.loginContainer}>
        <div className={styles.loginHeader}>
          <div className={styles.logoSection}>
            <img 
              src="/assets/images/logo.png" 
              alt="JavaCraft Academy" 
              className={styles.logo}
            />
            <h1 className={styles.title}>JavaCraft Academy</h1>
          </div>
          <h2 className={styles.subtitle}>Connectez-vous à votre compte</h2>
          <p className={styles.description}>
            Continuez votre apprentissage Java et accédez à tous vos cours
          </p>
        </div>

        <div className={styles.loginContent}>
          {/* Boutons de connexion sociale */}
          <div className={styles.socialLogin}>
            <Button
              variant="outline"
              onClick={handleGoogleLogin}
              className={styles.socialButton}
              disabled={isLoading}
            >
              <img 
                src="/assets/images/icons/google.svg" 
                alt="Google" 
                className={styles.socialIcon}
              />
              Continuer avec Google
            </Button>
            
            <Button
              variant="outline"
              onClick={handleGithubLogin}
              className={styles.socialButton}
              disabled={isLoading}
            >
              <img 
                src="/assets/images/icons/github.svg" 
                alt="GitHub" 
                className={styles.socialIcon}
              />
              Continuer avec GitHub
            </Button>
          </div>

          <div className={styles.divider}>
            <span className={styles.dividerText}>ou</span>
          </div>

          {/* Formulaire de connexion */}
          <LoginForm
            onSubmit={handleLogin}
            isLoading={isLoading}
            error={authError}
          />

          {/* Options additionnelles */}
          <div className={styles.loginOptions}>
            <label className={styles.rememberMe}>
              <input
                type="checkbox"
                checked={rememberMe}
                onChange={(e) => setRememberMe(e.target.checked)}
                className={styles.checkbox}
              />
              <span className={styles.checkboxLabel}>Se souvenir de moi</span>
            </label>
            
            <button
              type="button"
              onClick={() => setShowForgotPassword(true)}
              className={styles.forgotPassword}
            >
              Mot de passe oublié ?
            </button>
          </div>

          {/* Lien vers l'inscription */}
          <div className={styles.signupLink}>
            <p>
              Vous n'avez pas de compte ?{' '}
              <Link to="/register" className={styles.link}>
                Créer un compte
              </Link>
            </p>
          </div>
        </div>

        {/* Section informative */}
        <div className={styles.loginFooter}>
          <div className={styles.features}>
            <div className={styles.feature}>
              <div className={styles.featureIcon}>📚</div>
              <span>Cours interactifs</span>
            </div>
            <div className={styles.feature}>
              <div className={styles.featureIcon}>💻</div>
              <span>Éditeur de code intégré</span>
            </div>
            <div className={styles.feature}>
              <div className={styles.featureIcon}>🏆</div>
              <span>Suivi de progression</span>
            </div>
          </div>
        </div>
      </div>

      {/* Arrière-plan décoratif */}
      <div className={styles.backgroundPattern}>
        <div className={styles.codePattern}>
          <pre className={styles.codeSnippet}>
            {`public class Welcome {
    public static void main(String[] args) {
        System.out.println("Bienvenue sur JavaCraft Academy!");
    }
}`}
          </pre>
        </div>
      </div>
    </div>
  );
};

export default Login;