package com.javacraftacademy.courseservice.repository.search;

import com.javacraftacademy.courseservice.entity.Content;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repository Elasticsearch pour la recherche avancée de contenu dans JavaCraft Academy.
 * 
 * <p>Cette interface étend ElasticsearchRepository pour fournir des capacités de recherche
 * full-text sophistiquées sur le contenu pédagogique. Elle utilise les annotations
 * @Query pour définir des requêtes Elasticsearch personnalisées.</p>
 * 
 * <h3>Fonctionnalités de recherche :</h3>
 * <ul>
 *   <li><strong>Recherche par mots-clés</strong> : Multi-match avec boost sur titre/description</li>
 *   <li><strong>Recherche contextuelle</strong> : Filtrage par cours, chapitre, type</li>
 *   <li><strong>Recherche dans les transcripts</strong> : Spécifique aux vidéos</li>
 *   <li><strong>Filtres avancés</strong> : Difficulté, durée, tags, interactivité</li>
 *   <li><strong>Suggestions</strong> : Autocomplétion et contenu similaire</li>
 *   <li><strong>Tri et organisation</strong> : Par pertinence, date, ordre de cours</li>
 * </ul>
 * 
 * <h3>Architecture et intégration :</h3>
 * <ul>
 *   <li><strong>Service Layer</strong> : Utilisé par ContentSearchService</li>
 *   <li><strong>Controller Layer</strong> : Exposé via ContentSearchController</li>
 *   <li><strong>Frontend</strong> : Alimente la barre de recherche et les filtres</li>
 *   <li><strong>Analytics</strong> : Données de recherche pour les recommandations</li>
 * </ul>
 * 
 * <h3>Stratégies de recherche :</h3>
 * <ul>
 *   <li><strong>Boost fields</strong> : title^3, description^2, transcript^1</li>
 *   <li><strong>Boolean queries</strong> : Combinaison de critères must/should</li>
 *   <li><strong>Range queries</strong> : Filtrage par durée et date</li>
 *   <li><strong>More Like This</strong> : Recommandations de contenu similaire</li>
 * </ul>
 * 
 * @author JavaCraft Academy Team
 * @version 1.0
 * @since 1.0
 * @see Content
 * @see org.springframework.data.elasticsearch.repository.ElasticsearchRepository
 */
@Repository
public interface ContentSearchRepository extends ElasticsearchRepository<Content, Long> {

    /**
     * Recherche générale par mot-clé avec boost sur les champs importants.
     * 
     * <p>Utilise multi_match pour chercher dans title (boost x3), description (boost x2)
     * et transcript. Idéal pour la barre de recherche principale.</p>
     * 
     * @param keyword Le mot-clé à rechercher
     * @param pageable Configuration de pagination
     * @return Page de contenus correspondants
     */
    @Query("{\"multi_match\": {\"query\": \"?0\", \"fields\": [\"title^3\", \"description^2\", \"transcript\"]}}")
    Page<Content> searchByKeyword(String keyword, Pageable pageable);

    /**
     * Recherche dans un cours spécifique.
     * 
     * <p>Combine recherche par mot-clé et filtrage par cours. Utilisé pour
     * la recherche contextuelle dans l'interface de cours.</p>
     * 
     * @param keyword Le mot-clé à rechercher
     * @param courseId L'ID du cours
     * @param pageable Configuration de pagination
     * @return Page de contenus du cours correspondants
     */
    @Query("{\"bool\": {\"must\": [{\"multi_match\": {\"query\": \"?0\", \"fields\": [\"title^3\", \"description^2\", \"transcript\"]}}, {\"term\": {\"courseId\": ?1}}]}}")
    Page<Content> searchInCourse(String keyword, Long courseId, Pageable pageable);

    /**
     * Trouve le contenu par cours et type.
     * 
     * @param courseId L'ID du cours
     * @param type Le type de contenu (VIDEO, ARTICLE, etc.)
     * @param pageable Configuration de pagination
     * @return Page de contenus filtrés
     */
    @Query("{\"bool\": {\"must\": [{\"term\": {\"courseId\": ?0}}, {\"term\": {\"type\": \"?1\"}}]}}")
    Page<Content> findByCourseAndType(Long courseId, String type, Pageable pageable);

