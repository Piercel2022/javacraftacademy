package com.javacraftacademy.courseservice.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * �num�ration repr�sentant les diff�rents niveaux de difficult� des cours dans JavaCraft Academy.
 * 
 * <p>Cette �num�ration d�finit une hi�rarchie de niveaux permettant de classer les cours selon
 * leur complexit� et les pr�requis n�cessaires. Elle est utilis�e pour :
 * <ul>
 *   <li>Filtrer les cours par niveau de difficult�</li>
 *   <li>Recommander des parcours d'apprentissage progressifs</li>
 *   <li>Valider les pr�requis avant l'inscription</li>
 *   <li>Adapter l'interface utilisateur selon le niveau</li>
 * </ul>
 * 
 * <h3>Relations dans l'application :</h3>
 * <ul>
 *   <li><strong>Course Entity</strong> : Chaque cours poss�de un niveau d�fini</li>
 *   <li><strong>User Profiling</strong> : Utilis� pour matcher les utilisateurs avec des cours appropri�s</li>
 *   <li><strong>Search & Filtering</strong> : Crit�re de recherche dans CourseController</li>
 *   <li><strong>Recommendation Engine</strong> : Base pour les suggestions de cours</li>
 *   <li><strong>Progress Tracking</strong> : Validation de la progression logique</li>
 * </ul>
 * 
 * @author JavaCraft Academy Team
 * @version 1.0
 * @since 1.0
 */
public enum CourseLevel {
    
    /**
     * Niveau d�butant - Aucun pr�requis n�cessaire.
     * Cours d'introduction aux concepts fondamentaux.
     */
    BEGINNER(1, "D�butant", "Aucun pr�requis n�cessaire", "#4CAF50"),
    
    /**
     * Niveau interm�diaire - Pr�requis : bases solides.
     * Approfondissement des concepts avec des projets pratiques.
     */
    INTERMEDIATE(2, "Interm�diaire", "Bases solides requises", "#FF9800"),
    
    /**
     * Niveau avanc� - Pr�requis : exp�rience significative.
     * Concepts complexes et architectures avanc�es.
     */
    ADVANCED(3, "Avanc�", "Exp�rience significative requise", "#F44336"),
    
    /**
     * Niveau expert - Pr�requis : ma�trise avanc�e.
     * Sujets de pointe et cas d'usage sp�cialis�s.
     */
    EXPERT(4, "Expert", "Ma�trise avanc�e requise", "#9C27B0");
    
    private final int order;
    private final String displayName;
    private final String prerequisiteDescription;
    private final String colorCode;
    
    /**
     * Constructeur de l'�num�ration CourseLevel.
     * 
     * @param order L'ordre num�rique du niveau (1 = plus facile, 4 = plus difficile)
     * @param displayName Le nom d'affichage du niveau
     * @param prerequisiteDescription Description des pr�requis
     * @param colorCode Code couleur hexad�cimal pour l'affichage UI
     */
    CourseLevel(int order, String displayName, String prerequisiteDescription, String colorCode) {
        this.order = order;
        this.displayName = displayName;
        this.prerequisiteDescription = prerequisiteDescription;
        this.colorCode = colorCode;
    }
    
    /**
     * Retourne l'ordre num�rique du niveau.
     * Utilis� pour les comparaisons et le tri.
     * 
     * @return L'ordre du niveau (1-4)
     */
    public int getOrder() {
        return order;
    }
    
    /**
     * Retourne le nom d'affichage localis� du niveau.
     * 
     * @return Le nom d'affichage
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Retourne la description des pr�requis pour ce niveau.
     * 
     * @return La description des pr�requis
     */
    public String getPrerequisiteDescription() {
        return prerequisiteDescription;
    }
    
    /**
     * Retourne le code couleur hexad�cimal associ� au niveau.
     * Utilis� pour la repr�sentation visuelle dans l'interface.
     * 
     * @return Le code couleur hexad�cimal
     */
    public String getColorCode() {
        return colorCode;
    }
    
    /**
     * V�rifie si ce niveau est plus �lev� que le niveau sp�cifi�.
     * 
     * @param other Le niveau � comparer
     * @return true si ce niveau est plus �lev�, false sinon
     */
    public boolean isHigherThan(CourseLevel other) {
        return this.order > other.order;
    }
    
    /**
     * V�rifie si ce niveau est plus bas que le niveau sp�cifi�.
     * 
     * @param other Le niveau � comparer
     * @return true si ce niveau est plus bas, false sinon
     */
    public boolean isLowerThan(CourseLevel other) {
        return this.order < other.order;
    }
    
    /**
     * Retourne le niveau suivant dans la progression.
     * 
     * @return Le niveau suivant, ou null si c'est d�j� le niveau le plus �lev�
     */
    public CourseLevel getNextLevel() {
        CourseLevel[] levels = values();
        for (int i = 0; i < levels.length - 1; i++) {
            if (levels[i] == this) {
                return levels[i + 1];
            }
        }
        return null; // D�j� au niveau maximum
    }
    
