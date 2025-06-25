
import React, { useState, useEffect, useRef } from 'react';
import PropTypes from 'prop-types';
import styles from './ProgressBar.module.css';

/**
 * Composant ProgressBar - Affiche une barre de progression interactive
 * 
 * Ce composant fait partie du système de suivi de progression de JavaCraft Academy.
 * Il affiche visuellement le pourcentage de completion d'un cours, d'une leçon ou
 * de tout autre élément progressif dans l'application.
 * 
 * Relations avec l'application :
 * - Utilisé dans CourseProgress pour afficher la progression des cours
 * - Intégré dans le Dashboard pour montrer les statistiques globales
 * - Connecté au ProgressContext pour la gestion d'état globale
 * - Utilise le ProgressService pour synchroniser avec le backend
 * 
 * @component
 * @example
 * // Utilisation basique
 * <ProgressBar value={75} />
 * 
 * @example
 * // Avec toutes les options
 * <ProgressBar
 *   value={85}
 *   size="large"
 *   variant="success"
 *   showLabel={true}
 *   showPercentage={true}
 *   animated={true}
 *   striped={true}
 *   label="Progression du cours Java"
 *   color="#4CAF50"
 *   onComplete={(value) => console.log('Progression complète:', value)}
 * />
 */
const ProgressBar = ({
  value = 0,
  max = 100,
  min = 0,
  size = 'medium',
  variant = 'primary',
  showLabel = false,
  showPercentage = true,
  label = '',
  animated = false,
  striped = false,
  color = null,
  backgroundColor = null,
  borderRadius = null,
  height = null,
  className = '',
  style = {},
  onComplete = null,
  onProgress = null,
  gradual = false,
  duration = 1000,
  disabled = false,
  tooltip = null,
  milestones = [],
  showMilestones = false,
  pulse = false,
  ...rest
}) => {
  // États locaux pour la gestion de l'animation et de l'interaction
  const [currentValue, setCurrentValue] = useState(gradual ? 0 : value);
  const [isAnimating, setIsAnimating] = useState(false);
  const [isCompleted, setIsCompleted] = useState(false);
  const [hoveredMilestone, setHoveredMilestone] = useState(null);
  
  // Références pour l'animation et les effets
  const progressRef = useRef(null);
  const animationRef = useRef(null);
  const previousValueRef = useRef(value);

  /**
   * Calcule le pourcentage de progression normalisé
   * @param {number} val - Valeur à normaliser
   * @returns {number} Pourcentage entre 0 et 100
   */
  const calculatePercentage = (val) => {
    const normalizedValue = Math.max(min, Math.min(max, val));
    return ((normalizedValue - min) / (max - min)) * 100;
  };

  /**
   * Anime la progression de la valeur précédente vers la nouvelle valeur
   * @param {number} targetValue - Valeur cible à atteindre
   */
  const animateProgress = (targetValue) => {
    if (!gradual) {
      setCurrentValue(targetValue);
      return;
    }

    setIsAnimating(true);
    const startValue = currentValue;
    const difference = targetValue - startValue;
    const startTime = Date.now();

    const animate = () => {
      const elapsed = Date.now() - startTime;
      const progress = Math.min(elapsed / duration, 1);
      
      // Fonction d'easing pour une animation fluide
      const easeOutCubic = 1 - Math.pow(1 - progress, 3);
      const newValue = startValue + (difference * easeOutCubic);
      
      setCurrentValue(newValue);

      if (progress < 1) {
        animationRef.current = requestAnimationFrame(animate);
      } else {
        setIsAnimating(false);
        setCurrentValue(targetValue);
      }
    };

    animationRef.current = requestAnimationFrame(animate);
  };

  /**
   * Gère les changements de valeur et déclenche les callbacks appropriés
   */
  useEffect(() => {
    if (value !== previousValueRef.current) {
      animateProgress(value);
      
      // Déclenche le callback de progression
      if (onProgress && typeof onProgress === 'function') {
        onProgress(value, previousValueRef.current);
      }
      
      previousValueRef.current = value;
    }
  }, [value, onProgress, gradual, duration]);

  /**
   * Vérifie si la progression est complète et déclenche le callback
   */
  useEffect(() => {
    const percentage = calculatePercentage(currentValue);
    const wasCompleted = isCompleted;
    const nowCompleted = percentage >= 100;
    
    setIsCompleted(nowCompleted);
    
    // Déclenche le callback de completion seulement lors du passage à 100%
    if (!wasCompleted && nowCompleted && onComplete && typeof onComplete === 'function') {
      onComplete(currentValue);
    }
  }, [currentValue, isCompleted, onComplete, max, min]);

  /**
   * Nettoie les animations lors du démontage du composant
   */
  useEffect(() => {
    return () => {
      if (animationRef.current) {
        cancelAnimationFrame(animationRef.current);
      }
    };
  }, []);

  /**
   * Génère les styles CSS dynamiques pour la barre de progression
   * @returns {Object} Objet de styles CSS
   */
  const getProgressStyles = () => {
    const percentage = calculatePercentage(currentValue);
    
    return {
      width: `${percentage}%`,
      backgroundColor: color || `var(--progress-${variant}-color, #007bff)`,
      borderRadius: borderRadius || 'inherit',
      transition: gradual ? `width ${duration}ms cubic-bezier(0.4, 0, 0.2, 1)` : 'width 0.3s ease',
      ...(!disabled && pulse && percentage > 0 && {
        animation: `${styles.pulse} 2s infinite`
      })
    };
  };

  /**
   * Génère les styles pour le conteneur de la barre de progression
   * @returns {Object} Objet de styles CSS
   */
  const getContainerStyles = () => {
    return {
      backgroundColor: backgroundColor || `var(--progress-bg-color, #e9ecef)`,
      borderRadius: borderRadius || `var(--progress-border-radius, 0.375rem)`,
      height: height || `var(--progress-${size}-height, ${size === 'small' ? '0.5rem' : size === 'large' ? '1.5rem' : '1rem'})`,
      ...style
    };
  };

  /**
   * Rendu des jalons (milestones) sur la barre de progression
   * @returns {JSX.Element[]} Tableau d'éléments JSX pour les jalons
   */
  const renderMilestones = () => {
    if (!showMilestones || !milestones.length) return null;

    return milestones.map((milestone, index) => {
      const milestonePercentage = calculatePercentage(milestone.value);
      const isReached = currentValue >= milestone.value;
      
      return (
        <div
          key={`milestone-${index}`}
          className={`${styles.milestone} ${isReached ? styles.milestoneReached : ''}`}
          style={{ left: `${milestonePercentage}%` }}
          onMouseEnter={() => setHoveredMilestone(milestone)}
          onMouseLeave={() => setHoveredMilestone(null)}
          title={milestone.label || `Jalon: ${milestone.value}`}
        >
          {milestone.icon && <span className={styles.milestoneIcon}>{milestone.icon}</span>}
          {hoveredMilestone === milestone && (
            <div className={styles.milestoneTooltip}>
              {milestone.label || `${milestone.value}%`}
            </div>
          )}
        </div>
      );
    });
  };

  /**
   * Rendu du label et du pourcentage
   * @returns {JSX.Element} Élément JSX contenant le label et/ou le pourcentage
   */
  const renderLabel = () => {
    const percentage = Math.round(calculatePercentage(currentValue));
    
    return (
      <div className={styles.labelContainer}>
        {showLabel && label && (
          <span className={styles.label}>{label}</span>
        )}
        {showPercentage && (
          <span className={styles.percentage}>
            {percentage}%
            {isCompleted && <span className={styles.completedIcon}>✓</span>}
          </span>
        )}
      </div>
    );
  };

  // Construction des classes CSS
  const containerClasses = [
    styles.progressContainer,
    styles[`size-${size}`],
    styles[`variant-${variant}`],
    disabled && styles.disabled,
    isCompleted && styles.completed,
    isAnimating && styles.animating,
    className
  ].filter(Boolean).join(' ');

  const barClasses = [
    styles.progressBar,
    animated && styles.animated,
    striped && styles.striped,
    pulse && styles.pulse
  ].filter(Boolean).join(' ');

  return (
    <div 
      className={styles.progressWrapper}
      title={tooltip}
      {...rest}
    >
      {(showLabel || showPercentage) && renderLabel()}
      
      <div 
        ref={progressRef}
        className={containerClasses}
        style={getContainerStyles()}
        role="progressbar"
        aria-valuenow={Math.round(currentValue)}
        aria-valuemin={min}
        aria-valuemax={max}
        aria-label={label || `Progression: ${Math.round(calculatePercentage(currentValue))}%`}
      >
        <div 
          className={barClasses}
          style={getProgressStyles()}
        />
        
        {renderMilestones()}
        
        {/* Effet de brillance pour l'animation */}
        {animated && (
          <div className={styles.shine} />
        )}
      </div>
    </div>
  );
};

