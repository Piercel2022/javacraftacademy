package com.javacraftacademy.courseservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.util.HashSet;
import java.util.Set;

/**
 * Entité Tag pour le système de gestion de cours JavaCraftAcademy.
 * 
 * <p>Cette classe représente un tag (étiquette) qui peut être associé à des cours
 * pour faciliter la catégorisation, la recherche et l'organisation du contenu éducatif.</p>
 * 
 * <h3>Fonctionnalités principales :</h3>
 * <ul>
 *   <li>Gestion des métadonnées des tags (nom, slug, description, couleur)</li>
 *   <li>Classification par type de tag (technologie, compétence, niveau, etc.)</li>
 *   <li>Suivi de la popularité et du nombre d'utilisations</li>
 *   <li>Association many-to-many avec les cours</li>
 *   <li>Soft delete (suppression logique)</li>
 *   <li>Validation automatique des données</li>
 * </ul>
 * 
 * <h3>Relations dans l'application :</h3>
 * <ul>
 *   <li><strong>Course :</strong> Relation many-to-many bidirectionnelle</li>
 *   <li><strong>BaseEntity :</strong> Héritage pour les champs communs (id, dates, etc.)</li>
 *   <li><strong>Services :</strong> Utilisée par TagService pour la logique métier</li>
 *   <li><strong>Controllers :</strong> Exposée via TagController pour les API REST</li>
 *   <li><strong>Repositories :</strong> Persistance via TagRepository</li>
 * </ul>
 * 
 * <h3>Extensibilité future :</h3>
 * <p>Pour ajouter de nouvelles fonctionnalités :</p>
 * <ul>
 *   <li>Ajouter de nouveaux types dans l'enum TagType</li>
 *   <li>Créer des méthodes utilitaires supplémentaires</li>
 *   <li>Ajouter des validations personnalisées</li>
 *   <li>Étendre les relations avec d'autres entités</li>
 * </ul>
 * 
 * @author JavaCraftAcademy Team
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(name = "tags")
@Data // Lombok: génère getters, setters, toString, equals, hashCode
@EqualsAndHashCode(callSuper = true) // Lombok: inclut les champs de BaseEntity dans equals/hashCode
@SQLDelete(sql = "UPDATE tags SET deleted = true WHERE id = ?") // Hibernate: soft delete
@Where(clause = "deleted = false") // Hibernate: filtre automatique pour exclure les éléments supprimés
public class Tag extends BaseEntity {

    /**
     * Nom du tag, unique et obligatoire.
     * Utilisé pour l'affichage et l'identification du tag.
     */
    @Column(nullable = false, unique = true, length = 50)
    private String name;

    /**
     * Slug du tag, version URL-friendly du nom.
     * Utilisé dans les URLs et pour l'indexation SEO.
     */
    @Column(unique = true, nullable = false, length = 60)
    private String slug;

    /**
     * Description optionnelle du tag.
     * Fournit plus de contexte sur l'utilisation du tag.
     */
    @Column(length = 300)
    private String description;

    /**
     * Code couleur hexadécimal pour l'affichage du tag dans l'interface.
     * Format: #RRGGBB (ex: #FF5733)
     */
    @Column(name = "color_code", length = 7)
    private String colorCode;

    /**
     * Type de tag pour la classification.
     * Permet de regrouper les tags par catégories logiques.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "tag_type")
    private TagType tagType = TagType.GENERAL;

    /**
     * Indicateur de popularité du tag.
     * Peut être défini manuellement ou automatiquement selon l'usage.
     */
    @Column(name = "is_popular")
    private Boolean isPopular = false;

    /**
     * Compteur d'utilisation du tag.
     * Incrémenté/décrémenté automatiquement lors des associations/dissociations.
     */
    @Column(name = "usage_count")
    private Integer usageCount = 0;

    /**
     * Collection des cours associés à ce tag.
     * Relation many-to-many bidirectionnelle.
     */
    @ManyToMany(mappedBy = "tags")
    private Set<Course> courses = new HashSet<>();

    /**
     * Énumération des types de tags disponibles.
     * Extensible pour ajouter de nouveaux types selon les besoins.
     */
    public enum TagType {
        /** Tag général, non spécialisé */
        GENERAL,
        /** Tag lié à une technologie spécifique */
        TECHNOLOGY,
        /** Tag représentant une compétence */
        SKILL,
        /** Tag indiquant un niveau de difficulté */
        LEVEL,
        /** Tag pour un langage de programmation */
        LANGUAGE,
        /** Tag pour un framework */
        FRAMEWORK,
        /** Tag pour un outil */
        TOOL,
        /** Tag lié à un secteur d'activité */
        INDUSTRY
    }

    // ============ MÉTHODES UTILITAIRES ============

    /**
     * Incrémente le compteur d'utilisation du tag.
     * Appelée automatiquement lors de l'association à un nouveau cours.
     */
    public void incrementUsageCount() {
        this.usageCount = (this.usageCount == null ? 0 : this.usageCount) + 1;
    }

    /**
     * Décrémente le compteur d'utilisation du tag.
     * Appelée automatiquement lors de la dissociation d'un cours.
     * Le compteur ne peut pas être négatif.
     */
    public void decrementUsageCount() {
        this.usageCount = Math.max(0, (this.usageCount == null ? 0 : this.usageCount) - 1);
    }

    /**
     * Détermine si le tag est considéré comme populaire.
     * Un tag est populaire s'il est marqué manuellement comme tel
     * ou s'il est utilisé dans au moins 10 cours.
     * 
     * @return true si le tag est populaire, false sinon
     */
    public boolean isPopularTag() {
        return Boolean.TRUE.equals(this.isPopular) || (this.usageCount != null && this.usageCount >= 10);
    }

    /**
     * Retourne le nombre de cours associés à ce tag.
     * 
     * @return nombre de cours utilisant ce tag
     */
    public int getCoursesCount() {
        return this.courses != null ? this.courses.size() : 0;
    }

    /**
     * Vérifie si le tag est actuellement utilisé par au moins un cours.
     * 
     * @return true si le tag est utilisé, false sinon
     */
    public boolean isUsed() {
        return getCoursesCount() > 0;
    }

    /**
     * Méthode de validation appelée automatiquement avant la persistance.
     * Valide et normalise les données du tag.
     * 
     * @throws IllegalStateException si les données sont invalides
     */
    @PrePersist
    @PreUpdate
    private void validateTag() {
        if (this.name == null || this.name.trim().isEmpty()) {
            throw new IllegalStateException("Tag name cannot be empty");
        }

        if (this.slug == null || this.slug.trim().isEmpty()) {
            throw new IllegalStateException("Tag slug cannot be empty");
        }

        // Validation du code couleur hexadécimal
        if (this.colorCode != null && !this.colorCode.matches("^#[0-9A-Fa-f]{6}$")) {
            throw new IllegalStateException("Color code must be in hex format (#RRGGBB)");
        }

        // Normalisation du nom du tag
        this.name = this.name.trim().toLowerCase().replaceAll("\\s+", " ");
    }
}