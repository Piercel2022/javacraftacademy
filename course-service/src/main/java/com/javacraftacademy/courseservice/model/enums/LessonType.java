package com.javacraftacademy.courseservice.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Énumération représentant les différents types de leçons disponibles dans JavaCraft Academy.
 * 
 * <p>Cette énumération définit les formats pédagogiques et les modalités d'apprentissage
 * proposés dans les cours. Chaque type de leçon a ses propres caractéristiques en termes
 * de durée, d'interactivité, de ressources nécessaires et de méthodes d'évaluation.
 * 
 * <h3>Catégories principales :</h3>
 * <ul>
 *   <li><strong>Théoriques</strong> : LECTURE, READING</li>
 *   <li><strong>Pratiques</strong> : EXERCISE, LAB, PROJECT</li>
 *   <li><strong>Multimédias</strong> : VIDEO, INTERACTIVE</li>
 *   <li><strong>Évaluatives</strong> : QUIZ, ASSIGNMENT</li>
 * </ul>
 * 
 * <h3>Relations dans l'application :</h3>
 * <ul>
 *   <li><strong>Lesson Entity</strong> : Chaque leçon possède un type définissant sa structure</li>
 *   <li><strong>Content Rendering</strong> : Interface adaptée selon le type de leçon</li>
 *   <li><strong>Progress Tracking</strong> : Critères de completion spécifiques par type</li>
 *   <li><strong>Resource Management</strong> : Gestion des fichiers selon le type</li>
 *   <li><strong>Assessment Engine</strong> : Méthodes d'évaluation par type</li>
 *   <li><strong>Learning Analytics</strong> : Métriques d'engagement par type</li>
 * </ul>
 * 
 * @since 1.0.0
 * @version 1.2.0
 */
public enum LessonType {
    
    // === CATÉGORIE THÉORIQUE ===
    
    /**
     * Cours magistral avec présentation structurée.
     * Durée typique : 30-60 minutes.
     * Ressources : Slides, notes, références.
     */
    LECTURE("lecture", "Cours magistral", LessonCategory.THEORETICAL, 45, false, true),
    
    /**
     * Lecture de documents, articles ou chapitres.
     * Durée typique : 15-30 minutes.
     * Ressources : PDF, articles, documentation.
     */
    READING("reading", "Lecture", LessonCategory.THEORETICAL, 20, false, true),
    
    // === CATÉGORIE PRATIQUE ===
    
    /**
     * Exercice pratique guidé.
     * Durée typique : 20-45 minutes.
     * Ressources : Énoncé, code de base, tests.
     */
    EXERCISE("exercise", "Exercice", LessonCategory.PRACTICAL, 30, true, true),
    
    /**
     * Travaux pratiques en environnement contrôlé.
     * Durée typique : 60-120 minutes.
     * Ressources : Environnement de développement, données.
     */
    LAB("lab", "Travaux pratiques", LessonCategory.PRACTICAL, 90, true, true),
    
    /**
     * Projet d'application pratique.
     * Durée typique : 120-240 minutes.
     * Ressources : Spécifications, ressources externes.
     */
    PROJECT("project", "Projet", LessonCategory.PRACTICAL, 180, true, true),
    
    // === CATÉGORIE MULTIMÉDIA ===
    
    /**
     * Contenu vidéo éducatif.
     * Durée typique : 10-30 minutes.
     * Ressources : Fichier vidéo, sous-titres, transcription.
     */
    VIDEO("video", "Vidéo", LessonCategory.MULTIMEDIA, 20, false, false),
    
    /**
     * Contenu interactif avec animations.
     * Durée typique : 15-45 minutes.
     * Ressources : Module interactif, données dynamiques.
     */
    INTERACTIVE("interactive", "Interactif", LessonCategory.MULTIMEDIA, 25, true, false),
    
    // === CATÉGORIE ÉVALUATIVE ===
    
    /**
     * Quiz de compréhension.
     * Durée typique : 10-20 minutes.
     * Ressources : Questions, réponses, explications.
     */
    QUIZ("quiz", "Quiz", LessonCategory.EVALUATIVE, 15, true, true),
    
    /**
     * Devoir à rendre.
     * Durée typique : 60-180 minutes.
     * Ressources : Énoncé, critères d'évaluation, ressources.
     */
    ASSIGNMENT("assignment", "Devoir", LessonCategory.EVALUATIVE, 120, true, true);
    
    // === ATTRIBUTS ===
    
    private final String value;
    private final String displayName;
    private final LessonCategory category;
    private final int estimatedDurationMinutes;
    private final boolean requiresInteraction;
    private final boolean trackable;
    
    // Cache pour la désérialisation
    private static final Map<String, LessonType> VALUE_MAP = Arrays.stream(values())
            .collect(Collectors.toMap(LessonType::getValue, type -> type));
    
    // === CONSTRUCTEUR ===
    
