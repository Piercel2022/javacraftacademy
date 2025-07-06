package com.javacraftacademy.courseservice.repository;

import com.javacraftacademy.courseservice.model.entity.Answer;
import com.javacraftacademy.courseservice.model.enums.AnswerStatus;
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
 * Repository interface pour la gestion des réponses aux devoirs.
 * Cette interface étend JpaRepository pour fournir les opérations CRUD de base
 * et définit des méthodes personnalisées pour les requêtes spécifiques aux réponses.
 * 
 * <p>Ce repository est utilisé par AnswerService pour :
 * <ul>
 *   <li>Gérer les réponses soumises par les étudiants</li>
 *   <li>Suivre le statut des corrections</li>
 *   <li>Générer des rapports sur les performances</li>
 *   <li>Filtrer les réponses par critères multiples</li>
 * </ul>
 * 
 * <p>Relations avec l'application :
 * <ul>
 *   <li>Connecté à AssignmentRepository via l'ID du devoir</li>
 *   <li>Utilisé par EnrollmentService pour le suivi des progrès</li>
 *   <li>Intégré avec NotificationService pour les alertes de correction</li>
 *   <li>Connecté au UserService via l'ID de l'étudiant</li>
 * </ul>
 * 
 * @author JavaCraft Academy
 * @version 1.0
 * @since 2024-01-01
 */
@Repository
public interface AnswerRepository extends JpaRepository<Answer, Long> {

    /**
     * Trouve toutes les réponses soumises par un étudiant spécifique.
     * 
     * @param studentId L'identifiant unique de l'étudiant
     * @return Liste des réponses de l'étudiant, ordonnées par date de soumission décroissante
     */
    @Query("SELECT a FROM Answer a WHERE a.studentId = :studentId ORDER BY a.submittedAt DESC")
    List<Answer> findByStudentId(@Param("studentId") Long studentId);

    /**
     * Trouve toutes les réponses pour un devoir spécifique.
     * 
     * @param assignmentId L'identifiant unique du devoir
     * @return Liste des réponses pour le devoir
     */
    List<Answer> findByAssignmentId(Long assignmentId);

    /**
     * Trouve toutes les réponses pour un devoir avec pagination.
     * 
     * @param assignmentId L'identifiant unique du devoir
     * @param pageable Information de pagination
     * @return Page des réponses pour le devoir
     */
    Page<Answer> findByAssignmentId(Long assignmentId, Pageable pageable);

    /**
     * Trouve toutes les réponses ayant un statut spécifique.
     * 
     * @param status Le statut des réponses à rechercher
     * @return Liste des réponses avec le statut spécifié
     */
    List<Answer> findByStatus(AnswerStatus status);

    /**
     * Trouve toutes les réponses en attente de correction avec pagination.
     * 
     * @param pageable Information de pagination
     * @return Page des réponses en attente de correction, ordonnées par date de soumission
     */
    @Query("SELECT a FROM Answer a WHERE a.status = 'PENDING' ORDER BY a.submittedAt ASC")
    Page<Answer> findPendingAnswers(Pageable pageable);

    /**
     * Trouve la réponse d'un étudiant pour un devoir spécifique.
     * 
     * @param studentId L'identifiant de l'étudiant
     * @param assignmentId L'identifiant du devoir
     * @return Optional contenant la réponse si elle existe
     */
    Optional<Answer> findByStudentIdAndAssignmentId(Long studentId, Long assignmentId);

    /**
     * Compte le nombre de réponses soumises pour un devoir.
     * 
     * @param assignmentId L'identifiant du devoir
     * @return Le nombre total de réponses soumises
     */
    @Query("SELECT COUNT(a) FROM Answer a WHERE a.assignmentId = :assignmentId")
    Long countSubmissionsByAssignment(@Param("assignmentId") Long assignmentId);

    /**
     * Compte le nombre de réponses avec un statut spécifique pour un devoir.
     * 
     * @param assignmentId L'identifiant du devoir
     * @param status Le statut à compter
     * @return Le nombre de réponses avec le statut spécifié
     */
    @Query("SELECT COUNT(a) FROM Answer a WHERE a.assignmentId = :assignmentId AND a.status = :status")
    Long countByAssignmentIdAndStatus(@Param("assignmentId") Long assignmentId, @Param("status") AnswerStatus status);

    /**
     * Trouve toutes les réponses soumises après une date donnée.
     * 
     * @param dateTime La date limite
     * @return Liste des réponses soumises après la date spécifiée
     */
    @Query("SELECT a FROM Answer a WHERE a.submittedAt > :dateTime ORDER BY a.submittedAt DESC")
    List<Answer> findSubmittedAfter(@Param("dateTime") LocalDateTime dateTime);

