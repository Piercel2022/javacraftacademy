package com.javacraftacademy.courseservice.controller;

import com.javacraftacademy.courseservice.dto.request.CreateCategoryRequest;
import com.javacraftacademy.courseservice.dto.request.UpdateCategoryRequest;
import com.javacraftacademy.courseservice.dto.response.CategoryResponse;
import com.javacraftacademy.courseservice.service.CategoryService;
import com.javacraftacademy.courseservice.validation.ValidCategoryData;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.List;

/**
 * CategoryController - Contrôleur REST pour la gestion des catégories de cours
 * 
 * <p>Cette classe fait partie intégrante du microservice course-service de l'application JavaCraft Academy.
 * Elle expose les endpoints REST pour toutes les opérations CRUD liées aux catégories de cours,
 * permettant aux utilisateurs et administrateurs de gérer la taxonomie des cours.</p>
 * 
 * <h3>Fonctionnalités principales :</h3>
 * <ul>
 *   <li><strong>Création de catégories</strong> - Permet aux administrateurs de créer de nouvelles catégories</li>
 *   <li><strong>Consultation des catégories</strong> - Récupération de toutes les catégories ou d'une catégorie spécifique</li>
 *   <li><strong>Mise à jour des catégories</strong> - Modification des informations d'une catégorie existante</li>
 *   <li><strong>Suppression de catégories</strong> - Suppression logique ou physique des catégories</li>
 *   <li><strong>Recherche et filtrage</strong> - Recherche de catégories par nom ou description</li>
 *   <li><strong>Pagination</strong> - Support de la pagination pour les listes de catégories</li>
 * </ul>
 * 
 * <h3>Relations et dépendances dans l'écosystème JavaCraft Academy :</h3>
 * <ul>
 *   <li><strong>CategoryService</strong> - Logique métier pour les opérations sur les catégories</li>
 *   <li><strong>Course Entity</strong> - Une catégorie peut contenir plusieurs cours (relation One-to-Many)</li>
 *   <li><strong>Security</strong> - Intégration avec le système de sécurité pour l'autorisation</li>
 *   <li><strong>Validation</strong> - Validation des données d'entrée pour maintenir l'intégrité</li>
 *   <li><strong>Swagger/OpenAPI</strong> - Documentation automatique de l'API</li>
 *   <li><strong>Logging</strong> - Traçabilité des opérations pour le monitoring</li>
 * </ul>
 * 
 * <h3>Architecture et patterns utilisés :</h3>
 * <ul>
 *   <li><strong>MVC Pattern</strong> - Séparation des responsabilités (Controller-Service-Repository)</li>
 *   <li><strong>DTO Pattern</strong> - Utilisation de DTOs pour l'encapsulation des données</li>
 *   <li><strong>RESTful Design</strong> - Respect des conventions REST pour les endpoints</li>
 *   <li><strong>Dependency Injection</strong> - Injection des dépendances via Spring</li>
 * </ul>
 * 
 * <h3>Extensibilité future :</h3>
 * <p>Pour ajouter de nouvelles fonctionnalités, considérez :</p>
 * <ul>
 *   <li>Ajout d'endpoints pour les statistiques de catégories</li>
 *   <li>Implémentation de catégories hiérarchiques (parent-enfant)</li>
 *   <li>Intégration avec un système de tags</li>
 *   <li>Support de l'internationalisation pour les noms de catégories</li>
 *   <li>Cache Redis pour améliorer les performances</li>
 * </ul>
 * 
 * @author JavaCraft Academy Team
 * @version 1.0
 * @since 2024
 */
@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor // Lombok - Génère un constructeur avec les champs final/non-null
@Slf4j // Lombok - Génère automatiquement un logger SLF4J
@Validated // Spring Validation - Active la validation au niveau des méthodes
@Tag(name = "Categories", description = "API de gestion des catégories de cours")
public class CategoryController {

    // Injection de dépendance du service métier
    private final CategoryService categoryService;

