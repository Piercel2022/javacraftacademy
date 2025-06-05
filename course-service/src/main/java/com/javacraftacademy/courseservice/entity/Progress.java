
// ENTITÉ PROGRESS
// ========================================
// Localisation: src/main/java/com/javacraftacademy/courseservice/entity/Progress.java

package com.javacraftacademy.courseservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entité Progress - Gestion du suivi des progrès des étudiants dans les cours
 * 
 * <p>Cette entité représente le suivi personnalisé de chaque étudiant dans un cours spécifique.
 * Elle capture les informations de progression, les statistiques d'avancement et l'historique
 * d'activité de l'étudiant dans le contexte du cours.</p>
 * 
 * <h3>Fonctionnalités principales :</h3>
 * <ul>
 *   <li><strong>Suivi de progression</strong> : Pourcentage d'avancement et statut de complétion</li>
 *   <li><strong>Gestion temporelle</strong> : Horodatage des accès et dernière activité</li>
 *   <li><strong>Relation bidirectionnelle</strong> : Liaison avec les entités Course et Student</li>
 *   <li><strong>Audit automatique</strong> : Création et modification automatiques via JPA</li>
 *   <li><strong>Validation métier</strong> : Contraintes sur les données critiques</li>
 * </ul>
 * 
 * <h3>Intégration dans l'écosystème JavaCraftAcademy :</h3>
 * <ul>
 *   <li><strong>CourseService</strong> : Service principal pour la gestion des cours et progressions</li>
 *   <li><strong>StudentService</strong> : Interaction avec les données étudiants via API REST</li>
 *   <li><strong>NotificationService</strong> : Envoi d'alertes basées sur les progrès</li>
 *   <li><strong>AnalyticsService</strong> : Génération de rapports et statistiques</li>
 *   <li><strong>RecommendationEngine</strong> : Suggestions basées sur l'historique de progression</li>
 * </ul>
 * 
 * <h3>Extensibilité future :</h3>
 * <ul>
 *   <li>Ajout de badges et achievements basés sur les progrès</li>
 *   <li>Intégration avec un système de gamification</li>
 *   <li>Suivi granulaire par module/chapitre</li>
 *   <li>Historique détaillé des sessions d'apprentissage</li>
 *   <li>Synchronisation avec des plateformes externes (LMS)</li>
 * </ul>
 * 
 * @author JavaCraftAcademy Team
 * @version 2.0
 * @since 1.0
 */
