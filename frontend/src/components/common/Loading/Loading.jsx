// frontend/src/components/common/Loading/Loading.jsx
import React from 'react';
import styles from './Loading.module.css';

const Loading = ({
  type = 'spinner',
  size = 'medium',
  color = 'primary',
  text,
  fullScreen = false,
  overlay = false,
  className = '',
  ...props
}) => {
  const loadingClasses = [
    styles.loading,
    styles[`loading--${type}`],
    styles[`loading--${size}`],
    styles[`loading--${color}`],
    fullScreen && styles['loading--fullScreen'],
    overlay && styles['loading--overlay'],
    className
  ].filter(Boolean).join(' ');

  const renderSpinner = () => (
    <div className={styles.spinner} aria-hidden="true">
      <div className={styles.spinnerCircle}></div>
    </div>
  );

  const renderDots = () => (
    <div className={styles.dots} aria-hidden="true">
      <div className={styles.dot}></div>
      <div className={styles.dot}></div>
      <div className={styles.dot}></div>
    </div>
  );

  const renderPulse = () => (
    <div className={styles.pulse} aria-hidden="true">
      <div className={styles.pulseCircle}></div>
    </div>
  );

  const renderBars = () => (
    <div className={styles.bars} aria-hidden="true">
      <div className={styles.bar}></div>
      <div className={styles.bar}></div>
      <div className={styles.bar}></div>
      <div className={styles.bar}></div>
    </div>
  );

  const renderRipple = () => (
    <div className={styles.ripple} aria-hidden="true">
      <div className={styles.rippleCircle}></div>
      <div className={styles.rippleCircle}></div>
    </div>
  );

  const renderSkeleton = () => (
    <div className={styles.skeleton} aria-hidden="true">
      <div className={styles.skeletonLine}></div>
      <div className={styles.skeletonLine}></div>
      <div className={styles.skeletonLine}></div>
    </div>
  );

  const renderLoadingComponent = () => {
    switch (type) {
      case 'dots':
        return renderDots();
      case 'pulse':
        return renderPulse();
      case 'bars':
        return renderBars();
      case 'ripple':
        return renderRipple();
      case 'skeleton':
        return renderSkeleton();
      case 'spinner':
      default:
        return renderSpinner();
    }
  };

  const content = (
    <div className={loadingClasses} role="status" aria-live="polite" {...props}>
      <div className={styles.container}>
        {renderLoadingComponent()}
        {text && (
          <div className={styles.text} aria-label={text}>
            {text}
          </div>
        )}
      </div>
      <span className="sr-only">{text || 'Chargement en cours...'}</span>
    </div>
  );

  if (fullScreen) {
    return (
      <div className={styles.fullScreenWrapper}>
        {content}
      </div>
    );
  }

  return content;
};

// Composant de skeleton pour les listes
export const SkeletonList = ({ count = 3, className = '' }) => (
  <div className={`${styles.skeletonList} ${className}`}>
    {Array.from({ length: count }, (_, index) => (
      <div key={index} className={styles.skeletonItem}>
        <div className={styles.skeletonAvatar}></div>
        <div className={styles.skeletonContent}>
          <div className={styles.skeletonTitle}></div>
          <div className={styles.skeletonSubtitle}></div>
        </div>
      </div>
    ))}
  </div>
);

// Composant de skeleton pour les cartes
export const SkeletonCard = ({ className = '' }) => (
  <div className={`${styles.skeletonCard} ${className}`}>
    <div className={styles.skeletonCardImage}></div>
    <div className={styles.skeletonCardContent}>
      <div className={styles.skeletonCardTitle}></div>
      <div className={styles.skeletonCardText}></div>
      <div className={styles.skeletonCardText}></div>
    </div>
  </div>
);

// Composant de loading avec progress
export const LoadingWithProgress = ({ 
  progress = 0, 
  text = 'Chargement...', 
  showPercentage = true,
  className = '' 
}) => (
  <div className={`${styles.loadingProgress} ${className}`}>
    <div className={styles.progressContainer}>
      <div className={styles.progressBar}>
        <div 
          className={styles.progressFill}
          style={{ width: `${Math.min(100, Math.max(0, progress))}%` }}
        />
      </div>
      <div className={styles.progressText}>
        {text}
        {showPercentage && ` ${Math.round(progress)}%`}
      </div>
    </div>
  </div>
);

// Hook pour gérer les états de loading
export const useLoading = (initialState = false) => {
  const [loading, setLoading] = React.useState(initialState);
  const [error, setError] = React.useState(null);

  const startLoading = () => {
    setLoading(true);
    setError(null);
  };

  const stopLoading = () => {
    setLoading(false);
  };

  const setLoadingError = (errorMessage) => {
    setLoading(false);
    setError(errorMessage);
  };

  return {
    loading,
    error,
    startLoading,
    stopLoading,
    setLoadingError
  };
};

export default Loading;