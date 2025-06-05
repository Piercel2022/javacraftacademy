package com.javacraftacademy.courseservice.entity;

// Imports pour JPA et persistance
import jakarta.persistence.*;
// Imports pour Lombok - génération automatique de code
import lombok.Data;
import lombok.EqualsAndHashCode;
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
 * <h3>Relations dans l'écosystème JavaCraft Academy :</h3>
 * <ul>
 *   <li><strong>Course</strong> : Relation OneToMany - Une catégorie contient plusieurs cours</li>
 *   <li><strong>Self-Reference</strong> : Relation ManyToOne/OneToMany pour la hiérarchie</li>
 *   <li><strong>Instructor</strong> : Accès indirect pour spécialisations par domaine</li>
 *   <li><strong>Student</strong> : Accès indirect pour préférences et recommandations</li>
 *   <li><strong>Enrollment</strong> : Statistiques agrégées par catégorie</li>
 *   <li><strong>SearchIndex</strong> : Indexation pour moteur de recherche interne</li>
 * </ul>
 * 
 * <h3>Intégration système :</h3>
 * <ul>
 *   <li><strong>Navigation website</strong> : Génération automatique des menus</li>
 *   <li><strong>SEO</strong> : URLs structurées et métadonnées optimisées</li>
 *   <li><strong>Filtrage</strong> : Critères de recherche et navigation facettée</li>
 *   <li><strong>Analytics</strong> : Reporting par domaine et performance catégories</li>
 *   <li><strong>Recommandations</strong> : Algorithmes basés sur la catégorisation</li>
 *   <li><strong>Cache</strong> : Arbre de catégories mis en cache pour performance</li>
 * </ul>
 * 
 * <h3>Patterns implémentés :</h3>
 * <ul>
 *   <li><strong>Composite Pattern</strong> : Structure arborescente hiérarchique</li>
 *   <li><strong>Soft Delete Pattern</strong> : Préservation des données historiques</li>
 *   <li><strong>Slug Pattern</strong> : URLs lisibles et SEO-optimisées</li>
 *   <li><strong>Counter Cache Pattern</strong> : Dénormalisation pour performance</li>
 *   <li><strong>Tree Traversal</strong> : Algorithmes de parcours d'arbre optimisés</li>
 * </ul>
 * 
 * <h3>Optimisations performance :</h3>
 * <ul>
 *   <li>Lazy loading des relations pour éviter N+1 queries</li>
 *   <li>Index sur slug, parent_category_id, is_active</li>
 *   <li>Compteurs dénormalisés pour éviter les COUNT() coûteux</li>
 *   <li>Ordre prédéfini pour minimiser les tris à l'exécution</li>
 * </ul>
 * 
 * <h3>Extensibilité future :</h3>
 * <ul>
 *   <li>Multi-language : Support de slugs et noms traduits</li>
 *   <li>Permissions : Catégories privées ou par rôle</li>
 *   <li>Taxonomie avancée : Tags, attributs personnalisés</li>
 *   <li>Versioning : Historique des modifications de structure</li>
 *   <li>AI Integration : Catégorisation automatique par ML</li>
 *   <li>Marketplace : Catégories vendeur/acheteur distinctes</li>
 * </ul>
 * 
 * @author JavaCraft Academy Team
 * @version 1.0
 * @since 1.0
 * 
 * @see Course
 * @see BaseEntity
 * @see Enrollment
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
@Data
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
                totalEnroll += course.getEnrollmentCount() != null ? course.getEnrollmentCount() : 0;
            }
        }
        this.totalEnrollments = totalEnroll;
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
            if (!Boolean.TRUE.equals(parent.isActive)) {
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
                .filter(course -> course.isPublished())
                .count();
            
            if (activeCourses > 0) {
                throw new IllegalStateException(
                    "Impossible de supprimer une catégorie contenant des cours publiés");
            }
        }
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

