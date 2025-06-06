package com.javacraftacademy.courseservice.model.entity;

import com.javacraftacademy.courseservice.model.enums.CourseLevel;
import com.javacraftacademy.courseservice.model.enums.CourseStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Entité représentant un cours dans le système JavaCraft Academy.
 * 
 * Cette classe constitue l'entité centrale du domaine métier pour la gestion des cours.
 * Elle encapsule toutes les informations essentielles d'un cours : métadonnées, contenu,
 * tarification, et relations avec les autres entités du système.
 * 
 * Fonctionnalités principales :
 * - Gestion complète des métadonnées de cours (titre, description, durée, etc.)
 * - Support des niveaux de difficulté et statuts de publication
 * - Intégration avec le système de catégorisation
 * - Relations bidirectionnelles avec les leçons et inscriptions
 * - Audit automatique des créations/modifications
 * - Validation des données métier
 * 
 * Relations avec l'écosystème :
 * - {@link Category} : Relation ManyToOne pour la catégorisation
 * - {@link Lesson} : Relation OneToMany pour les leçons du cours
 * - {@link Enrollment} : Relation OneToMany pour les inscriptions
 * - {@link CourseContent} : Relation OneToMany pour les contenus multimédias
 * 
 * Intégration avec les services :
 * - Utilisée par {@code CourseService} pour la logique métier
 * - Mappée par {@code CourseMapper} vers les DTOs
 * - Persistée via {@code CourseRepository}
 * - Auditée automatiquement via JPA Auditing
 * 
 * Extensions futures possibles :
 * - Ajout de tags pour une meilleure recherche
 * - Support des prérequis entre cours
 * - Intégration avec un système de rating/reviews
 * - Support des cours multi-langues
 * - Gestion des certificats de completion
 */
@Entity
@Table(name = "courses", indexes = {
    @Index(name = "idx_course_slug", columnList = "slug", unique = true),
    @Index(name = "idx_course_status", columnList = "status"),
    @Index(name = "idx_course_category", columnList = "category_id"),
    @Index(name = "idx_course_level", columnList = "level"),
    @Index(name = "idx_course_created", columnList = "created_at")
})
@EntityListeners(AuditingEntityListener.class)
public class Course {

    /**
     * Identifiant unique du cours.
     * Généré automatiquement par la base de données.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Titre du cours.
     * Doit être unique et informatif pour l'utilisateur.
     */
    @Column(nullable = false, length = 200)
    @NotBlank(message = "Le titre du cours est obligatoire")
    @Size(min = 5, max = 200, message = "Le titre doit contenir entre 5 et 200 caractères")
    private String title;

    /**
     * Slug URL-friendly généré à partir du titre.
     * Utilisé pour les URLs SEO et les références externes.
     */
    @Column(nullable = false, unique = true, length = 250)
    @NotBlank(message = "Le slug est obligatoire")
    @Pattern(regexp = "^[a-z0-9-]+$", message = "Le slug ne peut contenir que des lettres minuscules, chiffres et tirets")
    private String slug;

    /**
     * Description courte du cours pour les listes et aperçus.
     */
    @Column(length = 500)
    @Size(max = 500, message = "La description courte ne peut dépasser 500 caractères")
    private String shortDescription;

    /**
     * Description complète et détaillée du cours.
     * Supporte le formatage Markdown pour une présentation riche.
     */
    @Column(columnDefinition = "TEXT")
    @NotBlank(message = "La description détaillée est obligatoire")
    private String description;

    /**
     * URL de l'image de couverture du cours.
     * Stockée dans le service de fichiers externe.
     */
    @Column(length = 500)
    private String thumbnailUrl;

    /**
     * Prix du cours en euros.
     * Null si le cours est gratuit.
     */
    @Column(precision = 10, scale = 2)
    @DecimalMin(value = "0.0", inclusive = false, message = "Le prix doit être positif")
    @Digits(integer = 8, fraction = 2, message = "Format de prix invalide")
    private BigDecimal price;

    /**
     * Niveau de difficulté du cours.
     * Enum : BEGINNER, INTERMEDIATE, ADVANCED, EXPERT
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @NotNull(message = "Le niveau du cours est obligatoire")
    private CourseLevel level;

    /**
     * Statut de publication du cours.
     * Enum : DRAFT, PUBLISHED, ARCHIVED, SUSPENDED
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @NotNull(message = "Le statut du cours est obligatoire")
    private CourseStatus status;

    /**
     * Durée estimée du cours en minutes.
     * Calculée automatiquement à partir des leçons.
     */
    @Column
    @Min(value = 1, message = "La durée doit être positive")
    private Integer estimatedDuration;

    /**
     * Identifiant de l'instructeur créateur du cours.
     * Référence vers le service utilisateur externe.
     */
    @Column(nullable = false)
    @NotNull(message = "L'instructeur est obligatoire")
    private Long instructorId;

    /**
     * Catégorie du cours.
     * Relation ManyToOne avec l'entité Category.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    @NotNull(message = "La catégorie est obligatoire")
    private Category category;

    /**
     * Liste des leçons appartenant à ce cours.
     * Relation OneToMany bidirectionnelle avec cascade.
     */
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    private List<Lesson> lessons = new ArrayList<>();

