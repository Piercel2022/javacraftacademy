package com.javacraftacademy.courseservice.entity;

import com.javacraftacademy.courseservice.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Entity
@Table(name = "lesson_resources")
@Data
@EqualsAndHashCode(callSuper = true)
@SQLDelete(sql = "UPDATE lesson_resources SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class LessonResource extends BaseEntity {

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 500)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ResourceType type;

    @Column(name = "file_url")
    private String fileUrl;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "file_size")
    private Long fileSize; // en bytes

    @Column(name = "mime_type")
    private String mimeType;

    @Column(name = "external_url")
    private String externalUrl;

    @Column(name = "order_index")
    private Integer orderIndex;

    @Column(name = "is_downloadable")
    private Boolean isDownloadable = true;

    @Column(name = "download_count")
    private Integer downloadCount = 0;

    public enum ResourceType {
        PDF, DOC, SLIDE, CODE, LINK, VIDEO, AUDIO, IMAGE, ARCHIVE, OTHER
    }

    // Méthodes utilitaires
    public void incrementDownloadCount() {
        this.downloadCount = (this.downloadCount == null ? 0 : this.downloadCount) + 1;
    }

    public boolean isExternalResource() {
        return this.externalUrl != null && !this.externalUrl.trim().isEmpty();
    }

    public boolean hasFile() {
        return this.fileUrl != null && !this.fileUrl.trim().isEmpty() && this.fileName != null;
    }

    public String getFormattedFileSize() {
        if (fileSize == null || fileSize == 0) {
            return "0 B";
        }

        String[] units = {"B", "KB", "MB", "GB"};
        int unitIndex = 0;
        double size = fileSize.doubleValue();

        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }

        return String.format("%.1f %s", size, units[unitIndex]);
    }

    @PrePersist
    @PreUpdate
    private void validateResource() {
        if (ResourceType.LINK.equals(this.type)) {
            if (this.externalUrl == null || this.externalUrl.trim().isEmpty()) {
                throw new IllegalStateException("External URL is required for LINK resources");
            }
        } else {
            if (this.fileUrl == null || this.fileUrl.trim().isEmpty()) {
                throw new IllegalStateException("File URL is required for file resources");
            }
        }
    }
}