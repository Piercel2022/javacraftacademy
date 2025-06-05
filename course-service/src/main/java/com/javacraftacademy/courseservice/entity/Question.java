package com.javacraftacademy.courseservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Entité JPA représentant une question dans une leçon du système JavaCraftAcademy.
 * 
 * <p>Cette classe centrale du module Course Service gère tous les types de questions
 * utilisées dans les leçons interactives de la plateforme d'apprentissage.</p>
 * 
 * <h3>Fonctionnalités principales :</h3>
 * <ul>
 *   <li>Support de multiples types de questions (choix multiples, vrai/faux, ouverte)</li>
 *   <li>Gestion flexible des options et réponses correctes</li>
 *   <li>Système de points et validation des réponses</li>
 *   <li>Ordonnancement des questions dans une leçon</li>
 *   <li>Explications détaillées pour chaque question</li>
 *   <li>Suivi complet des métadonnées (création, modification, auteurs)</li>
 * </ul>
 * 
 * <h3>Relations avec l'application JavaCraftAcademy :</h3>
 * <ul>
 *   <li><strong>Lesson</strong> : Relation Many-to-One - chaque question appartient à une leçon</li>
 *   <li><strong>Answer</strong> : Relation One-to-Many - une question peut avoir plusieurs réponses</li>
 *   <li><strong>UserAnswer</strong> : Relation One-to-Many - suivi des réponses des utilisateurs</li>
 *   <li><strong>QuizResult</strong> : Utilisée pour calculer les scores des évaluations</li>
 *   <li><strong>LearningProgress</strong> : Intégrée dans le suivi de progression</li>
 * </ul>
 * 
 * <h3>Architecture du système :</h3>
 * <ul>
 *   <li>Fait partie du microservice Course Service</li>
 *   <li>Intégrée au système d'évaluation automatique</li>
 *   <li>Compatible avec le générateur de contenu adaptatif</li>
 *   <li>Supporte l'analyse de performance des apprenants</li>
 * </ul>
 * 
 * <h3>Types de questions supportés :</h3>
 * <ul>
 *   <li><strong>MULTIPLE_CHOICE</strong> : Questions à choix multiples avec plusieurs options</li>
 *   <li><strong>TRUE_FALSE</strong> : Questions de type vrai/faux</li>
 *   <li><strong>OPEN_ENDED</strong> : Questions ouvertes nécessitant une réponse textuelle</li>
 * </ul>
 * 
 * @author JavaCraftAcademy Team
 * @version 2.0
 * @since 1.0
 */
// @Entity : Marque cette classe comme une entité JPA qui sera mappée vers une table en base de données
@Entity
// @Table : Spécifie le nom de la table en base de données (ici "questions")
@Table(name = "questions")
// @Data : Annotation Lombok qui génère automatiquement getters, setters, toString, equals et hashCode
@Data
// @NoArgsConstructor : Annotation Lombok qui génère un constructeur sans paramètres (requis par JPA)
@NoArgsConstructor
// @AllArgsConstructor : Annotation Lombok qui génère un constructeur avec tous les paramètres
@AllArgsConstructor
public class Question {

    // @Id : Marque ce champ comme clé primaire de l'entité
    @Id
    // @GeneratedValue : Spécifie la stratégie de génération automatique de la clé primaire (auto-increment)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // @NotBlank : Validation Bean qui vérifie que le champ n'est pas null, vide ou composé uniquement d'espaces
    @NotBlank(message = "Question text cannot be blank")
    // @Size : Validation Bean qui vérifie la taille du texte (entre 5 et 1000 caractères)
    @Size(min = 5, max = 1000, message = "Question text must be between 5 and 1000 characters")
    // @Column : Configure le mapping de colonne (TEXT pour supporter de longs textes, nullable=false pour NOT NULL)
    @Column(columnDefinition = "TEXT", nullable = false)
    private String questionText;

    // @Enumerated : Spécifie comment stocker l'enum en base (STRING pour stocker le nom, ORDINAL pour l'index)
    @Enumerated(EnumType.STRING)
    // @NotNull : Validation Bean qui vérifie que la valeur n'est pas null
    @NotNull(message = "Question type is required")
    @Column(nullable = false)
    private QuestionType type;