    /**
     * Liste des inscriptions à ce cours.
     * Relation OneToMany pour le suivi des apprenants.
     */
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL)
    private List<Enrollment> enrollments = new ArrayList<>();

    /**
     * Contenus multimédias associés au cours.
     * Relation OneToMany pour les ressources additionnelles.
     */
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CourseContent> contents = new ArrayList<>();

    /**
     * Date et heure de création de l'entité.
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
    public Course() {}

    /**
     * Constructeur de commodité pour la création d'un nouveau cours.
     * 
     * @param title Titre du cours
     * @param slug Slug URL-friendly
     * @param description Description détaillée
     * @param level Niveau de difficulté
     * @param instructorId Identifiant de l'instructeur
     * @param category Catégorie du cours
     */
    public Course(String title, String slug, String description, CourseLevel level, Long instructorId, Category category) {
        this.title = title;
        this.slug = slug;
        this.description = description;
        this.level = level;
        this.instructorId = instructorId;
        this.category = category;
        this.status = CourseStatus.DRAFT; // Statut par défaut
    }

    // === MÉTHODES MÉTIER ===

    /**
     * Ajoute une leçon au cours.
     * Maintient la cohérence de la relation bidirectionnelle.
     * 
     * @param lesson La leçon à ajouter
     */
    public void addLesson(Lesson lesson) {
        lessons.add(lesson);
        lesson.setCourse(this);
        updateEstimatedDuration();
    }

    /**
     * Supprime une leçon du cours.
     * 
     * @param lesson La leçon à supprimer
     */
    public void removeLesson(Lesson lesson) {
        lessons.remove(lesson);
        lesson.setCourse(null);
        updateEstimatedDuration();
    }

    /**
     * Ajoute une inscription au cours.
     * 
     * @param enrollment L'inscription à ajouter
     */
    public void addEnrollment(Enrollment enrollment) {
        enrollments.add(enrollment);
        enrollment.setCourse(this);
    }

    /**
     * Ajoute un contenu au cours.
     * 
     * @param content Le contenu à ajouter
     */
    public void addContent(CourseContent content) {
        contents.add(content);
        content.setCourse(this);
    }

    /**
     * Met à jour la durée estimée basée sur les leçons.
     * Appelée automatiquement lors de l'ajout/suppression de leçons.
     */
    private void updateEstimatedDuration() {
        this.estimatedDuration = lessons.stream()
            .mapToInt(lesson -> lesson.getDuration() != null ? lesson.getDuration() : 0)
            .sum();
    }

    /**
     * Vérifie si le cours est publié.
     * 
     * @return true si le cours est publié
     */
    public boolean isPublished() {
        return CourseStatus.PUBLISHED.equals(this.status);
    }

    /**
     * Vérifie si le cours est gratuit.
     * 
     * @return true si le cours est gratuit
     */
    public boolean isFree() {
        return this.price == null || BigDecimal.ZERO.compareTo(this.price) >= 0;
    }

    /**
     * Obtient le nombre total d'inscriptions.
     * 
     * @return Nombre d'inscriptions
     */
    public int getEnrollmentCount() {
        return enrollments.size();
    }

    /**
     * Obtient le nombre total de leçons.
     * 
     * @return Nombre de leçons
     */
    public int getLessonCount() {
        return lessons.size();
    }

    // === GETTERS ET SETTERS ===

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public String getShortDescription() { return shortDescription; }
    public void setShortDescription(String shortDescription) { this.shortDescription = shortDescription; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public CourseLevel getLevel() { return level; }
    public void setLevel(CourseLevel level) { this.level = level; }

    public CourseStatus getStatus() { return status; }
    public void setStatus(CourseStatus status) { this.status = status; }

    public Integer getEstimatedDuration() { return estimatedDuration; }
    public void setEstimatedDuration(Integer estimatedDuration) { this.estimatedDuration = estimatedDuration; }

    public Long getInstructorId() { return instructorId; }
    public void setInstructorId(Long instructorId) { this.instructorId = instructorId; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public List<Lesson> getLessons() { return new ArrayList<>(lessons); }
    public void setLessons(List<Lesson> lessons) { 
        this.lessons.clear();
        if (lessons != null) {
            lessons.forEach(this::addLesson);
        }
    }

    public List<Enrollment> getEnrollments() { return new ArrayList<>(enrollments); }
    public void setEnrollments(List<Enrollment> enrollments) { this.enrollments = enrollments; }

    public List<CourseContent> getContents() { return new ArrayList<>(contents); }
    public void setContents(List<CourseContent> contents) { this.contents = contents; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // === EQUALS, HASHCODE ET TOSTRING ===

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Course)) return false;
        Course course = (Course) o;
        return Objects.equals(id, course.id) && Objects.equals(slug, course.slug);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, slug);
    }

    @Override
    public String toString() {
        return "Course{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", slug='" + slug + '\'' +
                ", level=" + level +
                ", status=" + status +
                ", instructorId=" + instructorId +
                ", lessonCount=" + getLessonCount() +
                ", enrollmentCount=" + getEnrollmentCount() +
                '}';
    }
}