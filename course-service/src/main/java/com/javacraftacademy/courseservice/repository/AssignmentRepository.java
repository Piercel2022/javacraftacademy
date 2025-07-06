package com.javacraftacademy.courseservice.repository;

import com.javacraftacademy.courseservice.model.entity.Assignment;
import com.javacraftacademy.courseservice.model.enums.AssignmentType;
import com.javacraftacademy.courseservice.model.enums.AssignmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface pour la gestion des devoirs et évaluations.
 * Cette interface étend JpaRepository pour fournir les opérations CRUD de base
 * et définit des méthodes personnalisées pour les requêtes spécifiques aux devoirs.
 * 
 * <p>Ce repository est central dans le système d'évaluation et sert à :
 * <ul>
 *   <li>Gérer les devoirs, quiz et examens</li>
 *   <li>Suivre les dates limites et statuts</li>
 *   <li>Organiser les évaluations par cours et leçon</li>
 *   <li>Générer des rapports sur les performances</li>
 * </ul>
 * 
 * <p>Relations avec l'application :
 * <ul>
 *   <li>Connecté étroitement à CourseRepository et LessonRepository</li>
 *   <li>Utilisé par AnswerRepository pour lier réponses et devoirs</li>
 *   <li>Intégré avec EnrollmentService pour vérifier les accès</li>
 *   <li>Connecté au système de notifications pour les rappels</li>
 *   <li>Utilisé par le service de rapports pour les statistiques</li>
 * </ul>
 * 
 * @author JavaCraft Academy
 * @version 1.0
 * @since 2024-01-01
 */
