package com.javacraftacademy.courseservice.dto.request;

import com.javacraftacademy.courseservice.model.enums.CourseLevel;
import com.javacraftacademy.courseservice.model.enums.CourseStatus;
import com.javacraftacademy.courseservice.validation.ValidCourseData;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * DTO (Data Transfer Object) pour la création d'un nouveau cours.
 * 
 * <p>Cette classe encapsule toutes les données nécessaires pour créer un nouveau cours
 * dans le système JavaCraft Academy. Elle contient les validations métier appropriées
 * et sert d'interface entre le contrôleur REST et la couche service.</p>
 * 
 * <h3>Relations avec l'application :</h3>
 * <ul>
 *   <li><strong>CourseController</strong> : Reçoit cette requête via les endpoints REST POST /api/courses</li>
 *   <li><strong>CourseService</strong> : Traite cette requête pour créer l'entité Course</li>
 *   <li><strong>CourseMapper</strong> : Convertit cette DTO vers l'entité Course</li>
 *   <li><strong>ValidationMessages</strong> : Utilise les messages de validation personnalisés</li>
 *   <li><strong>CategoryService</strong> : Valide l'existence des catégories référencées</li>
 *   <li><strong>UserServiceClient</strong> : Vérifie l'existence et les permissions de l'instructeur</li>
 *   <li><strong>CourseEventProducer</strong> : Publie l'événement CourseCreatedEvent après création</li>
 * </ul>
 * 
 * <h3>Fonctionnalités principales :</h3>
 * <ul>
 *   <li>Validation complète des données d'entrée avec Bean Validation</li>
 *   <li>Validation métier personnalisée avec @ValidCourseData</li>
 *   <li>Support pour les cours gratuits et payants</li>
 *   <li>Gestion des catégories multiples</li>
 *   <li>Définition des prérequis du cours</li>
 *   <li>Configuration des dates de publication et d'inscription</li>
 * </ul>
 * 
 * @author JavaCraft Academy Team
 * @version 1.0
 * @since 1.0
 * 
 * @see com.javacraftacademy.courseservice.controller.CourseController
 * @see com.javacraftacademy.courseservice.service.CourseService
 * @see com.javacraftacademy.courseservice.dto.mapper.CourseMapper
 * @see com.javacraftacademy.courseservice.model.entity.Course
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ValidCourseData
@Schema(description = "Requête pour la création d'un nouveau cours")
public class CreateCourseRequest {

    /**
     * Titre du cours.
     * 
     * <p>Le titre doit être unique dans le système et servira à identifier
     * le cours dans l'interface utilisateur et les URLs.</p>
     */
    @NotBlank(message = "{validation.course.title.notblank}")
    @Size(min = 5, max = 200, message = "{validation.course.title.size}")
    @Schema(description = "Titre du cours", 
            example = "Introduction à Spring Boot", 
            minLength = 5, 
            maxLength = 200)
    private String title;

    /**
     * Description détaillée du cours.
     * 
     * <p>Cette description sera affichée sur la page de détail du cours
     * et utilisée pour le référencement (SEO).</p>
     */
    @NotBlank(message = "{validation.course.description.notblank}")
    @Size(min = 50, max = 5000, message = "{validation.course.description.size}")
    @Schema(description = "Description détaillée du cours", 
            example = "Ce cours vous apprendra les bases de Spring Boot...", 
            minLength = 50, 
            maxLength = 5000)
    private String description;

    /**
     * Résumé court du cours.
     * 
     * <p>Utilisé dans les listes de cours et les aperçus.
     * Doit être accrocheur et informatif.</p>
     */
    @Size(max = 500, message = "{validation.course.summary.size}")
    @Schema(description = "Résumé court du cours", 
            example = "Apprenez à créer des applications web modernes avec Spring Boot", 
            maxLength = 500)
    private String summary;

    /**
     * Identifiant de l'instructeur principal du cours.
     * 
     * <p>Cet ID sera validé via le UserServiceClient pour s'assurer
     * que l'utilisateur existe et a les permissions d'instructeur.</p>
     */
    @NotNull(message = "{validation.course.instructor.notnull}")
    @Positive(message = "{validation.course.instructor.positive}")
    @Schema(description = "Identifiant de l'instructeur principal", 
            example = "123")
    private Long instructorId;

