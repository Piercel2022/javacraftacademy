package com.javacraftacademy.courseservice.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Énumération représentant les différents types d'assignments dans l'application JavaCraftAcademy.
 * 
 * <h3>But de l'énumération :</h3>
 * Cette énumération définit et standardise les types d'assignments (devoirs/exercices) disponibles
 * dans la plateforme éducative, permettant une catégorisation cohérente et une gestion 
 * différenciée selon le type pédagogique.
 * 
 * <h3>Fonctionnalités principales :</h3>
 * <ul>
 *   <li><strong>Catégorisation standardisée :</strong> Types prédéfinis couvrant différentes 
 *       approches pédagogiques</li>
 *   <li><strong>Affichage multilingue :</strong> Noms d'affichage localisés pour l'interface utilisateur</li>
 *   <li><strong>Sérialisation JSON :</strong> Support pour les API REST et le stockage de données</li>
 *   <li><strong>Validation :</strong> Méthodes utilitaires pour la validation et la conversion</li>
 *   <li><strong>Métadonnées étendues :</strong> Informations supplémentaires pour chaque type 
 *       (durée estimée, icône, couleur thématique)</li>
 * </ul>
 * 
 * <h3>Relations et dépendances dans l'écosystème JavaCraftAcademy :</h3>
 * <ul>
 *   <li><strong>Assignment :</strong> Utilisé comme champ enum dans l'entité Assignment</li>
 *   <li><strong>AssignmentController :</strong> Filtrage et recherche par type via l'API REST</li>
 *   <li><strong>AssignmentService :</strong> Logique métier spécifique selon le type</li>
 *   <li><strong>NotificationService :</strong> Templates de notification personnalisés par type</li>
 *   <li><strong>GradingService :</strong> Critères d'évaluation adaptés au type d'assignment</li>
 *   <li><strong>StatisticsService :</strong> Analytiques et rapports groupés par type</li>
 *   <li><strong>TemplateService :</strong> Modèles d'assignments prédéfinis par type</li>
 *   <li><strong>UI Components :</strong> Icônes, couleurs et comportements spécifiques dans le frontend</li>
 * </ul>
 * 
 * <h3>Implémentation des fonctionnalités :</h3>
 * <ul>
 *   <li><strong>Constructeur privé :</strong> Encapsulation des propriétés de chaque type</li>
 *   <li><strong>Annotations Jackson :</strong> @JsonValue et @JsonCreator pour la sérialisation</li>
 *   <li><strong>Lombok @Getter :</strong> Accès sécurisé aux propriétés</li>
 *   <li><strong>Méthodes statiques :</strong> Utilitaires pour conversion et validation</li>
 *   <li><strong>Stream API :</strong> Filtrage et recherche efficaces</li>
 * </ul>
 * 
 * <h3>Types d'assignments et leurs caractéristiques :</h3>
 * <ul>
 *   <li><strong>PROJECT :</strong> Projets pratiques long terme (2-4 semaines)</li>
 *   <li><strong>CODE_CHALLENGE :</strong> Défis de programmation courts (1-3 heures)</li>
 *   <li><strong>LAB :</strong> Travaux pratiques guidés (2-4 heures)</li>
 *   <li><strong>EXAM :</strong> Évaluations formelles avec contraintes temporelles</li>
 *   <li><strong>HOMEWORK :</strong> Devoirs réguliers de révision</li>
 *   <li><strong>ESSAY :</strong> Rédactions et analyses théoriques</li>
 *   <li><strong>PRESENTATION :</strong> Présentations orales et supports visuels</li>
 *   <li><strong>RESEARCH :</strong> Recherches approfondies et bibliographies</li>
 * </ul>
 * 
 * <h3>Extensions futures possibles :</h3>
 * <ul>
 *   <li><strong>Types dynamiques :</strong> Permettre aux instructeurs de créer des types personnalisés</li>
 *   <li><strong>Hiérarchie de types :</strong> Sous-catégories et spécialisations</li>
 *   <li><strong>Workflow spécialisés :</strong> Étapes de validation différentes par type</li>
 *   <li><strong>Templates intelligents :</strong> Génération automatique basée sur le type</li>
 *   <li><strong>Intégration IA :</strong> Suggestions de type basées sur le contenu</li>
 *   <li><strong>Gamification :</strong> Points et badges spécifiques par type</li>
 *   <li><strong>Collaboration :</strong> Types pour travaux de groupe vs individuels</li>
 *   <li><strong>Adaptabilité :</strong> Types adaptatifs selon le niveau de l'étudiant</li>
 * </ul>
 * 
 * <h3>Exemple d'utilisation :</h3>
 * <pre>{@code
 * // Création d'un assignment avec type
 * Assignment assignment = Assignment.builder()
 *     .title("Développement d'une API REST")
 *     .type(AssignmentType.PROJECT)
 *     .build();
 * 
 * // Filtrage par type
 * List<Assignment> projects = assignmentService.findByType(AssignmentType.PROJECT);
 * 
 * // Validation côté API
 * AssignmentType type = AssignmentType.fromString("PROJECT");
 * if (type.isLongTerm()) {
 *     // Logique spécifique aux projets long terme
 * }
 * }</pre>
 * 
 * @author JavaCraftAcademy Team
 * @version 1.0
 * @since 1.0
 * 
 * @see Assignment
 * @see com.javacraftacademy.courseservice.service.AssignmentService
 * @see com.javacraftacademy.courseservice.controller.AssignmentController
 */
