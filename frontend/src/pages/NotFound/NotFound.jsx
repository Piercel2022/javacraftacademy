import React, { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import styles from './NotFound.module.css';

const NotFound = () => {
  const navigate = useNavigate();
  const [countdown, setCountdown] = useState(10);
  const [isAutoRedirect, setIsAutoRedirect] = useState(true);

  useEffect(() => {
    if (!isAutoRedirect) return;

    const timer = setInterval(() => {
      setCountdown((prev) => {
        if (prev <= 1) {
          navigate('/');
          return 0;
        }
        return prev - 1;
      });
    }, 1000);

    return () => clearInterval(timer);
  }, [navigate, isAutoRedirect]);

  const handleStopRedirect = () => {
    setIsAutoRedirect(false);
  };

  const handleGoBack = () => {
    if (window.history.length > 1) {
      navigate(-1);
    } else {
      navigate('/');
    }
  };

  return (
    <div className={styles.notFoundContainer}>
      <div className={styles.notFoundContent}>
        {/* Animated 404 */}
        <div className={styles.errorCode}>
          <span className={styles.digit}>4</span>
          <span className={styles.digit}>0</span>
          <span className={styles.digit}>4</span>
        </div>

        {/* Error Message */}
        <div className={styles.errorMessage}>
          <h1 className={styles.title}>Page introuvable</h1>
          <p className={styles.subtitle}>
            Oops ! La page que vous recherchez n'existe pas ou a été déplacée.
          </p>
        </div>

        {/* Illustrations */}
        <div className={styles.illustration}>
          <div className={styles.planet}>
            <div className={styles.ring}></div>
            <div className={styles.ring}></div>
            <div className={styles.ring}></div>
          </div>
          <div className={styles.astronaut}>
            <div className={styles.helmet}></div>
            <div className={styles.body}></div>
            <div className={styles.arm}></div>
            <div className={styles.leg}></div>
          </div>
        </div>

        {/* Suggestions */}
        <div className={styles.suggestions}>
          <h3 className={styles.suggestionsTitle}>Que souhaitez-vous faire ?</h3>
          <div className={styles.suggestionsList}>
            <Link to="/" className={styles.suggestionItem}>
              <span className={styles.suggestionIcon}>🏠</span>
              <span>Retour à l'accueil</span>
            </Link>
            <Link to="/courses" className={styles.suggestionItem}>
              <span className={styles.suggestionIcon}>📚</span>
              <span>Voir les cours</span>
            </Link>
            <Link to="/dashboard" className={styles.suggestionItem}>
              <span className={styles.suggestionIcon}>📊</span>
              <span>Tableau de bord</span>
            </Link>
            <button onClick={handleGoBack} className={styles.suggestionItem}>
              <span className={styles.suggestionIcon}>↩️</span>
              <span>Page précédente</span>
            </button>
          </div>
        </div>

        {/* Search Bar */}
        <div className={styles.searchSection}>
          <p className={styles.searchText}>Ou recherchez ce que vous cherchez :</p>
          <div className={styles.searchBar}>
            <input
              type="text"
              placeholder="Rechercher un cours, une leçon..."
              className={styles.searchInput}
              onKeyPress={(e) => {
                if (e.key === 'Enter' && e.target.value.trim()) {
                  navigate(`/search?q=${encodeURIComponent(e.target.value.trim())}`);
                }
              }}
            />
            <button className={styles.searchButton}>
              <span className={styles.searchIcon}>🔍</span>
            </button>
          </div>
        </div>

        {/* Auto Redirect */}
        {isAutoRedirect && (
          <div className={styles.autoRedirect}>
            <p className={styles.redirectText}>
              Redirection automatique vers l'accueil dans{' '}
              <span className={styles.countdown}>{countdown}</span> secondes
            </p>
            <button onClick={handleStopRedirect} className={styles.stopButton}>
              Annuler
            </button>
          </div>
        )}

        {/* Help Section */}
        <div className={styles.helpSection}>
          <p className={styles.helpText}>
            Besoin d'aide ? Contactez notre{' '}
            <Link to="/support" className={styles.helpLink}>
              support technique
            </Link>
          </p>
        </div>
      </div>

      {/* Background Animation */}
      <div className={styles.stars}>
        {[...Array(50)].map((_, i) => (
          <div
            key={i}
            className={styles.star}
            style={{
              left: `${Math.random() * 100}%`,
              top: `${Math.random() * 100}%`,
              animationDelay: `${Math.random() * 3}s`,
              animationDuration: `${2 + Math.random() * 2}s`
            }}
          ></div>
        ))}
      </div>
    </div>
  );
};

export default NotFound;