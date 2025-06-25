
import React from 'react';
import PropTypes from 'prop-types';
import styles from './StatsCard.module.css';

/**
 * Composant StatsCard - Affiche une carte de statistiques avec icône, valeur et label
 * 
 * Ce composant fait partie du système de suivi de progression de JavaCraft Academy.
 * Il présente des métriques clés de l'utilisateur de manière visuelle et accessible.
 * 
 * Relations avec l'application :
 * - Utilisé dans Dashboard.jsx pour afficher les statistiques générales
 * - Intégré dans Progress.jsx pour les métriques détaillées
 * - Connecté au ProgressContext pour récupérer les données en temps réel
 * - Stylé selon le ThemeContext (mode clair/sombre)
 * 
 * Fonctionnalités :
 * - Affichage de métriques avec formatage automatique
 * - Support des icônes personnalisées
 * - Animation au hover et focus
 * - Accessibilité complète (ARIA)
 * - Responsive design
 * - Support des thèmes
 * - Gestion des états de chargement et d'erreur
 * 
 * @component
 * @example
 * // Affichage du nombre de cours complétés
 * <StatsCard
 *   icon="📚"
 *   value={15}
 *   label="Cours Complétés"
 *   color="success"
 *   onClick={() => navigateToCourses()}
 * />
 * 
 * @example
 * // Affichage du temps d'apprentissage avec formatage
 * <StatsCard
 *   icon="⏱️"
 *   value={245}
 *   label="Minutes d'Apprentissage"
 *   formatter={(value) => `${Math.floor(value / 60)}h ${value % 60}m`}
 *   trend={{ value: 12, direction: 'up' }}
 * />
 */
const StatsCard = ({
  icon,
  iconComponent: IconComponent,
  value,
  label,
  sublabel,
  color = 'primary',
  size = 'medium',
  formatter,
  trend,
  loading = false,
  error = null,
  clickable = false,
  onClick,
  className = '',
  testId = 'stats-card',
  ...props
}) => {
  /**
   * Formate la valeur selon le formateur fourni ou le formatage par défaut
   * @param {number|string} val - Valeur à formater
   * @returns {string} Valeur formatée
   */
  const formatValue = (val) => {
    if (formatter && typeof formatter === 'function') {
      return formatter(val);
    }
    
    if (typeof val === 'number') {
      // Formatage par défaut pour les nombres
      if (val >= 1000000) {
        return `${(val / 1000000).toFixed(1)}M`;
      } else if (val >= 1000) {
        return `${(val / 1000).toFixed(1)}K`;
      }
      return val.toLocaleString();
    }
    
    return val;
  };

  /**
   * Gère le clic sur la carte
   * @param {Event} event - Événement de clic
   */
  const handleClick = (event) => {
    if (clickable && onClick && !loading && !error) {
      onClick(event);
    }
  };

  /**
   * Gère l'activation par clavier (Enter/Space)
   * @param {KeyboardEvent} event - Événement clavier
   */
  const handleKeyDown = (event) => {
    if (clickable && onClick && !loading && !error) {
      if (event.key === 'Enter' || event.key === ' ') {
        event.preventDefault();
        onClick(event);
      }
    }
  };

  /**
   * Rendu de l'icône (emoji ou composant)
   * @returns {JSX.Element|string} Élément icône
   */
  const renderIcon = () => {
    if (IconComponent) {
      return <IconComponent className={styles.iconComponent} />;
    }
    return <span className={styles.iconEmoji}>{icon}</span>;
  };

  /**
   * Rendu de l'indicateur de tendance
   * @returns {JSX.Element|null} Élément de tendance ou null
   */
  const renderTrend = () => {
    if (!trend || loading || error) return null;

    const { value: trendValue, direction } = trend;
    const trendClass = direction === 'up' ? styles.trendUp : 
                      direction === 'down' ? styles.trendDown : 
                      styles.trendStable;

    const trendIcon = direction === 'up' ? '↗️' : 
                     direction === 'down' ? '↘️' : '➡️';

    return (
      <div className={`${styles.trend} ${trendClass}`}>
        <span className={styles.trendIcon}>{trendIcon}</span>
        <span className={styles.trendValue}>
          {trendValue > 0 ? '+' : ''}{trendValue}%
        </span>
      </div>
    );
  };

  /**
   * Rendu du contenu de la carte selon l'état
   * @returns {JSX.Element} Contenu de la carte
   */
  const renderContent = () => {
    if (loading) {
      return (
        <div className={styles.loadingState}>
          <div className={styles.skeleton}>
            <div className={styles.skeletonIcon}></div>
            <div className={styles.skeletonText}>
              <div className={styles.skeletonValue}></div>
              <div className={styles.skeletonLabel}></div>
            </div>
          </div>
        </div>
      );
    }

    if (error) {
      return (
        <div className={styles.errorState}>
          <span className={styles.errorIcon}>⚠️</span>
          <div className={styles.errorText}>
            <span className={styles.errorMessage}>Erreur</span>
            <span className={styles.errorLabel}>{error}</span>
          </div>
        </div>
      );
    }

    return (
      <div className={styles.content}>
        <div className={styles.iconContainer}>
          {renderIcon()}
        </div>
        <div className={styles.textContainer}>
          <div className={styles.valueContainer}>
            <span className={styles.value}>{formatValue(value)}</span>
            {renderTrend()}
          </div>
          <span className={styles.label}>{label}</span>
          {sublabel && <span className={styles.sublabel}>{sublabel}</span>}
        </div>
      </div>
    );
  };

  // Classes CSS dynamiques
  const cardClasses = [
    styles.statsCard,
    styles[`color-${color}`],
    styles[`size-${size}`],
    clickable && !loading && !error ? styles.clickable : '',
    loading ? styles.loading : '',
    error ? styles.error : '',
    className
  ].filter(Boolean).join(' ');

  return (
    <div
      className={cardClasses}
      onClick={handleClick}
      onKeyDown={handleKeyDown}
      role={clickable ? 'button' : 'article'}
      tabIndex={clickable ? 0 : -1}
      aria-label={`${label}: ${formatValue(value)}`}
      aria-describedby={sublabel ? `${testId}-sublabel` : undefined}
      data-testid={testId}
      {...props}
    >
      {renderContent()}
      
      {/* Focus ring pour l'accessibilité */}
      {clickable && <div className={styles.focusRing} />}
    </div>
  );
};

