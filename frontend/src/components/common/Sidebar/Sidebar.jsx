// frontend/src/components/common/Sidebar/Sidebar.jsx
import React, { useState, useEffect } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../../../hooks/useAuth';
import { useNotification } from '../../../hooks/useNotification';
import styles from './Sidebar.module.css';

const Sidebar = ({ isOpen, onClose, className = '' }) => {
  const location = useLocation();
  const navigate = useNavigate();
  const { user, logout } = useAuth();
  const { showNotification } = useNotification();
  const [activeSection, setActiveSection] = useState('');

  // Menu items for authenticated users
  const menuItems = [
    {
      section: 'main',
      title: 'Navigation principale',
      items: [
        {
          path: '/dashboard',
          icon: '🏠',
          label: 'Tableau de bord',
          description: 'Vue d\'ensemble de votre progression'
        },
        {
          path: '/courses',
          icon: '📚',
          label: 'Cours',
          description: 'Parcourir tous les cours disponibles'
        },
        {
          path: '/playground',
          icon: '⚡',
          label: 'Playground',
          description: 'Éditeur de code interactif'
        },
        {
          path: '/progress',
          icon: '📊',
          label: 'Progression',
          description: 'Suivre vos statistiques d\'apprentissage'
        }
      ]
    },
    {
      section: 'learning',
      title: 'Apprentissage',
      items: [
        {
          path: '/courses/java-basics',
          icon: '☕',
          label: 'Java Fondamentaux',
          description: 'Bases du langage Java'
        },
        {
          path: '/courses/oop',
          icon: '🏗️',
          label: 'POO',
          description: 'Programmation orientée objet'
        },
        {
          path: '/courses/advanced',
          icon: '🚀',
          label: 'Java Avancé',
          description: 'Concepts avancés de Java'
        }
      ]
    },
    {
      section: 'tools',
      title: 'Outils',
      items: [
        {
          path: '/playground/compiler',
          icon: '🔧',
          label: 'Compilateur',
          description: 'Compiler et exécuter du code Java'
        },
        {
          path: '/playground/debugger',
          icon: '🐛',
          label: 'Débuggeur',
          description: 'Déboguer votre code Java'
        }
      ]
    },
    {
      section: 'account',
      title: 'Compte',
      items: [
        {
          path: '/profile',
          icon: '👤',
          label: 'Profil',
          description: 'Gérer votre profil utilisateur'
        },
        {
          path: '/profile/settings',
          icon: '⚙️',
          label: 'Paramètres',
          description: 'Configuration de l\'application'
        }
      ]
    }
  ];

  // Menu items for non-authenticated users
  const publicMenuItems = [
    {
      section: 'main',
      title: 'Navigation',
      items: [
        {
          path: '/',
          icon: '🏠',
          label: 'Accueil',
          description: 'Page d\'accueil de JavaCraft Academy'
        },
        {
          path: '/courses',
          icon: '📚',
          label: 'Cours disponibles',
          description: 'Découvrir nos cours Java'
        },
        {
          path: '/login',
          icon: '🔑',
          label: 'Connexion',
          description: 'Se connecter à votre compte'
        },
        {
          path: '/register',
          icon: '📝',
          label: 'Inscription',
          description: 'Créer un nouveau compte'
        }
      ]
    }
  ];

  useEffect(() => {
    const currentPath = location.pathname;
    const currentSection = menuItems.find(section =>
      section.items.some(item => item.path === currentPath)
    );
    if (currentSection) {
      setActiveSection(currentSection.section);
    }
  }, [location.pathname]);

  const handleLogout = async () => {
    try {
      await logout();
      showNotification('Déconnexion réussie', 'success');
      navigate('/');
      onClose();
    } catch (error) {
      showNotification('Erreur lors de la déconnexion', 'error');
    }
  };

  const handleItemClick = (path) => {
    navigate(path);
    onClose();
  };

  const handleSectionToggle = (section) => {
    setActiveSection(activeSection === section ? '' : section);
  };

  const currentMenuItems = user ? menuItems : publicMenuItems;

  return (
    <>
      {/* Backdrop */}
      {isOpen && (
        <div 
          className={styles.backdrop}
          onClick={onClose}
          aria-hidden="true"
        />
      )}
      
      {/* Sidebar */}
      <aside 
        className={`${styles.sidebar} ${isOpen ? styles.open : ''} ${className}`}
        aria-label="Navigation latérale"
      >
        <div className={styles.sidebarHeader}>
          <div className={styles.logo}>
            <span className={styles.logoIcon}>☕</span>
            <h2 className={styles.logoText}>JavaCraft</h2>
          </div>
          <button
            className={styles.closeButton}
            onClick={onClose}
            aria-label="Fermer la navigation"
          >
            ✕
          </button>
        </div>

        <div className={styles.sidebarContent}>
          {user && (
            <div className={styles.userSection}>
              <div className={styles.userInfo}>
                <div className={styles.userAvatar}>
                  {user.avatar ? (
                    <img src={user.avatar} alt={`Avatar de ${user.name}`} />
                  ) : (
                    <span>{user.name?.charAt(0)?.toUpperCase() || '?'}</span>
                  )}
                </div>
                <div className={styles.userDetails}>
                  <p className={styles.userName}>{user.name}</p>
                  <p className={styles.userRole}>{user.role || 'Étudiant'}</p>
                </div>
              </div>
            </div>
          )}

          <nav className={styles.navigation}>
            {currentMenuItems.map((section) => (
              <div key={section.section} className={styles.menuSection}>
                <button
                  className={`${styles.sectionHeader} ${
                    activeSection === section.section ? styles.active : ''
                  }`}
                  onClick={() => handleSectionToggle(section.section)}
                  aria-expanded={activeSection === section.section}
                >
                  <span className={styles.sectionTitle}>{section.title}</span>
                  <span className={styles.sectionToggle}>
                    {activeSection === section.section ? '▼' : '▶'}
                  </span>
                </button>
                
                <div 
                  className={`${styles.sectionContent} ${
                    activeSection === section.section ? styles.expanded : ''
                  }`}
                >
                  <ul className={styles.menuList}>
                    {section.items.map((item) => (
                      <li key={item.path} className={styles.menuItem}>
                        <button
                          className={`${styles.menuLink} ${
                            location.pathname === item.path ? styles.active : ''
                          }`}
                          onClick={() => handleItemClick(item.path)}
                        >
                          <span className={styles.menuIcon}>{item.icon}</span>
                          <div className={styles.menuContent}>
                            <span className={styles.menuLabel}>{item.label}</span>
                            <span className={styles.menuDescription}>{item.description}</span>
                          </div>
                        </button>
                      </li>
                    ))}
                  </ul>
                </div>
              </div>
            ))}
          </nav>
        </div>

        {user && (
          <div className={styles.sidebarFooter}>
            <button
              className={styles.logoutButton}
              onClick={handleLogout}
              aria-label="Se déconnecter"
            >
              <span className={styles.logoutIcon}>🚪</span>
              <span className={styles.logoutText}>Déconnexion</span>
            </button>
          </div>
        )}
      </aside>
    </>
  );
};

export default Sidebar;