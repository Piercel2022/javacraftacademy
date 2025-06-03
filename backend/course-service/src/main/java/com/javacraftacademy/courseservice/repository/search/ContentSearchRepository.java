// Localisation: src/main/java/com/javacraftacademy/courseservice/repository/search/ContentSearchRepository.java
package com.javacraftacademy.courseservice.repository.search;

import com.javacraftacademy.courseservice.entity.Content;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ContentSearchRepository extends ElasticsearchRepository<Content, Long> {

    @Query("{\"multi_match\": {\"query\": \"?0\", \"fields\": [\"title^3\", \"description^2\", \"transcript\"]}}")
    Page<Content> searchByKeyword(String keyword, Pageable pageable);

    @Query("{\"bool\": {\"must\": [{\"multi_match\": {\"query\": \"?0\", \"fields\": [\"title^3\", \"description^2\", \"transcript\"]}}, {\"term\": {\"courseId\": ?1}}]}}")
    Page<Content> searchInCourse(String keyword, Long courseId, Pageable pageable);

    @Query("{\"bool\": {\"must\": [{\"term\": {\"courseId\": ?0}}, {\"term\": {\"type\": \"?1\"}}]}}")
    Page<Content> findByCourseAndType(Long courseId, String type, Pageable pageable);

    @Query("{\"bool\": {\"must\": [{\"term\": {\"courseId\": ?0}}, {\"term\": {\"chapterId\": ?1}}]}}")
    Page<Content> findByCourseAndChapter(Long courseId, Long chapterId, Pageable pageable);

    @Query("{\"bool\": {\"must\": [{\"multi_match\": {\"query\": \"?0\", \"fields\": [\"transcript\"]}}, {\"term\": {\"type\": \"VIDEO\"}}]}}")
    Page<Content> searchInVideoTranscripts(String keyword, Pageable pageable);

    @Query("{\"bool\": {\"must\": [{\"term\": {\"type\": \"?0\"}}, {\"range\": {\"duration\": {\"gte\": ?1, \"lte\": ?2}}}]}}")
    Page<Content> findByTypeAndDurationRange(String type, Integer minDuration, Integer maxDuration, Pageable pageable);

    @Query("{\"bool\": {\"must\": [{\"terms\": {\"tags\": [?0]}}]}}")
    Page<Content> findByTags(List<String> tags, Pageable pageable);

    // Recherche de contenu par difficulté
    @Query("{\"bool\": {\"must\": [{\"term\": {\"difficulty\": \"?0\"}}, {\"term\": {\"courseId\": ?1}}]}}")
    Page<Content> findByDifficultyAndCourse(String difficulty, Long courseId, Pageable pageable);

    // Recherche avec filtres multiples
    @Query("{\"bool\": {\"must\": [{\"multi_match\": {\"query\": \"?0\", \"fields\": [\"title^3\", \"description^2\"]}}, {\"term\": {\"courseId\": ?1}}, {\"term\": {\"type\": \"?2\"}}, {\"term\": {\"difficulty\": \"?3\"}}]}}")
    Page<Content> searchWithFilters(String keyword, Long courseId, String type, String difficulty, Pageable pageable);

    // Recherche de contenu interactif
    @Query("{\"bool\": {\"must\": [{\"term\": {\"interactive\": true}}, {\"term\": {\"courseId\": ?0}}]}}")
    Page<Content> findInteractiveContent(Long courseId, Pageable pageable);

    // Suggestions pour l'autocomplétion
    @Query("{\"multi_match\": {\"query\": \"?0\", \"type\": \"phrase_prefix\", \"fields\": [\"title^3\", \"description\"]}}")
    List<Content> findContentSuggestions(String prefix);

    // Recherche de contenu récemment ajouté
    @Query("{\"bool\": {\"must\": [{\"range\": {\"createdDate\": {\"gte\": \"now-?0d\"}}}, {\"term\": {\"courseId\": ?1}}]}}")
    Page<Content> findRecentContent(Integer days, Long courseId, Pageable pageable);

    // Recherche de contenu par ordre dans le cours
    @Query("{\"bool\": {\"must\": [{\"term\": {\"courseId\": ?0}}], \"sort\": [{\"orderIndex\": {\"order\": \"asc\"}}]}}")
    List<Content> findByCourseOrderedByIndex(Long courseId);

    // Recherche de contenu similaire
    @Query("{\"more_like_this\": {\"fields\": [\"title\", \"description\", \"tags\"], \"like\": [{\"_index\": \"contents\", \"_id\": \"?0\"}], \"min_term_freq\": 1, \"max_query_terms\": 10}}")
    List<Content> findSimilarContent(Long contentId);
}