package com.javacraftacademy.courseservice.model.entity;

import com.javacraftacademy.courseservice.model.enums.LessonType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entité représentant une leçon dans le système JavaCraft Academy.
 * 
 * Cette classe modélise une leçon individuelle appartenant à un cours.
 * Elle encapsule le contenu pédagogique, les métadonnées d'organisation,
 * et les informations de progression pour les apprenants.
 * 
 * Fonctionnalités principales :
 * - Gestion du contenu pédagogique (vidéo, texte, quiz, exercice)
 * - Organisation séquentielle au sein d'un cours
 * - Support de différents types de contenu multimédia
 * - Tracking de durée pour estimation de progression
 * - Contrôle de visibilité et de disponibilité
 * - Audit automatique des modifications
 * 
 * Relations avec l'écosystème :
 * - {@link Course} : Relation ManyToOne obligatoire (leçon appartient à un cours)
 * - {@link LessonType} : Enum définissant le type de contenu
 * - Intégration avec les services de streaming vidéo
 * - Liaison avec le système de progression utilisateur
 * 
 * Intégration avec les services :
 * - Utilisée par {@code LessonService} pour la logique métier
 * - Mappée par {@code LessonMapper} vers les DTOs
 * - Persistée via {@code LessonRepository}
 * - Intégrée au système de fichiers via {@code FileStorageService}
 * 
 * Types de contenu supportés :
 * - VIDEO : Contenu vidéo avec URL de streaming
 * - TEXT : Article ou documentation textuelle
 * - QUIZ : Questions à choix multiples
 * - EXERCISE : Exercices pratiques de programmation
 * - RESOURCE : Ressources téléchargeables (PDF, code, etc.)
 * 
 * Extensions futures possibles :
 * - Support des sous-titres multi-langues
 * - Intégration avec des outils de live coding
 * - Système de notes et bookmarks
 * - Analytics détaillées de visionnage
 * - Support des leçons interactives
 * - Intégration avec des IDE en ligne
 */
@Entity
@Table(name = "lessons", indexes = {
    @Index(name = "idx_lesson_course", columnList = "course_id"),
    @Index(name = "idx_lesson_order", columnList = "course_id, order_index"),
    @Index(name = "idx_lesson_type", columnList = "lesson_type"),
    @Index(name = "idx_lesson_free", columnList = "is_free")
})
@EntityListeners(AuditingEntityListener.class)
public class Lesson {

    /**
     * Identifiant unique de la leçon.
     * Généré automatiquement par la base de données.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Titre de la leçon.
     * Doit être descriptif et informatif pour l'apprenant.
     */
    @Column(nullable = false, length = 200)
    @NotBlank(message = "Le titre de la leçon est obligatoire")
    @Size(min = 3, max = 200, message = "Le titre doit contenir entre 3 et 200 caractères")
    private String title;

    /**
     * Slug URL-friendly généré à partir du titre.
     * Utilisé pour les URLs et références directes.
     */
    @Column(nullable = false, length = 250)
    @NotBlank(message = "Le slug est obligatoire")
    @Pattern(regexp = "^[a-z0-9-]+$", message = "Le slug ne peut contenir que des lettres minuscules, chiffres et tirets")
    private String slug;

    /**
     * Description détaillée de la leçon.
     * Explique les objectifs d'apprentissage et le contenu.
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Contenu principal de la leçon.
     * Format dépendant du type (HTML pour TEXT, URL pour VIDEO, JSON pour QUIZ).
     */
    @Column(columnDefinition = "MEDIUMTEXT")
    private String content;

    /**
     * URL de la vidéo si le type est VIDEO.
     * Référence vers le service de streaming vidéo.
     */
    @Column(length = 500)
    private String videoUrl;

    /**
     * Durée de la leçon en minutes.
     * Utilisée pour calculer la progression et l'estimation de temps.
     */
    @Column
    @Min(value = 1, message = "La durée doit être positive")
    private Integer duration;

