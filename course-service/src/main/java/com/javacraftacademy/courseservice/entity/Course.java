// Localisation: course-service/src/main/java/com/javacraftacademy/courseservice/entity/Course.java
package com.javacraftacademy.courseservice.entity;

// Imports JPA/Hibernate pour la persistance des données
import jakarta.persistence.*;  // Annotations JPA pour mapping objet-relationnel
import org.hibernate.annotations.SQLDelete;  // Soft delete personnalisé
import org.hibernate.annotations.Where;      // Filtre automatique sur les requêtes

// Import Lombok pour réduire le code boilerplate
import lombok.Data;           // Génère getters, setters, toString, equals, hashCode
import lombok.EqualsAndHashCode; // Personnalise equals/hashCode pour l'héritage

// Imports Java standard
import java.math.BigDecimal;   // Pour les calculs financiers précis (prix, notes)
import java.time.LocalDateTime; // Pour la gestion des dates/heures
import java.util.ArrayList;    // Liste ordonnée et redimensionnable
import java.util.HashSet;      // Set pour éviter les doublons (tags)
import java.util.List;         // Interface pour les collections ordonnées
import java.util.Set;          // Interface pour les collections sans doublons

/**
 * Entité principale représentant un cours dans l'application JavaCraft Academy.
 * 
 * <h2>But de la classe :</h2>
 * Cette classe constitue le cœur métier de l'application, modélisant un cours en ligne
 * avec toutes ses caractéristiques (contenu, tarification, métadonnées, statistiques).
 * Elle sert de modèle de données pour la persistance en base et les échanges API.
 * 
 * <h2>Fonctionnalités principales :</h2>
 * <ul>
 *   <li><strong>Gestion du contenu :</strong> titre, description, média (image/vidéo)</li>
 *   <li><strong>Structure pédagogique :</strong> leçons, quiz, devoirs organisés</li>
 *   <li><strong>Tarification flexible :</strong> prix, remises, cours gratuits</li>
 *   <li><strong>Métadonnées SEO :</strong> optimisation pour les moteurs de recherche</li>
 *   <li><strong>Statistiques temps réel :</strong> inscriptions, completions, notes</li>
 *   <li><strong>Système de workflow :</strong> brouillon → révision → publication</li>
 *   <li><strong>Soft delete :</strong> suppression logique pour préserver l'historique</li>
 * </ul>
 * 
 * <h2>Relations dans l'écosystème JavaCraft Academy :</h2>
 * <ul>
 *   <li><strong>Category :</strong> Organisation thématique des cours (ManyToOne)</li>
 *   <li><strong>Lesson :</strong> Contenu pédagogique structuré (OneToMany)</li>
 *   <li><strong>Quiz :</strong> Évaluations interactives (OneToMany)</li>
 *   <li><strong>Assignment :</strong> Projets pratiques (OneToMany)</li>
 *   <li><strong>Tag :</strong> Mots-clés pour la recherche (ManyToMany)</li>
 *   <li><strong>User (instructorId) :</strong> Créateur du cours (référence)</li>
 *   <li><strong>Enrollment :</strong> Inscriptions étudiants (via services)</li>
 *   <li><strong>Review :</strong> Évaluations et commentaires (via services)</li>
 * </ul>
 * 
 * <h2>Patterns et architectures utilisés :</h2>
 * <ul>
 *   <li><strong>Entity Pattern :</strong> Modèle de domaine riche avec logique métier</li>
 *   <li><strong>Soft Delete Pattern :</strong> Préservation des données historiques</li>
 *   <li><strong>Enumeration Pattern :</strong> États typés et contrôlés</li>
 *   <li><strong>Aggregate Root :</strong> Point d'entrée pour les opérations complexes</li>
 * </ul>
 * 
 * <h2>Extensions futures possibles :</h2>
 * <ul>
 *   <li>Système de versioning des cours</li>
 *   <li>Support multi-langues</li>
 *   <li>Analytics avancées (temps de visionnage, progression)</li>
 *   <li>Système de badges et certifications</li>
 *   <li>Intégration avec des plateformes externes (Zoom, YouTube)</li>
 *   <li>Recommandations basées sur l'IA</li>
 * </ul>
 * 
 * @author JavaCraft Academy Team
 * @version 1.0
 * @since 1.0
 */
@Entity  // Marque cette classe comme une entité JPA mappée en base
@Table(name = "courses")  // Spécifie le nom de la table en base de données
@Data  // Lombok : génère automatiquement getters, setters, toString, equals, hashCode
@EqualsAndHashCode(callSuper = true)  // Inclut les champs de BaseEntity dans equals/hashCode
@SQLDelete(sql = "UPDATE courses SET deleted = true WHERE id = ?")  // Soft delete : marque comme supprimé
@Where(clause = "deleted = false")  // Filtre automatique : ignore les enregistrements supprimés
public class Course extends BaseEntity {

