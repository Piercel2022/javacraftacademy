import React, { useState, useEffect, useMemo } from 'react';
import PropTypes from 'prop-types';
import styles from './CourseList.module.css';
import { useCourse } from '../../hooks/useCourse';
import { useAuth } from '../../hooks/useAuth';
import { useNotification } from '../../hooks/useNotification';
import CourseCard from '../CourseCard';
import Loading from '../common/Loading';
import Button from '../common/Button';

/**
 * @fileoverview Composant CourseList - Affiche une liste paginée et filtrable de cours
 * @author JavaCraft Academy Team
 * @version 1.0.0
 * @since 2024-01-01
 */

/**
 * Composant CourseList - Gère l'affichage d'une liste de cours avec fonctionnalités avancées
 * 
 * @description
 * Ce composant est responsable de l'affichage d'une liste de cours avec les fonctionnalités suivantes :
 * - Pagination des résultats
 * - Filtrage par catégorie, niveau et statut
 * - Recherche textuelle
 * - Tri par différents critères
 * - Gestion des états de chargement et d'erreur
 * - Inscription/désinscription aux cours
 * - Affichage adaptatif (grille/liste)
 * 
 * @relations
 * - Utilise CourseCard pour afficher chaque cours individuellement
 * - Communique avec le CourseContext via useCourse hook
 * - Utilise AuthContext pour gérer les permissions utilisateur
 * - Intègre avec le système de notifications
 * - Peut être utilisé dans les pages Courses, Dashboard, Home
 * 
 * @param {Object} props - Propriétés du composant
 * @param {Array} props.courses - Liste des cours à afficher (optionnel si utilise le contexte)
 * @param {Object} props.filters - Filtres à appliquer par défaut
 * @param {string} props.viewMode - Mode d'affichage ('grid' | 'list')
 * @param {boolean} props.showFilters - Afficher les contrôles de filtrage
 * @param {boolean} props.showPagination - Afficher la pagination
 * @param {boolean} props.showEnrollButton - Afficher les boutons d'inscription
 * @param {number} props.itemsPerPage - Nombre d'éléments par page
 * @param {Function} props.onCourseSelect - Callback appelé lors de la sélection d'un cours
 * @param {Function} props.onEnroll - Callback appelé lors de l'inscription à un cours
 * @param {string} props.className - Classes CSS additionnelles
 * 
 * @returns {JSX.Element} Le composant CourseList rendu
 * 
 * @example
 * // Utilisation basique avec contexte
 * <CourseList 
 *   showFilters={true}
 *   showPagination={true}
 *   itemsPerPage={12}
 * />
 * 
 * @example
 * // Utilisation avec cours spécifiques
 * <CourseList 
 *   courses={myCourses}
 *   viewMode="list"
 *   showEnrollButton={false}
 *   onCourseSelect={handleCourseSelect}
 * />
 */
