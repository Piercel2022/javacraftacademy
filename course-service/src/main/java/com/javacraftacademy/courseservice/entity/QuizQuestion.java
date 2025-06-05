package com.javacraftacademy.courseservice.entity;

import jakarta.persistence.*; // Pour les annotations JPA comme @Entity, @Id, @ManyToOne, etc.
import lombok.Data; // Génère automatiquement les getters, setters, toString, equals, et hashCode.
import lombok.EqualsAndHashCode; // Personnalise equals/hashCode pour inclure la superclasse.
import org.hibernate.annotations.SQLDelete; // Permet la suppression logique (soft delete).
import org.hibernate.annotations.Where; // Exclut les entités marquées comme "deleted" lors des requêtes.

import java.util.ArrayList;
import java.util.List;

/**
 * Représente une question appartenant à un quiz dans l’application JavaCraftAcademy.
 *
 * <p>Chaque `QuizQuestion` est liée à une entité `Quiz` et contient un ou plusieurs choix (`QuizOption`).
 * Elle permet de structurer des questionnaires dynamiques avec différents types de questions :
 * choix multiples, choix unique, vrai/faux, réponse texte, correspondance (matching), etc.</p>
 *
 * <p>Fonctionnalités clés :</p>
 * <ul>
 *   <li>Définir le texte de la question, son type et son ordre dans le quiz</li>
 *   <li>Associer des options/réponses possibles via l'entité `QuizOption`</li>
 *   <li>Supporter différents types de questions grâce à l’énumération `QuestionType`</li>
 *   <li>Utiliser la suppression logique pour conserver l’historique (grâce à Hibernate)</li>
 *   <li>Permet des extensions futures comme : ajout de fichiers multimédias, timing individuel par question, etc.</li>
 * </ul>
 *
 * <p>Rôle dans JavaCraftAcademy :</p>
 * Cette classe s’intègre dans le module de gestion des quizzes qui enrichit l’expérience d’apprentissage.
 * Elle est essentielle pour l’évaluation des compétences des apprenants.
 */
@Entity
@Table(name = "quiz_questions")
@Data
@EqualsAndHashCode(callSuper = true)
@SQLDelete(sql = "UPDATE quiz_questions SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class QuizQuestion extends BaseEntity {

    /**
     * Relation ManyToOne avec le Quiz parent.
     * Chaque question appartient à un seul quiz.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    /**
     * Texte de la question (obligatoire).
     */
    @Column(nullable = false, length = 1000)
    private String questionText;

    /**
     * Explication affichée après la réponse (optionnelle).
     */
    @Column(length = 500)
    private String explanation;

    /**
     * Type de question (choix multiples, vrai/faux, etc.).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", nullable = false)
    private QuestionType questionType = QuestionType.MULTIPLE_CHOICE;

    /**
     * Ordre d'affichage de la question dans le quiz.
     */
    @Column(name = "order_index")
    private Integer orderIndex;

    /**
     * Nombre de points attribués pour cette question.
     */
    @Column(name = "points", nullable = false)
    private Integer points = 1;

    /**
     * Liste des options/réponses proposées pour cette question.
     */
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuizOption> options = new ArrayList<>();

    /**
     * Enumération des types de questions disponibles.
     */
    public enum QuestionType {
        MULTIPLE_CHOICE, SINGLE_CHOICE, TRUE_FALSE, TEXT, MATCHING
    }

    // Méthodes utilitaires :

    public boolean isMultipleChoice() {
        return this.questionType == QuestionType.MULTIPLE_CHOICE;
    }

    public boolean isSingleChoice() {
        return this.questionType == QuestionType.SINGLE_CHOICE;
    }

    public boolean isTrueFalse() {
        return this.questionType == QuestionType.TRUE_FALSE;
    }

    public boolean isText() {
        return this.questionType == QuestionType.TEXT;
    }

    public boolean isMatching() {
        return this.questionType == QuestionType.MATCHING;
    }

    /**
     * Vérifie si au moins une réponse correcte est définie pour la question.
     */
    public boolean hasCorrectOption() {
        return options != null && options.stream().anyMatch(QuizOption::getIsCorrect);
    }

    /**
     * Valide la question avant sauvegarde ou mise à jour.
     */
    @PrePersist
    @PreUpdate
    private void validateQuestion() {
        if (this.questionText == null || this.questionText.trim().isEmpty()) {
            throw new IllegalStateException("Le texte de la question ne peut pas être vide.");
        }

        if (this.points == null || this.points < 0) {
            throw new IllegalStateException("Les points doivent être un entier positif.");
        }

        if ((isMultipleChoice() || isSingleChoice() || isTrueFalse()) && (options == null || options.isEmpty())) {
            throw new IllegalStateException("Les questions de type choix doivent avoir au moins une option.");
        }
    }

    // Évolutions futures possibles :
    // - Ajouter un champ "mediaUrl" pour inclure une image ou vidéo liée à la question
    // - Ajouter un "temps maximum" par question
    // - Ajouter un champ "tags" pour catégoriser les questions
}
