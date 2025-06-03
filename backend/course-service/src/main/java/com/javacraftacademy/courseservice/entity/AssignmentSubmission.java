// AssignmentSubmission.java - Soumissions d'assignments
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
@Table(name = "assignment_submissions")
@Data
@EqualsAndHashCode(callSuper = true)
@SQLDelete(sql = "UPDATE assignment_submissions SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class AssignmentSubmission extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id", nullable = false)
    private Assignment assignment;
    
    @Column(name = "student_id", nullable = false)
    private Long studentId;
    
    @Column(columnDefinition = "TEXT")
    private String content;
    
    @Column(name = "submission_date", nullable = false)
    private LocalDateTime submissionDate = LocalDateTime.now();
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubmissionStatus status = SubmissionStatus.SUBMITTED;
    
    @Column(name = "grade")
    private Integer grade;
    
    @Column(name = "graded_by")
    private Long gradedBy;
    
    @Column(name = "graded_date")
    private LocalDateTime gradedDate;
    
    @Column(columnDefinition = "TEXT")
    private String feedback;
    
    @Column(name = "is_late")
    private Boolean isLate = false;
    
    @Column(name = "attempt_number")
    private Integer attemptNumber = 1;
    
    @OneToMany(mappedBy = "submission", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SubmissionFile> files = new ArrayList<>();
    
    // Méthodes utilitaires
    public boolean isPassed() {
        if (grade == null || assignment == null || assignment.getPassingPoints() == null) {
            return false;
        }
        return grade >= assignment.getPassingPoints();
    }
    
    public boolean isGraded() {
        return status == SubmissionStatus.GRADED;
    }
}