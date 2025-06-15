// frontend/src/components/common/Footer/Footer.jsx
import React, { useContext } from 'react';
import { Link } from 'react-router-dom';
import { ThemeContext } from '../../../context/ThemeContext';
import styles from './Footer.module.css';

const Footer = () => {
  const { theme } = useContext(ThemeContext);
  const currentYear = new Date().getFullYear();

  const socialLinks = [
    { name: 'GitHub', url: 'https://github.com/javacraftacademy', icon: '🐙' },
    { name: 'LinkedIn', url: 'https://linkedin.com/company/javacraftacademy', icon: '💼' },
    { name: 'Twitter', url: 'https://twitter.com/javacraftacademy', icon: '🐦' },
    { name: 'YouTube', url: 'https://youtube.com/javacraftacademy', icon: '📺' }
  ];

  const quickLinks = [
    { name: 'Accueil', path: '/' },
    { name: 'Cours', path: '/courses' },
    { name: 'À propos', path: '/about' },
    { name: 'Contact', path: '/contact' }
  ];

  const resourceLinks = [
    { name: 'Centre d\'aide', path: '/help' },
    { name: 'FAQ', path: '/faq' },
    { name: 'Communauté', path: '/community' },
    { name: 'Blog', path: '/blog' }
  ];

  const legalLinks = [
    { name: 'Conditions d\'utilisation', path: '/terms' },
    { name: 'Politique de confidentialité', path: '/privacy' },
    { name: 'Mentions légales', path: '/legal' },
    { name: 'Cookies', path: '/cookies' }
  ];

  return (
    <footer className={styles.footer}>
      <div className={styles.container}>
        <div className={styles.content}>
          <div className={`${styles.section} ${styles.brandSection}`}>
            {/* Logo et description */}
            <div className={styles.logo}>
              <img 
                src="/assets/images/logo.png" 
                alt="JavaCraft Academy" 
                className={styles.logoImage}
              />
              
            </div>
            <p className={styles.description}>
              Apprenez Java de manière interactive avec des cours pratiques, 
              des exercices guidés et une communauté active de développeurs.
            </p>
            <div className={styles.socialLinks}>
              {socialLinks.map((social) => (
                <a
                  key={social.name}
                  href={social.url}
                  target="_blank"
                  rel="noopener noreferrer"
                  className={styles.socialLink}
                  aria-label={social.name}
                >
                  <span className={styles.socialIcon}>{social.icon}</span>
                </a>
              ))}
            </div>
          </div>

          <div className={styles.section}>
            {/* Liens rapides */}
            <h3 className={styles.linkTitle}>Navigation</h3>
            <ul className={styles.linkList}>
              {quickLinks.map((link) => (
                <li key={link.name} className={styles.linkItem}>
                  <Link to={link.path} className={styles.link}>
                    {link.name}
                  </Link>
                </li>
              ))}
            </ul>
          </div>

          <div className={styles.section}>
            {/* Ressources */}
            <h3 className={styles.linkTitle}>Ressources</h3>
            <ul className={styles.linkList}>
              {resourceLinks.map((link) => (
                <li key={link.name} className={styles.linkItem}>
                  <Link to={link.path} className={styles.link}>
                    {link.name}
                  </Link>
                </li>
              ))}
            </ul>
            
            <h4 className={styles.subTitle}>Légal</h4>
            <ul className={styles.linkList}>
              {legalLinks.map((link) => (
                <li key={link.name} className={styles.linkItem}>
                  <Link to={link.path} className={styles.link}>
                    {link.name}
                  </Link>
                </li>
              ))}
            </ul>
          </div>

          <div className={`${styles.section} ${styles.contactSection}`}>
            {/* Contact et newsletter */}
            <h3 className={styles.linkTitle}>Newsletter</h3>
            <p className={styles.newsletterDescription}>
              Recevez les dernières actualités et cours directement dans votre boîte mail.
            </p>
            <form className={styles.newsletterForm}>
              <input
                type="email"
                placeholder="Votre adresse email"
                className={styles.newsletterInput}
                required
              />
              <button type="submit" className={styles.newsletterButton}>
                S'abonner
              </button>
            </form>
            <p className={styles.newsletterDisclaimer}>
              En vous inscrivant, vous acceptez de recevoir nos emails. 
              Vous pouvez vous désabonner à tout moment.
            </p>
          </div>
        </div>

        {/* Bottom section */}
        <div className={styles.bottomSection}>
          <div className={styles.copyright}>
            <p>© {currentYear} JavaCraft Academy. Tous droits réservés.</p>
          </div>
          
          <div className={styles.bottomLinks}>
            <span className={styles.madeWith}>
              Fait avec ❤️ pour la communauté Java
            </span>
          </div>
          
          <div className={styles.backToTop}>
            <button 
              className={styles.backToTopButton}
              onClick={() => window.scrollTo({ top: 0, behavior: 'smooth' })}
              aria-label="Retour en haut"
            >
              ⬆️
            </button>
          </div>
        </div>
      </div>
    </footer>
  );
};

export default Footer;