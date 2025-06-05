package com.javacraftacademy.courseservice.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;
import org.springframework.data.elasticsearch.annotations.DateFormat;

// Migration de javax.persistence vers jakarta.persistence
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Entité Content représentant le contenu pédagogique dans JavaCraft Academy.
 * 
 * <p>Cette classe modélise les différents types de contenus éducatifs disponibles
 * dans la plateforme : vidéos, articles, exercices, quiz, etc. Elle est conçue
 * pour être utilisée à la fois avec JPA (base de données relationnelle) et
 * Elasticsearch (recherche full-text).</p>
 * 
 * <h3>Fonctionnalités principales :</h3>
 * <ul>
 *   <li>Stockage du contenu pédagogique avec métadonnées</li>
 *   <li>Support de la recherche full-text via Elasticsearch</li>
 *   <li>Classification par type, difficulté et tags</li>
 *   <li>Organisation hiérarchique par cours et chapitres</li>
 *   <li>Gestion de l'interactivité et du suivi de progression</li>
 * </ul>
 * 
 * <h3>Relations dans l'application :</h3>
 * <ul>
 *   <li><strong>Course</strong> : Un contenu appartient à un cours (Many-to-One)</li>
 *   <li><strong>Chapter</strong> : Un contenu peut faire partie d'un chapitre (Many-to-One)</li>
 *   <li><strong>User</strong> : Suivi des progrès utilisateur via UserProgress</li>
 *   <li><strong>ContentSearchRepository</strong> : Recherche avancée Elasticsearch</li>
 * </ul>
 * 
 * <h3>Types de contenu supportés :</h3>
 * <ul>
 *   <li>VIDEO : Contenu vidéo avec transcript pour la recherche</li>
 *   <li>ARTICLE : Articles et documentation</li>
 *   <li>EXERCISE : Exercices pratiques</li>
 *   <li>QUIZ : Quiz d'évaluation</li>
 *   <li>RESOURCE : Ressources téléchargeables</li>
 * </ul>
 * 
 * @author JavaCraft Academy Team
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(name = "contents", indexes = {
    @Index(name = "idx_content_course", columnList = "course_id"),
    @Index(name = "idx_content_chapter", columnList = "chapter_id"),
    @Index(name = "idx_content_type", columnList = "type"),
    @Index(name = "idx_content_created", columnList = "created_date")
})
@Document(indexName = "contents")
@Setting(settingPath = "/elasticsearch/content-settings.json")
public class Content {

    @jakarta.persistence.Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @org.springframework.data.annotation.Id
    private Long id;

    @NotBlank(message = "Le titre est obligatoire")
    @Size(max = 200, message = "Le titre ne peut pas dépasser 200 caractères")
    @Column(name = "title", nullable = false, length = 200)
    @Field(type = FieldType.Text, analyzer = "french")
    private String title;

    @Size(max = 1000, message = "La description ne peut pas dépasser 1000 caractères")
    @Column(name = "description", length = 1000)
    @Field(type = FieldType.Text, analyzer = "french")
    private String description;

    @NotNull(message = "Le type de contenu est obligatoire")
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    @Field(type = FieldType.Keyword)
    private ContentType type;

    @NotNull(message = "L'ID du cours est obligatoire")
    @Column(name = "course_id", nullable = false)
    @Field(type = FieldType.Long)
    private Long courseId;

    @Column(name = "chapter_id")
    @Field(type = FieldType.Long)
    private Long chapterId;

    @Lob
    @Column(name = "content_data", columnDefinition = "LONGTEXT")
    @Field(type = FieldType.Text, analyzer = "french")
    private String contentData;

    // Transcript pour les vidéos (utilisé pour la recherche full-text)
    @Lob
    @Column(name = "transcript", columnDefinition = "LONGTEXT")
    @Field(type = FieldType.Text, analyzer = "french")
    private String transcript;

    @Min(value = 0, message = "La durée doit être positive")
    @Column(name = "duration")
    @Field(type = FieldType.Integer)
    private Integer duration; // en secondes

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty")
    @Field(type = FieldType.Keyword)
    private DifficultyLevel difficulty;

