package com.javacraftacademy.courseservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entité JPA représentant un devoir/assignation dans le système JavaCraftAcademy.
 * 
 * <p>Cette classe centrale du module Course Service gère tous les types de devoirs
 * et projets assignés aux étudiants dans le cadre des cours de programmation.</p>
 * 
 * <h3>Fonctionnalités principales :</h3>
 * <ul>
 *   <li>Gestion complète des devoirs avec instructions et critères d'évaluation</li>
 *   <li>Support de multiples types d'assignations (projets, exercices, quiz)</li>
 *   <li>Système de notation flexible avec points maximum et seuil de réussite</li>
 *   <li>Gestion des échéances et soumissions tardives avec pénalités</li>
 *   <li>Support de la correction automatique et manuelle</li>
 *   <li>Limitation du nombre de soumissions par étudiant</li>
 *   <li>Système de fichiers attachés (énoncés, ressources)</li>
 *   <li>Soft delete pour la conservation de l'historique académique</li>
 * </ul>
 * 
 * <h3>Relations avec l'écosystème JavaCraftAcademy :</h3>
 * <ul>
 *   <li><strong>Course</strong> : Relation Many-to-One - chaque devoir appartient à un cours</li>
 *   <li><strong>Lesson</strong> : Relation Many-to-One optionnelle - peut être lié à une leçon spécifique</li>
 *   <li><strong>AssignmentSubmission</strong> : Relation One-to-Many - gestion des soumissions d'étudiants</li>
 *   <li><strong>AssignmentFile</strong> : Relation One-to-Many - fichiers et ressources attachés</li>
 *   <li><strong>Grade</strong> : Utilisé pour le calcul des notes finales</li>
 *   <li><strong>LearningProgress</strong> : Intégré dans le suivi de progression</li>
 *   <li><strong>User</strong> : Via les soumissions pour le suivi des étudiants</li>
 * </ul>
 * 
 * <h3>Architecture du système :</h3>
 * <ul>
 *   <li>Partie intégrante du microservice Course Service</li>
 *   <li>Intégration avec le système de notification (rappels d'échéances)</li>
 *   <li>Compatible avec le système d'évaluation automatique</li>
 *   <li>Support du plagiat detection via l'analyse de code</li>
 *   <li>Intégration avec le système de feedback et révision par pairs</li>
 * </ul>
 * 
 * <h3>Types d'assignations supportés :</h3>
 * <ul>
 *   <li><strong>PROJECT</strong> : Projets de développement complets</li>
 *   <li><strong>EXERCISE</strong> : Exercices pratiques ciblés</li>
 *   <li><strong>QUIZ</strong> : Évaluations courtes et automatisées</li>
 *   <li><strong>EXAM</strong> : Examens formels avec contraintes temporelles</li>
 * </ul>
 * 
 * <h3>Fonctionnalités avancées :</h3>
 * <ul>
 *   <li>Soft delete avec annotation @SQLDelete pour préserver l'historique</li>
 *   <li>Filtrage automatique des éléments supprimés avec @Where</li>
 *   <li>Héritage de BaseEntity pour les métadonnées communes</li>
 *   <li>Cascade operations pour la gestion des relations</li>
 * </ul>
 * 
 * @author JavaCraftAcademy Team
 * @version 2.1
 * @since 1.0
 */
@Entity
@Table(name = "assignments")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SQLDelete(sql = "UPDATE assignments SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class Assignment extends BaseEntity {
    
    @NotBlank(message = "Assignment title cannot be blank")
    @Size(max = 200, message = "Title cannot exceed 200 characters")
    @Column(nullable = false, length = 200)
    private String title;
    
    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @NotBlank(message = "Instructions are required")
    @Column(columnDefinition = "TEXT", nullable = false)
    private String instructions;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    @NotNull(message = "Assignment must belong to a course")
    private Course course;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id")
    private Lesson lesson;
    
    @Min(value = 0, message = "Order index must be positive")
    @Column(name = "order_index")
    private Integer orderIndex;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull(message = "Assignment type is required")
    private AssignmentType assignmentType = AssignmentType.PROJECT;
    
    @NotNull(message = "Maximum points must be specified")
    @Min(value = 1, message = "Maximum points must be at least 1")
    @Max(value = 1000, message = "Maximum points cannot exceed 1000")
    @Column(name = "max_points", nullable = false)
    private Integer maxPoints = 100;
    
    @Min(value = 0, message = "Passing points cannot be negative")
    @Column(name = "passing_points")
    private Integer passingPoints;
    
    @Column(name = "due_date")
    private LocalDateTime dueDate;
    
    @Column(name = "late_submission_allowed")
    private Boolean lateSubmissionAllowed = false;
    
    @Min(value = 0, message = "Late penalty percentage cannot be negative")
    @Max(value = 100, message = "Late penalty percentage cannot exceed 100")
    @Column(name = "late_penalty_percentage")
    private Integer latePenaltyPercentage = 0;
    
    @Column(name = "auto_grade")
    private Boolean autoGrade = false;
    
    @Min(value = 1, message = "Submission limit must be at least 1")
    @Column(name = "submission_limit")
    private Integer submissionLimit;
    
    @Size(max = 5000, message = "Rubric cannot exceed 5000 characters")
    @Column(columnDefinition = "TEXT")
    private String rubric;
    
    // Add deleted field for soft delete functionality
    @Column(name = "deleted")
    private Boolean deleted = false;
    
    @OneToMany(mappedBy = "assignment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AssignmentSubmission> submissions = new ArrayList<>();
    
    @OneToMany(mappedBy = "assignment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AssignmentFile> files = new ArrayList<>();
    
    /**
     * Vérifie si la date limite de soumission est dépassée.
     * 
     * @return true si l'assignation est en retard, false sinon
     */
    public boolean isOverdue() {
        return dueDate != null && LocalDateTime.now().isAfter(dueDate);
    }
    
    /**
     * Détermine si les soumissions sont encore autorisées.
     * Prend en compte les soumissions tardives et les limites.
     * 
     * @return true si les soumissions sont autorisées, false sinon
     */
    public boolean isSubmissionAllowed() {
        if (isOverdue() && !Boolean.TRUE.equals(lateSubmissionAllowed)) {
            return false;
        }
        return true;
    }
    
    /**
     * Calcule le pourcentage de pénalité à appliquer pour une soumission tardive.
     * 
     * @param submissionDate la date de soumission
     * @return le pourcentage de pénalité à appliquer
     */
    public int calculateLatePenalty(LocalDateTime submissionDate) {
        if (dueDate == null || submissionDate == null || !submissionDate.isAfter(dueDate)) {
            return 0;
        }
        return latePenaltyPercentage != null ? latePenaltyPercentage : 0;
    }
    
    /**
     * Vérifie si l'assignation nécessite une correction automatique.
     * 
     * @return true si la correction est automatique, false si manuelle
     */
    public boolean requiresAutoGrading() {
        return Boolean.TRUE.equals(autoGrade);
    }
    
    /**
     * Détermine si l'assignation a un seuil de réussite défini.
     * 
     * @return true si un seuil de réussite est défini, false sinon
     */
    public boolean hasPassingThreshold() {
        return passingPoints != null && passingPoints > 0;
    }
    
    /**
     * Calcule le pourcentage de réussite requis.
     * 
     * @return le pourcentage de points requis pour réussir
     */
    public double getPassingPercentage() {
        if (!hasPassingThreshold()) {
            return 0.0;
        }
        return (double) passingPoints / maxPoints * 100;
    }
    
    /**
     * Vérifie si l'assignation est active et peut recevoir des soumissions.
     * 
     * @return true si l'assignation est active, false sinon
     */
    public boolean isActive() {
        return !Boolean.TRUE.equals(deleted) && isSubmissionAllowed();
    }
    
    /**
     * Retourne le nombre de soumissions reçues pour cette assignation.
     * 
     * @return le nombre total de soumissions
     */
    public int getSubmissionCount() {
        return submissions != null ? submissions.size() : 0;
    }
    
    /**
     * Ajoute une soumission à l'assignation.
     * Maintient la cohérence bidirectionnelle de la relation.
     * 
     * @param submission la soumission à ajouter
     */
    public void addSubmission(AssignmentSubmission submission) {
        if (submissions == null) {
            submissions = new ArrayList<>();
        }
        submissions.add(submission);
        submission.setAssignment(this);
    }
    
    /**
     * Ajoute un fichier à l'assignation.
     * Maintient la cohérence bidirectionnelle de la relation.
     * 
     * @param file le fichier à ajouter
     */
    public void addFile(AssignmentFile file) {
        if (files == null) {
            files = new ArrayList<>();
        }
        files.add(file);
        file.setAssignment(this);
    }
    
    /**
     * Représentation textuelle simplifiée de l'assignation.
     * 
     * @return une chaîne contenant les informations essentielles
     */
    @Override
    public String toString() {
        return "Assignment{" +
                "id=" + (getId() != null ? getId() : "null") +
                ", title='" + title + '\'' +
                ", assignmentType=" + assignmentType +
                ", maxPoints=" + maxPoints +
                ", dueDate=" + dueDate +
                ", submissionCount=" + getSubmissionCount() +
                ", isActive=" + isActive() +
                '}';
    }
}