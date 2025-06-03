// AssignmentFile.java - Fichiers attachés aux assignments
package com.javacraftacademy.courseservice.entity;

import com.javacraftacademy.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Entity
@Table(name = "assignment_files")
@Data
@EqualsAndHashCode(callSuper = true)
@SQLDelete(sql = "UPDATE assignment_files SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class AssignmentFile extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id", nullable = false)
    private Assignment assignment;
    
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
    
    @Column(name = "is_template")
    private Boolean isTemplate = false;
    
    @Column(name = "download_count")
    private Integer downloadCount = 0;
}