    @Column(name = "interactive")
    @Field(type = FieldType.Boolean)
    private Boolean interactive = false;

    @Min(value = 0, message = "L'index d'ordre doit être positif")
    @Column(name = "order_index")
    @Field(type = FieldType.Integer)
    private Integer orderIndex;

    @ElementCollection
    @CollectionTable(name = "content_tags", joinColumns = @JoinColumn(name = "content_id"))
    @Column(name = "tag")
    @Field(type = FieldType.Keyword)
    private List<String> tags;

    @Column(name = "url")
    @Field(type = FieldType.Keyword)
    private String url;

    @Column(name = "file_path")
    private String filePath;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    @Column(name = "created_date", nullable = false)
    @Field(type = FieldType.Date, format = DateFormat.date_time)
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    @Field(type = FieldType.Date, format = DateFormat.date_time)
    private LocalDateTime updatedDate;

    @Column(name = "published")
    @Field(type = FieldType.Boolean)
    private Boolean published = false;

    @Column(name = "view_count")
    @Field(type = FieldType.Integer)
    private Integer viewCount = 0;

    // Énumérations
    public enum ContentType {
        VIDEO, ARTICLE, EXERCISE, QUIZ, RESOURCE, LIVE_SESSION, ASSIGNMENT
    }

    public enum DifficultyLevel {
        BEGINNER, INTERMEDIATE, ADVANCED, EXPERT
    }

    // Constructeurs
    public Content() {
        this.createdDate = LocalDateTime.now();
    }

    public Content(String title, ContentType type, Long courseId) {
        this();
        this.title = title;
        this.type = type;
        this.courseId = courseId;
    }

    // Méthodes utilitaires
    @PrePersist
    protected void onCreate() {
        if (createdDate == null) {
            createdDate = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedDate = LocalDateTime.now();
    }

    /**
     * Vérifie si le contenu est de type vidéo
     */
    public boolean isVideo() {
        return ContentType.VIDEO.equals(this.type);
    }

    /**
     * Vérifie si le contenu est interactif
     */
    public boolean isInteractive() {
        return Boolean.TRUE.equals(this.interactive);
    }

    /**
     * Obtient la durée formatée en format HH:MM:SS
     */
    public String getFormattedDuration() {
        if (duration == null || duration <= 0) {
            return "00:00:00";
        }
        
        int hours = duration / 3600;
        int minutes = (duration % 3600) / 60;
        int seconds = duration % 60;
        
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public ContentType getType() { return type; }
    public void setType(ContentType type) { this.type = type; }

    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }

    public Long getChapterId() { return chapterId; }
    public void setChapterId(Long chapterId) { this.chapterId = chapterId; }

    public String getContentData() { return contentData; }
    public void setContentData(String contentData) { this.contentData = contentData; }

    public String getTranscript() { return transcript; }
    public void setTranscript(String transcript) { this.transcript = transcript; }

    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }

    public DifficultyLevel getDifficulty() { return difficulty; }
    public void setDifficulty(DifficultyLevel difficulty) { this.difficulty = difficulty; }

    public Boolean getInteractive() { return interactive; }
    public void setInteractive(Boolean interactive) { this.interactive = interactive; }

    public Integer getOrderIndex() { return orderIndex; }
    public void setOrderIndex(Integer orderIndex) { this.orderIndex = orderIndex; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    public LocalDateTime getUpdatedDate() { return updatedDate; }
    public void setUpdatedDate(LocalDateTime updatedDate) { this.updatedDate = updatedDate; }

    public Boolean getPublished() { return published; }
    public void setPublished(Boolean published) { this.published = published; }

    public Integer getViewCount() { return viewCount; }
    public void setViewCount(Integer viewCount) { this.viewCount = viewCount; }

    // equals, hashCode et toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Content content = (Content) o;
        return Objects.equals(id, content.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Content{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", type=" + type +
                ", courseId=" + courseId +
                ", difficulty=" + difficulty +
                ", published=" + published +
                '}';
    }
}