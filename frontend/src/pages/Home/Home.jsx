// src/pages/Home/Home.jsx
import React from 'react';
import { Link } from 'react-router-dom';
import styles from './Home.module.css';

const Home = () => {
  const features = [
    {
      icon: '💻',
      title: 'Éditeur de Code Intégré',
      description: 'Codez directement dans votre navigateur avec notre éditeur Java avancé'
    },
    {
      icon: '🎯',
      title: 'Exercices Pratiques',
      description: 'Apprenez en pratiquant avec des centaines d\'exercices progressifs'
    },
    {
      icon: '📊',
      title: 'Suivi de Progression',
      description: 'Suivez vos progrès et débloquez des achievements'
    },
    {
      icon: '🏆',
      title: 'Certifications',
      description: 'Obtenez des certificats reconnus pour valider vos compétences'
    }
  ];

  const testimonials = [
    {
      name: 'Marie Dubois',
      role: 'Développeuse Junior',
      text: 'JavaCraft Academy m\'a permis de maîtriser Java en quelques mois. Les exercices pratiques sont excellents !',
      avatar: 'M'
    },
    {
      name: 'Pierre Martin',
      role: 'Étudiant en Informatique',
      text: 'La progression est parfaitement structurée. Je recommande vivement cette plateforme.',
      avatar: 'P'
    },
    {
      name: 'Sophie Chen',
      role: 'Reconversion IT',
      text: 'Grâce aux cours interactifs, j\'ai pu décrocher mon premier emploi de développeuse.',
      avatar: 'S'
    }
  ];

  return (
    <div className={styles.home}>
      {/* Hero Section */}
      <section className={styles.hero}>
        <div className={styles.heroContent}>
          <h1 className={styles.heroTitle}>
            Maîtrisez <span className={styles.highlight}>Java</span> avec JavaCraft Academy
          </h1>
          <p className={styles.heroSubtitle}>
            La plateforme d'apprentissage interactive pour devenir un expert en programmation Java.
            Apprenez, pratiquez et progressez à votre rythme.
          </p>
          <div className={styles.heroActions}>
            <Link to="/register" className={styles.ctaPrimary}>
              Commencer Gratuitement
            </Link>
            <Link to="/courses" className={styles.ctaSecondary}>
              Explorer les Cours
            </Link>
          </div>
        </div>
        <div className={styles.heroImage}>
          <div className={styles.codePreview}>
            <div className={styles.codeHeader}>
              <div className={styles.codeDots}>
                <span></span>
                <span></span>
                <span></span>
              </div>
              <span className={styles.fileName}>HelloWorld.java</span>
            </div>
            <div className={styles.codeContent}>
              <pre>
{`public class HelloWorld {
    public static void main(String[] args) {
        System.out.println("Bienvenue sur JavaCraft!");
        // Votre aventure Java commence ici
    }
}`}
              </pre>
            </div>
          </div>
        </div>
      </section>

      {/* Features Section */}
      <section className={styles.features}>
        <div className={styles.container}>
          <h2 className={styles.sectionTitle}>Pourquoi choisir JavaCraft Academy ?</h2>
          <div className={styles.featuresGrid}>
            {features.map((feature, index) => (
              <div key={index} className={styles.featureCard}>
                <div className={styles.featureIcon}>{feature.icon}</div>
                <h3 className={styles.featureTitle}>{feature.title}</h3>
                <p className={styles.featureDescription}>{feature.description}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Stats Section */}
      <section className={styles.stats}>
        <div className={styles.container}>
          <div className={styles.statsGrid}>
            <div className={styles.statCard}>
              <div className={styles.statNumber}>10K+</div>
              <div className={styles.statLabel}>Étudiants Actifs</div>
            </div>
            <div className={styles.statCard}>
              <div className={styles.statNumber}>500+</div>
              <div className={styles.statLabel}>Exercices Pratiques</div>
            </div>
            <div className={styles.statCard}>
              <div className={styles.statNumber}>50+</div>
              <div className={styles.statLabel}>Heures de Contenu</div>
            </div>
            <div className={styles.statCard}>
              <div className={styles.statNumber}>95%</div>
              <div className={styles.statLabel}>Taux de Satisfaction</div>
            </div>
          </div>
        </div>
      </section>

      {/* Learning Path Preview */}
      <section className={styles.learningPath}>
        <div className={styles.container}>
          <h2 className={styles.sectionTitle}>Votre Parcours d'Apprentissage</h2>
          <div className={styles.pathSteps}>
            <div className={styles.pathStep}>
              <div className={styles.stepNumber}>1</div>
              <div className={styles.stepContent}>
                <h3>Fondamentaux Java</h3>
                <p>Variables, types, structures de contrôle</p>
              </div>
            </div>
            <div className={styles.pathConnector}></div>
            <div className={styles.pathStep}>
              <div className={styles.stepNumber}>2</div>
              <div className={styles.stepContent}>
                <h3>Programmation Orientée Objet</h3>
                <p>Classes, objets, héritage, polymorphisme</p>
              </div>
            </div>
            <div className={styles.pathConnector}></div>
            <div className={styles.pathStep}>
              <div className={styles.stepNumber}>3</div>
              <div className={styles.stepContent}>
                <h3>Concepts Avancés</h3>
                <p>Collections, exceptions, threads, I/O</p>
              </div>
            </div>
            <div className={styles.pathConnector}></div>
            <div className={styles.pathStep}>
              <div className={styles.stepNumber}>4</div>
              <div className={styles.stepContent}>
                <h3>Projets Pratiques</h3>
                <p>Applications réelles et portfolio</p>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Testimonials */}
      <section className={styles.testimonials}>
        <div className={styles.container}>
          <h2 className={styles.sectionTitle}>Ce que disent nos étudiants</h2>
          <div className={styles.testimonialsGrid}>
            {testimonials.map((testimonial, index) => (
              <div key={index} className={styles.testimonialCard}>
                <div className={styles.testimonialContent}>
                  <p>"{testimonial.text}"</p>
                </div>
                <div className={styles.testimonialAuthor}>
                  <div className={styles.authorAvatar}>{testimonial.avatar}</div>
                  <div className={styles.authorInfo}>
                    <div className={styles.authorName}>{testimonial.name}</div>
                    <div className={styles.authorRole}>{testimonial.role}</div>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* CTA Section */}
      <section className={styles.finalCta}>
        <div className={styles.container}>
          <div className={styles.ctaContent}>
            <h2>Prêt à commencer votre aventure Java ?</h2>
            <p>Rejoignez des milliers d'étudiants qui ont transformé leur carrière avec JavaCraft Academy</p>
            <Link to="/register" className={styles.ctaPrimary}>
              S'inscrire Maintenant
            </Link>
          </div>
        </div>
      </section>
    </div>
  );
};

export default Home;
