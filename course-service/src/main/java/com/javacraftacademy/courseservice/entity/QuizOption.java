package com.javacraftacademy.courseservice.entity;

import jakarta.persistence.*; // Annotations JPA : @Entity, @Table, @Id, @Column, @ManyToOne, @JoinColumn, @Enumerated
import lombok.Data; // Génère automatiquement getters, setters, toString, equals, hashCode
import lombok.EqualsAndHashCode; // Personnalise equals/hashCode pour hériter du comportement de la superclasse
import lombok.NoArgsConstructor; // Génère un constructeur sans paramètres (requis par JPA)
import lombok.AllArgsConstructor; // Génère un constructeur avec tous les paramètres
import lombok.Builder; // Pattern Builder pour une création d'objets plus fluide
import org.hibernate.annotations.SQLDelete; // Annotation pour la suppression logique (soft delete)
import org.hibernate.annotations.Where; // Filtre automatique pour exclure les entités supprimées logiquement

import jakarta.validation.constraints.NotNull; // Validation Bean : champ obligatoire
import jakarta.validation.constraints.NotBlank; // Validation Bean : chaîne non vide
import jakarta.validation.constraints.Size; // Validation Bean : contrainte de taille
import jakarta.validation.constraints.Min; // Validation Bean : valeur minimale
import jakarta.validation.constraints.Max; // Validation Bean : valeur maximale

/**
 * Représente une option de réponse pour une question de quiz dans l'application JavaCraftAcademy.
 *
 * <p>Cette classe modélise les différentes réponses possibles associées à une {@link QuizQuestion}.
 * Elle permet de créer des questions interactives avec diverses modalités de réponse :
 * choix multiples, choix unique, correspondances, etc.</p>
 *
 * <p><strong>Fonctionnalités principales :</strong></p>
 * <ul>
 *   <li><strong>Gestion des réponses</strong> : Stocke le texte de l'option et indique si elle est correcte</li>
 *   <li><strong>Ordre personnalisable</strong> : Permet d'organiser l'affichage des options</li>
 *   <li><strong>Types de correspondance</strong> : Support pour les questions de type "matching"</li>
 *   <li><strong>Feedback individuel</strong> : Explication spécifique pour chaque option</li>
 *   <li><strong>Suppression logique</strong> : Conservation de l'historique via soft delete</li>
 *   <li><strong>Validation robuste</strong> : Contrôles de cohérence avant persistence</li>
 * </ul>
 *
 * <p><strong>Intégration avec JavaCraftAcademy :</strong></p>
 * <ul>
 *   <li><strong>Module Quiz</strong> : Composant essentiel du système d'évaluation</li>
 *   <li><strong>Service d'apprentissage</strong> : Permet l'évaluation des compétences des apprenants</li>
 *   <li><strong>Analytics</strong> : Les réponses sont analysées pour adapter le parcours pédagogique</li>
 *   <li><strong>Gamification</strong> : Points et feedback contribuent à l'engagement des utilisateurs</li>
 *   <li><strong>Rapports</strong> : Données utilisées pour générer des statistiques de performance</li>
 * </ul>
 *
 * <p><strong>Relations dans le système :</strong></p>
 * <ul>
 *   <li><strong>QuizQuestion</strong> : Relation ManyToOne (plusieurs options par question)</li>
 *   <li><strong>UserQuizAttempt</strong> : Référencée dans les tentatives de réponse</li>
 *   <li><strong>QuizResult</strong> : Utilisée pour calculer les scores</li>
 *   <li><strong>LearningPath</strong> : Influence les recommandations de contenu</li>
 * </ul>
 *
 * <p><strong>Exemples d'utilisation :</strong></p>
 * <pre>{@code
 * // Création d'une option de réponse correcte
 * QuizOption correctOption = QuizOption.builder()
 *     .question(quizQuestion)
 *     .optionText("Java est un langage orienté objet")
 *     .isCorrect(true)
 *     .orderIndex(1)
 *     .points(2)
 *     .explanation("Correct ! Java utilise les principes de la POO")
 *     .build();
 *
 * // Option pour question de type matching
 * QuizOption matchingOption = QuizOption.builder()
 *     .question(matchingQuestion)
 *     .optionText("ArrayList")
 *     .matchingKey("dynamic_array")
 *     .isCorrect(true)
 *     .build();
 * }</pre>
 *
 * <p><strong>Évolutions futures prévues :</strong></p>
 * <ul>
 *   <li><strong>Multimédia</strong> : Support d'images, vidéos, audio dans les options</li>
 *   <li><strong>Formatage riche</strong> : Support HTML/Markdown pour le texte</li>
 *   <li><strong>Réponses partielles</strong> : Points fractionnels pour réponses incomplètes</li>
 *   <li><strong>Feedback adaptatif</strong> : Explications personnalisées selon le niveau</li>
 *   <li><strong>Analytics avancées</strong> : Tracking des patterns de réponse</li>
 * </ul>
 *
 * @author JavaCraftAcademy Team
 * @version 1.0
 * @since 1.0.0
 * @see QuizQuestion
 * @see Quiz
 * @see BaseEntity
 */