    /**
     * Niveau de difficulté du cours.
     * 
     * <p>Utilisé pour filtrer et recommander les cours appropriés
     * selon le niveau de l'apprenant.</p>
     */
    @NotNull(message = "{validation.course.level.notnull}")
    @Schema(description = "Niveau de difficulté du cours", 
            example = "BEGINNER")
    private CourseLevel level;

    /**
     * Prix du cours en euros.
     * 
     * <p>Si null ou 0, le cours sera considéré comme gratuit.
     * Le prix doit être positif et avoir au maximum 2 décimales.</p>
     */
    @DecimalMin(value = "0.0", inclusive = true, message = "{validation.course.price.min}")
    @DecimalMax(value = "9999.99", message = "{validation.course.price.max}")
    @Digits(integer = 4, fraction = 2, message = "{validation.course.price.digits}")
    @Schema(description = "Prix du cours en euros (null ou 0 pour gratuit)", 
            example = "99.99")
    private BigDecimal price;

    /**
     * Durée estimée du cours en heures.
     * 
     * <p>Aide les étudiants à planifier leur apprentissage.
     * Calculée automatiquement si non fournie, basée sur les leçons.</p>
     */
    @Min(value = 1, message = "{validation.course.duration.min}")
    @Max(value = 1000, message = "{validation.course.duration.max}")
    @Schema(description = "Durée estimée du cours en heures", 
            example = "40")
    private Integer estimatedDurationHours;

    /**
     * URL de l'image de couverture du cours.
     * 
     * <p>Doit pointer vers une image hébergée sur le système de stockage
     * de fichiers de l'application ou un CDN autorisé.</p>
     */
    @Pattern(regexp = "^(https?://.*\\.(jpg|jpeg|png|gif|webp))$", 
             message = "{validation.course.thumbnailurl.pattern}")
    @Schema(description = "URL de l'image de couverture du cours", 
            example = "https://cdn.javacraftacademy.com/courses/spring-boot-intro.jpg")
    private String thumbnailUrl;

    /**
     * Ensemble des identifiants des catégories associées au cours.
     * 
     * <p>Un cours peut appartenir à plusieurs catégories pour une meilleure
     * organisation et recherche. Les IDs seront validés via CategoryService.</p>
     */
    @NotEmpty(message = "{validation.course.categories.notempty}")
    @Size(max = 5, message = "{validation.course.categories.maxsize}")
    @Schema(description = "Identifiants des catégories du cours", 
            example = "[1, 2, 3]")
    private Set<@Positive(message = "{validation.course.category.positive}") Long> categoryIds;

    /**
     * Liste des prérequis du cours.
     * 
     * <p>Aide les étudiants à évaluer s'ils ont les connaissances
     * nécessaires avant de s'inscrire.</p>
     */
    @Size(max = 10, message = "{validation.course.prerequisites.maxsize}")
    @Schema(description = "Liste des prérequis du cours", 
            example = "[\"Connaissance de base de Java\", \"Familiarité avec les IDE\"]")
    private Set<@NotBlank(message = "{validation.course.prerequisite.notblank}")
              @Size(max = 200, message = "{validation.course.prerequisite.size}") String> prerequisites;

    /**
     * Objectifs d'apprentissage du cours.
     * 
     * <p>Décrit ce que l'étudiant sera capable de faire après avoir
     * terminé le cours. Utilisé pour le marketing et l'évaluation.</p>
     */
    @Size(max = 15, message = "{validation.course.objectives.maxsize}")
    @Schema(description = "Objectifs d'apprentissage du cours", 
            example = "[\"Créer une application Spring Boot\", \"Configurer une base de données\"]")
    private Set<@NotBlank(message = "{validation.course.objective.notblank}")
              @Size(max = 300, message = "{validation.course.objective.size}") String> learningObjectives;

    /**
     * Date et heure de publication du cours.
     * 
     * <p>Si null, le cours sera publié immédiatement.
     * Si dans le futur, le cours sera programmé pour publication.</p>
     */
    @Future(message = "{validation.course.publishdate.future}")
    @Schema(description = "Date et heure de publication du cours", 
            example = "2024-12-01T10:00:00")
    private LocalDateTime publishedAt;

