import React, { useState, useEffect, useCallback, useMemo } from 'react';
import PropTypes from 'prop-types';
import { useAuth } from '../../../hooks/useAuth';
import { usePayment } from '../../../hooks/usePayment';
import { useSubscription } from '../../../hooks/useSubscription';
import { useNotification } from '../../../hooks/useNotification';
import { Button } from '../../common/Button';
import { Loading } from '../../common/Loading';
import { formatCurrency, formatDiscount, calculateDiscountedPrice } from '../../../utils/priceUtils';
import { trackEvent } from '../../../utils/analytics';
import { PRICING_EVENTS, SUBSCRIPTION_TYPES } from '../../../utils/constants';
import styles from './PricingCard.module.css';

/**
 * Composant PricingCard - Affiche une carte de tarification avec les détails d'un plan
 * 
 * @component
 * @author JavaCraft Academy
 * @version 1.0.0
 * @since 2024-01-01
 * 
 * @description
 * Ce composant affiche une carte de tarification interactive qui permet aux utilisateurs
 * de visualiser les détails d'un plan d'abonnement et de procéder à l'achat.
 * 
 * Fonctionnalités principales :
 * - Affichage des détails du plan (prix, fonctionnalités, durée)
 * - Calcul automatique des remises et prix promotionnels
 * - Intégration avec le système de paiement
 * - Gestion des états d'abonnement existants
 * - Suivi analytique des interactions utilisateur
 * - Support des codes promotionnels
 * - Animations et transitions fluides
 * 
 * Relations avec l'application :
 * - Utilise useAuth pour l'authentification utilisateur
 * - Utilise usePayment pour la gestion des paiements
 * - Utilise useSubscription pour les abonnements
 * - Utilise useNotification pour les notifications
 * - Communique avec PaymentService pour les transactions
 * - Intègre avec SubscriptionService pour les plans
 * - Connecté au système d'analytics
 * 
 * @param {Object} props - Propriétés du composant
 * @param {Object} props.plan - Objet contenant les détails du plan de tarification
 * @param {boolean} props.isPopular - Indique si le plan est populaire (mise en évidence)
 * @param {boolean} props.isCurrentPlan - Indique si c'est le plan actuel de l'utilisateur
 * @param {Function} props.onSelectPlan - Callback appelé lors de la sélection du plan
 * @param {Object} props.discount - Objet contenant les informations de remise
 * @param {boolean} props.showAnnualDiscount - Affiche la remise annuelle
 * @param {string} props.currency - Devise à utiliser pour l'affichage des prix
 * @param {string} props.billingPeriod - Période de facturation ('monthly' ou 'annual')
 * @param {boolean} props.loading - État de chargement
 * @param {Function} props.onUpgrade - Callback pour la mise à niveau
 * @param {Function} props.onDowngrade - Callback pour la rétrogradation
 * @param {Object} props.customization - Options de personnalisation visuelle
 * @param {Array} props.additionalFeatures - Fonctionnalités supplémentaires à afficher
 * @param {boolean} props.showComparison - Affiche les éléments de comparaison
 * @param {Function} props.onCompare - Callback pour la comparaison
 * @param {Object} props.analytics - Configuration analytique
 * @param {boolean} props.disabled - Désactive la carte
 * @param {string} props.theme - Thème de la carte ('light', 'dark', 'custom')
 * 
 * @returns {JSX.Element} Composant PricingCard rendu
 * 
 * @example
 * ```jsx
 * // Utilisation basique
 * <PricingCard
 *   plan={{
 *     id: 'premium',
 *     name: 'Premium',
 *     price: 29.99,
 *     features: ['Accès illimité', 'Support prioritaire'],
 *     duration: 'monthly'
 *   }}
 *   onSelectPlan={handleSelectPlan}
 * />
 * 
 * // Utilisation avancée avec remise
 * <PricingCard
 *   plan={premiumPlan}
 *   isPopular={true}
 *   discount={{ percentage: 20, validUntil: '2024-12-31' }}
 *   showAnnualDiscount={true}
 *   currency="EUR"
 *   billingPeriod="annual"
 *   customization={{
 *     accentColor: '#007bff',
 *     borderRadius: '12px'
 *   }}
 *   analytics={{
 *     source: 'pricing-page',
 *     campaign: 'black-friday'
 *   }}
 * />
 * ```
 */
