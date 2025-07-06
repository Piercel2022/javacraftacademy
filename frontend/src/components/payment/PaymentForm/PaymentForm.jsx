import React, { useState, useEffect, useContext } from 'react';
import { useNavigate } from 'react-router-dom';
import styles from './PaymentForm.module.css';
import { PaymentContext } from '../../../context/PaymentContext';
import { AuthContext } from '../../../context/AuthContext';
import { NotificationContext } from '../../../context/NotificationContext';
import { usePayment } from '../../../hooks/usePayment';
import { useStripe } from '../../../hooks/useStripe';
import { usePaypal } from '../../../hooks/usePaypal';
import { paymentService } from '../../../services/paymentService';
import { stripeService } from '../../../services/stripeService';
import { paypalService } from '../../../services/paypalService';
import { billingService } from '../../../services/billingService';
import { promoCodeService } from '../../../services/promoCodeService';
import { validatePaymentForm } from '../../../utils/validators';
import { formatCurrency } from '../../../utils/currencyUtils';
import { formatPrice } from '../../../utils/priceUtils';
import { securityUtils } from '../../../utils/securityUtils';
import Button from '../../common/Button';
import Loading from '../../common/Loading';
import CreditCardForm from '../CreditCardForm';
import PaymentMethod from '../PaymentMethod';
import BillingAddress from '../BillingAddress';
import PaymentSummary from '../PaymentSummary';
import PromoCode from '../PromoCode';
import PaymentSecurityBadge from '../Security/PaymentSecurityBadge';

/**
 * Composant PaymentForm - Formulaire de paiement principal
 * 
 * Ce composant centralise toute la logique de paiement de l'application JavaCraft Academy.
 * Il gère les différentes méthodes de paiement (cartes de crédit, PayPal, Apple Pay),
 * la validation des données, la sécurité des transactions et l'intégration avec
 * les services de paiement externes.
 * 
 * @component
 * @example
 * // Utilisation simple avec un cours
 * <PaymentForm 
 *   itemType="course"
 *   itemId="java-fundamentals"
 *   amount={99.99}
 *   currency="EUR"
 *   onSuccess={handlePaymentSuccess}
 *   onError={handlePaymentError}
 * />
 * 
 * @example
 * // Utilisation avec un abonnement
 * <PaymentForm 
 *   itemType="subscription"
 *   itemId="premium-plan"
 *   amount={19.99}
 *   currency="EUR"
 *   recurring={true}
 *   billingCycle="monthly"
 *   onSuccess={handleSubscriptionSuccess}
 * />
 * 
 * @param {Object} props - Propriétés du composant
 * @param {string} props.itemType - Type d'élément à acheter ('course', 'subscription', 'bundle')
 * @param {string} props.itemId - Identifiant unique de l'élément
 * @param {number} props.amount - Montant à payer
 * @param {string} props.currency - Code de devise (EUR, USD, etc.)
 * @param {boolean} props.recurring - Indique si c'est un paiement récurrent
 * @param {string} props.billingCycle - Cycle de facturation ('monthly', 'yearly')
 * @param {Function} props.onSuccess - Callback appelé en cas de succès
 * @param {Function} props.onError - Callback appelé en cas d'erreur
 * @param {Function} props.onCancel - Callback appelé en cas d'annulation
 * @param {Object} props.metadata - Métadonnées supplémentaires
 * @param {boolean} props.showSummary - Afficher ou non le résumé du paiement
 * @param {boolean} props.enablePromoCode - Activer les codes promotionnels
 * @param {string} props.theme - Thème du formulaire ('light', 'dark')
 * @param {Object} props.customStyles - Styles personnalisés
 * 
 * @returns {JSX.Element} Composant PaymentForm
 * 
 * @author JavaCraft Academy
 * @version 1.0.0
 * @since 2024-01-01
 * 
 * @description
 * Relations avec l'application :
 * - Utilise PaymentContext pour l'état global des paiements
 * - Intègre AuthContext pour l'authentification utilisateur
 * - Communique avec NotificationContext pour les messages
 * - Utilise les hooks usePayment, useStripe, usePaypal pour la logique métier
 * - Intègre les services de paiement (Stripe, PayPal, Apple Pay)
 * - Valide les données avec validators.js
 * - Formate les prix avec currencyUtils et priceUtils
 * - Sécurise les transactions avec securityUtils
 * - Affiche les composants enfants : CreditCardForm, PaymentMethod, etc.
 * - Redirige vers les pages de succès/échec via useNavigate
 * - Communique avec le backend via les services API
 * - Gère les codes promotionnels via promoCodeService
 * - Sauvegarde les données de facturation via billingService
 * - Envoie les métriques de performance via reportWebVitals
 */
