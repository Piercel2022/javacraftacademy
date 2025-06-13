// src/pages/Login/Login.jsx
import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import styles from './Login.module.css';

const Login = () => {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    email: '',
    password: '',
    rememberMe: false
  });
  const [errors, setErrors] = useState({});
  const [isLoading, setIsLoading] = useState(false);
  const [showPassword, setShowPassword] = useState(false);

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value
    }));
    
    // Clear error when user starts typing
    if (errors[name]) {
      setErrors(prev => ({
        ...prev,
        [name]: ''
      }));
    }
  };

  const validateForm = () => {
    const newErrors = {};

    if (!formData.email) {
      newErrors.email = 'L\'email est requis';
    } else if (!/\S+@\S+\.\S+/.test(formData.email)) {
      newErrors.email = 'Veuillez entrer un email valide';
    }

    if (!formData.password) {
      newErrors.password = 'Le mot de passe est requis';
    } else if (formData.password.length < 6) {
      newErrors.password = 'Le mot de passe doit contenir au moins 6 caractères';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!validateForm()) return;

    setIsLoading(true);
    
    try {
      // Simuler un appel API
      await new Promise(resolve => setTimeout(resolve, 1500));
      
      // En cas de succès, rediriger vers le dashboard
      navigate('/dashboard');
    } catch (error) {
      setErrors({ general: 'Erreur de connexion. Veuillez réessayer.' });
    } finally {
      setIsLoading(false);
    }
  };

  const handleSocialLogin = (provider) => {
    console.log(`Connexion avec ${provider}`);
    // Logique de connexion sociale à implémenter
  };

  return (
    <div className={styles.loginPage}>
      <div className={styles.loginContainer}>
        <div className={styles.loginCard}>
          <div className={styles.loginHeader}>
            <h1 className={styles.title}>Bon retour !</h1>
            <p className={styles.subtitle}>
              Connectez-vous pour continuer votre apprentissage Java
            </p>
          </div>

          {errors.general && (
            <div className={styles.errorAlert}>
              <span className={styles.errorIcon}>⚠️</span>
              {errors.general}
            </div>
          )}

          <form onSubmit={handleSubmit} className={styles.loginForm}>
            <div className={styles.formGroup}>
              <label htmlFor="email" className={styles.label}>
                Adresse email
              </label>
              <div className={styles.inputWrapper}>
                <input
                  type="email"
                  id="email"
                  name="email"
                  value={formData.email}
                  onChange={handleChange}
                  className={`${styles.input} ${errors.email ? styles.inputError : ''}`}
                  placeholder="votre@email.com"
                  autoComplete="email"
                />
                <span className={styles.inputIcon}>📧</span>
              </div>
              {errors.email && (
                <span className={styles.errorText}>{errors.email}</span>
              )}
            </div>

            <div className={styles.formGroup}>
              <label htmlFor="password" className={styles.label}>
                Mot de passe
              </label>
              <div className={styles.inputWrapper}>
                <input
                  type={showPassword ? 'text' : 'password'}
                  id="password"
                  name="password"
                  value={formData.password}
                  onChange={handleChange}
                  className={`${styles.input} ${errors.password ? styles.inputError : ''}`}
                  placeholder="Votre mot de passe"
                  autoComplete="current-password"
                />
                <button
                  type="button"
                  className={styles.passwordToggle}
                  onClick={() => setShowPassword(!showPassword)}
                >
                  {showPassword ? '🙈' : '👁️'}
                </button>
              </div>
              {errors.password && (
                <span className={styles.errorText}>{errors.password}</span>
              )}
            </div>

            <div className={styles.formOptions}>
              <label className={styles.checkboxLabel}>
                <input
                  type="checkbox"
                  name="rememberMe"
                  checked={formData.rememberMe}
                  onChange={handleChange}
                  className={styles.checkbox}
                />
                <span className={styles.checkboxCustom}></span>
                Se souvenir de moi
              </label>
              
              <Link to="/forgot-password" className={styles.forgotLink}>
                Mot de passe oublié ?
              </Link>
            </div>

            <button
              type="submit"
              disabled={isLoading}
              className={`${styles.submitButton} ${isLoading ? styles.loading : ''}`}
            >
              {isLoading ? (
                <>
                  <span className={styles.spinner}></span>
                  Connexion...
                </>
              ) : (
                'Se connecter'
              )}
            </button>
          </form>

          <div className={styles.divider}>
            <span>ou</span>
          </div>

          <div className={styles.socialLogin}>
            <button
              type="button"
              onClick={() => handleSocialLogin('Google')}
              className={styles.socialButton}
            >
              <img src="/api/placeholder/20/20" alt="Google" className={styles.socialIcon} />
              Continuer avec Google
            </button>
            
            <button
              type="button"
              onClick={() => handleSocialLogin('GitHub')}
              className={styles.socialButton}
            >
              <span className={styles.socialIcon}>⚫</span>
              Continuer avec GitHub
            </button>
          </div>

          <div className={styles.signupPrompt}>
            <p>
              Vous n'avez pas encore de compte ?{' '}
              <Link to="/register" className={styles.signupLink}>
                Créer un compte
              </Link>
            </p>
          </div>
        </div>

        <div className={styles.loginGraphic}>
          <div className={styles.codeAnimation}>
            <div className={styles.codeBlock}>
              <div className={styles.codeLine}>
                <span className={styles.keyword}>public</span>
                <span className={styles.className}>class</span>
                <span className={styles.identifier}>JavaCraft</span>
                <span className={styles.bracket}>{'{'}</span>
              </div>
              <div className={styles.codeLine}>
                <span className={styles.indent}>    </span>
                <span className={styles.keyword}>public</span>
                <span className={styles.keyword}>static</span>
                <span className={styles.keyword}>void</span>
                <span className={styles.method}>main</span>
                <span className={styles.bracket}>() {'{'}</span>
              </div>
              <div className={styles.codeLine}>
                <span className={styles.indent}>        </span>
                <span className={styles.identifier}>System</span>
                <span className={styles.dot}>.</span>
                <span className={styles.method}>out</span>
                <span className={styles.dot}>.</span>
                <span className={styles.method}>println</span>
                <span className={styles.bracket}>(</span>
                <span className={styles.string}>"Bienvenue !"</span>
                <span className={styles.bracket}>);</span>
              </div>
              <div className={styles.codeLine}>
                <span className={styles.indent}>    </span>
                <span className={styles.bracket}>{'}'}</span>
              </div>
              <div className={styles.codeLine}>
                <span className={styles.bracket}>{'}'}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Login;
