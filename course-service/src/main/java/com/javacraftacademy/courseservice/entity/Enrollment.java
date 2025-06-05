package com.javacraftacademy.courseservice.entity;

import jakarta.persistence.*;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Représente l’inscription d’un étudiant à un cours dans JavaCraftAcademy.
 * 
 * <p>Cette entité permet de suivre :</p>
 * <ul>
 *   <li>La date d'inscription</li>
 *   <li>Le statut de progression (en cours, complété, annulé, etc.)</li>
 *   <li>Le pourcentage de complétion du cours</li>
 *   <li>Les certificats délivrés et leurs dates</li>
 *   <li>Les paiements associés à l’inscription</li>
 *   <li>La note finale obtenue et toute remarque de suivi</li>
 * </ul>
 * 
 * <p>Fonctionnalités notables :</p>
 * <ul>
 *   <li>Suppression logique via {@code @SQLDelete}</li>
 *   <li>Filtrage automatique des inscriptions supprimées grâce à {@code @Where}</li>
 *   <li>Unicité : un étudiant ne peut s’inscrire qu’une seule fois à un même cours</li>
 * </ul>
 * 
 * <p>Héritage :</p>
 * Cette classe hérite de {@link BaseEntity}, lui apportant un identifiant, des métadonnées (création, mise à jour) et un champ logique de suppression.
 * 
 * <p>Améliorations possibles :</p>
 * <ul>
 *   <li>Historique complet de progression par module</li>
 *   <li>Ajout de champs de feedback après cours</li>
 *   <li>Intégration d’un système de rappels automatiques ou de relance</li>
 * </ul>
 * 
 * @author JavaCraftacademyTeam
 * @since 1.0
 */
@Entity
@Table(
    name = "enrollments",
    uniqueConstraints = @UniqueConstraint(columnNames = {"course_id", "student_id"})
)
@Data
@EqualsAndHashCode(callSuper = true)
@SQLDelete(sql = "UPDATE enrollments SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class Enrollment extends BaseEntity {

    /**
     * Référence au cours dans lequel l'étudiant est inscrit.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    /**
     * Identifiant unique de l'étudiant.
     */
    @Column(name = "student_id", nullable = false)
    private Long studentId;

    /**
     * Date d'inscription au cours.
     */
    @Column(name = "enrollment_date", nullable = false)
    private LocalDateTime enrollmentDate = LocalDateTime.now();

    /**
     * Statut de l'inscription (e.g., ACTIVE, COMPLETED, CANCELLED).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EnrollmentStatus status = EnrollmentStatus.ACTIVE;

    /**
     * Pourcentage de complétion du cours (0 à 100).
     */
    @Column(name = "completion_percentage")
    private Integer completionPercentage = 0;

    /**
     * Date à laquelle l'étudiant a terminé le cours.
     */
    @Column(name = "completed_date")
    private LocalDateTime completedDate;

    /**
     * Indique si un certificat a été délivré à la fin du cours.
     */
    @Column(name = "certificate_issued")
    private Boolean certificateIssued = false;

    /**
     * Date d’émission du certificat.
     */
    @Column(name = "certificate_issued_date")
    private LocalDateTime certificateIssuedDate;

    /**
     * Dernière date d’accès au contenu du cours.
     */
    @Column(name = "last_accessed_date")
    private LocalDateTime lastAccessedDate;

    /**
     * Montant payé pour s’inscrire à ce cours.
     */
    @Column(name = "paid_amount", precision = 10, scale = 2)
    private BigDecimal paidAmount;

    /**
     * Date de paiement.
     */
    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    /**
     * Note finale obtenue par l’étudiant dans ce cours.
     */
    @Column(name = "final_grade")
    private Integer finalGrade;

    /**
     * Notes administratives ou pédagogiques associées à cette inscription.
     */
    @Column(columnDefinition = "TEXT")
    private String notes;

    // ----------------- MÉTHODES UTILITAIRES --------------------

    /**
     * Vérifie si l'inscription est actuellement active.
     * 
     * @return true si le statut est ACTIVE, false sinon.
     */
    public boolean isActive() {
        return status == EnrollmentStatus.ACTIVE;
    }

    /**
     * Vérifie si l’inscription est complétée, soit via le statut, soit via le pourcentage.
     * 
     * @return true si le statut est COMPLETED ou si completionPercentage est à 100.
     */
    public boolean isCompleted() {
        return status == EnrollmentStatus.COMPLETED ||
               (completionPercentage != null && completionPercentage >= 100);
    }

    /**
     * Marque l’inscription comme complétée.
     * Modifie le statut, fixe le pourcentage à 100 et enregistre la date de complétion.
     */
    public void markAsCompleted() {
        this.status = EnrollmentStatus.COMPLETED;
        this.completionPercentage = 100;
        this.completedDate = LocalDateTime.now();
    }
}