    // === INFORMATIONS PRINCIPALES ===
    
    @Column(nullable = false, length = 200)
    private String title;  // Titre du cours (obligatoire, max 200 caractères)

    @Column(unique = true, nullable = false, length = 250)
    private String slug;  // URL-friendly identifier (unique, pour SEO)

    @Column(columnDefinition = "TEXT")
    private String description;  // Description détaillée (texte long)

    @Column(name = "short_description", length = 500)
    private String shortDescription;  // Résumé court pour les listes

    // === MÉDIAS ET CONTENU VISUEL ===
    
    @Column(name = "cover_image_url")
    private String coverImageUrl;  // Image de couverture du cours

    @Column(name = "trailer_video_url")
    private String trailerVideoUrl;  // Vidéo de présentation

    // === CLASSIFICATION ET STATUT ===
    
    @Enumerated(EnumType.STRING)  // Stocke la valeur enum comme string en DB
    @Column(nullable = false)
    private CourseLevel level;  // Niveau de difficulté (BEGINNER, INTERMEDIATE, etc.)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CourseStatus status;  // État du cours (DRAFT, PUBLISHED, etc.)

    // === TARIFICATION ===
    
    @Column(precision = 10, scale = 2)  // Précision financière : 10 chiffres, 2 décimales
    private BigDecimal price;  // Prix de base

    @Column(name = "discounted_price", precision = 10, scale = 2)
    private BigDecimal discountedPrice;  // Prix promotionnel

    @Column(name = "is_free")
    private Boolean isFree = false;  // Indicateur cours gratuit

    // === INFORMATIONS INSTRUCTEUR ===
    
    @Column(name = "instructor_id", nullable = false)
    private Long instructorId;  // ID de l'instructeur (référence vers User)

    @Column(name = "instructor_name", nullable = false)
    private String instructorName;  // Nom de l'instructeur (dénormalisé pour performance)

    // === RELATIONS AVEC D'AUTRES ENTITÉS ===
    
    @ManyToOne(fetch = FetchType.LAZY)  // Chargement paresseux pour optimiser les performances
    @JoinColumn(name = "category_id")
    private Category category;  // Catégorie thématique du cours

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Lesson> lessons = new ArrayList<>();  // Leçons du cours

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Quiz> quizzes = new ArrayList<>();  // Quiz d'évaluation

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Assignment> assignments = new ArrayList<>();  // Devoirs pratiques

    @ManyToMany  // Relation plusieurs-à-plusieurs avec table de jointure
    @JoinTable(
        name = "course_tags",  // Nom de la table de jointure
        joinColumns = @JoinColumn(name = "course_id"),  // Clé étrangère vers Course
        inverseJoinColumns = @JoinColumn(name = "tag_id")  // Clé étrangère vers Tag
    )
    private Set<Tag> tags = new HashSet<>();  // Tags pour la recherche et classification

    // === MÉTRIQUES ET STATISTIQUES ===
    
    @Column(name = "duration_minutes")
    private Integer durationMinutes;  // Durée totale estimée en minutes

    @Column(name = "lessons_count")
    private Integer lessonsCount = 0;  // Nombre de leçons (cache pour performance)

    @Column(name = "quizzes_count")
    private Integer quizzesCount = 0;  // Nombre de quiz

    @Column(name = "assignments_count")
    private Integer assignmentsCount = 0;  // Nombre de devoirs

    @Column(name = "enrollment_count")
    private Integer enrollmentCount = 0;  // Nombre d'inscrits

    @Column(name = "completion_count")
    private Integer completionCount = 0;  // Nombre de complétions

    @Column(name = "average_rating", precision = 3, scale = 2)
    private BigDecimal averageRating;  // Note moyenne (ex: 4.75/5)

    @Column(name = "reviews_count")
    private Integer reviewsCount = 0;  // Nombre d'avis

    // === FONCTIONNALITÉS ET OPTIONS ===
    
    @Column(name = "published_at")
    private LocalDateTime publishedAt;  // Date de publication

    @Column(name = "is_featured")
    private Boolean isFeatured = false;  // Cours mis en vedette

    @Column(name = "certificate_enabled")
    private Boolean certificateEnabled = true;  // Certificat de réussite disponible

    // === CONTENU PÉDAGOGIQUE DÉTAILLÉ ===
    
    @Column(columnDefinition = "TEXT")
    private String requirements;  // Prérequis pour suivre le cours

