
import React, { useState, useEffect, useMemo, useCallback } from 'react';
import PropTypes from 'prop-types';
import { useProgress } from '../../../hooks/useProgress';
import { useNotification } from '../../../hooks/useNotification';
import { useAuth } from '../../../hooks/useAuth';
import styles from './Achievement.module.css';

/**
 * @fileoverview Composant Achievement - Affiche et gère les réalisations/succès de l'utilisateur
 * 
 * Ce composant fait partie du système de gamification de JavaCraft Academy.
 * Il permet d'afficher les différents achievements (réalisations) que l'utilisateur
 * peut débloquer en progressant dans ses cours et exercices.
 * 
 * @component Achievement
 * @author JavaCraft Academy Team
 * @version 1.0.0
 * @since 2024-01-01
 * 
 * Relations avec l'application :
 * - Utilise useProgress pour récupérer les données de progression
 * - Utilise useNotification pour afficher les notifications de nouveaux achievements
 * - Utilise useAuth pour identifier l'utilisateur courant
 * - Communique avec ProgressContext pour mettre à jour l'état global
 * - Intégré dans la page Progress et le Dashboard utilisateur
 * 
 * Fonctionnalités principales :
 * - Affichage des achievements débloqués et verrouillés
 * - Animation lors du déblocage d'un nouvel achievement
 * - Filtrage par catégorie (cours, exercices, temps, streak)
 * - Système de rareté (commun, rare, épique, légendaire)
 * - Tooltip avec description détaillée
 * - Mode compact et étendu d'affichage
 */

/**
 * Types d'achievements disponibles dans l'application
 * @constant {Object} ACHIEVEMENT_TYPES
 */
const ACHIEVEMENT_TYPES = {
  COURSE: 'course',
  EXERCISE: 'exercise',
  TIME: 'time',
  STREAK: 'streak',
  SPECIAL: 'special'
};

/**
 * Niveaux de rareté des achievements
 * @constant {Object} RARITY_LEVELS
 */
const RARITY_LEVELS = {
  COMMON: 'common',
  RARE: 'rare',
  EPIC: 'epic',
  LEGENDARY: 'legendary'
};

/**
 * Composant Achievement principal
 * 
 * @param {Object} props - Propriétés du composant
 * @param {string} [props.userId] - ID de l'utilisateur (optionnel, utilise l'utilisateur connecté par défaut)
 * @param {string} [props.displayMode='grid'] - Mode d'affichage ('grid', 'list', 'compact')
 * @param {string[]} [props.filterTypes] - Types d'achievements à afficher
 * @param {boolean} [props.showUnlocked=true] - Afficher les achievements débloqués
 * @param {boolean} [props.showLocked=true] - Afficher les achievements verrouillés
 * @param {boolean} [props.animated=true] - Activer les animations
 * @param {Function} [props.onAchievementClick] - Callback lors du clic sur un achievement
 * @param {Function} [props.onNewAchievement] - Callback lors du déblocage d'un nouvel achievement
 * @param {string} [props.className] - Classes CSS additionnelles
 * @returns {JSX.Element} Composant Achievement rendu
 */