const PricingCard = ({
  plan,
  isPopular = false,
  isCurrentPlan = false,
  onSelectPlan,
  discount = null,
  showAnnualDiscount = false,
  currency = 'EUR',
  billingPeriod = 'monthly',
  loading = false,
  onUpgrade,
  onDowngrade,
  customization = {},
  additionalFeatures = [],
  showComparison = false,
  onCompare,
  analytics = {},
  disabled = false,
  theme = 'light',
  ...props
}) => {
  // Hooks
  const { user, isAuthenticated } = useAuth();
  const { createPaymentIntent, isProcessing } = usePayment();
  const { currentSubscription, canUpgrade, canDowngrade } = useSubscription();
  const { showNotification } = useNotification();

  // États locaux
  const [isHovered, setIsHovered] = useState(false);
  const [isAnimating, setIsAnimating] = useState(false);
  const [showFeatures, setShowFeatures] = useState(false);
  const [selectedBillingPeriod, setSelectedBillingPeriod] = useState(billingPeriod);

  /**
   * Calcule le prix final avec les remises applicables
   * @returns {Object} Prix original et prix avec remise
   */
  const calculatedPrice = useMemo(() => {
    const basePrice = plan.price;
    let finalPrice = basePrice;
    let discountAmount = 0;

    // Application de la remise du plan
    if (discount && discount.percentage) {
      discountAmount = calculateDiscountedPrice(basePrice, discount.percentage);
      finalPrice = basePrice - discountAmount;
    }

    // Application de la remise annuelle
    if (showAnnualDiscount && selectedBillingPeriod === 'annual') {
      const annualDiscount = plan.annualDiscount || 0;
      const annualDiscountAmount = calculateDiscountedPrice(finalPrice, annualDiscount);
      finalPrice = finalPrice - annualDiscountAmount;
      discountAmount += annualDiscountAmount;
    }

    return {
      original: basePrice,
      final: finalPrice,
      discount: discountAmount,
      savings: basePrice - finalPrice
    };
  }, [plan, discount, showAnnualDiscount, selectedBillingPeriod]);

  /**
   * Détermine l'état du bouton d'action
   * @returns {Object} Configuration du bouton
   */
  const buttonConfig = useMemo(() => {
    if (isCurrentPlan) {
      return {
        text: 'Plan actuel',
        variant: 'secondary',
        disabled: true,
        icon: 'check'
      };
    }

    if (currentSubscription) {
      if (canUpgrade(plan.id)) {
        return {
          text: 'Mettre à niveau',
          variant: 'primary',
          disabled: false,
          icon: 'arrow-up'
        };
      } else if (canDowngrade(plan.id)) {
        return {
          text: 'Rétrograder',
          variant: 'outline',
          disabled: false,
          icon: 'arrow-down'
        };
      }
    }

    return {
      text: 'Choisir ce plan',
      variant: 'primary',
      disabled: false,
      icon: 'arrow-right'
    };
  }, [isCurrentPlan, currentSubscription, canUpgrade, canDowngrade, plan.id]);

  /**
   * Gère le clic sur le bouton de sélection du plan
   * @param {Event} event - Événement de clic
   */
  const handlePlanSelection = useCallback(async (event) => {
    event.preventDefault();
    
    if (disabled || loading || isProcessing) return;

    setIsAnimating(true);

    try {
      // Tracking analytique
      trackEvent(PRICING_EVENTS.PLAN_SELECTED, {
        planId: plan.id,
        planName: plan.name,
        price: calculatedPrice.final,
        currency,
        billingPeriod: selectedBillingPeriod,
        isUpgrade: canUpgrade(plan.id),
        isDowngrade: canDowngrade(plan.id),
        hasDiscount: !!discount,
        ...analytics
      });

      // Vérification de l'authentification
      if (!isAuthenticated) {
        showNotification({
          type: 'info',
          message: 'Veuillez vous connecter pour choisir un plan',
          action: {
            label: 'Se connecter',
            onClick: () => window.location.href = '/login'
          }
        });
        return;
      }

      // Gestion des actions spécifiques
      if (currentSubscription) {
        if (canUpgrade(plan.id) && onUpgrade) {
          await onUpgrade(plan);
        } else if (canDowngrade(plan.id) && onDowngrade) {
          await onDowngrade(plan);
        }
      } else {
        // Nouveau plan
        if (onSelectPlan) {
          await onSelectPlan(plan, {
            billingPeriod: selectedBillingPeriod,
            price: calculatedPrice.final,
            discount,
            analytics
          });
        }
      }

      showNotification({
        type: 'success',
        message: `Plan ${plan.name} sélectionné avec succès!`
      });

    } catch (error) {
      console.error('Erreur lors de la sélection du plan:', error);
      
      trackEvent(PRICING_EVENTS.PLAN_SELECTION_ERROR, {
        planId: plan.id,
        error: error.message,
        ...analytics
      });

      showNotification({
        type: 'error',
        message: 'Erreur lors de la sélection du plan. Veuillez réessayer.'
      });
    } finally {
      setIsAnimating(false);
    }
  }, [
    disabled, loading, isProcessing, isAuthenticated, plan, calculatedPrice,
    currency, selectedBillingPeriod, discount, analytics, canUpgrade, canDowngrade,
    currentSubscription, onSelectPlan, onUpgrade, onDowngrade, showNotification
  ]);

  /**
   * Gère le changement de période de facturation
   * @param {string} period - Nouvelle période
   */
  const handleBillingPeriodChange = useCallback((period) => {
    setSelectedBillingPeriod(period);
    
    trackEvent(PRICING_EVENTS.BILLING_PERIOD_CHANGED, {
      planId: plan.id,
      fromPeriod: selectedBillingPeriod,
      toPeriod: period,
      ...analytics
    });
  }, [plan.id, selectedBillingPeriod, analytics]);

  /**
   * Gère l'affichage/masquage des fonctionnalités
   */
  const toggleFeatures = useCallback(() => {
    setShowFeatures(prev => !prev);
    
    trackEvent(PRICING_EVENTS.FEATURES_TOGGLED, {
      planId: plan.id,
      expanded: !showFeatures,
      ...analytics
    });
  }, [plan.id, showFeatures, analytics]);

  /**
   * Gère la comparaison de plans
   */
  const handleCompare = useCallback(() => {
    if (onCompare) {
      onCompare(plan);
      
      trackEvent(PRICING_EVENTS.PLAN_COMPARED, {
        planId: plan.id,
        ...analytics
      });
    }
  }, [plan, onCompare, analytics]);

  // Effet pour l'animation d'entrée
  useEffect(() => {
    const timer = setTimeout(() => {
      setIsAnimating(true);
    }, 100);

    return () => clearTimeout(timer);
  }, []);

  // Classes CSS dynamiques
  const cardClasses = [
    styles.pricingCard,
    styles[theme],
    isPopular && styles.popular,
    isCurrentPlan && styles.current,
    isHovered && styles.hovered,
    isAnimating && styles.animated,
    disabled && styles.disabled,
    loading && styles.loading
  ].filter(Boolean).join(' ');

  // Rendu du composant
  return (
    <div
      className={cardClasses}
      onMouseEnter={() => setIsHovered(true)}
      onMouseLeave={() => setIsHovered(false)}
      style={{
        '--accent-color': customization.accentColor,
        '--border-radius': customization.borderRadius,
        ...customization.style
      }}
      {...props}
    >
      {/* Badge populaire */}
      {isPopular && (
        <div className={styles.popularBadge}>
          <span className={styles.popularText}>Plus populaire</span>
        </div>
      )}

      {/* Badge plan actuel */}
      {isCurrentPlan && (
        <div className={styles.currentBadge}>
          <span className={styles.currentText}>Plan actuel</span>
        </div>
      )}

      {/* En-tête du plan */}
      <div className={styles.header}>
        <h3 className={styles.planName}>{plan.name}</h3>
        {plan.description && (
          <p className={styles.planDescription}>{plan.description}</p>
        )}
      </div>

      {/* Section prix */}
      <div className={styles.pricing}>
        <div className={styles.priceContainer}>
          {calculatedPrice.savings > 0 && (
            <div className={styles.originalPrice}>
              {formatCurrency(calculatedPrice.original, currency)}
            </div>
          )}
          
          <div className={styles.currentPrice}>
            <span className={styles.currency}>{currency}</span>
            <span className={styles.amount}>
              {formatCurrency(calculatedPrice.final, currency, false)}
            </span>
            <span className={styles.period}>
              /{selectedBillingPeriod === 'monthly' ? 'mois' : 'an'}
            </span>
          </div>

          {calculatedPrice.savings > 0 && (
            <div className={styles.savings}>
              Économisez {formatCurrency(calculatedPrice.savings, currency)}
            </div>
          )}
        </div>

        {/* Sélecteur de période de facturation */}
        {showAnnualDiscount && (
          <div className={styles.billingToggle}>
            <button
              className={`${styles.billingButton} ${
                selectedBillingPeriod === 'monthly' ? styles.active : ''
              }`}
              onClick={() => handleBillingPeriodChange('monthly')}
            >
              Mensuel
            </button>
            <button
              className={`${styles.billingButton} ${
                selectedBillingPeriod === 'annual' ? styles.active : ''
              }`}
              onClick={() => handleBillingPeriodChange('annual')}
            >
              Annuel
              {plan.annualDiscount && (
                <span className={styles.discountBadge}>
                  -{plan.annualDiscount}%
                </span>
              )}
            </button>
          </div>
        )}
      </div>

      {/* Fonctionnalités */}
      <div className={styles.features}>
        <ul className={styles.featuresList}>
          {plan.features.slice(0, showFeatures ? undefined : 5).map((feature, index) => (
            <li key={index} className={styles.featureItem}>
              <span className={styles.featureIcon}>✓</span>
              <span className={styles.featureText}>{feature}</span>
            </li>
          ))}
          
          {additionalFeatures.map((feature, index) => (
            <li key={`additional-${index}`} className={styles.featureItem}>
              <span className={styles.featureIcon}>✓</span>
              <span className={styles.featureText}>{feature}</span>
            </li>
          ))}
        </ul>

        {plan.features.length > 5 && (
          <button
            className={styles.toggleFeatures}
            onClick={toggleFeatures}
          >
            {showFeatures ? 'Voir moins' : `Voir ${plan.features.length - 5} autres`}
          </button>
        )}
      </div>

      {/* Bouton d'action */}
      <div className={styles.actionContainer}>
        <Button
          variant={buttonConfig.variant}
          size="large"
          disabled={buttonConfig.disabled || loading || isProcessing}
          onClick={handlePlanSelection}
          className={styles.actionButton}
          loading={isProcessing}
        >
          {loading || isProcessing ? (
            <Loading size="small" />
          ) : (
            buttonConfig.text
          )}
        </Button>

        {showComparison && (
          <button
            className={styles.compareButton}
            onClick={handleCompare}
          >
            Comparer les plans
          </button>
        )}
      </div>

      {/* Informations supplémentaires */}
      <div className={styles.footer}>
        {discount && discount.validUntil && (
          <div className={styles.discountInfo}>
            <span className={styles.discountText}>
              Offre valable jusqu'au {new Date(discount.validUntil).toLocaleDateString()}
            </span>
          </div>
        )}

        {plan.trialPeriod && (
          <div className={styles.trialInfo}>
            <span className={styles.trialText}>
              Essai gratuit de {plan.trialPeriod} jours
            </span>
          </div>
        )}

        {plan.guarantee && (
          <div className={styles.guaranteeInfo}>
            <span className={styles.guaranteeText}>
              {plan.guarantee}
            </span>
          </div>
        )}
      </div>
    </div>
  );
};

