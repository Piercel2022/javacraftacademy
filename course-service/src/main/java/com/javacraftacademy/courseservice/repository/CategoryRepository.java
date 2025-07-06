package com.javacraftacademy.courseservice.repository;

import com.javacraftacademy.courseservice.model.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface pour la gestion des catégories de cours.
 * 
 * Cette interface étend JpaRepository pour fournir les opérations CRUD de base
 * et définit des méthodes personnalisées pour les besoins spécifiques de l'application.
 * 
 * Relations avec l'application :
 * - Utilisé par CategoryService pour l'accès aux données des catégories
 * - Connecté à l'entité Category qui peut avoir une relation avec Course (One-to-Many)
 * - Supporte la hiérarchie des catégories (parent-enfant)
 * - Intégré avec Spring Data JPA pour la gestion automatique des transactions
 * 
 * Fonctionnalités principales :
 * - Recherche par nom, slug, et code de catégorie
 * - Gestion des catégories hierarchiques (parent/enfant)
 * - Recherche de catégories actives/inactives
 * - Pagination et tri des résultats
 * - Comptage des cours par catégorie
 * - Recherche textuelle dans les noms et descriptions
 * 
 * @author JavaCraft Academy
 * @version 1.0
 * @since 1.0
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Recherche une catégorie par son nom exact.
     * 
     * Cette méthode est utilisée pour vérifier l'unicité des noms de catégories
     * lors de la création ou de la mise à jour.
     * 
     * @param name le nom de la catégorie à rechercher
     * @return Optional contenant la catégorie si trouvée, sinon Optional.empty()
     */
    Optional<Category> findByName(String name);

    /**
     * Recherche une catégorie par son slug.
     * 
     * Le slug est utilisé dans les URLs pour identifier de manière unique
     * une catégorie de façon SEO-friendly.
     * 
     * @param slug le slug de la catégorie
     * @return Optional contenant la catégorie si trouvée, sinon Optional.empty()
     */
    Optional<Category> findBySlug(String slug);

    /**
     * Recherche une catégorie par son code unique.
     * 
     * Le code est un identifiant technique unique utilisé pour
     * l'intégration avec des systèmes externes.
     * 
     * @param code le code de la catégorie
     * @return Optional contenant la catégorie si trouvée, sinon Optional.empty()
     */
    Optional<Category> findByCode(String code);

    /**
     * Récupère toutes les catégories actives.
     * 
     * Cette méthode est utilisée pour afficher uniquement les catégories
     * disponibles aux utilisateurs finaux.
     * 
     * @return List des catégories actives
     */
    List<Category> findByActiveTrue();

    /**
     * Récupère toutes les catégories inactives.
     * 
     * Utilisée principalement pour l'administration et la gestion
     * des catégories désactivées.
     * 
     * @return List des catégories inactives
     */
    List<Category> findByActiveFalse();

    /**
     * Récupère les catégories racines (sans parent).
     * 
     * Cette méthode est essentielle pour construire l'arbre hiérarchique
     * des catégories en commençant par les catégories de premier niveau.
     * 
     * @return List des catégories racines
     */
    List<Category> findByParentCategoryIsNull();

    /**
     * Récupère les catégories racines actives.
     * 
     * Combinaison de la recherche des catégories racines et du filtre actif,
     * utilisée pour l'affichage public de la hiérarchie des catégories.
     * 
     * @return List des catégories racines actives
     */
    List<Category> findByParentCategoryIsNullAndActiveTrue();

    /**
     * Récupère les sous-catégories d'une catégorie parent.
     * 
     * Utilisée pour construire l'arbre hiérarchique et permettre
     * la navigation dans les sous-catégories.
     * 
     * @param parentCategory la catégorie parent
     * @return List des sous-catégories
     */
    List<Category> findByParentCategory(Category parentCategory);

    /**
     * Récupère les sous-catégories actives d'une catégorie parent.
     * 
     * Version filtrée de la méthode précédente pour l'affichage public.
     * 
     * @param parentCategory la catégorie parent
     * @return List des sous-catégories actives
     */
    List<Category> findByParentCategoryAndActiveTrue(Category parentCategory);

    /**
     * Récupère les catégories par ordre de priorité décroissant.
     * 
     * Le champ displayOrder permet de contrôler l'ordre d'affichage
     * des catégories dans l'interface utilisateur.
     * 
     * @return List des catégories triées par ordre d'affichage
     */
    List<Category> findAllByOrderByDisplayOrderAsc();

    /**
     * Récupère les catégories actives par ordre de priorité.
     * 
     * Combinaison du filtre actif et du tri par ordre d'affichage.
     * 
     * @return List des catégories actives triées
     */
    List<Category> findByActiveTrueOrderByDisplayOrderAsc();

    /**
     * Recherche textuelle dans les noms de catégories.
     * 
     * Utilise LIKE avec des wildcards pour permettre une recherche
     * flexible et insensible à la casse.
     * 
     * @param name le terme de recherche
     * @return List des catégories correspondantes
     */
    List<Category> findByNameContainingIgnoreCase(String name);

    /**
     * Recherche textuelle dans les descriptions de catégories.
     * 
     * Permet de rechercher des catégories par leur contenu descriptif.
     * 
     * @param description le terme de recherche dans la description
     * @return List des catégories correspondantes
     */
    List<Category> findByDescriptionContainingIgnoreCase(String description);

    /**
     * Recherche combinée dans le nom et la description.
     * 
     * Recherche plus large permettant de trouver des catégories
     * soit par leur nom soit par leur description.
     * 
     * @param searchTerm le terme de recherche
     * @return List des catégories correspondantes
     */
    @Query("SELECT c FROM Category c WHERE " +
           "LOWER(c.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Category> findByNameOrDescriptionContaining(@Param("searchTerm") String searchTerm);

    /**
     * Recherche paginée des catégories actives.
     * 
     * Permet d'afficher les catégories par pages pour optimiser
     * les performances et l'expérience utilisateur.
     * 
     * @param pageable informations de pagination
     * @return Page des catégories actives
     */
    Page<Category> findByActiveTrue(Pageable pageable);

    /**
     * Compte le nombre de cours dans une catégorie.
     * 
     * Cette méthode utilise une requête personnalisée pour compter
     * les cours associés à une catégorie spécifique.
     * 
     * @param categoryId l'ID de la catégorie
     * @return le nombre de cours dans la catégorie
     */
    @Query("SELECT COUNT(c) FROM Course c WHERE c.category.id = :categoryId")
    Long countCoursesByCategory(@Param("categoryId") Long categoryId);

    /**
     * Trouve les catégories avec un nombre minimum de cours.
     * 
     * Utilisée pour identifier les catégories populaires ou
     * pour filtrer les catégories ayant du contenu.
     * 
     * @param minCourseCount nombre minimum de cours requis
     * @return List des catégories avec suffisamment de cours
     */
    @Query("SELECT c FROM Category c WHERE " +
           "(SELECT COUNT(course) FROM Course course WHERE course.category = c) >= :minCourseCount")
    List<Category> findCategoriesWithMinimumCourses(@Param("minCourseCount") Long minCourseCount);

    /**
     * Trouve les catégories populaires (avec le plus de cours).
     * 
     * Retourne les catégories triées par le nombre de cours décroissant,
     * limitées par le paramètre limit.
     * 
     * @param pageable informations de pagination pour limiter les résultats
     * @return Page des catégories les plus populaires
     */
    @Query("SELECT c FROM Category c LEFT JOIN Course course ON course.category = c " +
           "GROUP BY c ORDER BY COUNT(course) DESC")
    Page<Category> findMostPopularCategories(Pageable pageable);

    /**
     * Vérifie si une catégorie a des sous-catégories.
     * 
     * Utilisée avant la suppression d'une catégorie pour vérifier
     * les contraintes de hiérarchie.
     * 
     * @param categoryId l'ID de la catégorie parent
     * @return true si la catégorie a des sous-catégories, false sinon
     */
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END " +
           "FROM Category c WHERE c.parentCategory.id = :categoryId")
    boolean hasSubcategories(@Param("categoryId") Long categoryId);

    /**
     * Récupère l'arbre complet des catégories avec leurs niveaux.
     * 
     * Cette requête récursive (si supportée par la base de données)
     * permet de récupérer toute la hiérarchie des catégories.
     * 
     * @return List des catégories avec informations hiérarchiques
     */
    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.parentCategory " +
           "LEFT JOIN FETCH c.subcategories ORDER BY c.displayOrder")
    List<Category> findAllWithHierarchy();

    /**
     * Recherche les catégories par niveau dans la hiérarchie.
     * 
     * Permet de récupérer toutes les catégories d'un niveau spécifique
     * dans l'arbre hiérarchique.
     * 
     * @param level le niveau dans la hiérarchie (0 = racine, 1 = premier niveau, etc.)
     * @return List des catégories du niveau spécifié
     */
    @Query(value = "WITH RECURSIVE category_hierarchy AS (" +
                   "SELECT id, name, parent_category_id, 0 as level " +
                   "FROM categories WHERE parent_category_id IS NULL " +
                   "UNION ALL " +
                   "SELECT c.id, c.name, c.parent_category_id, ch.level + 1 " +
                   "FROM categories c " +
                   "INNER JOIN category_hierarchy ch ON c.parent_category_id = ch.id" +
                   ") SELECT * FROM category_hierarchy WHERE level = :level", 
           nativeQuery = true)
    List<Category> findCategoriesByLevel(@Param("level") Integer level);

    /**
     * Récupère les statistiques des catégories.
     * 
     * Requête personnalisée pour obtenir des informations statistiques
     * sur les catégories (nombre de cours, nombre d'inscriptions, etc.).
     * 
     * @return List d'objets contenant les statistiques des catégories
     */
    @Query("SELECT c.id, c.name, COUNT(course), COUNT(DISTINCT enrollment) " +
           "FROM Category c " +
           "LEFT JOIN Course course ON course.category = c " +
           "LEFT JOIN Enrollment enrollment ON enrollment.course = course " +
           "GROUP BY c.id, c.name")
    List<Object[]> getCategoryStatistics();

    /**
     * Trouve les catégories récemment créées.
     * 
     * Retourne les catégories créées dans les X derniers jours,
     * utile pour les tableaux de bord administratifs.
     * 
     * @param days nombre de jours à partir d'aujourd'hui
     * @return List des catégories récemment créées
     */
    @Query("SELECT c FROM Category c WHERE c.createdAt >= CURRENT_DATE - :days")
    List<Category> findRecentlyCreated(@Param("days") Integer days);

    /**
     * Vérifie l'existence d'une catégorie par nom (insensible à la casse).
     * 
     * Utilisée pour valider l'unicité des noms lors de la création
     * ou de la mise à jour des catégories.
     * 
     * @param name le nom à vérifier
     * @return true si le nom existe déjà, false sinon
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * Vérifie l'existence d'une catégorie par slug.
     * 
     * Utilisée pour garantir l'unicité des slugs pour les URLs.
     * 
     * @param slug le slug à vérifier
     * @return true si le slug existe déjà, false sinon
     */
    boolean existsBySlug(String slug);

    /**
     * Vérifie l'existence d'une catégorie par code.
     * 
     * Utilisée pour garantir l'unicité des codes techniques.
     * 
     * @param code le code à vérifier
     * @return true si le code existe déjà, false sinon
     */
    boolean existsByCode(String code);
}