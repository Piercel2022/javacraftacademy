package com.javacraftacademy.courseservice.controller;

import com.javacraftacademy.courseservice.dto.request.CreateCourseRequest;
import com.javacraftacademy.courseservice.dto.request.UpdateCourseRequest;
import com.javacraftacademy.courseservice.dto.response.CourseResponse;
import com.javacraftacademy.courseservice.dto.response.CourseDetailResponse;
import com.javacraftacademy.courseservice.service.CourseService;
import com.javacraftacademy.courseservice.model.enums.CourseLevel;
import com.javacraftacademy.courseservice.model.enums.CourseStatus;
import com.javacraftacademy.courseservice.exception.CourseNotFoundException;
import com.javacraftacademy.courseservice.exception.InvalidCourseDataException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.List;

/**
 * Contrôleur REST pour la gestion des cours dans JavaCraft Academy.
 * 
 * <p>Cette classe expose les endpoints REST pour toutes les opérations CRUD
 * relatives aux cours de formation. Elle constitue la couche de présentation
 * pour le service de cours et gère les interactions HTTP avec les clients.</p>
 * 
 * <h3>Fonctionnalités principales :</h3>
 * <ul>
 *   <li>Création de nouveaux cours avec métadonnées complètes</li>
 *   <li>Récupération des cours avec pagination et filtrage</li>
 *   <li>Mise à jour des informations de cours</li>
 *   <li>Suppression de cours (soft delete)</li>
 *   <li>Gestion des images de cours</li>
 *   <li>Recherche avancée par critères multiples</li>
 * </ul>
 * 
 * <h3>Relations avec l'application :</h3>
 * <ul>
 *   <li><strong>CourseService</strong> : Délègue la logique métier</li>
 *   <li><strong>SecurityConfig</strong> : Contrôle d'accès basé sur les rôles</li>
 *   <li><strong>CategoryController</strong> : Liaison avec les catégories</li>
 *   <li><strong>LessonController</strong> : Gestion des leçons associées</li>
 *   <li><strong>EnrollmentController</strong> : Gestion des inscriptions</li>
 *   <li><strong>FileStorageService</strong> : Upload d'images et ressources</li>
 *   <li><strong>Kafka Events</strong> : Publication d'événements de cours</li>
 * </ul>
 * 
 * <h3>Extensions futures possibles :</h3>
 * <ul>
 *   <li>Système de notation et avis des cours</li>
 *   <li>Gestion des prérequis entre cours</li>
 *   <li>Analytics et statistiques d'engagement</li>
 *   <li>Support multilingue des cours</li>
 *   <li>Intégration avec des systèmes LMS externes</li>
 * </ul>
 * 
 * @author JavaCraft Academy Team
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/api/v1/courses")
@Tag(name = "Course Management", description = "APIs pour la gestion des cours")
@Validated
@CrossOrigin(origins = "${app.cors.allowed-origins}")
public class CourseController {

    /**
     * Service de gestion des cours - Contient toute la logique métier
     * pour les opérations sur les cours.
     */
    private final CourseService courseService;

    /**
     * Constructeur avec injection de dépendance.
     * 
     * @param courseService Service de gestion des cours injecté par Spring
     */
    @Autowired
    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    /**
     * Crée un nouveau cours dans le système.
     * 
     * <p>Cette méthode permet aux formateurs autorisés de créer de nouveaux cours.
     * Elle valide les données d'entrée, génère un slug unique, et publie un événement
     * de création via Kafka pour notifier les autres services.</p>
     * 
     * @param request Données du cours à créer (validées automatiquement)
     * @return ResponseEntity contenant les détails du cours créé
     * @throws InvalidCourseDataException Si les données sont invalides
     */
    @PostMapping
    @PreAuthorize("hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    @Operation(summary = "Créer un nouveau cours", 
               description = "Crée un nouveau cours avec les informations fournies")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Cours créé avec succès"),
        @ApiResponse(responseCode = "400", description = "Données invalides"),
        @ApiResponse(responseCode = "403", description = "Accès refusé - Rôle insuffisant"),
        @ApiResponse(responseCode = "409", description = "Conflit - Cours déjà existant")
    })
    public ResponseEntity<CourseDetailResponse> createCourse(
            @Valid @RequestBody CreateCourseRequest request) {
        
        CourseDetailResponse response = courseService.createCourse(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Récupère tous les cours avec pagination et filtrage optionnel.
     * 
     * <p>Endpoint public permettant de lister les cours disponibles.
     * Support de la pagination, du tri et du filtrage par différents critères.</p>
     * 
     * @param pageable Configuration de pagination (taille, page, tri)
     * @param categoryId Filtre par catégorie (optionnel)
     * @param level Filtre par niveau de difficulté (optionnel)
     * @param status Filtre par statut du cours (optionnel)
     * @param search Recherche textuelle dans titre/description (optionnel)
     * @return Page de cours correspondant aux critères
     */
    @GetMapping
    @Operation(summary = "Lister les cours", 
               description = "Récupère la liste paginée des cours avec filtres optionnels")
    @ApiResponse(responseCode = "200", description = "Liste récupérée avec succès")
    public ResponseEntity<Page<CourseResponse>> getAllCourses(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable,
            @RequestParam(required = false) @Parameter(description = "ID de la catégorie") Long categoryId,
            @RequestParam(required = false) @Parameter(description = "Niveau du cours") CourseLevel level,
            @RequestParam(required = false) @Parameter(description = "Statut du cours") CourseStatus status,
            @RequestParam(required = false) @Parameter(description = "Recherche textuelle") String search) {
        
        Page<CourseResponse> courses = courseService.getAllCourses(
            pageable, categoryId, level, status, search);
        return ResponseEntity.ok(courses);
    }

    /**
     * Récupère les détails complets d'un cours spécifique.
     * 
     * @param id Identifiant unique du cours
     * @return Détails complets du cours incluant les leçons
     * @throws CourseNotFoundException Si le cours n'existe pas  
     */
    @GetMapping("/{id}")
    @Operation(summary = "Détails d'un cours", 
               description = "Récupère les informations détaillées d'un cours")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cours trouvé"),
        @ApiResponse(responseCode = "404", description = "Cours non trouvé")
    })
    public ResponseEntity<CourseDetailResponse> getCourse(
            @PathVariable @NotNull @Positive Long id) {
        
        CourseDetailResponse course = courseService.getCourseById(id);
        return ResponseEntity.ok(course);
    }

    /**
     * Récupère un cours par son slug unique.
     * 
     * <p>Alternative à la récupération par ID, utilise le slug généré
     * automatiquement pour des URLs plus SEO-friendly.</p>
     * 
     * @param slug Slug unique du cours
     * @return Détails du cours
     * @throws CourseNotFoundException Si le cours n'existe pas
     */
    @GetMapping("/slug/{slug}")
    @Operation(summary = "Cours par slug", 
               description = "Récupère un cours via son slug unique")
    public ResponseEntity<CourseDetailResponse> getCourseBySlug(
            @PathVariable String slug) {
        
        CourseDetailResponse course = courseService.getCourseBySlug(slug);
        return ResponseEntity.ok(course);
    }

    /**
     * Met à jour les informations d'un cours existant.
     * 
     * <p>Seuls les formateurs propriétaires du cours ou les administrateurs
     * peuvent effectuer cette opération. Publie un événement de mise à jour.</p>
     * 
     * @param id Identifiant du cours à modifier
     * @param request Nouvelles données du cours
     * @return Cours mis à jour
     * @throws CourseNotFoundException Si le cours n'existe pas
     * @throws InvalidCourseDataException Si les données sont invalides
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('INSTRUCTOR') and @courseService.isOwner(#id, authentication.name))")
    @Operation(summary = "Mettre à jour un cours", 
               description = "Modifie les informations d'un cours existant")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cours mis à jour"),
        @ApiResponse(responseCode = "404", description = "Cours non trouvé"),
        @ApiResponse(responseCode = "403", description = "Accès refusé")
    })
    public ResponseEntity<CourseDetailResponse> updateCourse(
            @PathVariable @NotNull @Positive Long id,
            @Valid @RequestBody UpdateCourseRequest request) {
        
        CourseDetailResponse updatedCourse = courseService.updateCourse(id, request);
        return ResponseEntity.ok(updatedCourse);
    }

    /**
     * Supprime un cours (soft delete).
     * 
     * <p>Marque le cours comme supprimé sans l'effacer physiquement
     * pour préserver l'intégrité des données d'inscription.</p>
     * 
     * @param id Identifiant du cours à supprimer
     * @return Confirmation de suppression
     * @throws CourseNotFoundException Si le cours n'existe pas
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('INSTRUCTOR') and @courseService.isOwner(#id, authentication.name))")
    @Operation(summary = "Supprimer un cours", 
               description = "Supprime un cours (soft delete)")
    @ApiResponse(responseCode = "204", description = "Cours supprimé avec succès")
    public ResponseEntity<Void> deleteCourse(@PathVariable @NotNull @Positive Long id) {
        courseService.deleteCourse(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Upload d'une image pour un cours.
     * 
     * <p>Gère l'upload et le stockage des images de cours via le FileStorageService.
     * Supporte différents formats d'image avec validation de taille et type.</p>
     * 
     * @param id Identifiant du cours
     * @param image Fichier image à uploader
     * @return URL de l'image uploadée
     */
    @PostMapping("/{id}/image")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('INSTRUCTOR') and @courseService.isOwner(#id, authentication.name))")
    @Operation(summary = "Upload image de cours", 
               description = "Upload une image pour un cours")
    public ResponseEntity<String> uploadCourseImage(
            @PathVariable @NotNull @Positive Long id,
            @RequestParam("image") MultipartFile image) {
        
        String imageUrl = courseService.uploadCourseImage(id, image);
        return ResponseEntity.ok(imageUrl);
    }

    /**
     * Récupère les cours populaires basés sur les inscriptions.
     * 
     * @param limit Nombre maximum de cours à retourner
     * @return Liste des cours les plus populaires
     */
    @GetMapping("/popular")
    @Operation(summary = "Cours populaires", 
               description = "Récupère les cours les plus populaires")
    public ResponseEntity<List<CourseResponse>> getPopularCourses(
            @RequestParam(defaultValue = "10") int limit) {
        
        List<CourseResponse> popularCourses = courseService.getPopularCourses(limit);
        return ResponseEntity.ok(popularCourses);
    }

    /**
     * Récupère les cours d'un formateur spécifique.
     * 
     * @param instructorId Identifiant du formateur
     * @param pageable Configuration de pagination
     * @return Cours du formateur
     */
    @GetMapping("/instructor/{instructorId}")
    @Operation(summary = "Cours par formateur", 
               description = "Récupère les cours d'un formateur spécifique")
    public ResponseEntity<Page<CourseResponse>> getCoursesByInstructor(
            @PathVariable Long instructorId,
            @PageableDefault(size = 20) Pageable pageable) {
        
        Page<CourseResponse> courses = courseService.getCoursesByInstructor(instructorId, pageable);
        return ResponseEntity.ok(courses);
    }
}