    @Column(name = "learning_objectives", columnDefinition = "TEXT")
    private String learningObjectives;  // Objectifs d'apprentissage

    @Column(name = "target_audience", columnDefinition = "TEXT")
    private String targetAudience;  // Public cible

    // === MÉTADONNÉES SEO ===
    
    @Column(name = "meta_title")
    private String metaTitle;  // Titre pour les moteurs de recherche

    @Column(name = "meta_description", length = 500)
    private String metaDescription;  // Description pour les moteurs de recherche

    @Column(name = "meta_keywords")
    private String metaKeywords;  // Mots-clés pour le référencement

    // === ÉNUMÉRATIONS ===
    
    /**
     * Niveaux de difficulté disponibles pour les cours.
     * Permet une classification claire et une recherche filtrée.
     */
    public enum CourseLevel {
        BEGINNER,     // Débutant - aucune connaissance préalable
        INTERMEDIATE, // Intermédiaire - connaissances de base requises
        ADVANCED,     // Avancé - expérience significative nécessaire
        EXPERT        // Expert - maîtrise approfondie du domaine
    }

    /**
     * États du workflow de publication des cours.
     * Contrôle le cycle de vie et la visibilité des cours.
     */
    public enum CourseStatus {
        DRAFT,      // Brouillon - en cours de création
        REVIEW,     // En révision - soumis pour validation
        PUBLISHED,  // Publié - visible par les étudiants
        ARCHIVED,   // Archivé - plus disponible aux nouveaux étudiants
        SUSPENDED   // Suspendu - temporairement indisponible
    }

    // === MÉTHODES UTILITAIRES ===

    /**
     * Incrémente le compteur d'inscriptions de façon sécurisée.
     * Utilisé lors de nouvelles inscriptions d'étudiants.
     */
    public void incrementEnrollmentCount() {
        this.enrollmentCount = (this.enrollmentCount == null ? 0 : this.enrollmentCount) + 1;
    }

    /**
     * Incrémente le compteur de complétions de façon sécurisée.
     * Utilisé lorsqu'un étudiant termine complètement le cours.
     */
    public void incrementCompletionCount() {
        this.completionCount = (this.completionCount == null ? 0 : this.completionCount) + 1;
    }

    /**
     * Met à jour les compteurs basés sur les collections.
     * À appeler après modification des leçons, quiz ou devoirs.
     * Maintient la cohérence des données dénormalisées.
     */
    public void updateCounts() {
        this.lessonsCount = this.lessons != null ? this.lessons.size() : 0;
        this.quizzesCount = this.quizzes != null ? this.quizzes.size() : 0;
        this.assignmentsCount = this.assignments != null ? this.assignments.size() : 0;
    }

    /**
     * Vérifie si le cours est publié et accessible aux étudiants.
     * @return true si le cours est en statut PUBLISHED
     */
    public boolean isPublished() {
        return CourseStatus.PUBLISHED.equals(this.status);
    }

    /**
     * Détermine si le cours est accessible gratuitement.
     * @return true si marqué gratuit ou si le prix est zéro
     */
    public boolean isFreeAccess() {
        return Boolean.TRUE.equals(this.isFree) || BigDecimal.ZERO.equals(this.price);
    }

    // === MÉTHODES D'EXTENSION POUR FUTURES FONCTIONNALITÉS ===

    /**
     * Calcule le taux de completion du cours.
     * Utile pour les analytics et recommandations.
     * @return taux de completion (0.0 à 1.0)
     */
    public double getCompletionRate() {
        if (enrollmentCount == null || enrollmentCount == 0) return 0.0;
        if (completionCount == null) return 0.0;
        return (double) completionCount / enrollmentCount;
    }

    /**
     * Vérifie si le cours nécessite un paiement.
     * @return true si le cours est payant
     */
    public boolean requiresPayment() {
        return !isFreeAccess();
    }

    /**
     * Obtient le prix effectif (avec remise si applicable).
     * @return prix à payer par l'étudiant
     */
    public BigDecimal getEffectivePrice() {
        if (isFreeAccess()) return BigDecimal.ZERO;
        return (discountedPrice != null && discountedPrice.compareTo(BigDecimal.ZERO) > 0) 
               ? discountedPrice : price;
    }

    /**
     * Vérifie si le cours a une promotion active.
     * @return true si un prix réduit est défini
     */
    public boolean hasDiscount() {
        return discountedPrice != null && 
               discountedPrice.compareTo(BigDecimal.ZERO) > 0 &&
               price != null &&
               discountedPrice.compareTo(price) < 0;
    }
}