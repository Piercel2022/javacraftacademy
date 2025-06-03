// Localisation: course-service/src/main/java/com/javacraftacademy/courseservice/entity/Course.java
package com.javacraftacademy.courseservice.entity;

import com.javacraftacademy.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "courses")
@Data
@EqualsAndHashCode(callSuper = true)
@SQLDelete(sql = "UPDATE courses SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class Course extends BaseEntity {

    @Column(nullable = false, length = 200)
    private String title;

    @Column(unique = true, nullable = false, length = 250)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "short_description", length = 500)
    private String shortDescription;

    @Column(name = "cover_image_url")
    private String coverImageUrl;

    @Column(name = "trailer_video_url")
    private String trailerVideoUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CourseLevel level;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CourseStatus status;

    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "discounted_price", precision = 10, scale = 2)
    private BigDecimal discountedPrice;

    @Column(name = "instructor_id", nullable = false)
    private Long instructorId;

    @Column(name = "instructor_name", nullable = false)
    private String instructorName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Lesson> lessons = new ArrayList<>();

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Quiz> quizzes = new ArrayList<>();

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Assignment> assignments = new ArrayList<>();

    @ManyToMany
    @JoinTable(
        name = "course_tags",
        joinColumns = @JoinColumn(name = "course_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(name = "lessons_count")
    private Integer lessonsCount = 0;

    @Column(name = "quizzes_count")
    private Integer quizzesCount = 0;

    @Column(name = "assignments_count")
    private Integer assignmentsCount = 0;

    @Column(name = "enrollment_count")
    private Integer enrollmentCount = 0;

    @Column(name = "completion_count")
    private Integer completionCount = 0;

    @Column(name = "average_rating", precision = 3, scale = 2)
    private BigDecimal averageRating;

    @Column(name = "reviews_count")
    private Integer reviewsCount = 0;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "is_featured")
    private Boolean isFeatured = false;

    @Column(name = "is_free")
    private Boolean isFree = false;

    @Column(name = "certificate_enabled")
    private Boolean certificateEnabled = true;

    @Column(columnDefinition = "TEXT")
    private String requirements;

    @Column(name = "learning_objectives", columnDefinition = "TEXT")
    private String learningObjectives;

    @Column(name = "target_audience", columnDefinition = "TEXT")
    private String targetAudience;

    @Column(name = "meta_title")
    private String metaTitle;

    @Column(name = "meta_description", length = 500)
    private String metaDescription;

    @Column(name = "meta_keywords")
    private String metaKeywords;

    public enum CourseLevel {
        BEGINNER, INTERMEDIATE, ADVANCED, EXPERT
    }

    public enum CourseStatus {
        DRAFT, REVIEW, PUBLISHED, ARCHIVED, SUSPENDED
    }

    // Méthodes utilitaires
    public void incrementEnrollmentCount() {
        this.enrollmentCount = (this.enrollmentCount == null ? 0 : this.enrollmentCount) + 1;
    }

    public void incrementCompletionCount() {
        this.completionCount = (this.completionCount == null ? 0 : this.completionCount) + 1;
    }

    public void updateCounts() {
        this.lessonsCount = this.lessons != null ? this.lessons.size() : 0;
        this.quizzesCount = this.quizzes != null ? this.quizzes.size() : 0;
        this.assignmentsCount = this.assignments != null ? this.assignments.size() : 0;
    }

    public boolean isPublished() {
        return CourseStatus.PUBLISHED.equals(this.status);
    }

    public boolean isFreeAccess() {
        return Boolean.TRUE.equals(this.isFree) || BigDecimal.ZERO.equals(this.price);
    }
}
