// components/common/LoadingSpinner.jsx
import React from 'react';
import './LoadingSpinner.css';

const LoadingSpinner = ({ 
  size = 'medium', 
  color = 'primary', 
  className = '' 
}) => {
  return (
    <div 
      className={`loading-spinner loading-spinner--${size} loading-spinner--${color} ${className}`}
      role="status"
      aria-label="Chargement en cours"
    >
      <div className="spinner-circle"></div>
    </div>
  );
};

export default LoadingSpinner;