@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    /**
     * Trouve tous les devoirs d'un cours spécifique.
     * 
     * @param courseId L'identifiant unique du cours
     * @return Liste des devoirs du cours, ordonnés par date de création
     */
    @Query("SELECT a FROM Assignment a WHERE a.courseId = :courseId ORDER BY a.createdAt ASC")
    List<Assignment> findByCourseId(@Param("courseId") Long courseId);

    /**
     * Trouve tous les devoirs d'un cours avec pagination.
     * 
     * @param courseId L'identifiant unique du cours
     * @param pageable Information de pagination
     * @return Page des devoirs du cours
     */
    Page<Assignment> findByCourseId(Long courseId, Pageable pageable);

    /**
     * Trouve tous les devoirs d'une leçon spécifique.
     * 
     * @param lessonId L'identifiant unique de la leçon
     * @return Liste des devoirs de la leçon, ordonnés par ordre d'affichage
     */
    @Query("SELECT a FROM Assignment a WHERE a.lessonId = :lessonId ORDER BY a.displayOrder ASC")
    List<Assignment> findByLessonId(@Param("lessonId") Long lessonId);

    /**
     * Trouve tous les devoirs ayant un type spécifique.
     * 
     * @param type Le type de devoir (QUIZ, HOMEWORK, EXAM, etc.)
     * @return Liste des devoirs du type spécifié
     */
    List<Assignment> findByType(AssignmentType type);

    /**
     * Trouve tous les devoirs ayant un statut spécifique.
     * 
     * @param status Le statut du devoir (DRAFT, PUBLISHED, ARCHIVED)
     * @return Liste des devoirs avec le statut spécifié
     */
    List<Assignment> findByStatus(AssignmentStatus status);

    /**
     * Trouve tous les devoirs publiés d'un cours.
     * 
     * @param courseId L'identifiant du cours
     * @return Liste des devoirs publiés, ordonnés par date limite
     */
    @Query("SELECT a FROM Assignment a WHERE a.courseId = :courseId AND a.status = 'PUBLISHED' " +
           "ORDER BY a.dueDate ASC")
    List<Assignment> findPublishedByCourseId(@Param("courseId") Long courseId);

    /**
     * Trouve tous les devoirs avec une date limite dans une période donnée.
     * 
     * @param startDate Date de début de la période
     * @param endDate Date de fin de la période
     * @return Liste des devoirs dont la date limite est dans la période
     */
    @Query("SELECT a FROM Assignment a WHERE a.dueDate BETWEEN :startDate AND :endDate " +
           "ORDER BY a.dueDate ASC")
    List<Assignment> findByDueDateBetween(@Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate);

    /**
     * Trouve tous les devoirs arrivant à échéance prochainement.
     * 
     * @param beforeDate Date limite pour considérer un devoir comme "proche"
     * @return Liste des devoirs arrivant à échéance avant la date spécifiée
     */
    @Query("SELECT a FROM Assignment a WHERE a.dueDate <= :beforeDate AND a.status = 'PUBLISHED' " +
           "ORDER BY a.dueDate ASC")
    List<Assignment> findUpcomingDeadlines(@Param("beforeDate") LocalDateTime beforeDate);

    /**
     * Trouve tous les devoirs en retard (date limite dépassée).
     * 
     * @return Liste des devoirs en retard
     */
    @Query("SELECT a FROM Assignment a WHERE a.dueDate < CURRENT_TIMESTAMP AND a.status = 'PUBLISHED' " +
           "ORDER BY a.dueDate DESC")
    List<Assignment> findOverdueAssignments();

    /**
     * Compte le nombre total de devoirs dans un cours.
     * 
     * @param courseId L'identifiant du cours
     * @return Le nombre total de devoirs dans le cours
     */
    @Query("SELECT COUNT(a) FROM Assignment a WHERE a.courseId = :courseId")
    Long countByCourseId(@Param("courseId") Long courseId);

    /**
     * Compte le nombre de devoirs publiés dans un cours.
     * 
     * @param courseId L'identifiant du cours
     * @return Le nombre de devoirs publiés dans le cours
     */
    @Query("SELECT COUNT(a) FROM Assignment a WHERE a.courseId = :courseId AND a.status = 'PUBLISHED'")
    Long countPublishedByCourseId(@Param("courseId") Long courseId);

    /**
     * Trouve le prochain numéro d'ordre pour un cours.
     * 
     * @param courseId L'identifiant du cours
     * @return Le prochain numéro d'ordre disponible
     */
    @Query("SELECT COALESCE(MAX(a.displayOrder), 0) + 1 FROM Assignment a WHERE a.courseId = :courseId")
    Integer findNextDisplayOrderByCourseId(@Param("courseId") Long courseId);

    /**
     * Trouve les devoirs créés par un instructeur spécifique.
     * 
     * @param instructorId L'identifiant de l'instructeur
     * @return Liste des devoirs créés par l'instructeur
     */
    @Query("SELECT a FROM Assignment a WHERE a.createdBy = :instructorId ORDER BY a.createdAt DESC")
    List<Assignment> findByInstructorId(@Param("instructorId") Long instructorId);

    /**
     * Trouve les devoirs d'un type spécifique dans un cours.
     * 
     * @param courseId L'identifiant du cours
     * @param type Le type de devoir
     * @return Liste des devoirs du type spécifié dans le cours
     */
    @Query("SELECT a FROM Assignment a WHERE a.courseId = :courseId AND a.type = :type " +
           "ORDER BY a.displayOrder ASC")
    List<Assignment> findByCourseIdAndType(@Param("courseId") Long courseId, 
                                          @Param("type") AssignmentType type);

    /**
     * Calcule la note maximale possible pour un cours.
     * 
     * @param courseId L'identifiant du cours
     * @return La somme de tous les points possibles dans le cours
     */
    @Query("SELECT SUM(a.maxPoints) FROM Assignment a WHERE a.courseId = :courseId AND a.status = 'PUBLISHED'")
    Double calculateMaxPointsByCourse(@Param("courseId") Long courseId);

    /**
     * Trouve les devoirs avec une durée limitée (examens chronométrés).
     * 
     * @return Liste des devoirs avec une durée limitée
     */
    @Query("SELECT a FROM Assignment a WHERE a.timeLimit IS NOT NULL ORDER BY a.dueDate ASC")
    List<Assignment> findTimedAssignments();

    /**
     * Trouve les devoirs permettant les tentatives multiples.
     * 
     * @return Liste des devoirs permettant plusieurs tentatives
     */
    @Query("SELECT a FROM Assignment a WHERE a.maxAttempts > 1 OR a.maxAttempts IS NULL")
    List<Assignment> findMultipleAttemptsAllowed();

    /**
     * Recherche les devoirs par titre (recherche partielle, insensible à la casse).
     * 
     * @param title Le titre ou partie du titre à rechercher
     * @return Liste des devoirs correspondant à la recherche
     */
    @Query("SELECT a FROM Assignment a WHERE LOWER(a.title) LIKE LOWER(CONCAT('%', :title, '%')) " +
           "ORDER BY a.title ASC")
    List<Assignment> findByTitleContainingIgnoreCase(@Param("title") String title);

    /**
     * Trouve les devoirs archivés d'un cours.
     * 
     * @param courseId L'identifiant du cours
     * @return Liste des devoirs archivés
     */
    @Query("SELECT a FROM Assignment a WHERE a.courseId = :courseId AND a.status = 'ARCHIVED' " +
           "ORDER BY a.updatedAt DESC")
    List<Assignment> findArchivedByCourseId(@Param("courseId") Long courseId);

    /**
     * Trouve les devoirs modifiés récemment.
     * 
     * @param since Date à partir de laquelle chercher les modifications
     * @return Liste des devoirs modifiés depuis la date spécifiée
     */
    @Query("SELECT a FROM Assignment a WHERE a.updatedAt >= :since ORDER BY a.updatedAt DESC")
    List<Assignment> findRecentlyModified(@Param("since") LocalDateTime since);

    /**
     * Vérifie l'existence d'un devoir avec un titre spécifique dans un cours.
     * 
     * @param courseId L'identifiant du cours
     * @param title Le titre du devoir
     * @return true si un devoir avec ce titre existe déjà dans le cours
     */
    boolean existsByCourseIdAndTitle(Long courseId, String title);

    /**
     * Supprime tous les devoirs d'un cours.
     * Utilisé lors de la suppression d'un cours.
     * 
     * @param courseId L'identifiant du cours
     */
    void deleteByCourseId(Long courseId);

    /**
     * Supprime tous les devoirs d'une leçon.
     * Utilisé lors de la suppression d'une leçon.
     * 
     * @param lessonId L'identifiant de la leçon
     */
    void deleteByLessonId(Long lessonId);

    /**
     * Trouve les statistiques d'un devoir (nombre de soumissions, note moyenne, etc.).
     * 
     * @param assignmentId L'identifiant du devoir
     * @return Map contenant les statistiques du devoir
     */
    @Query("SELECT new map(" +
           "COUNT(ans.id) as submissionCount, " +
           "AVG(ans.score) as averageScore, " +
           "MAX(ans.score) as maxScore, " +
           "MIN(ans.score) as minScore" +
           ") FROM Assignment a LEFT JOIN Answer ans ON a.id = ans.assignmentId " +
           "WHERE a.id = :assignmentId GROUP BY a.id")
    Optional<java.util.Map<String, Object>> findAssignmentStatistics(@Param("assignmentId") Long assignmentId);

    /**
     * Trouve les devoirs disponibles pour un étudiant dans un cours.
     * (Devoirs publiés et non encore expirés)
     * 
     * @param courseId L'identifiant du cours
     * @param currentTime L'heure actuelle pour vérifier les dates limites
     * @return Liste des devoirs disponibles pour l'étudiant
     */
    @Query("SELECT a FROM Assignment a WHERE a.courseId = :courseId AND a.status = 'PUBLISHED' " +
           "AND (a.dueDate IS NULL OR a.dueDate > :currentTime) " +
           "ORDER BY a.displayOrder ASC")
    List<Assignment> findAvailableForStudent(@Param("courseId") Long courseId, 
                                           @Param("currentTime") LocalDateTime currentTime);
}