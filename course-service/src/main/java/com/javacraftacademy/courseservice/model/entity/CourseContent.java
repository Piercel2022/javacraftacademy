package com.javacraftacademy.courseservice.model.entity;

import com.javacraftacademy.courseservice.model.enums.ContentType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entité JPA représentant le contenu multimédia d'un cours dans JavaCraftAcademy.
 * 
 * <p>Cette classe gère les ressources numériques associées aux cours, permettant
 * un stockage flexible et une gestion avancée des médias éducatifs. Elle supporte
 * différents types de contenu (vidéos, documents, images, exercices interactifs)
 * avec métadonnées complètes pour l'optimisation et l'accessibilité.</p>
 * 
 * <h3>Types de contenu supportés :</h3>
 * <ul>
 *   <li><strong>VIDEO</strong> : Cours vidéo avec chapitrages et sous-titres</li>
 *   <li><strong>DOCUMENT</strong> : PDF, slides, guides de référence</li>
 *   <li><strong>IMAGE</strong> : Diagrammes, captures d'écran, illustrations</li>
 *   <li><strong>AUDIO</strong> : Podcasts, explications orales</li>
 *   <li><strong>INTERACTIVE</strong> : Quiz, exercices, simulations</li>
 *   <li><strong>CODE_SAMPLE</strong> : Exemples de code avec syntaxe highlighting</li>
 *   <li><strong>ARCHIVE</strong> : Fichiers ZIP avec projets complets</li>
 * </ul>
 * 
 * <h3>Fonctionnalités avancées :</h3>
 * <ul>
 *   <li>Gestion des versions de contenu pour mises à jour transparentes</li>
 *   <li>Support multi-format avec conversion automatique</li>
 *   <li>Optimisation automatique selon la bande passante</li>
 *   <li>Génération de miniatures et previews</li>
 *   <li>Analyse de qualité et conformité pédagogique</li>
 *   <li>Système de tags pour recherche et classification</li>
 *   <li>Métriques d'engagement et d'utilisation</li>
 * </ul>
 * 
 * <h3>Intégration système :</h3>
 * <ul>
 *   <li><strong>File Storage Service</strong> : Stockage distribué (AWS S3, Azure Blob)</li>
 *   <li><strong>CDN Integration</strong> : Diffusion optimisée mondiale</li>
 *   <li><strong>Transcoding Service</strong> : Conversion multi-format automatique</li>
 *   <li><strong>Analytics Service</strong> : Suivi d'utilisation et performance</li>
 *   <li><strong>Security Service</strong> : Protection anti-piratage et DRM</li>
 *   <li><strong>Search Service</strong> : Indexation pour recherche full-text</li>
 * </ul>
 * 
 * <h3>Optimisations performance :</h3>
 * <ul>
 *   <li>Lazy loading des contenus volumineux</li>
 *   <li>Cache distribué pour métadonnées fréquentes</li>
 *   <li>Compression adaptative selon le client</li>
 *   <li>Streaming progressif pour vidéos</li>
 *   <li>Prefetch intelligent des ressources suivantes</li>
 * </ul>
 * 
 * @author JavaCraftAcademy Team
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(name = "course_contents",
       indexes = {
           @Index(name = "idx_content_lesson", columnList = "lesson_id"),
           @Index(name = "idx_content_type", columnList = "content_type"),
           @Index(name = "idx_content_order", columnList = "lesson_id, display_order"),
           @Index(name = "idx_content_active", columnList = "is_active"),
           @Index(name = "idx_content_created", columnList = "created_at"),
           @Index(name = "idx_content_size", columnList = "file_size"),
           @Index(name = "idx_content_duration", columnList = "duration_seconds")
       })
public class CourseContent {

    /**
     * Identifiant unique du contenu.
     * Clé primaire auto-générée pour référencement.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Leçon à laquelle appartient ce contenu.
     * Relation Many-to-One - une leçon peut avoir plusieurs contenus.
     */
    @NotNull(message = "La leçon est obligatoire")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    /**
     * Type de contenu multimédia.
     * Détermine le traitement et l'affichage du contenu.
     */
    @NotNull(message = "Le type de contenu est obligatoire")
    @Enumerated(EnumType.STRING)
    @Column(name = "content_type", nullable = false, length = 20)
    private ContentType contentType;

    /**
     * Titre descriptif du contenu.
     * Affiché dans l'interface utilisateur.
     */
    @NotBlank(message = "Le titre ne peut pas être vide")
    @Size(max = 255, message = "Le titre ne peut pas dépasser 255 caractères")
    @Column(name = "title", nullable = false)
    private String title;

    /**
     * Description détaillée du contenu.
     * Aide pédagogique et contexte d'utilisation.
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * URL de stockage du fichier principal.
     * Référence vers le système de stockage (S3, Azure, etc.).
     */
    @Column(name = "file_url", nullable = false)
    private String fileUrl;

    /**
     * URL de la miniature/aperçu.
     * Générée automatiquement pour prévisualisations.
     */
    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    /**
     * Nom original du fichier uploadé.
     * Conservé pour référence et téléchargements.
     */
    @Column(name = "original_filename")
    private String originalFilename;

    /**
     * Type MIME du fichier.
     * Utilisé pour la validation et l'affichage correct.
     */
    @Column(name = "mime_type", length = 100)
    private String mimeType;

    /**
     * Taille du fichier en octets.
     * Pour gestion du stockage et estimation de bande passante.
     */
    @Min(value = 0, message = "La taille du fichier ne peut pas être négative")
    @Column(name = "file_size")
    private Long fileSize;

