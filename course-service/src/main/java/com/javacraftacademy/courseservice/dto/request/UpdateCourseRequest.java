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
 * DTO (Data Transfer Object) pour la mise � jour d'un cours existant.
 * 
 * <p>Cette classe encapsule toutes les donn�es modifiables d'un cours existant
 * dans le syst�me JavaCraft Academy. Contrairement � CreateCourseRequest,
 * tous les champs sont optionnels pour permettre les mises � jour partielles.</p>
 * 
 * <h3>Relations avec l'application :</h3>
 * <ul>
 *   <li><strong>CourseController</strong> : Re�oit cette requ�te via les endpoints REST PUT/PATCH /api/courses/{id}</li>
 *   <li><strong>CourseService</strong> : Traite cette requ�te pour mettre � jour l'entit� Course</li>
 *   <li><strong>CourseMapper</strong> : Applique les modifications sur l'entit� Course existante</li>
 *   <li><strong>CourseRepository</strong> : V�rifie l'existence du cours avant mise � jour</li>
 *   <li><strong>CategoryService</strong> : Valide les nouvelles cat�gories si modifi�es</li>
 *   <li><strong>UserServiceClient</strong> : V�rifie les permissions de modification de l'instructeur</li>
 *   <li><strong>CourseEventProducer</strong> : Publie l'�v�nement CourseUpdatedEvent apr�s modification</li>
 *   <li><strong>EnrollmentService</strong> : V�rifie les contraintes lors des changements de prix/statut</li>
 * </ul>
 * 
 * <h3>Fonctionnalit�s principales :</h3>
 * <ul>
 *   <li>Mise � jour partielle : seuls les champs non-null sont modifi�s</li>
 *   <li>Validation des contraintes m�tier (ex: pas de changement de prix si �tudiants inscrits)</li>
 *   <li>Gestion des transitions de statut (DRAFT \u2192 PUBLISHED \u2192 ARCHIVED)</li>
 *   <li>Validation des permissions d'�dition</li>
 *   <li>Notification automatique des changements importants</li>
 *   <li>Historique des modifications (via events)</li>
 * </ul>
 * 
 * <h3>R�gles m�tier pour la mise � jour :</h3>
 * <ul>
 *   <li>Un cours publi� ne peut pas repasser en DRAFT</li>
 *   <li>Le prix ne peut pas �tre modifi� si des �tudiants sont d�j� inscrits</li>
 *   <li>L'instructeur principal ne peut �tre chang� que par un administrateur</li>
 *   <li>Certains champs n�cessitent une revalidation du contenu</li>
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
 * @see CreateCourseRequest
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ValidCourseData
@Schema(description = "Requ�te pour la mise � jour d'un cours existant")
public class UpdateCourseRequest {

    /**
     * Nouveau titre du cours.
     * 
     * <p>Si fourni, le titre sera mis � jour et un nouveau slug sera g�n�r�.
     * Le titre doit rester unique dans le syst�me.</p>
     */
    @Size(min = 5, max = 200, message = "{validation.course.title.size}")
    @Schema(description = "Nouveau titre du cours", 
            example = "Introduction � Spring Boot - Edition 2024", 
            minLength = 5, 
            maxLength = 200)
    private String title;

    /**
     * Nouvelle description d�taill�e du cours.
     * 
     * <p>La modification de la description peut n�cessiter une r�vision
     * du contenu par l'�quipe �ditoriale si le cours est publi�.</p>
     */
    @Size(min = 50, max = 5000, message = "{validation.course.description.size}")
    @Schema(description = "Nouvelle description d�taill�e du cours", 
            example = "Ce cours mis � jour vous apprendra les derni�res fonctionnalit�s...", 
            minLength = 50, 
            maxLength = 5000)
    private String description;

    /**
     * Nouveau r�sum� court du cours.
     * 
     * <p>Utilis� dans les listes de cours et les aper�us.
     * La modification affecte imm�diatement l'affichage public.</p>
     */
    @Size(max = 500, message = "{validation.course.summary.size}")
    @Schema(description = "Nouveau r�sum� court du cours", 
            example = "Version mise � jour avec les derni�res bonnes pratiques Spring Boot", 
            maxLength = 500)
    private String summary;

    /**
     * Nouvel identifiant de l'instructeur principal.
     * 
     * <p><strong>Attention :</strong> Cette modification n�cessite des permissions
     * administrateur et doit �tre accompagn�e d'une notification � l'ancien
     * et au nouveau instructeur.</p>
     */
    @Positive(message = "{validation.course.instructor.positive}")
    @Schema(description = "Nouvel identifiant de l'instructeur principal", 
            example = "456",
            accessMode = Schema.AccessMode.WRITE_ONLY)
    private Long instructorId;

    /**
     * Nouveau niveau de difficult� du cours.
     * 
     * <p>La modification du niveau peut affecter les recommandations
     * et n�cessiter une mise � jour des pr�requis.</p>
     */
    @Schema(description = "Nouveau niveau de difficult� du cours", 
            example = "INTERMEDIATE")
    private CourseLevel level;

