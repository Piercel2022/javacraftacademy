// RegisterForm.jsx
import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import { useNotification } from '../../hooks/useNotification';
import Button from '../common/Button';
import styles from './RegisterForm.module.css';

const RegisterForm = ({ onSuccess, redirectTo = '/dashboard' }) => {
  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    email: '',
    password: '',
    confirmPassword: '',
    acceptTerms: false
  });
  const [errors, setErrors] = useState({});
  const [isLoading, setIsLoading] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);

  const { register } = useAuth();
  const { showNotification } = useNotification();

  const validateForm = () => {
    const newErrors = {};

    // Validation prénom
    if (!formData.firstName.trim()) {
      newErrors.firstName = 'Le prénom est requis';
    } else if (formData.firstName.trim().length < 2) {
      newErrors.firstName = 'Le prénom doit contenir au moins 2 caractères';
    }

    // Validation nom
    if (!formData.lastName.trim()) {
      newErrors.lastName = 'Le nom est requis';
    } else if (formData.lastName.trim().length < 2) {
      newErrors.lastName = 'Le nom doit contenir au moins 2 caractères';
    }

    // Validation email
    if (!formData.email) {
      newErrors.email = 'L\'email est requis';
    } else if (!/\S+@\S+\.\S+/.test(formData.email)) {
      newErrors.email = 'L\'email n\'est pas valide';
    }

    // Validation mot de passe
    if (!formData.password) {
      newErrors.password = 'Le mot de passe est requis';
    } else if (formData.password.length < 8) {
      newErrors.password = 'Le mot de passe doit contenir au moins 8 caractères';
    } else if (!/(?=.*[a-z])(?=.*[A-Z])(?=.*\d)/.test(formData.password)) {
      newErrors.password = 'Le mot de passe doit contenir au moins une minuscule, une majuscule et un chiffre';
    }

    // Validation confirmation mot de passe
    if (!formData.confirmPassword) {
      newErrors.confirmPassword = 'La confirmation du mot de passe est requise';
    } else if (formData.password !== formData.confirmPassword) {
      newErrors.confirmPassword = 'Les mots de passe ne correspondent pas';
    }

    // Validation acceptation des conditions
    if (!formData.acceptTerms) {
      newErrors.acceptTerms = 'Vous devez accepter les conditions d\'utilisation';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value
    }));

    // Effacer l'erreur quand l'utilisateur commence à taper
    if (errors[name]) {
      setErrors(prev => ({
        ...prev,
        [name]: ''
      }));
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!validateForm()) {
      return;
    }

    setIsLoading(true);

    try {
      await register({
        firstName: formData.firstName.trim(),
        lastName: formData.lastName.trim(),
        email: formData.email,
        password: formData.password
      });

      showNotification('Compte créé avec succès! Bienvenue!', 'success');
      
      if (onSuccess) {
        onSuccess();
      }
    } catch (error) {
      const errorMessage = error.response?.data?.message || 'Erreur lors de la création du compte';
      setErrors({ submit: errorMessage });
      showNotification(errorMessage, 'error');
    } finally {
      setIsLoading(false);
    }
  };

  const getPasswordStrength = (password) => {
    let strength = 0;
    if (password.length >= 8) strength++;
    if (/[a-z]/.test(password)) strength++;
    if (/[A-Z]/.test(password)) strength++;
    if (/\d/.test(password)) strength++;
    if (/[^A-Za-z0-9]/.test(password)) strength++;
    return strength;
  };

  const passwordStrength = getPasswordStrength(formData.password);
  const strengthLabels = ['Très faible', 'Faible', 'Moyen', 'Fort', 'Très fort'];
  const strengthColors = ['#e74c3c', '#e67e22', '#f39c12', '#27ae60', '#16a085'];

  return (
    <div className={styles.registerContainer}>
      <div className={styles.registerCard}>
        <div className={styles.registerHeader}>
          <h2 className={styles.registerTitle}>Créer un compte</h2>
          <p className={styles.registerSubtitle}>
            Rejoignez JavaCraft Academy et commencez votre apprentissage
          </p>
        </div>

        <form onSubmit={handleSubmit} className={styles.registerForm}>
          {errors.submit && (
            <div className={styles.errorAlert}>
              <svg className={styles.errorIcon} viewBox="0 0 20 20" fill="currentColor">
                <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clipRule="evenodd" />
              </svg>
              {errors.submit}
            </div>
          )}

          <div className={styles.nameGroup}>
            <div className={styles.formGroup}>
              <label htmlFor="firstName" className={styles.formLabel}>
                Prénom
              </label>
              <input
                type="text"
                id="firstName"
                name="firstName"
                value={formData.firstName}
                onChange={handleChange}
                className={`${styles.formInput} ${errors.firstName ? styles.inputError : ''}`}
                placeholder="John"
                disabled={isLoading}
              />
              {errors.firstName && (
                <span className={styles.errorMessage}>{errors.firstName}</span>
              )}
            </div>

            <div className={styles.formGroup}>
              <label htmlFor="lastName" className={styles.formLabel}>
                Nom
              </label>
              <input
                type="text"
                id="lastName"
                name="lastName"
                value={formData.lastName}
                onChange={handleChange}
                className={`${styles.formInput} ${errors.lastName ? styles.inputError : ''}`}
                placeholder="Doe"
                disabled={isLoading}
              />
              {errors.lastName && (
                <span className={styles.errorMessage}>{errors.lastName}</span>
              )}
            </div>
          </div>

          <div className={styles.formGroup}>
            <label htmlFor="email" className={styles.formLabel}>
              Adresse email
            </label>
            <input
              type="email"
              id="email"
              name="email"
              value={formData.email}
              onChange={handleChange}
              className={`${styles.formInput} ${errors.email ? styles.inputError : ''}`}
              placeholder="john.doe@example.com"
              disabled={isLoading}
            />
            {errors.email && (
              <span className={styles.errorMessage}>{errors.email}</span>
            )}
          </div>

          <div className={styles.formGroup}>
            <label htmlFor="password" className={styles.formLabel}>
              Mot de passe
            </label>
            <div className={styles.passwordInputGroup}>
              <input
                type={showPassword ? 'text' : 'password'}
                id="password"
                name="password"
                value={formData.password}
                onChange={handleChange}
                className={`${styles.formInput} ${errors.password ? styles.inputError : ''}`}
                placeholder="••••••••"
                disabled={isLoading}
              />
              <button
                type="button"
                className={styles.passwordToggle}
                onClick={() => setShowPassword(!showPassword)}
                disabled={isLoading}
              >
                {showPassword ? (
                  <svg className={styles.eyeIcon} viewBox="0 0 20 20" fill="currentColor">
                    <path fillRule="evenodd" d="M3.707 2.293a1 1 0 00-1.414 1.414l14 14a1 1 0 001.414-1.414l-1.473-1.473A10.014 10.014 0 0019.542 10C18.268 5.943 14.478 3 10 3a9.958 9.958 0 00-4.512 1.074l-1.78-1.781zm4.261 4.26l1.514 1.515a2.003 2.003 0 012.45 2.45l1.514 1.514a4 4 0 00-5.478-5.478z" clipRule="evenodd" />
                    <path d="M12.454 16.697L9.75 13.992a4 4 0 01-3.742-3.741L2.335 6.578A9.98 9.98 0 00.458 10c1.274 4.057 5.065 7 9.542 7 .847 0 1.669-.105 2.454-.303z" />
                  </svg>
                ) : (
                  <svg className={styles.eyeIcon} viewBox="0 0 20 20" fill="currentColor">
                    <path d="M10 12a2 2 0 100-4 2 2 0 000 4z" />
                    <path fillRule="evenodd" d="M.458 10C1.732 5.943 5.522 3 10 3s8.268 2.943 9.542 7c-1.274 4.057-5.064 7-9.542 7S1.732 14.057.458 10zM14 10a4 4 0 11-8 0 4 4 0 018 0z" clipRule="evenodd" />
                  </svg>
                )}
              </button>
            </div>
            {formData.password && (
              <div className={styles.passwordStrength}>
                <div className={styles.strengthBar}>
                  <div 
                    className={styles.strengthFill}
                    style={{
                      width: `${(passwordStrength / 5) * 100}%`,
                      backgroundColor: strengthColors[passwordStrength - 1] || strengthColors[0]
                    }}
                  />
                </div>
                <span className={styles.strengthLabel}>
                  {strengthLabels[passwordStrength - 1] || strengthLabels[0]}
                </span>
              </div>
            )}
            {errors.password && (
              <span className={styles.errorMessage}>{errors.password}</span>
            )}
          </div>

          <div className={styles.formGroup}>
            <label htmlFor="confirmPassword" className={styles.formLabel}>
              Confirmer le mot de passe
            </label>
            <div className={styles.passwordInputGroup}>
              <input
                type={showConfirmPassword ? 'text' : 'password'}
                id="confirmPassword"
                name="confirmPassword"
                value={formData.confirmPassword}
                onChange={handleChange}
                className={`${styles.formInput} ${errors.confirmPassword ? styles.inputError : ''}`}
                placeholder="••••••••"
                disabled={isLoading}
              />
              <button
                type="button"
                className={styles.passwordToggle}
                onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                disabled={isLoading}
              >
                {showConfirmPassword ? (
                  <svg className={styles.eyeIcon} viewBox="0 0 20 20" fill="currentColor">
                    <path fillRule="evenodd" d="M3.707 2.293a1 1 0 00-1.414 1.414l14 14a1 1 0 001.414-1.414l-1.473-1.473A10.014 10.014 0 0019.542 10C18.268 5.943 14.478 3 10 3a9.958 9.958 0 00-4.512 1.074l-1.78-1.781zm4.261 4.26l1.514 1.515a2.003 2.003 0 012.45 2.45l1.514 1.514a4 4 0 00-5.478-5.478z" clipRule="evenodd" />
                    <path d="M12.454 16.697L9.75 13.992a4 4 0 01-3.742-3.741L2.335 6.578A9.98 9.98 0 00.458 10c1.274 4.057 5.065 7 9.542 7 .847 0 1.669-.105 2.454-.303z" />
                  </svg>
                ) : (
                  <svg className={styles.eyeIcon} viewBox="0 0 20 20" fill="currentColor">
                    <path d="M10 12a2 2 0 100-4 2 2 0 000 4z" />
                    <path fillRule="evenodd" d="M.458 10C1.732 5.943 5.522 3 10 3s8.268 2.943 9.542 7c-1.274 4.057-5.064 7-9.542 7S1.732 14.057.458 10zM14 10a4 4 0 11-8 0 4 4 0 018 0z" clipRule="evenodd" />
                  </svg>
                )}
              </button>
            </div>
            {errors.confirmPassword && (
              <span className={styles.errorMessage}>{errors.confirmPassword}</span>
            )}
          </div>

          <div className={styles.formGroup}>
            <label className={styles.checkboxLabel}>
              <input
                type="checkbox"
                name="acceptTerms"
                checked={formData.acceptTerms}
                onChange={handleChange}
                className={styles.checkbox}
                disabled={isLoading}
              />
              <span className={styles.checkboxText}>
                J'accepte les{' '}
                <Link to="/terms" className={styles.link}>
                  conditions d'utilisation
                </Link>{' '}
                et la{' '}
                <Link to="/privacy" className={styles.link}>
                  politique de confidentialité
                </Link>
              </span>
            </label>
            {errors.acceptTerms && (
              <span className={styles.errorMessage}>{errors.acceptTerms}</span>
            )}
          </div>

          <Button
            type="submit"
            variant="primary"
            size="large"
            fullWidth
            loading={isLoading}
            disabled={isLoading}
          >
            {isLoading ? 'Création du compte...' : 'Créer mon compte'}
          </Button>
        </form>

        <div className={styles.registerFooter}>
          <p className={styles.loginPrompt}>
            Vous avez déjà un compte ?{' '}
            <Link to="/login" className={styles.loginLink}>
              Se connecter
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
};

export default RegisterForm;