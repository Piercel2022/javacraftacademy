package com.javacraftacademy.courseservice.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * Énumération représentant les différents statuts du cycle de vie d'un cours dans JavaCraft Academy.
 * 
 * <p>Cette énumération modélise le workflow complet de gestion des cours, depuis leur création
 * jusqu'à leur archivage. Elle permet de contrôler la visibilité, l'accessibilité et les actions
 * autorisées sur chaque cours selon son état actuel.
 * 
 * <h3>Cycle de vie typique :</h3>
 * <ol>
 *   <li><strong>DRAFT</strong> : Cours en création/modification</li>
 *   <li><strong>UNDER_REVIEW</strong> : Soumis pour validation</li>
 *   <li><strong>PUBLISHED</strong> : Disponible pour inscription</li>
 *   <li><strong>SUSPENDED</strong> : Temporairement indisponible</li>
 *   <li><strong>ARCHIVED</strong> : Historisé, non accessible</li>
 * </ol>
 * 
 * <h3>Relations dans l'application :</h3>
 * <ul>
 *   <li><strong>Course Entity</strong> : Chaque cours possède un statut définissant ses règles</li>
 *   <li><strong>Enrollment Service</strong> : Contrôle les inscriptions selon le statut</li>
 *   <li><strong>Course Controller</strong> : Filtrage et autorisation d'accès</li>
 *   <li><strong>Search Engine</strong> : Indexation basée sur le statut</li>
 *   <li><strong>Notification Service</strong> : Alertes sur changements de statut</li>
 *   <li><strong>Admin Dashboard</strong> : Workflow de gestion des cours</li>
 *   <li><strong>Content Management</strong> : Règles d'édition selon le statut</li>
 * </ul>
 * 
 * @author JavaCraft Academy Team
 * @version 1.0
 * @since 1.0
 */
public enum CourseStatus {
    
    /**
     * Cours en brouillon - En cours de création ou modification.
     * <ul>
     *   <li>Visible uniquement par l'auteur et les administrateurs</li>
     *   <li>Modifications autorisées</li>
     *   <li>Pas d'inscription possible</li>
     *   <li>Pas d'indexation dans les recherches publiques</li>
     * </ul>
     */
    DRAFT("Brouillon", "En cours de création", false, true, false, "#9E9E9E"),
    
    /**
     * Cours en révision - Soumis pour validation par l'équipe pédagogique.
     * <ul>
     *   <li>Visible par l'auteur, réviseurs et administrateurs</li>
     *   <li>Modifications limitées (corrections mineures)</li>
     *   <li>Pas d'inscription possible</li>
     *   <li>En attente d'approbation</li>
     * </ul>
     */
    UNDER_REVIEW("En révision", "En attente de validation", false, false, false, "#FF9800"),
    
    /**
     * Cours publié - Disponible pour les étudiants.
     * <ul>
     *   <li>Visible publiquement</li>
     *   <li>Inscriptions ouvertes</li>
     *   <li>Modifications restreintes</li>
     *   <li>Indexé dans les recherches</li>
     * </ul>
     */
    PUBLISHED("Publié", "Disponible aux étudiants", true, false, true, "#4CAF50"),
    
    /**
     * Cours suspendu - Temporairement indisponible.
     * <ul>
     *   <li>Visible pour les inscrits existants</li>
     *   <li>Nouvelles inscriptions bloquées</li>
     *   <li>Accès au contenu maintenu pour les inscrits</li>
     *   <li>Retiré des recherches publiques</li>
     * </ul>
     */
    SUSPENDED("Suspendu", "Temporairement indisponible", false, false, false, "#FF5722"),
    
    /**
     * Cours archivé - Retiré définitivement de l'offre.
     * <ul>
     *   <li>Invisible publiquement</li>
     *   <li>Pas d'inscription possible</li>
     *   <li>Accès lecture seule pour les anciens inscrits</li>
     *   <li>Conservé pour l'historique</li>
     * </ul>
     */
    ARCHIVED("Archivé", "Retiré de l'offre", false, false, false, "#607D8B");
    
    private final String displayName;
    private final String description;
    private final boolean publiclyVisible;
    private final boolean editable;
    private final boolean enrollmentAllowed;
    private final String colorCode;
    
