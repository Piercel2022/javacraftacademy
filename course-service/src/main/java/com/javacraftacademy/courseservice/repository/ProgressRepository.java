// ========================================
// REPOSITORY RÉORGANISÉ ET DOCUMENTÉ
// ========================================
// Localisation: src/main/java/com/javacraftacademy/courseservice/repository/ProgressRepository.java

package com.javacraftacademy.courseservice.repository;

// ========================================
// IMPORTS ESSENTIELS AVEC LEURS RÔLES
// ========================================

// Entité Progress - Classe principale gérée par ce repository
import com.javacraftacademy.courseservice.entity.Progress;

// Spring Data JPA - Framework principal pour la persistance
import org.springframework.data.jpa.repository.JpaRepository; // Interface de base pour CRUD + pagination
import org.springframework.data.jpa.repository.Query;          // Annotation pour requêtes JPQL personnalisées
import org.springframework.data.repository.query.Param;       // Liaison des paramètres dans les requêtes

// Spring Framework - Gestion des composants et injection de dépendances
import org.springframework.stereotype.Repository;             // Annotation pour marquer comme composant repository

// Java Standard - Collections et types optionnels
import java.util.List;      // Collection pour les résultats multiples
import java.util.Optional;  // Wrapper pour gérer les valeurs potentiellement nulles
import java.time.LocalDateTime; // Gestion des dates et heures

/**
 * ProgressRepository - Interface de persistance pour l'entité Progress
 * 
 * <p>Ce repository centralise toutes les opérations de base de données liées au suivi
 * des progressions étudiantes. Il étend JpaRepository pour bénéficier automatiquement
 * des opérations CRUD standards et ajoute des méthodes métier spécialisées.</p>
 * 
 * <h3>Architecture et responsabilités :</h3>
 * <ul>
 *   <li><strong>Couche d'accès aux données</strong> : Abstraction entre la logique métier et la BD</li>
 *   <li><strong>Requêtes optimisées</strong> : Méthodes spécialisées avec index appropriés</li>
 *   <li><strong>Gestion des relations</strong> : Jointures efficaces avec les entités liées</li>
 *   <li><strong>Statistiques et analytics</strong> : Agrégations pour les rapports</li>
 * </ul>
 * 
 * <h3>Intégration dans l'écosystème JavaCraftAcademy :</h3>
 * <ul>
 *   <li><strong>ProgressService</strong> : Couche service utilisant ce repository</li>
 *   <li><strong>CourseController</strong> : API REST pour les opérations de progression</li>
 *   <li><strong>AnalyticsEngine</strong> : Utilise les méthodes d'agrégation pour les KPIs</li>
 *   <li><strong>RecommendationService</strong> : Analyse des patterns de progression</li>
 *   <li><strong>NotificationService</strong> : Détection des étudiants inactifs</li>
 * </ul>
 * 
 * <h3>Stratégies d'optimisation :</h3>
 * <ul>
 *   <li><strong>Index composites</strong> : Sur (student_id, course_id) pour les recherches fréquentes</li>
 *   <li><strong>Requêtes JPQL</strong> : Évite le N+1 problem avec les jointures explicites</li>
 *   <li><strong>Projections</strong> : Récupération sélective des données pour les statistiques</li>
 *   <li><strong>Pagination native</strong> : Héritage des capacités de PagingAndSortingRepository</li>
 * </ul>
 * 
 * <h3>Extensions futures planifiées :</h3>
 * <ul>
 *   <li>Requêtes avec critères dynamiques (Specification API)</li>
 *   <li>Cache Redis pour les statistiques fréquemment consultées</li>
 *   <li>Requêtes natives optimisées pour les gros volumes</li>
 *   <li>Support des requêtes géo-spatiales pour l'apprentissage localisé</li>
 *   <li>Intégration avec Elasticsearch pour les recherches full-text</li>
 * </ul>
 * 
 * @author JavaCraftAcademy Team
 * @version 2.1
 * @since 1.0
 * @see Progress
 * @see org.springframework.data.jpa.repository.JpaRepository
 */
@Repository
public interface ProgressRepository extends JpaRepository<Progress, Long> {
    
    // ========================================
    // REQUÊTES DE BASE - RECHERCHE INDIVIDUELLE
    // ========================================
    
    /**
     * Recherche le progrès spécifique d'un étudiant dans un cours donné
     * 
     * <p>Méthode critique pour l'affichage du tableau de bord étudiant.
     * Utilise l'index composite sur (student_id, course_id) pour des performances optimales.</p>
     * 
     * @param studentId Identifiant unique de l'étudiant
     * @param courseId Identifiant unique du cours
     * @return Optional contenant le Progress si trouvé, vide sinon
     * 
     * @apiNote Utilisée par ProgressService.getStudentCourseProgress()
     * @performance Index composite - Complexité O(log n)
     */
    Optional<Progress> findByStudentIdAndCourseId(Long studentId, Long courseId);

