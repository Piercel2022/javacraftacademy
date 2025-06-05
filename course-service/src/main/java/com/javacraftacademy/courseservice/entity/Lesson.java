package com.javacraftacademy.courseservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Représente une leçon d’un cours dans la plateforme JavaCraft Academy.
 *
 * <p>Chaque leçon est associée à un cours parent et peut contenir du contenu textuel,
 * des vidéos, des devoirs, et des métadonnées de publication.</p>
 *
 * <p>La suppression logique est gérée via les annotations {@link SQLDelete} et {@link Where}.</p>
 */
@Entity
@Table(name = "lessons")
@Data
@EqualsAndHashCode(callSuper = true)
@SQLDelete(sql = "UPDATE lessons SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class Lesson extends BaseEntity {

    /** Titre de la leçon (obligatoire, maximum 200 caractères) */
    @Column(nullable = false, length = 200)
    private String title;

    /** Description facultative de la leçon (résumé ou objectifs pédagogiques) */
    @Column(columnDefinition = "TEXT")
    private String description;

    /** Contenu principal de la leçon, généralement HTML ou Markdown */
    @Column(columnDefinition = "LONGTEXT")
    private String content;

    /** Cours auquel appartient cette leçon */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    /** Ordre d’apparition de la leçon dans le cours */
    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    /** Type de leçon : TEXT, VIDEO, QUIZ, etc. (défini via enum LessonType) */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LessonType lessonType = LessonType.TEXT;

    /** URL de la vidéo si la leçon est de type VIDEO */
    @Column(name = "video_url")
    private String videoUrl;

    /** Durée de la vidéo (en secondes) */
    @Column(name = "video_duration")
    private Integer videoDuration;

    /** Indique si la leçon est accessible sans inscription/paiement */
    @Column(name = "is_free")
    private Boolean isFree = false;

    /** Statut de publication de la leçon */
    @Column(name = "is_published")
    private Boolean isPublished = false;

    /** Date à laquelle la leçon a été publiée */
    @Column(name = "published_date")
    private LocalDateTime publishedDate;

    /** Durée estimée pour suivre la leçon (en minutes) */
    @Column(name = "estimated_duration")
    private Integer estimatedDuration;

    /** Liste des devoirs liés à cette leçon */
    @OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Assignment> assignments = new ArrayList<>();

    /** Liste des suivis de progression des étudiants pour cette leçon */
    @OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LessonProgress> lessonProgresses = new ArrayList<>();

    /**
     * Indique si la leçon est accessible actuellement.
     *
     * <p>Conditions : la leçon est publiée et, si une date de publication est spécifiée,
     * celle-ci est déjà atteinte.</p>
     *
     * @return true si la leçon est visible par les utilisateurs
     */
    public boolean isAccessible() {
        return Boolean.TRUE.equals(isPublished) &&
               (publishedDate == null || publishedDate.isBefore(LocalDateTime.now()));
    }

    /**
     * Retourne la durée totale de la leçon sous forme textuelle (ex: "15 minutes", "3 min vidéo").
     *
     * @return texte lisible représentant la durée
     */
    public String getReadableDuration() {
        if (lessonType == LessonType.VIDEO && videoDuration != null) {
            int minutes = videoDuration / 60;
            return minutes + " min de vidéo";
        } else if (estimatedDuration != null) {
            return estimatedDuration + " minutes estimées";
        }
        return "Durée inconnue";
    }
}
