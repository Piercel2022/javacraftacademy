package com.javacraftacademy.courseservice.entity;

import com.javacraftacademy.courseservice.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Entity
@Table(name = "lesson_notes")
@Data
@EqualsAndHashCode(callSuper = true)
@SQLDelete(sql = "UPDATE lesson_notes SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class LessonNote extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "video_timestamp")
    private Integer videoTimestamp; // en secondes, position dans la vidéo

    @Column(name = "is_public")
    private Boolean isPublic = false;

    @Column(name = "is_highlighted")
    private Boolean isHighlighted = false;

    @Column(name = "highlight_color")
    private String highlightColor;

    // Méthodes utilitaires
    public boolean hasVideoTimestamp() {
        return this.videoTimestamp != null && this.videoTimestamp >= 0;
    }

    public String getFormattedTimestamp() {
        if (videoTimestamp == null || videoTimestamp < 0) {
            return "0:00";
        }
        
        int minutes = videoTimestamp / 60;
        int seconds = videoTimestamp % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    public boolean isHighlightedNote() {
        return Boolean.TRUE.equals(this.isHighlighted);
    }

    public boolean isPublicNote() {
        return Boolean.TRUE.equals(this.isPublic);
    }

    @PrePersist
    @PreUpdate
    private void validateNote() {
        if (this.content == null || this.content.trim().isEmpty()) {
            throw new IllegalStateException("Note content cannot be empty");
        }
        
        if (this.content.length() > 5000) {
            throw new IllegalStateException("Note content cannot exceed 5000 characters");
        }
        
        if (this.videoTimestamp != null && this.videoTimestamp < 0) {
            throw new IllegalStateException("Video timestamp cannot be negative");
        }
    }
}