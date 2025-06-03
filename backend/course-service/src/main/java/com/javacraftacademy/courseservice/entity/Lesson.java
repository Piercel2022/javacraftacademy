// Lesson.java - Leçons du cours
package com.javacraftacademy.courseservice.entity;

import com.javacraftacademy.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "lessons")
@Data
@EqualsAndHashCode(callSuper = true)
@SQLDelete(sql = "UPDATE lessons SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class Lesson extends BaseEntity {
    
    @Column(nullable = false, length = 200)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(columnDefinition = "LONGTEXT")
    private String content;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;
    
    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LessonType lessonType = LessonType.TEXT;
    
    @Column(name = "video_url")
    private String videoUrl;
    
    @Column(name = "video_duration")
    private Integer videoDuration; // en secondes
    
    @Column(name = "is_free")
    private Boolean isFree = false;
    
    @Column(name = "is_published")
    private Boolean isPublished = false;
    
    @Column(name = "published_date")
    private LocalDateTime publishedDate;
    
    @Column(name = "estimated_duration")
    private Integer estimatedDuration; // en minutes
    
    @OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Assignment> assignments = new ArrayList<>();
    
    @OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LessonProgress> lessonProgresses = new ArrayList<>();
    
    // Méthodes utilitaires
    public boolean isAccessible() {
        return Boolean.TRUE.equals(isPublished) && 
               (publishedDate == null || publishedDate.isBefore(LocalDateTime.now()));
    }
}