@Getter
public enum AssignmentType {
    
    /**
     * Projet pratique de développement.
     * Durée estimée : 2-4 semaines, travail en autonomie, livrables multiples.
     */
    PROJECT("Projet", "project", "#3B82F6", "🚀", 
            "Projet pratique de développement avec livrables multiples", 
            14, true, true),
    
    /**
     * Dissertation ou rédaction académique.
     * Durée estimée : 3-7 jours, travail de recherche et rédaction.
     */
    ESSAY("Dissertation", "essay", "#10B981", "📝", 
          "Rédaction académique avec analyse approfondie", 
          5, false, true),
    
    /**
     * Défi de programmation court et intensif.
     * Durée estimée : 1-3 heures, résolution de problèmes algorithmiques.
     */
    CODE_CHALLENGE("Défi de Code", "code-challenge", "#F59E0B", "💻", 
                   "Défi de programmation avec contraintes temporelles", 
                   1, false, false),
    
    /**
     * Présentation orale avec support visuel.
     * Durée estimée : 1-2 semaines de préparation, présentation 15-30 minutes.
     */
    PRESENTATION("Présentation", "presentation", "#EF4444", "🎤", 
                "Présentation orale avec support visuel", 
                7, false, true),
    
    /**
     * Travail de recherche approfondi.
     * Durée estimée : 2-3 semaines, méthodologie de recherche académique.
     */
    RESEARCH("Recherche", "research", "#8B5CF6", "🔍", 
            "Travail de recherche avec méthodologie académique", 
            14, false, true),
    
    /**
     * Travaux pratiques en laboratoire.
     * Durée estimée : 2-4 heures, travail guidé avec expérimentation.
     */
    LAB("Laboratoire", "lab", "#06B6D4", "🧪", 
        "Travaux pratiques guidés avec expérimentation", 
        1, false, false),
    
    /**
     * Devoir régulier de révision.
     * Durée estimée : 2-5 jours, consolidation des acquis.
     */
    HOMEWORK("Devoir", "homework", "#84CC16", "📚", 
            "Devoir régulier pour consolider les acquis", 
            3, false, false),
    
    /**
     * Évaluation formelle avec contraintes.
     * Durée estimée : 1-3 heures, évaluation sous surveillance.
     */
    EXAM("Examen", "exam", "#DC2626", "📋", 
         "Évaluation formelle avec contraintes temporelles", 
         1, true, false);
    
    /**
     * Nom d'affichage localisé pour l'interface utilisateur.
     */
    private final String displayName;
    
    /**
     * Identifiant technique utilisé dans les URLs et APIs.
     */
    private final String slug;
    
    /**
     * Couleur hexadécimale pour l'interface utilisateur.
     */
    private final String color;
    
    /**
     * Icône emoji ou Unicode pour l'affichage.
     */
    private final String icon;
    
    /**
     * Description détaillée du type d'assignment.
     */
    private final String description;
    
    /**
     * Durée estimée en jours pour compléter l'assignment.
     */
    private final int estimatedDays;
    
    /**
     * Indique si ce type d'assignment nécessite une surveillance.
     */
    private final boolean requiresSupervision;
    
    /**
     * Indique si ce type d'assignment permet des soumissions multiples.
     */
    private final boolean allowsMultipleSubmissions;
    
    /**
     * Constructeur privé pour initialiser les propriétés de chaque type.
     * 
     * @param displayName Nom affiché dans l'interface utilisateur
     * @param slug Identifiant technique
     * @param color Couleur hexadécimale
     * @param icon Icône pour l'affichage
     * @param description Description détaillée
     * @param estimatedDays Durée estimée en jours
     * @param requiresSupervision Nécessite une surveillance
     * @param allowsMultipleSubmissions Permet soumissions multiples
     */
    AssignmentType(String displayName, String slug, String color, String icon, 
                   String description, int estimatedDays, boolean requiresSupervision, 
                   boolean allowsMultipleSubmissions) {
        this.displayName = displayName;
        this.slug = slug;
        this.color = color;
        this.icon = icon;
        this.description = description;
        this.estimatedDays = estimatedDays;
        this.requiresSupervision = requiresSupervision;
        this.allowsMultipleSubmissions = allowsMultipleSubmissions;
    }
    
