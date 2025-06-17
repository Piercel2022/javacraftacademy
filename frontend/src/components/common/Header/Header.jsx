// frontend/src/components/common/Header/Header.jsx
import React, { useState, useContext } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { AuthContext } from '../../../context/AuthContext';
import { ThemeContext } from '../../../context/ThemeContext';
import { NotificationContext } from '../../../context/NotificationContext';
import styles from './Header.module.css';

// Composant Logo SVG JavaCraft Academy
const JavaCraftLogo = ({ className }) => (
  <svg 
    viewBox="0 0 200 60" 
    className={className}
    xmlns="http://www.w3.org/2000/svg"
  >
    {/* Dégradés pour les éléments principaux */}
    <defs>
      <linearGradient id="javaGradient" x1="0%" y1="0%" x2="100%" y2="100%">
        <stop offset="0%" stopColor="#FF6B35" />
        <stop offset="100%" stopColor="#F7931E" />
      </linearGradient>
      <linearGradient id="craftGradient" x1="0%" y1="0%" x2="100%" y2="100%">
        <stop offset="0%" stopColor="#4A90E2" />
        <stop offset="100%" stopColor="#2C5AA0" />
      </linearGradient>
    </defs>
    
    {/* Icône stylisée représentant Java + Craft */}
    <g transform="translate(8, 8)">
      {/* Tasse de café Java stylisée */}
      <path 
        d="M8 35 Q8 25 18 25 L28 25 Q38 25 38 35 L38 40 Q38 45 33 45 L13 45 Q8 45 8 40 Z" 
        fill="url(#javaGradient)"
      />
      {/* Vapeur de café */}
      <path 
        d="M15 20 Q17 15 19 20 M23 18 Q25 13 27 18 M31 20 Q33 15 35 20" 
        stroke="url(#javaGradient)" 
        strokeWidth="2" 
        fill="none"
        strokeLinecap="round"
      />
      {/* Anse de la tasse */}
      <path 
        d="M38 30 Q45 30 45 37 Q45 42 40 42" 
        stroke="url(#javaGradient)" 
        strokeWidth="2.5" 
        fill="none"
      />
      
      {/* Éléments "Craft" - outils/engrenage */}
      <circle cx="55" cy="35" r="8" fill="url(#craftGradient)" opacity="0.9"/>
      <circle cx="55" cy="35" r="4" fill="white"/>
      <rect x="51" y="27" width="8" height="3" fill="url(#craftGradient)" rx="1"/>
      <rect x="51" y="40" width="8" height="3" fill="url(#craftGradient)" rx="1"/>
      <rect x="47" y="31" width="3" height="8" fill="url(#craftGradient)" rx="1"/>
      <rect x="60" y="31" width="3" height="8" fill="url(#craftGradient)" rx="1"/>
    </g>
    
    {/* Texte "JavaCraft" */}
    <text x="100" y="50" fontFamily="Arial, sans-serif" fontSize="16" fontWeight="bold" fill="url(#javaGradient)">
      Java
    </text>
    <text x="120" y="62" fontFamily="Arial, sans-serif" fontSize="16" fontWeight="bold" fill="url(#craftGradient)">
      Craft
    </text>
    
    {/* "Academy" en plus petit */}
    <text x="170" y="75" fontFamily="Arial, sans-serif" fontSize="10" fill="#666" fontWeight="500">
      ACADEMY
    </text>
    
    {/* Petits accents décoratifs */}
    <circle cx="185" cy="30" r="2" fill="url(#javaGradient)" opacity="0.6"/>
    <circle cx="192" cy="35" r="1.5" fill="url(#craftGradient)" opacity="0.6"/>
  </svg>
);

const Header = () => {
  const [isMenuOpen, setIsMenuOpen] = useState(false);
  const [isProfileMenuOpen, setIsProfileMenuOpen] = useState(false);
  const { user, logout } = useContext(AuthContext);
  const { theme, toggleTheme } = useContext(ThemeContext);
  const { notifications, unreadCount } = useContext(NotificationContext);
  const navigate = useNavigate();

  const handleLogout = async () => {
    try {
      await logout();
      navigate('/login');
    } catch (error) {
      console.error('Erreur lors de la déconnexion:', error);
    }
  };

  const toggleMenu = () => {
    setIsMenuOpen(!isMenuOpen);
  };

  const toggleProfileMenu = () => {
    setIsProfileMenuOpen(!isProfileMenuOpen);
  };

  return (
    <header className={`${styles.header} ${styles[theme]}`}>
      <div className={styles.container}>
        {/* Logo avec le nouveau design SVG */}
        <div className={styles.logo}>
          <Link to="/" className={styles.logoLink}>
            <JavaCraftLogo className={styles.logoImage} />
          </Link>
        </div>

        {/* Navigation principale */}
        <nav className={`${styles.nav} ${isMenuOpen ? styles.navOpen : ''}`}>
          <ul className={styles.navList}>
            <li className={styles.navItem}>
              <Link to="/dashboard" className={styles.navLink}>
                Tableau de bord
              </Link>
            </li>
            <li className={styles.navItem}>
              <Link to="/courses" className={styles.navLink}>
                Cours
              </Link>
            </li>
            <li className={styles.navItem}>
              <Link to="/code-playground" className={styles.navLink}>
                Éditeur
              </Link>
            </li>
            <li className={styles.navItem}>
              <Link to="/progress" className={styles.navLink}>
                Progression
              </Link>
            </li>
          </ul>
        </nav>

        {/* Actions utilisateur */}
        <div className={styles.userActions}>
          {user ? (
            <>
              {/* Bouton de changement de thème */}
              <button 
                className={styles.themeToggle}
                onClick={toggleTheme}
                aria-label="Changer le thème"
              >
                {theme === 'light' ? '🌙' : '☀️'}
              </button>

              {/* Notifications */}
              <div className={styles.notifications}>
                <button className={styles.notificationButton}>
                  🔔
                  {unreadCount > 0 && (
                    <span className={styles.notificationBadge}>
                      {unreadCount}
                    </span>
                  )}
                </button>
              </div>

              {/* Menu profil */}
              <div className={styles.profileMenu}>
                <button 
                  className={styles.profileButton}
                  onClick={toggleProfileMenu}
                >
                  <img 
                    src={user.avatar || '/assets/images/avatars/default.png'} 
                    alt="Profil"
                    className={styles.avatar}
                  />
                  <span className={styles.username}>{user.name}</span>
                  <span className={styles.dropdownArrow}>▼</span>
                </button>

                {isProfileMenuOpen && (
                  <div className={styles.profileDropdown}>
                    <Link to="/profile" className={styles.dropdownItem}>
                      Mon profil
                    </Link>
                    <Link to="/profile/settings" className={styles.dropdownItem}>
                      Paramètres
                    </Link>
                    <hr className={styles.dropdownDivider} />
                    <button 
                      onClick={handleLogout}
                      className={styles.dropdownItem}
                    >
                      Déconnexion
                    </button>
                  </div>
                )}
              </div>
            </>
          ) : (
            <div className={styles.authButtons}>
              <Link to="/login" className={styles.loginBtn}>
                Connexion
              </Link>
              <Link to="/register" className={styles.registerBtn}>
                Inscription
              </Link>
            </div>
          )}

          {/* Menu hamburger mobile */}
          <button 
            className={styles.mobileMenuToggle}
            onClick={toggleMenu}
            aria-label="Menu"
          >
            <span className={styles.hamburger}></span>
            <span className={styles.hamburger}></span>
            <span className={styles.hamburger}></span>
          </button>
        </div>
      </div>
    </header>
  );
};

export default Header;