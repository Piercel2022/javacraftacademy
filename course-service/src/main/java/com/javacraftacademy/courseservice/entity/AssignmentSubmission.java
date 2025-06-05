// AssignmentSubmission.java - Soumissions d'assignments
package com.javacraftacademy.courseservice.entity;

import com.javacraftacademy.courseservice.entity.BaseEntity;
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
        if (grade == null || assignment == null) {
            return false;
        }
        
        // Option 1: Si Assignment a un champ maxPoints, utilisez un pourcentage
        Integer maxPoints = assignment.getMaxPoints();
        if (maxPoints == null || maxPoints == 0) {
            return false;
        }
        
        // Considère comme réussi si >= 60% des points maximum
        double passingThreshold = maxPoints * 0.6;
        return grade >= passingThreshold;
        
        // Option 2: Si vous voulez utiliser une valeur fixe
        // return grade >= 60; // ou toute autre valeur de seuil
        
        // Option 3: Si Assignment devrait avoir un champ passingPoints
        // Ajoutez ce champ à l'entité Assignment et décommentez:
        // Integer passingPoints = assignment.getPassingPoints();
        // if (passingPoints == null) {
        //     return false;
        // }
        // return grade >= passingPoints;
    }
    
    public boolean isGraded() {
        return status == SubmissionStatus.GRADED;
    }
}