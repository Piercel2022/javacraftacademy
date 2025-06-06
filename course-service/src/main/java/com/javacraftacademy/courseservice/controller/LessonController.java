package com.javacraftacademy.courseservice.controller;

import com.javacraftacademy.courseservice.dto.request.CreateLessonRequest;
import com.javacraftacademy.courseservice.dto.request.UpdateLessonRequest;
import com.javacraftacademy.courseservice.dto.response.LessonResponse;
import com.javacraftacademy.courseservice.service.LessonService;
import com.javacraftacademy.courseservice.model.enums.LessonType;
import com.javacraftacademy.courseservice.exception.LessonNotFoundException;
import com.javacraftacademy.courseservice.exception.CourseNotFoundException;

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
 * Contrôleur REST pour la gestion des leçons dans JavaCraft Academy.
 * 
 * <p>Cette classe gère toutes les opérations CRUD relatives aux leçons
 * de cours. Elle constitue l'interface REST pour la manipulation du contenu
 * pédagogique détaillé des cours de formation.</p>
 * 
 * <h3>Fonctionnalités principales :</h3>
 * <ul>
 *   <li>Création et organisation des leçons dans un cours</li>
 *   <li>Gestion de différents types de contenu (vidéo, texte, quiz, exercice)</li>
 *   <li>Upload et gestion des ressources multimédias</li>
 *   <li>Réorganisation de l'ordre des leçons</li>
 *   <li>Gestion des prérequis entre leçons</li>
 *   <li>Tracking de progression des apprenants</li>
 * </ul>
 * 
 * <h3>Relations avec l'application :</h3>
 * <ul>
 *   <li><strong>LessonService</strong> : Logique métier des leçons</li>
 *   <li><strong>CourseController</strong> : Liaison parent-enfant avec les cours</li>
 *   <li><strong>FileStorageService</strong> : Stockage des ressources multimédias</li>
 *   <li><strong>EnrollmentService</strong> : Suivi de progression des apprenants</li>
 *   <li><strong>SecurityConfig</strong> : Contrôle d'accès au contenu</li>
 *   <li><strong>CourseContent</strong> : Entité de contenu riche</li>
 * </ul>
 * 
 * <h3>Types de leçons supportés :</h3>
 * <ul>
 *   <li><strong>VIDEO</strong> : Contenu vidéo avec transcription</li>
 *   <li><strong>TEXT</strong> : Contenu textuel riche (Markdown/HTML)</li>
 *   <li><strong>QUIZ</strong> : Questions à choix multiples</li>
 *   <li><strong>EXERCISE</strong> : Exercices pratiques interactifs</li>
 *   <li><strong>DOCUMENT</strong> : Documents téléchargeables</li>
 * </ul>
 * 
 * <h3>Extensions futures :</h3>
 * <ul>
 *   <li>Système de commentaires sur les leçons</li>
 *   <li>Évaluation automatique des exercices</li>
 *   <li>Streaming vidéo adaptatif</li>
 *   <li>Réalité virtuelle/augmentée pour certaines leçons</li>
 *   <li>Analyse d'engagement par leçon</li>
 * </ul>
 * 
 * @author JavaCraft Academy Team
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/api/v1/lessons")
@Tag(name = "Lesson Management", description = "APIs pour la gestion des leçons")
@Validated
@CrossOrigin(origins = "${app.cors.allowed-origins}")
public class LessonController {

    /**
     * Service de gestion des leçons - Contient la logique métier
     * pour toutes les opérations sur les leçons.
     */
    private final LessonService lessonService;

    /**
     * Constructeur avec injection de dépendance.
     * 
     * @param lessonService Service de gestion des leçons injecté par Spring
     */
    @Autowired
    public LessonController(LessonService lessonService) {
        this.lessonService = lessonService;
    }