    // Méthodes utilitaires pour la sérialisation JSON
    
    /**
     * Retourne la valeur utilisée pour la sérialisation JSON.
     * Utilise le nom de l'enum pour maintenir la compatibilité.
     * 
     * @return Nom de l'enum
     */
    @JsonValue
    public String toJson() {
        return this.name();
    }
    
    /**
     * Crée une instance à partir d'une chaîne JSON.
     * Support pour nom d'enum et slug.
     * 
     * @param value Valeur JSON (nom d'enum ou slug)
     * @return Instance de AssignmentType correspondante
     * @throws IllegalArgumentException Si la valeur n'est pas reconnue
     */
    @JsonCreator
    public static AssignmentType fromJson(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("La valeur AssignmentType ne peut pas être nulle ou vide");
        }
        
        // Tentative par nom d'enum
        try {
            return AssignmentType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            // Tentative par slug
            return fromSlug(value);
        }
    }
    
    // Méthodes utilitaires statiques
    
    /**
     * Trouve un type d'assignment par son slug.
     * 
     * @param slug Identifiant technique
     * @return Instance correspondante
     * @throws IllegalArgumentException Si le slug n'est pas trouvé
     */
    public static AssignmentType fromSlug(String slug) {
        return Arrays.stream(values())
                .filter(type -> type.slug.equalsIgnoreCase(slug))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Slug AssignmentType non reconnu : " + slug));
    }
    
    /**
     * Trouve un type d'assignment par son nom d'affichage.
     * 
     * @param displayName Nom d'affichage
     * @return Instance correspondante
     * @throws IllegalArgumentException Si le nom n'est pas trouvé
     */
    public static AssignmentType fromDisplayName(String displayName) {
        return Arrays.stream(values())
                .filter(type -> type.displayName.equalsIgnoreCase(displayName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Nom d'affichage AssignmentType non reconnu : " + displayName));
    }
    
    /**
     * Retourne tous les types d'assignments sous forme de liste.
     * Utile pour les interfaces utilisateur et validations.
     * 
     * @return Liste de tous les types
     */
    public static List<AssignmentType> getAllTypes() {
        return Arrays.asList(values());
    }
    
    /**
     * Retourne les types d'assignments qui nécessitent une surveillance.
     * 
     * @return Liste des types avec surveillance requise
     */
    public static List<AssignmentType> getSupervisedTypes() {
        return Arrays.stream(values())
                .filter(AssignmentType::requiresSupervision)
                .collect(Collectors.toList());
    }
    
    /**
     * Retourne les types d'assignments qui permettent des soumissions multiples.
     * 
     * @return Liste des types avec soumissions multiples
     */
    public static List<AssignmentType> getMultipleSubmissionTypes() {
        return Arrays.stream(values())
                .filter(AssignmentType::allowsMultipleSubmissions)
                .collect(Collectors.toList());
    }
    
    /**
     * Retourne les types d'assignments considérés comme long terme.
     * 
     * @return Liste des types long terme (> 7 jours)
     */
    public static List<AssignmentType> getLongTermTypes() {
        return Arrays.stream(values())
                .filter(type -> type.estimatedDays > 7)
                .collect(Collectors.toList());
    }
    
    // Méthodes utilitaires d'instance
    
    /**
     * Vérifie si ce type d'assignment est considéré comme long terme.
     * 
     * @return true si la durée estimée est supérieure à 7 jours
     */
    public boolean isLongTerm() {
        return this.estimatedDays > 7;
    }
    
    /**
     * Vérifie si ce type d'assignment est rapide (1 jour ou moins).
     * 
     * @return true si la durée estimée est de 1 jour ou moins
     */
    public boolean isQuickTask() {
        return this.estimatedDays <= 1;
    }
    
    /**
     * Vérifie si ce type d'assignment est lié à la programmation.
     * 
     * @return true si c'est un défi de code, projet ou laboratoire
     */
    public boolean isProgrammingRelated() {
        return this == CODE_CHALLENGE || this == PROJECT || this == LAB;
    }
    
    /**
     * Vérifie si ce type d'assignment est académique (théorique).
     * 
     * @return true si c'est un essai, recherche ou examen
     */
    public boolean isAcademic() {
        return this == ESSAY || this == RESEARCH || this == EXAM;
    }
    
    /**
     * Retourne une description complète avec métadonnées.
     * 
     * @return Description formatée avec durée et caractéristiques
     */
    public String getFullDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append(description);
        sb.append(" (Durée estimée : ").append(estimatedDays).append(" jour");
        if (estimatedDays > 1) sb.append("s");
        sb.append(")");
        
        if (requiresSupervision) {
            sb.append(" - Surveillance requise");
        }
        if (allowsMultipleSubmissions) {
            sb.append(" - Soumissions multiples autorisées");
        }
        
        return sb.toString();
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}