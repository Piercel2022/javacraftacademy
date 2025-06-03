// frontend/src/components/common/Navigation/Navigation.jsx
import React, { useState, useEffect } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { useAuth } from '../../../hooks/useAuth';
import styles from './Navigation.module.css';

const Navigation = ({ className = '', variant = 'horizontal', onItemClick }) => {
  const location = useLocation();
  const { user } = useAuth();
  const [activeItem, setActiveItem] = useState('');

  // Navigation items based on user authentication
  const navigationItems = user ? [
    {
      id: 'dashboard',
      path: '/dashboard',
      label: 'Tableau de bord',
      icon: '🏠',
      description: 'Vue d\'ensemble de votre progression'
    },
    {
      id: 'courses',
      path: '/courses',
      label: 'Cours',
      icon: '📚',
      description: 'Parcourir les cours disponibles'
    },
    {
      id: 'playground',
      path: '/playground',
      label: 'Playground',
      icon: '⚡',
      description: 'Éditeur de code interactif'
    },
    {
      id: 'progress',
      path: '/progress',
      label: 'Progression',
      icon: '📊',
      description: 'Suivre vos statistiques'
    },
    {
      id: 'profile',
      path: '/profile',
      label: 'Profil',
      icon: '👤',
      description: 'Gérer votre profil'
    }
  ] : [
    {
      id: 'home',
      path: '/',
      label: 'Accueil',
      icon: '🏠',
      description: 'Page d\'accueil'
    },
    {
      id: 'courses',
      path: '/courses',
      label: 'Cours',
      icon: '📚',
      description: 'Découvrir nos cours'
    },
    {
      id: 'about',
      path: '/about',
      label: 'À propos',
      icon: 'ℹ️',
      description: 'En savoir plus sur JavaCraft'
    },
    {
      id: 'contact',
      path: '/contact',
      label: 'Contact',
      icon: '📧',
      description: 'Nous contacter'
    }
  ];

  useEffect(() => {
    const currentPath = location.pathname;
    const activeNav = navigationItems.find(item => {
      if (item.path === '/') {
        return currentPath === '/';
      }
      return currentPath.startsWith(item.path);
    });
    setActiveItem(activeNav?.id || '');
  }, [location.pathname]);

  const handleItemClick = (item) => {
    setActiveItem(item.id);
    if (onItemClick) {
      onItemClick(item);
    }
  };

  return (
    <nav 
      className={`${styles.navigation} ${styles[variant]} ${className}`}
      aria-label="Navigation principale"
    >
      <ul className={styles.navList}>
        {navigationItems.map((item) => (
          <li key={item.id} className={styles.navItem}>
            <Link
              to={item.path}
              className={`${styles.navLink} ${
                activeItem === item.id ? styles.active : ''
              }`}
              onClick={() => handleItemClick(item)}
              aria-current={activeItem === item.id ? 'page' : undefined}
            >
              <span className={styles.navIcon} aria-hidden="true">
                {item.icon}
              </span>
              <span className={styles.navLabel}>{item.label}</span>
              {variant === 'vertical' && (
                <span className={styles.navDescription}>
                  {item.description}
                </span>
              )}
              {activeItem === item.id && (
                <span className={styles.activeIndicator} aria-hidden="true" />
              )}
            </Link>
          </li>
        ))}
      </ul>
    </nav>
  );
};

export default Navigation;