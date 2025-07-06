// components/payment/SubscriptionPlans/SubscriptionPlans.jsx
import React, { useState, useEffect } from 'react';
import { Check, Star, Zap, Shield } from 'lucide-react';
import paymentService from '../../../services/paymentService';
import { useAuth } from '../../../hooks/useAuth';
import { useNotification } from '../../../hooks/useNotification';
import Button from '../../common/Button';
import Loading from '../../common/Loading';
import styles from './SubscriptionPlans.module.css';

const SubscriptionPlans = ({ onPlanSelect }) => {
  const [plans, setPlans] = useState([]);
  const [billingPeriod, setBillingPeriod] = useState('monthly');
  const [loading, setLoading] = useState(false);
  const [currentSubscription, setCurrentSubscription] = useState(null);
  
  const { user } = useAuth();
  const { showNotification } = useNotification();

  useEffect(() => {
    loadPlans();
    loadCurrentSubscription();
  }, []);

  const loadPlans = () => {
    const availablePlans = paymentService.getSubscriptionPlans();
    setPlans(availablePlans);
  };

  const loadCurrentSubscription = async () => {
    if (user) {
      try {
        const subscription = await paymentService.getCurrentSubscription();
        setCurrentSubscription(subscription);
      } catch (error) {
        // L'utilisateur n'a pas d'abonnement actuel
      }
    }
  };

  const handlePlanSelect = async (plan) => {
    if (loading) return;

    if (!user) {
      showNotification('Veuillez vous connecter pour continuer', 'warning');
      return;
    }

    setLoading(true);
    
    try {
      const session = await paymentService.createCheckoutSession(
        plan.id,
        billingPeriod,
        user.id
      );

      await paymentService.redirectToCheckout(session.sessionId);
    } catch (error) {
      showNotification(error.message, 'error');
    } finally {
      setLoading(false);
    }
  };

  const getPlanPrice = (plan) => {
    return billingPeriod === 'yearly' ? plan.priceYearly : plan.priceMonthly;
  };

  const getPlanIcon = (planId) => {
    switch (planId) {
      case 'basic':
        return <Star className={styles.planIcon} />;
      case 'premium':
        return <Zap className={styles.planIcon} />;
      case 'enterprise':
        return <Shield className={styles.planIcon} />;
      default:
        return <Star className={styles.planIcon} />;
    }
  };

  const isCurrentPlan = (planId) => {
    return currentSubscription && currentSubscription.planId === planId;
  };

  const getDiscount = (plan) => {
    return paymentService.calculateDiscount(plan.priceMonthly, plan.priceYearly);
  };

  if (loading) {
    return <Loading message="Redirection vers le paiement..." />;
  }

  return (
    <div className={styles.subscriptionPlans}>
      <div className={styles.header}>
        <h2 className={styles.title}>
          Choisissez votre plan d'apprentissage Java
        </h2>
        <p className={styles.subtitle}>
          Déverrouillez tout le potentiel de JavaCraft Academy
        </p>
        
        {/* Toggle de période de facturation */}
        <div className={styles.billingToggle}>
          <div className={styles.toggleContainer}>
            <button
              className={`${styles.toggleButton} ${
                billingPeriod === 'monthly' ? styles.active : ''
              }`}
              onClick={() => setBillingPeriod('monthly')}
            >
              Mensuel
            </button>
            <button
              className={`${styles.toggleButton} ${
                billingPeriod === 'yearly' ? styles.active : ''
              }`}
              onClick={() => setBillingPeriod('yearly')}
            >
              Annuel
              <span className={styles.discountBadge}>-20%</span>
            </button>
          </div>
        </div>
      </div>

      <div className={styles.plansGrid}>
        {plans.map((plan) => {
          const price = getPlanPrice(plan);
          const discount = getDiscount(plan);
          const isPopular = plan.popular;
          const isCurrent = isCurrentPlan(plan.id);

          return (
            <div
              key={plan.id}
              className={`${styles.planCard} ${
                isPopular ? styles.popular : ''
              } ${isCurrent ? styles.current : ''}`}
            >
              {isPopular && (
                <div className={styles.popularBadge}>
                  Plus populaire
                </div>
              )}

              {isCurrent && (
                <div className={styles.currentBadge}>
                  Plan actuel
                </div>
              )}

              <div className={styles.planHeader}>
                <div className={styles.planIconContainer}>
                  {getPlanIcon(plan.id)}
                </div>
                <h3 className={styles.planName}>{plan.name}</h3>
              </div>

              <div className={styles.priceContainer}>
                <div className={styles.price}>
                  <span className={styles.currency}>€</span>
                  <span className={styles.amount}>
                    {Math.floor(price)}
                  </span>
                  <span className={styles.decimal}>
                    {(price % 1).toFixed(2).substring(1)}
                  </span>
                </div>
                <div className={styles.priceFrequency}>
                  /{billingPeriod === 'yearly' ? 'an' : 'mois'}
                </div>
                
                {billingPeriod === 'yearly' && (
                  <div className={styles.savingsInfo}>
                    Économisez {discount.percentage}% 
                    ({paymentService.formatPrice(discount.monthlySavings * 12)}/an)
                  </div>
                )}
              </div>

              <div className={styles.features}>
                {plan.features.map((feature, index) => (
                  <div key={index} className={styles.feature}>
                    <Check className={styles.checkIcon} />
                    <span>{feature}</span>
                  </div>
                ))}
              </div>

              <div className={styles.planActions}>
                {isCurrent ? (
                  <Button
                    variant="outline"
                    size="large"
                    disabled
                    className={styles.currentButton}
                  >
                    Plan actuel
                  </Button>
                ) : (
                  <Button
                    variant={isPopular ? 'primary' : 'outline'}
                    size="large"
                    onClick={() => handlePlanSelect(plan)}
                    disabled={loading}
                    className={styles.selectButton}
                  >
                    {currentSubscription ? 'Changer de plan' : 'Commencer'}
                  </Button>
                )}
              </div>
            </div>
          );
        })}
      </div>

      <div className={styles.footer}>
        <div className={styles.guarantees}>
          <div className={styles.guarantee}>
            <Shield size={20} />
            <span>Paiement sécurisé par Stripe</span>
          </div>
          <div className={styles.guarantee}>
            <Check size={20} />
            <span>Annulation à tout moment</span>
          </div>
          <div className={styles.guarantee}>
            <Star size={20} />
            <span>Support client 24/7</span>
          </div>
        </div>
        
        <p className={styles.footerNote}>
          Tous les prix sont TTC. Vous pouvez annuler votre abonnement à tout moment.
          L'accès reste actif jusqu'à la fin de votre période de facturation.
        </p>
      </div>
    </div>
  );
};

export default SubscriptionPlans;