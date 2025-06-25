import React, { useState, useRef } from 'react';
import styles from './AvatarUpload.module.css';

const AvatarUpload = ({ 
  currentAvatar, 
  onAvatarChange, 
  maxSize = 5 * 1024 * 1024, // 5MB par défaut
  acceptedTypes = ['image/jpeg', 'image/png', 'image/gif'],
  size = 'medium' // small, medium, large
}) => {
  const [preview, setPreview] = useState(currentAvatar);
  const [isDragging, setIsDragging] = useState(false);
  const [isUploading, setIsUploading] = useState(false);
  const [error, setError] = useState(null);
  const fileInputRef = useRef(null);

  const validateFile = (file) => {
    if (!file) return false;

    // Vérifier le type de fichier
    if (!acceptedTypes.includes(file.type)) {
      setError(`Type de fichier non supporté. Formats acceptés: ${acceptedTypes.join(', ')}`);
      return false;
    }

    // Vérifier la taille du fichier
    if (file.size > maxSize) {
      setError(`Fichier trop volumineux. Taille maximum: ${(maxSize / 1024 / 1024).toFixed(1)}MB`);
      return false;
    }

    setError(null);
    return true;
  };

  const processFile = (file) => {
    if (!validateFile(file)) return;

    const reader = new FileReader();
    reader.onload = (e) => {
      const imageUrl = e.target.result;
      setPreview(imageUrl);
      
      // Créer un objet avec les informations du fichier
      const fileInfo = {
        file,
        preview: imageUrl,
        name: file.name,
        size: file.size,
        type: file.type
      };

      onAvatarChange && onAvatarChange(fileInfo);
    };
    reader.readAsDataURL(file);
  };

  const handleFileSelect = (event) => {
    const file = event.target.files[0];
    if (file) {
      processFile(file);
    }
  };

  const handleDragOver = (event) => {
    event.preventDefault();
    setIsDragging(true);
  };

  const handleDragLeave = (event) => {
    event.preventDefault();
    setIsDragging(false);
  };

  const handleDrop = (event) => {
    event.preventDefault();
    setIsDragging(false);
    
    const files = event.dataTransfer.files;
    if (files.length > 0) {
      processFile(files[0]);
    }
  };

  const handleClick = () => {
    fileInputRef.current?.click();
  };

  const handleRemove = () => {
    setPreview(null);
    setError(null);
    if (fileInputRef.current) {
      fileInputRef.current.value = '';
    }
    onAvatarChange && onAvatarChange(null);
  };

  const getInitials = (name = 'User') => {
    return name
      .split(' ')
      .map(word => word.charAt(0))
      .join('')
      .toUpperCase()
      .slice(0, 2);
  };

  const sizeClass = {
    small: styles.small,
    medium: styles.medium,
    large: styles.large
  }[size] || styles.medium;

  return (
    <div className={`${styles.avatarUpload} ${sizeClass}`}>
      <div 
        className={`${styles.uploadArea} ${isDragging ? styles.dragging : ''} ${error ? styles.error : ''}`}
        onDragOver={handleDragOver}
        onDragLeave={handleDragLeave}
        onDrop={handleDrop}
        onClick={handleClick}
      >
        {preview ? (
          <div className={styles.avatarPreview}>
            <img 
              src={preview} 
              alt="Avatar preview" 
              className={styles.avatarImage}
            />
            <div className={styles.overlay}>
              <div className={styles.overlayContent}>
                <svg className={styles.uploadIcon} viewBox="0 0 24 24" fill="none" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 16.5v2.25A2.25 2.25 0 005.25 21h13.5A2.25 2.25 0 0021 18.75V16.5m-13.5-9L12 3m0 0l4.5 4.5M12 3v13.5" />
                </svg>
                <span>Changer</span>
              </div>
            </div>
            <button 
              className={styles.removeButton}
              onClick={(e) => {
                e.stopPropagation();
                handleRemove();
              }}
              type="button"
              aria-label="Supprimer l'avatar"
            >
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor">
                <line x1="18" y1="6" x2="6" y2="18"></line>
                <line x1="6" y1="6" x2="18" y2="18"></line>
              </svg>
            </button>
          </div>
        ) : (
          <div className={styles.uploadPlaceholder}>
            <div className={styles.defaultAvatar}>
              {currentAvatar ? (
                <img src={currentAvatar} alt="Avatar actuel" />
              ) : (
                <span className={styles.initials}>
                  {getInitials()}
                </span>
              )}
            </div>
            <div className={styles.uploadPrompt}>
              <svg className={styles.uploadIcon} viewBox="0 0 24 24" fill="none" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 16.5v2.25A2.25 2.25 0 005.25 21h13.5A2.25 2.25 0 0021 18.75V16.5m-13.5-9L12 3m0 0l4.5 4.5M12 3v13.5" />
              </svg>
              <p className={styles.uploadText}>
                <span className={styles.uploadLink}>Cliquez pour télécharger</span>
                <span className={styles.uploadHint}>ou glissez-déposez votre image</span>
              </p>
              <p className={styles.uploadInfo}>
                PNG, JPG, GIF jusqu'à {(maxSize / 1024 / 1024).toFixed(0)}MB
              </p>
            </div>
          </div>
        )}

        <input
          ref={fileInputRef}
          type="file"
          accept={acceptedTypes.join(',')}
          onChange={handleFileSelect}
          className={styles.hiddenInput}
          aria-label="Sélectionner un fichier avatar"
        />
      </div>

      {error && (
        <div className={styles.errorMessage}>
          <svg className={styles.errorIcon} viewBox="0 0 24 24" fill="none" stroke="currentColor">
            <circle cx="12" cy="12" r="10"></circle>
            <line x1="15" y1="9" x2="9" y2="15"></line>
            <line x1="9" y1="9" x2="15" y2="15"></line>
          </svg>
          {error}
        </div>
      )}

      {isUploading && (
        <div className={styles.uploadingIndicator}>
          <div className={styles.spinner}></div>
          <span>Téléchargement en cours...</span>
        </div>
      )}
    </div>
  );
};

export default AvatarUpload;