// ForgotPassword.jsx
import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import { useNotification } from '../../hooks/useNotification';
import Button from '../common/Button';
import styles from './ForgotPassword.module.css';

const ForgotPassword = ({ onSuccess }) => {
  const [email, setEmail] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [isEmailSent, setIsEmailSent] = useState(false);
  const [errors, setErrors] = useState({});
  const [countdown, setCountdown] = useState(0);
  
  const { forgotPassword } = useAuth();
  const { showNotification } = useNotification();

  const validateEmail = (email) => {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    // Validation
    if (!email.trim()) {
      setErrors({ email: 'L\'adresse email est requise' });
      return;
    }
    
    if (!validateEmail(email)) {
      setErrors({ email: 'L\'adresse email n\'est pas valide' });
      return;
    }

    setErrors({});
    setIsLoading(true);

    try {
      await forgotPassword(email);
      setIsEmailSent(true);
      setCountdown(60); // 60 seconds countdown
      
      // Start countdown
      const timer = setInterval(() => {
        setCountdown((prev) => {
          if (prev <= 1) {
            clearInterval(timer);
            return 0;
          }
          return prev - 1;
        });
      }, 1000);

      showNotification(
        'Instructions de réinitialisation envoyées par email',
        'success'
      );
      
      if (onSuccess) {
        onSuccess();
      }
    } catch (error) {
      const errorMessage = 
        error.response?.data?.message || 
        'Erreur lors de l\'envoi de l\'email de réinitialisation';
      
      setErrors({ submit: errorMessage });
      showNotification(errorMessage, 'error');
    } finally {
      setIsLoading(false);
    }
  };

  const handleResendEmail = async () => {
    if (countdown > 0) return;
    
    setIsLoading(true);
    try {
      await forgotPassword(email);
      setCountdown(60);
      
      const timer = setInterval(() => {
        setCountdown((prev) => {
          if (prev <= 1) {
            clearInterval(timer);
            return 0;
          }
          return prev - 1;
        });
      }, 1000);

      showNotification('Email renvoyé avec succès', 'success');
    } catch (error) {
      const errorMessage = 
        error.response?.data?.message || 
        'Erreur lors du renvoi de l\'email';
      
      setErrors({ submit: errorMessage });
      showNotification(errorMessage, 'error');
    } finally {
      setIsLoading(false);
    }
  };

  const resetForm = () => {
    setEmail('');
    setIsEmailSent(false);
    setErrors({});
    setCountdown(0);
  };

  if (isEmailSent) {
    return (
      <div className={styles.forgotPasswordContainer}>
        <div className={styles.forgotPasswordCard}>
          <div className={styles.successIcon}>
            <svg
              width="64"
              height="64"
              viewBox="0 0 24 24"
              fill="none"
              xmlns="http://www.w3.org/2000/svg"
            >
              <circle cx="12" cy="12" r="10" stroke="#22C55E" strokeWidth="2"/>
              <path d="m9 12 2 2 4-4" stroke="#22C55E" strokeWidth="2"/>
            </svg>
          </div>
          
          <h2 className={styles.title}>Email envoyé !</h2>
          
          <p className={styles.description}>
            Nous avons envoyé les instructions de réinitialisation à{' '}
            <strong>{email}</strong>
          </p>
          
          <p className={styles.instruction}>
            Vérifiez votre boîte de réception et suivez les instructions pour 
            réinitialiser votre mot de passe.
          </p>

          <div className={styles.actions}>
            <Button
              onClick={handleResendEmail}
              variant="secondary"
              disabled={countdown > 0 || isLoading}
              className={styles.resendButton}
            >
              {countdown > 0 
                ? `Renvoyer dans ${countdown}s`
                : isLoading 
                  ? 'Envoi...'
                  : 'Renvoyer l\'email'
              }
            </Button>
            
            <Button
              onClick={resetForm}
              variant="tertiary"
              className={styles.backButton}
            >
              Utiliser une autre adresse
            </Button>
          </div>

          <div className={styles.footer}>
            <Link to="/login" className={styles.backToLogin}>
              ← Retour à la connexion
            </Link>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className={styles.forgotPasswordContainer}>
      <div className={styles.forgotPasswordCard}>
        <div className={styles.header}>
          <h2 className={styles.title}>Mot de passe oublié ?</h2>
          <p className={styles.subtitle}>
            Entrez votre adresse email et nous vous enverrons les instructions 
            pour réinitialiser votre mot de passe.
          </p>
        </div>

        <form onSubmit={handleSubmit} className={styles.form}>
          <div className={styles.formGroup}>
            <label htmlFor="email" className={styles.label}>
              Adresse email
            </label>
            <input
              type="email"
              id="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className={`${styles.input} ${errors.email ? styles.inputError : ''}`}
              placeholder="votre@email.com"
              disabled={isLoading}
              autoComplete="email"
              autoFocus
            />
            {errors.email && (
              <span className={styles.errorMessage}>{errors.email}</span>
            )}
          </div>

          {errors.submit && (
            <div className={styles.submitError}>
              {errors.submit}
            </div>
          )}

          <Button
            type="submit"
            loading={isLoading}
            disabled={isLoading}
            className={styles.submitButton}
          >
            {isLoading ? 'Envoi en cours...' : 'Envoyer les instructions'}
          </Button>
        </form>

        <div className={styles.footer}>
          <Link to="/login" className={styles.backToLogin}>
            ← Retour à la connexion
          </Link>
          
          <div className={styles.registerLink}>
            Pas encore de compte ?{' '}
            <Link to="/register">S'inscrire</Link>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ForgotPassword;