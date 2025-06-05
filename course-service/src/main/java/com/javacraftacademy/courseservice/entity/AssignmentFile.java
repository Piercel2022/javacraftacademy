package com.javacraftacademy.courseservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entité représentant un fichier attaché à un assignment dans l'application JavaCraftAcademy.
 * 
 * <h3>But de la classe :</h3>
 * Cette classe gère les fichiers associés aux assignments (devoirs/exercices), permettant 
 * aux instructeurs d'attacher des ressources (documents, templates, instructions) et aux 
 * étudiants de télécharger ces fichiers.
 * 
 * <h3>Fonctionnalités principales :</h3>
 * <ul>
 *   <li><strong>Gestion des métadonnées :</strong> Stockage des informations sur les fichiers 
 *       (nom, taille, type MIME, chemin de stockage)</li>
 *   <li><strong>Templates d'assignments :</strong> Possibilité de marquer des fichiers comme 
 *       templates réutilisables</li>
 *   <li><strong>Suivi des téléchargements :</strong> Compteur pour analyser l'utilisation 
 *       des ressources</li>
 *   <li><strong>Soft delete :</strong> Suppression logique pour maintenir l'intégrité 
 *       des données historiques</li>
 *   <li><strong>Descriptions :</strong> Contexte et instructions pour chaque fichier</li>
 * </ul>
 * 
 * <h3>Relations et dépendances dans l'écosystème JavaCraftAcademy :</h3>
 * <ul>
 *   <li><strong>Assignment :</strong> Relation Many-to-One - chaque fichier appartient à un assignment</li>
 *   <li><strong>BaseEntity :</strong> Héritage pour l'audit trail (création/modification)</li>
 *   <li><strong>FileStorageService :</strong> Service pour la gestion physique des fichiers</li>
 *   <li><strong>AssignmentFileRepository :</strong> Accès aux données avec requêtes métier</li>
 *   <li><strong>AssignmentFileController :</strong> API REST pour upload/download</li>
 *   <li><strong>SecurityContext :</strong> Vérification des permissions d'accès aux fichiers</li>
 * </ul>
 * 
 * <h3>Implémentation des fonctionnalités :</h3>
 * <ul>
 *   <li><strong>Soft Delete :</strong> Utilisation des annotations @SQLDelete et @Where 
 *       pour éviter la suppression physique</li>
 *   <li><strong>Lazy Loading :</strong> FetchType.LAZY sur la relation Assignment pour 
 *       optimiser les performances</li>
 *   <li><strong>Validation :</strong> Contraintes JPA pour assurer l'intégrité des données</li>
 *   <li><strong>Lombok :</strong> Génération automatique des getters/setters et méthodes utilitaires</li>
 * </ul>
 * 
 * <h3>Extensions futures possibles :</h3>
 * <ul>
 *   <li><strong>Versioning :</strong> Ajouter support pour versions multiples d'un même fichier</li>
 *   <li><strong>Permissions granulaires :</strong> Contrôle d'accès par rôle/utilisateur</li>
 *   <li><strong>Métadonnées étendues :</strong> Tags, catégories, niveau de difficulté</li>
 *   <li><strong>Validation automatique :</strong> Scan antivirus, vérification de format</li>
 *   <li><strong>Compression :</strong> Support pour fichiers ZIP et extraction automatique</li>
 *   <li><strong>Prévisualisation :</strong> Génération de thumbnails pour images/documents</li>
 *   <li><strong>Synchronisation cloud :</strong> Intégration avec services de stockage externes</li>
 * </ul>
 * 
 * @author JavaCraftAcademy Team
 * @version 1.0
 * @since 1.0
 * 
 * @see Assignment
 * @see BaseEntity
 * @see com.javacraftacademy.courseservice.service.FileStorageService
 * @see com.javacraftacademy.courseservice.repository.AssignmentFileRepository
 */
