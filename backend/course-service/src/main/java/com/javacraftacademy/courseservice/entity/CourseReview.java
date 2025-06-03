// CourseReview.java - Avis sur les cours
package com.javacraftacademy.courseservice.entity;

import com.javacraftacademy.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Entity
@Table(name = "course_reviews", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"course_id", "student_id"}))
@Data
@EqualsAndHashCode(callSuper = true)
@SQLDelete(sql = "UPDATE course_reviews SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class CourseReview extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;
    
    @Column(name = "student_id", nullable = false)
    private Long studentId;
    
    @Column(nullable = false)
    private Integer rating; // 1-5 étoiles
    
    @Column(columnDefinition = "TEXT")
    private String comment;
    
    @Column(name = "is_verified")
    private Boolean isVerified = false; // Vérifié si l'étudiant a terminé le cours
    
    @Column(name = "is_featured")
    private Boolean isFeatured = false;
    
    @Column(name = "helpful_count")
    private Integer helpfulCount = 0;
    
    // Méthodes de validation
    @PrePersist
    @PreUpdate
    private void validateRating() {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("La note doit être comprise entre 1 et 5");
        }
    }
}