    /**
     * Crée une nouvelle leçon dans un cours spécifique.
     * 
     * <p>Permet aux formateurs autorisés de créer des leçons de différents types.
     * La leçon est automatiquement positionnée à la fin du cours, mais peut
     * être réorganisée ultérieurement.</p>
     * 
     * @param courseId Identifiant du cours parent
     * @param request Données de la leçon à créer
     * @return Détails de la leçon créée
     * @throws CourseNotFoundException Si le cours parent n'existe pas
     */
    @PostMapping("/course/{courseId}")
    @PreAuthorize("hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    @Operation(summary = "Créer une leçon", 
               description = "Crée une nouvelle leçon dans un cours")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Leçon créée avec succès"),
        @ApiResponse(responseCode = "400", description = "Données invalides"),
        @ApiResponse(responseCode = "403", description = "Accès refusé"),
        @ApiResponse(responseCode = "404", description = "Cours non trouvé")
    })
    public ResponseEntity<LessonResponse> createLesson(
            @PathVariable @NotNull @Positive Long courseId,
            @Valid @RequestBody CreateLessonRequest request) {
        
        LessonResponse lesson = lessonService.createLesson(courseId, request);
        return new ResponseEntity<>(lesson, HttpStatus.CREATED);
    }

    /**
     * Récupère toutes les leçons d'un cours avec pagination.
     * 
     * <p>Retourne les leçons dans l'ordre défini par le formateur.
     * Filtre automatiquement selon les droits d'accès de l'utilisateur.</p>
     * 
     * @param courseId Identifiant du cours
     * @param pageable Configuration de pagination
     * @param type Filtre par type de leçon (optionnel)
     * @return Page de leçons du cours
     */
    @GetMapping("/course/{courseId}")
    @Operation(summary = "Leçons d'un cours", 
               description = "Récupère toutes les leçons d'un cours")
    @ApiResponse(responseCode = "200", description = "Leçons récupérées avec succès")
    public ResponseEntity<Page<LessonResponse>> getLessonsByCourse(
            @PathVariable @NotNull @Positive Long courseId,
            @PageableDefault(size = 50, sort = "orderIndex") Pageable pageable,
            @RequestParam(required = false) @Parameter(description = "Type de leçon") LessonType type) {
        
        Page<LessonResponse> lessons = lessonService.getLessonsByCourse(courseId, pageable, type);
        return ResponseEntity.ok(lessons);
    }

    /**
     * Récupère les détails d'une leçon spécifique.
     * 
     * <p>Vérifie automatiquement les droits d'accès basés sur l'inscription
     * au cours et le statut de progression de l'apprenant.</p>
     * 
     * @param id Identifiant de la leçon
     * @return Détails complets de la leçon
     * @throws LessonNotFoundException Si la leçon n'existe pas
     */
    @GetMapping("/{id}")
    @Operation(summary = "Détails d'une leçon", 
               description = "Récupère les informations détaillées d'une leçon")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Leçon trouvée"),
        @ApiResponse(responseCode = "404", description = "Leçon non trouvée"),
        @ApiResponse(responseCode = "403", description = "Accès refusé - Inscription requise")
    })
    public ResponseEntity<LessonResponse> getLesson(@PathVariable @NotNull @Positive Long id) {
        LessonResponse lesson = lessonService.getLessonById(id);
        return ResponseEntity.ok(lesson);
    }

    /**
     * Met à jour les informations d'une leçon existante.
     * 
     * <p>Permet la modification du contenu, du type, et des métadonnées.
     * Seuls les formateurs propriétaires du cours peuvent effectuer cette opération.</p>
     * 
     * @param id Identifiant de la leçon à modifier
     * @param request Nouvelles données de la leçon
     * @return Leçon mise à jour
     * @throws LessonNotFoundException Si la leçon n'existe pas
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('INSTRUCTOR') and @lessonService.isOwner(#id, authentication.name))")
    @Operation(summary = "Mettre à jour une leçon", 
               description = "Modifie les informations d'une leçon existante")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Leçon mise à jour"),
        @ApiResponse(responseCode = "404", description = "Leçon non trouvée"),
        @ApiResponse(responseCode = "403", description = "Accès refusé")
    })
    public ResponseEntity<LessonResponse> updateLesson(
            @PathVariable @NotNull @Positive Long id,
            @Valid @RequestBody UpdateLessonRequest request) {
        
        LessonResponse updatedLesson = lessonService.updateLesson(id, request);
        return ResponseEntity.ok(updatedLesson);
    }

    /**
     * Supprime une leçon (soft delete).
     * 
     * <p>Marque la leçon comme supprimée tout en préservant les données
     * de progression des apprenants pour l'historique.</p>
     * 
     * @param id Identifiant de la leçon à supprimer
     * @return Confirmation de suppression
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('INSTRUCTOR') and @lessonService.isOwner(#id, authentication.name))")
    @Operation(summary = "Supprimer une leçon", 
               description = "Supprime une leçon (soft delete)")
    @ApiResponse(responseCode = "204", description = "Leçon supprimée avec succès")
    public ResponseEntity<Void> deleteLesson(@PathVariable @NotNull @Positive Long id) {
        lessonService.deleteLesson(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Upload de ressources multimédias pour une leçon.
     * 
     * <p>Gère l'upload de vidéos, documents, images et autres ressources.
     * Intègre le FileStorageService pour le stockage sécurisé.</p>
     * 
     * @param id Identifiant de la leçon
     * @param file Fichier à uploader
     * @param type Type de ressource (video, document, image)
     * @return URL de la ressource uploadée
     */
    @PostMapping("/{id}/resources")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('INSTRUCTOR') and @lessonService.isOwner(#id, authentication.name))")
    @Operation(summary = "Upload ressource de leçon", 
               description = "Upload une ressource multimédia pour une leçon")
    public ResponseEntity<String> uploadLessonResource(
            @PathVariable @NotNull @Positive Long id,
            @RequestParam("file") MultipartFile file,
            @RequestParam("type") String type) {
        
        String resourceUrl = lessonService.uploadLessonResource(id, file, type);
        return ResponseEntity.ok(resourceUrl);
    }

    /**
     * Réorganise l'ordre des leçons dans un cours.
     * 
     * <p>Permet de changer l'ordre d'affichage des leçons selon
     * la progression pédagogique souhaitée par le formateur.</p>
     * 
     * @param courseId Identifiant du cours
     * @param lessonIds Liste ordonnée des IDs de leçons
     * @return Confirmation de réorganisation
     */
    @PutMapping("/course/{courseId}/reorder")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('INSTRUCTOR') and @courseService.isOwner(#courseId, authentication.name))")
    @Operation(summary = "Réorganiser les leçons", 
               description = "Change l'ordre des leçons dans un cours")
    public ResponseEntity<Void> reorderLessons(
            @PathVariable @NotNull @Positive Long courseId,
            @RequestBody @Valid List<Long> lessonIds) {
        
        lessonService.reorderLessons(courseId, lessonIds);
        return ResponseEntity.ok().build();
    }

    /**
     * Marque une leçon comme complétée pour un apprenant.
     * 
     * <p>Endpoint utilisé par les apprenants pour signaler la complétion
     * d'une leçon. Met à jour leur progression dans le cours.</p>
     * 
     * @param id Identifiant de la leçon
     * @return Confirmation de complétion
     */
    @PostMapping("/{id}/complete")
    @PreAuthorize("hasRole('STUDENT') and @enrollmentService.isEnrolled(#id, authentication.name)")
    @Operation(summary = "Marquer leçon complétée", 
               description = "Marque une leçon comme complétée par l'apprenant")
    public ResponseEntity<Void> completeLesson(@PathVariable @NotNull @Positive Long id) {
        lessonService.markLessonComplete(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Récupère la progression d'une leçon pour l'apprenant connecté.
     * 
     * <p>Retourne les informations de progression comme le temps passé,
     * le pourcentage de complétion, et les dernières interactions.</p>
     * 
     * @param id Identifiant de la leçon
     * @return Données de progression de la leçon
     */
    @GetMapping("/{id}/progress")
    @PreAuthorize("hasRole('STUDENT') and @enrollmentService.isEnrolled(#id, authentication.name)")
    @Operation(summary = "Progression de la leçon", 
               description = "Récupère la progression de l'apprenant sur une leçon")
    @ApiResponse(responseCode = "200", description = "Progression récupérée avec succès")
    public ResponseEntity<Object> getLessonProgress(@PathVariable @NotNull @Positive Long id) {
        Object progress = lessonService.getLessonProgress(id);
        return ResponseEntity.ok(progress);
    }

    /**
     * Récupère les prérequis d'une leçon.
     * 
     * <p>Retourne la liste des leçons qui doivent être complétées
     * avant de pouvoir accéder à cette leçon.</p>
     * 
     * @param id Identifiant de la leçon
     * @return Liste des prérequis
     */
    @GetMapping("/{id}/prerequisites")
    @Operation(summary = "Prérequis de la leçon", 
               description = "Récupère les prérequis d'une leçon")
    @ApiResponse(responseCode = "200", description = "Prérequis récupérés avec succès")
    public ResponseEntity<List<LessonResponse>> getLessonPrerequisites(
            @PathVariable @NotNull @Positive Long id) {
        
        List<LessonResponse> prerequisites = lessonService.getLessonPrerequisites(id);
        return ResponseEntity.ok(prerequisites);
    }

    /**
     * Définit les prérequis d'une leçon.
     * 
     * <p>Permet aux formateurs de configurer les dépendances entre leçons
     * pour créer un parcours d'apprentissage structuré.</p>
     * 
     * @param id Identifiant de la leçon
     * @param prerequisiteIds Liste des IDs des leçons prérequises
     * @return Confirmation de mise à jour
     */
    @PutMapping("/{id}/prerequisites")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('INSTRUCTOR') and @lessonService.isOwner(#id, authentication.name))")
    @Operation(summary = "Définir les prérequis", 
               description = "Configure les prérequis d'une leçon")
    @ApiResponse(responseCode = "200", description = "Prérequis mis à jour avec succès")
    public ResponseEntity<Void> setLessonPrerequisites(
            @PathVariable @NotNull @Positive Long id,
            @RequestBody @Valid List<Long> prerequisiteIds) {
        
        lessonService.setLessonPrerequisites(id, prerequisiteIds);
        return ResponseEntity.ok().build();
    }

    /**
     * Clone une leçon existante.
     * 
     * <p>Crée une copie complète d'une leçon incluant son contenu
     * et ses ressources, utile pour réutiliser du contenu pédagogique.</p>
     * 
     * @param id Identifiant de la leçon à cloner
     * @param targetCourseId Identifiant du cours de destination
     * @return Leçon clonée
     */
    @PostMapping("/{id}/clone")
    @PreAuthorize("hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    @Operation(summary = "Cloner une leçon", 
               description = "Crée une copie d'une leçon existante")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Leçon clonée avec succès"),
        @ApiResponse(responseCode = "404", description = "Leçon source non trouvée"),
        @ApiResponse(responseCode = "403", description = "Accès refusé")
    })
    public ResponseEntity<LessonResponse> cloneLesson(
            @PathVariable @NotNull @Positive Long id,
            @RequestParam @NotNull @Positive Long targetCourseId) {
        
        LessonResponse clonedLesson = lessonService.cloneLesson(id, targetCourseId);
        return new ResponseEntity<>(clonedLesson, HttpStatus.CREATED);
    }

    /**
     * Recherche de leçons par mots-clés.
     * 
     * <p>Permet de rechercher dans le contenu des leçons, les titres,
     * et les descriptions pour faciliter la navigation du contenu.</p>
     * 
     * @param query Terme de recherche
     * @param courseId Limite la recherche à un cours spécifique (optionnel)
     * @param pageable Configuration de pagination
     * @return Résultats de recherche paginés
     */
    @GetMapping("/search")
    @Operation(summary = "Rechercher des leçons", 
               description = "Recherche de leçons par mots-clés")
    @ApiResponse(responseCode = "200", description = "Résultats de recherche")
    public ResponseEntity<Page<LessonResponse>> searchLessons(
            @RequestParam @Parameter(description = "Terme de recherche") String query,
            @RequestParam(required = false) @Parameter(description = "ID du cours") Long courseId,
            @PageableDefault(size = 20) Pageable pageable) {
        
        Page<LessonResponse> results = lessonService.searchLessons(query, courseId, pageable);
        return ResponseEntity.ok(results);
    }

    /**
     * Export des données d'une leçon.
     * 
     * <p>Génère un export complet des données de la leçon au format
     * spécifié (JSON, PDF, etc.) pour archivage ou migration.</p>
     * 
     * @param id Identifiant de la leçon
     * @param format Format d'export souhaité
     * @return Données exportées
     */
    @GetMapping("/{id}/export")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('INSTRUCTOR') and @lessonService.isOwner(#id, authentication.name))")
    @Operation(summary = "Exporter une leçon", 
               description = "Exporte les données d'une leçon")
    @ApiResponse(responseCode = "200", description = "Export généré avec succès")
    public ResponseEntity<byte[]> exportLesson(
            @PathVariable @NotNull @Positive Long id,
            @RequestParam(defaultValue = "json") @Parameter(description = "Format d'export") String format) {
        
        byte[] exportData = lessonService.exportLesson(id, format);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=lesson-" + id + "." + format)
                .body(exportData);
    }
}