    /**
     * Trouve toutes les réponses d'un étudiant pour un cours spécifique.
     * 
     * @param studentId L'identifiant de l'étudiant
     * @param courseId L'identifiant du cours
     * @return Liste des réponses de l'étudiant pour le cours
     */
    @Query("SELECT a FROM Answer a JOIN Assignment ass ON a.assignmentId = ass.id " +
           "WHERE a.studentId = :studentId AND ass.courseId = :courseId " +
           "ORDER BY a.submittedAt DESC")
    List<Answer> findByStudentIdAndCourseId(@Param("studentId") Long studentId, 
                                           @Param("courseId") Long courseId);

    /**
     * Calcule la note moyenne d'un étudiant pour un cours.
     * 
     * @param studentId L'identifiant de l'étudiant
     * @param courseId L'identifiant du cours
     * @return La note moyenne ou null si aucune note n'est disponible
     */
    @Query("SELECT AVG(a.score) FROM Answer a JOIN Assignment ass ON a.assignmentId = ass.id " +
           "WHERE a.studentId = :studentId AND ass.courseId = :courseId AND a.score IS NOT NULL")
    Double calculateAverageScoreByStudentAndCourse(@Param("studentId") Long studentId, 
                                                  @Param("courseId") Long courseId);

    /**
     * Trouve toutes les réponses nécessitant une attention particulière.
     * (Score faible ou soumission tardive)
     * 
     * @param minScore Score minimum considéré comme acceptable
     * @return Liste des réponses nécessitant une attention
     */
    @Query("SELECT a FROM Answer a JOIN Assignment ass ON a.assignmentId = ass.id " +
           "WHERE (a.score < :minScore AND a.score IS NOT NULL) " +
           "OR a.submittedAt > ass.dueDate " +
           "ORDER BY a.submittedAt DESC")
    List<Answer> findAnswersNeedingAttention(@Param("minScore") Double minScore);

    /**
     * Trouve toutes les réponses corrigées par un correcteur spécifique.
     * 
     * @param graderId L'identifiant du correcteur
     * @return Liste des réponses corrigées par le correcteur
     */
    @Query("SELECT a FROM Answer a WHERE a.gradedBy = :graderId ORDER BY a.gradedAt DESC")
    List<Answer> findByGraderId(@Param("graderId") Long graderId);

    /**
     * Trouve les statistiques de correction d'un correcteur.
     * 
     * @param graderId L'identifiant du correcteur
     * @param startDate Date de début de la période
     * @param endDate Date de fin de la période
     * @return Nombre de réponses corrigées dans la période
     */
    @Query("SELECT COUNT(a) FROM Answer a WHERE a.gradedBy = :graderId " +
           "AND a.gradedAt BETWEEN :startDate AND :endDate")
    Long countGradedByGraderInPeriod(@Param("graderId") Long graderId,
                                    @Param("startDate") LocalDateTime startDate,
                                    @Param("endDate") LocalDateTime endDate);

    /**
     * Supprime toutes les réponses associées à un devoir.
     * Utilisé lors de la suppression d'un devoir.
     * 
     * @param assignmentId L'identifiant du devoir
     */
    void deleteByAssignmentId(Long assignmentId);

    /**
     * Vérifie si un étudiant a déjà soumis une réponse pour un devoir.
     * 
     * @param studentId L'identifiant de l'étudiant
     * @param assignmentId L'identifiant du devoir
     * @return true si l'étudiant a déjà soumis une réponse
     */
    boolean existsByStudentIdAndAssignmentId(Long studentId, Long assignmentId);

    /**
     * Trouve toutes les réponses en retard (soumises après la date limite).
     * 
     * @return Liste des réponses en retard
     */
    @Query("SELECT a FROM Answer a JOIN Assignment ass ON a.assignmentId = ass.id " +
           "WHERE a.submittedAt > ass.dueDate ORDER BY a.submittedAt DESC")
    List<Answer> findLateSubmissions();

    /**
     * Trouve les meilleures réponses pour un devoir (top scores).
     * 
     * @param assignmentId L'identifiant du devoir
     * @param limit Nombre maximum de réponses à retourner
     * @return Liste des meilleures réponses ordonnées par score décroissant
     */
    @Query("SELECT a FROM Answer a WHERE a.assignmentId = :assignmentId " +
           "AND a.score IS NOT NULL ORDER BY a.score DESC")
    List<Answer> findTopAnswersByAssignment(@Param("assignmentId") Long assignmentId, 
                                           Pageable pageable);
}