/**
 * PropTypes pour la validation des props
 */
StatsCard.propTypes = {
  /** Icône emoji à afficher */
  icon: PropTypes.string,
  
  /** Composant d'icône React à afficher (alternative à icon) */
  iconComponent: PropTypes.elementType,
  
  /** Valeur principale à afficher */
  value: PropTypes.oneOfType([
    PropTypes.number,
    PropTypes.string
  ]).isRequired,
  
  /** Label principal décrivant la statistique */
  label: PropTypes.string.isRequired,
  
  /** Label secondaire optionnel */
  sublabel: PropTypes.string,
  
  /** Couleur thématique de la carte */
  color: PropTypes.oneOf([
    'primary',
    'secondary',
    'success',
    'warning',
    'error',
    'info'
  ]),
  
  /** Taille de la carte */
  size: PropTypes.oneOf(['small', 'medium', 'large']),
  
  /** Fonction pour formater la valeur */
  formatter: PropTypes.func,
  
  /** Objet décrivant la tendance */
  trend: PropTypes.shape({
    value: PropTypes.number.isRequired,
    direction: PropTypes.oneOf(['up', 'down', 'stable']).isRequired
  }),
  
  /** État de chargement */
  loading: PropTypes.bool,
  
  /** Message d'erreur */
  error: PropTypes.string,
  
  /** La carte est-elle cliquable */
  clickable: PropTypes.bool,
  
  /** Fonction appelée au clic */
  onClick: PropTypes.func,
  
  /** Classes CSS additionnelles */
  className: PropTypes.string,
  
  /** ID de test pour les tests automatisés */
  testId: PropTypes.string
};

/**
 * Valeurs par défaut des props
 */
StatsCard.defaultProps = {
  color: 'primary',
  size: 'medium',
  loading: false,
  error: null,
  clickable: false,
  className: '',
  testId: 'stats-card'
};

export default StatsCard;