    /**
     * Durée du contenu en secondes (pour vidéos/audios).
     * Utilisée pour planification temporelle du cours.
     */
    @Min(value = 0, message = "La durée ne peut pas être négative")
    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    /**
     * Ordre d'affichage dans la leçon.
     * Permet l'organisation séquentielle du contenu.
     */
    @Min(value = 0, message = "L'ordre d'affichage ne peut pas être négatif")
    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    /**
     * Indique si le contenu est actif et visible.
     * Permet la gestion du cycle de vie du contenu.
     */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    /**
     * Indique si le contenu nécessite une authentification.
     * Pour contenu premium ou restreint.
     */
    @Column(name = "requires_authentication", nullable = false)
    private Boolean requiresAuthentication = false;

    /**
     * Métadonnées additionnelles au format JSON.
     * Stockage flexible d'informations spécifiques au type.
     */
    @Column(name = "metadata", columnDefinition = "JSON")
    private String metadata;

    /**
     * Tags pour classification et recherche.
     * Séparés par des virgules pour simplicité.
     */
    @Column(name = "tags")
    private String tags;

    /**
     * Version du contenu.
     * Permet le versioning et les mises à jour.
     */
    @Column(name = "version", length = 10)
    private String version = "1.0";

    /**
     * Checksums MD5 pour vérification d'intégrité.
     * Sécurité et détection de corruption.
     */
    @Column(name = "checksum", length = 32)
    private String checksum;

    /**
     * Date et heure de création du contenu.
     * Gérée automatiquement par Hibernate.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Date et heure de dernière modification.
     * Mise à jour automatique à chaque modification.
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Date de dernière modification du fichier.
     * Pour synchronisation avec stockage externe.
     */
    @Column(name = "file_modified_at")
    private LocalDateTime fileModifiedAt;

    // Constructeurs
    
    /**
     * Constructeur par défaut requis par JPA.
     */
    public CourseContent() {
    }

    /**
     * Constructeur avec paramètres essentiels.
     * 
     * @param lesson La leçon associée
     * @param contentType Le type de contenu
     * @param title Le titre du contenu
     * @param fileUrl L'URL du fichier
     * @param displayOrder L'ordre d'affichage
     */
    public CourseContent(Lesson lesson, ContentType contentType, String title, 
                        String fileUrl, Integer displayOrder) {
        this.lesson = lesson;
        this.contentType = contentType;
        this.title = title;
        this.fileUrl = fileUrl;
        this.displayOrder = displayOrder;
        this.isActive = true;
        this.requiresAuthentication = false;
        this.version = "1.0";
    }

    // Getters et Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Lesson getLesson() {
        return lesson;
    }

    public void setLesson(Lesson lesson) {
        this.lesson = lesson;
    }

    public ContentType getContentType() {
        return contentType;
    }

    public void setContentType(ContentType contentType) {
        this.contentType = contentType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public Integer getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(Integer durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Boolean getRequiresAuthentication() {
        return requiresAuthentication;
    }

    public void setRequiresAuthentication(Boolean requiresAuthentication) {
        this.requiresAuthentication = requiresAuthentication;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getFileModifiedAt() {
        return fileModifiedAt;
    }

    public void setFileModifiedAt(LocalDateTime fileModifiedAt) {
        this.fileModifiedAt = fileModifiedAt;
    }

    // Méthodes utilitaires

    /**
     * Vérifie si le contenu est une vidéo.
     * 
     * @return true si le type est VIDEO
     */
    public boolean isVideo() {
        return ContentType.VIDEO.equals(this.contentType);
    }

    /**
     * Vérifie si le contenu est un document.
     * 
     * @return true si le type est DOCUMENT
     */
    public boolean isDocument() {
        return ContentType.DOCUMENT.equals(this.contentType);
    }

    /**
     * Vérifie si le contenu a une durée définie.
     * 
     * @return true si durationSeconds est défini et > 0
     */
    public boolean hasDuration() {
        return durationSeconds != null && durationSeconds > 0;
    }

    /**
     * Formate la durée en format lisible (MM:SS ou HH:MM:SS).
     * 
     * @return La durée formatée ou "N/A" si non définie
     */
    public String getFormattedDuration() {
        if (durationSeconds == null || durationSeconds <= 0) {
            return "N/A";
        }
        
        int hours = durationSeconds / 3600;
        int minutes = (durationSeconds % 3600) / 60;
        int seconds = durationSeconds % 60;
        
        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%02d:%02d", minutes, seconds);
        }
    }

    /**
     * Formate la taille du fichier en format lisible.
     * 
     * @return La taille formatée (KB, MB, GB) ou "N/A" si non définie
     */
    public String getFormattedFileSize() {
        if (fileSize == null || fileSize <= 0) {
            return "N/A";
        }
        
        if (fileSize < 1024) {
            return fileSize + " B";
        } else if (fileSize < 1024 * 1024) {
            return String.format("%.1f KB", fileSize / 1024.0);
        } else if (fileSize < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", fileSize / (1024.0 * 1024.0));
        } else {
            return String.format("%.1f GB", fileSize / (1024.0 * 1024.0 * 1024.0));
        }
    }

    // Méthodes Object

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CourseContent that = (CourseContent) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "CourseContent{" +
                "id=" + id +
                ", contentType=" + contentType +
                ", title='" + title + '\'' +
                ", fileUrl='" + fileUrl + '\'' +
                ", displayOrder=" + displayOrder +
                ", isActive=" + isActive +
                ", createdAt=" + createdAt +
                '}';
    }
}