    /**
     * Type de contenu de la leçon.
     * Détermine comment le contenu est présenté et traité.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "lesson_type", nullable = false, length = 20)
    @NotNull(message = "Le type de leçon est obligatoire")
    private LessonType lessonType;

    /**
     * Position de la leçon dans l'ordre du cours.
     * Détermine l'ordre de présentation aux apprenants.
     */
    @Column(name = "order_index", nullable = false)
    @NotNull(message = "L'ordre de la leçon est obligatoire")
    @Min(value = 1, message = "L'ordre doit être positif")
    private Integer orderIndex;

    /**
     * Indique si la leçon est accessible gratuitement.
     * Les leçons gratuites peuvent être consultées sans inscription.
     */
    @Column(name = "is_free", nullable = false)
    private Boolean isFree = false;

    /**
     * Indique si la leçon est publiée et visible.
     * Les leçons non publiées ne sont visibles que par les instructeurs.
     */
    @Column(name = "is_published", nullable = false)
    private Boolean isPublished = false;

    /**
     * Cours auquel appartient cette leçon.
     * Relation ManyToOne obligatoire.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    @NotNull(message = "Le cours est obligatoire")
    private Course course;

    /**
     * Date et heure de création de la leçon.
     * Gérée automatiquement par JPA Auditing.
     */
    @CreatedDate
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Date et heure de dernière modification.
     * Mise à jour automatiquement à chaque modification.
     */
    @LastModifiedDate
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Constructeur par défaut requis par JPA.
     */
    public Lesson() {}

    /**
     * Constructeur de commodité pour créer une nouvelle leçon.
     * 
     * @param title Titre de la leçon
     * @param slug Slug URL-friendly
     * @param lessonType Type de contenu
     * @param orderIndex Position dans le cours
     * @param course Cours parent
     */
    public Lesson(String title, String slug, LessonType lessonType, Integer orderIndex, Course course) {
        this.title = title;
        this.slug = slug;
        this.lessonType = lessonType;
        this.orderIndex = orderIndex;
        this.course = course;
        this.isFree = false;
        this.isPublished = false;
    }

    // === MÉTHODES MÉTIER ===

    /**
     * Vérifie si la leçon est accessible gratuitement.
     * 
     * @return true si la leçon est gratuite
     */
    public boolean isAccessibleForFree() {
        return Boolean.TRUE.equals(this.isFree);
    }

    /**
     * Vérifie si la leçon est publiée et visible.
     * 
     * @return true si la leçon est publiée
     */
    public boolean isVisible() {
        return Boolean.TRUE.equals(this.isPublished);
    }

    /**
     * Vérifie si la leçon contient du contenu vidéo.
     * 
     * @return true si c'est une leçon vidéo
     */
    public boolean isVideoLesson() {
        return LessonType.VIDEO.equals(this.lessonType);
    }

    /**
     * Vérifie si la leçon est un quiz.
     * 
     * @return true si c'est un quiz
     */
    public boolean isQuiz() {
        return LessonType.QUIZ.equals(this.lessonType);
    }

    /**
     * Vérifie si la leçon est un exercice pratique.
     * 
     * @return true si c'est un exercice
     */
    public boolean isExercise() {
        return LessonType.EXERCISE.equals(this.lessonType);
    }

    /**
     * Obtient l'URL de la vidéo ou du contenu principal.
     * 
     * @return URL du contenu principal
     */
    public String getMainContentUrl() {
        if (isVideoLesson() && videoUrl != null) {
            return videoUrl;
        }
        return null;
    }

    /**
     * Calcule la durée formatée en heures et minutes.
     * 
     * @return Durée formatée (ex: "1h 23min" ou "45min")
     */
    public String getFormattedDuration() {
        if (duration == null || duration <= 0) {
            return "Durée non spécifiée";
        }
        
        int hours = duration / 60;
        int minutes = duration % 60;
        
        if (hours > 0) {
            return String.format("%dh %02dmin", hours, minutes);
        } else {
            return String.format("%dmin", minutes);
        }
    }