    /**
     * Nouveau prix du cours en euros.
     * 
     * <p><strong>Contrainte importante :</strong> Le prix ne peut �tre modifi�
     * que si aucun �tudiant n'est inscrit au cours. Cette validation est
     * effectu�e au niveau du service.</p>
     */
    @DecimalMin(value = "0.0", inclusive = true, message = "{validation.course.price.min}")
    @DecimalMax(value = "9999.99", message = "{validation.course.price.max}")
    @Digits(integer = 4, fraction = 2, message = "{validation.course.price.digits}")
    @Schema(description = "Nouveau prix du cours en euros (null ou 0 pour gratuit)", 
            example = "129.99",
            accessMode = Schema.AccessMode.WRITE_ONLY)
    private BigDecimal price;

    /**
     * Nouvelle dur�e estim�e du cours en heures.
     * 
     * <p>Cette modification affecte les estimations d'apprentissage
     * et peut n�cessiter une mise � jour des planning des �tudiants inscrits.</p>
     */
    @Min(value = 1, message = "{validation.course.duration.min}")
    @Max(value = 1000, message = "{validation.course.duration.max}")
    @Schema(description = "Nouvelle dur�e estim�e du cours en heures", 
            example = "45")
    private Integer estimatedDurationHours;

    /**
     * Nouvelle URL de l'image de couverture du cours.
     * 
     * <p>La modification de l'image est imm�diatement visible
     * sur toutes les pages o� le cours est affich�.</p>
     */
    @Pattern(regexp = "^(https?://.*\\.(jpg|jpeg|png|gif|webp))$", 
             message = "{validation.course.thumbnailurl.pattern}")
    @Schema(description = "Nouvelle URL de l'image de couverture du cours", 
            example = "https://cdn.javacraftacademy.com/courses/spring-boot-intro-v2.jpg")
    private String thumbnailUrl;

    /**
     * Nouveaux identifiants des cat�gories associ�es au cours.
     * 
     * <p>Cette modification remplace compl�tement les cat�gories existantes.
     * Pour ajouter/supprimer des cat�gories sp�cifiques, utilisez les
     * endpoints d�di�s.</p>
     */
    @Size(max = 5, message = "{validation.course.categories.maxsize}")
    @Schema(description = "Nouveaux identifiants des cat�gories du cours", 
            example = "[1, 3, 5]")
    private Set<@Positive(message = "{validation.course.category.positive}") Long> categoryIds;

    /**
     * Nouveaux pr�requis du cours.
     * 
     * <p>La modification des pr�requis peut affecter l'�ligibilit�
     * des �tudiants d�j� inscrits et n�cessiter une communication.</p>
     */
    @Size(max = 10, message = "{validation.course.prerequisites.maxsize}")
    @Schema(description = "Nouveaux pr�requis du cours", 
            example = "[\"Connaissance avanc�e de Java\", \"Exp�rience avec Maven/Gradle\"]")
    private Set<@NotBlank(message = "{validation.course.prerequisite.notblank}")
              @Size(max = 200, message = "{validation.course.prerequisite.size}") String> prerequisites;

    /**
     * Nouveaux objectifs d'apprentissage du cours.
     * 
     * <p>La modification des objectifs peut n�cessiter une r�vision
     * du contenu et des �valuations existantes.</p>
     */
    @Size(max = 15, message = "{validation.course.objectives.maxsize}")
    @Schema(description = "Nouveaux objectifs d'apprentissage du cours", 
            example = "[\"Ma�triser Spring Boot 3.x\", \"Impl�menter des microservices\"]")
    private Set<@NotBlank(message = "{validation.course.objective.notblank}")
              @Size(max = 300, message = "{validation.course.objective.size}") String> learningObjectives;

    /**
     * Nouvelle date et heure de publication du cours.
     * 
     * <p>Permet de reprogrammer la publication d'un cours.
     * Si dans le pass� et status=PUBLISHED, la publication est imm�diate.</p>
     */
    @Schema(description = "Nouvelle date et heure de publication du cours", 
            example = "2024-12-15T10:00:00")
    private LocalDateTime publishedAt;

    /**
     * Nouvelle date limite d'inscription au cours.
     * 
     * <p>Peut �tre �tendue ou raccourcie selon les besoins.
     * Les �tudiants d�j� inscrits ne sont pas affect�s.</p>
     */
    @Schema(description = "Nouvelle date limite d'inscription au cours", 
            example = "2024-12-10T23:59:59")
    private LocalDateTime enrollmentDeadline;

    /**
     * Nouveau statut du cours.
     * 
     * <p><strong>Transitions autoris�es :</strong></p>
     * <ul>
     *   <li>DRAFT \u2192 PUBLISHED (avec validation du contenu)</li>
     *   <li>PUBLISHED \u2192 ARCHIVED (fermeture d�finitive)</li>
     *   <li>ARCHIVED \u2192 PUBLISHED (r�ouverture avec confirmation)</li>
     * </ul>
     * 
     * <p>Certaines transitions d�clenchent des notifications automatiques.</p>
     */
    @Schema(description = "Nouveau statut du cours", 
            example = "PUBLISHED")
    private CourseStatus status;