// Validation des props
PricingCard.propTypes = {
  plan: PropTypes.shape({
    id: PropTypes.string.isRequired,
    name: PropTypes.string.isRequired,
    description: PropTypes.string,
    price: PropTypes.number.isRequired,
    features: PropTypes.arrayOf(PropTypes.string).isRequired,
    duration: PropTypes.string,
    annualDiscount: PropTypes.number,
    trialPeriod: PropTypes.number,
    guarantee: PropTypes.string
  }).isRequired,
  isPopular: PropTypes.bool,
  isCurrentPlan: PropTypes.bool,
  onSelectPlan: PropTypes.func,
  discount: PropTypes.shape({
    percentage: PropTypes.number,
    validUntil: PropTypes.string
  }),
  showAnnualDiscount: PropTypes.bool,
  currency: PropTypes.string,
  billingPeriod: PropTypes.oneOf(['monthly', 'annual']),
  loading: PropTypes.bool,
  onUpgrade: PropTypes.func,
  onDowngrade: PropTypes.func,
  customization: PropTypes.object,
  additionalFeatures: PropTypes.arrayOf(PropTypes.string),
  showComparison: PropTypes.bool,
  onCompare: PropTypes.func,
  analytics: PropTypes.object,
  disabled: PropTypes.bool,
  theme: PropTypes.oneOf(['light', 'dark', 'custom'])
};

export default PricingCard;