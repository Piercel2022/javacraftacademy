package com.javacraftacademy.courseservice.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Entité JPA représentant une catégorie de cours dans l'application JavaCraftAcademy.
 * 
 * <p>Cette classe gère la classification et l'organisation des cours en catégories hiérarchiques.
 * Elle permet de structurer le catalogue de cours et facilite la navigation et la recherche
 * pour les utilisateurs de la plateforme d'apprentissage.</p>
 * 
 * <h3>Fonctionnalités principales :</h3>
 * <ul>
 *   <li>Classification hiérarchique des cours (catégories parentes/enfants)</li>
 *   <li>Gestion des métadonnées (nom, description, slug URL-friendly)</li>
 *   <li>Suivi de l'activité (statut actif/inactif, compteur de cours)</li>
 *   <li>Audit automatique (dates de création et modification)</li>
 *   <li>Support SEO avec slug personnalisable</li>
 * </ul>
 * 
 * <h3>Relations avec l'écosystème JavaCraftAcademy :</h3>
 * <ul>
 *   <li><strong>Course</strong> : Relation One-to-Many - Une catégorie peut contenir plusieurs cours</li>
 *   <li><strong>CategoryController</strong> : Exposition REST API pour CRUD des catégories</li>
 *   <li><strong>CategoryService</strong> : Logique métier pour la gestion des catégories</li>
 *   <li><strong>CategoryRepository</strong> : Accès aux données avec requêtes personnalisées</li>
 *   <li><strong>Frontend</strong> : Navigation par catégories, filtrage des cours</li>
 * </ul>
 * 
 * <h3>Extensions futures possibles :</h3>
 * <ul>
 *   <li>Ajout d'icônes/images pour chaque catégorie</li>
 *   <li>Système de tags/étiquettes supplémentaires</li>
 *   <li>Métriques avancées (popularité, taux de complétion)</li>
 *   <li>Gestion multilingue des catégories</li>
 *   <li>Catégories dynamiques basées sur l'IA</li>
 * </ul>
 * 
 * @author JavaCraftAcademy Team
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(name = "categories", indexes = {
    @Index(name = "idx_category_slug", columnList = "slug"),
    @Index(name = "idx_category_parent", columnList = "parent_id"),
    @Index(name = "idx_category_active", columnList = "is_active")
})
public class Category {

    /**
     * Identifiant unique de la catégorie.
     * Clé primaire auto-générée pour assurer l'unicité.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nom affiché de la catégorie.
     * Utilisé dans l'interface utilisateur et les menus de navigation.
     * 
     * @NotBlank Validation : ne peut pas être vide ou null
     * @Size Limitation : entre 2 et 100 caractères pour l'ergonomie
     */
    @NotBlank(message = "Le nom de la catégorie est obligatoire")
    @Size(min = 2, max = 100, message = "Le nom doit contenir entre 2 et 100 caractères")
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /**
     * Identifiant URL-friendly pour le SEO et les liens.
     * Généré automatiquement à partir du nom ou défini manuellement.
     * Doit être unique pour éviter les conflits d'URL.
     */
    @NotBlank(message = "Le slug est obligatoire")
    @Size(max = 150, message = "Le slug ne peut pas dépasser 150 caractères")
    @Column(name = "slug", nullable = false, unique = true, length = 150)
    private String slug;

    /**
     * Description détaillée de la catégorie.
     * Aide les utilisateurs à comprendre le contenu et l'objectif de la catégorie.
     * Optionnelle mais recommandée pour l'expérience utilisateur.
     */
    @Size(max = 500, message = "La description ne peut pas dépasser 500 caractères")
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * Référence vers la catégorie parente pour la hiérarchie.
     * Permet de créer une structure arborescente de catégories.
     * Null pour les catégories racines.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    /**
     * Liste des sous-catégories enfants.
     * Gestion bidirectionnelle de la relation hiérarchique.
     * CascadeType.ALL : les opérations sur le parent affectent les enfants.
     */
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference // Évite la récursion infinie lors de la sérialisation JSON
    private List<Category> children = new ArrayList<>();

    /**
     * Liste des cours associés à cette catégorie.
     * Relation One-to-Many avec l'entité Course.
     * Permet de récupérer tous les cours d'une catégorie donnée.
     */
    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    private List<Course> courses = new ArrayList<>();

    /**
     * Statut d'activation de la catégorie.
     * false = catégorie désactivée (masquée dans l'interface)
     * true = catégorie active et visible
     */
    @NotNull(message = "Le statut d'activation est obligatoire")
    @Column(name = "is_active", nullable = false, columnDefinition = "BOOLEAN DEFAULT true")
    private Boolean isActive = true;