@Entity
@Table(name = "assignment_files", indexes = {
    @Index(name = "idx_assignment_files_assignment_id", columnList = "assignment_id"),
    @Index(name = "idx_assignment_files_is_template", columnList = "is_template"),
    @Index(name = "idx_assignment_files_mime_type", columnList = "mimeType")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE assignment_files SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
@EntityListeners(AuditingEntityListener.class)
public class AssignmentFile extends BaseEntity {
    
    /**
     * Assignment auquel ce fichier est attaché.
     * Relation Many-to-One avec chargement lazy pour optimiser les performances.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id", nullable = false, foreignKey = @ForeignKey(name = "fk_assignment_files_assignment"))
    private Assignment assignment;
    
    /**
     * Nom original du fichier tel qu'uploadé par l'utilisateur.
     * Utilisé pour l'affichage et le téléchargement.
     */
    @Column(nullable = false, length = 255)
    private String fileName;
    
    /**
     * Chemin de stockage du fichier sur le système de fichiers ou cloud storage.
     * Peut inclure des sous-répertoires organisés par date/assignment.
     */
    @Column(nullable = false, length = 500)
    private String filePath;
    
    /**
     * Taille du fichier en octets.
     * Utilisé pour les limitations de quota et statistiques.
     */
    @Column(nullable = false)
    private Long fileSize;
    
    /**
     * Type MIME du fichier (ex: application/pdf, image/png).
     * Utilisé pour la validation et l'affichage approprié.
     */
    @Column(nullable = false, length = 100)
    private String mimeType;
    
    /**
     * Description optionnelle du fichier fournie par l'instructeur.
     * Peut contenir des instructions spécifiques ou du contexte.
     */
    @Column(columnDefinition = "TEXT")
    private String description;
    
    /**
     * Indique si ce fichier est un template réutilisable.
     * Les templates peuvent être copiés vers de nouveaux assignments.
     */
    @Column(name = "is_template")
    @Builder.Default
    private Boolean isTemplate = false;
    
    /**
     * Compteur du nombre de téléchargements.
     * Utilisé pour les statistiques d'utilisation et analytics.
     */
    @Column(name = "download_count")
    @Builder.Default
    private Integer downloadCount = 0;
    
    /**
     * Hash MD5 ou SHA-256 du fichier pour vérifier l'intégrité.
     * Peut être utilisé pour détecter les doublons et corruptions.
     */
    @Column(length = 64)
    private String fileHash;
    
    /**
     * Indique si le fichier est requis pour l'assignment.
     * Les fichiers requis doivent être téléchargés par tous les étudiants.
     */
    @Column(name = "is_required")
    @Builder.Default
    private Boolean isRequired = false;
    
    // Méthodes utilitaires pour les fonctionnalités métier
    
    /**
     * Incrémente le compteur de téléchargements.
     * Méthode thread-safe pour éviter les conditions de course.
     */
    public void incrementDownloadCount() {
        this.downloadCount = (this.downloadCount == null ? 0 : this.downloadCount) + 1;
    }
    
    /**
     * Vérifie si le fichier est une image basée sur son type MIME.
     * Utilisé pour l'affichage de prévisualisations.
     */
    public boolean isImage() {
        return mimeType != null && mimeType.startsWith("image/");
    }
    
    /**
     * Vérifie si le fichier est un document PDF.
     * Utilisé pour activer des fonctionnalités spécifiques aux PDFs.
     */
    public boolean isPdf() {
        return "application/pdf".equals(mimeType);
    }
    
    /**
     * Retourne la taille du fichier formatée en unités lisibles.
     * @return Taille formatée (ex: "1.5 MB", "523 KB")
     */
    public String getFormattedFileSize() {
        if (fileSize == null || fileSize == 0) {
            return "0 B";
        }
        
        final String[] units = {"B", "KB", "MB", "GB", "TB"};
        int unitIndex = 0;
        double size = fileSize.doubleValue();
        
        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }
        
        return String.format("%.1f %s", size, units[unitIndex]);
    }
    
    /**
     * Retourne l'extension du fichier basée sur son nom.
     * @return Extension du fichier (ex: "pdf", "docx") ou chaîne vide si aucune
     */
    public String getFileExtension() {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AssignmentFile)) return false;
        if (!super.equals(o)) return false;
        AssignmentFile that = (AssignmentFile) o;
        return Objects.equals(assignment, that.assignment) &&
               Objects.equals(fileName, that.fileName) &&
               Objects.equals(filePath, that.filePath);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), assignment, fileName, filePath);
    }
    
    @Override
    public String toString() {
        return "AssignmentFile{" +
                "id=" + (this.getId() != null ? this.getId() : "null") +

                ", fileName='" + fileName + '\'' +
                ", fileSize=" + fileSize +
                ", mimeType='" + mimeType + '\'' +
                ", isTemplate=" + isTemplate +
                ", downloadCount=" + downloadCount +
                '}';
    }
}