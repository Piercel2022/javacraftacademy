package com.javacraftacademy.courseservice.entity;

// Imports pour JPA et persistance
import jakarta.persistence.*;
// Imports pour Lombok - génération automatique de code
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
// Imports pour Hibernate - soft delete et filtres
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
// Imports pour les collections Java
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
// Imports pour validation (pour futures améliorations)
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
// Imports pour JSON (pour API REST)
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonBackReference;

/**
 * Entité Category - Système de catégorisation hiérarchique des cours
 * 
 * <p>Cette classe implémente un système de catégories arborescentes permettant
 * d'organiser les cours de manière hiérarchique dans l'application JavaCraft Academy.
 * Elle supporte une structure parent-enfant illimitée avec gestion de métadonnées
 * et optimisations pour l'affichage et la navigation.</p>
 * 
 * <h3>Architecture hiérarchique :</h3>
 * <ul>
 *   <li><strong>Catégories racines</strong> : Domaines principaux (ex: Programmation, Design)</li>
 *   <li><strong>Sous-catégories</strong> : Spécialisations (ex: Java, Python sous Programmation)</li>
 *   <li><strong>Catégories feuilles</strong> : Niveau le plus spécifique contenant les cours</li>
 *   <li><strong>Profondeur illimitée</strong> : Support théorique de n niveaux de hiérarchie</li>
 * </ul>
 * 
 * <h3>Fonctionnalités principales :</h3>
 * <ul>
 *   <li>Gestion hiérarchique parent-enfant avec navigation bidirectionnelle</li>
 *   <li>Système de slug unique pour URLs SEO-friendly</li>
 *   <li>Compteurs automatiques de cours et d'inscriptions</li>
 *   <li>Métadonnées SEO complètes (title, description, keywords)</li>
 *   <li>Système d'activation/désactivation en cascade</li>
 *   <li>Mise en avant (featured) pour promotions</li>
 *   <li>Personnalisation visuelle (icône, couleur)</li>
 *   <li>Tri et ordonnancement personnalisé</li>
 *   <li>Soft delete pour préservation de l'historique</li>
 * </ul>
 * 
 * @author JavaCraft Academy Team
 * @version 1.0
 * @since 1.0
 * 
 * @see Course
 * @see BaseEntity
 */
@Entity
@Table(name = "categories", 
       indexes = {
           @Index(name = "idx_category_slug", columnList = "slug"),
           @Index(name = "idx_category_parent", columnList = "parent_category_id"),
           @Index(name = "idx_category_active", columnList = "is_active"),
           @Index(name = "idx_category_featured", columnList = "is_featured"),
           @Index(name = "idx_category_order", columnList = "order_index"),
           @Index(name = "idx_category_parent_order", columnList = "parent_category_id, order_index")
       },
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_category_slug", columnNames = "slug")
       })