    /**
     * Constructeur de l'énumération CourseStatus.
     * 
     * @param displayName Nom d'affichage du statut
     * @param description Description détaillée du statut
     * @param publiclyVisible Si le cours est visible publiquement
     * @param editable Si le cours peut être modifié
     * @param enrollmentAllowed Si les inscriptions sont autorisées
     * @param colorCode Code couleur hexadécimal pour l'affichage UI
     */
    CourseStatus(String displayName, String description, boolean publiclyVisible, 
                boolean editable, boolean enrollmentAllowed, String colorCode) {
        this.displayName = displayName;
        this.description = description;
        this.publiclyVisible = publiclyVisible;
        this.editable = editable;
        this.enrollmentAllowed = enrollmentAllowed;
        this.colorCode = colorCode;
    }
    
    /**
     * Retourne le nom d'affichage localisé du statut.
     * 
     * @return Le nom d'affichage
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Retourne la description détaillée du statut.
     * 
     * @return La description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Indique si le cours est visible publiquement dans ce statut.
     * 
     * @return true si visible publiquement
     */
    public boolean isPubliclyVisible() {
        return publiclyVisible;
    }
    
    /**
     * Indique si le cours peut être modifié dans ce statut.
     * 
     * @return true si éditable
     */
    public boolean isEditable() {
        return editable;
    }
    
    /**
     * Indique si les inscriptions sont autorisées dans ce statut.
     * 
     * @return true si les inscriptions sont possibles
     */
    public boolean isEnrollmentAllowed() {
        return enrollmentAllowed;
    }
    
    /**
     * Retourne le code couleur hexadécimal associé au statut.
     * 
     * @return Le code couleur
     */
    public String getColorCode() {
        return colorCode;
    }
    
    /**
     * Vérifie si une transition vers le statut spécifié est autorisée.
     * 
     * @param targetStatus Le statut de destination
     * @return true si la transition est valide
     */
    public boolean canTransitionTo(CourseStatus targetStatus) {
        if (this == targetStatus) {
            return false; // Pas de transition vers le même statut
        }
        
        switch (this) {
            case DRAFT:
                return targetStatus == UNDER_REVIEW || targetStatus == ARCHIVED;
                
            case UNDER_REVIEW:
                return targetStatus == DRAFT || targetStatus == PUBLISHED || targetStatus == ARCHIVED;
                
            case PUBLISHED:
                return targetStatus == SUSPENDED || targetStatus == ARCHIVED || targetStatus == UNDER_REVIEW;
                
            case SUSPENDED:
                return targetStatus == PUBLISHED || targetStatus == ARCHIVED;
                
            case ARCHIVED:
                return false; // Aucune transition possible depuis archivé
                
            default:
                return false;
        }
    }
    