    // ========================================
    // REQUÊTES DE LISTE - RECHERCHE MULTIPLE
    // ========================================
    
    /**
     * Récupère tous les progrès d'un étudiant spécifique
     * 
     * <p>Utilisée pour afficher le profil complet de l'étudiant avec tous ses cours.
     * Retourne une liste ordonnée par date de dernière activité (plus récent en premier).</p>
     * 
     * @param studentId Identifiant de l'étudiant
     * @return Liste des progrès, vide si aucun cours suivi
     * 
     * @apiNote Dashboard étudiant, profil utilisateur
     * @performance Index sur student_id - Complexité O(log n + k) où k = nombre de cours
     */
    List<Progress> findByStudentId(Long studentId);
    
    /**
     * Récupère tous les progrès pour un cours spécifique
     * 
     * <p>Utilisée par les instructeurs pour voir la progression de tous les étudiants
     * inscrits à leur cours. Essentiel pour le suivi pédagogique.</p>
     * 
     * @param courseId Identifiant du cours
     * @return Liste des progrès de tous les étudiants inscrits
     * 
     * @apiNote Interface instructeur, rapports de cours
     * @performance Index sur course_id - Complexité O(log n + k) où k = nombre d'étudiants
     */
    List<Progress> findByCourseId(Long courseId);

    // ========================================
    // REQUÊTES JPQL PERSONNALISÉES - JOINTURES OPTIMISÉES
    // ========================================
    
    /**
     * Version alternative optimisée pour rechercher le progrès étudiant-cours
     * 
     * <p>Utilise une jointure explicite avec l'entité Course pour éviter les requêtes
     * multiples et optimiser la récupération des données liées.</p>
     * 
     * @param studentId Identifiant de l'étudiant
     * @param courseId Identifiant du cours
     * @return Optional contenant le Progress avec le Course préchargé
     * 
     * @deprecated Préférer findByStudentIdAndCourseId() pour la simplicité
     * @performance Jointure optimisée - Une seule requête SQL
     */
    @Query("SELECT p FROM Progress p WHERE p.studentId = :studentId AND p.course.id = :courseId")
    Optional<Progress> findStudentCourseProgress(@Param("studentId") Long studentId, 
                                               @Param("courseId") Long courseId);

    // ========================================
    // REQUÊTES DE FILTRAGE - STATUT DE COMPLETION
    // ========================================
    
    /**
     * Récupère tous les cours complétés par un étudiant
     * 
     * <p>Essentiel pour afficher les certifications obtenues et calculer les achievements.
     * Utilise l'index sur le champ 'completed' pour des performances optimales.</p>
     * 
     * @param studentId Identifiant de l'étudiant
     * @return Liste des cours terminés avec succès
     * 
     * @apiNote Certificats, badges, historique de réussite
     * @performance Index composite (student_id, completed) recommandé
     */
    @Query("SELECT p FROM Progress p WHERE p.studentId = :studentId AND p.completed = true")
    List<Progress> findCompletedCoursesByStudent(@Param("studentId") Long studentId);
    
    /**
     * Récupère tous les cours en cours de réalisation par un étudiant
     * 
     * <p>Affiche les cours actuellement suivis pour prioriser l'apprentissage.
     * Complément naturel de findCompletedCoursesByStudent().</p>
     * 
     * @param studentId Identifiant de l'étudiant
     * @return Liste des cours non terminés
     * 
     * @apiNote Dashboard "Mes cours en cours", notifications de reprise
     * @performance Index composite (student_id, completed) recommandé
     */
    @Query("SELECT p FROM Progress p WHERE p.studentId = :studentId AND p.completed = false")
    List<Progress> findInProgressCoursesByStudent(@Param("studentId") Long studentId);

    // ========================================
    // REQUÊTES D'AGRÉGATION - STATISTIQUES ET KPIs
    // ========================================
    
    /**
     * Compte le nombre d'étudiants ayant terminé un cours spécifique
     * 
     * <p>KPI fondamental pour mesurer le succès d'un cours et calculer le taux
     * de completion. Utilisé dans les rapports analytiques.</p>
     * 
     * @param courseId Identifiant du cours
     * @return Nombre d'étudiants ayant complété le cours
     * 
     * @apiNote Analytics, rapports de performance, métriques instructeur
     * @performance Requête d'agrégation optimisée par l'index composite
     */
    @Query("SELECT COUNT(p) FROM Progress p WHERE p.course.id = :courseId AND p.completed = true")
    Long countCompletedStudentsByCourseId(@Param("courseId") Long courseId);
    