    /**
     * Vérifie si la leçon nécessite une inscription au cours.
     * Les leçons gratuites peuvent être consultées sans inscription.
     * 
     * @return true si une inscription est requise
     */
    public boolean requiresEnrollment() {
        return !isAccessibleForFree();
    }

    /**
     * Met à jour l'ordre de la leçon dans le cours.
     * Utilisé pour réorganiser les leçons.
     * 
     * @param newOrder Nouvel ordre
     */
    public void updateOrder(Integer newOrder) {
        if (newOrder != null && newOrder > 0) {
            this.orderIndex = newOrder;
        }
    }

    /**
     * Publie la leçon (la rend visible).
     */
    public void publish() {
        this.isPublished = true;
    }

    /**
     * Dépublie la leçon (la cache).
     */
    public void unpublish() {
        this.isPublished = false;
    }

    /**
     * Rend la leçon gratuite.
     */
    public void makeFree() {
        this.isFree = true;
    }

    /**
     * Rend la leçon payante.
     */
    public void makePaid() {
        this.isFree = false;
    }

    // === GETTERS ET SETTERS ===

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }

    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }

    public LessonType getLessonType() { return lessonType; }
    public void setLessonType(LessonType lessonType) { this.lessonType = lessonType; }

    public Integer getOrderIndex() { return orderIndex; }
    public void setOrderIndex(Integer orderIndex) { this.orderIndex = orderIndex; }

    public Boolean getIsFree() { return isFree; }
    public void setIsFree(Boolean isFree) { this.isFree = isFree; }

    public Boolean getIsPublished() { return isPublished; }
    public void setIsPublished(Boolean isPublished) { this.isPublished = isPublished; }

    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // === EQUALS, HASHCODE ET TOSTRING ===

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Lesson)) return false;
        Lesson lesson = (Lesson) o;
        return Objects.equals(id, lesson.id) && Objects.equals(slug, lesson.slug);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, slug);
    }

    @Override
    public String toString() {
        return "Lesson{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", slug='" + slug + '\'' +
                ", lessonType=" + lessonType +
                ", orderIndex=" + orderIndex +
                ", duration=" + duration +
                ", isFree=" + isFree +
                ", isPublished=" + isPublished +
                ", courseId=" + (course != null ? course.getId() : null) +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }

    /**
     * Builder pattern pour créer des instances de Lesson de manière fluide.
     */
    public static class Builder {
        private String title;
        private String slug;
        private String description;
        private String content;
        private String videoUrl;
        private Integer duration;
        private LessonType lessonType;
        private Integer orderIndex;
        private Boolean isFree = false;
        private Boolean isPublished = false;
        private Course course;

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder slug(String slug) {
            this.slug = slug;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder content(String content) {
            this.content = content;
            return this;
        }

        public Builder videoUrl(String videoUrl) {
            this.videoUrl = videoUrl;
            return this;
        }

        public Builder duration(Integer duration) {
            this.duration = duration;
            return this;
        }

        public Builder lessonType(LessonType lessonType) {
            this.lessonType = lessonType;
            return this;
        }

        public Builder orderIndex(Integer orderIndex) {
            this.orderIndex = orderIndex;
            return this;
        }

        public Builder isFree(Boolean isFree) {
            this.isFree = isFree;
            return this;
        }

        public Builder isPublished(Boolean isPublished) {
            this.isPublished = isPublished;
            return this;
        }

        public Builder course(Course course) {
            this.course = course;
            return this;
        }

        public Lesson build() {
            Lesson lesson = new Lesson();
            lesson.title = this.title;
            lesson.slug = this.slug;
            lesson.description = this.description;
            lesson.content = this.content;
            lesson.videoUrl = this.videoUrl;
            lesson.duration = this.duration;
            lesson.lessonType = this.lessonType;
            lesson.orderIndex = this.orderIndex;
            lesson.isFree = this.isFree;
            lesson.isPublished = this.isPublished;
            lesson.course = this.course;
            return lesson;
        }
    }

    /**
     * Méthode statique pour créer un builder.
     * 
     * @return Nouvelle instance du builder
     */
    public static Builder builder() {
        return new Builder();
    }
}