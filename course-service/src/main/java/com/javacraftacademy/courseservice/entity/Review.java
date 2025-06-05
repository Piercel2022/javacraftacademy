package com.javacraftacademy.courseservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;

/**
 * Représente un avis laissé par un étudiant sur un cours dans l’application JavaCraftAcademy.
 *
 * <p>Un `Review` est lié à un étudiant et à un cours. Il contient une note, un commentaire optionnel, 
 * un indicateur de publication (pour la validation modérateur), et une date de création.</p>
 *
 * <p>Fonctionnalités principales :</p>
 * <ul>
 *   <li>Associer un avis à un étudiant et un cours</li>
 *   <li>Supporter la publication modérée via le champ `published`</li>
 *   <li>Utiliser les requêtes personnalisées dans `ReviewRepository` pour l’analyse et la recherche</li>
 *   <li>Activer la suppression logique (soft delete)</li>
 * </ul>
 *
 * <p>Rôle dans JavaCraftAcademy :</p>
 * Permet de recueillir des retours authentiques sur les cours, utiles à la fois pour les enseignants,
 * les apprenants, et le système de recommandation de contenu.
 */
@Entity
@Table(name = "reviews")
@Data
@EqualsAndHashCode(callSuper = true)
@SQLDelete(sql = "UPDATE reviews SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class Review extends BaseEntity {

    /**
     * Identifiant de l'étudiant ayant rédigé l’avis.
     * Dans une future version, une relation ManyToOne avec une entité Student peut être ajoutée.
     */
    @Column(name = "student_id", nullable = false)
    private Long studentId;

    /**
     * Relation ManyToOne avec l'entité Course.
     * Chaque avis est lié à un seul cours.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    /**
     * Note donnée par l'étudiant (entre 1 et 5).
     */
    @Column(nullable = false)
    private Integer rating;

    /**
     * Commentaire écrit par l’étudiant (optionnel).
     */
    @Column(length = 1000)
    private String comment;

    /**
     * Indique si l’avis est publié et visible publiquement.
     */
    @Column(nullable = false)
    private boolean published = false;

    /**
     * Date de création de l'avis. Automatiquement définie à l'insertion.
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * Définir ou mettre à jour la date de création avant persistance si elle n’est pas fournie.
     */
    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    // Évolutions futures possibles :
    // - Ajouter un champ "title" ou "headline"
    // - Ajouter un champ "moderatedBy" (nom de l'admin ayant approuvé l’avis)
    // - Lier studentId à une vraie entité Student
    // - Ajouter une note globale calculée dynamiquement (facilité, contenu, utilité, etc.)
}