    /**
     * Nouvelle valeur pour l'indicateur de certification.
     * 
     * <p>Activer la certification n�cessite une configuration
     * additionnelle des crit�res d'�valuation.</p>
     */
    @Schema(description = "Nouveau statut de certification du cours", 
            example = "true")
    private Boolean isCertified;

    /**
     * Nouvelle capacit� maximale d'�tudiants.
     * 
     * <p>L'augmentation est toujours possible. La diminution n'est
     * autoris�e que si le nombre d'inscrits actuels le permet.</p>
     */
    @Min(value = 1, message = "{validation.course.maxstudents.min}")
    @Max(value = 10000, message = "{validation.course.maxstudents.max}")
    @Schema(description = "Nouvelle capacit� maximale d'�tudiants", 
            example = "150")
    private Integer maxStudents;

    /**
     * Nouveaux mots-cl�s pour le r�f�rencement.
     * 
     * <p>Cette modification affecte imm�diatement la recherche
     * et l'indexation du cours.</p>
     */
    @Size(max = 20, message = "{validation.course.tags.maxsize}")
    @Schema(description = "Nouveaux mots-cl�s pour le r�f�rencement", 
            example = "[\"spring\", \"boot\", \"microservices\", \"java\", \"api\"]")
    private Set<@NotBlank(message = "{validation.course.tag.notblank}")
              @Size(max = 50, message = "{validation.course.tag.size}") String> tags;

    /**
     * Nouvelle langue principale du cours.
     * 
     * <p>La modification de la langue peut n�cessiter une traduction
     * du contenu existant et affecte l'affichage dans les filtres.</p>
     */
    @Pattern(regexp = "^[a-z]{2}$", message = "{validation.course.language.pattern}")
    @Schema(description = "Nouveau code langue ISO 639-1 du cours", 
            example = "en")
    private String language;

    /**
     * Indique s'il s'agit d'une mise � jour majeure du cours.
     * 
     * <p>Une mise � jour majeure d�clenche des notifications sp�ciales
     * aux �tudiants inscrits et peut n�cessiter une nouvelle validation
     * du contenu par l'�quipe �ditoriale.</p>
     */
    @Schema(description = "Indique s'il s'agit d'une mise � jour majeure", 
            example = "false")
    private Boolean isMajorUpdate;

    /**
     * Notes de mise � jour pour expliquer les modifications apport�es.
     * 
     * <p>Ces notes sont visibles par l'�quipe �ditoriale et peuvent
     * �tre communiqu�es aux �tudiants en cas de mise � jour majeure.</p>
     */
    @Size(max = 1000, message = "{validation.course.updatenotes.size}")
    @Schema(description = "Notes expliquant les modifications apport�es", 
            example = "Mise � jour du contenu pour int�grer Spring Boot 3.2 et les derni�res bonnes pratiques")
    private String updateNotes;

    /**
     * V�rifie si au moins un champ de mise � jour est fourni.
     * 
     * <p>Cette m�thode est utile pour valider qu'une requ�te de mise � jour
     * contient effectivement des modifications.</p>
     * 
     * @return true si au moins un champ n'est pas null, false sinon
     */
    public boolean hasUpdates() {
        return title != null ||
               description != null ||
               summary != null ||
               instructorId != null ||
               level != null ||
               price != null ||
               estimatedDurationHours != null ||
               thumbnailUrl != null ||
               categoryIds != null ||
               prerequisites != null ||
               learningObjectives != null ||
               publishedAt != null ||
               enrollmentDeadline != null ||
               status != null ||
               isCertified != null ||
               maxStudents != null ||
               tags != null ||
               language != null ||
               isMajorUpdate != null ||
               updateNotes != null;
    }

    /**
     * V�rifie si la mise � jour contient des modifications critiques.
     * 
     * <p>Les modifications critiques incluent les changements de prix,
     * d'instructeur, de statut, ou les mises � jour majeures qui n�cessitent
     * des validations suppl�mentaires.</p>
     * 
     * @return true si la mise � jour contient des modifications critiques
     */
    public boolean hasCriticalUpdates() {
        return price != null ||
               instructorId != null ||
               status != null ||
               (isMajorUpdate != null && isMajorUpdate) ||
               maxStudents != null;
    }

    /**
     * V�rifie si la mise � jour affecte le contenu visible publiquement.
     * 
     * <p>Ces modifications sont imm�diatement visibles sur les pages
     * publiques du cours.</p>
     * 
     * @return true si la mise � jour affecte le contenu public
     */
    public boolean affectsPublicContent() {
        return title != null ||
               description != null ||
               summary != null ||
               thumbnailUrl != null ||
               level != null ||
               estimatedDurationHours != null ||
               prerequisites != null ||
               learningObjectives != null ||
               tags != null ||
               language != null;
    }
}