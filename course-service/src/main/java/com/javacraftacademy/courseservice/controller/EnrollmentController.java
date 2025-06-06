package com.javacraftacademy.courseservice.controller;

import com.javacraftacademy.courseservice.dto.request.EnrollmentRequest;
import com.javacraftacademy.courseservice.dto.response.EnrollmentResponse;
import com.javacraftacademy.courseservice.model.enums.EnrollmentStatus;
import com.javacraftacademy.courseservice.service.EnrollmentService;
import com.javacraftacademy.courseservice.messaging.producer.EnrollmentEventProducer;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * EnrollmentController - Contrôleur REST pour la gestion des inscriptions aux cours
 * 
 * <p>Cette classe constitue le point d'entrée principal pour toutes les opérations liées aux inscriptions
 * dans l'écosystème JavaCraft Academy. Elle orchestre les interactions entre les étudiants, les cours,
 * et le système de paiement pour gérer le cycle de vie complet d'une inscription.</p>
 * 
 * <h3>Fonctionnalités principales :</h3>
 * <ul>
 *   <li><strong>Inscription aux cours</strong> - Gestion du processus d'inscription des étudiants</li>
 *   <li><strong>Gestion des statuts</strong> - Suivi des différents états d'inscription (PENDING, ACTIVE, COMPLETED, CANCELLED)</li>
 *   <li><strong>Désistement</strong> - Annulation d'inscriptions selon les politiques définies</li>
 *   <li><strong>Consultation des inscriptions</strong> - Visualisation des inscriptions par utilisateur ou cours</li>
 *   <li><strong>Suivi de progression</strong> - Mise à jour du progrès des étudiants dans leurs cours</li>
 *   <li><strong>Statistiques et rapports</strong> - Métriques sur les inscriptions pour les analyses</li>
 * </ul>
 * 
 * <h3>Intégrations et dépendances dans l'architecture JavaCraft Academy :</h3>
 * <ul>
 *   <li><strong>User Service</strong> - Validation de l'existence et du statut des utilisateurs</li>
 *   <li><strong>Payment Service</strong> - Gestion des transactions financières via Kafka</li>
 *   <li><strong>Notification Service</strong> - Envoi de notifications d'inscription/confirmation</li>
 *   <li><strong>Course Entity</strong> - Vérification de la disponibilité et des prérequis des cours</li>
 *   <li><strong>Kafka Messaging</strong> - Communication asynchrone pour les événements d'inscription</li>
 *   <li><strong>Security Context</strong> - Authentification et autorisation des opérations</li>
 * </ul>
 * 
 * <h3>Patterns et architectures implémentés :</h3>
 * <ul>
 *   <li><strong>Event-Driven Architecture</strong> - Publication d'événements lors des changements d'état</li>
 *   <li><strong>CQRS Pattern</strong> - Séparation des opérations de lecture et d'écriture</li>
 *   <li><strong>State Machine</strong> - Gestion des transitions d'état des inscriptions</li>
 *   <li><strong>Circuit Breaker</strong> - Résilience lors des appels aux services externes</li>
 *   <li><strong>Saga Pattern</strong> - Coordination des transactions distribuées</li>
 * </ul>
 * 
 * <h3>Flux de données et événements :</h3>
 * <ul>
 *   <li><strong>Inscription</strong> : User → EnrollmentController → PaymentService → NotificationService</li>
 *   <li><strong>Confirmation</strong> : PaymentService → EnrollmentEventConsumer → EnrollmentService</li>
 *   <li><strong>Progression</strong> : LearningTracker → EnrollmentController → ProgressUpdate</li>
 * </ul>
 * 
 * <h3>Sécurité et contrôles d'accès :</h3>
 * <ul>
 *   <li><strong>Authentification</strong> - Vérification de l'identité via JWT</li>
 *   <li><strong>Autorisation</strong> - Contrôle d'accès basé sur les rôles (RBAC)</li>
 *   <li><strong>Ownership</strong> - Vérification que l'utilisateur ne peut accéder qu'à ses propres inscriptions</li>
 *   <li><strong>Rate Limiting</strong> - Protection contre les inscriptions abusives</li>
 * </ul>
 * 
 * <h3>Extensibilité et évolutions futures :</h3>
 * <ul>
 *   <li>Intégration avec un système de recommandations de cours</li>
 *   <li>Support des inscriptions par lots (bulk enrollment)</li>
 *   <li>Système de liste d'attente pour les cours complets</li>
 *   <li>Intégration avec des plateformes LMS externes</li>
 *   <li>Analytics avancées pour prédire les taux d'abandon</li>
 * </ul>
 * 
 * @author JavaCraft Academy Team
 * @version 1.0
 * @since 2024
 */
