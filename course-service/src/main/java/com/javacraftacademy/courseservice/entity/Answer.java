package com.javacraftacademy.courseservice.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entité JPA représentant une réponse à une question dans le système JavaCraftAcademy.
 * 
 * <p>Cette classe fait partie du modèle de données de l'application d'apprentissage
 * et gère les réponses associées aux questions des quiz et exercices.</p>
 * 
 * <h3>Fonctionnalités principales :</h3>
 * <ul>
 *   <li>Stockage du texte de la réponse avec support de contenu volumineux (TEXT)</li>
 *   <li>Indication si la réponse est correcte ou non</li>
 *   <li>Gestion de l'ordre d'affichage des réponses</li>
 *   <li>Stockage d'explications détaillées pour chaque réponse</li>
 *   <li>Suivi automatique des dates de création et de modification</li>
 * </ul>
 * 
 * <h3>Relations avec l'application :</h3>
 * <ul>
 *   <li><strong>Question</strong> : Relation Many-to-One - chaque réponse appartient à une question</li>
 *   <li><strong>UserAnswer</strong> : Relation One-to-Many - une réponse peut être sélectionnée par plusieurs utilisateurs</li>
 *   <li><strong>QuizResult</strong> : Utilisée indirectement pour calculer les scores des quiz</li>
 * </ul>
 * 
 * <h3>Utilisation dans l'écosystème JavaCraftAcademy :</h3>
 * <ul>
 *   <li>Système de quiz interactifs</li>
 *   <li>Évaluations et examens</li>
 *   <li>Exercices pratiques avec feedback</li>
 *   <li>Génération de rapports de performance</li>
 * </ul>
 * 
 * @author JavaCraftAcademy Team
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(name = "answers")
public class Answer {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "answer_id")
    private Long answerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(name = "answer_text", nullable = false, columnDefinition = "TEXT")
    private String answerText;

    @Column(name = "is_correct", nullable = false)
    private Boolean isCorrect = false;

    @Column(name = "answer_order")
    private Integer answerOrder;

    @Column(name = "explanation", columnDefinition = "TEXT")
    private String explanation;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Constructeur par défaut.
     * Initialise automatiquement les timestamps de création et de modification.
     */
    public Answer() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Constructeur avec paramètres essentiels.
     * 
     * @param question la question à laquelle cette réponse est associée
     * @param answerText le texte de la réponse
     * @param isCorrect indique si cette réponse est correcte
     */
    public Answer(Question question, String answerText, Boolean isCorrect) {
        this();
        this.question = question;
        this.answerText = answerText;
        this.isCorrect = isCorrect;
    }

    // Getters and Setters

    public Long getAnswerId() {
        return answerId;
    }

    public void setAnswerId(Long answerId) {
        this.answerId = answerId;
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    public String getAnswerText() {
        return answerText;
    }

    public void setAnswerText(String answerText) {
        this.answerText = answerText;
        this.updatedAt = LocalDateTime.now();
    }

    public Boolean getIsCorrect() {
        return isCorrect;
    }

    public void setIsCorrect(Boolean isCorrect) {
        this.isCorrect = isCorrect;
        this.updatedAt = LocalDateTime.now();
    }

    public Integer getAnswerOrder() {
        return answerOrder;
    }

    public void setAnswerOrder(Integer answerOrder) {
        this.answerOrder = answerOrder;
        this.updatedAt = LocalDateTime.now();
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Vérifie l'égalité entre deux objets Answer basée sur l'ID.
     * 
     * @param obj l'objet à comparer
     * @return true si les objets sont égaux, false sinon
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Answer answer = (Answer) obj;
        return answerId != null && answerId.equals(answer.answerId);
    }

    /**
     * Génère le code de hachage basé sur l'ID de la réponse.
     * 
     * @return le code de hachage
     */
    @Override
    public int hashCode() {
        return answerId != null ? answerId.hashCode() : 0;
    }

    /**
     * Représentation textuelle de l'objet Answer.
     * 
     * @return une chaîne représentant l'objet
     */
    @Override
    public String toString() {
        return "Answer{" +
                "answerId=" + answerId +
                ", answerText='" + answerText + '\'' +
                ", isCorrect=" + isCorrect +
                ", answerOrder=" + answerOrder +
                ", createdAt=" + createdAt +
                '}';
    }
}