const PaymentForm = ({
  itemType = 'course',
  itemId,
  amount,
  currency = 'EUR',
  recurring = false,
  billingCycle = 'monthly',
  onSuccess,
  onError,
  onCancel,
  metadata = {},
  showSummary = true,
  enablePromoCode = true,
  theme = 'light',
  customStyles = {},
  ...props
}) => {
  // Hooks et contextes
  const navigate = useNavigate();
  const { user } = useContext(AuthContext);
  const { showNotification } = useContext(NotificationContext);
  const { 
    paymentState, 
    updatePaymentState, 
    resetPaymentState 
  } = useContext(PaymentContext);
  
  const {
    processPayment,
    validatePayment,
    isProcessing,
    paymentError,
    clearPaymentError
  } = usePayment();
  
  const {
    stripe,
    elements,
    isStripeReady,
    createPaymentIntent,
    confirmPayment
  } = useStripe();
  
  const {
    paypal,
    isPaypalReady,
    createPaypalOrder,
    capturePaypalPayment
  } = usePaypal();

  // État local du composant
  const [formData, setFormData] = useState({
    paymentMethod: 'card', // 'card', 'paypal', 'apple_pay'
    cardDetails: {
      number: '',
      expiry: '',
      cvc: '',
      name: ''
    },
    billingAddress: {
      firstName: user?.firstName || '',
      lastName: user?.lastName || '',
      email: user?.email || '',
      address1: '',
      address2: '',
      city: '',
      state: '',
      zipCode: '',
      country: 'FR'
    },
    savePaymentMethod: false,
    agreeToTerms: false
  });

  const [promoCode, setPromoCode] = useState('');
  const [discount, setDiscount] = useState(0);
  const [finalAmount, setFinalAmount] = useState(amount);
  const [validationErrors, setValidationErrors] = useState({});
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [paymentIntentId, setPaymentIntentId] = useState(null);

  /**
   * Calcule le montant final après application du code promotionnel
   * @param {number} baseAmount - Montant de base
   * @param {number} discountPercent - Pourcentage de remise
   * @returns {number} Montant final
   */
  const calculateFinalAmount = (baseAmount, discountPercent) => {
    const discountAmount = (baseAmount * discountPercent) / 100;
    return Math.max(0, baseAmount - discountAmount);
  };

  /**
   * Applique un code promotionnel
   * @param {string} code - Code promotionnel
   */
  const applyPromoCode = async (code) => {
    try {
      const promoResult = await promoCodeService.validatePromoCode(code, {
        itemType,
        itemId,
        amount,
        userId: user?.id
      });

      if (promoResult.valid) {
        setDiscount(promoResult.discount);
        setFinalAmount(calculateFinalAmount(amount, promoResult.discount));
        showNotification('Code promotionnel appliqué avec succès!', 'success');
      } else {
        showNotification('Code promotionnel invalide', 'error');
      }
    } catch (error) {
      console.error('Erreur lors de l\'application du code promo:', error);
      showNotification('Erreur lors de l\'application du code promotionnel', 'error');
    }
  };

  /**
   * Valide les données du formulaire
   * @param {Object} data - Données à valider
   * @returns {Object} Erreurs de validation
   */
  const validateForm = (data) => {
    const errors = {};

    // Validation des données de paiement
    const paymentValidation = validatePaymentForm(data);
    if (paymentValidation.errors) {
      Object.assign(errors, paymentValidation.errors);
    }

    // Validation des conditions générales
    if (!data.agreeToTerms) {
      errors.agreeToTerms = 'Vous devez accepter les conditions générales';
    }

    // Validation spécifique selon la méthode de paiement
    if (data.paymentMethod === 'card') {
      if (!data.cardDetails.number) {
        errors.cardNumber = 'Numéro de carte requis';
      }
      if (!data.cardDetails.expiry) {
        errors.cardExpiry = 'Date d\'expiration requise';
      }
      if (!data.cardDetails.cvc) {
        errors.cardCvc = 'Code CVC requis';
      }
      if (!data.cardDetails.name) {
        errors.cardName = 'Nom du titulaire requis';
      }
    }

    return errors;
  };

  /**
   * Traite le paiement par carte de crédit via Stripe
   * @param {Object} paymentData - Données de paiement
   */
  const processCardPayment = async (paymentData) => {
    try {
      // Création de l'intention de paiement
      const paymentIntent = await createPaymentIntent({
        amount: Math.round(finalAmount * 100), // En centimes
        currency: currency.toLowerCase(),
        metadata: {
          ...metadata,
          itemType,
          itemId,
          userId: user?.id,
          promoCode: promoCode || null,
          discount: discount || 0
        }
      });

      setPaymentIntentId(paymentIntent.id);

      // Confirmation du paiement
      const result = await confirmPayment({
        elements,
        confirmParams: {
          return_url: `${window.location.origin}/payment/success`,
          payment_method_data: {
            billing_details: {
              name: paymentData.cardDetails.name,
              email: paymentData.billingAddress.email,
              address: {
                line1: paymentData.billingAddress.address1,
                line2: paymentData.billingAddress.address2,
                city: paymentData.billingAddress.city,
                state: paymentData.billingAddress.state,
                postal_code: paymentData.billingAddress.zipCode,
                country: paymentData.billingAddress.country
              }
            }
          }
        }
      });

      if (result.error) {
        throw new Error(result.error.message);
      }

      return result;
    } catch (error) {
      console.error('Erreur lors du paiement par carte:', error);
      throw error;
    }
  };

  /**
   * Traite le paiement via PayPal
   * @param {Object} paymentData - Données de paiement
   */
  const processPaypalPayment = async (paymentData) => {
    try {
      // Création de la commande PayPal
      const order = await createPaypalOrder({
        amount: finalAmount,
        currency,
        items: [{
          name: `${itemType === 'course' ? 'Cours' : 'Abonnement'} JavaCraft Academy`,
          quantity: 1,
          price: finalAmount,
          currency
        }],
        metadata: {
          ...metadata,
          itemType,
          itemId,
          userId: user?.id,
          promoCode: promoCode || null,
          discount: discount || 0
        }
      });

      // Capture du paiement
      const result = await capturePaypalPayment(order.id);
      
      return result;
    } catch (error) {
      console.error('Erreur lors du paiement PayPal:', error);
      throw error;
    }
  };

  /**
   * Gère la soumission du formulaire
   * @param {Event} e - Événement du formulaire
   */
  const handleSubmit = async (e) => {
    e.preventDefault();
    
    // Validation du formulaire
    const errors = validateForm(formData);
    if (Object.keys(errors).length > 0) {
      setValidationErrors(errors);
      return;
    }

    setIsSubmitting(true);
    setValidationErrors({});

    try {
      let paymentResult;

      // Traitement selon la méthode de paiement
      switch (formData.paymentMethod) {
        case 'card':
          paymentResult = await processCardPayment(formData);
          break;
        case 'paypal':
          paymentResult = await processPaypalPayment(formData);
          break;
        default:
          throw new Error('Méthode de paiement non supportée');
      }

      // Sauvegarde des données de facturation
      if (formData.savePaymentMethod) {
        await billingService.saveBillingAddress(user.id, formData.billingAddress);
      }

      // Enregistrement de la transaction
      await paymentService.recordTransaction({
        userId: user.id,
        itemType,
        itemId,
        amount: finalAmount,
        currency,
        paymentMethod: formData.paymentMethod,
        paymentIntentId: paymentResult.paymentIntent?.id,
        status: 'completed',
        metadata: {
          ...metadata,
          promoCode: promoCode || null,
          discount: discount || 0
        }
      });

      // Notification de succès
      showNotification('Paiement effectué avec succès!', 'success');

      // Callback de succès
      if (onSuccess) {
        onSuccess({
          paymentResult,
          formData,
          finalAmount,
          currency
        });
      }

      // Redirection vers la page de succès
      navigate('/payment/success', {
        state: {
          paymentResult,
          itemType,
          itemId,
          amount: finalAmount,
          currency
        }
      });

    } catch (error) {
      console.error('Erreur lors du paiement:', error);
      
      // Notification d'erreur
      showNotification(
        error.message || 'Une erreur est survenue lors du paiement',
        'error'
      );

      // Callback d'erreur
      if (onError) {
        onError(error);
      }

      // Redirection vers la page d'erreur
      navigate('/payment/failed', {
        state: {
          error: error.message,
          itemType,
          itemId,
          amount: finalAmount,
          currency
        }
      });
    } finally {
      setIsSubmitting(false);
    }
  };

  /**
   * Met à jour les données du formulaire
   * @param {string} field - Champ à mettre à jour
   * @param {*} value - Nouvelle valeur
   */
  const updateFormData = (field, value) => {
    setFormData(prev => ({
      ...prev,
      [field]: value
    }));
  };

  /**
   * Met à jour les détails de la carte
   * @param {Object} cardData - Données de la carte
   */
  const updateCardDetails = (cardData) => {
    setFormData(prev => ({
      ...prev,
      cardDetails: {
        ...prev.cardDetails,
        ...cardData
      }
    }));
  };

  /**
   * Met à jour l'adresse de facturation
   * @param {Object} addressData - Données d'adresse
   */
  const updateBillingAddress = (addressData) => {
    setFormData(prev => ({
      ...prev,
      billingAddress: {
        ...prev.billingAddress,
        ...addressData
      }
    }));
  };

  /**
   * Gère l'annulation du paiement
   */
  const handleCancel = () => {
    if (onCancel) {
      onCancel();
    }
    navigate(-1); // Retour à la page précédente
  };

  // Effet pour initialiser les données utilisateur
  useEffect(() => {
    if (user) {
      updateBillingAddress({
        firstName: user.firstName || '',
        lastName: user.lastName || '',
        email: user.email || ''
      });
    }
  }, [user]);

  // Effet pour calculer le montant final
  useEffect(() => {
    setFinalAmount(calculateFinalAmount(amount, discount));
  }, [amount, discount]);

  // Nettoyage lors du démontage
  useEffect(() => {
    return () => {
      clearPaymentError();
      resetPaymentState();
    };
  }, []);

  // Affichage du chargement
  if (isProcessing || isSubmitting) {
    return (
      <div className={styles.loadingContainer}>
        <Loading size="large" message="Traitement du paiement en cours..." />
      </div>
    );
  }

  return (
    <div className={`${styles.paymentForm} ${styles[theme]}`} style={customStyles}>
      <div className={styles.container}>
        <div className={styles.header}>
          <h2 className={styles.title}>Finaliser votre achat</h2>
          <PaymentSecurityBadge />
        </div>

        <form onSubmit={handleSubmit} className={styles.form}>
          {/* Résumé du paiement */}
          {showSummary && (
            <div className={styles.summarySection}>
              <PaymentSummary
                itemType={itemType}
                itemId={itemId}
                amount={amount}
                discount={discount}
                finalAmount={finalAmount}
                currency={currency}
                promoCode={promoCode}
                recurring={recurring}
                billingCycle={billingCycle}
              />
            </div>
          )}

          {/* Code promotionnel */}
          {enablePromoCode && (
            <div className={styles.promoSection}>
              <PromoCode
                value={promoCode}
                onChange={setPromoCode}
                onApply={applyPromoCode}
                disabled={isSubmitting}
              />
            </div>
          )}

          {/* Méthode de paiement */}
          <div className={styles.paymentMethodSection}>
            <h3 className={styles.sectionTitle}>Méthode de paiement</h3>
            <PaymentMethod
              selectedMethod={formData.paymentMethod}
              onMethodChange={(method) => updateFormData('paymentMethod', method)}
              disabled={isSubmitting}
            />
          </div>

          {/* Détails de la carte (si carte sélectionnée) */}
          {formData.paymentMethod === 'card' && (
            <div className={styles.cardSection}>
              <h3 className={styles.sectionTitle}>Informations de la carte</h3>
              <CreditCardForm
                cardDetails={formData.cardDetails}
                onCardChange={updateCardDetails}
                errors={validationErrors}
                disabled={isSubmitting}
              />
            </div>
          )}

          {/* Adresse de facturation */}
          <div className={styles.billingSection}>
            <h3 className={styles.sectionTitle}>Adresse de facturation</h3>
            <BillingAddress
              address={formData.billingAddress}
              onAddressChange={updateBillingAddress}
              errors={validationErrors}
              disabled={isSubmitting}
            />
          </div>

          {/* Options supplémentaires */}
          <div className={styles.optionsSection}>
            <label className={styles.checkboxLabel}>
              <input
                type="checkbox"
                checked={formData.savePaymentMethod}
                onChange={(e) => updateFormData('savePaymentMethod', e.target.checked)}
                disabled={isSubmitting}
              />
              <span className={styles.checkboxText}>
                Sauvegarder cette méthode de paiement pour les prochains achats
              </span>
            </label>

            <label className={styles.checkboxLabel}>
              <input
                type="checkbox"
                checked={formData.agreeToTerms}
                onChange={(e) => updateFormData('agreeToTerms', e.target.checked)}
                disabled={isSubmitting}
                required
              />
              <span className={styles.checkboxText}>
                J'accepte les{' '}
                <a href="/terms" target="_blank" rel="noopener noreferrer">
                  conditions générales
                </a>{' '}
                et la{' '}
                <a href="/privacy" target="_blank" rel="noopener noreferrer">
                  politique de confidentialité
                </a>
              </span>
            </label>
            {validationErrors.agreeToTerms && (
              <span className={styles.error}>{validationErrors.agreeToTerms}</span>
            )}
          </div>

          {/* Boutons d'action */}
          <div className={styles.actions}>
            <Button
              type="button"
              variant="secondary"
              onClick={handleCancel}
              disabled={isSubmitting}
              className={styles.cancelButton}
            >
              Annuler
            </Button>
            <Button
              type="submit"
              variant="primary"
              disabled={isSubmitting || !formData.agreeToTerms}
              className={styles.submitButton}
            >
              {isSubmitting ? (
                <>
                  <Loading size="small" />
                  Traitement...
                </>
              ) : (
                `Payer ${formatCurrency(finalAmount, currency)}`
              )}
            </Button>
          </div>
        </form>

        {/* Erreurs globales */}
        {paymentError && (
          <div className={styles.errorMessage}>
            {paymentError}
          </div>
        )}
      </div>
    </div>
  );
};

export default PaymentForm;