/**
 * Définition des PropTypes pour la validation des props
 */
ProgressBar.propTypes = {
  /** Valeur actuelle de la progression (entre min et max) */
  value: PropTypes.number,
  
  /** Valeur maximale de la progression */
  max: PropTypes.number,
  
  /** Valeur minimale de la progression */
  min: PropTypes.number,
  
  /** Taille de la barre de progression */
  size: PropTypes.oneOf(['small', 'medium', 'large']),
  
  /** Variante de couleur de la barre */
  variant: PropTypes.oneOf(['primary', 'secondary', 'success', 'warning', 'danger', 'info']),
  
  /** Afficher le label au-dessus de la barre */
  showLabel: PropTypes.bool,
  
  /** Afficher le pourcentage */
  showPercentage: PropTypes.bool,
  
  /** Texte du label */
  label: PropTypes.string,
  
  /** Activer l'animation de la barre */
  animated: PropTypes.bool,
  
  /** Afficher des rayures sur la barre */
  striped: PropTypes.bool,
  
  /** Couleur personnalisée de la barre */
  color: PropTypes.string,
  
  /** Couleur de fond personnalisée */
  backgroundColor: PropTypes.string,
  
  /** Rayon de bordure personnalisé */
  borderRadius: PropTypes.string,
  
  /** Hauteur personnalisée */
  height: PropTypes.string,
  
  /** Classes CSS supplémentaires */
  className: PropTypes.string,
  
  /** Styles inline supplémentaires */
  style: PropTypes.object,
  
  /** Callback appelé lorsque la progression atteint 100% */
  onComplete: PropTypes.func,
  
  /** Callback appelé lors des changements de progression */
  onProgress: PropTypes.func,
  
  /** Activer l'animation graduelle */
  gradual: PropTypes.bool,
  
  /** Durée de l'animation en millisecondes */
  duration: PropTypes.number,
  
  /** Désactiver la barre de progression */
  disabled: PropTypes.bool,
  
  /** Texte du tooltip */
  tooltip: PropTypes.string,
  
  /** Tableau des jalons à afficher */
  milestones: PropTypes.arrayOf(PropTypes.shape({
    value: PropTypes.number.isRequired,
    label: PropTypes.string,
    icon: PropTypes.node
  })),
  
  /** Afficher les jalons */
  showMilestones: PropTypes.bool,
  
  /** Effet de pulsation */
  pulse: PropTypes.bool
};

/**
 * Valeurs par défaut des props
 */
ProgressBar.defaultProps = {
  value: 0,
  max: 100,
  min: 0,
  size: 'medium',
  variant: 'primary',
  showLabel: false,
  showPercentage: true,
  label: '',
  animated: false,
  striped: false,
  color: null,
  backgroundColor: null,
  borderRadius: null,
  height: null,
  className: '',
  style: {},
  onComplete: null,
  onProgress: null,
  gradual: false,
  duration: 1000,
  disabled: false,
  tooltip: null,
  milestones: [],
  showMilestones: false,
  pulse: false
};

export default ProgressBar;