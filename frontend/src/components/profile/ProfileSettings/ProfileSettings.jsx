
import React, { useState, useContext } from 'react';
import { AuthContext } from '../../../context/AuthContext';
import { NotificationContext } from '../../../context/NotificationContext';
import { ThemeContext } from '../../../context/ThemeContext';
import { userService } from '../../../services/userService';
import Button from '../../common/Button';
import Modal from '../../common/Modal';
import AvatarUpload from '../AvatarUpload';
import styles from './ProfileSettings.module.css';

const ProfileSettings = () => {
  const { user, updateUser } = useContext(AuthContext);
  const { showNotification } = useContext(NotificationContext);
  const { theme, toggleTheme } = useContext(ThemeContext);
  
  const [isEditing, setIsEditing] = useState(false);
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [formData, setFormData] = useState({
    firstName: user?.firstName || '',
    lastName: user?.lastName || '',
    email: user?.email || '',
    bio: user?.bio || '',
    location: user?.location || '',
    website: user?.website || ''
  });
  const [passwordData, setPasswordData] = useState({
    currentPassword: '',
    newPassword: '',
    confirmPassword: ''
  });
  const [showPasswordForm, setShowPasswordForm] = useState(false);

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handlePasswordChange = (e) => {
    const { name, value } = e.target;
    setPasswordData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleSaveProfile = async () => {
    if (!formData.firstName.trim() || !formData.lastName.trim()) {
      showNotification('Le prénom et le nom sont requis', 'error');
      return;
    }

    if (!isValidEmail(formData.email)) {
      showNotification('Veuillez entrer une adresse email valide', 'error');
      return;
    }

    setIsLoading(true);
    try {
      const updatedUser = await userService.updateProfile(formData);
      updateUser(updatedUser);
      setIsEditing(false);
      showNotification('Profil mis à jour avec succès', 'success');
    } catch (error) {
      showNotification('Erreur lors de la mise à jour du profil', 'error');
      console.error('Error updating profile:', error);
    } finally {
      setIsLoading(false);
    }
  };

  const handleChangePassword = async () => {
    if (!passwordData.currentPassword || !passwordData.newPassword) {
      showNotification('Tous les champs sont requis', 'error');
      return;
    }

    if (passwordData.newPassword !== passwordData.confirmPassword) {
      showNotification('Les mots de passe ne correspondent pas', 'error');
      return;
    }

    if (passwordData.newPassword.length < 8) {
      showNotification('Le mot de passe doit contenir au moins 8 caractères', 'error');
      return;
    }

    setIsLoading(true);
    try {
      await userService.changePassword(passwordData);
      setPasswordData({
        currentPassword: '',
        newPassword: '',
        confirmPassword: ''
      });
      setShowPasswordForm(false);
      showNotification('Mot de passe modifié avec succès', 'success');
    } catch (error) {
      showNotification('Erreur lors de la modification du mot de passe', 'error');
      console.error('Error changing password:', error);
    } finally {
      setIsLoading(false);
    }
  };

  const handleDeleteAccount = async () => {
    setIsLoading(true);
    try {
      await userService.deleteAccount();
      showNotification('Compte supprimé avec succès', 'success');
      // Redirection vers la page de connexion sera gérée par le contexte Auth
    } catch (error) {
      showNotification('Erreur lors de la suppression du compte', 'error');
      console.error('Error deleting account:', error);
    } finally {
      setIsLoading(false);
      setShowDeleteModal(false);
    }
  };

  const handleCancel = () => {
    setFormData({
      firstName: user?.firstName || '',
      lastName: user?.lastName || '',
      email: user?.email || '',
      bio: user?.bio || '',
      location: user?.location || '',
      website: user?.website || ''
    });
    setIsEditing(false);
  };

  const isValidEmail = (email) => {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
  };

  return (
    <div className={styles.profileSettings}>
      <div className={styles.header}>
        <h2>Paramètres du profil</h2>
        <p>Gérez vos informations personnelles et vos préférences</p>
      </div>

      {/* Section Avatar */}
      <div className={styles.section}>
        <h3>Photo de profil</h3>
        <AvatarUpload />
      </div>

      {/* Section Informations personnelles */}
      <div className={styles.section}>
        <div className={styles.sectionHeader}>
          <h3>Informations personnelles</h3>
          {!isEditing && (
            <Button
              variant="secondary"
              size="small"
              onClick={() => setIsEditing(true)}
            >
              Modifier
            </Button>
          )}
        </div>

        <div className={styles.form}>
          <div className={styles.row}>
            <div className={styles.field}>
              <label htmlFor="firstName">Prénom *</label>
              <input
                type="text"
                id="firstName"
                name="firstName"
                value={formData.firstName}
                onChange={handleInputChange}
                disabled={!isEditing}
                className={styles.input}
              />
            </div>
            <div className={styles.field}>
              <label htmlFor="lastName">Nom *</label>
              <input
                type="text"
                id="lastName"
                name="lastName"
                value={formData.lastName}
                onChange={handleInputChange}
                disabled={!isEditing}
                className={styles.input}
              />
            </div>
          </div>

          <div className={styles.field}>
            <label htmlFor="email">Email *</label>
            <input
              type="email"
              id="email"
              name="email"
              value={formData.email}
              onChange={handleInputChange}
              disabled={!isEditing}
              className={styles.input}
            />
          </div>

          <div className={styles.field}>
            <label htmlFor="bio">Bio</label>
            <textarea
              id="bio"
              name="bio"
              value={formData.bio}
              onChange={handleInputChange}
              disabled={!isEditing}
              rows={4}
              placeholder="Parlez-nous de vous..."
              className={styles.textarea}
            />
          </div>

          <div className={styles.row}>
            <div className={styles.field}>
              <label htmlFor="location">Localisation</label>
              <input
                type="text"
                id="location"
                name="location"
                value={formData.location}
                onChange={handleInputChange}
                disabled={!isEditing}
                placeholder="Ville, Pays"
                className={styles.input}
              />
            </div>
            <div className={styles.field}>
              <label htmlFor="website">Site web</label>
              <input
                type="url"
                id="website"
                name="website"
                value={formData.website}
                onChange={handleInputChange}
                disabled={!isEditing}
                placeholder="https://votre-site.com"
                className={styles.input}
              />
            </div>
          </div>

          {isEditing && (
            <div className={styles.actions}>
              <Button
                variant="primary"
                onClick={handleSaveProfile}
                disabled={isLoading}
              >
                {isLoading ? 'Enregistrement...' : 'Enregistrer'}
              </Button>
              <Button
                variant="secondary"
                onClick={handleCancel}
                disabled={isLoading}
              >
                Annuler
              </Button>
            </div>
          )}
        </div>
      </div>

      {/* Section Préférences */}
      <div className={styles.section}>
        <h3>Préférences</h3>
        <div className={styles.preference}>
          <div className={styles.preferenceInfo}>
            <label>Thème</label>
            <p>Choisissez entre le thème clair et sombre</p>
          </div>
          <Button
            variant="secondary"
            onClick={toggleTheme}
          >
            {theme === 'light' ? 'Mode sombre' : 'Mode clair'}
          </Button>
        </div>
      </div>

      {/* Section Sécurité */}
      <div className={styles.section}>
        <h3>Sécurité</h3>
        
        <div className={styles.securityItem}>
          <div className={styles.securityInfo}>
            <h4>Mot de passe</h4>
            <p>Modifiez votre mot de passe pour sécuriser votre compte</p>
          </div>
          <Button
            variant="secondary"
            onClick={() => setShowPasswordForm(!showPasswordForm)}
          >
            Changer le mot de passe
          </Button>
        </div>

        {showPasswordForm && (
          <div className={styles.passwordForm}>
            <div className={styles.field}>
              <label htmlFor="currentPassword">Mot de passe actuel</label>
              <input
                type="password"
                id="currentPassword"
                name="currentPassword"
                value={passwordData.currentPassword}
                onChange={handlePasswordChange}
                className={styles.input}
              />
            </div>
            
            <div className={styles.field}>
              <label htmlFor="newPassword">Nouveau mot de passe</label>
              <input
                type="password"
                id="newPassword"
                name="newPassword"
                value={passwordData.newPassword}
                onChange={handlePasswordChange}
                className={styles.input}
              />
            </div>
            
            <div className={styles.field}>
              <label htmlFor="confirmPassword">Confirmer le mot de passe</label>
              <input
                type="password"
                id="confirmPassword"
                name="confirmPassword"
                value={passwordData.confirmPassword}
                onChange={handlePasswordChange}
                className={styles.input}
              />
            </div>

            <div className={styles.actions}>
              <Button
                variant="primary"
                onClick={handleChangePassword}
                disabled={isLoading}
              >
                {isLoading ? 'Modification...' : 'Modifier le mot de passe'}
              </Button>
              <Button
                variant="secondary"
                onClick={() => {
                  setShowPasswordForm(false);
                  setPasswordData({
                    currentPassword: '',
                    newPassword: '',
                    confirmPassword: ''
                  });
                }}
              >
                Annuler
              </Button>
            </div>
          </div>
        )}
      </div>

      {/* Section Danger */}
      <div className={styles.section}>
        <h3 className={styles.dangerTitle}>Zone de danger</h3>
        <div className={styles.dangerItem}>
          <div className={styles.dangerInfo}>
            <h4>Supprimer le compte</h4>
            <p>Cette action est irréversible. Toutes vos données seront définitivement supprimées.</p>
          </div>
          <Button
            variant="danger"
            onClick={() => setShowDeleteModal(true)}
          >
            Supprimer le compte
          </Button>
        </div>
      </div>

      {/* Modal de confirmation de suppression */}
      <Modal
        isOpen={showDeleteModal}
        onClose={() => setShowDeleteModal(false)}
        title="Confirmer la suppression"
      >
        <div className={styles.deleteModal}>
          <p>
            Êtes-vous sûr de vouloir supprimer définitivement votre compte ?
            Cette action ne peut pas être annulée.
          </p>
          <div className={styles.modalActions}>
            <Button
              variant="danger"
              onClick={handleDeleteAccount}
              disabled={isLoading}
            >
              {isLoading ? 'Suppression...' : 'Supprimer définitivement'}
            </Button>
            <Button
              variant="secondary"
              onClick={() => setShowDeleteModal(false)}
              disabled={isLoading}
            >
              Annuler
            </Button>
          </div>
        </div>
      </Modal>
    </div>
  );
};

export default ProfileSettings;