    /**
     * Trouve le contenu par cours et chapitre.
     * 
     * @param courseId L'ID du cours
     * @param chapterId L'ID du chapitre
     * @param pageable Configuration de pagination
     * @return Page de contenus du chapitre
     */
    @Query("{\"bool\": {\"must\": [{\"term\": {\"courseId\": ?0}}, {\"term\": {\"chapterId\": ?1}}]}}")
    Page<Content> findByCourseAndChapter(Long courseId, Long chapterId, Pageable pageable);

    /**
     * Recherche spécifique dans les transcripts vidéo.
     * 
     * <p>Optimisé pour trouver des passages spécifiques dans les vidéos.
     * Particulièrement utile pour les fonctionnalités de sous-titres recherchables.</p>
     * 
     * @param keyword Le mot-clé à rechercher dans les transcripts
     * @param pageable Configuration de pagination
     * @return Page de vidéos avec transcripts correspondants
     */
    @Query("{\"bool\": {\"must\": [{\"multi_match\": {\"query\": \"?0\", \"fields\": [\"transcript\"]}}, {\"term\": {\"type\": \"VIDEO\"}}]}}")
    Page<Content> searchInVideoTranscripts(String keyword, Pageable pageable);

    /**
     * Filtre par type et plage de durée.
     * 
     * <p>Utile pour trouver du contenu de durée spécifique, par exemple :
     * - Vidéos courtes (< 5 min) pour révisions rapides
     * - Contenus longs (> 30 min) pour apprentissage approfondi</p>
     * 
     * @param type Le type de contenu
     * @param minDuration Durée minimale en secondes
     * @param maxDuration Durée maximale en secondes
     * @param pageable Configuration de pagination
     * @return Page de contenus dans la plage de durée
     */
    @Query("{\"bool\": {\"must\": [{\"term\": {\"type\": \"?0\"}}, {\"range\": {\"duration\": {\"gte\": ?1, \"lte\": ?2}}}]}}")
    Page<Content> findByTypeAndDurationRange(String type, Integer minDuration, Integer maxDuration, Pageable pageable);

    /**
     * Recherche par tags.
     * 
     * <p>Permet de trouver du contenu étiquetté avec des mots-clés spécifiques.
     * Supporte la recherche multi-tags avec opérateur OR.</p>
     * 
     * @param tags Liste des tags à rechercher
     * @param pageable Configuration de pagination
     * @return Page de contenus taggés
     */
    @Query("{\"bool\": {\"must\": [{\"terms\": {\"tags\": [?0]}}]}}")
    Page<Content> findByTags(List<String> tags, Pageable pageable);

    /**
     * Recherche par difficulté dans un cours.
     * 
     * @param difficulty Le niveau de difficulté (BEGINNER, INTERMEDIATE, etc.)
     * @param courseId L'ID du cours
     * @param pageable Configuration de pagination
     * @return Page de contenus du niveau de difficulté spécifié
     */
    @Query("{\"bool\": {\"must\": [{\"term\": {\"difficulty\": \"?0\"}}, {\"term\": {\"courseId\": ?1}}]}}")
    Page<Content> findByDifficultyAndCourse(String difficulty, Long courseId, Pageable pageable);

    /**
     * Recherche avancée avec filtres multiples.
     * 
     * <p>Combine recherche textuelle et filtres structurés. Idéal pour
     * les interfaces de recherche avancée avec multiple critères.</p>
     * 
     * @param keyword Mot-clé de recherche
     * @param courseId ID du cours
     * @param type Type de contenu
     * @param difficulty Niveau de difficulté
     * @param pageable Configuration de pagination
     * @return Page de contenus correspondant à tous les critères
     */
    @Query("{\"bool\": {\"must\": [{\"multi_match\": {\"query\": \"?0\", \"fields\": [\"title^3\", \"description^2\"]}}, {\"term\": {\"courseId\": ?1}}, {\"term\": {\"type\": \"?2\"}}, {\"term\": {\"difficulty\": \"?3\"}}]}}")
    Page<Content> searchWithFilters(String keyword, Long courseId, String type, String difficulty, Pageable pageable);

    /**
     * Trouve le contenu interactif d'un cours.
     * 
     * <p>Spécifiquement pour les exercices, quiz et autres contenus
     * nécessitant une interaction utilisateur.</p>
     * 
     * @param courseId L'ID du cours
     * @param pageable Configuration de pagination
     * @return Page de contenus interactifs
     */
    @Query("{\"bool\": {\"must\": [{\"term\": {\"interactive\": true}}, {\"term\": {\"courseId\": ?0}}]}}")
    Page<Content> findInteractiveContent(Long courseId, Pageable pageable);

