import React, { useEffect, useState } from 'react';
import { CheckCircle, ArrowRight, Download, Calendar, CreditCard, Gift, Clock } from 'lucide-react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import paymentService from '../../../services/paymentService';
import { useAuth } from '../../../hooks/useAuth';
import Button from '../../common/Button';
import Loading from '../../common/Loading';
import styles from './PaymentSuccess.module.css';

const PaymentSuccess = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const { user, refreshUser } = useAuth();
  
  const [loading, setLoading] = useState(true);
  const [paymentData, setPaymentData] = useState(null);
  const [error, setError] = useState(null);

  useEffect(() => {
    verifyPayment();
  }, []);

  const verifyPayment = async () => {
    const sessionId = searchParams.get('session_id');
    
    if (!sessionId) {
      setError('Session de paiement introuvable');
      setLoading(false);
      return;
    }

    try {
      const result = await paymentService.verifyPaymentSession(sessionId);
      setPaymentData(result);
      
      // Rafraîchir les données utilisateur pour mettre à jour l'abonnement
      if (refreshUser) {
        await refreshUser();
      }
    } catch (err) {
      setError('Impossible de vérifier le paiement');
      console.error('Erreur vérification paiement:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleContinueToDashboard = () => {
    navigate('/dashboard');
  };

  const handleDownloadInvoice = async () => {
    try {
      const invoiceUrl = paymentData?.invoiceUrl;
      if (invoiceUrl) {
        window.open(invoiceUrl, '_blank');
      }
    } catch (error) {
      console.error('Erreur téléchargement facture:', error);
    }
  };

  const handleExploreCourses = () => {
    navigate('/courses');
  };

  const formatDate = (dateString) => {
    if (!dateString) return '';
    return new Date(dateString).toLocaleDateString('fr-FR', {
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    });
  };

  if (loading) {
    return (
      <div className={styles.container}>
        <Loading message="Vérification de votre paiement..." />
      </div>
    );
  }

  if (error) {
    return (
      <div className={styles.container}>
        <div className={styles.errorCard}>
          <div className={styles.errorIcon}>❌</div>
          <h2>Erreur de vérification</h2>
          <p>{error}</p>
          <Button onClick={() => navigate('/pricing')} variant="primary">
            Retour aux plans
          </Button>
        </div>
      </div>
    );
  }

  if (!paymentData) {
    return null;
  }

  const { subscription, customer, invoice } = paymentData;

  return (
    <div className={styles.container}>
      <div className={styles.successCard}>
        {/* Animation de succès */}
        <div className={styles.successAnimation}>
          <CheckCircle className={styles.successIcon} />
          <div className={styles.successRipple}></div>
        </div>

        {/* Titre principal */}
        <h1 className={styles.title}>
          Félicitations, {user?.firstName || 'cher apprenant'} ! 🎉
        </h1>
        
        <p className={styles.subtitle}>
          Votre abonnement à JavaCraft Academy est maintenant actif
        </p>

        {/* Détails de l'abonnement */}
        <div className={styles.subscriptionDetails}>
          <div className={styles.planBadge}>
            <span className={styles.planName}>{subscription?.planName}</span>
            <span className={styles.planPrice}>
              {paymentService.formatPrice(subscription?.amount)}
              /{subscription?.interval === 'year' ? 'an' : 'mois'}
            </span>
          </div>

          <div className={styles.detailsGrid}>
            <div className={styles.detailItem}>
              <Calendar className={styles.detailIcon} />
              <div>
                <span className={styles.detailLabel}>Date d'activation</span>
                <span className={styles.detailValue}>
                  {formatDate(subscription?.startDate)}
                </span>
              </div>
            </div>

            <div className={styles.detailItem}>
              <Clock className={styles.detailIcon} />
              <div>
                <span className={styles.detailLabel}>Prochaine facturation</span>
                <span className={styles.detailValue}>
                  {formatDate(subscription?.nextBillingDate)}
                </span>
              </div>
            </div>

            <div className={styles.detailItem}>
              <CreditCard className={styles.detailIcon} />
              <div>
                <span className={styles.detailLabel}>Numéro de facture</span>
                <span className={styles.detailValue}>
                  {invoice?.number || 'N/A'}
                </span>
              </div>
            </div>
          </div>
        </div>

        {/* Avantages débloqués */}
        <div className={styles.benefits}>
          <div className={styles.benefitsHeader}>
            <Gift className={styles.giftIcon} />
            <h3>Vous avez maintenant accès à :</h3>
          </div>
          
          <ul className={styles.benefitsList}>
            <li>✨ Tous les cours Java premium</li>
            <li>🚀 Projets pratiques guidés</li>
            <li>💻 Environnement de développement intégré</li>
            <li>🎯 Suivi personnalisé de progression</li>
            <li>📞 Support prioritaire</li>
            <li>🏆 Certificats de completion</li>
          </ul>
        </div>

        {/* Actions */}
        <div className={styles.actions}>
          <Button 
            onClick={handleContinueToDashboard}
            variant="primary"
            size="large"
            className={styles.primaryAction}
          >
            Accéder au tableau de bord
            <ArrowRight className={styles.actionIcon} />
          </Button>

          <div className={styles.secondaryActions}>
            <Button 
              onClick={handleExploreCourses}
              variant="outline"
              size="medium"
            >
              Explorer les cours
            </Button>

            {invoice?.downloadUrl && (
              <Button 
                onClick={handleDownloadInvoice}
                variant="ghost"
                size="medium"
              >
                <Download className={styles.downloadIcon} />
                Télécharger la facture
              </Button>
            )}
          </div>
        </div>

        {/* Message de confirmation */}
        <div className={styles.confirmationMessage}>
          <p>
            Un email de confirmation a été envoyé à <strong>{customer?.email || user?.email}</strong>
          </p>
        </div>
      </div>
    </div>
  );
};

export default PaymentSuccess;