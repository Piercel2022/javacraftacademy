package com.javacraftacademy.courseservice.entity;

import com.javacraftacademy.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tags")
@Data
@EqualsAndHashCode(callSuper = true)
@SQLDelete(sql = "UPDATE tags SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class Tag extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(unique = true, nullable = false, length = 60)
    private String slug;

    @Column(length = 300)
    private String description;

    @Column(name = "color_code", length = 7)
    private String colorCode; // Format hex: #FFFFFF

    @Enumerated(EnumType.STRING)
    @Column(name = "tag_type")
    private TagType tagType = TagType.GENERAL;

    @Column(name = "is_popular")
    private Boolean isPopular = false;

    @Column(name = "usage_count")
    private Integer usageCount = 0;

    @ManyToMany(mappedBy = "tags")
    private Set<Course> courses = new HashSet<>();

    public enum TagType {
        GENERAL, TECHNOLOGY, SKILL, LEVEL, LANGUAGE, FRAMEWORK, TOOL, INDUSTRY
    }

    // Méthodes utilitaires
    public void incrementUsageCount() {
        this.usageCount = (this.usageCount == null ? 0 : this.usageCount) + 1;
    }

    public void decrementUsageCount() {
        this.usageCount = Math.max(0, (this.usageCount == null ? 0 : this.usageCount) - 1);
    }

    public boolean isPopularTag() {
        return Boolean.TRUE.equals(this.isPopular) || (this.usageCount != null && this.usageCount >= 10);
    }

    public int getCoursesCount() {
        return this.courses != null ? this.courses.size() : 0;
    }

    public boolean isUsed() {
        return getCoursesCount() > 0;
    }

    @PrePersist
    @PreUpdate
    private void validateTag() {
        if (this.name == null || this.name.trim().isEmpty()) {
            throw new IllegalStateException("Tag name cannot be empty");
        }
        
        if (this.slug == null || this.slug.trim().isEmpty()) {
            throw new IllegalStateException("Tag slug cannot be empty");
        }
        
        // Validation du code couleur
        if (this.colorCode != null && !this.colorCode.matches("^#[0-9A-Fa-f]{6}$")) {
            throw new IllegalStateException("Color code must be in hex format (#RRGGBB)");
        }
        
        // Normaliser le nom du tag (minuscules, pas d'espaces multiples)
        this.name = this.name.trim().toLowerCase().replaceAll("\\s+", " ");
    }
}