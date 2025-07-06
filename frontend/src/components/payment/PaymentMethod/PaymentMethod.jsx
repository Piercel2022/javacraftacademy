import React, { useState, useEffect, useCallback } from 'react';
import PropTypes from 'prop-types';
import { usePayment } from '../../../hooks/usePayment';
import { useAuth } from '../../../hooks/useAuth';
import { useNotification } from '../../../hooks/useNotification';
import { CreditCard, Wallet, Phone, Shield, CheckCircle, XCircle, Plus, Trash2, Edit3 } from 'lucide-react';
import styles from './PaymentMethod.module.css';

/**
 * Composant PaymentMethod - Gestion des m�thodes de paiement utilisateur
 * 
 * Ce composant permet aux utilisateurs de:
 * - Visualiser leurs m�thodes de paiement enregistr�es
 * - Ajouter de nouvelles m�thodes de paiement
 * - Modifier les m�thodes existantes
 * - Supprimer des m�thodes de paiement
 * - D�finir une m�thode par d�faut
 * 
 * Relations avec l'application:
 * - Utilise usePayment pour la gestion des paiements
 * - Utilise useAuth pour l'authentification utilisateur
 * - Utilise useNotification pour les notifications
 * - Connect� aux services de paiement (Stripe, PayPal, etc.)
 * - Int�gr� dans les pages de checkout et de profil
 * 
 * @component
 * @param {Object} props - Propri�t�s du composant
 * @param {Array} props.methods - Liste des m�thodes de paiement
 * @param {Function} props.onMethodSelect - Callback lors de la s�lection d'une m�thode
 * @param {Function} props.onMethodAdd - Callback lors de l'ajout d'une m�thode
 * @param {Function} props.onMethodUpdate - Callback lors de la modification d'une m�thode
 * @param {Function} props.onMethodDelete - Callback lors de la suppression d'une m�thode
 * @param {string} props.selectedMethodId - ID de la m�thode s�lectionn�e
 * @param {boolean} props.allowSelection - Permet la s�lection de m�thodes
 * @param {boolean} props.allowEdit - Permet l'�dition des m�thodes
 * @param {boolean} props.showAddButton - Affiche le bouton d'ajout
 * @param {string} props.context - Contexte d'utilisation ('checkout', 'profile', 'subscription')
 * @param {string} props.className - Classes CSS personnalis�es
 * @returns {JSX.Element} Composant PaymentMethod
 */
