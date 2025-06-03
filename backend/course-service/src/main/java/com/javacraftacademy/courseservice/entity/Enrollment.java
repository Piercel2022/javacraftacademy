// Enrollment.java - Inscriptions aux cours
package com.javacraftacademy.courseservice.entity;

import com.javacraftacademy.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "enrollments", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"course_id", "student_id"}))
@Data
@EqualsAndHashCode(callSuper = true)
@SQLDelete(sql = "UPDATE enrollments SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class Enrollment extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;
    
    @Column(name = "student_id", nullable = false)
    private Long studentId;
    
    @Column(name = "enrollment_date", nullable = false)
    private LocalDateTime enrollmentDate = LocalDateTime.now();
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EnrollmentStatus status = EnrollmentStatus.ACTIVE;
    
    @Column(name = "completion_percentage")
    private Integer completionPercentage = 0;
    
    @Column(name = "completed_date")
    private LocalDateTime completedDate;
    
    @Column(name = "certificate_issued")
    private Boolean certificateIssued = false;
    
    @Column(name = "certificate_issued_date")
    private LocalDateTime certificateIssuedDate;
    
    @Column(name = "last_accessed_date")
    private LocalDateTime lastAccessedDate;
    
    @Column(name = "paid_amount", precision = 10, scale = 2)
    private BigDecimal paidAmount;
    
    @Column(name = "payment_date")
    private LocalDateTime paymentDate;
    
    @Column(name = "final_grade")
    private Integer finalGrade;
    
    @Column(columnDefinition = "TEXT")
    private String notes;
    
    // Méthodes utilitaires
    public boolean isActive() {
        return status == EnrollmentStatus.ACTIVE;
    }
    
    public boolean isCompleted() {
        return status == EnrollmentStatus.COMPLETED || 
               (completionPercentage != null && completionPercentage >= 100);
    }
    
    public void markAsCompleted() {
        this.status = EnrollmentStatus.COMPLETED;
        this.completionPercentage = 100;
        this.completedDate = LocalDateTime.now();
    }
}