const CourseList = ({
  courses: propCourses = null,
  filters: defaultFilters = {},
  viewMode = 'grid',
  showFilters = true,
  showPagination = true,
  showEnrollButton = true,
  itemsPerPage = 12,
  onCourseSelect = null,
  onEnroll = null,
  className = ''
}) => {
  // Hooks pour la gestion des données et de l'état
  const {
    courses: contextCourses,
    loading,
    error,
    categories,
    enrollCourse,
    unenrollCourse,
    searchCourses,
    refreshCourses
  } = useCourse();
  
  const { user, isAuthenticated } = useAuth();
  const { showNotification } = useNotification();

  // État local du composant
  const [currentPage, setCurrentPage] = useState(1);
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('all');
  const [selectedLevel, setSelectedLevel] = useState('all');
  const [selectedStatus, setSelectedStatus] = useState('all');
  const [sortBy, setSortBy] = useState('title');
  const [sortOrder, setSortOrder] = useState('asc');
  const [localViewMode, setLocalViewMode] = useState(viewMode);

  // Déterminer la source des cours (props ou contexte)
  const coursesData = propCourses || contextCourses;

  /**
   * Filtre et trie les cours selon les critères sélectionnés
   * 
   * @returns {Array} Liste des cours filtrés et triés
   */
  const filteredAndSortedCourses = useMemo(() => {
    if (!coursesData || coursesData.length === 0) return [];

    let filtered = [...coursesData];

    // Filtrage par terme de recherche
    if (searchTerm) {
      filtered = filtered.filter(course =>
        course.title.toLowerCase().includes(searchTerm.toLowerCase()) ||
        course.description.toLowerCase().includes(searchTerm.toLowerCase()) ||
        course.instructor.toLowerCase().includes(searchTerm.toLowerCase())
      );
    }

    // Filtrage par catégorie
    if (selectedCategory !== 'all') {
      filtered = filtered.filter(course => course.category === selectedCategory);
    }

    // Filtrage par niveau
    if (selectedLevel !== 'all') {
      filtered = filtered.filter(course => course.level === selectedLevel);
    }

    // Filtrage par statut d'inscription
    if (selectedStatus !== 'all' && isAuthenticated) {
      filtered = filtered.filter(course => {
        const isEnrolled = user?.enrolledCourses?.includes(course.id);
        return selectedStatus === 'enrolled' ? isEnrolled : !isEnrolled;
      });
    }

    // Tri des résultats
    filtered.sort((a, b) => {
      let aValue = a[sortBy];
      let bValue = b[sortBy];

      // Gestion spéciale pour certains champs
      if (sortBy === 'createdAt' || sortBy === 'updatedAt') {
        aValue = new Date(aValue);
        bValue = new Date(bValue);
      } else if (sortBy === 'rating') {
        aValue = parseFloat(aValue) || 0;
        bValue = parseFloat(bValue) || 0;
      } else if (typeof aValue === 'string') {
        aValue = aValue.toLowerCase();
        bValue = bValue.toLowerCase();
      }

      if (sortOrder === 'asc') {
        return aValue < bValue ? -1 : aValue > bValue ? 1 : 0;
      } else {
        return aValue > bValue ? -1 : aValue < bValue ? 1 : 0;
      }
    });

    return filtered;
  }, [coursesData, searchTerm, selectedCategory, selectedLevel, selectedStatus, sortBy, sortOrder, user, isAuthenticated]);

  /**
   * Calcule les cours à afficher pour la page courante
   * 
   * @returns {Array} Cours de la page courante
   */
  const paginatedCourses = useMemo(() => {
    if (!showPagination) return filteredAndSortedCourses;

    const startIndex = (currentPage - 1) * itemsPerPage;
    const endIndex = startIndex + itemsPerPage;
    return filteredAndSortedCourses.slice(startIndex, endIndex);
  }, [filteredAndSortedCourses, currentPage, itemsPerPage, showPagination]);

  /**
   * Calcule le nombre total de pages
   * 
   * @returns {number} Nombre total de pages
   */
  const totalPages = useMemo(() => {
    return Math.ceil(filteredAndSortedCourses.length / itemsPerPage);
  }, [filteredAndSortedCourses.length, itemsPerPage]);

  /**
   * Gère l'inscription/désinscription à un cours
   * 
   * @param {Object} course - Le cours concerné
   * @param {boolean} isCurrentlyEnrolled - Statut d'inscription actuel
   * @returns {Promise<void>}
   */
  const handleEnrollToggle = async (course, isCurrentlyEnrolled) => {
    if (!isAuthenticated) {
      showNotification('Vous devez être connecté pour vous inscrire à un cours', 'warning');
      return;
    }

    try {
      if (isCurrentlyEnrolled) {
        await unenrollCourse(course.id);
        showNotification(`Vous avez été désinscrit du cours "${course.title}"`, 'success');
      } else {
        await enrollCourse(course.id);
        showNotification(`Vous êtes maintenant inscrit au cours "${course.title}"`, 'success');
      }

      // Callback personnalisé si fourni
      if (onEnroll) {
        onEnroll(course, !isCurrentlyEnrolled);
      }
    } catch (error) {
      showNotification(
        `Erreur lors de l'${isCurrentlyEnrolled ? 'désinscription' : 'inscription'}: ${error.message}`,
        'error'
      );
    }
  };

  /**
   * Gère la sélection d'un cours
   * 
   * @param {Object} course - Le cours sélectionné
   */
  const handleCourseSelect = (course) => {
    if (onCourseSelect) {
      onCourseSelect(course);
    }
  };

  /**
   * Remet à zéro tous les filtres
   */
  const resetFilters = () => {
    setSearchTerm('');
    setSelectedCategory('all');
    setSelectedLevel('all');
    setSelectedStatus('all');
    setSortBy('title');
    setSortOrder('asc');
    setCurrentPage(1);
  };

  /**
   * Change la page courante
   * 
   * @param {number} page - Numéro de la nouvelle page
   */
  const handlePageChange = (page) => {
    setCurrentPage(page);
    // Scroll vers le haut de la liste
    document.querySelector(`.${styles.courseList}`)?.scrollIntoView({ behavior: 'smooth' });
  };

  // Effet pour réinitialiser la page lors du changement de filtres
  useEffect(() => {
    setCurrentPage(1);
  }, [searchTerm, selectedCategory, selectedLevel, selectedStatus, sortBy, sortOrder]);

  // Effet pour appliquer les filtres par défaut
  useEffect(() => {
    if (defaultFilters.category) setSelectedCategory(defaultFilters.category);
    if (defaultFilters.level) setSelectedLevel(defaultFilters.level);
    if (defaultFilters.status) setSelectedStatus(defaultFilters.status);
    if (defaultFilters.search) setSearchTerm(defaultFilters.search);
  }, [defaultFilters]);

  // Gestion des états de chargement et d'erreur
  if (loading && !coursesData) {
    return <Loading message="Chargement des cours..." />;
  }

  if (error) {
    return (
      <div className={styles.errorContainer}>
        <div className={styles.errorMessage}>
          <h3>Erreur lors du chargement des cours</h3>
          <p>{error}</p>
          <Button onClick={refreshCourses} variant="primary">
            Réessayer
          </Button>
        </div>
      </div>
    );
  }

  return (
    <div className={`${styles.courseList} ${className}`}>
      {/* Barre de filtres et de recherche */}
      {showFilters && (
        <div className={styles.filtersContainer}>
          <div className={styles.searchAndFilters}>
            {/* Barre de recherche */}
            <div className={styles.searchContainer}>
              <input
                type="text"
                placeholder="Rechercher un cours..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className={styles.searchInput}
              />
              <button
                type="button"
                className={styles.searchButton}
                aria-label="Rechercher"
              >
                🔍
              </button>
            </div>

            {/* Filtres */}
            <div className={styles.filters}>
              <select
                value={selectedCategory}
                onChange={(e) => setSelectedCategory(e.target.value)}
                className={styles.filterSelect}
              >
                <option value="all">Toutes les catégories</option>
                {categories.map(category => (
                  <option key={category.id} value={category.id}>
                    {category.name}
                  </option>
                ))}
              </select>

              <select
                value={selectedLevel}
                onChange={(e) => setSelectedLevel(e.target.value)}
                className={styles.filterSelect}
              >
                <option value="all">Tous les niveaux</option>
                <option value="beginner">Débutant</option>
                <option value="intermediate">Intermédiaire</option>
                <option value="advanced">Avancé</option>
              </select>

              {isAuthenticated && (
                <select
                  value={selectedStatus}
                  onChange={(e) => setSelectedStatus(e.target.value)}
                  className={styles.filterSelect}
                >
                  <option value="all">Tous les cours</option>
                  <option value="enrolled">Mes cours</option>
                  <option value="available">Disponibles</option>
                </select>
              )}

              <select
                value={`${sortBy}-${sortOrder}`}
                onChange={(e) => {
                  const [field, order] = e.target.value.split('-');
                  setSortBy(field);
                  setSortOrder(order);
                }}
                className={styles.filterSelect}
              >
                <option value="title-asc">Titre (A-Z)</option>
                <option value="title-desc">Titre (Z-A)</option>
                <option value="createdAt-desc">Plus récents</option>
                <option value="createdAt-asc">Plus anciens</option>
                <option value="rating-desc">Mieux notés</option>
                <option value="level-asc">Niveau croissant</option>
              </select>
            </div>
          </div>

          {/* Contrôles d'affichage */}
          <div className={styles.displayControls}>
            <div className={styles.viewModeToggle}>
              <button
                type="button"
                className={`${styles.viewModeButton} ${localViewMode === 'grid' ? styles.active : ''}`}
                onClick={() => setLocalViewMode('grid')}
                aria-label="Affichage en grille"
              >
                ⊞
              </button>
              <button
                type="button"
                className={`${styles.viewModeButton} ${localViewMode === 'list' ? styles.active : ''}`}
                onClick={() => setLocalViewMode('list')}
                aria-label="Affichage en liste"
              >
                ☰
              </button>
            </div>

            <Button
              onClick={resetFilters}
              variant="secondary"
              size="small"
            >
              Réinitialiser
            </Button>
          </div>
        </div>
      )}

      {/* Informations sur les résultats */}
      <div className={styles.resultsInfo}>
        <p className={styles.resultsCount}>
          {filteredAndSortedCourses.length} cours trouvé{filteredAndSortedCourses.length > 1 ? 's' : ''}
          {searchTerm && ` pour "${searchTerm}"`}
        </p>
      </div>

      {/* Liste des cours */}
      {paginatedCourses.length > 0 ? (
        <div className={`${styles.coursesGrid} ${styles[localViewMode]}`}>
          {paginatedCourses.map(course => {
            const isEnrolled = isAuthenticated && user?.enrolledCourses?.includes(course.id);
            
            return (
              <CourseCard
                key={course.id}
                course={course}
                isEnrolled={isEnrolled}
                showEnrollButton={showEnrollButton}
                viewMode={localViewMode}
                onSelect={() => handleCourseSelect(course)}
                onEnrollToggle={(isCurrentlyEnrolled) => handleEnrollToggle(course, isCurrentlyEnrolled)}
              />
            );
          })}
        </div>
      ) : (
        <div className={styles.emptyState}>
          <div className={styles.emptyStateContent}>
            <h3>Aucun cours trouvé</h3>
            <p>
              {searchTerm || selectedCategory !== 'all' || selectedLevel !== 'all' || selectedStatus !== 'all'
                ? 'Essayez de modifier vos critères de recherche.'
                : 'Aucun cours disponible pour le moment.'}
            </p>
            {(searchTerm || selectedCategory !== 'all' || selectedLevel !== 'all' || selectedStatus !== 'all') && (
              <Button onClick={resetFilters} variant="primary">
                Voir tous les cours
              </Button>
            )}
          </div>
        </div>
      )}

      {/* Pagination */}
      {showPagination && totalPages > 1 && (
        <div className={styles.pagination}>
          <button
            type="button"
            className={styles.paginationButton}
            onClick={() => handlePageChange(currentPage - 1)}
            disabled={currentPage === 1}
          >
            Précédent
          </button>

          <div className={styles.paginationNumbers}>
            {Array.from({ length: totalPages }, (_, index) => index + 1).map(page => (
              <button
                key={page}
                type="button"
                className={`${styles.paginationNumber} ${currentPage === page ? styles.active : ''}`}
                onClick={() => handlePageChange(page)}
              >
                {page}
              </button>
            ))}
          </div>

          <button
            type="button"
            className={styles.paginationButton}
            onClick={() => handlePageChange(currentPage + 1)}
            disabled={currentPage === totalPages}
          >
            Suivant
          </button>
        </div>
      )}
    </div>
  );
};