    /**
     * Retourne la liste des statuts vers lesquels une transition est possible.
     * 
     * @return Liste des statuts de destination valides
     */
    public List<CourseStatus> getAllowedTransitions() {
        return Arrays.stream(values())
                .filter(this::canTransitionTo)
                .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * Vérifie si ce statut nécessite une validation manuelle.
     * 
     * @return true si une validation est requise
     */
    public boolean requiresApproval() {
        return this == UNDER_REVIEW;
    }
    
    /**
     * Vérifie si ce statut permet l'accès au contenu du cours.
     * 
     * @param isEnrolled Si l'utilisateur est inscrit au cours
     * @return true si l'accès au contenu est autorisé
     */
    public boolean allowsContentAccess(boolean isEnrolled) {
        switch (this) {
            case PUBLISHED:
                return true;
            case SUSPENDED:
            case ARCHIVED:
                return isEnrolled;
            case DRAFT:
            case UNDER_REVIEW:
            default:
                return false;
        }
    }
    
    /**
     * Détermine si ce statut doit être inclus dans les recherches publiques.
     * 
     * @return true si indexé dans la recherche
     */
    public boolean isSearchable() {
        return this == PUBLISHED;
    }
    
    /**
     * Vérifie si ce statut permet les notifications aux étudiants.
     * 
     * @return true si les notifications sont autorisées
     */
    public boolean allowsNotifications() {
        return this == PUBLISHED || this == SUSPENDED;
    }
    
    /**
     * Retourne le niveau de priorité du statut pour les tris.
     * Plus la valeur est faible, plus la priorité est élevée.
     * 
     * @return Niveau de priorité (0-4)
     */
    public int getPriority() {
        switch (this) {
            case PUBLISHED: return 0;
            case UNDER_REVIEW: return 1;
            case DRAFT: return 2;
            case SUSPENDED: return 3;
            case ARCHIVED: return 4;
            default: return 5;
        }
    }
    
    /**
     * Calcule le délai recommandé avant transition automatique (en jours).
     * Retourne -1 si aucune transition automatique n'est recommandée.
     * 
     * @return Nombre de jours, ou -1 si pas applicable
     */
    public int getAutoTransitionDelayDays() {
        switch (this) {
            case UNDER_REVIEW: return 7; // 7 jours pour révision
            case SUSPENDED: return 30; // 30 jours avant archivage
            default: return -1;
        }
    }
    
    /**
     * Détermine le statut suivant en cas de transition automatique.
     * 
     * @return Le statut de transition automatique, ou null si pas applicable
     */
    public CourseStatus getAutoTransitionTarget() {
        switch (this) {
            case UNDER_REVIEW: return DRAFT; // Retour en brouillon si pas validé
            case SUSPENDED: return ARCHIVED; // Archivage après suspension prolongée
            default: return null;
        }
    }
    
    /**
     * Valide si une action spécifique est autorisée dans ce statut.
     * 
     * @param action L'action à valider
     * @return true si l'action est autorisée
     */
    public boolean isActionAllowed(CourseAction action) {
        switch (action) {
            case EDIT_CONTENT:
                return isEditable();
            case VIEW_PUBLIC:
                return isPubliclyVisible();
            case ENROLL:
                return isEnrollmentAllowed();
            case DELETE:
                return this == DRAFT || this == ARCHIVED;
            case SUBMIT_REVIEW:
                return this == DRAFT;
            case APPROVE:
                return this == UNDER_REVIEW;
            case SUSPEND:
                return this == PUBLISHED;
            case ARCHIVE:
                return this != ARCHIVED;
            default:
                return false;
        }
    }
    
    /**
     * Génère un message de statut contextualisé avec horodatage.
     * 
     * @param lastUpdated Date de dernière mise à jour
     * @return Message de statut formaté
     */
    public String getStatusMessage(LocalDateTime lastUpdated) {
        String baseMessage = String.format("Statut : %s - %s", displayName, description);
        
        if (lastUpdated != null) {
            baseMessage += String.format(" (Mise à jour : %s)", 
                lastUpdated.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        }
        
        return baseMessage;
    }
    
    /**
     * Actions possibles sur un cours selon son statut.
     */
    public enum CourseAction {
        EDIT_CONTENT, VIEW_PUBLIC, ENROLL, DELETE, 
        SUBMIT_REVIEW, APPROVE, SUSPEND, ARCHIVE
    }
    
    /**
     * Sérialisation JSON - retourne le nom de l'énumération.
     * 
     * @return Le nom de l'énumération pour JSON
     */
    @JsonValue
    public String toJson() {
        return this.name();
    }
    
    /**
     * Désérialisation JSON - crée une instance à partir d'une chaîne.
     * 
     * @param value La valeur JSON
     * @return L'instance CourseStatus correspondante
     * @throws IllegalArgumentException si la valeur n'est pas valide
     */
    @JsonCreator
    public static CourseStatus fromJson(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("CourseStatus value cannot be null or empty");
        }
        
        try {
            return CourseStatus.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid CourseStatus: " + value + 
                ". Valid values are: " + Arrays.toString(values()));
        }
    }
    
    /**
     * Retourne tous les statuts visibles publiquement.
     * 
     * @return Liste des statuts publics
     */
    public static List<CourseStatus> getPublicStatuses() {
        return Arrays.stream(values())
                .filter(CourseStatus::isPubliclyVisible)
                .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * Retourne les statuts permettant l'inscription.
     * 
     * @return Liste des statuts avec inscription ouverte
     */
    public static List<CourseStatus> getEnrollableStatuses() {
        return Arrays.stream(values())
                .filter(CourseStatus::isEnrollmentAllowed)
                .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * Retourne une représentation textuelle complète du statut.
     * 
     * @return Description détaillée du statut
     */
    @Override
    public String toString() {
        return String.format("%s - %s (Public: %s, Éditable: %s, Inscription: %s)", 
            displayName, description, publiclyVisible, editable, enrollmentAllowed);
    }
}