
import React, { useState } from 'react';
import { X, Info, CheckCircle, AlertTriangle, AlertCircle } from 'lucide-react';
import styles from './Alert.module.css';

const Alert = ({ 
  type = 'info',
  variant = 'filled', // filled, outlined, minimal
  size = 'medium', // small, medium, large
  title,
  children,
  dismissible = false,
  onDismiss,
  icon = true,
  actions,
  className = ''
}) => {
  const [isVisible, setIsVisible] = useState(true);

  const handleDismiss = () => {
    setIsVisible(false);
    setTimeout(() => {
      onDismiss?.();
    }, 300);
  };

  const getIcon = () => {
    if (!icon) return null;
    
    switch (type) {
      case 'success':
        return <CheckCircle className={styles.icon} />;
      case 'error':
        return <AlertCircle className={styles.icon} />;
      case 'warning':
        return <AlertTriangle className={styles.icon} />;
      case 'info':
      default:
        return <Info className={styles.icon} />;
    }
  };

  if (!isVisible) return null;

  const alertClasses = [
    styles.alert,
    styles[type],
    styles[variant],
    styles[size],
    className
  ].filter(Boolean).join(' ');

  return (
    <div className={alertClasses} role="alert">
      <div className={styles.content}>
        {icon && (
          <div className={styles.iconContainer}>
            {getIcon()}
          </div>
        )}
        
        <div className={styles.message}>
          {title && <div className={styles.title}>{title}</div>}
          {children && <div className={styles.description}>{children}</div>}
        </div>

        {actions && (
          <div className={styles.actions}>
            {actions}
          </div>
        )}

        {dismissible && (
          <button
            className={styles.dismissButton}
            onClick={handleDismiss}
            aria-label="Fermer l'alerte"
          >
            <X className={styles.dismissIcon} />
          </button>
        )}
      </div>
    </div>
  );
};

export default Alert;