@Entity
@Table(name = "progress", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "course_id"}),
       indexes = {
           @Index(name = "idx_progress_student", columnList = "student_id"),
           @Index(name = "idx_progress_course", columnList = "course_id"),
           @Index(name = "idx_progress_completion", columnList = "completed"),
           @Index(name = "idx_progress_percentage", columnList = "completion_percentage"),
           @Index(name = "idx_progress_last_access", columnList = "last_accessed_at")
       })
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Progress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * Identifiant de l'étudiant - Référence vers le microservice Student
     * Permet le découplage entre les services tout en maintenant l'intégrité référentielle
     */
    @NotNull(message = "L'identifiant étudiant est obligatoire")
    @Column(name = "student_id", nullable = false)
    private Long studentId;

    /**
     * Relation avec l'entité Course
     * FetchType.LAZY pour optimiser les performances lors des requêtes batch
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    @NotNull(message = "Le cours associé est obligatoire")
    private Course course;

    /**
     * Pourcentage de progression dans le cours (0.0 à 100.0)
     * Utilisé pour les calculs statistiques et les rapports visuels
     */
    @DecimalMin(value = "0.0", message = "Le pourcentage de progression ne peut pas être négatif")
    @DecimalMax(value = "100.0", message = "Le pourcentage de progression ne peut pas dépasser 100%")
    @Column(name = "completion_percentage", precision = 5, scale = 2)
    private Double completionPercentage = 0.0;

    /**
     * Statut de complétion du cours
     * Détermine si l'étudiant a terminé tous les modules requis
     */
    @Column(name = "completed", nullable = false)
    private Boolean completed = false;

    /**
     * Horodatage de la dernière activité de l'étudiant dans ce cours
     * Utilisé pour identifier les étudiants inactifs et les relances
     */
    @Column(name = "last_accessed_at")
    private LocalDateTime lastAccessedAt;

    /**
     * Date de création automatique de l'enregistrement
     * Gérée automatiquement par Hibernate
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Date de dernière modification automatique
     * Mise à jour automatique à chaque modification de l'entité
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Temps total passé sur le cours (en minutes)
     * Utilité future pour les analytics et rapports détaillés
     */
    @Min(value = 0, message = "Le temps passé ne peut pas être négatif")
    @Column(name = "time_spent_minutes")
    private Long timeSpentMinutes = 0L;

    /**
     * Score ou note obtenue dans le cours
     * Peut être utilisé pour les certifications et évaluations
     */
    @DecimalMin(value = "0.0", message = "Le score ne peut pas être négatif")
    @DecimalMax(value = "100.0", message = "Le score ne peut pas dépasser 100")
    @Column(name = "score", precision = 5, scale = 2)
    private Double score;

    // ========================================
    // CONSTRUCTEURS
    // ========================================

    /**
     * Constructeur par défaut requis par JPA
     */
    public Progress() {}

    /**
     * Constructeur pour la création d'un nouveau suivi de progression
     * 
     * @param studentId Identifiant de l'étudiant
     * @param course Entité cours associée
     */
    public Progress(Long studentId, Course course) {
        this.studentId = studentId;
        this.course = course;
        this.completionPercentage = 0.0;
        this.completed = false;
        this.timeSpentMinutes = 0L;
        this.lastAccessedAt = LocalDateTime.now();
    }

    // ========================================
    // MÉTHODES MÉTIER
    // ========================================

    /**
     * Met à jour le pourcentage de progression et détermine automatiquement
     * si le cours est complété
     * 
     * @param percentage Nouveau pourcentage (0.0 à 100.0)
     */
    public void updateProgress(Double percentage) {
        if (percentage != null && percentage >= 0.0 && percentage <= 100.0) {
            this.completionPercentage = percentage;
            this.completed = (percentage >= 100.0);
            this.lastAccessedAt = LocalDateTime.now();
        }
    }

    /**
     * Marque le cours comme terminé et met à jour le pourcentage à 100%
     */
    public void markAsCompleted() {
        this.completed = true;
        this.completionPercentage = 100.0;
        this.lastAccessedAt = LocalDateTime.now();
    }

    /**
     * Enregistre une session d'apprentissage
     * 
     * @param minutesSpent Nombre de minutes de la session
     */
    public void addStudySession(Long minutesSpent) {
        if (minutesSpent != null && minutesSpent > 0) {
            this.timeSpentMinutes += minutesSpent;
            this.lastAccessedAt = LocalDateTime.now();
        }
    }

    /**
     * Vérifie si l'étudiant est inactif (pas d'accès depuis plus de X jours)
     * 
     * @param daysThreshold Seuil en jours pour considérer comme inactif
     * @return true si inactif, false sinon
     */
    public boolean isInactive(int daysThreshold) {
        if (lastAccessedAt == null) return true;
        return lastAccessedAt.isBefore(LocalDateTime.now().minusDays(daysThreshold));
    }

    // ========================================
    // GETTERS ET SETTERS
    // ========================================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }

    public Double getCompletionPercentage() { return completionPercentage; }
    public void setCompletionPercentage(Double completionPercentage) { 
        this.completionPercentage = completionPercentage; 
    }

    public Boolean getCompleted() { return completed; }
    public void setCompleted(Boolean completed) { this.completed = completed; }

    public LocalDateTime getLastAccessedAt() { return lastAccessedAt; }
    public void setLastAccessedAt(LocalDateTime lastAccessedAt) { 
        this.lastAccessedAt = lastAccessedAt; 
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public Long getTimeSpentMinutes() { return timeSpentMinutes; }
    public void setTimeSpentMinutes(Long timeSpentMinutes) { 
        this.timeSpentMinutes = timeSpentMinutes; 
    }

    public Double getScore() { return score; }
    public void setScore(Double score) { this.score = score; }

    // ========================================
    // MÉTHODES EQUALS, HASHCODE ET TOSTRING
    // ========================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Progress progress = (Progress) o;
        return Objects.equals(studentId, progress.studentId) &&
               Objects.equals(course, progress.course);
    }

    @Override
    public int hashCode() {
        return Objects.hash(studentId, course);
    }

    @Override
    public String toString() {
        return "Progress{" +
                "id=" + id +
                ", studentId=" + studentId +
                ", courseId=" + (course != null ? course.getId() : null) +
                ", completionPercentage=" + completionPercentage +
                ", completed=" + completed +
                ", lastAccessedAt=" + lastAccessedAt +
                ", timeSpentMinutes=" + timeSpentMinutes +
                '}';
    }
}