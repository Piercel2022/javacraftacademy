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
@Table(name = "quizzes")
@Data
@EqualsAndHashCode(callSuper = true)
@SQLDelete(sql = "UPDATE quizzes SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class Quiz extends BaseEntity {

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 500)
    private String description;

    @Column(columnDefinition = "TEXT")
    private String instructions;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id")
    private Lesson lesson; // Quiz peut être lié à une leçon spécifique

    @Column(name = "order_index")
    private Integer orderIndex;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuizType quizType = QuizType.PRACTICE;

    @Column(name = "time_limit_minutes")
    private Integer timeLimitMinutes;

    @Column(name = "max_attempts")
    private Integer maxAttempts;

    @Column(name = "passing_score")
    private Integer passingScore = 70; // Pourcentage

    @Column(name = "is_mandatory")
    private Boolean isMandatory = false;

    @Column(name = "is_published")
    private Boolean isPublished = false;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "shuffle_questions")
    private Boolean shuffleQuestions = false;

    @Column(name = "show_results_immediately")
    private Boolean showResultsImmediately = true;

    @Column(name = "show_correct_answers")
    private Boolean showCorrectAnswers = true;

    @Column(name = "allow_review")
    private Boolean allowReview = true;

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    private List<QuizQuestion> questions = new ArrayList<>();

    @Column(name = "questions_count")
    private Integer questionsCount = 0;

    @Column(name = "attempts_count")
    private Integer attemptsCount = 0;

    @Column(name = "average_score")
    private Double averageScore;

    @Column(name = "completion_rate")
    private Double completionRate;

    public enum QuizType {
        PRACTICE, ASSESSMENT, FINAL_EXAM, CERTIFICATION, SURVEY
    }

    // Méthodes utilitaires
    public boolean isPublished() {
        return Boolean.TRUE.equals(this.isPublished) && this.publishedAt != null;
    }

    public boolean hasTimeLimit() {
        return this.timeLimitMinutes != null && this.timeLimitMinutes > 0;
    }

    public boolean hasAttemptLimit() {
        return this.maxAttempts != null && this.maxAttempts > 0;
    }

    public boolean isMandatoryQuiz() {
        return Boolean.TRUE.equals(this.isMandatory);
    }

    public boolean canShowResults() {
        return Boolean.TRUE.equals(this.showResultsImmediately);
    }

    public boolean canShowCorrectAnswers() {
        return Boolean.TRUE.equals(this.showCorrectAnswers);
    }

    public boolean canReview() {
        return Boolean.TRUE.equals(this.allowReview);
    }

    public boolean shouldShuffleQuestions() {
        return Boolean.TRUE.equals(this.shuffleQuestions);
    }

    public void updateQuestionsCount() {
        this.questionsCount = this.questions != null ? this.questions.size() : 0;
    }

    public void incrementAttemptsCount() {
        this.attemptsCount = (this.attemptsCount == null ? 0 : this.attemptsCount) + 1;
    }

    public void updateStatistics(double score, boolean completed) {
        // Mise à jour du score moyen
        if (this.averageScore == null) {
            this.averageScore = score;
        } else {
            this.averageScore = ((this.averageScore * (this.attemptsCount - 1)) + score) / this.attemptsCount;
        }

        // Mise à jour du taux de completion
        if (completed) {
            // Logic to calculate completion rate would need additional data
            // This is a simplified version
        }
    }

    public boolean isPassingScore(double score) {
        return score >= this.passingScore;
    }

    public String getFormattedTimeLimit() {
        if (timeLimitMinutes == null || timeLimitMinutes == 0) {
            return "Pas de limite";
        }
        
        if (timeLimitMinutes < 60) {
            return timeLimitMinutes + " min";
        } else {
            int hours = timeLimitMinutes / 60;
            int minutes = timeLimitMinutes % 60;
            if (minutes == 0) {
                return hours + "h";
            } else {
                return hours + "h " + minutes + "min";
            }
        }
    }

    @PrePersist
    @PreUpdate
    private void validateQuiz() {
        if (this.title == null || this.title.trim().isEmpty()) {
            throw new IllegalStateException("Quiz title cannot be empty");
        }
        
        if (this.passingScore != null && (this.passingScore < 0 || this.passingScore > 100)) {
            throw new IllegalStateException("Passing score must be between 0 and 100");
        }
        
        if (this.timeLimitMinutes != null && this.timeLimitMinutes < 0) {
            throw new IllegalStateException("Time limit cannot be negative");
        }
        
        if (this.maxAttempts != null && this.maxAttempts < 1) {
            throw new IllegalStateException("Max attempts must be at least 1");
        }
        
        updateQuestionsCount();
    }
}