/*
========== GUIDE DÉTAILLÉ DES IMPORTS ==========

1. **jakarta.persistence.*** :
   - @Entity : Marque la classe comme entité JPA mappée en base
   - @Table : Configuration de la table (nom, index, contraintes)
   - @Column : Mapping des colonnes avec contraintes
   - @ManyToOne/@OneToMany : Relations entre entités
   - @JoinColumn : Configuration des clés étrangères
   - @OrderBy : Tri automatique des collections
   - @Index/@UniqueConstraint : Optimisations base de données
   - @PrePersist/@PreUpdate/@PostPersist/@PreRemove : Hooks de cycle de vie

2. **lombok.Data et lombok.EqualsAndHashCode** :
   - @Data : Génère getters/setters, toString, equals, hashCode
   - @EqualsAndHashCode(callSuper = true) : Inclut les champs de BaseEntity
   - exclude = {...} : Exclut les relations pour éviter les cycles infinis
   - Réduit drastiquement le code boilerplate

3. **org.hibernate.annotations.*** :
   - @SQLDelete : Implémente le soft delete (UPDATE au lieu de DELETE)
   - @Where : Filtre automatique pour exclure les entités supprimées
   - Pattern essentiel pour préserver l'historique et les relations

4. **java.util.*** :
   - ArrayList/List : Collections pour les relations OneToMany
   - HashSet/Set : Collections sans doublons pour futures extensions
   - Collections thread-safe et optimisées pour JPA

5. **jakarta.validation.constraints.*** :
   - @NotBlank : Validation non-null et non-vide
   - @Size : Contraintes de longueur
   - @Pattern : Validation par expressions régulières
   - Validation déclarative intégrée avec Spring Boot

6. **com.fasterxml.jackson.annotation.*** :
   - @JsonIgnore : Exclut de la sérialisation JSON (évite les données lourdes)
   - @JsonManagedReference/@JsonBackReference : Gère les références circulaires
   - Essentiel pour les API REST sans erreurs de sérialisation

========== FUTURES AMÉLIORATIONS TECHNIQUES ==========

1. **Cache et Performance** :
   ```java
   @Cacheable("categories")
   @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
   ```

========== FUTURES AMÉLIORATIONS TECHNIQUES (suite) ==========

2. **Audit automatique** :
   ```java
   @EntityListeners(AuditingEntityListener.class)
   @CreatedDate
   @LastModifiedDate
   @CreatedBy
   @LastModifiedBy
   ```

3. **Indexation pour recherche** :
   ```java
   @FullTextFilterDef(name = "activeCategory", impl = ActiveCategoryFilter.class)
   @Indexed
   @Field(name = "name", analyze = Analyze.YES)
   @Field(name = "description", analyze = Analyze.YES)
   ```

4. **Versioning et historique** :
   ```java
   @Audited
   @RevisionEntity(CategoryRevisionEntity.class)
   ```

5. **Permissions et sécurité** :
   ```java
   @PreAuthorize("hasRole('ADMIN') or hasPermission(#category, 'WRITE')")
   @PostFilter("hasPermission(filterObject, 'READ')")
   ```

6. **Multi-tenant support** :
   ```java
   @TenantId
   @Column(name = "tenant_id")
   private String tenantId;
   ```

7. **Optimisations requêtes** :
   ```java
   @NamedEntityGraph(
       name = "Category.withSubCategories",
       attributeNodes = @NamedAttributeNode("subCategories")
   )
   ```

========== REQUÊTES CUSTOM SUGGÉRÉES ==========

Dans CategoryRepository, ajouter ces méthodes :

```java
// Récupérer l'arbre complet des catégories
@Query("SELECT c FROM Category c LEFT JOIN FETCH c.subCategories WHERE c.parentCategory IS NULL ORDER BY c.orderIndex")
List<Category> findRootCategoriesWithChildren();

// Catégories populaires
@Query("SELECT c FROM Category c WHERE c.isActive = true AND c.totalEnrollments > :minEnrollments ORDER BY c.totalEnrollments DESC")
List<Category> findPopularCategories(@Param("minEnrollments") int minEnrollments, Pageable pageable);

// Recherche avec path complet
@Query("SELECT c FROM Category c WHERE c.isActive = true AND (c.name LIKE %:search% OR c.description LIKE %:search%)")
List<Category> searchCategories(@Param("search") String search);

// Catégories par niveau de profondeur
@Query(value = "WITH RECURSIVE category_tree AS (" +
               "SELECT id, name, parent_category_id, 0 as level FROM categories WHERE parent_category_id IS NULL " +
               "UNION ALL " +
               "SELECT c.id, c.name, c.parent_category_id, ct.level + 1 " +
               "FROM categories c JOIN category_tree ct ON c.parent_category_id = ct.id" +
               ") SELECT * FROM category_tree WHERE level = :level", nativeQuery = true)
List<Category> findCategoriesByLevel(@Param("level") int level);
```

========== SERVICES MÉTIER SUGGÉRÉS ==========

CategoryService avec ces méthodes clées :

```java
@Service
@Transactional
public class CategoryService {
    
    // Création avec validation de hiérarchie
    public Category createCategory(CategoryCreateDTO dto) {
        validateCategoryCreation(dto);
        return categoryRepository.save(convertToEntity(dto));
    }
    
    // Déplacement dans la hiérarchie
    public void moveCategory(Long categoryId, Long newParentId) {
        Category category = findById(categoryId);
        Category newParent = newParentId != null ? findById(newParentId) : null;
        
        if (category.wouldCreateCycle(newParent)) {
            throw new InvalidCategoryHierarchyException("Déplacement créerait une référence circulaire");
        }
        
        category.setParentCategory(newParent);
        categoryRepository.save(category);
        
        // Invalider le cache
        cacheManager.evict("categories");
    }
    
    // Synchronisation des compteurs
    @Scheduled(fixedDelay = 3600000) // Toutes les heures
    public void synchronizeCounters() {
        List<Category> categories = categoryRepository.findAll();
        categories.forEach(Category::recalculateCounters);
        categoryRepository.saveAll(categories);
    }
    
    // Génération de sitemap
    public List<CategorySitemapEntry> generateSitemap() {
        return categoryRepository.findActiveCategories()
            .stream()
            .map(this::toCategorySitemapEntry)
            .collect(Collectors.toList());
    }
}
```

========== ENDPOINTS REST SUGGÉRÉS ==========

CategoryController avec API complète :

```java
@RestController
@RequestMapping("/api/categories")
@Validated
public class CategoryController {
    
    // Arbre complet des catégories
    @GetMapping("/tree")
    public ResponseEntity<List<CategoryTreeDTO>> getCategoryTree() {
        return ResponseEntity.ok(categoryService.getCategoryTree());
    }
    
    // Fil d'Ariane
    @GetMapping("/{id}/breadcrumb")
    public ResponseEntity<List<CategoryBreadcrumbDTO>> getBreadcrumb(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getBreadcrumb(id));
    }
    
    // Statistiques de catégorie
    @GetMapping("/{id}/stats")
    public ResponseEntity<CategoryStatsDTO> getCategoryStats(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getCategoryStats(id));
    }
    
    // Réorganisation par drag & drop
    @PutMapping("/reorder")
    public ResponseEntity<Void> reorderCategories(@RequestBody @Valid CategoryReorderDTO reorderDTO) {
        categoryService.reorderCategories(reorderDTO);
        return ResponseEntity.ok().build();
    }
}
```

========== TESTS UNITAIRES ESSENTIELS ==========

```java
@DataJpaTest
class CategoryEntityTest {
    
    @Test
    void shouldDetectCyclicReference() {
        Category parent = new Category();
        Category child = new Category();
        Category grandChild = new Category();
        
        parent.getSubCategories().add(child);
        child.setParentCategory(parent);
        child.getSubCategories().add(grandChild);
        grandChild.setParentCategory(child);
        
        // Tenter de créer un cycle
        assertTrue(parent.wouldCreateCycle(grandChild));
    }
    
    @Test
    void shouldCalculateCorrectLevel() {
        Category root = new Category();
        Category level1 = new Category();
        Category level2 = new Category();
        
        level1.setParentCategory(root);
        level2.setParentCategory(level1);
        
        assertEquals(0, root.getLevel());
        assertEquals(1, level1.getLevel());
        assertEquals(2, level2.getLevel());
    }
    
    @Test
    void shouldBuildCorrectFullPath() {
        Category tech = createCategory("Technologie");
        Category prog = createCategory("Programmation");
        Category java = createCategory("Java");
        
        prog.setParentCategory(tech);
        java.setParentCategory(prog);
        
        assertEquals("Technologie > Programmation > Java", java.getFullPath());
    }
}
```

========== CONFIGURATIONS ADDITIONNELLES ==========

1. **Configuration JPA** :
```properties
# Optimisations Hibernate
spring.jpa.properties.hibernate.batch_size=25
spring.jpa.properties.hibernate.order_inserts=true
spring.jpa.properties.hibernate.order_updates=true
spring.jpa.properties.hibernate.jdbc.batch_versioned_data=true

# Cache de second niveau
spring.jpa.properties.hibernate.cache.use_second_level_cache=true
spring.jpa.properties.hibernate.cache.region.factory_class=org.hibernate.cache.jcache.JCacheRegionFactory
```

2. **Configuration Redis Cache** :
```java
@Configuration
@EnableCaching
public class CacheConfig {
    
    @Bean
    public CacheManager cacheManager() {
        RedisCacheManager.Builder builder = RedisCacheManager
            .RedisCacheManagerBuilder
            .fromConnectionFactory(redisConnectionFactory())
            .cacheDefaults(cacheConfiguration());
        
        return builder.build();
    }
    
    private RedisCacheConfiguration cacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofHours(1))
            .serializeKeysWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new GenericJackson2JsonRedisSerializer()));
    }
}
```

========== MONITORING ET MÉTRIQUES ==========

```java
@Component
public class CategoryMetrics {
    
    private final MeterRegistry meterRegistry;
    private final Counter categoryCreationCounter;
    private final Timer categoryQueryTimer;
    
    public CategoryMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.categoryCreationCounter = Counter.builder("category.created")
            .description("Number of categories created")
            .register(meterRegistry);
        this.categoryQueryTimer = Timer.builder("category.query.duration")
            .description("Time taken to query categories")
            .register(meterRegistry);
    }
    
    public void recordCategoryCreation() {
        categoryCreationCounter.increment();
    }
    
    public Timer.Sample startQueryTimer() {
        return Timer.start(meterRegistry);
    }
}
```

========== SÉCURITÉ ET VALIDATION ==========

```java
@Component
public class CategorySecurityService {
    
    @PreAuthorize("hasRole('ADMIN') or hasRole('CONTENT_MANAGER')")
    public void validateCategoryAccess(Category category, String operation) {
        // Validation des permissions par catégorie
        if ("DELETE".equals(operation) && category.getCoursesCount() > 0) {
            throw new CategoryDeletionException("Cannot delete category with active courses");
        }
    }
    
    @EventListener
    public void handleCategoryModification(CategoryModifiedEvent event) {
        // Audit des modifications
        auditService.logCategoryChange(event.getCategory(), event.getOperation());
        
        // Invalidation du cache
        cacheManager.evict("categories");
        
        // Notification aux services dépendants
        eventPublisher.publishEvent(new CategoryHierarchyChangedEvent(event.getCategory()));
    }
}
```

Cette extension complète l'entité Category avec toutes les fonctionnalités avancées nécessaires pour un système de e-learning professionnel, incluant la gestion de cache, la sécurité, les métriques, et les optimisations de performance.
*/