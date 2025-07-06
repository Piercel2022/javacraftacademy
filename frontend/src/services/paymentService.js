// services/paymentService.js
import { loadStripe } from '@stripe/stripe-js';
import api from './api';

// Initialisation de Stripe avec votre clé publique
const stripePromise = loadStripe(process.env.REACT_APP_STRIPE_PUBLISHABLE_KEY);

class PaymentService {
  constructor() {
    this.stripe = null;
    this.elements = null;
    this.init();
  }

  async init() {
    this.stripe = await stripePromise;
  }

  // Plans d'abonnement disponibles
  getSubscriptionPlans() {
    return [
      {
        id: 'basic',
        name: 'Plan Étudiant',
        priceMonthly: 19.99,
        priceYearly: 199.99,
        stripePriceIdMonthly: 'price_basic_monthly',
        stripePriceIdYearly: 'price_basic_yearly',
        features: [
          'Accès aux cours de base Java',
          'Éditeur de code intégré',
          'Support par email',
          'Certificats de completion'
        ],
        color: 'blue'
      },
      {
        id: 'premium',
        name: 'Plan Développeur',
        priceMonthly: 39.99,
        priceYearly: 399.99,
        stripePriceIdMonthly: 'price_premium_monthly',
        stripePriceIdYearly: 'price_premium_yearly',
        features: [
          'Tous les cours Java (débutant à expert)',
          'Projets pratiques avancés',
          'Sessions de mentorat en ligne',
          'Accès aux webinaires exclusifs',
          'Support prioritaire 24/7',
          'Accès anticipé aux nouveaux cours'
        ],
        color: 'purple',
        popular: true
      },
      {
        id: 'enterprise',
        name: 'Plan Entreprise',
        priceMonthly: 99.99,
        priceYearly: 999.99,
        stripePriceIdMonthly: 'price_enterprise_monthly',
        stripePriceIdYearly: 'price_enterprise_yearly',
        features: [
          'Accès illimité à tous les contenus',
          'Formation sur mesure',
          'Tableau de bord équipe',
          'Rapports de progression détaillés',
          'Support dédié',
          'Intégration SSO',
          'API personnalisée'
        ],
        color: 'gold'
      }
    ];
  }

  // Créer une session de checkout Stripe
  async createCheckoutSession(planId, billingPeriod, userId) {
    try {
      const plan = this.getSubscriptionPlans().find(p => p.id === planId);
      if (!plan) {
        throw new Error('Plan non trouvé');
      }

      const priceId = billingPeriod === 'yearly' 
        ? plan.stripePriceIdYearly 
        : plan.stripePriceIdMonthly;

      const response = await api.post('/payments/create-checkout-session', {
        priceId,
        userId,
        planId,
        billingPeriod,
        successUrl: `${window.location.origin}/payment/success?session_id={CHECKOUT_SESSION_ID}`,
        cancelUrl: `${window.location.origin}/payment/cancel`
      });

      return response.data;
    } catch (error) {
      console.error('Erreur création session checkout:', error);
      throw new Error('Impossible de créer la session de paiement');
    }
  }

  // Rediriger vers Stripe Checkout
  async redirectToCheckout(sessionId) {
    try {
      if (!this.stripe) {
        await this.init();
      }

      const { error } = await this.stripe.redirectToCheckout({
        sessionId: sessionId
      });

      if (error) {
        throw new Error(error.message);
      }
    } catch (error) {
      console.error('Erreur redirection checkout:', error);
      throw error;
    }
  }

  // Gérer l'abonnement (upgrade/downgrade)
  async manageSubscription(action, newPlanId = null) {
    try {
      const response = await api.post('/payments/manage-subscription', {
        action, // 'cancel', 'pause', 'resume', 'upgrade', 'downgrade'
        newPlanId
      });

      return response.data;
    } catch (error) {
      console.error('Erreur gestion abonnement:', error);
      throw new Error('Impossible de modifier l\'abonnement');
    }
  }

  // Annuler un abonnement
  async cancelSubscription(reason = '') {
    try {
      const response = await api.post('/payments/cancel-subscription', {
        reason,
        cancelAtPeriodEnd: true // L'abonnement continue jusqu'à la fin de la période payée
      });

      return response.data;
    } catch (error) {
      console.error('Erreur annulation abonnement:', error);
      throw new Error('Impossible d\'annuler l\'abonnement');
    }
  }

  // Récupérer les détails de l'abonnement actuel
  async getCurrentSubscription() {
    try {
      const response = await api.get('/payments/subscription');
      return response.data;
    } catch (error) {
      console.error('Erreur récupération abonnement:', error);
      throw new Error('Impossible de récupérer les détails de l\'abonnement');
    }
  }

  // Récupérer l'historique des paiements
  async getPaymentHistory() {
    try {
      const response = await api.get('/payments/history');
      return response.data;
    } catch (error) {
      console.error('Erreur historique paiements:', error);
      throw new Error('Impossible de récupérer l\'historique des paiements');
    }
  }

  // Créer un portail client Stripe pour gérer l'abonnement
  async createCustomerPortalSession() {
    try {
      const response = await api.post('/payments/create-portal-session', {
        returnUrl: `${window.location.origin}/profile/subscription`
      });

      return response.data;
    } catch (error) {
      console.error('Erreur création portail client:', error);
      throw new Error('Impossible d\'ouvrir le portail de gestion');
    }
  }

  // Vérifier le statut de paiement après checkout
  async verifyPaymentSession(sessionId) {
    try {
      const response = await api.get(`/payments/verify-session/${sessionId}`);
      return response.data;
    } catch (error) {
      console.error('Erreur vérification session:', error);
      throw new Error('Impossible de vérifier le paiement');
    }
  }

  // Calculer les remises
  calculateDiscount(monthlyPrice, yearlyPrice) {
    const yearlyMonthly = yearlyPrice / 12;
    const savings = monthlyPrice - yearlyMonthly;
    const percentage = Math.round((savings / monthlyPrice) * 100);
    
    return {
      monthlySavings: savings.toFixed(2),
      percentage,
      yearlyTotal: yearlyPrice.toFixed(2)
    };
  }

  // Formater le prix pour l'affichage
  formatPrice(price, currency = 'EUR') {
    return new Intl.NumberFormat('fr-FR', {
      style: 'currency',
      currency: currency
    }).format(price);
  }
}

// Instance singleton
const paymentService = new PaymentService();
export default paymentService;