    // @Column avec name : Spécifie un nom de colonne différent du nom de l'attribut Java
    @Column(name = "order_index")
    private Integer orderIndex;

    @Column(columnDefinition = "TEXT")
    private String explanation;

    @NotNull(message = "Points must be specified")
    @Column(nullable = false)
    private Integer points = 1;

    @Column(nullable = false)
    private Boolean isRequired = true;

    @Column(nullable = false)
    private Boolean isActive = true;

    // @ManyToOne : Relation plusieurs-à-un (plusieurs questions peuvent appartenir à une leçon)
    // fetch = FetchType.LAZY : Chargement paresseux pour optimiser les performances
    @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn : Spécifie la colonne de jointure (clé étrangère vers la table lessons)
    @JoinColumn(name = "lesson_id", nullable = false)
    @NotNull(message = "Question must belong to a lesson")
    private Lesson lesson;

    // @ElementCollection : Mappe une collection d'éléments simples (pas d'entités) vers une table séparée
    @ElementCollection
    // @CollectionTable : Configure la table de collection avec la clé étrangère
    @CollectionTable(name = "question_options", joinColumns = @JoinColumn(name = "question_id"))
    // @Column : Spécifie le nom de la colonne pour les éléments de la collection
    @Column(name = "option_text")
    private List<String> options;

    @ElementCollection
    @CollectionTable(name = "question_correct_answers", joinColumns = @JoinColumn(name = "question_id"))
    @Column(name = "correct_answer")
    private List<String> correctAnswers;

    @Column(columnDefinition = "TEXT")
    private String correctAnswer;

    // @CreationTimestamp : Annotation Hibernate qui automatise la définition du timestamp à la création
    @CreationTimestamp
    // updatable = false : Empêche la modification de cette colonne lors des mises à jour
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // @UpdateTimestamp : Annotation Hibernate qui met à jour automatiquement le timestamp à chaque modification
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    /**
     * Vérifie si la question est de type choix multiples.
     * 
     * @return true si la question est à choix multiples, false sinon
     */
    public boolean isMultipleChoice() {
        return type == QuestionType.MULTIPLE_CHOICE;
    }

    /**
     * Vérifie si la question est de type vrai/faux.
     * 
     * @return true si la question est de type vrai/faux, false sinon
     */
    public boolean isTrueFalse() {
        return type == QuestionType.TRUE_FALSE;
    }

    /**
     * Vérifie si la question est de type ouverte.
     * 
     * @return true si la question est ouverte, false sinon
     */
    public boolean isOpenEnded() {
        return type == QuestionType.OPEN_ENDED;
    }

    /**
     * Vérifie si la question a plusieurs réponses correctes.
     * 
     * @return true si la question a plusieurs réponses correctes, false sinon
     */
    public boolean hasMultipleCorrectAnswers() {
        return correctAnswers != null && correctAnswers.size() > 1;
    }

    /**
     * Vérifie si la question est valide pour l'évaluation.
     * 
     * @return true si la question peut être utilisée dans une évaluation
     */
    public boolean isValidForAssessment() {
        return isActive && questionText != null && !questionText.trim().isEmpty();
    }

    /**
     * Calcule le score maximum possible pour cette question.
     * 
     * @return le nombre de points maximum
     */
    public int getMaxScore() {
        return points != null ? points : 1;
    }

    // @PrePersist : Callback JPA exécuté avant la persistance de l'entité (INSERT)
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }

    // @PreUpdate : Callback JPA exécuté avant la mise à jour de l'entité (UPDATE)
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Représentation textuelle simplifiée de la question.
     * 
     * @return une chaîne représentant les informations essentielles de la question
     */
    @Override
    public String toString() {
        return "Question{" +
                "id=" + id +
                ", type=" + type +
                ", questionText='" + (questionText != null && questionText.length() > 50 ? 
                    questionText.substring(0, 50) + "..." : questionText) + '\'' +
                ", points=" + points +
                ", isActive=" + isActive +
                ", createdAt=" + createdAt +
                '}';
    }
}