    /**
     * Constructeur de l'énumération LessonType.
     *
     * @param value Valeur utilisée pour la sérialisation JSON
     * @param displayName Nom d'affichage pour l'interface utilisateur
     * @param category Catégorie de la leçon
     * @param estimatedDurationMinutes Durée estimée en minutes
     * @param requiresInteraction Indique si la leçon nécessite une interaction
     * @param trackable Indique si le progrès peut être suivi
     */
    LessonType(String value, String displayName, LessonCategory category, 
               int estimatedDurationMinutes, boolean requiresInteraction, boolean trackable) {
        this.value = value;
        this.displayName = displayName;
        this.category = category;
        this.estimatedDurationMinutes = estimatedDurationMinutes;
        this.requiresInteraction = requiresInteraction;
        this.trackable = trackable;
    }
    
    // === MÉTHODES JACKSON ===
    
    /**
     * Retourne la valeur utilisée pour la sérialisation JSON.
     *
     * @return La valeur string de l'énumération
     */
    @JsonValue
    public String getValue() {
        return value;
    }
    
    /**
     * Créé une instance de LessonType à partir d'une valeur string.
     * Utilisé pour la désérialisation JSON.
     *
     * @param value La valeur string à convertir
     * @return L'instance LessonType correspondante
     * @throws IllegalArgumentException Si la valeur n'est pas reconnue
     */
    @JsonCreator
    public static LessonType fromValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("La valeur du type de leçon ne peut pas être nulle ou vide");
        }
        
        LessonType type = VALUE_MAP.get(value.toLowerCase());
        if (type == null) {
            throw new IllegalArgumentException("Type de leçon non reconnu: " + value);
        }
        
        return type;
    }
    
    // === GETTERS ===
    
    /**
     * @return Le nom d'affichage de la leçon
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * @return La catégorie de la leçon
     */
    public LessonCategory getCategory() {
        return category;
    }
    
    /**
     * @return La durée estimée en minutes
     */
    public int getEstimatedDurationMinutes() {
        return estimatedDurationMinutes;
    }
    
    /**
     * @return true si la leçon nécessite une interaction de l'utilisateur
     */
    public boolean requiresInteraction() {
        return requiresInteraction;
    }
    
    /**
     * @return true si le progrès de la leçon peut être suivi
     */
    public boolean isTrackable() {
        return trackable;
    }
    
    // === MÉTHODES UTILITAIRES ===
    
    /**
     * Retourne tous les types de leçons d'une catégorie donnée.
     *
     * @param category La catégorie à filtrer
     * @return Liste des types de leçons de cette catégorie
     */
    public static List<LessonType> getByCategory(LessonCategory category) {
        return Arrays.stream(values())
                .filter(type -> type.getCategory() == category)
                .collect(Collectors.toList());
    }
    
    /**
     * Retourne tous les types de leçons interactives.
     *
     * @return Liste des types de leçons nécessitant une interaction
     */
    public static List<LessonType> getInteractiveTypes() {
        return Arrays.stream(values())
                .filter(LessonType::requiresInteraction)
                .collect(Collectors.toList());
    }
    
    /**
     * Retourne tous les types de leçons traçables.
     *
     * @return Liste des types de leçons dont le progrès peut être suivi
     */
    public static List<LessonType> getTrackableTypes() {
        return Arrays.stream(values())
                .filter(LessonType::isTrackable)
                .collect(Collectors.toList());
    }
    
    /**
     * Vérifie si un type de leçon est théorique.
     *
     * @return true si le type est théorique
     */
    public boolean isTheoretical() {
        return category == LessonCategory.THEORETICAL;
    }
    
    /**
     * Vérifie si un type de leçon est pratique.
     *
     * @return true si le type est pratique
     */
    public boolean isPractical() {
        return category == LessonCategory.PRACTICAL;
    }
    
    /**
     * Vérifie si un type de leçon est multimédia.
     *
     * @return true si le type est multimédia
     */
    public boolean isMultimedia() {
        return category == LessonCategory.MULTIMEDIA;
    }
    
    /**
     * Vérifie si un type de leçon est évaluatif.
     *
     * @return true si le type est évaluatif
     */
    public boolean isEvaluative() {
        return category == LessonCategory.EVALUATIVE;
    }
    
    @Override
    public String toString() {
        return String.format("LessonType{value='%s', displayName='%s', category=%s, duration=%d min}", 
                             value, displayName, category, estimatedDurationMinutes);
    }
    
    // === ÉNUMÉRATION INTERNE ===
    
    /**
     * Catégories de leçons pour classification.
     */
    public enum LessonCategory {
        THEORETICAL("Théorique"),
        PRACTICAL("Pratique"),
        MULTIMEDIA("Multimédia"),
        EVALUATIVE("Évaluatif");
        
        private final String displayName;
        
        LessonCategory(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
}