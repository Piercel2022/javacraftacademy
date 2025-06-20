import React from 'react';
import { Link } from 'react-router-dom';
import styles from './Header.module.css';

const Header = () => {
  return (
    <header className={styles.header}>
      <div className={styles.container}>
        {/* Logo et titre */}
        <div className={styles.brand}>
          <Link to="/" className={styles.brandLink}>
            <img 
              src="/favicon.ico" 
              alt="JavaCraft Academy Logo" 
              className={styles.logo}
            />
            <span className={styles.brandText}>JavaCraft Academy</span>
          </Link>
        </div>

        {/* Navigation principale */}
        <nav className={styles.nav}>
          <ul className={styles.navList}>
            <li className={styles.navItem}>
              <Link to="/dashboard" className={styles.navLink}>
                Dashboard
              </Link>
            </li>
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
              <Link to="/about" className={styles.navLink}>
                À propos
              </Link>
            </li>
          </ul>
        </nav>

        {/* Actions utilisateur */}
        <div className={styles.actions}>
          <Link to="/register" className={styles.registerBtn}>
            S'inscrire
          </Link>
          <Link to="/login" className={styles.loginBtn}>
            Se connecter
          </Link>
        </div>

        {/* Menu mobile burger */}
        <button className={styles.mobileMenuBtn} aria-label="Menu mobile">
          <span className={styles.burgerLine}></span>
          <span className={styles.burgerLine}></span>
          <span className={styles.burgerLine}></span>
        </button>
      </div>
    </header>
  );
};

export default Header;