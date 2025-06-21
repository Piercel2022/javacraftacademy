import React, { useState, useContext } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { AuthContext } from '../../../context/AuthContext';
import { NotificationContext } from '../../../context/NotificationContext';
import { ThemeContext } from '../../../context/ThemeContext';
import styles from './Header.module.css';

// Composant Logo JavaCraftAcademy
const JavaCraftLogo = ({ className, size = 'medium' }) => {
  const sizes = {
    small: { width: 32, height: 32, fontSize: 12 },
    medium: { width: 40, height: 40, fontSize: 16 },
    large: { width: 48, height: 48, fontSize: 20 }
  };
  
  const currentSize = sizes[size];
  
  return (
    <svg 
      width={currentSize.width} 
      height={currentSize.height} 
      viewBox="0 0 60 60" 
      className={className}
      style={{ flexShrink: 0 }}
    >
      <defs>
        <linearGradient id="bookGradient" x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" stopColor="#FF6B35" />
          <stop offset="100%" stopColor="#FF8A65" />
        </linearGradient>
        <filter id="shadow" x="-20%" y="-20%" width="140%" height="140%">
          <feDropShadow dx="0" dy="2" stdDeviation="3" floodColor="#FF6B35" floodOpacity="0.3"/>
        </filter>
      </defs>
      
      {/* Livre de base */}
      <rect 
        x="0" 
        y="0" 
        width="60" 
        height="60" 
        rx="8" 
        fill="url(#bookGradient)" 
        filter="url(#shadow)"
      />
      
      {/* Pages du livre */}
      <rect 
        x="8" 
        y="8" 
        width="44" 
        height="44" 
        rx="4" 
        fill="white"
      />
      
      {/* Accolades de code */}
      <text 
        x="30" 
        y="38" 
        textAnchor="middle" 
        fontFamily="'Courier New', monospace" 
        fontSize="24" 
        fontWeight="bold" 
        fill="#2E86AB"
      >
        {'{}'}
      </text>
    </svg>
  );
};