    /**
     * Retourne le niveau pr�c�dent dans la progression.
     * 
     * @return Le niveau pr�c�dent, ou null si c'est d�j� le niveau le plus bas
     */
    public CourseLevel getPreviousLevel() {
        CourseLevel[] levels = values();
        for (int i = 1; i < levels.length; i++) {
            if (levels[i] == this) {
                return levels[i - 1];
            }
        }
        return null; // D�j� au niveau minimum
    }
    
    /**
     * Calcule la diff�rence de niveau entre ce niveau et un autre.
     * 
     * @param other L'autre niveau
     * @return La diff�rence (positif si ce niveau est plus �lev�)
     */
    public int getLevelDifference(CourseLevel other) {
        return this.order - other.order;
    }
    
    /**
     * Retourne tous les niveaux inf�rieurs ou �gaux � ce niveau.
     * Utile pour d�terminer les cours accessibles � un utilisateur.
     * 
     * @return Tableau des niveaux accessibles
     */
    public CourseLevel[] getAccessibleLevels() {
        CourseLevel[] allLevels = values();
        CourseLevel[] accessible = new CourseLevel[this.order];
        
        for (int i = 0; i < this.order; i++) {
            accessible[i] = allLevels[i];
        }
        
        return accessible;
    }
    
    /**
     * D�termine si un utilisateur peut acc�der � ce niveau en fonction
     * de son niveau actuel.
     * 
     * @param userCurrentLevel Le niveau actuel de l'utilisateur
     * @param allowSkipOneLevel Autorise-t-il de sauter un niveau
     * @return true si l'acc�s est autoris�
     */
    public boolean isAccessibleFor(CourseLevel userCurrentLevel, boolean allowSkipOneLevel) {
        if (userCurrentLevel == null) {
            return this == BEGINNER;
        }
        
        int levelGap = this.order - userCurrentLevel.order;
        return levelGap <= (allowSkipOneLevel ? 2 : 1);
    }
    
    /**
     * Retourne une estimation du temps d'�tude moyen pour ce niveau.
     * 
     * @return Nombre d'heures estim�es par cours de ce niveau
     */
    public int getEstimatedStudyHours() {
        switch (this) {
            case BEGINNER: return 20;
            case INTERMEDIATE: return 35;
            case ADVANCED: return 50;
            case EXPERT: return 80;
            default: return 30;
        }
    }
    
    /**
     * Retourne une liste de comp�tences typiques d�velopp�es � ce niveau.
     * 
     * @return Tableau des comp�tences du niveau
     */
    public String[] getTypicalSkills() {
        switch (this) {
            case BEGINNER:
                return new String[]{"Syntaxe de base", "Concepts fondamentaux", "Premiers projets"};
            case INTERMEDIATE:
                return new String[]{"OOP", "Collections", "Gestion d'erreurs", "Tests unitaires"};
            case ADVANCED:
                return new String[]{"Design Patterns", "Architecture", "Performance", "Frameworks"};
            case EXPERT:
                return new String[]{"Microservices", "S�curit� avanc�e", "Optimisation", "Architecture distribu�e"};
            default:
                return new String[]{};
        }
    }
    
    /**
     * S�rialisation JSON - retourne le nom de l'�num�ration.
     * 
     * @return Le nom de l'�num�ration pour JSON
     */
    @JsonValue
    public String toJson() {
        return this.name();
    }
    
    /**
     * D�s�rialisation JSON - cr�e une instance � partir d'une cha�ne.
     * 
     * @param value La valeur JSON
     * @return L'instance CourseLevel correspondante
     * @throws IllegalArgumentException si la valeur n'est pas valide
     */
    @JsonCreator
    public static CourseLevel fromJson(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("CourseLevel value cannot be null or empty");
        }
        
        try {
            return CourseLevel.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid CourseLevel: " + value + 
                ". Valid values are: " + java.util.Arrays.toString(values()));
        }
    }
    
    /**
     * M�thode utilitaire pour obtenir un niveau par son ordre.
     * 
     * @param order L'ordre du niveau (1-4)
     * @return Le CourseLevel correspondant
     * @throws IllegalArgumentException si l'ordre n'est pas valide
     */
    public static CourseLevel fromOrder(int order) {
        for (CourseLevel level : values()) {
            if (level.order == order) {
                return level;
            }
        }
        throw new IllegalArgumentException("Invalid order: " + order + 
            ". Valid orders are: 1-" + values().length);
    }
    
    /**
     * Retourne une repr�sentation textuelle enrichie du niveau.
     * 
     * @return Description compl�te du niveau
     */
    @Override
    public String toString() {
        return String.format("%s (Niveau %d) - %s", 
            displayName, order, prerequisiteDescription);
    }
}