const Achievement = ({
  userId,
  displayMode = 'grid',
  filterTypes = [],
  showUnlocked = true,
  showLocked = true,
  animated = true,
  onAchievementClick,
  onNewAchievement,
  className = ''
}) => {
  // Hooks pour la gestion de l'état et des données
  const { user } = useAuth();
  const { 
    achievements, 
    userAchievements, 
    loadAchievements, 
    loadUserAchievements,
    checkNewAchievements 
  } = useProgress();
  const { showNotification } = useNotification();

  // État local du composant
  const [selectedCategory, setSelectedCategory] = useState('all');
  const [selectedRarity, setSelectedRarity] = useState('all');
  const [searchTerm, setSearchTerm] = useState('');
  const [isLoading, setIsLoading] = useState(true);
  const [newAchievements, setNewAchievements] = useState([]);
  const [showTooltip, setShowTooltip] = useState(null);

  // ID utilisateur effectif (props ou utilisateur connecté)
  const effectiveUserId = userId || user?.id;

  /**
   * Charge les données d'achievements au montage du composant
   * @function loadData
   */
  const loadData = useCallback(async () => {
    if (!effectiveUserId) return;

    try {
      setIsLoading(true);
      await Promise.all([
        loadAchievements(),
        loadUserAchievements(effectiveUserId)
      ]);
    } catch (error) {
      console.error('Erreur lors du chargement des achievements:', error);
      showNotification('Erreur lors du chargement des réalisations', 'error');
    } finally {
      setIsLoading(false);
    }
  }, [effectiveUserId, loadAchievements, loadUserAchievements, showNotification]);

  /**
   * Vérifie les nouveaux achievements de manière périodique
   * @function checkForNewAchievements
   */
  const checkForNewAchievements = useCallback(async () => {
    if (!effectiveUserId) return;

    try {
      const newAchievs = await checkNewAchievements(effectiveUserId);
      if (newAchievs && newAchievs.length > 0) {
        setNewAchievements(prev => [...prev, ...newAchievs]);
        
        // Notifier chaque nouvel achievement
        newAchievs.forEach(achievement => {
          showNotification(
            `🏆 Nouveau succès débloqué : ${achievement.name}!`,
            'success',
            5000
          );
          
          // Callback personnalisé si fourni
          if (onNewAchievement) {
            onNewAchievement(achievement);
          }
        });
      }
    } catch (error) {
      console.error('Erreur lors de la vérification des nouveaux achievements:', error);
    }
  }, [effectiveUserId, checkNewAchievements, showNotification, onNewAchievement]);

  // Effets
  useEffect(() => {
    loadData();
  }, [loadData]);

  useEffect(() => {
    // Vérification périodique des nouveaux achievements
    const interval = setInterval(checkForNewAchievements, 30000); // Toutes les 30 secondes
    return () => clearInterval(interval);
  }, [checkForNewAchievements]);

  /**
   * Filtre et trie les achievements selon les critères sélectionnés
   * @function filteredAchievements
   * @returns {Array} Liste des achievements filtrés
   */
  const filteredAchievements = useMemo(() => {
    if (!achievements) return [];

    return achievements.filter(achievement => {
      // Filtre par type
      if (filterTypes.length > 0 && !filterTypes.includes(achievement.type)) {
        return false;
      }

      // Filtre par catégorie sélectionnée
      if (selectedCategory !== 'all' && achievement.type !== selectedCategory) {
        return false;
      }

      // Filtre par rareté
      if (selectedRarity !== 'all' && achievement.rarity !== selectedRarity) {
        return false;
      }

      // Filtre par terme de recherche
      if (searchTerm && !achievement.name.toLowerCase().includes(searchTerm.toLowerCase()) &&
          !achievement.description.toLowerCase().includes(searchTerm.toLowerCase())) {
        return false;
      }

      // Filtre par statut (débloqué/verrouillé)
      const isUnlocked = userAchievements?.some(ua => ua.achievementId === achievement.id);
      if (!showUnlocked && isUnlocked) return false;
      if (!showLocked && !isUnlocked) return false;

      return true;
    }).sort((a, b) => {
      // Tri : débloqués en premier, puis par rareté, puis par nom
      const aUnlocked = userAchievements?.some(ua => ua.achievementId === a.id);
      const bUnlocked = userAchievements?.some(ua => ua.achievementId === b.id);

      if (aUnlocked !== bUnlocked) {
        return bUnlocked ? 1 : -1;
      }

      const rarityOrder = { legendary: 4, epic: 3, rare: 2, common: 1 };
      if (a.rarity !== b.rarity) {
        return rarityOrder[b.rarity] - rarityOrder[a.rarity];
      }

      return a.name.localeCompare(b.name);
    });
  }, [achievements, userAchievements, filterTypes, selectedCategory, selectedRarity, searchTerm, showUnlocked, showLocked]);

  /**
   * Calcule les statistiques des achievements
   * @function achievementStats
   * @returns {Object} Statistiques des achievements
   */
  const achievementStats = useMemo(() => {
    if (!achievements || !userAchievements) return { total: 0, unlocked: 0, percentage: 0 };

    const total = achievements.length;
    const unlocked = userAchievements.length;
    const percentage = total > 0 ? Math.round((unlocked / total) * 100) : 0;

    return { total, unlocked, percentage };
  }, [achievements, userAchievements]);

  /**
   * Gère le clic sur un achievement
   * @function handleAchievementClick
   * @param {Object} achievement - Achievement cliqué
   */
  const handleAchievementClick = useCallback((achievement) => {
    if (onAchievementClick) {
      onAchievementClick(achievement);
    }
  }, [onAchievementClick]);

  /**
   * Gère l'affichage du tooltip
   * @function handleTooltip
   * @param {string|null} achievementId - ID de l'achievement ou null pour fermer
   */
  const handleTooltip = useCallback((achievementId) => {
    setShowTooltip(achievementId);
  }, []);

  /**
   * Obtient l'icône pour un type d'achievement
   * @function getAchievementIcon
   * @param {string} type - Type d'achievement
   * @returns {string} Emoji ou caractère représentant le type
   */
  const getAchievementIcon = (type) => {
    const icons = {
      [ACHIEVEMENT_TYPES.COURSE]: '📚',
      [ACHIEVEMENT_TYPES.EXERCISE]: '💪',
      [ACHIEVEMENT_TYPES.TIME]: '⏰',
      [ACHIEVEMENT_TYPES.STREAK]: '🔥',
      [ACHIEVEMENT_TYPES.SPECIAL]: '⭐'
    };
    return icons[type] || '🏆';
  };

  /**
   * Obtient la classe CSS pour la rareté
   * @function getRarityClass
   * @param {string} rarity - Niveau de rareté
   * @returns {string} Classe CSS correspondante
   */
  const getRarityClass = (rarity) => {
    return styles[`rarity-${rarity}`] || '';
  };

  /**
   * Vérifie si un achievement est débloqué
   * @function isAchievementUnlocked
   * @param {string} achievementId - ID de l'achievement
   * @returns {boolean} True si débloqué
   */
  const isAchievementUnlocked = (achievementId) => {
    return userAchievements?.some(ua => ua.achievementId === achievementId) || false;
  };

  /**
   * Obtient la date de déblocage d'un achievement
   * @function getUnlockDate
   * @param {string} achievementId - ID de l'achievement
   * @returns {Date|null} Date de déblocage ou null
   */
  const getUnlockDate = (achievementId) => {
    const userAchievement = userAchievements?.find(ua => ua.achievementId === achievementId);
    return userAchievement ? new Date(userAchievement.unlockedAt) : null;
  };

  if (isLoading) {
    return (
      <div className={`${styles.achievement} ${styles.loading} ${className}`}>
        <div className={styles.loadingSpinner}>
          <div className={styles.spinner}></div>
          <p>Chargement des réalisations...</p>
        </div>
      </div>
    );
  }

  return (
    <div className={`${styles.achievement} ${styles[displayMode]} ${className}`}>
      {/* En-tête avec statistiques */}
      <div className={styles.header}>
        <div className={styles.stats}>
          <h2 className={styles.title}>
            🏆 Réalisations
            <span className={styles.badge}>
              {achievementStats.unlocked}/{achievementStats.total}
            </span>
          </h2>
          <div className={styles.progressBar}>
            <div 
              className={styles.progressFill} 
              style={{ width: `${achievementStats.percentage}%` }}
            ></div>
            <span className={styles.progressText}>
              {achievementStats.percentage}% complété
            </span>
          </div>
        </div>

        {/* Filtres */}
        <div className={styles.filters}>
          <div className={styles.searchBox}>
            <input
              type="text"
              placeholder="Rechercher une réalisation..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className={styles.searchInput}
            />
          </div>

          <select
            value={selectedCategory}
            onChange={(e) => setSelectedCategory(e.target.value)}
            className={styles.filterSelect}
          >
            <option value="all">Toutes les catégories</option>
            <option value={ACHIEVEMENT_TYPES.COURSE}>📚 Cours</option>
            <option value={ACHIEVEMENT_TYPES.EXERCISE}>💪 Exercices</option>
            <option value={ACHIEVEMENT_TYPES.TIME}>⏰ Temps</option>
            <option value={ACHIEVEMENT_TYPES.STREAK}>🔥 Séries</option>
            <option value={ACHIEVEMENT_TYPES.SPECIAL}>⭐ Spéciaux</option>
          </select>

          <select
            value={selectedRarity}
            onChange={(e) => setSelectedRarity(e.target.value)}
            className={styles.filterSelect}
          >
            <option value="all">Toutes les raretés</option>
            <option value={RARITY_LEVELS.COMMON}>⚪ Commun</option>
            <option value={RARITY_LEVELS.RARE}>🔵 Rare</option>
            <option value={RARITY_LEVELS.EPIC}>🟣 Épique</option>
            <option value={RARITY_LEVELS.LEGENDARY}>🟡 Légendaire</option>
          </select>
        </div>
      </div>

      {/* Liste des achievements */}
      <div className={styles.achievementList}>
        {filteredAchievements.length === 0 ? (
          <div className={styles.emptyState}>
            <div className={styles.emptyIcon}>🎯</div>
            <h3>Aucune réalisation trouvée</h3>
            <p>Ajustez vos filtres ou commencez votre apprentissage pour débloquer des réalisations !</p>
          </div>
        ) : (
          filteredAchievements.map((achievement) => {
            const isUnlocked = isAchievementUnlocked(achievement.id);
            const unlockDate = getUnlockDate(achievement.id);
            const isNew = newAchievements.some(na => na.id === achievement.id);

            return (
              <div
                key={achievement.id}
                className={`
                  ${styles.achievementCard} 
                  ${isUnlocked ? styles.unlocked : styles.locked}
                  ${getRarityClass(achievement.rarity)}
                  ${isNew ? styles.newAchievement : ''}
                  ${animated ? styles.animated : ''}
                `}
                onClick={() => handleAchievementClick(achievement)}
                onMouseEnter={() => handleTooltip(achievement.id)}
                onMouseLeave={() => handleTooltip(null)}
              >
                {/* Badge de nouveauté */}
                {isNew && <div className={styles.newBadge}>NOUVEAU!</div>}

                {/* Icône et indicateur de rareté */}
                <div className={styles.achievementIcon}>
                  <span className={styles.typeIcon}>
                    {getAchievementIcon(achievement.type)}
                  </span>
                  <div className={`${styles.rarityBorder} ${getRarityClass(achievement.rarity)}`}></div>
                </div>

                {/* Contenu */}
                <div className={styles.achievementContent}>
                  <h4 className={styles.achievementName}>
                    {achievement.name}
                    {isUnlocked && <span className={styles.checkmark}>✓</span>}
                  </h4>
                  
                  <p className={styles.achievementDescription}>
                    {achievement.description}
                  </p>

                  {/* Conditions et progression */}
                  <div className={styles.achievementDetails}>
                    {achievement.conditions && (
                      <div className={styles.conditions}>
                        <small>Conditions: {achievement.conditions}</small>
                      </div>
                    )}

                    {achievement.points && (
                      <div className={styles.points}>
                        <span className={styles.pointsBadge}>
                          {achievement.points} pts
                        </span>
                      </div>
                    )}
                  </div>

                  {/* Date de déblocage */}
                  {isUnlocked && unlockDate && (
                    <div className={styles.unlockDate}>
                      <small>Débloqué le {unlockDate.toLocaleDateString('fr-FR')}</small>
                    </div>
                  )}
                </div>

                {/* Tooltip détaillé */}
                {showTooltip === achievement.id && (
                  <div className={styles.tooltip}>
                    <div className={styles.tooltipContent}>
                      <h5>{achievement.name}</h5>
                      <p>{achievement.detailedDescription || achievement.description}</p>
                      
                      {achievement.tips && (
                        <div className={styles.tips}>
                          <strong>Conseil:</strong> {achievement.tips}
                        </div>
                      )}

                      <div className={styles.tooltipFooter}>
                        <span className={styles.rarityLabel}>
                          {achievement.rarity.charAt(0).toUpperCase() + achievement.rarity.slice(1)}
                        </span>
                        {achievement.category && (
                          <span className={styles.categoryLabel}>
                            {achievement.category}
                          </span>
                        )}
                      </div>
                    </div>
                  </div>
                )}
              </div>
            );
          })
        )}
      </div>

      {/* Indicateur de nouveaux achievements */}
      {newAchievements.length > 0 && (
        <div className={styles.newAchievementsIndicator}>
          <button
            className={styles.newAchievementsButton}
            onClick={() => setNewAchievements([])}
          >
            🎉 {newAchievements.length} nouvelle(s) réalisation(s) !
          </button>
        </div>
      )}
    </div>
  );
};

// Validation des props avec PropTypes
Achievement.propTypes = {
  userId: PropTypes.string,
  displayMode: PropTypes.oneOf(['grid', 'list', 'compact']),
  filterTypes: PropTypes.arrayOf(PropTypes.string),
  showUnlocked: PropTypes.bool,
  showLocked: PropTypes.bool,
  animated: PropTypes.bool,
  onAchievementClick: PropTypes.func,
  onNewAchievement: PropTypes.func,
  className: PropTypes.string
};

export default Achievement;