@Getter
@Setter
@EqualsAndHashCode(callSuper = true, exclude = {"parentCategory", "subCategories", "courses"})
@SQLDelete(sql = "UPDATE categories SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class Category extends BaseEntity {

    // ========== ATTRIBUTS PRINCIPAUX ==========

    /**
     * Nom de la catégorie
     * Affiché dans l'interface utilisateur et utilisé pour la navigation
     * Obligatoire, longueur max 100 caractères
     */
    @Column(nullable = false, length = 100)
    @NotBlank(message = "Le nom de la catégorie est obligatoire")
    @Size(max = 100, message = "Le nom ne peut pas dépasser 100 caractères")
    private String name;

    /**
     * Slug unique pour les URLs
     * Format: kebab-case (ex: programmation-web, intelligence-artificielle)
     * Utilisé pour des URLs SEO-friendly et la navigation
     */
    @Column(unique = true, nullable = false, length = 150)
    @NotBlank(message = "Le slug est obligatoire")
    @Size(max = 150, message = "Le slug ne peut pas dépasser 150 caractères")
    @Pattern(regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$", 
             message = "Le slug doit être en format kebab-case (ex: ma-categorie)")
    private String slug;

    /**
     * Description détaillée de la catégorie
     * Utilisée pour le SEO et l'information utilisateur
     */
    @Column(length = 500)
    @Size(max = 500, message = "La description ne peut pas dépasser 500 caractères")
    private String description;

    // ========== APPARENCE ET BRANDING ==========

    /**
     * URL de l'icône de la catégorie
     * Utilisée dans les menus, cartes de cours, breadcrumbs
     * Format suggéré: SVG ou PNG 64x64px
     */
    @Column(name = "icon_url", length = 500)
    private String iconUrl;

    /**
     * Code couleur hexadécimal pour le thème de la catégorie
     * Format strict: #RRGGBB (ex: #FF5722, #2196F3)
     * Utilisé pour la cohérence visuelle et le branding
     */
    @Column(name = "color_code", length = 7)
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", 
             message = "Le code couleur doit être au format hexadécimal #RRGGBB")
    private String colorCode;

    // ========== RELATIONS HIÉRARCHIQUES ==========

    /**
     * Catégorie parente pour la hiérarchie
     * null = catégorie racine
     * FetchType.LAZY pour optimiser les performances
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_category_id")
    @JsonBackReference // Évite les références circulaires JSON
    private Category parentCategory;

    /**
     * Liste des sous-catégories
     * Cascade ALL pour propager les opérations
     * orphanRemoval = true pour supprimer les orphelins
     * Ordonnées par orderIndex puis nom
     */
    @OneToMany(mappedBy = "parentCategory", 
               cascade = CascadeType.ALL, 
               orphanRemoval = true, 
               fetch = FetchType.LAZY)
    @OrderBy("orderIndex ASC, name ASC")
    @JsonManagedReference // Gestion des références circulaires JSON
    private List<Category> subCategories = new ArrayList<>();

    /**
     * Liste des cours dans cette catégorie
     * Cascade REMOVE uniquement (pas PERSIST pour éviter les créations accidentelles)
     */
    @OneToMany(mappedBy = "category", 
               cascade = CascadeType.REMOVE, 
               fetch = FetchType.LAZY)
    @JsonIgnore // Évite la sérialisation lourde
    private List<Course> courses = new ArrayList<>();

    // ========== ORGANISATION ET AFFICHAGE ==========

    /**
     * Index d'ordre pour le tri personnalisé
     * Permet un ordonnancement spécifique différent de l'alphabétique
     * Plus petit = affiché en premier
     */
    @Column(name = "order_index", nullable = false)
    private Integer orderIndex = 0;

    /**
     * Indicateur d'activation de la catégorie
     * false = catégorie désactivée (non visible publiquement)
     * Propagation en cascade dans la hiérarchie
     */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    /**
     * Indicateur de mise en avant
     * true = catégorie promue (affichage prioritaire, page d'accueil)
     */
    @Column(name = "is_featured", nullable = false)
    private Boolean isFeatured = false;

    // ========== COMPTEURS ET STATISTIQUES ==========

    /**
     * Nombre de cours dans cette catégorie
     * Compteur dénormalisé pour éviter les COUNT() coûteux
     * Mis à jour automatiquement via les méthodes utilitaires
     */
    @Column(name = "courses_count", nullable = false)
    private Integer coursesCount = 0;

    /**
     * Nombre total d'inscriptions dans les cours de cette catégorie
     * Agrégation pour analytics et recommandations
     */
    @Column(name = "total_enrollments", nullable = false)
    private Integer totalEnrollments = 0;

    // ========== MÉTADONNÉES SEO ==========

    /**
     * Titre SEO personnalisé
     * Si null, utilise le nom de la catégorie
     * Utilisé pour la balise <title> des pages
     */
    @Column(name = "meta_title", length = 200)
    @Size(max = 200, message = "Le titre SEO ne peut pas dépasser 200 caractères")
    private String metaTitle;

    /**
     * Description SEO pour la meta description
     * Utilisée pour les résultats de recherche Google
     * Longueur optimale: 150-160 caractères
     */
    @Column(name = "meta_description", length = 500)
    @Size(max = 500, message = "La meta description ne peut pas dépasser 500 caractères")
    private String metaDescription;

    /**
     * Mots-clés SEO séparés par des virgules
     * Utilisés pour le référencement et la recherche interne
     */
    @Column(name = "meta_keywords", length = 500)
    @Size(max = 500, message = "Les mots-clés ne peuvent pas dépasser 500 caractères")
    private String metaKeywords;

    // ========== MÉTHODES UTILITAIRES - STRUCTURE HIÉRARCHIQUE ==========

    /**
     * Vérifie si la catégorie est une catégorie racine
     * Une catégorie racine n'a pas de parent
     * 
     * @return true si c'est une catégorie racine, false sinon
     */
    public boolean isRootCategory() {
        return this.parentCategory == null;
    }

    /**
     * Vérifie si la catégorie a des sous-catégories
     * 
     * @return true si elle a des sous-catégories, false sinon
     */
    public boolean hasSubCategories() {
        return this.subCategories != null && !this.subCategories.isEmpty();
    }

    /**
     * Vérifie si la catégorie est une feuille (sans sous-catégories)
     * Les catégories feuilles contiennent généralement les cours
     * 
     * @return true si c'est une catégorie feuille, false sinon
     */
    public boolean isLeafCategory() {
        return !hasSubCategories();
    }

    /**
     * Calcule le niveau de profondeur dans la hiérarchie
     * Niveau 0 = catégorie racine, 1 = premier niveau, etc.
     * 
     * @return le niveau de profondeur (0 pour racine)
     */
    public int getLevel() {
        int level = 0;
        Category parent = this.parentCategory;
        while (parent != null) {
            level++;
            parent = parent.getParentCategory();
        }
        return level;
    }

    /**
     * Génère le chemin complet depuis la racine
     * Format: "Racine > Niveau1 > Niveau2 > Actuel"
     * Utilisé pour les breadcrumbs et la navigation
     * 
     * @return le chemin hiérarchique complet
     */
    public String getFullPath() {
        List<String> names = new ArrayList<>();
        
        Category current = this;
        while (current != null) {
            names.add(0, current.getName());
            current = current.getParentCategory();
        }
        
        return String.join(" > ", names);
    }

    /**
     * Récupère toutes les sous-catégories récursivement
     * Parcours en profondeur de l'arbre de catégories
     * Utile pour les opérations en masse
     * 
     * @return liste de toutes les sous-catégories (tous niveaux)
     */
    public List<Category> getAllSubCategories() {
        List<Category> allSubs = new ArrayList<>();
        if (this.subCategories != null) {
            for (Category sub : this.subCategories) {
                allSubs.add(sub);
                allSubs.addAll(sub.getAllSubCategories());
            }
        }
        return allSubs;
    }

    /**
     * Récupère tous les cours de cette catégorie et de ses sous-catégories
     * Agrégation récursive pour affichage complet
     * 
     * @return liste de tous les cours dans la branche
     */
    public List<Course> getAllCoursesInBranch() {
        List<Course> allCourses = new ArrayList<>();
        
        // Courses de cette catégorie
        if (this.courses != null) {
            allCourses.addAll(this.courses);
        }
        
        // Courses des sous-catégories récursivement
        for (Category subCategory : getAllSubCategories()) {
            if (subCategory.getCourses() != null) {
                allCourses.addAll(subCategory.getCourses());
            }
        }
        
        return allCourses;
    }

    // ========== MÉTHODES UTILITAIRES - COMPTEURS ==========

    /**
     * Incrémente le compteur de cours
     * Appelée automatiquement lors de l'ajout d'un cours
     */
    public void incrementCoursesCount() {
        this.coursesCount = (this.coursesCount == null ? 0 : this.coursesCount) + 1;
    }

    /**
     * Décrémente le compteur de cours
     * Appelée automatiquement lors de la suppression d'un cours
     * Ne peut pas descendre en dessous de 0
     */
    public void decrementCoursesCount() {
        this.coursesCount = Math.max(0, (this.coursesCount == null ? 0 : this.coursesCount) - 1);
    }

    /**
     * Incrémente le compteur total d'inscriptions
     * Utilisé pour les statistiques et la popularité
     * 
     * @param count nombre d'inscriptions à ajouter
     */
    public void incrementTotalEnrollments(int count) {
        if (count < 0) {
            throw new IllegalArgumentException("Le nombre d'inscriptions doit être positif");
        }
        this.totalEnrollments = (this.totalEnrollments == null ? 0 : this.totalEnrollments) + count;
    }

    /**
     * Recalcule les compteurs à partir des données réelles
     * Méthode de synchronisation en cas d'incohérence
     */
    public void recalculateCounters() {
        this.coursesCount = this.courses != null ? this.courses.size() : 0;
        
        int totalEnroll = 0;
        if (this.courses != null) {
            for (Course course : this.courses) {
                // Assuming Course has a method to get enrollment count
                // You may need to adjust this based on your Course entity structure
                Integer courseEnrollments = getEnrollmentCountForCourse(course);
                totalEnroll += courseEnrollments != null ? courseEnrollments : 0;
            }
        }
        this.totalEnrollments = totalEnroll;
    }

    /**
     * Helper method to get enrollment count for a course
     * This method should be implemented based on your Course entity structure
     */
    private Integer getEnrollmentCountForCourse(Course course) {
        // This is a placeholder - implement based on your Course entity
        // For example: return course.getEnrollments().size();
        // or return course.getEnrollmentCount();
        return 0; // Default implementation
    }

    // ========== MÉTHODES UTILITAIRES - ÉTAT ET VALIDATION ==========

    /**
     * Vérifie si toute la branche de catégories est active
     * Une catégorie n'est visible que si elle ET tous ses parents sont actifs
     * 
     * @return true si la catégorie et tous ses parents sont actifs
     */
    public boolean isActiveCategoryTree() {
        if (!Boolean.TRUE.equals(this.isActive)) {
            return false;
        }
        
        Category parent = this.parentCategory;
        while (parent != null) {
            if (!Boolean.TRUE.equals(parent.getIsActive())) {
                return false;
            }
            parent = parent.getParentCategory();
        }
        return true;
    }

    /**
     * Vérifie s'il y a un cycle dans la hiérarchie
     * Détecte les références circulaires dans l'arbre
     * 
     * @param targetParent la nouvelle catégorie parente proposée
     * @return true si ça créerait un cycle, false sinon
     */
    public boolean wouldCreateCycle(Category targetParent) {
        if (targetParent == null) {
            return false;
        }
        
        Category current = targetParent;
        while (current != null) {
            if (current.equals(this)) {
                return true;
            }
            current = current.getParentCategory();
        }
        return false;
    }

    /**
     * Active/désactive récursivement toutes les sous-catégories
     * Propagation en cascade de l'état d'activation
     * 
     * @param active true pour activer, false pour désactiver
     */
    public void setActiveRecursively(boolean active) {
        this.isActive = active;
        if (this.subCategories != null) {
            for (Category subCategory : this.subCategories) {
                subCategory.setActiveRecursively(active);
            }
        }
    }

    // ========== MÉTHODES UTILITAIRES - SEO ET AFFICHAGE ==========

    /**
     * Retourne le titre SEO ou le nom si pas défini
     * 
     * @return le titre optimisé pour SEO
     */
    public String getEffectiveMetaTitle() {
        return metaTitle != null && !metaTitle.trim().isEmpty() ? metaTitle : name;
    }

    /**
     * Retourne la description SEO ou la description standard si pas définie
     * 
     * @return la description optimisée pour SEO
     */
    public String getEffectiveMetaDescription() {
        return metaDescription != null && !metaDescription.trim().isEmpty() ? metaDescription : description;
    }

    /**
     * Génère une URL complète basée sur la hiérarchie
     * Format: /categories/parent-slug/child-slug/current-slug
     * 
     * @return l'URL hiérarchique complète
     */
    public String getHierarchicalUrl() {
        List<String> slugs = new ArrayList<>();
        
        Category current = this;
        while (current != null) {
            slugs.add(0, current.getSlug());
            current = current.getParentCategory();
        }
        
        return "/categories/" + String.join("/", slugs);
    }

    // ========== HOOKS JPA ==========

    /**
     * Validation avant persistance et mise à jour
     * Vérifications métier et cohérence des données
     */
    @PrePersist
    @PreUpdate
    private void validateCategory() {
        // Validation du nom
        if (this.name == null || this.name.trim().isEmpty()) {
            throw new IllegalStateException("Le nom de la catégorie ne peut pas être vide");
        }
        
        // Validation du slug
        if (this.slug == null || this.slug.trim().isEmpty()) {
            throw new IllegalStateException("Le slug de la catégorie ne peut pas être vide");
        }
        
        // Normalisation du slug
        this.slug = this.slug.toLowerCase().trim();
        
        // Vérification des références circulaires
        if (this.parentCategory != null && this.parentCategory.equals(this)) {
            throw new IllegalStateException("Une catégorie ne peut pas être son propre parent");
        }
        
        // Validation approfondie des cycles (coûteux mais nécessaire)
        if (this.parentCategory != null && wouldCreateCycle(this.parentCategory)) {
            throw new IllegalStateException("Cette affectation créerait une référence circulaire");
        }
        
        // Validation du code couleur
        if (this.colorCode != null && !this.colorCode.matches("^#[0-9A-Fa-f]{6}$")) {
            throw new IllegalStateException("Le code couleur doit être au format hexadécimal #RRGGBB");
        }
        
        // Normalisation des compteurs
        if (this.coursesCount == null) this.coursesCount = 0;
        if (this.totalEnrollments == null) this.totalEnrollments = 0;
        if (this.orderIndex == null) this.orderIndex = 0;
        
        // Validation des longueurs
        if (this.name.length() > 100) {
            throw new IllegalStateException("Le nom ne peut pas dépasser 100 caractères");
        }
        
        if (this.slug.length() > 150) {
            throw new IllegalStateException("Le slug ne peut pas dépasser 150 caractères");
        }
    }

    /**
     * Actions après persistance
     * Mise à jour des compteurs du parent si nécessaire
     */
    @PostPersist
    private void afterPersist() {
        // Logique additionnelle si nécessaire
        // Ex: invalidation de cache, notification, etc.
    }

    /**
     * Actions avant suppression
     * Vérifications de contraintes métier
     */
    @PreRemove
    private void beforeRemove() {
        // Vérifier qu'il n'y a pas de cours actifs
        if (this.courses != null && !this.courses.isEmpty()) {
            long activeCourses = this.courses.stream()
                .filter(course -> isPublishedCourse(course))
                .count();
            
            if (activeCourses > 0) {
                throw new IllegalStateException(
                    "Impossible de supprimer une catégorie contenant des cours publiés");
            }
        }
    }

    /**
     * Helper method to check if a course is published
     * This method should be implemented based on your Course entity structure
     */
    private boolean isPublishedCourse(Course course) {
        // This is a placeholder - implement based on your Course entity
        // For example: return course.isPublished();
        return false; // Default implementation
    }

    // ========== MÉTHODES D'EXTENSION FUTURE ==========

    /**
     * Support multi-langue (préparation future)
     * Retourne le nom dans la langue spécifiée
     * 
     * @param locale code de langue (ex: "fr", "en", "es")
     * @return le nom traduit ou le nom par défaut
     */
    public String getLocalizedName(String locale) {
        // Implémentation future avec table de traductions
        // Pour l'instant, retourne le nom par défaut
        return this.name;
    }

    /**
     * Calcul de score de popularité (préparation future)
     * Basé sur le nombre de cours, inscriptions, et activité récente
     * 
     * @return score de popularité (0-100)
     */
    public double getPopularityScore() {
        // Algorithme à implémenter :
        // - Nombre de cours actifs (30%)
        // - Nombre d'inscriptions récentes (40%)
        // - Taux de completion des cours (20%)
        // - Ratings moyens des cours (10%)
        
        double courseWeight = Math.min(this.coursesCount * 2, 30);
        double enrollmentWeight = Math.min(this.totalEnrollments * 0.1, 40);
        
        return Math.min(courseWeight + enrollmentWeight, 100.0);
    }

    /**
     * Génération de tags automatiques (préparation IA future)
     * Analyse le contenu des cours pour générer des tags pertinents
     * 
     * @return set de tags suggérés
     */
    public Set<String> generateAutoTags() {
        Set<String> tags = new HashSet<>();
        
        // Analyse des titres et descriptions des cours
        // Implémentation future avec NLP/ML
        
        // Pour l'instant, tags basiques basés sur le nom
        String[] words = this.name.toLowerCase().split("\\s+");
        for (String word : words) {
            if (word.length() > 3) { // Éviter les mots trop courts
                tags.add(word);
            }
        }
        
        return tags;
    }
}