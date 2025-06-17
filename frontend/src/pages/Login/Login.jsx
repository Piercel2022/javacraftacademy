import React, { useState, useContext } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { AuthContext } from '../../context/AuthContext';
import authService from '../../services/authService'; // Fixed: removed extra space
import Button from '../../components/common/Button';
import Loading from '../../components/common/Loading';
import Alert from '../../components/notification/Alert';
import styles from './Login.module.css';

const Login = () => {
  const [formData, setFormData] = useState({
    email: '',
    password: ''
  });
  const [errors, setErrors] = useState({});
  const [loading, setLoading] = useState(false);
  const [alert, setAlert] = useState({ show: false, message: '', type: '' });
  
  const { login } = useContext(AuthContext);
  const navigate = useNavigate();

  // Gestion des changements dans les champs du formulaire
  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
    // Effacer l'erreur du champ modifié
    if (errors[name]) {
      setErrors(prev => ({
        ...prev,
        [name]: ''
      }));
    }
  };

  // Validation du formulaire
  const validateForm = () => {
    const newErrors = {};
    
    if (!formData.email) {
      newErrors.email = 'L\'email est requis';
    } else if (!/\S+@\S+\.\S+/.test(formData.email)) {
      newErrors.email = 'Format d\'email invalide';
    }
    
    if (!formData.password) {
      newErrors.password = 'Le mot de passe est requis';
    } else if (formData.password.length < 6) {
      newErrors.password = 'Le mot de passe doit contenir au moins 6 caractères';
    }
    
    return newErrors;
  };

  // Connexion classique avec email/password
  const handleSubmit = async (e) => {
    e.preventDefault();
    
    const newErrors = validateForm();
    if (Object.keys(newErrors).length > 0) {
      setErrors(newErrors);
      return;
    }

    setLoading(true);
    setErrors({});

    try {
      const response = await authService.login(formData.email, formData.password);
      
      if (response.success) {
        login(response.user, response.token);
        setAlert({
          show: true,
          message: 'Connexion réussie ! Redirection...',
          type: 'success'
        });
        
        setTimeout(() => {
          navigate('/dashboard');
        }, 1500);
      } else {
        setAlert({
          show: true,
          message: response.message || 'Erreur lors de la connexion',
          type: 'error'
        });
      }
    } catch (error) {
      console.error('Erreur de connexion:', error);
      setAlert({
        show: true,
        message: 'Une erreur est survenue. Veuillez réessayer.',
        type: 'error'
      });
    } finally {
      setLoading(false);
    }
  };

  // Connexion avec Google
  const handleGoogleLogin = async () => {
    setLoading(true);
    try {
      // Méthode 1: Redirection vers l'URL d'authentification Google
      /*
      const googleAuthUrl = `${process.env.REACT_APP_API_URL}/auth/google`;
      window.location.href = googleAuthUrl;
      */
      // Méthode 2: Utilisation de Google OAuth2 (si vous utilisez la librairie Google)
      
      const response = await authService.googleLogin();
      if (response.success) {
        login(response.user, response.token);
        navigate('/dashboard');
      }
      
    } catch (error) {
      console.error('Erreur connexion Google:', error);
      setAlert({
        show: true,
        message: 'Erreur lors de la connexion avec Google',
        type: 'error'
      });
      setLoading(false);
    }
  };

  // Connexion avec GitHub
  const handleGitHubLogin = async () => {
    setLoading(true);
    try {
      // Méthode 1: Redirection vers l'URL d'authentification GitHub
      /*
      const githubAuthUrl = `${process.env.REACT_APP_API_URL}/auth/github`;
      window.location.href = githubAuthUrl;
      */
      // Méthode 2: Utilisation de l'API GitHub OAuth (si vous gérez côté client)
      
      const response = await authService.githubLogin();
      if (response.success) {
        login(response.user, response.token);
        navigate('/dashboard');
      }
      
    } catch (error) {
      console.error('Erreur connexion GitHub:', error);
      setAlert({
        show: true,
        message: 'Erreur lors de la connexion avec GitHub',
        type: 'error'
      });
      setLoading(false);
    }
  };

  return (
    <div className={styles.loginContainer}>
      <div className={styles.loginCard}>
        <div className={styles.loginHeader}>
          <h1 className={styles.title}>JavaCraft Academy</h1>
          <p className={styles.subtitle}>Connectez-vous à votre compte</p>
        </div>

        {alert.show && (
          <Alert
            type={alert.type}
            message={alert.message}
            onClose={() => setAlert({ show: false, message: '', type: '' })}
          />
        )}

        <form onSubmit={handleSubmit} className={styles.loginForm}>
          <div className={styles.formGroup}>
            <label htmlFor="email" className={styles.label}>
              Email
            </label>
            <input
              type="email"
              id="email"
              name="email"
              value={formData.email}
              onChange={handleChange}
              className={`${styles.input} ${errors.email ? styles.inputError : ''}`}
              placeholder="votre@email.com"
              disabled={loading}
            />
            {errors.email && (
              <span className={styles.errorMessage}>{errors.email}</span>
            )}
          </div>

          <div className={styles.formGroup}>
            <label htmlFor="password" className={styles.label}>
              Mot de passe
            </label>
            <input
              type="password"
              id="password"
              name="password"
              value={formData.password}
              onChange={handleChange}
              className={`${styles.input} ${errors.password ? styles.inputError : ''}`}
              placeholder="••••••••"
              disabled={loading}
            />
            {errors.password && (
              <span className={styles.errorMessage}>{errors.password}</span>
            )}
          </div>

          <div className={styles.formOptions}>
            <Link to="/forgot-password" className={styles.forgotPassword}>
              Mot de passe oublié ?
            </Link>
          </div>

          <Button
            type="submit"
            className={styles.loginButton}
            disabled={loading}
            fullWidth
          >
            {loading ? <Loading size="small" /> : 'Se connecter'}
          </Button>
        </form>

        <div className={styles.divider}>
          <span className={styles.dividerText}>ou</span>
        </div>

        <div className={styles.socialLogin}>
          <button
            type="button"
            onClick={handleGoogleLogin}
            className={`${styles.socialButton} ${styles.googleButton}`}
            disabled={loading}
          >
            <svg className={styles.socialIcon} viewBox="0 0 24 24">
              <path fill="#4285f4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/>
              <path fill="#34a853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
              <path fill="#fbbc05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"/>
              <path fill="#ea4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/>
            </svg>
            Continuer avec Google
          </button>

          <button
            type="button"
            onClick={handleGitHubLogin}
            className={`${styles.socialButton} ${styles.githubButton}`}
            disabled={loading}
          >
            <svg className={styles.socialIcon} viewBox="0 0 24 24" fill="currentColor">
              <path d="M12 0c-6.626 0-12 5.373-12 12 0 5.302 3.438 9.8 8.207 11.387.599.111.793-.261.793-.577v-2.234c-3.338.726-4.033-1.416-4.033-1.416-.546-1.387-1.333-1.756-1.333-1.756-1.089-.745.083-.729.083-.729 1.205.084 1.839 1.237 1.839 1.237 1.07 1.834 2.807 1.304 3.492.997.107-.775.418-1.305.762-1.604-2.665-.305-5.467-1.334-5.467-5.931 0-1.311.469-2.381 1.236-3.221-.124-.303-.535-1.524.117-3.176 0 0 1.008-.322 3.301 1.23.957-.266 1.983-.399 3.003-.404 1.02.005 2.047.138 3.006.404 2.291-1.552 3.297-1.23 3.297-1.23.653 1.653.242 2.874.118 3.176.77.84 1.235 1.911 1.235 3.221 0 4.609-2.807 5.624-5.479 5.921.43.372.823 1.102.823 2.222v3.293c0 .319.192.694.801.576 4.765-1.589 8.199-6.086 8.199-11.386 0-6.627-5.373-12-12-12z"/>
            </svg>
            Continuer avec GitHub
          </button>
        </div>

        <div className={styles.registerLink}>
          <p>
            Vous n'avez pas de compte ?{' '}
            <Link to="/register" className={styles.link}>
              S'inscrire
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
};

export default Login;