const Header = () => {
  const { user, login, logout, isAuthenticated } = useContext(AuthContext);
  const { notifications, unreadCount } = useContext(NotificationContext);
  const { theme, toggleTheme } = useContext(ThemeContext);
  const [isMenuOpen, setIsMenuOpen] = useState(false);
  const [isProfileMenuOpen, setIsProfileMenuOpen] = useState(false);
  const [isNotificationsOpen, setIsNotificationsOpen] = useState(false);
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/');
    setIsProfileMenuOpen(false);
  };

  const toggleMenu = () => {
    setIsMenuOpen(!isMenuOpen);
  };

  const toggleProfileMenu = () => {
    setIsProfileMenuOpen(!isProfileMenuOpen);
  };

  const toggleNotifications = () => {
    setIsNotificationsOpen(!isNotificationsOpen);
  };

  // Fonction de connexion développeur corrigée
  const devLogin = async () => {
    try {
      // Simuler les données utilisateur complètes
      const devUser = {
        id: 'dev-001',
        username: 'developer',
        email: 'dev@javacraft.academy', 
        firstName: 'John',
        lastName: 'Developer',
        role: 'admin',
        avatar: '/assets/images/avatars/dev-avatar.png',
        joinDate: '2024-01-01',
        completedCourses: 15,
        currentLevel: 'Expert'
      };

      // CORRECTION : Utiliser la fonction login du contexte
      // Option 1: Si votre fonction login accepte directement l'objet utilisateur
      if (typeof login === 'function') {
        await login(devUser);
      }
      
      // Option 2: Si vous devez simuler un appel API
      // Décommentez cette partie si nécessaire
      /*
      const response = await fetch('/api/auth/dev-login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          email: 'dev@javacraft.academy',
          password: 'DevPass123!'
        }),
      });
      
      if (response.ok) {
        const userData = await response.json();
        login(userData);
      }
      */
      
      console.log('Connexion développeur réussie:', devUser);
      
      // Redirection vers le dashboard
      navigate('/dashboard');
      
    } catch (error) {
      console.error('Erreur lors de la connexion développeur:', error);
      
      // Afficher une notification d'erreur si disponible
      // notificationContext?.addNotification?.({
      //   type: 'error',
      //   message: 'Erreur de connexion développeur'
      // });
    }
  };

  return (
    <header className={styles.header}>
      <div className={styles.container}>
        {/* Logo et titre */}
        <div className={styles.brand}>
          <Link to="/" className={styles.logoLink}>
            <JavaCraftLogo 
              className={styles.logo} 
              size="medium"
            />
            <div className={styles.brandText}>
              <div className={styles.topLine}>
                <span className={styles.javaText}>Java</span>
                <span className={styles.craftText}>Craft</span>
              </div>
              <div className={styles.bottomLine}>
                <span className={styles.academyText}>Academy</span>
              </div>
            </div>
          </Link>
        </div>

        {/* Navigation principale */}
        <nav className={`${styles.nav} ${isMenuOpen ? styles.navOpen : ''}`}>
          <ul className={styles.navList}>
            <li className={styles.navItem}>
              <Link to="/courses" className={styles.navLink}>
                Cours
              </Link>
            </li>
            <li className={styles.navItem}>
              <Link to="/code-playground" className={styles.navLink}>
                Playground
              </Link>
            </li>
            <li className={styles.navItem}>
              <Link to="/progress" className={styles.navLink}>
                Progression
              </Link>
            </li>
            <li className={styles.navItem}>
              <Link to="/community" className={styles.navLink}>
                Communauté
              </Link>
            </li>
          </ul>
        </nav>

        {/* Actions utilisateur */}
        <div className={styles.userActions}>
          {/* Bouton de changement de thème */}
          <button 
            onClick={toggleTheme}
            className={styles.themeToggle}
            title={`Passer au thème ${theme === 'light' ? 'sombre' : 'clair'}`}
          >
            {theme === 'light' ? '🌙' : '☀️'}
          </button>

          {isAuthenticated ? (
            <>
              {/* Notifications */}
              <div className={styles.notificationContainer}>
                <button 
                  onClick={toggleNotifications}
                  className={styles.notificationButton}
                  title="Notifications"
                >
                  🔔
                  {unreadCount > 0 && (
                    <span className={styles.notificationBadge}>
                      {unreadCount > 99 ? '99+' : unreadCount}
                    </span>
                  )}
                </button>
                
                {isNotificationsOpen && (
                  <div className={styles.notificationDropdown}>
                    <div className={styles.notificationHeader}>
                      <h3>Notifications</h3>
                      <button className={styles.markAllRead}>
                        Tout marquer comme lu
                      </button>
                    </div>
                    <div className={styles.notificationList}>
                      {notifications.length > 0 ? (
                        notifications.slice(0, 5).map(notification => (
                          <div 
                            key={notification.id} 
                            className={`${styles.notificationItem} ${!notification.read ? styles.unread : ''}`}
                          >
                            <div className={styles.notificationContent}>
                              <p className={styles.notificationText}>
                                {notification.message}
                              </p>
                              <span className={styles.notificationTime}>
                                {notification.createdAt}
                              </span>
                            </div>
                          </div>
                        ))
                      ) : (
                        <div className={styles.noNotifications}>
                          Aucune notification
                        </div>
                      )}
                    </div>
                    <Link to="/notifications" className={styles.viewAllNotifications}>
                      Voir toutes les notifications
                    </Link>
                  </div>
                )}
              </div>

              {/* Menu profil utilisateur */}
              <div className={styles.profileContainer}>
                <button 
                  onClick={toggleProfileMenu}
                  className={styles.profileButton}
                  title="Menu profil"
                >
                  <img 
                    src={user?.avatar || '/assets/images/avatars/default-avatar.png'} 
                    alt={`Avatar de ${user?.firstName || 'Utilisateur'}`}
                    className={styles.avatar}
                  />
                  <span className={styles.username}>
                    {user?.firstName || 'Utilisateur'}
                  </span>
                  <span className={styles.dropdownArrow}>▼</span>
                </button>

                {isProfileMenuOpen && (
                  <div className={styles.profileDropdown}>
                    <div className={styles.profileInfo}>
                      <img 
                        src={user?.avatar || '/assets/images/avatars/default-avatar.png'} 
                        alt="Avatar"
                        className={styles.profileAvatar}
                      />
                      <div className={styles.profileDetails}>
                        <p className={styles.profileName}>
                          {user?.firstName} {user?.lastName}
                        </p>
                        <p className={styles.profileEmail}>
                          {user?.email}
                        </p>
                        <span className={styles.profileLevel}>
                          {user?.currentLevel || 'Débutant'}
                        </span>
                      </div>
                    </div>
                    
                    <div className={styles.profileMenu}>
                      <Link 
                        to="/dashboard" 
                        className={styles.profileMenuItem}
                        onClick={() => setIsProfileMenuOpen(false)}
                      >
                        📊 Tableau de bord
                      </Link>
                      <Link 
                        to="/profile" 
                        className={styles.profileMenuItem}
                        onClick={() => setIsProfileMenuOpen(false)}
                      >
                        👤 Mon profil
                      </Link>
                      <Link 
                        to="/profile/settings" 
                        className={styles.profileMenuItem}
                        onClick={() => setIsProfileMenuOpen(false)}
                      >
                        ⚙️ Paramètres
                      </Link>
                      <Link 
                        to="/progress" 
                        className={styles.profileMenuItem}
                        onClick={() => setIsProfileMenuOpen(false)}
                      >
                        📈 Ma progression
                      </Link>
                      <div className={styles.menuDivider}></div>
                      <button 
                        onClick={handleLogout}
                        className={`${styles.profileMenuItem} ${styles.logoutButton}`}
                      >
                        🚪 Se déconnecter
                      </button>
                    </div>
                  </div>
                )}
              </div>
            </>
          ) : (
            <>
              {/* Boutons de connexion/inscription */}
              <div className={styles.authButtons}>
                <Link to="/login" className={styles.loginButton}>
                  Se connecter
                </Link>
                <Link to="/register" className={styles.registerButton}>
                  S'inscrire
                </Link>
                {/* Bouton de connexion développeur pour les tests */}
                <button 
                  onClick={devLogin}
                  className={styles.devLoginButton}
                  title="Connexion développeur (test)"
                >
                  Dev Login
                </button>
              </div>
            </>
          )}

          {/* Bouton menu mobile */}
          <button 
            className={styles.mobileMenuButton}
            onClick={toggleMenu}
            title="Menu"
          >
            <span className={styles.hamburger}></span>
            <span className={styles.hamburger}></span>
            <span className={styles.hamburger}></span>
          </button>
        </div>
      </div>

      {/* Menu mobile overlay */}
      {isMenuOpen && (
        <div className={styles.mobileMenuOverlay} onClick={toggleMenu}>
          <div className={styles.mobileMenu} onClick={e => e.stopPropagation()}>
            <div className={styles.mobileMenuHeader}>
              <h3>Menu</h3>
              <button onClick={toggleMenu} className={styles.closeButton}>
                ✕
              </button>
            </div>
            <nav className={styles.mobileNav}>
              <Link to="/courses" className={styles.mobileNavLink} onClick={toggleMenu}>
                📚 Cours
              </Link>
              <Link to="/code-playground" className={styles.mobileNavLink} onClick={toggleMenu}>
                💻 Playground
              </Link>
              <Link to="/progress" className={styles.mobileNavLink} onClick={toggleMenu}>
                📈 Progression
              </Link>
              <Link to="/community" className={styles.mobileNavLink} onClick={toggleMenu}>
                👥 Communauté
              </Link>
              {isAuthenticated ? (
                <>
                  <div className={styles.mobileDivider}></div>
                  <Link to="/dashboard" className={styles.mobileNavLink} onClick={toggleMenu}>
                    📊 Dashboard
                  </Link>
                  <Link to="/profile" className={styles.mobileNavLink} onClick={toggleMenu}>
                    👤 Profil
                  </Link>
                  <button onClick={handleLogout} className={styles.mobileLogoutButton}>
                    🚪 Se déconnecter
                  </button>
                </>
              ) : (
                <>
                  <div className={styles.mobileDivider}></div>
                  <Link to="/login" className={styles.mobileNavLink} onClick={toggleMenu}>
                    🔑 Se connecter
                  </Link>
                  <Link to="/register" className={styles.mobileNavLink} onClick={toggleMenu}>
                    ✨ S'inscrire
                  </Link>
                  <button onClick={devLogin} className={styles.mobileDevButton}>
                    🔧 Dev Login
                  </button>
                </>
              )}
            </nav>
          </div>
        </div>
      )}
    </header>
  );
};

export default Header;