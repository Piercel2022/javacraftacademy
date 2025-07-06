package com.javacraftacademy.courseservice.repository;

import com.javacraftacademy.courseservice.model.entity.Lesson;
import com.javacraftacademy.courseservice.model.enums.LessonType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository pour la gestion des opérations de base de données liées aux leçons.
 * 
 * Cette interface étend JpaRepository et JpaSpecificationExecutor pour fournir :
 * - Les opérations CRUD de base pour les entités Lesson
 * - Des méthodes de requête personnalisées pour la gestion des leçons
 * - Support des spécifications JPA pour les requêtes dynamiques
 * - Gestion de l'ordre et de la hiérarchie des leçons
 * 
 * Relations avec l'application :
 * - Utilisé par LessonService et LessonServiceImpl pour l'accès aux données
 * - Intégré avec CourseService pour la gestion du contenu des cours
 * - Référencé dans les contrôleurs de leçons via la couche service
 * - Supporte la progression des étudiants et le suivi des leçons
 * - Fournit des métriques sur le contenu des cours
 * 
 * Fonctionnalités spécifiques :
 * - Gestion de l'ordre des leçons dans un cours
 * - Support des différents types de leçons (vidéo, texte, quiz, etc.)
 * - Requêtes optimisées pour l'affichage séquentiel des leçons
 * - Gestion de la durée totale des cours
 * - Support des leçons gratuites vs payantes
 * 
 * @author JavaCraft Academy
 * @version 1.0
 * @since 1.0
 */
@Repository
@Transactional(readOnly = true)
public interface LessonRepository extends JpaRepository<Lesson, Long>, JpaSpecificationExecutor<Lesson> {
    
    // ======================== RECHERCHE PAR COURS ========================
    
    /**
     * Récupère toutes les leçons d'un cours, triées par ordre séquentiel.
     * 
     * @param courseId L'identifiant du cours
     * @return Liste des leçons triées par ordre
     */
    @Query("SELECT l FROM Lesson l WHERE l.courseId = :courseId ORDER BY l.orderIndex ASC")
    List<Lesson> findByCourseIdOrderByOrderIndexAsc(@Param("courseId") Long courseId);
    
    /**
     * Récupère les leçons d'un cours avec pagination.
     * 
     * @param courseId L'identifiant du cours
     * @param pageable Informations de pagination
     * @return Page des leçons
     */
    Page<Lesson> findByCourseIdOrderByOrderIndexAsc(Long courseId, Pageable pageable);
    
    /**
     * Récupère les leçons d'un cours par type.
     * 
     * @param courseId L'identifiant du cours
     * @param lessonType Le type de leçon
     * @return Liste des leçons du type spécifié
     */
    List<Lesson> findByCourseIdAndLessonTypeOrderByOrderIndexAsc(Long courseId, LessonType lessonType);
    
    /**
     * Récupère les leçons gratuites d'un cours.
     * 
     * @param courseId L'identifiant du cours
     * @return Liste des leçons gratuites
     */
    @Query("SELECT l FROM Lesson l WHERE l.courseId = :courseId AND l.isFree = true ORDER BY l.orderIndex ASC")
    List<Lesson> findFreeLessonsByCourseId(@Param("courseId") Long courseId);
    
    // ======================== RECHERCHE PAR POSITION ========================
    
    /**
     * Trouve une leçon par son cours et sa position.
     * 
     * @param courseId L'identifiant du cours
     * @param orderIndex L'index d'ordre de la leçon
     * @return Optional contenant la leçon si trouvée
     */
    Optional<Lesson> findByCourseIdAndOrderIndex(Long courseId, Integer orderIndex);
    
    /**
     * Trouve the première leçon d'un cours.
     * 
     * @param courseId L'identifiant du cours
     * @return Optional contenant la première leçon
     */
    @Query("SELECT l FROM Lesson l WHERE l.courseId = :courseId ORDER BY l.orderIndex ASC LIMIT 1")
    Optional<Lesson> findFirstLessonByCourseId(@Param("courseId") Long courseId);
    
    /**
     * Trouve la dernière leçon d'un cours.
     * 
     * @param courseId L'identifiant du cours
     * @return Optional contenant la dernière leçon
     */
    @Query("SELECT l FROM Lesson l WHERE l.courseId = :courseId ORDER BY l.orderIndex DESC LIMIT 1")
    Optional<Lesson> findLastLessonByCourseId(@Param("courseId") Long courseId);
    
    /**
     * Trouve la leçon suivante dans un cours.
     * 
     * @param courseId L'identifiant du cours
     * @param currentOrderIndex L'index actuel
     * @return Optional contenant la leçon suivante
     */
    @Query("SELECT l FROM Lesson l WHERE l.courseId = :courseId AND l.orderIndex > :currentOrderIndex ORDER BY l.orderIndex ASC LIMIT 1")
    Optional<Lesson> findNextLesson(@Param("courseId") Long courseId, @Param("currentOrderIndex") Integer currentOrderIndex);
    