// Définition des PropTypes pour la validation des propriétés
CourseList.propTypes = {
  /** Liste des cours à afficher (optionnel si utilise le contexte) */
  courses: PropTypes.arrayOf(
    PropTypes.shape({
      id: PropTypes.oneOfType([PropTypes.string, PropTypes.number]).isRequired,
      title: PropTypes.string.isRequired,
      description: PropTypes.string.isRequired,
      instructor: PropTypes.string.isRequired,
      category: PropTypes.string.isRequired,
      level: PropTypes.oneOf(['beginner', 'intermediate', 'advanced']).isRequired,
      rating: PropTypes.number,
      createdAt: PropTypes.string,
      updatedAt: PropTypes.string
    })
  ),
  /** Filtres à appliquer par défaut */
  filters: PropTypes.shape({
    category: PropTypes.string,
    level: PropTypes.string,
    status: PropTypes.string,
    search: PropTypes.string
  }),
  /** Mode d'affichage */
  viewMode: PropTypes.oneOf(['grid', 'list']),
  /** Afficher les contrôles de filtrage */
  showFilters: PropTypes.bool,
  /** Afficher la pagination */
  showPagination: PropTypes.bool,
  /** Afficher les boutons d'inscription */
  showEnrollButton: PropTypes.bool,
  /** Nombre d'éléments par page */
  itemsPerPage: PropTypes.number,
  /** Callback appelé lors de la sélection d'un cours */
  onCourseSelect: PropTypes.func,
  /** Callback appelé lors de l'inscription à un cours */
  onEnroll: PropTypes.func,
  /** Classes CSS additionnelles */
  className: PropTypes.string
};

export default CourseList;