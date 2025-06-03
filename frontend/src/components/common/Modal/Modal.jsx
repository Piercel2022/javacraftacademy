// frontend/src/components/common/Modal/Modal.jsx
import React, { useEffect, useRef } from 'react';
import { createPortal } from 'react-dom';
import styles from './Modal.module.css';

const Modal = ({
  isOpen,
  onClose,
  title,
  children,
  size = 'medium',
  variant = 'default',
  showCloseButton = true,
  closeOnBackdropClick = true,
  closeOnEscape = true,
  className = '',
  headerActions,
  footer,
  ...props
}) => {
  const modalRef = useRef(null);
  const previousFocusRef = useRef(null);

  useEffect(() => {
    if (isOpen) {
      // Store previously focused element
      previousFocusRef.current = document.activeElement;
      
      // Focus the modal
      if (modalRef.current) {
        modalRef.current.focus();
      }

      // Prevent body scrolling
      document.body.style.overflow = 'hidden';
    }

    return () => {
      // Restore body scrolling
      document.body.style.overflow = 'unset';
      
      // Restore focus to previously focused element
      if (previousFocusRef.current) {
        previousFocusRef.current.focus();
      }
    };
  }, [isOpen]);

  useEffect(() => {
    const handleEscapeKey = (event) => {
      if (closeOnEscape && event.key === 'Escape') {
        onClose();
      }
    };

    if (isOpen) {
      document.addEventListener('keydown', handleEscapeKey);
    }

    return () => {
      document.removeEventListener('keydown', handleEscapeKey);
    };
  }, [isOpen, closeOnEscape, onClose]);

  const handleBackdropClick = (event) => {
    if (closeOnBackdropClick && event.target === event.currentTarget) {
      onClose();
    }
  };

  const handleKeyDown = (event) => {
    // Trap focus within modal
    if (event.key === 'Tab') {
      const focusableElements = modalRef.current?.querySelectorAll(
        'button, [href], input, select, textarea, [tabindex]:not([tabindex="-1"])'
      );
      
      if (focusableElements && focusableElements.length > 0) {
        const firstElement = focusableElements[0];
        const lastElement = focusableElements[focusableElements.length - 1];

        if (event.shiftKey) {
          if (document.activeElement === firstElement) {
            event.preventDefault();
            lastElement.focus();
          }
        } else {
          if (document.activeElement === lastElement) {
            event.preventDefault();
            firstElement.focus();
          }
        }
      }
    }
  };

  if (!isOpen) {
    return null;
  }

  const modalClasses = [
    styles.modal,
    styles[`modal--${size}`],
    styles[`modal--${variant}`],
    className
  ].filter(Boolean).join(' ');

  const modalContent = (
    <div 
      className={styles.backdrop}
      onClick={handleBackdropClick}
      aria-hidden="true"
    >
      <div
        ref={modalRef}
        className={modalClasses}
        role="dialog"
        aria-modal="true"
        aria-labelledby={title ? 'modal-title' : undefined}
        tabIndex={-1}
        onKeyDown={handleKeyDown}
        {...props}
      >
        {(title || showCloseButton || headerActions) && (
          <div className={styles.header}>
            <div className={styles.headerContent}>
              {title && (
                <h2 id="modal-title" className={styles.title}>
                  {title}
                </h2>
              )}
              {headerActions && (
                <div className={styles.headerActions}>
                  {headerActions}
                </div>
              )}
            </div>
            {showCloseButton && (
              <button
                type="button"
                className={styles.closeButton}
                onClick={onClose}
                aria-label="Fermer la modal"
              >
                <svg
                  width="24"
                  height="24"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  strokeWidth="2"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                >
                  <line x1="18" y1="6" x2="6" y2="18"></line>
                  <line x1="6" y1="6" x2="18" y2="18"></line>
                </svg>
              </button>
            )}
          </div>
        )}
        
        <div className={styles.content}>
          {children}
        </div>
        
        {footer && (
          <div className={styles.footer}>
            {footer}
          </div>
        )}
      </div>
    </div>
  );

  return createPortal(modalContent, document.body);
};

export default Modal;