// LessonProgress.java - Progression des étudiants dans les leçons
package com.javacraftacademy.courseservice.entity;

import com.javacraftacademy.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;

@Entity
@Table(name = "lesson_progresses", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"lesson_id", "student_id"}))
@Data
@EqualsAndHashCode(callSuper = true)
@SQLDelete(sql = "UPDATE lesson_progresses SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class LessonProgress extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;
    
    @Column(name = "student_id", nullable = false)
    private Long studentId;
    
    @Column(name = "completion_percentage", nullable = false)
    private Integer completionPercentage = 0;
    
    @Column(name = "is_completed")
    private Boolean isCompleted = false;
    
    @Column(name = "completed_date")
    private LocalDateTime completedDate;
    
    @Column(name = "last_accessed_date")
    private LocalDateTime lastAccessedDate;
    
    @Column(name = "watch_time")
    private Integer watchTime = 0; // en secondes pour les vidéos
    
    @Column(name = "last_position")
    private Integer lastPosition = 0; // position dans la vidéo/contenu
    
    // Méthodes utilitaires
    public void markAsCompleted() {
        this.isCompleted = true;
        this.completionPercentage = 100;
        this.completedDate = LocalDateTime.now();
    }
    
    public void updateProgress(int percentage) {
        this.completionPercentage = Math.min(100, Math.max(0, percentage));
        this.lastAccessedDate = LocalDateTime.now();
        
        if (this.completionPercentage >= 100 && !Boolean.TRUE.equals(this.isCompleted)) {
            markAsCompleted();
        }
    }
}