@Entity
@Table(name = "quiz_options", indexes = {
    @Index(name = "idx_quiz_option_question", columnList = "question_id"),
    @Index(name = "idx_quiz_option_order", columnList = "question_id, order_index"),
    @Index(name = "idx_quiz_option_correct", columnList = "question_id, is_correct")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE quiz_options SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class QuizOption extends BaseEntity {

    // ====================================
    // RELATIONS ET ASSOCIATIONS
    // ====================================

    /**
     * Relation ManyToOne avec la question parente.
     * Chaque option appartient à une seule question de quiz.
     * 
     * <p>Utilise un fetch LAZY pour optimiser les performances lors du chargement
     * des entités. La question sera chargée uniquement si elle est explicitement accédée.</p>
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false, 
                foreignKey = @ForeignKey(name = "fk_quiz_option_question"))
    @NotNull(message = "Une option doit être associée à une question")
    private QuizQuestion question;

    // ====================================
    // PROPRIÉTÉS PRINCIPALES
    // ====================================

    /**
     * Texte de l'option de réponse.
     * 
     * <p>Contient le libellé affiché à l'utilisateur. Peut inclure du texte formaté
     * selon le type de question (formules mathématiques, code, etc.)</p>
     */
    @Column(name = "option_text", nullable = false, length = 1000)
    @NotBlank(message = "Le texte de l'option ne peut pas être vide")
    @Size(max = 1000, message = "Le texte de l'option ne peut pas dépasser 1000 caractères")
    private String optionText;

    /**
     * Indique si cette option est une réponse correcte.
     * 
     * <p>Pour les questions à choix multiple, plusieurs options peuvent être correctes.
     * Pour les questions à choix unique, seule une option doit être correcte.</p>
     */
    @Column(name = "is_correct", nullable = false)
    @NotNull(message = "Le statut de correction doit être défini")
    private Boolean isCorrect = false;

    /**
     * Ordre d'affichage de l'option dans la liste.
     * 
     * <p>Permet de contrôler l'ordre de présentation des options à l'utilisateur.
     * Un orderIndex plus petit signifie un affichage plus haut dans la liste.</p>
     */
    @Column(name = "order_index")
    @Min(value = 0, message = "L'ordre doit être positif ou nul")
    private Integer orderIndex = 0;

    /**
     * Points attribués pour cette option spécifique.
     * 
     * <p>Permet un système de points granulaire où chaque option peut avoir
     * une valeur différente. Utile pour les réponses partiellement correctes.</p>
     */
    @Column(name = "points")
    @Min(value = 0, message = "Les points doivent être positifs ou nuls")
    @Max(value = 100, message = "Les points ne peuvent pas dépasser 100")
    private Integer points = 0;

    /**
     * Explication ou feedback spécifique à cette option.
     * 
     * <p>Texte affiché après sélection de l'option pour expliquer pourquoi
     * elle est correcte ou incorrecte. Enrichit l'expérience d'apprentissage.</p>
     */
    @Column(name = "explanation", length = 500)
    @Size(max = 500, message = "L'explication ne peut pas dépasser 500 caractères")
    private String explanation;

    // ====================================
    // PROPRIÉTÉS POUR QUESTIONS AVANCÉES
    // ====================================

    /**
     * Clé de correspondance pour les questions de type "matching".
     * 
     * <p>Utilisée dans les questions où il faut associer des éléments.
     * Par exemple : associer un terme technique à sa définition.</p>
     */
    @Column(name = "matching_key", length = 100)
    @Size(max = 100, message = "La clé de correspondance ne peut pas dépasser 100 caractères")
    private String matchingKey;

    /**
     * Type de l'option pour des comportements spécialisés.
     * 
     * <p>Permet d'étendre les fonctionnalités sans modifier la structure de base.
     * Exemples : IMAGE, AUDIO, CODE_SNIPPET, FORMULA, etc.</p>
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "option_type")
    private OptionType optionType = OptionType.TEXT;

    /**
     * URL ou chemin vers un fichier multimédia associé.
     * 
     * <p>Pour les questions incluant des images, sons, ou autres ressources.
     * Le chemin peut être relatif (stockage local) ou absolu (CDN).</p>
     */
    @Column(name = "media_url", length = 255)
    @Size(max = 255, message = "L'URL média ne peut pas dépasser 255 caractères")
    private String mediaUrl;

    // ====================================
    // ÉNUMÉRATIONS
    // ====================================

    /**
     * Types d'options disponibles pour étendre les fonctionnalités.
     * 
     * <p>Cette énumération permet d'adapter le comportement et l'affichage
     * des options selon leur nature.</p>
     */
    public enum OptionType {
        /** Option textuelle standard */
        TEXT,
        /** Option avec image associée */
        IMAGE,
        /** Option avec contenu audio */
        AUDIO,
        /** Option contenant du code source */
        CODE_SNIPPET,
        /** Option avec formule mathématique */
        FORMULA,
        /** Option avec lien hypertexte */
        LINK,
        /** Option pour glisser-déposer */
        DRAGGABLE
    }

    // ====================================
    // MÉTHODES UTILITAIRES
    // ====================================

    /**
     * Vérifie si l'option est de type textuel.
     * @return true si l'option est de type TEXT
     */
    public boolean isTextOption() {
        return this.optionType == OptionType.TEXT;
    }

    /**
     * Vérifie si l'option contient du média.
     * @return true si l'option a une URL média définie
     */
    public boolean hasMedia() {
        return this.mediaUrl != null && !this.mediaUrl.trim().isEmpty();
    }

    /**
     * Vérifie si l'option est utilisée pour du matching.
     * @return true si une clé de correspondance est définie
     */
    public boolean isMatchingOption() {
        return this.matchingKey != null && !this.matchingKey.trim().isEmpty();
    }

    /**
     * Vérifie si l'option a une explication.
     * @return true si une explication est fournie
     */
    public boolean hasExplanation() {
        return this.explanation != null && !this.explanation.trim().isEmpty();
    }

    /**
     * Retourne une représentation courte de l'option pour les logs.
     * @return description concise de l'option
     */
    public String getShortDescription() {
        String text = optionText != null && optionText.length() > 50 
            ? optionText.substring(0, 47) + "..." 
            : optionText;
        return String.format("QuizOption[%s, correct=%s]", text, isCorrect);
    }

    // ====================================
    // VALIDATION ET CALLBACKS JPA
    // ====================================

    /**
     * Validation exécutée avant la persistence ou mise à jour.
     * 
     * <p>Assure la cohérence des données et respecte les règles métier
     * spécifiques aux différents types de questions.</p>
     */
    @PrePersist
    @PreUpdate
    private void validateOption() {
        // Validation du texte
        if (this.optionText == null || this.optionText.trim().isEmpty()) {
            throw new IllegalStateException("Le texte de l'option ne peut pas être vide.");
        }

        // Validation des points
        if (this.points != null && this.points < 0) {
            throw new IllegalStateException("Les points doivent être positifs ou nuls.");
        }

        // Validation pour questions de matching
        if (this.question != null && this.question.isMatching()) {
            if (this.isCorrect && (this.matchingKey == null || this.matchingKey.trim().isEmpty())) {
                throw new IllegalStateException(
                    "Les options correctes de type matching doivent avoir une clé de correspondance.");
            }
        }

        // Validation de l'URL média
        if (this.mediaUrl != null && !this.mediaUrl.trim().isEmpty()) {
            if (!isValidUrl(this.mediaUrl)) {
                throw new IllegalStateException("L'URL média n'est pas valide : " + this.mediaUrl);
            }
        }

        // Nettoyage des chaînes
        this.optionText = this.optionText.trim();
        if (this.explanation != null) {
            this.explanation = this.explanation.trim();
        }
        if (this.matchingKey != null) {
            this.matchingKey = this.matchingKey.trim();
        }
    }

    /**
     * Valide basiquement une URL.
     * @param url l'URL à valider
     * @return true si l'URL semble valide
     */
    private boolean isValidUrl(String url) {
        return url.startsWith("http://") || url.startsWith("https://") || url.startsWith("/");
    }

    // ====================================
    // EXTENSIBILITÉ FUTURE
    // ====================================

    /*
     * ROADMAP DES ÉVOLUTIONS FUTURES :
     * 
     * 1. SUPPORT MULTIMÉDIA AVANCÉ :
     *    - Champ `metadata` JSON pour stocker des informations spécifiques
     *    - Support des thumbnails pour les images/vidéos
     *    - Intégration avec des services de streaming
     * 
     * 2. INTELLIGENCE ARTIFICIELLE :
     *    - Champ `aiGenerated` pour marquer les options générées automatiquement
     *    - Integration avec des modèles de langage pour générer des distracteurs
     * 
     * 3. ANALYTICS ET PERSONNALISATION :
     *    - Tracking des temps de réponse par option
     *    - Adaptation dynamique de la difficulté
     *    - Recommandations personnalisées basées sur l'historique
     * 
     * 4. COLLABORATION :
     *    - Support des options créées collaborativement
     *    - Système de versioning pour les modifications
     *    - Approbation par pairs pour les nouvelles options
     * 
     * 5. ACCESSIBILITÉ :
     *    - Support des descriptions alternatives pour les malvoyants
     *    - Options audio automatiques via text-to-speech
     *    - Modes de contraste élevé
     */
}