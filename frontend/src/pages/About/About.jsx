import React from 'react';
import styles from './About.module.css';

const About = () => {
  return (
    <div className={styles.aboutPage}>
      <div className={styles.container}>
        <h1>À propos de JavaCraft Academy</h1>
        <div className={styles.aboutContent}>
          <section className={styles.mission}>
            <h2>Notre Mission</h2>
            <p>JavaCraft Academy a pour mission de démocratiser l'apprentissage de Java en proposant des cours interactifs, pratiques et accessibles à tous.</p>
          </section>
          <section className={styles.values}>
            <h2>Nos Valeurs</h2>
            <ul>
              <li>Excellence pédagogique</li>
              <li>Apprentissage pratique</li>
              <li>Communauté bienveillante</li>
              <li>Innovation continue</li>
            </ul>
          </section>
          <section className={styles.team}>
            <h2>Notre Équipe</h2>
            <p>Une équipe passionnée d'experts Java et de pédagogues dédiés à votre réussite.</p>
          </section>
        </div>
      </div>
    </div>
  );
};

export default About;