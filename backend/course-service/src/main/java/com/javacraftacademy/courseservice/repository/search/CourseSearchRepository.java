// Localisation: src/main/java/com/javacraftacademy/courseservice/repository/search/CourseSearchRepository.java
package com.javacraftacademy.courseservice.repository.search;

import com.javacraftacademy.courseservice.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CourseSearchRepository extends ElasticsearchRepository<Course, Long> {

    @Query("{\"multi_match\": {\"query\": \"?0\", \"fields\": [\"title^3\", \"description^2\", \"tags^1.5\", \"category.name\"]}}")
    Page<Course> searchByKeyword(String keyword, Pageable pageable);

    @Query("{\"bool\": {\"must\": [{\"multi_match\": {\"query\": \"?0\", \"fields\": [\"title^3\", \"description^2\", \"tags^1.5\"]}}, {\"term\": {\"published\": true}}]}}")
    Page<Course> searchPublishedCourses(String keyword, Pageable pageable);

    @Query("{\"bool\": {\"must\": [{\"multi_match\": {\"query\": \"?0\", \"fields\": [\"title^3\", \"description^2\"]}}, {\"term\": {\"category.id\": ?1}}, {\"term\": {\"published\": true}}]}}")
    Page<Course> searchByKeywordAndCategory(String keyword, Long categoryId, Pageable pageable);

    @Query("{\"bool\": {\"must\": [{\"term\": {\"category.id\": ?0}}, {\"term\": {\"published\": true}}]}}")
    Page<Course> findByCategoryAndPublished(Long categoryId, Pageable pageable);

    @Query("{\"bool\": {\"must\": [{\"terms\": {\"tags\": [?0]}}, {\"term\": {\"published\": true}}]}}")
    Page<Course> findByTagsAndPublished(List<String> tags, Pageable pageable);

    @Query("{\"bool\": {\"must\": [{\"range\": {\"rating\": {\"gte\": ?0}}}, {\"term\": {\"published\": true}}]}}")
    Page<Course> findByMinimumRating(Double minRating, Pageable pageable);

    @Query("{\"bool\": {\"must\": [{\"term\": {\"level\": \"?0\"}}, {\"term\": {\"published\": true}}]}}")
    Page<Course> findByLevelAndPublished(String level, Pageable pageable);

    @Query("{\"bool\": {\"must\": [{\"term\": {\"instructorId\": ?0}}, {\"term\": {\"published\": true}}]}}")
    Page<Course> findByInstructorAndPublished(Long instructorId, Pageable pageable);

    // Recherche avec filtres multiples
    @Query("{\"bool\": {\"must\": [{\"multi_match\": {\"query\": \"?0\", \"fields\": [\"title^3\", \"description^2\"]}}, {\"term\": {\"category.id\": ?1}}, {\"term\": {\"level\": \"?2\"}}, {\"range\": {\"rating\": {\"gte\": ?3}}}, {\"term\": {\"published\": true}}]}}")
    Page<Course> searchWithFilters(String keyword, Long categoryId, String level, Double minRating, Pageable pageable);

    // Suggestions automatiques
    @Query("{\"multi_match\": {\"query\": \"?0\", \"type\": \"phrase_prefix\", \"fields\": [\"title^3\", \"description\"]}}")
    List<Course> findSuggestions(String prefix);

    // Recherche par similarité (More Like This)
    @Query("{\"more_like_this\": {\"fields\": [\"title\", \"description\", \"tags\"], \"like\": [{\"_index\": \"courses\", \"_id\": \"?0\"}], \"min_term_freq\": 1, \"max_query_terms\": 12}}")
    List<Course> findSimilarCourses(Long courseId);
}