    /**
     * Compteur du nombre de cours dans cette catégorie.
     * Maintenu automatiquement ou via des triggers de base de données.
     * Utile pour l'affichage et les statistiques.
     */
    @Column(name = "course_count", columnDefinition = "INTEGER DEFAULT 0")
    private Integer courseCount = 0;

    /**
     * Ordre d'affichage de la catégorie.
     * Permet de personnaliser l'ordre dans les menus et listes.
     * Plus la valeur est petite, plus la catégorie apparaît en premier.
     */
    @Column(name = "display_order", columnDefinition = "INTEGER DEFAULT 0")
    private Integer displayOrder = 0;

    /**
     * Date et heure de création de la catégorie.
     * Gérée automatiquement par Hibernate lors de la persistance.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Date et heure de dernière modification.
     * Mise à jour automatiquement à chaque modification de l'entité.
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Constructeurs

    /**
     * Constructeur par défaut requis par JPA.
     * Utilisé par Hibernate pour l'instanciation des entités.
     */
    public Category() {}

    /**
     * Constructeur avec nom pour création rapide.
     * Pratique pour les tests et l'initialisation de données.
     * 
     * @param name Le nom de la catégorie
     */
    public Category(String name) {
        this.name = name;
        this.slug = generateSlugFromName(name);
    }

    /**
     * Constructeur complet pour initialisation avec tous les champs principaux.
     * 
     * @param name Le nom de la catégorie
     * @param slug L'identifiant URL-friendly
     * @param description La description de la catégorie
     * @param parent La catégorie parente (peut être null)
     */
    public Category(String name, String slug, String description, Category parent) {
        this.name = name;
        this.slug = slug;
        this.description = description;
        this.parent = parent;
    }

    // Méthodes utilitaires

    /**
     * Génère un slug URL-friendly à partir du nom.
     * Méthode utilitaire pour automatiser la création du slug.
     * 
     * @param name Le nom à convertir en slug
     * @return Le slug généré (minuscules, tirets pour espaces)
     */
    private String generateSlugFromName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "";
        }
        return name.toLowerCase()
                   .replaceAll("[^a-z0-9\\s-]", "")
                   .replaceAll("\\s+", "-")
                   .replaceAll("-+", "-")
                   .replaceAll("^-|-$", "");
    }

    /**
     * Ajoute une sous-catégorie enfant.
     * Maintient la cohérence bidirectionnelle de la relation.
     * 
     * @param child La catégorie enfant à ajouter
     */
    public void addChild(Category child) {
        if (child != null) {
            children.add(child);
            child.setParent(this);
        }
    }

    /**
     * Supprime une sous-catégorie enfant.
     * Maintient la cohérence bidirectionnelle de la relation.
     * 
     * @param child La catégorie enfant à supprimer
     */
    public void removeChild(Category child) {
        if (child != null) {
            children.remove(child);
            child.setParent(null);
        }
    }

    /**
     * Vérifie si cette catégorie est une catégorie racine.
     * 
     * @return true si c'est une catégorie racine (pas de parent)
     */
    public boolean isRootCategory() {
        return parent == null;
    }

    /**
     * Vérifie si cette catégorie a des sous-catégories.
     * 
     * @return true si la catégorie a des enfants
     */
    public boolean hasChildren() {
        return children != null && !children.isEmpty();
    }

    /**
     * Calcule le niveau de profondeur dans la hiérarchie.
     * 0 = racine, 1 = premier niveau, etc.
     * 
     * @return Le niveau de profondeur
     */
    public int getDepthLevel() {
        int depth = 0;
        Category current = this.parent;
        while (current != null) {
            depth++;
            current = current.getParent();
        }
        return depth;
    }

    // Getters et Setters avec documentation

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { 
        this.name = name;
        // Auto-génération du slug si pas défini
        if (this.slug == null || this.slug.isEmpty()) {
            this.slug = generateSlugFromName(name);
        }
    }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Category getParent() { return parent; }
    public void setParent(Category parent) { this.parent = parent; }

    public List<Category> getChildren() { return children; }
    public void setChildren(List<Category> children) { this.children = children; }

    public List<Course> getCourses() { return courses; }
    public void setCourses(List<Course> courses) { this.courses = courses; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public Integer getCourseCount() { return courseCount; }
    public void setCourseCount(Integer courseCount) { this.courseCount = courseCount; }

    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // Méthodes Object (equals, hashCode, toString)

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Category category = (Category) o;
        return Objects.equals(id, category.id) && 
               Objects.equals(slug, category.slug);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, slug);
    }

    @Override
    public String toString() {
        return "Category{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", slug='" + slug + '\'' +
                ", isActive=" + isActive +
                ", courseCount=" + courseCount +
                ", depthLevel=" + getDepthLevel() +
                ", createdAt=" + createdAt +
                '}';
    }
}