    /**
     * Suggestions pour l'autocomplétion.
     * 
     * <p>Utilise phrase_prefix pour suggérer des contenus basés sur
     * la saisie partielle de l'utilisateur. Optimisé pour la réactivité.</p>
     * 
     * @param prefix Le début du terme recherché
     * @return Liste de suggestions de contenu
     */
    @Query("{\"multi_match\": {\"query\": \"?0\", \"type\": \"phrase_prefix\", \"fields\": [\"title^3\", \"description\"]}}")
    List<Content> findContentSuggestions(String prefix);

    /**
     * Contenu récemment ajouté.
     * 
     * <p>Trouve le contenu ajouté dans les X derniers jours.
     * Utile pour les sections "Nouveautés" et notifications.</p>
     * 
     * @param days Nombre de jours à considérer comme "récent"
     * @param courseId L'ID du cours
     * @param pageable Configuration de pagination
     * @return Page de contenus récents
     */
    @Query("{\"bool\": {\"must\": [{\"range\": {\"createdDate\": {\"gte\": \"now-?0d\"}}}, {\"term\": {\"courseId\": ?1}}]}}")
    Page<Content> findRecentContent(Integer days, Long courseId, Pageable pageable);

    /**
     * Contenu d'un cours ordonné par index.
     * 
     * <p>Retourne le contenu dans l'ordre pédagogique défini.
     * Essentiel pour l'affichage séquentiel des leçons.</p>
     * 
     * @param courseId L'ID du cours
     * @return Liste ordonnée des contenus du cours
     */
    @Query("{\"bool\": {\"must\": [{\"term\": {\"courseId\": ?0}}], \"sort\": [{\"orderIndex\": {\"order\": \"asc\"}}]}}")
    List<Content> findByCourseOrderedByIndex(Long courseId);

    /**
     * Trouve du contenu similaire (recommandations).
     * 
     * <p>Utilise l'algorithme "More Like This" d'Elasticsearch pour
     * suggérer du contenu similaire basé sur titre, description et tags.</p>
     * 
     * @param contentId L'ID du contenu de référence
     * @return Liste de contenus similaires
     */
    @Query("{\"more_like_this\": {\"fields\": [\"title\", \"description\", \"tags\"], \"like\": [{\"_index\": \"contents\", \"_id\": \"?0\"}], \"min_term_freq\": 1, \"max_query_terms\": 10}}")
    List<Content> findSimilarContent(Long contentId);

    // ===================================================================
    // MÉTHODES FUTURES À IMPLÉMENTER
    // ===================================================================
    
    /*
     * Exemples de fonctionnalités futures :
     * 
     * // Recherche géolocalisée pour le contenu local
     * @Query("{\"bool\": {\"must\": [{\"multi_match\": {\"query\": \"?0\"}}, {\"geo_distance\": {\"distance\": \"?1km\", \"location\": {\"lat\": ?2, \"lon\": ?3}}}]}}")
     * Page<Content> searchNearLocation(String keyword, Double distance, Double lat, Double lon, Pageable pageable);
     * 
     * // Recherche avec analyse de sentiment
     * @Query("{\"bool\": {\"must\": [{\"multi_match\": {\"query\": \"?0\"}}, {\"range\": {\"sentimentScore\": {\"gte\": ?1}}}]}}")
     * Page<Content> searchWithPositiveSentiment(String keyword, Double minScore, Pageable pageable);
     * 
     * // Recherche par popularité et évaluation
     * @Query("{\"bool\": {\"must\": [{\"multi_match\": {\"query\": \"?0\"}}], \"sort\": [{\"rating\": {\"order\": \"desc\"}}, {\"viewCount\": {\"order\": \"desc\"}}]}}")
     * Page<Content> searchByPopularity(String keyword, Pageable pageable);
     * 
     * // Recherche avec A/B testing
     * @Query("{\"function_score\": {\"query\": {\"multi_match\": {\"query\": \"?0\"}}, \"functions\": [{\"filter\": {\"term\": {\"experiment\": \"?1\"}}, \"weight\": ?2}]}}")
     * Page<Content> searchWithExperiment(String keyword, String experiment, Double weight, Pageable pageable);
     */
}