    /**
     * Trouve la leçon précédente dans un cours.
     * 
     * @param courseId L'identifiant du cours
     * @param currentOrderIndex L'index actuel
     * @return Optional contenant la leçon précédente
     */
    @Query("SELECT l FROM Lesson l WHERE l.courseId = :courseId AND l.orderIndex < :currentOrderIndex ORDER BY l.orderIndex DESC LIMIT 1")
    Optional<Lesson> findPreviousLesson(@Param("courseId") Long courseId, @Param("currentOrderIndex") Integer currentOrderIndex);
    
    // ======================== STATISTIQUES ET MÉTRIQUES ========================
    
    /**
     * Compte le nombre total de leçons dans un cours.
     * 
     * @param courseId L'identifiant du cours
     * @return Nombre de leçons
     */
    @Query("SELECT COUNT(l) FROM Lesson l WHERE l.courseId = :courseId")
    Long countLessonsByCourseId(@Param("courseId") Long courseId);
    
    /**
     * Compte les leçons par type dans un cours.
     * 
     * @param courseId L'identifiant du cours
     * @param lessonType Le type de leçon
     * @return Nombre de leçons du type spécifié
     */
    Long countByCourseIdAndLessonType(Long courseId, LessonType lessonType);
    
    /**
     * Calcule la durée totale des leçons d'un cours (en minutes).
     * 
     * @param courseId L'identifiant du cours
     * @return Durée totale en minutes
     */
    @Query("SELECT COALESCE(SUM(l.durationMinutes), 0) FROM Lesson l WHERE l.courseId = :courseId")
    Long calculateTotalDurationByCourseId(@Param("courseId") Long courseId);
    
    /**
     * Calcule la durée des leçons gratuites d'un cours.
     * 
     * @param courseId L'identifiant du cours
     * @return Durée des leçons gratuites en minutes
     */
    @Query("SELECT COALESCE(SUM(l.durationMinutes), 0) FROM Lesson l WHERE l.courseId = :courseId AND l.isFree = true")
    Long calculateFreeLessonsDurationByCourseId(@Param("courseId") Long courseId);
    
    // ======================== GESTION DE L'ORDRE ========================
    
    /**
     * Trouve le prochain index d'ordre disponible pour un cours.
     * 
     * @param courseId L'identifiant du cours
     * @return Le prochain index d'ordre
     */
    @Query("SELECT COALESCE(MAX(l.orderIndex), 0) + 1 FROM Lesson l WHERE l.courseId = :courseId")
    Integer findNextOrderIndexByCourseId(@Param("courseId") Long courseId);
    
    /**
     * Met à jour l'ordre des leçons après une position donnée.
     * 
     * @param courseId L'identifiant du cours
     * @param fromOrderIndex L'index à partir duquel décaler
     * @param increment Le nombre de positions à ajouter
     */
    @Modifying
    @Transactional
    @Query("UPDATE Lesson l SET l.orderIndex = l.orderIndex + :increment WHERE l.courseId = :courseId AND l.orderIndex >= :fromOrderIndex")
    void updateOrderIndexFromPosition(@Param("courseId") Long courseId, 
                                     @Param("fromOrderIndex") Integer fromOrderIndex, 
                                     @Param("increment") Integer increment);
    
    /**
     * Réorganise l'ordre des leçons d'un cours.
     * 
     * @param courseId L'identifiant du cours
     */
    @Modifying
    @Transactional
    @Query("UPDATE Lesson l SET l.orderIndex = " +
           "(SELECT ROW_NUMBER() OVER (ORDER BY l2.orderIndex ASC) FROM Lesson l2 WHERE l2.courseId = :courseId AND l2.id = l.id) " +
           "WHERE l.courseId = :courseId")
    void reorderLessonsByCourseId(@Param("courseId") Long courseId);
    
    // ======================== RECHERCHE AVANCÉE ========================
    
