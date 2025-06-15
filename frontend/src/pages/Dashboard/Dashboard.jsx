import React from 'react';
import styles from './Dashboard.module.css';

const Dashboard = () => {
  return (
    <div className={styles.dashboard}>
      <div className={styles.header}>
        <h1 className={styles.title}>Dashboard</h1>
        <p className={styles.subtitle}>Bienvenue sur votre tableau de bord</p>
      </div>
      
      <div className={styles.content}>
        <div className={styles.statsGrid}>
          <div className={styles.statCard}>
            <h3 className={styles.statTitle}>Cours terminés</h3>
            <p className={styles.statValue}>12</p>
          </div>
          
          <div className={styles.statCard}>
            <h3 className={styles.statTitle}>Heures d'apprentissage</h3>
            <p className={styles.statValue}>48h</p>
          </div>
          
          <div className={styles.statCard}>
            <h3 className={styles.statTitle}>Projets en cours</h3>
            <p className={styles.statValue}>3</p>
          </div>
          
          <div className={styles.statCard}>
            <h3 className={styles.statTitle}>Score moyen</h3>
            <p className={styles.statValue}>85%</p>
          </div>
        </div>
        
        <div className={styles.recentActivity}>
          <h2 className={styles.sectionTitle}>Activité récente</h2>
          <div className={styles.activityList}>
            <div className={styles.activityItem}>
              <span className={styles.activityText}>Cours "Introduction à Java" terminé</span>
              <span className={styles.activityTime}>Il y a 2 heures</span>
            </div>
            <div className={styles.activityItem}>
              <span className={styles.activityText}>Nouveau projet "Application Web" créé</span>
              <span className={styles.activityTime}>Hier</span>
            </div>
            <div className={styles.activityItem}>
              <span className={styles.activityText}>Quiz "Programmation Orientée Objet" réussi</span>
              <span className={styles.activityTime}>Il y a 3 jours</span>
            </div>
          </div>
        </div>
        
        <div className={styles.quickActions}>
          <h2 className={styles.sectionTitle}>Actions rapides</h2>
          <div className={styles.actionButtons}>
            <button className={styles.actionBtn}>Nouveau cours</button>
            <button className={styles.actionBtn}>Voir les projets</button>
            <button className={styles.actionBtn}>Accéder aux quiz</button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;