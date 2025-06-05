// SubmissionFile.java - Fichiers des soumissions
package com.javacraftacademy.courseservice.entity;

import com.javacraftacademy.courseservice.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Entity
@Table(name = "submission_files")
@Data
@EqualsAndHashCode(callSuper = true)
@SQLDelete(sql = "UPDATE submission_files SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class SubmissionFile extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id", nullable = false)
    private AssignmentSubmission submission;
    
    @Column(nullable = false, length = 255)
    private String fileName;
    
    @Column(nullable = false, length = 500)
    private String filePath;
    
    @Column(nullable = false)
    private Long fileSize;
    
    @Column(nullable = false, length = 100)
    private String mimeType;
    
    @Column(columnDefinition = "TEXT")
    private String description;
}