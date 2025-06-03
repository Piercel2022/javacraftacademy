// frontend/src/components/common/Header/Header.jsx
import React, { useState, useContext } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { AuthContext } from '../../../context/AuthContext';
import { ThemeContext } from '../../../context/ThemeContext';
import { NotificationContext } from '../../../context/NotificationContext';
import styles from './Header.module.css';

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
        {/* Logo et titre */}
        <div className={styles.logo}>
          <Link to="/" className={styles.logoLink}>
            <img 
              src="/assets/images/logo.png" 
              alt="JavaCraft Academy" 
              className={styles.logoImage}
            />
            <span className={styles.logoText}>JavaCraft Academy</span>
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