@RestController
@RequestMapping("/api/v1/enrollments")
@RequiredArgsConstructor // Lombok - Injection de dépendances via constructeur
@Slf4j // Lombok - Logger automatique
@Validated // Spring Validation - Validation au niveau des méthodes
@Tag(name = "Enrollments", description = "API de gestion des inscriptions aux cours")
@SecurityRequirement(name = "bearerAuth") // Swagger - Authentification requise
public class EnrollmentController {

    // Injection des services métier
    private final EnrollmentService enrollmentService;
    private final EnrollmentEventProducer enrollmentEventProducer;

    /**
     * Inscrit un utilisateur à un cours.
     * 
     * <p>Cette méthode initie le processus d'inscription en créant une inscription en statut PENDING,
     * puis déclenche le processus de paiement via Kafka. Une fois le paiement confirmé,
     * l'inscription passe au statut ACTIVE.</p>
     * 
     * <h4>Flux de traitement :</h4>
     * <ol>
     *   <li>Validation des données d'entrée</li>
     *   <li>Vérification des prérequis du cours</li>
     *   <li>Création de l'inscription (statut PENDING)</li>
     *   <li>Publication de l'événement d'inscription</li>
     *   <li>Déclenchement du processus de paiement</li>
     * </ol>
     * 
     * @param request Les détails de l'inscription (courseId, userId, etc.)
     * @return L'inscription créée avec son statut initial
     */
    @PostMapping
    @PreAuthorize("hasRole('STUDENT') or hasRole('ADMIN')")
    @Operation(
        summary = "S'inscrire à un cours",
        description = "Initie le processus d'inscription d'un étudiant à un cours spécifique"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Inscription créée avec succès"),
        @ApiResponse(responseCode = "400", description = "Données d'inscription invalides"),
        @ApiResponse(responseCode = "401", description = "Authentification requise"),
        @ApiResponse(responseCode = "403", description = "Accès refusé"),
        @ApiResponse(responseCode = "409", description = "Conflit - déjà inscrit ou cours complet"),
        @ApiResponse(responseCode = "422", description = "Prérequis non satisfaits")
    })
    public ResponseEntity<EnrollmentResponse> enrollInCourse(
            @Parameter(description = "Détails de l'inscription", required = true)
            @Valid @RequestBody EnrollmentRequest request) {
        
        // Récupération du contexte de sécurité pour l'utilisateur authentifié
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();
        
        log.info("Nouvelle demande d'inscription - Utilisateur: {}, Cours: {}, Type: {}", 
                currentUsername, request.getCourseId(), request.getEnrollmentType());
        
        // Validation métier et création de l'inscription
        EnrollmentResponse enrollment = enrollmentService.enrollUserInCourse(request, currentUsername);
        
        // Publication de l'événement d'inscription pour déclencher les processus downstream
        enrollmentEventProducer.publishEnrollmentCreated(enrollment);
        
        log.info("Inscription créée avec succès - ID: {}, Statut: {}", 
                enrollment.getId(), enrollment.getStatus());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(enrollment);
    }

    /**
     * Récupère toutes les inscriptions d'un utilisateur.
     * 
     * <p>Permet à un utilisateur de consulter l'historique complet de ses inscriptions
     * avec support de filtrage par statut et pagination pour les performances.</p>
     * 
     * @param userId L'identifiant de l'utilisateur (optionnel pour les admins)
     * @param status Filtrage par statut d'inscription (optionnel)
     * @param pageable Paramètres de pagination
     * @return Page contenant les inscriptions de l'utilisateur
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('STUDENT') and #userId == authentication.principal.userId)")
    @Operation(
        summary = "Récupérer les inscriptions d'un utilisateur",
        description = "Récupère toutes les inscriptions d'un utilisateur spécifique avec filtrage optionnel"
    )
    public ResponseEntity<Page<EnrollmentResponse>> getUserEnrollments(
            @Parameter(description = "ID de l'utilisateur", required = true)
            @PathVariable @NotNull @Positive Long userId,
            @Parameter(description = "Filtrage par statut d'inscription")
            @RequestParam(required = false) EnrollmentStatus status,
            @Parameter(description = "Paramètres de pagination")
            Pageable pageable) {
        
        log.info("Récupération des inscriptions - Utilisateur: {}, Statut: {}", userId, status);
        
        Page<EnrollmentResponse> enrollments = enrollmentService.getUserEnrollments(userId, status, pageable);
        
        log.info("Inscriptions récupérées avec succès - Utilisateur: {}, Nombre: {}", 
                userId, enrollments.getTotalElements());
        
        return ResponseEntity.ok(enrollments);
    }

    /**
     * Récupère les détails d'une inscription spécifique.
     * 
     * <p>Permet de consulter les détails complets d'une inscription, incluant
     * les informations de progression et l'historique des changements de statut.</p>
     * 
     * @param enrollmentId L'identifiant de l'inscription
     * @return Les détails de l'inscription
     */
    @GetMapping("/{enrollmentId}")
    @PreAuthorize("hasRole('ADMIN') or @enrollmentService.isEnrollmentOwner(#enrollmentId, authentication.principal.userId)")
    @Operation(
        summary = "Récupérer les détails d'une inscription",
        description = "Récupère les détails complets d'une inscription spécifique"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Détails de l'inscription récupérés"),
        @ApiResponse(responseCode = "404", description = "Inscription non trouvée"),
        @ApiResponse(responseCode = "403", description = "Accès refusé")
    })
    public ResponseEntity<EnrollmentResponse> getEnrollmentDetails(
            @Parameter(description = "ID de l'inscription", required = true)
            @PathVariable @NotNull @Positive Long enrollmentId) {
        
        log.info("Récupération des détails d'inscription - ID: {}", enrollmentId);
        
        EnrollmentResponse enrollment = enrollmentService.getEnrollmentById(enrollmentId);
        
        log.info("Détails d'inscription récupérés - ID: {}, Statut: {}", 
                enrollment.getId(), enrollment.getStatus());
        
        return ResponseEntity.ok(enrollment);
    }

    /**
     * Annule une inscription.
     * 
     * <p>Permet à un utilisateur d'annuler son inscription selon les politiques
     * de remboursement définies. Déclenche le processus de remboursement si applicable.</p>
     * 
     * @param enrollmentId L'identifiant de l'inscription à annuler
     * @return L'inscription mise à jour avec le statut CANCELLED
     */
    @DeleteMapping("/{enrollmentId}")
    @PreAuthorize("hasRole('ADMIN') or @enrollmentService.isEnrollmentOwner(#enrollmentId, authentication.principal.userId)")
    @Operation(
        summary = "Annuler une inscription",
        description = "Annule une inscription et déclenche le processus de remboursement si applicable"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Inscription annulée avec succès"),
        @ApiResponse(responseCode = "400", description = "Inscription ne peut pas être annulée"),
        @ApiResponse(responseCode = "404", description = "Inscription non trouvée"),
        @ApiResponse(responseCode = "403", description = "Accès refusé")
    })
    public ResponseEntity<EnrollmentResponse> cancelEnrollment(
            @Parameter(description = "ID de l'inscription à annuler", required = true)
            @PathVariable @NotNull @Positive Long enrollmentId) {
        
        log.info("Demande d'annulation d'inscription - ID: {}", enrollmentId);
        
        EnrollmentResponse cancelledEnrollment = enrollmentService.cancelEnrollment(enrollmentId);
        
        // Publication de l'événement d'annulation
        enrollmentEventProducer.publishEnrollmentCancelled(cancelledEnrollment);
        
        log.info("Inscription annulée avec succès - ID: {}, Nouveau statut: {}", 
                enrollmentId, cancelledEnrollment.getStatus());
        
        return ResponseEntity.ok(cancelledEnrollment);
    }

    /**
     * Met à jour la progression d'un étudiant dans un cours.
     * 
     * <p>Permet de mettre à jour le pourcentage de progression d'un étudiant
     * dans son cours. Peut déclencher des événements de complétion si 100% atteint.</p>
     * 
     * @param enrollmentId L'identifiant de l'inscription
     * @param progressData Les données de progression à mettre à jour
     * @return L'inscription mise à jour avec la nouvelle progression
     */
    @PutMapping("/{enrollmentId}/progress")
    @PreAuthorize("hasRole('ADMIN') or @enrollmentService.isEnrollmentOwner(#enrollmentId, authentication.principal.userId)")
    @Operation(
        summary = "Mettre à jour la progression",
        description = "Met à jour la progression d'un étudiant dans son cours"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Progression mise à jour"),
        @ApiResponse(responseCode = "400", description = "Données de progression invalides"),
        @ApiResponse(responseCode = "404", description = "Inscription non trouvée"),
        @ApiResponse(responseCode = "403", description = "Accès refusé")
    })
    public ResponseEntity<EnrollmentResponse> updateProgress(
            @Parameter(description = "ID de l'inscription", required = true)
            @PathVariable @NotNull @Positive Long enrollmentId,
            @Parameter(description = "Données de progression", required = true)
            @Valid @RequestBody Map<String, Object> progressData) {
        
        log.info("Mise à jour de progression - Inscription: {}, Données: {}", 
                enrollmentId, progressData);
        
        EnrollmentResponse updatedEnrollment = enrollmentService.updateProgress(enrollmentId, progressData);
        
        // Publication de l'événement de progression
        enrollmentEventProducer.publishProgressUpdated(updatedEnrollment);
        
        log.info("Progression mise à jour - ID: {}, Nouveau pourcentage: {}%", 
                enrollmentId, updatedEnrollment.getProgressPercentage());
        
        return ResponseEntity.ok(updatedEnrollment);
    }

    /**
     * Récupère les inscriptions pour un cours spécifique.
     * 
     * <p>Permet aux instructeurs et administrateurs de voir tous les étudiants
     * inscrits à un cours particulier avec leurs statuts et progressions.</p>
     * 
     * @param courseId L'identifiant du cours
     * @param status Filtrage par statut (optionnel)
     * @param pageable Paramètres de pagination
     * @return Page contenant les inscriptions du cours
     */
    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INSTRUCTOR')")
    @Operation(
        summary = "Récupérer les inscriptions d'un cours",
        description = "Récupère toutes les inscriptions pour un cours spécifique"
    )
    public ResponseEntity<Page<EnrollmentResponse>> getCourseEnrollments(
            @Parameter(description = "ID du cours", required = true)
            @PathVariable @NotNull @Positive Long courseId,
            @Parameter(description = "Filtrage par statut d'inscription")
            @RequestParam(required = false) EnrollmentStatus status,
            @Parameter(description = "Paramètres de pagination")
            Pageable pageable) {
        
        log.info("Récupération des inscriptions du cours - Cours: {}, Statut: {}", courseId, status);
        
        Page<EnrollmentResponse> enrollments = enrollmentService.getCourseEnrollments(courseId, status, pageable);
        
        log.info("Inscriptions du cours récupérées - Cours: {}, Nombre: {}", 
                courseId, enrollments.getTotalElements());
        
        return ResponseEntity.ok(enrollments);
    }

    /**
     * Récupère les statistiques d'inscription.
     * 
     * <p>Fournit des métriques détaillées sur les inscriptions pour les analyses
     * et rapports administratifs.</p>
     * 
     * @param startDate Date de début pour les statistiques (optionnel)
     * @param endDate Date de fin pour les statistiques (optionnel)
     * @return Les statistiques d'inscription
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INSTRUCTOR')")
    @Operation(
        summary = "Récupérer les statistiques d'inscription",
        description = "Récupère les métriques et statistiques sur les inscriptions"
    )
    public ResponseEntity<Map<String, Object>> getEnrollmentStatistics(
            @Parameter(description = "Date de début (format: yyyy-MM-dd'T'HH:mm:ss)")
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "Date de fin (format: yyyy-MM-dd'T'HH:mm:ss)")
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        log.info("Récupération des statistiques d'inscription - Période: {} à {}", startDate, endDate);
        
        Map<String, Object> statistics = enrollmentService.getEnrollmentStatistics(startDate, endDate);
        
        log.info("Statistiques d'inscription récupérées - Nombre de métriques: {}", statistics.size());
        
        return ResponseEntity.ok(statistics);
    }

    /**
     * Inscription en lot pour les administrateurs.
     * 
     * <p>Permet aux administrateurs d'inscrire plusieurs utilisateurs
     * à un ou plusieurs cours en une seule opération.</p>
     * 
     * @param bulkEnrollmentRequest Les détails des inscriptions en lot
     * @return La liste des inscriptions créées
     */
    @PostMapping("/bulk")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Inscription en lot",
        description = "Inscrit plusieurs utilisateurs à des cours en une seule opération"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Inscriptions en lot créées"),
        @ApiResponse(responseCode = "400", description = "Données d'inscription invalides"),
        @ApiResponse(responseCode = "403", description = "Accès refusé - Administrateur requis")
    })
    public ResponseEntity<List<EnrollmentResponse>> bulkEnrollment(
            @Parameter(description = "Détails des inscriptions en lot", required = true)
            @Valid @RequestBody List<EnrollmentRequest> bulkEnrollmentRequest) {
        
        log.info("Demande d'inscription en lot - Nombre d'inscriptions: {}", bulkEnrollmentRequest.size());
        
        List<EnrollmentResponse> enrollments = enrollmentService.bulkEnrollment(bulkEnrollmentRequest);
        
        // Publication des événements d'inscription en lot
        enrollments.forEach(enrollmentEventProducer::publishEnrollmentCreated);
        
        log.info("Inscriptions en lot créées avec succès - Nombre: {}", enrollments.size());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(enrollments);
    }

    /**
     * Réactive une inscription annulée.
     * 
     * <p>Permet de réactiver une inscription précédemment annulée,
     * sous réserve des politiques de réactivation.</p>
     * 
     * @param enrollmentId L'identifiant de l'inscription à réactiver
     * @return L'inscription réactivée
     */
    @PutMapping("/{enrollmentId}/reactivate")
    @PreAuthorize("hasRole('ADMIN') or @enrollmentService.isEnrollmentOwner(#enrollmentId, authentication.principal.userId)")
    @Operation(
        summary = "Réactiver une inscription",
        description = "Réactive une inscription précédemment annulée"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Inscription réactivée avec succès"),
        @ApiResponse(responseCode = "400", description = "Inscription ne peut pas être réactivée"),
        @ApiResponse(responseCode = "404", description = "Inscription non trouvée"),
        @ApiResponse(responseCode = "403", description = "Accès refusé")
    })
    public ResponseEntity<EnrollmentResponse> reactivateEnrollment(
            @Parameter(description = "ID de l'inscription à réactiver", required = true)
            @PathVariable @NotNull @Positive Long enrollmentId) {
        
        log.info("Demande de réactivation d'inscription - ID: {}", enrollmentId);
        
        EnrollmentResponse reactivatedEnrollment = enrollmentService.reactivateEnrollment(enrollmentId);
        
        // Publication de l'événement de réactivation
        enrollmentEventProducer.publishEnrollmentReactivated(reactivatedEnrollment);
        
        log.info("Inscription réactivée avec succès - ID: {}, Nouveau statut: {}", 
                enrollmentId, reactivatedEnrollment.getStatus());
        
        return ResponseEntity.ok(reactivatedEnrollment);
    }

    /**
     * Exporte les données d'inscription.
     * 
     * <p>Permet d'exporter les données d'inscription dans différents formats
     * pour les rapports et analyses externes.</p>
     * 
     * @param format Le format d'export (CSV, Excel, JSON)
     * @param startDate Date de début pour l'export (optionnel)
     * @param endDate Date de fin pour l'export (optionnel)
     * @return Les données d'inscription exportées
     */
    @GetMapping("/export")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Exporter les données d'inscription",
        description = "Exporte les données d'inscription dans le format spécifié"
    )
    public ResponseEntity<byte[]> exportEnrollmentData(
            @Parameter(description = "Format d'export (CSV, EXCEL, JSON)")
            @RequestParam(defaultValue = "CSV") String format,
            @Parameter(description = "Date de début pour l'export")
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "Date de fin pour l'export")
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        log.info("Demande d'export des données d'inscription - Format: {}, Période: {} à {}", 
                format, startDate, endDate);
        
        byte[] exportData = enrollmentService.exportEnrollmentData(format, startDate, endDate);
        
        String filename = "enrollments_" + LocalDateTime.now().toString().replace(":", "-") + 
                         "." + format.toLowerCase();
        
        log.info("Export des données d'inscription terminé - Taille: {} bytes, Fichier: {}", 
                exportData.length, filename);
        
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=" + filename)
                .header("Content-Type", getContentTypeByFormat(format))
                .body(exportData);
    }

    /**
     * Détermine le type de contenu basé sur le format d'export.
     * 
     * @param format Le format d'export
     * @return Le type de contenu MIME correspondant
     */
    private String getContentTypeByFormat(String format) {
        switch (format.toUpperCase()) {
            case "CSV":
                return "text/csv";
            case "EXCEL":
                return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "JSON":
                return "application/json";
            default:
                return "application/octet-stream";
        }
    }
}