const PaymentMethod = ({
  methods = [],
  onMethodSelect,
  onMethodAdd,
  onMethodUpdate,
  onMethodDelete,
  selectedMethodId,
  allowSelection = true,
  allowEdit = true,
  showAddButton = true,
  context = 'profile',
  className = ''
}) => {
  // �tats locaux
  const [isLoading, setIsLoading] = useState(false);
  const [editingMethodId, setEditingMethodId] = useState(null);
  const [showAddForm, setShowAddForm] = useState(false);
  const [selectedMethod, setSelectedMethod] = useState(selectedMethodId);

  // Hooks personnalis�s
  const { user } = useAuth();
  const { showNotification } = useNotification();
  const {
    paymentMethods,
    addPaymentMethod,
    updatePaymentMethod,
    deletePaymentMethod,
    setDefaultPaymentMethod,
    isProcessing,
    error
  } = usePayment();

  /**
   * Effet pour synchroniser la m�thode s�lectionn�e
   */
  useEffect(() => {
    setSelectedMethod(selectedMethodId);
  }, [selectedMethodId]);

  /**
   * Effet pour g�rer les erreurs de paiement
   */
  useEffect(() => {
    if (error) {
      showNotification({
        type: 'error',
        message: error.message || 'Une erreur est survenue lors de la gestion des m�thodes de paiement',
        duration: 5000
      });
    }
  }, [error, showNotification]);

  /**
   * Obtient l'ic�ne appropri�e pour le type de m�thode de paiement
   * @param {string} type - Type de m�thode de paiement
   * @param {string} brand - Marque de la carte (pour les cartes de cr�dit)
   * @returns {JSX.Element} Ic�ne correspondante
   */
  const getPaymentIcon = useCallback((type, brand) => {
    switch (type) {
      case 'card':
        return <CreditCard className={styles.paymentIcon} />;
      case 'paypal':
        return <Wallet className={styles.paymentIcon} />;
      case 'apple_pay':
      case 'google_pay':
        return <Phone className={styles.paymentIcon} />;
      default:
        return <Shield className={styles.paymentIcon} />;
    }
  }, []);

  /**
   * Formate les informations d'affichage d'une m�thode de paiement
   * @param {Object} method - M�thode de paiement
   * @returns {Object} Informations format�es
   */
  const formatPaymentMethod = useCallback((method) => {
    switch (method.type) {
      case 'card':
        return {
          title: `${method.brand?.toUpperCase()} \u2022\u2022\u2022\u2022 ${method.last4}`,
          subtitle: `Expire ${method.exp_month}/${method.exp_year}`,
          icon: getPaymentIcon('card', method.brand)
        };
      case 'paypal':
        return {
          title: 'PayPal',
          subtitle: method.email || 'Compte PayPal',
          icon: getPaymentIcon('paypal')
        };
      case 'apple_pay':
        return {
          title: 'Apple Pay',
          subtitle: `${method.brand?.toUpperCase()} \u2022\u2022\u2022\u2022 ${method.last4}`,
          icon: getPaymentIcon('apple_pay')
        };
      case 'google_pay':
        return {
          title: 'Google Pay',
          subtitle: `${method.brand?.toUpperCase()} \u2022\u2022\u2022\u2022 ${method.last4}`,
          icon: getPaymentIcon('google_pay')
        };
      default:
        return {
          title: 'M�thode de paiement',
          subtitle: 'M�thode personnalis�e',
          icon: getPaymentIcon('default')
        };
    }
  }, [getPaymentIcon]);

  /**
   * G�re la s�lection d'une m�thode de paiement
   * @param {string} methodId - ID de la m�thode s�lectionn�e
   */
  const handleMethodSelect = useCallback((methodId) => {
    if (!allowSelection) return;
    
    setSelectedMethod(methodId);
    onMethodSelect?.(methodId);
    
    showNotification({
      type: 'info',
      message: 'M�thode de paiement s�lectionn�e',
      duration: 3000
    });
  }, [allowSelection, onMethodSelect, showNotification]);

  /**
   * G�re l'ajout d'une nouvelle m�thode de paiement
   * @param {Object} methodData - Donn�es de la nouvelle m�thode
   */
  const handleMethodAdd = useCallback(async (methodData) => {
    setIsLoading(true);
    
    try {
      const newMethod = await addPaymentMethod(methodData);
      
      onMethodAdd?.(newMethod);
      setShowAddForm(false);
      
      showNotification({
        type: 'success',
        message: 'M�thode de paiement ajout�e avec succ�s',
        duration: 4000
      });
    } catch (error) {
      showNotification({
        type: 'error',
        message: error.message || 'Erreur lors de l\'ajout de la m�thode de paiement',
        duration: 5000
      });
    } finally {
      setIsLoading(false);
    }
  }, [addPaymentMethod, onMethodAdd, showNotification]);

  /**
   * G�re la modification d'une m�thode de paiement
   * @param {string} methodId - ID de la m�thode � modifier
   * @param {Object} updateData - Donn�es de mise � jour
   */
  const handleMethodUpdate = useCallback(async (methodId, updateData) => {
    setIsLoading(true);
    
    try {
      const updatedMethod = await updatePaymentMethod(methodId, updateData);
      
      onMethodUpdate?.(updatedMethod);
      setEditingMethodId(null);
      
      showNotification({
        type: 'success',
        message: 'M�thode de paiement mise � jour',
        duration: 4000
      });
    } catch (error) {
      showNotification({
        type: 'error',
        message: error.message || 'Erreur lors de la mise � jour',
        duration: 5000
      });
    } finally {
      setIsLoading(false);
    }
  }, [updatePaymentMethod, onMethodUpdate, showNotification]);

  /**
   * G�re la suppression d'une m�thode de paiement
   * @param {string} methodId - ID de la m�thode � supprimer
   */
  const handleMethodDelete = useCallback(async (methodId) => {
    if (!window.confirm('�tes-vous s�r de vouloir supprimer cette m�thode de paiement ?')) {
      return;
    }
    
    setIsLoading(true);
    
    try {
      await deletePaymentMethod(methodId);
      
      onMethodDelete?.(methodId);
      
      // Si la m�thode supprim�e �tait s�lectionn�e, d�s�lectionner
      if (selectedMethod === methodId) {
        setSelectedMethod(null);
        onMethodSelect?.(null);
      }
      
      showNotification({
        type: 'success',
        message: 'M�thode de paiement supprim�e',
        duration: 4000
      });
    } catch (error) {
      showNotification({
        type: 'error',
        message: error.message || 'Erreur lors de la suppression',
        duration: 5000
      });
    } finally {
      setIsLoading(false);
    }
  }, [deletePaymentMethod, onMethodDelete, selectedMethod, onMethodSelect, showNotification]);

  /**
   * G�re la d�finition d'une m�thode par d�faut
   * @param {string} methodId - ID de la m�thode � d�finir par d�faut
   */
  const handleSetDefault = useCallback(async (methodId) => {
    setIsLoading(true);
    
    try {
      await setDefaultPaymentMethod(methodId);
      
      showNotification({
        type: 'success',
        message: 'M�thode de paiement d�finie par d�faut',
        duration: 4000
      });
    } catch (error) {
      showNotification({
        type: 'error',
        message: error.message || 'Erreur lors de la d�finition par d�faut',
        duration: 5000
      });
    } finally {
      setIsLoading(false);
    }
  }, [setDefaultPaymentMethod, showNotification]);

  /**
   * Rendu d'une m�thode de paiement individuelle
   * @param {Object} method - M�thode de paiement
   * @returns {JSX.Element} �l�ment de m�thode de paiement
   */
  const renderPaymentMethod = useCallback((method) => {
    const { title, subtitle, icon } = formatPaymentMethod(method);
    const isSelected = selectedMethod === method.id;
    const isDefault = method.is_default;
    
    return (
      <div
        key={method.id}
        className={`${styles.paymentMethod} ${isSelected ? styles.selected : ''} ${isDefault ? styles.default : ''}`}
        onClick={() => handleMethodSelect(method.id)}
        role="button"
        tabIndex={0}
        onKeyDown={(e) => {
          if (e.key === 'Enter' || e.key === ' ') {
            handleMethodSelect(method.id);
          }
        }}
      >
        <div className={styles.methodInfo}>
          <div className={styles.methodIcon}>
            {icon}
          </div>
          <div className={styles.methodDetails}>
            <h4 className={styles.methodTitle}>{title}</h4>
            <p className={styles.methodSubtitle}>{subtitle}</p>
            {isDefault && (
              <span className={styles.defaultBadge}>
                <CheckCircle size={12} />
                Par d�faut
              </span>
            )}
          </div>
        </div>
        
        {allowEdit && (
          <div className={styles.methodActions}>
            {!isDefault && (
              <button
                className={styles.actionButton}
                onClick={(e) => {
                  e.stopPropagation();
                  handleSetDefault(method.id);
                }}
                title="D�finir par d�faut"
                disabled={isLoading}
              >
                <CheckCircle size={16} />
              </button>
            )}
            
            <button
              className={styles.actionButton}
              onClick={(e) => {
                e.stopPropagation();
                setEditingMethodId(method.id);
              }}
              title="Modifier"
              disabled={isLoading}
            >
              <Edit3 size={16} />
            </button>
            
            <button
              className={`${styles.actionButton} ${styles.deleteButton}`}
              onClick={(e) => {
                e.stopPropagation();
                handleMethodDelete(method.id);
              }}
              title="Supprimer"
              disabled={isLoading}
            >
              <Trash2 size={16} />
            </button>
          </div>
        )}
        
        {isSelected && (
          <div className={styles.selectionIndicator}>
            <CheckCircle className={styles.selectionIcon} />
          </div>
        )}
      </div>
    );
  }, [
    formatPaymentMethod,
    selectedMethod,
    allowEdit,
    isLoading,
    handleMethodSelect,
    handleSetDefault,
    handleMethodDelete
  ]);

  // Utilise les m�thodes pass�es en props ou celles du hook
  const displayMethods = methods.length > 0 ? methods : paymentMethods;

  return (
    <div className={`${styles.paymentMethodContainer} ${className}`}>
      <div className={styles.header}>
        <h3 className={styles.title}>
          {context === 'checkout' ? 'M�thodes de paiement' : 'Mes m�thodes de paiement'}
        </h3>
        {showAddButton && (
          <button
            className={styles.addButton}
            onClick={() => setShowAddForm(true)}
            disabled={isLoading}
          >
            <Plus size={16} />
            Ajouter une m�thode
          </button>
        )}
      </div>
      
      {displayMethods.length === 0 ? (
        <div className={styles.emptyState}>
          <CreditCard className={styles.emptyIcon} />
          <h4>Aucune m�thode de paiement</h4>
          <p>Ajoutez une m�thode de paiement pour commencer</p>
          {showAddButton && (
            <button
              className={styles.addButtonLarge}
              onClick={() => setShowAddForm(true)}
              disabled={isLoading}
            >
              <Plus size={20} />
              Ajouter une m�thode de paiement
            </button>
          )}
        </div>
      ) : (
        <div className={styles.methodsList}>
          {displayMethods.map(renderPaymentMethod)}
        </div>
      )}
      
      {(isLoading || isProcessing) && (
        <div className={styles.loadingOverlay}>
          <div className={styles.spinner} />
          <p>Traitement en cours...</p>
        </div>
      )}
      
      {/* Formulaires d'ajout et de modification (� impl�menter) */}
      {showAddForm && (
        <div className={styles.formModal}>
          {/* Formulaire d'ajout de m�thode de paiement */}
          <div className={styles.formContainer}>
            <h4>Ajouter une m�thode de paiement</h4>
            {/* Contenu du formulaire */}
            <div className={styles.formActions}>
              <button
                className={styles.cancelButton}
                onClick={() => setShowAddForm(false)}
              >
                Annuler
              </button>
              <button
                className={styles.saveButton}
                onClick={() => {
                  // Logique d'ajout
                  setShowAddForm(false);
                }}
              >
                Ajouter
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

PaymentMethod.propTypes = {
  methods: PropTypes.arrayOf(
    PropTypes.shape({
      id: PropTypes.string.isRequired,
      type: PropTypes.oneOf(['card', 'paypal', 'apple_pay', 'google_pay']).isRequired,
      brand: PropTypes.string,
      last4: PropTypes.string,
      exp_month: PropTypes.number,
      exp_year: PropTypes.number,
      email: PropTypes.string,
      is_default: PropTypes.bool,
      created_at: PropTypes.string,
      updated_at: PropTypes.string
    })
  ),
  onMethodSelect: PropTypes.func,
  onMethodAdd: PropTypes.func,
  onMethodUpdate: PropTypes.func,
  onMethodDelete: PropTypes.func,
  selectedMethodId: PropTypes.string,
  allowSelection: PropTypes.bool,
  allowEdit: PropTypes.bool,
  showAddButton: PropTypes.bool,
  context: PropTypes.oneOf(['checkout', 'profile', 'subscription']),
  className: PropTypes.string
};

export default PaymentMethod;