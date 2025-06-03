// frontend/src/components/common/Button/Button.jsx
import React, { forwardRef } from 'react';
import styles from './Button.module.css';

const Button = forwardRef(({
  children,
  variant = 'primary',
  size = 'medium',
  type = 'button',
  disabled = false,
  loading = false,
  fullWidth = false,
  leftIcon,
  rightIcon,
  className = '',
  onClick,
  ...props
}, ref) => {
  const buttonClasses = [
    styles.button,
    styles[`button--${variant}`],
    styles[`button--${size}`],
    fullWidth && styles['button--fullWidth'],
    loading && styles['button--loading'],
    disabled && styles['button--disabled'],
    className
  ].filter(Boolean).join(' ');

  const handleClick = (event) => {
    if (disabled || loading) {
      event.preventDefault();
      return;
    }
    onClick?.(event);
  };

  const LoadingSpinner = () => (
    <svg
      className={styles.spinner}
      width="16"
      height="16"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="2"
      strokeLinecap="round"
      strokeLinejoin="round"
    >
      <circle cx="12" cy="12" r="10" opacity="0.25" />
      <path d="M12 2a10 10 0 0 1 10 10" opacity="0.75" />
    </svg>
  );

  return (
    <button
      ref={ref}
      type={type}
      className={buttonClasses}
      disabled={disabled || loading}
      onClick={handleClick}
      aria-disabled={disabled || loading}
      {...props}
    >
      <span className={styles.content}>
        {loading ? (
          <LoadingSpinner />
        ) : (
          leftIcon && <span className={styles.leftIcon}>{leftIcon}</span>
        )}
        
        {children && (
          <span className={styles.text}>
            {children}
          </span>
        )}
        
        {!loading && rightIcon && (
          <span className={styles.rightIcon}>{rightIcon}</span>
        )}
      </span>
    </button>
  );
});

Button.displayName = 'Button';

export default Button;