    /**
     * Recherche les leçons par titre ou description.
     * 
     * @param searchTerm Le terme de recherche
     * @param pageable Informations de pagination
     * @return Page des leçons correspondant au critère
     */
    @Query("SELECT l FROM Lesson l WHERE " +
           "LOWER(l.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(l.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Lesson> searchLessonsByTitleOrDescription(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    /**
     * Recherche les leçons dans un cours par titre.
     * 
     * @param courseId L'identifiant du cours
     * @param searchTerm Le terme de recherche
     * @return Liste des leçons correspondantes
     */
    @Query("SELECT l FROM Lesson l WHERE l.courseId = :courseId AND " +
           "LOWER(l.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "ORDER BY l.orderIndex ASC")
    List<Lesson> searchLessonsByCourseIdAndTitle(@Param("courseId") Long courseId, 
                                                @Param("searchTerm") String searchTerm);
    
    // ======================== GESTION DU STATUT ========================
    
    /**
     * Trouve les leçons publiées d'un cours.
     * 
     * @param courseId L'identifiant du cours
     * @return Liste des leçons publiées
     */
    @Query("SELECT l FROM Lesson l WHERE l.courseId = :courseId AND l.isPublished = true ORDER BY l.orderIndex ASC")
    List<Lesson> findPublishedLessonsByCourseId(@Param("courseId") Long courseId);
    
    /**
     * Trouve les leçons en brouillon d'un cours.
     * 
     * @param courseId L'identifiant du cours
     * @return Liste des leçons en brouillon
     */
    @Query("SELECT l FROM Lesson l WHERE l.courseId = :courseId AND l.isPublished = false ORDER BY l.orderIndex ASC")
    List<Lesson> findDraftLessonsByCourseId(@Param("courseId") Long courseId);
    
    /**
     * Met à jour le statut de publication d'une leçon.
     * 
     * @param lessonId L'identifiant de la leçon
     * @param isPublished Le nouveau statut de publication
     * @param publishedAt La date de publication
     */
    @Modifying
    @Transactional
    @Query("UPDATE Lesson l SET l.isPublished = :isPublished, l.publishedAt = :publishedAt WHERE l.id = :lessonId")
    void updatePublishStatus(@Param("lessonId") Long lessonId, 
                            @Param("isPublished") Boolean isPublished, 
                            @Param("publishedAt") LocalDateTime publishedAt);
    
    // ======================== GESTION DES DATES ========================
    
    /**
     * Trouve les leçons créées après une date donnée.
     * 
     * @param courseId L'identifiant du cours
     * @param date La date de référence
     * @return Liste des leçons créées après la date
     */
    @Query("SELECT l FROM Lesson l WHERE l.courseId = :courseId AND l.createdAt > :date ORDER BY l.createdAt DESC")
    List<Lesson> findLessonsCreatedAfter(@Param("courseId") Long courseId, @Param("date") LocalDateTime date);
    
    /**
     * Trouve les leçons modifiées récemment.
     * 
     * @param courseId L'identifiant du cours
     * @param date La date de référence
     * @return Liste des leçons modifiées après la date
     */
    @Query("SELECT l FROM Lesson l WHERE l.courseId = :courseId AND l.updatedAt > :date ORDER BY l.updatedAt DESC")
    List<Lesson> findLessonsUpdatedAfter(@Param("courseId") Long courseId, @Param("date") LocalDateTime date);
    
    // ======================== VALIDATION ET VÉRIFICATION ========================
    
    /**
     * Vérifie si une leçon existe dans un cours.
     * 
     * @param courseId L'identifiant du cours
     * @param lessonId L'identifiant de la leçon
     * @return true si la leçon existe dans le cours
     */
    @Query("SELECT COUNT(l) > 0 FROM Lesson l WHERE l.courseId = :courseId AND l.id = :lessonId")
    boolean existsByCourseIdAndLessonId(@Param("courseId") Long courseId, @Param("lessonId") Long lessonId);
    
    /**
     * Vérifie si un index d'ordre est déjà utilisé dans un cours.
     * 
     * @param courseId L'identifiant du cours
     * @param orderIndex L'index d'ordre à vérifier
     * @return true si l'index est déjà utilisé
     */
    boolean existsByCourseIdAndOrderIndex(Long courseId, Integer orderIndex);
    
    /**
     * Vérifie si un cours a des leçons.
     * 
     * @param courseId L'identifiant du cours
     * @return true si le cours a des leçons
     */
    boolean existsByCourseId(Long courseId);
    
    // ======================== SUPPRESSION ET NETTOYAGE ========================
    
    /**
     * Supprime toutes les leçons d'un cours.
     * 
     * @param courseId L'identifiant du cours
     * @return Nombre de leçons supprimées
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM Lesson l WHERE l.courseId = :courseId")
    int deleteAllByCourseId(@Param("courseId") Long courseId);
    
    /**
     * Supprime les leçons en brouillon anciennes.
     * 
     * @param date La date limite
     * @return Nombre de leçons supprimées
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM Lesson l WHERE l.isPublished = false AND l.createdAt < :date")
    int deleteOldDraftLessons(@Param("date") LocalDateTime date);
    
    // ======================== MÉTHODES CUSTOM POUR LA PROGRESSION ========================
    
    /**
     * Trouve les leçons accessibles pour un utilisateur (leçons gratuites ou cours acheté).
     * Cette méthode sera utilisée en conjonction avec les services de progression.
     * 
     * @param courseId L'identifiant du cours
     * @param includeOnlyFree true pour inclure seulement les leçons gratuites
     * @return Liste des leçons accessibles
     */
    @Query("SELECT l FROM Lesson l WHERE l.courseId = :courseId AND " +
           "(:includeOnlyFree = false OR l.isFree = true) AND l.isPublished = true " +
           "ORDER BY l.orderIndex ASC")
    List<Lesson> findAccessibleLessons(@Param("courseId") Long courseId, 
                                      @Param("includeOnlyFree") boolean includeOnlyFree);
}