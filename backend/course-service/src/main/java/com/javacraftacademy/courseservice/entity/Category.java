package com.javacraftacademy.courseservice.entity;

import com.javacraftacademy.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categories")
@Data
@EqualsAndHashCode(callSuper = true)
@SQLDelete(sql = "UPDATE categories SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class Category extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(unique = true, nullable = false, length = 150)
    private String slug;

    @Column(length = 500)
    private String description;

    @Column(name = "icon_url")
    private String iconUrl;

    @Column(name = "color_code", length = 7)
    private String colorCode; // Format hex: #FFFFFF

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_category_id")
    private Category parentCategory;

    @OneToMany(mappedBy = "parentCategory", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC, name ASC")
    private List<Category> subCategories = new ArrayList<>();

    @OneToMany(mappedBy = "category", cascade = CascadeType.REMOVE)
    private List<Course> courses = new ArrayList<>();

    @Column(name = "order_index")
    private Integer orderIndex = 0;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "is_featured")
    private Boolean isFeatured = false;

    @Column(name = "courses_count")
    private Integer coursesCount = 0;

    @Column(name = "total_enrollments")
    private Integer totalEnrollments = 0;

    @Column(name = "meta_title")
    private String metaTitle;

    @Column(name = "meta_description", length = 500)
    private String metaDescription;

    @Column(name = "meta_keywords")
    private String metaKeywords;

    // Méthodes utilitaires
    public boolean isRootCategory() {
        return this.parentCategory == null;
    }

    public boolean hasSubCategories() {
        return this.subCategories != null && !this.subCategories.isEmpty();
    }

    public boolean isLeafCategory() {
        return !hasSubCategories();
    }

    public int getLevel() {
        int level = 0;
        Category parent = this.parentCategory;
        while (parent != null) {
            level++;
            parent = parent.getParentCategory();
        }
        return level;
    }

    public String getFullPath() {
        StringBuilder path = new StringBuilder();
        List<String> names = new ArrayList<>();
        
        Category current = this;
        while (current != null) {
            names.add(0, current.getName());
            current = current.getParentCategory();
        }
        
        return String.join(" > ", names);
    }

    public List<Category> getAllSubCategories() {
        List<Category> allSubs = new ArrayList<>();
        for (Category sub : this.subCategories) {
            allSubs.add(sub);
            allSubs.addAll(sub.getAllSubCategories());
        }
        return allSubs;
    }

    public void incrementCoursesCount() {
        this.coursesCount = (this.coursesCount == null ? 0 : this.coursesCount) + 1;
    }

    public void decrementCoursesCount() {
        this.coursesCount = Math.max(0, (this.coursesCount == null ? 0 : this.coursesCount) - 1);
    }

    public void incrementTotalEnrollments(int count) {
        this.totalEnrollments = (this.totalEnrollments == null ? 0 : this.totalEnrollments) + count;
    }

    public boolean isActiveCategoryTree() {
        if (!Boolean.TRUE.equals(this.isActive)) {
            return false;
        }
        
        Category parent = this.parentCategory;
        while (parent != null) {
            if (!Boolean.TRUE.equals(parent.isActive)) {
                return false;
            }
            parent = parent.getParentCategory();
        }
        return true;
    }

    @PrePersist
    @PreUpdate
    private void validateCategory() {
        if (this.name == null || this.name.trim().isEmpty()) {
            throw new IllegalStateException("Category name cannot be empty");
        }
        
        if (this.slug == null || this.slug.trim().isEmpty()) {
            throw new IllegalStateException("Category slug cannot be empty");
        }
        
        // Vérifier qu'une catégorie ne peut pas être son propre parent
        if (this.parentCategory != null && this.parentCategory.equals(this)) {
            throw new IllegalStateException("A category cannot be its own parent");
        }
        
        // Validation du code couleur
        if (this.colorCode != null && !this.colorCode.matches("^#[0-9A-Fa-f]{6}$")) {
            throw new IllegalStateException("Color code must be in hex format (#RRGGBB)");
        }
    }
}