    /**
     * Date limite d'inscription au cours.
     * 
     * <p>Après cette date, les nouvelles inscriptions ne seront plus possibles.
     * Si null, les inscriptions restent ouvertes indéfiniment.</p>
     */
    @Future(message = "{validation.course.enrollmentdeadline.future}")
    @Schema(description = "Date limite d'inscription au cours", 
            example = "2024-11-30T23:59:59")
    private LocalDateTime enrollmentDeadline;

    /**
     * Statut initial du cours.
     * 
     * <p>Par défaut, les nouveaux cours sont créés avec le statut DRAFT.
     * Peut être défini sur PUBLISHED si le cours est prêt immédiatement.</p>
     */
    @Schema(description = "Statut initial du cours", 
            example = "DRAFT", 
            defaultValue = "DRAFT")
    private CourseStatus status = CourseStatus.DRAFT;

    /**
     * Indique si le cours est certifiant.
     * 
     * <p>Les cours certifiants génèrent un certificat de completion
     * pour les étudiants qui terminent avec succès.</p>
     */
    @Schema(description = "Indique si le cours est certifiant", 
            example = "true", 
            defaultValue = "false")
    private Boolean isCertified = false;

    /**
     * Capacité maximale d'étudiants pour ce cours.
     * 
     * <p>Si null, la capacité est illimitée.
     * Utilisé pour les cours avec suivi personnalisé.</p>
     */
    @Min(value = 1, message = "{validation.course.maxstudents.min}")
    @Max(value = 10000, message = "{validation.course.maxstudents.max}")
    @Schema(description = "Capacité maximale d'étudiants", 
            example = "100")
    private Integer maxStudents;

    /**
     * Mots-clés pour le référencement et la recherche.
     * 
     * <p>Utilisés pour améliorer la découvrabilité du cours
     * dans les moteurs de recherche et le système de recherche interne.</p>
     */
    @Size(max = 20, message = "{validation.course.tags.maxsize}")
    @Schema(description = "Mots-clés pour le référencement", 
            example = "[\"spring\", \"boot\", \"java\", \"web\"]")
    private Set<@NotBlank(message = "{validation.course.tag.notblank}")
              @Size(max = 50, message = "{validation.course.tag.size}") String> tags;

    /**
     * Langue principale du cours.
     * 
     * <p>Code ISO 639-1 de la langue (ex: "fr", "en", "es").
     * Utilisé pour le filtrage et la localisation.</p>
     */
    @Pattern(regexp = "^[a-z]{2}$", message = "{validation.course.language.pattern}")
    @Schema(description = "Code langue ISO 639-1 du cours", 
            example = "fr", 
            defaultValue = "fr")
    private String language = "fr";

    /**
     * Validations métier personnalisées.
     * 
     * <p>Cette méthode peut être étendue pour ajouter des validations
     * complexes qui nécessitent l'accès à plusieurs champs.</p>
     * 
     * @return true si les données sont cohérentes
     */
    public boolean isValid() {
        // Validation: si un prix est défini, il doit être positif
        if (price != null && price.compareTo(BigDecimal.ZERO) < 0) {
            return false;
        }
        
        // Validation: la date limite d'inscription doit être avant la publication
        if (enrollmentDeadline != null && publishedAt != null && 
            enrollmentDeadline.isBefore(publishedAt)) {
            return false;
        }
        
        return true;
    }

    /**
     * Vérifie si le cours est gratuit.
     * 
     * @return true si le prix est null ou égal à zéro
     */
    public boolean isFree() {
        return price == null || price.compareTo(BigDecimal.ZERO) == 0;
    }

    /**
     * Génère un slug URL-friendly basé sur le titre.
     * 
     * <p>Utilisé pour créer des URLs lisibles pour le cours.
     * Délègue à SlugUtils pour la génération.</p>
     * 
     * @return slug généré à partir du titre
     */
    public String generateSlug() {
        if (title == null || title.trim().isEmpty()) {
            return null;
        }
        // Cette méthode utiliserait SlugUtils.createSlug(title)
        return title.toLowerCase()
                   .replaceAll("[^a-z0-9\\s-]", "")
                   .replaceAll("\\s+", "-")
                   .replaceAll("-+", "-")
                   .replaceAll("^-|-$", "");
    }
}