    /**
     * Récupère toutes les catégories avec support de la pagination.
     * 
     * <p>Cet endpoint permet de récupérer la liste complète des catégories disponibles
     * dans le système. Il supporte la pagination pour optimiser les performances
     * lors de la récupération de grandes quantités de données.</p>
     * 
     * @param pageable Paramètres de pagination (page, size, sort)
     * @return Une page contenant les catégories disponibles
     */
    @GetMapping
    @Operation(
        summary = "Récupérer toutes les catégories",
        description = "Récupère une liste paginée de toutes les catégories disponibles dans le système"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Liste des catégories récupérée avec succès"),
        @ApiResponse(responseCode = "500", description = "Erreur interne du serveur")
    })
    public ResponseEntity<Page<CategoryResponse>> getAllCategories(
            @Parameter(description = "Paramètres de pagination") Pageable pageable) {
        
        log.info("Récupération de toutes les catégories avec pagination: {}", pageable);
        
        Page<CategoryResponse> categories = categoryService.getAllCategories(pageable);
        
        log.info("Nombre de catégories récupérées: {}", categories.getTotalElements());
        
        return ResponseEntity.ok(categories);
    }

    /**
     * Récupère une catégorie spécifique par son identifiant.
     * 
     * @param id L'identifiant unique de la catégorie
     * @return La catégorie correspondant à l'identifiant fourni
     */
    @GetMapping("/{id}")
    @Operation(
        summary = "Récupérer une catégorie par ID",
        description = "Récupère les détails d'une catégorie spécifique en utilisant son identifiant unique"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Catégorie trouvée et retournée"),
        @ApiResponse(responseCode = "404", description = "Catégorie non trouvée"),
        @ApiResponse(responseCode = "400", description = "ID invalide")
    })
    public ResponseEntity<CategoryResponse> getCategoryById(
            @Parameter(description = "ID unique de la catégorie", required = true)
            @PathVariable @NotNull @Positive Long id) {
        
        log.info("Récupération de la catégorie avec l'ID: {}", id);
        
        CategoryResponse category = categoryService.getCategoryById(id);
        
        log.info("Catégorie récupérée: {}", category.getName());
        
        return ResponseEntity.ok(category);
    }

    /**
     * Recherche des catégories par nom.
     * 
     * <p>Permet de rechercher des catégories en utilisant une recherche textuelle
     * sur le nom de la catégorie. Utile pour les fonctionnalités d'autocomplétion
     * ou de recherche dans l'interface utilisateur.</p>
     * 
     * @param name Le nom ou partie du nom à rechercher
     * @param pageable Paramètres de pagination
     * @return Liste des catégories correspondant à la recherche
     */
    @GetMapping("/search")
    @Operation(
        summary = "Rechercher des catégories par nom",
        description = "Effectue une recherche de catégories basée sur le nom fourni"
    )
    public ResponseEntity<Page<CategoryResponse>> searchCategoriesByName(
            @Parameter(description = "Nom de la catégorie à rechercher")
            @RequestParam String name,
            Pageable pageable) {
        
        log.info("Recherche de catégories avec le nom: '{}'", name);
        
        Page<CategoryResponse> categories = categoryService.searchCategoriesByName(name, pageable);
        
        log.info("Nombre de catégories trouvées: {}", categories.getTotalElements());
        
        return ResponseEntity.ok(categories);
    }

    /**
     * Crée une nouvelle catégorie.
     * 
     * <p>Endpoint réservé aux administrateurs pour créer de nouvelles catégories.
     * La validation des données est effectuée automatiquement via les annotations
     * de validation sur le DTO de requête.</p>
     * 
     * @param request Les données de la nouvelle catégorie à créer
     * @return La catégorie créée avec son identifiant assigné
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('INSTRUCTOR')") // Sécurité - Seuls les admins/instructeurs peuvent créer
    @Operation(
        summary = "Créer une nouvelle catégorie",
        description = "Crée une nouvelle catégorie de cours. Nécessite les privilèges d'administrateur."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Catégorie créée avec succès"),
        @ApiResponse(responseCode = "400", description = "Données de requête invalides"),
        @ApiResponse(responseCode = "403", description = "Accès refusé - privilèges insuffisants"),
        @ApiResponse(responseCode = "409", description = "Conflit - catégorie déjà existante")
    })
    public ResponseEntity<CategoryResponse> createCategory(
            @Parameter(description = "Données de la nouvelle catégorie", required = true)
            @Valid @RequestBody CreateCategoryRequest request) {
        
        log.info("Création d'une nouvelle catégorie: {}", request.getName());
        
        CategoryResponse createdCategory = categoryService.createCategory(request);
        
        log.info("Catégorie créée avec succès. ID: {}, Nom: {}", 
                createdCategory.getId(), createdCategory.getName());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCategory);
    }

    /**
     * Met à jour une catégorie existante.
     * 
     * @param id L'identifiant de la catégorie à mettre à jour
     * @param request Les nouvelles données de la catégorie
     * @return La catégorie mise à jour
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INSTRUCTOR')")
    @Operation(
        summary = "Mettre à jour une catégorie",
        description = "Met à jour les informations d'une catégorie existante"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Catégorie mise à jour avec succès"),
        @ApiResponse(responseCode = "404", description = "Catégorie non trouvée"),
        @ApiResponse(responseCode = "400", description = "Données de requête invalides"),
        @ApiResponse(responseCode = "403", description = "Accès refusé")
    })
    public ResponseEntity<CategoryResponse> updateCategory(
            @Parameter(description = "ID de la catégorie à mettre à jour", required = true)
            @PathVariable @NotNull @Positive Long id,
            @Parameter(description = "Nouvelles données de la catégorie", required = true)
            @Valid @RequestBody UpdateCategoryRequest request) {
        
        log.info("Mise à jour de la catégorie ID: {} avec les données: {}", id, request.getName());
        
        CategoryResponse updatedCategory = categoryService.updateCategory(id, request);
        
        log.info("Catégorie mise à jour avec succès: {}", updatedCategory.getName());
        
        return ResponseEntity.ok(updatedCategory);
    }

    /**
     * Supprime une catégorie.
     * 
     * <p>Effectue une suppression logique de la catégorie. Si la catégorie
     * contient des cours, la suppression peut être refusée selon la politique
     * de l'application.</p>
     * 
     * @param id L'identifiant de la catégorie à supprimer
     * @return Réponse vide avec statut de succès
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Supprimer une catégorie",
        description = "Supprime une catégorie du système. Nécessite les privilèges d'administrateur."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Catégorie supprimée avec succès"),
        @ApiResponse(responseCode = "404", description = "Catégorie non trouvée"),
        @ApiResponse(responseCode = "403", description = "Accès refusé"),
        @ApiResponse(responseCode = "409", description = "Impossible de supprimer - catégorie utilisée par des cours")
    })
    public ResponseEntity<Void> deleteCategory(
            @Parameter(description = "ID de la catégorie à supprimer", required = true)
            @PathVariable @NotNull @Positive Long id) {
        
        log.info("Suppression de la catégorie ID: {}", id);
        
        categoryService.deleteCategory(id);
        
        log.info("Catégorie supprimée avec succès. ID: {}", id);
        
        return ResponseEntity.noContent().build();
    }

    /**
     * Récupère les catégories actives uniquement.
     * 
     * <p>Endpoint optimisé pour les interfaces utilisateur qui n'ont besoin
     * que des catégories actives (non supprimées/désactivées).</p>
     * 
     * @return Liste des catégories actives
     */
    @GetMapping("/active")
    @Operation(
        summary = "Récupérer les catégories actives",
        description = "Récupère uniquement les catégories actives et disponibles"
    )
    public ResponseEntity<List<CategoryResponse>> getActiveCategories() {
        
        log.info("Récupération des catégories actives");
        
        List<CategoryResponse> activeCategories = categoryService.getActiveCategories();
        
        log.info("Nombre de catégories actives: {}", activeCategories.size());
        
        return ResponseEntity.ok(activeCategories);
    }

    /**
     * Récupère les statistiques d'une catégorie.
     * 
     * <p>Endpoint pour obtenir des métriques sur une catégorie spécifique,
     * comme le nombre de cours, d'étudiants inscrits, etc.</p>
     * 
     * @param id L'identifiant de la catégorie
     * @return Statistiques de la catégorie
     */
    @GetMapping("/{id}/stats")
    @Operation(
        summary = "Récupérer les statistiques d'une catégorie",
        description = "Récupère les métriques et statistiques d'une catégorie spécifique"
    )
    public ResponseEntity<CategoryStatsResponse> getCategoryStats(
            @Parameter(description = "ID de la catégorie", required = true)
            @PathVariable @NotNull @Positive Long id) {
        
        log.info("Récupération des statistiques pour la catégorie ID: {}", id);
        
        CategoryStatsResponse stats = categoryService.getCategoryStats(id);
        
        log.info("Statistiques récupérées pour la catégorie: {} cours, {} étudiants", 
                stats.getTotalCourses(), stats.getTotalStudents());
        
        return ResponseEntity.ok(stats);
    }
}

/*
 * GUIDE D'EXTENSION ET BONNES PRATIQUES :
 * 
 * 1. AJOUT DE NOUVELLES FONCTIONNALITÉS :
 *    - Suivre le pattern établi pour les nouveaux endpoints
 *    - Ajouter la documentation Swagger appropriée
 *    - Implémenter la validation des données
 *    - Considérer les aspects de sécurité
 * 
 * 2. SÉCURITÉ :
 *    - Utiliser @PreAuthorize pour les contrôles d'accès
 *    - Valider toutes les entrées utilisateur
 *    - Logger les opérations sensibles
 * 
 * 3. PERFORMANCE :
 *    - Utiliser la pagination pour les listes importantes
 *    - Considérer la mise en cache pour les données fréquemment consultées
 *    - Optimiser les requêtes de base de données
 * 
 * 4. MONITORING :
 *    - Logger les opérations importantes
 *    - Ajouter des métriques personnalisées si nécessaire
 *    - Utiliser des codes de statut HTTP appropriés
 * 
 * 5. EXEMPLES D'EXTENSIONS FUTURES :
 *    - Endpoint pour l'import/export de catégories
 *    - Support des catégories hiérarchiques
 *    - Intégration avec un système de recommandations
 *    - API de statistiques avancées
 */