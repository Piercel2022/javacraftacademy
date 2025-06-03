package com.javacraftacademy.courseservice.entity;

import com.javacraftacademy.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Entity
@Table(name = "quiz_question_options")
@Data
@EqualsAndHashCode(callSuper = true)
@SQLDelete(sql = "UPDATE quiz_question_options SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class QuizQuestionOption extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private QuizQuestion question;

    @Column(name = "option_text", nullable = false, columnDefinition = "TEXT")
    private String optionText;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    @Column(name = "is_correct", nullable = false)
    private Boolean isCorrect = false;

    @Column(name = "explanation", columnDefinition = "TEXT")
    private String explanation;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "partial_credit_percentage")
    private Integer partialCreditPercentage; // Pour crédit partiel (0-100)

    @Column(name = "selection_count")
    private Integer selectionCount = 0; // Statistiques

    // Méthodes utilitaires
    public boolean isCorrectOption() {
        return Boolean.TRUE.equals(this.isCorrect);
    }

    public boolean hasExplanation() {
        return this.explanation != null && !this.explanation.trim().isEmpty();
    }

    public boolean hasImage() {
        return this.imageUrl != null && !this.imageUrl.trim().isEmpty();
    }

    public boolean hasPartialCredit() {
        return this.partialCreditPercentage != null && this.partialCreditPercentage > 0;
    }

    public void incrementSelectionCount() {
        this.selectionCount = (this.selectionCount == null ? 0 : this.selectionCount) + 1;
    }

    public double getSelectionRate() {
        // Cette méthode nécessiterait l'accès aux statistiques globales de la question
        // Pour l'instant, retourne 0 - à implémenter avec les statistiques complètes
        return 0.0;
    }

    public int getEffectivePoints(int questionMaxPoints) {
        if (Boolean.TRUE.equals(this.isCorrect)) {
            return questionMaxPoints;
        }
        
        if (this.partialCreditPercentage != null && this.partialCreditPercentage > 0) {
            return (questionMaxPoints * this.partialCreditPercentage) / 100;
        }
        
        return 0;
    }

    @PrePersist
    @PreUpdate
    private void validateOption() {
        if (this.optionText == null || this.optionText.trim().isEmpty()) {
            throw new IllegalStateException("Option text cannot be empty");
        }
        
        if (this.orderIndex == null || this.orderIndex < 0) {
            throw new IllegalStateException("Order index must be non-negative");
        }
        
        if (this.partialCreditPercentage != null && 
            (this.partialCreditPercentage < 0 || this.partialCreditPercentage > 100)) {
            throw new IllegalStateException("Partial credit percentage must be between 0 and 100");
        }
    }
}