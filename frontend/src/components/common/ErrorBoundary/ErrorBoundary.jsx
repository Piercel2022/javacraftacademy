import React from 'react';
import styles from './ErrorBoundary.module.css';

class ErrorBoundary extends React.Component {
  constructor(props) {
    super(props);
    this.state = { hasError: false, error: null, errorInfo: null };
  }

  static getDerivedStateFromError(error) {
    return { hasError: true };
  }

  componentDidCatch(error, errorInfo) {
    this.setState({
      error: error,
      errorInfo: errorInfo
    });
    // Log l'erreur pour le monitoring
    console.error('ErrorBoundary caught an error:', error, errorInfo);
    
    // Ici vous pourriez envoyer l'erreur à un service de monitoring
    // comme Sentry, LogRocket, etc.
  }

  handleReload = () => {
    window.location.reload();
  };

  handleReset = () => {
    this.setState({ hasError: false, error: null, errorInfo: null });
  };

  render() {
    if (this.state.hasError) {
      return (
        <div className={styles.errorContainer}>
          <div className={styles.errorContent}>
            <div className={styles.errorIcon}>
              <svg 
                width="64" 
                height="64" 
                viewBox="0 0 24 24" 
                fill="none" 
                stroke="currentColor" 
                strokeWidth="2"
              >
                <circle cx="12" cy="12" r="10"/>
                <line x1="12" y1="8" x2="12" y2="12"/>
                <line x1="12" y1="16" x2="12.01" y2="16"/>
              </svg>
            </div>
            
            <h1 className={styles.errorTitle}>
              Oops! Quelque chose s'est mal passé
            </h1>
            
            <p className={styles.errorMessage}>
              Une erreur inattendue s'est produite. Ne vous inquiétez pas, 
              nos développeurs ont été notifiés et travaillent sur une solution.
            </p>

            <div className={styles.errorActions}>
              <button 
                onClick={this.handleReset}
                className={`${styles.button} ${styles.buttonPrimary}`}
              >
                Réessayer
              </button>
              
              <button 
                onClick={this.handleReload}
                className={`${styles.button} ${styles.buttonSecondary}`}
              >
                Recharger la page
              </button>
            </div>

            {process.env.NODE_ENV === 'development' && (
              <details className={styles.errorDetails}>
                <summary className={styles.errorSummary}>
                  Détails de l'erreur (développement)
                </summary>
                <div className={styles.errorTrace}>
                  <h3>Erreur:</h3>
                  <pre>{this.state.error && this.state.error.toString()}</pre>
                  
                  <h3>Stack trace:</h3>
                  <pre>
                    {this.state.errorInfo && this.state.errorInfo.componentStack 
                      ? this.state.errorInfo.componentStack 
                      : 'Stack trace non disponible'}
                  </pre>
                </div>
              </details>
            )}
          </div>
        </div>
      );
    }

    return this.props.children;
  }
}

export default ErrorBoundary;