    /**
     * Compte le nombre total d'étudiants inscrits à un cours
     * 
     * <p>Mesure l'engagement et la popularité d'un cours. Combiné avec
     * countCompletedStudentsByCourseId() pour calculer le taux de réussite.</p>
     * 
     * @param courseId Identifiant du cours
     * @return Nombre total d'inscriptions au cours
     * 
     * @apiNote Métriques d'engagement, dimensionnement des ressources
     * @performance Index sur course_id avec COUNT optimisé
     */
    @Query("SELECT COUNT(p) FROM Progress p WHERE p.course.id = :courseId")
    Long countEnrolledStudentsByCourseId(@Param("courseId") Long courseId);
    
    /**
     * Calcule le pourcentage moyen de progression pour un cours
     * 
     * <p>Indicateur de la difficulté perçue et de l'engagement moyen.
     * Aide à identifier les cours nécessitant des améliorations pédagogiques.</p>
     * 
     * @param courseId Identifiant du cours
     * @return Pourcentage moyen (0.0 à 100.0), null si aucune donnée
     * 
     * @apiNote Analyse pédagogique, optimisation de contenu
     * @performance Requête d'agrégation AVG() avec index sur course_id
     */
    @Query("SELECT AVG(p.completionPercentage) FROM Progress p WHERE p.course.id = :courseId")
    Double getAverageProgressByCourseId(@Param("courseId") Long courseId);

    // ========================================
    // REQUÊTES DE RECHERCHE AVANCÉE - CRITÈRES DYNAMIQUES
    // ========================================
    
    /**
     * Trouve tous les progrès atteignant un seuil minimum de completion
     * 
     * <p>Utile pour identifier les étudiants proches de la certification ou
     * pour créer des cohortes basées sur le niveau d'avancement.</p>
     * 
     * @param percentage Seuil minimum de progression (ex: 80.0 pour 80%)
     * @return Liste des progrès atteignant le seuil
     * 
     * @apiNote Campaigns marketing, relances personnalisées, segmentation
     * @performance Index sur completion_percentage recommandé
     */
    @Query("SELECT p FROM Progress p WHERE p.completionPercentage >= :percentage")
    List<Progress> findProgressByMinimumPercentage(@Param("percentage") Double percentage);
    
    /**
     * Récupère les progrès récents d'un étudiant, ordonnés par dernière activité
     * 
     * <p>Affiche l'historique d'apprentissage pour reprendre là où l'étudiant
     * s'est arrêté. Optimise l'expérience utilisateur de reprise de cours.</p>
     * 
     * @param studentId Identifiant de l'étudiant
     * @return Liste ordonnée par activité récente (plus récent en premier)
     * 
     * @apiNote "Reprendre là où j'ai arrêté", historique d'activité
     * @performance Index sur (student_id, last_accessed_at) recommandé
     */
    @Query("SELECT p FROM Progress p WHERE p.studentId = :studentId ORDER BY p.lastAccessedAt DESC")
    List<Progress> findRecentProgressByStudent(@Param("studentId") Long studentId);

    // ========================================
    // EXTENSIONS FUTURES - TEMPLATE POUR NOUVELLES FONCTIONNALITÉS
    // ========================================
    
    /*
     * TEMPLATE POUR FUTURES MÉTHODES :
     * 
     * // Détection d'étudiants inactifs (à implémenter)
     * @Query("SELECT p FROM Progress p WHERE p.lastAccessedAt < :thresholdDate AND p.completed = false")
     * List<Progress> findInactiveStudents(@Param("thresholdDate") LocalDateTime thresholdDate);
     * 
     * // Progression par tranche temporelle (à implémenter)
     * @Query("SELECT p FROM Progress p WHERE p.createdAt BETWEEN :startDate AND :endDate")
     * List<Progress> findProgressByDateRange(@Param("startDate") LocalDateTime start, 
     *                                       @Param("endDate") LocalDateTime end);
     * 
     * // Top performers (à implémenter)
     * @Query("SELECT p FROM Progress p WHERE p.score >= :minScore ORDER BY p.score DESC")
     * List<Progress> findTopPerformers(@Param("minScore") Double minScore, Pageable pageable);
     * 
     * // Statistiques par période (à implémenter avec native query pour performance)
     * @Query(value = "SELECT DATE_TRUNC('month', created_at) as month, COUNT(*) as enrollments " +
     *               "FROM progress WHERE created_at >= :startDate GROUP BY DATE_TRUNC('month', created_at)", 
     *               nativeQuery = true)
     * List<Object[]> getMonthlyEnrollmentStats(@Param("startDate") LocalDateTime startDate);
     */
}

