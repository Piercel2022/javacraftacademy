package com.javacraftacademy.courseservice.entity;

// Imports JPA/Hibernate pour la persistance des données
import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

// Imports pour la validation des données
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Positive;

// Imports Lombok pour réduire le code boilerplate
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

// Imports pour la gestion des dates et utilitaires
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * Entité représentant un fichier attaché à une soumission d'assignment dans JavaCraftAcademy.
 * 
 * <h2>But de la classe :</h2>
 * Cette classe gère les métadonnées et les informations des fichiers soumis par les étudiants
 * dans le cadre d'assignments (devoirs, projets, exercices). Elle fait partie du système de
 * gestion de cours et permet de tracker, valider et organiser les fichiers soumis.
 * 
 * <h2>Fonctionnalités principales :</h2>
 * <ul>
 *   <li>Stockage des métadonnées de fichiers (nom, taille, type MIME, chemin)</li>
 *   <li>Association avec les soumissions d'assignments et les assignments</li>
 *   <li>Catégorisation des fichiers (ressources, documents, images, etc.)</li>
 *   <li>Validation automatique des contraintes de fichiers</li>
 *   <li>Soft delete pour la traçabilité des suppressions</li>
 *   <li>Méthodes utilitaires pour l'identification et le formatage</li>
 * </ul>
 * 
 * <h2>Relations dans l'architecture JavaCraftAcademy :</h2>
 * <ul>
 *   <li><strong>AssignmentSubmission</strong> : Relation Many-to-One - Un fichier appartient à une soumission</li>
 *   <li><strong>Assignment</strong> : Relation Many-to-One - Un fichier est lié à un assignment spécifique</li>
 *   <li><strong>BaseEntity</strong> : Héritage - Fournit les champs communs (id, dates, soft delete)</li>
 *   <li><strong>FileCategory</strong> : Énumération - Définit les catégories de fichiers acceptés</li>
 * </ul>
 * 
 * <h2>Utilisation dans l'écosystème :</h2>
 * - Utilisée par le CourseService pour gérer les soumissions d'étudiants
 * - Intégrée avec le système de stockage de fichiers (FileStorageService)
 * - Connectée au système de notation et d'évaluation
 * - Utilisée par les APIs REST pour l'upload et la récupération de fichiers
 * 
 * <h2>Extensions futures possibles :</h2>
 * - Ajout de prévisualisation automatique de fichiers
 * - Intégration avec des services de scan antivirus
 * - Versioning des fichiers
 * - Compression automatique
 * - Chiffrement des fichiers sensibles
 * 
 * @author JavaCraftAcademy Team
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(name = "submission_files", indexes = {
    @Index(name = "idx_submission_file_submission_id", columnList = "submission_id"),
    @Index(name = "idx_submission_file_assignment_id", columnList = "assignment_id"),
    @Index(name = "idx_submission_file_category", columnList = "file_category")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE submission_files SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class SubmissionFile extends BaseEntity {
    
    /**
     * Soumission à laquelle ce fichier appartient.
     * Relation Many-to-One avec chargement paresseux pour optimiser les performances.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id", nullable = false, 
                foreignKey = @ForeignKey(name = "fk_submission_file_submission"))
    @NotNull(message = "Le fichier doit appartenir à une soumission")
    private AssignmentSubmission submission;
    
    /**
     * Assignment auquel ce fichier est associé.
     * Permet de lier directement le fichier à l'assignment sans passer par la soumission.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_submission_file_assignment"))
    @NotNull(message = "Le fichier doit être associé à un assignment")
    private Assignment assignment;
    
    /**
     * Nom original du fichier tel que fourni par l'utilisateur.
     */
    @NotBlank(message = "Le nom du fichier ne peut pas être vide")
    @Size(max = 255, message = "Le nom du fichier ne peut pas dépasser 255 caractères")
    @Column(name = "file_name", nullable = false)
    private String fileName;
    
    /**
     * Chemin complet vers le fichier dans le système de stockage.
     * Peut être un chemin local ou une URL vers un service cloud.
     */
    @NotBlank(message = "Le chemin du fichier ne peut pas être vide")
    @Size(max = 500, message = "Le chemin du fichier ne peut pas dépasser 500 caractères")
    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;
    
    /**
     * Type MIME du fichier pour déterminer le type de contenu.
     */
    @Size(max = 100, message = "Le type MIME ne peut pas dépasser 100 caractères")
    @Column(name = "mime_type", length = 100)
    private String mimeType;
    
    /**
     * Taille du fichier en octets.
     */
    @Positive(message = "La taille du fichier doit être positive")
    @Column(name = "file_size")
    private Long fileSize;
    
    /**
     * Description optionnelle du fichier fournie par l'étudiant.
     */
    @Size(max = 1000, message = "La description ne peut pas dépasser 1000 caractères")
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    /**
     * Catégorie du fichier selon l'énumération FileCategory.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "file_category", nullable = false)
    @Builder.Default
    private FileCategory fileCategory = FileCategory.RESOURCE;
    
    /**
     * Indique si ce fichier est obligatoire pour la soumission.
     */
    @Column(name = "is_required")
    @Builder.Default
    private Boolean isRequired = false;
    
    /**
     * Date limite jusqu'à laquelle ce fichier peut être modifié ou remplacé.
     */
    @Column(name = "modification_deadline")
    private LocalDateTime modificationDeadline;
    
    // ========== MÉTHODES UTILITAIRES ==========
    
    /**
     * Vérifie si le fichier est un document textuel ou PDF.
     * Utile pour déterminer si le fichier peut être prévisualisé ou analysé.
     * 
     * @return true si le fichier est un document, false sinon
     */
    public boolean isDocument() {
        if (mimeType == null) return false;
        
        List<String> documentTypes = Arrays.asList(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "text/plain",
            "text/rtf"
        );
        
        return documentTypes.contains(mimeType.toLowerCase()) ||
               mimeType.toLowerCase().startsWith("text/");
    }
    
    /**
     * Vérifie si le fichier est une image.
     * Utilisé pour activer les fonctionnalités de prévisualisation d'images.
     * 
     * @return true si le fichier est une image, false sinon
     */
    public boolean isImage() {
        return mimeType != null && mimeType.toLowerCase().startsWith("image/");
    }
    
    /**
     * Vérifie si le fichier est un fichier de code source.
     * Basé sur l'extension du fichier et le type MIME.
     * 
     * @return true si le fichier est du code source, false sinon
     */
    public boolean isSourceCode() {
        if (fileName == null) return false;
        
        String lowerFileName = fileName.toLowerCase();
        List<String> codeExtensions = Arrays.asList(
            ".java", ".js", ".py", ".cpp", ".c", ".html", ".css", 
            ".xml", ".json", ".sql", ".php", ".rb", ".go", ".rs"
        );
        
        return codeExtensions.stream().anyMatch(lowerFileName::endsWith) ||
               (mimeType != null && mimeType.contains("text/"));
    }
    
    /**
     * Retourne la taille du fichier formatée en unités lisibles par l'humain.
     * 
     * @return Taille formatée (ex: "1.5 MB", "256 KB")
     */
    public String getFormattedFileSize() {
        if (fileSize == null || fileSize <= 0) return "Taille inconnue";
        
        long bytes = fileSize;
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024));
        return String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024));
    }
    
    /**
     * Retourne l'extension du fichier basée sur le nom.
     * 
     * @return Extension du fichier (sans le point) ou chaîne vide si aucune extension
     */
    public String getFileExtension() {
        if (fileName == null || !fileName.contains(".")) return "";
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }
    
    /**
     * Vérifie si le fichier peut encore être modifié selon la deadline.
     * 
     * @return true si le fichier est encore modifiable, false sinon
     */
    public boolean isModifiable() {
        return modificationDeadline == null || LocalDateTime.now().isBefore(modificationDeadline);
    }
    
    /**
     * Génère un nom unique pour le fichier basé sur l'ID et le timestamp.
     * Utile pour éviter les conflits de noms dans le système de stockage.
     * 
     * @return Nom unique du fichier
     */
    public String generateUniqueFileName() {
        String extension = getFileExtension();
        String baseName = fileName.substring(0, fileName.lastIndexOf('.') >= 0 ? 
                         fileName.lastIndexOf('.') : fileName.length());
        return String.format("%s_%d_%d.%s", 
                           baseName.replaceAll("[^a-zA-Z0-9]", "_"),
                           getId() != null ? getId() : 0,
                           System.currentTimeMillis(),
                           extension);
    }
    
    // ========== MÉTHODES DE VALIDATION PERSONNALISÉES ==========
    
    /**
     * Valide que la taille du fichier ne dépasse pas les limites configurées.
     * 
     * @param maxSizeInBytes Taille maximale autorisée en octets
     * @return true si la taille est valide, false sinon
     */
    public boolean isValidSize(long maxSizeInBytes) {
        return fileSize != null && fileSize > 0 && fileSize <= maxSizeInBytes;
    }
    
    /**
     * Valide que le type de fichier est autorisé selon les catégories.
     * 
     * @param allowedTypes Liste des types MIME autorisés
     * @return true si le type est autorisé, false sinon
     */
    public boolean isAllowedType(List<String> allowedTypes) {
        return mimeType != null && allowedTypes.contains(mimeType.toLowerCase());
    }
}

/**
 * Énumération définissant les catégories de fichiers acceptés dans le système.
 * 
 * Cette énumération permet de classifier les fichiers selon leur usage :
 * - RESOURCE : Fichiers de ressources générales
 * - DOCUMENT : Documents textuels (PDF, Word, etc.)
 * - IMAGE : Fichiers images
 * - SOURCE_CODE : Code source et fichiers de programmation
 * - ARCHIVE : Fichiers compressés (ZIP, RAR, etc.)
 * - MULTIMEDIA : Fichiers audio et vidéo
 * - PRESENTATION : Présentations (PowerPoint, etc.)
 * 
 * Chaque catégorie peut avoir ses propres règles de validation et de traitement.
 */
enum FileCategory {
    RESOURCE,       // Fichiers de ressources générales
    DOCUMENT,       // Documents (PDF, DOC, TXT)
    IMAGE,          // Images (JPEG, PNG, GIF)
    SOURCE_CODE,    // Code source (JAVA, JS, PY, etc.)
    ARCHIVE,        // Archives (ZIP, RAR, TAR)
    MULTIMEDIA,     // Audio/Vidéo (MP3, MP4, AVI)
    PRESENTATION    // Présentations (PPT, PPTX)
}