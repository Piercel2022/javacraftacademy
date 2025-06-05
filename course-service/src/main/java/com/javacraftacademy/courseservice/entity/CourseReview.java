package com.javacraftacademy.courseservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

/**
 * Représente un avis (évaluation) laissé par un étudiant sur un cours au sein de JavaCraftAcademy.
 * <p>
 * Cette entité permet de :
 * <ul>
 *   <li>Stocker la note (1 à 5 étoiles) et un commentaire d’un étudiant sur un cours</li>
 *   <li>Identifier si l'avis est vérifié (cours terminé)</li>
 *   <li>Marquer certains avis comme « mis en avant » (featured)</li>
 *   <li>Suivre le nombre de fois qu’un avis a été jugé utile (helpfulCount)</li>
 *   <li>Empêcher les doublons : un étudiant ne peut donner qu’un seul avis par cours</li>
 * </ul>
 * 
 * <p>
 * **Relations & dépendances dans l’application JavaCraftAcademy :**
 * <ul>
 *   <li>Cette classe hérite de {@link BaseEntity}, lui apportant la gestion des métadonnées (création, mise à jour, suppression logique, audit, etc.)</li>
 *   <li>Elle est liée à l'entité {@link Course}, représentant le cours auquel l'avis est associé</li>
 * </ul>
 * 
 * <p>
 * **Fonctionnalités implémentées :**
 * <ul>
 *   <li>Suppression logique via @SQLDelete et @Where</li>
 *   <li>Validation automatique de la note avant l'insertion ou la mise à jour</li>
 * </ul>
 * 
 * <p>
 * **Évolutions futures possibles :**
 * <ul>
 *   <li>Ajouter un système de réponse de l’instructeur (champ de type `reply`)</li>
 *   <li>Permettre de noter les commentaires comme utiles/non utiles (via une table séparée)</li>
 *   <li>Ajouter une date de publication ou de mise en avant</li>
 * </ul>
 * 
 * @author JavaCraft
 * @since 1.0
 */
@Entity
@Table(
    name = "course_reviews",
    uniqueConstraints = @UniqueConstraint(columnNames = {"course_id", "student_id"})
)
@Data
@EqualsAndHashCode(callSuper = true)
@SQLDelete(sql = "UPDATE course_reviews SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class CourseReview extends BaseEntity {

    /**
     * Lien vers le cours évalué.
     * Représente une relation plusieurs-avis-vers-un-cours.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    /**
     * Identifiant de l'étudiant ayant laissé l'avis.
     */
    @Column(name = "student_id", nullable = false)
    private Long studentId;

    /**
     * Note attribuée au cours (1 à 5 étoiles).
     */
    @Column(nullable = false)
    private Integer rating;

    /**
     * Commentaire textuel associé à la note.
     */
    @Column(columnDefinition = "TEXT")
    private String comment;

    /**
     * Indique si l'avis est vérifié (l’étudiant a terminé le cours).
     */
    @Column(name = "is_verified")
    private Boolean isVerified = false;

    /**
     * Indique si l’avis est mis en avant sur la plateforme.
     */
    @Column(name = "is_featured")
    private Boolean isFeatured = false;

    /**
     * Nombre de fois où l’avis a été jugé utile par d'autres utilisateurs.
     */
    @Column(name = "helpful_count")
    private Integer helpfulCount = 0;

    /**
     * Méthode de validation exécutée avant la persistance ou la mise à jour :
     * s’assure que la note est comprise entre 1 et 5.
     */
    @PrePersist
    @PreUpdate
    private void validateRating() {
        if (rating == null || rating < 1 || rating > 5) {
            throw new IllegalArgumentException("La note doit être comprise entre 1 et 5");
        }
    }
}
