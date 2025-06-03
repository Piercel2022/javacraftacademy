// Localisation: course-service/src/main/java/com/javacraftacademy/courseservice/entity/Assignement.java
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
@Table(name = "assignments")
@Data
@EqualsAndHashCode(callSuper = true)
@SQLDelete(sql = "UPDATE assignments SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class Assignment extends BaseEntity {
    
    @Column(nullable = false, length = 200)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(columnDefinition = "TEXT", nullable = false)
    private String instructions;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id")
    private Lesson lesson;
    
    @Column(name = "order_index")
    private Integer orderIndex;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssignmentType assignmentType = AssignmentType.PROJECT;
    
    @Column(name = "max_points", nullable = false)
    private Integer maxPoints = 100;
    
    @Column(name = "passing_points")
    private Integer passingPoints;
    
    @Column(name = "due_date")
    private LocalDateTime dueDate;
    
    @Column(name = "late_submission_allowed")
    private Boolean lateSubmissionAllowed = false;
    
    @Column(name = "late_penalty_percentage")
    private Integer latePenaltyPercentage = 0;
    
    @Column(name = "auto_grade")
    private Boolean autoGrade = false;
    
    @Column(name = "submission_limit")
    private Integer submissionLimit;
    
    @Column(columnDefinition = "TEXT")
    private String rubric;
    
    @OneToMany(mappedBy = "assignment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AssignmentSubmission> submissions = new ArrayList<>();
    
    @OneToMany(mappedBy = "assignment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AssignmentFile> files = new ArrayList<>();
    
    // Méthodes utilitaires
    public boolean isOverdue() {
        return dueDate != null && LocalDateTime.now().isAfter(dueDate);
    }
    
    public boolean isSubmissionAllowed() {
        if (isOverdue() && !Boolean.TRUE.equals(lateSubmissionAllowed)) {
            return false;
        }
        return true;
    }
}