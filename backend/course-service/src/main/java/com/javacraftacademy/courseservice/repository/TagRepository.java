// Localisation: src/main/java/com/javacraftacademy/courseservice/repository/TagRepository.java
package com.javacraftacademy.courseservice.repository;

import com.javacraftacademy.courseservice.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    
    Optional<Tag> findByName(String name);
    
    List<Tag> findByNameContainingIgnoreCase(String name);
    
    @Query("SELECT t FROM Tag t WHERE t.active = true")
    List<Tag> findActiveTags();
    
    @Query("SELECT t FROM Tag t ORDER BY t.usageCount DESC")
    List<Tag> findTagsOrderedByUsage();
    
    @Query("SELECT t FROM Tag t WHERE t.usageCount > 0 ORDER BY t.usageCount DESC")
    List<Tag> findPopularTags();
    
    @Query("SELECT t FROM Tag t WHERE t.usageCount >= :minUsage ORDER BY t.usageCount DESC")
    List<Tag> findTagsByMinimumUsage(@Param("minUsage") Long minUsage);
    
    @Query("SELECT t FROM Tag t JOIN t.courses c WHERE c.id = :courseId")
    List<Tag> findTagsByCourseId(@Param("courseId") Long courseId);
    
    boolean existsByName(String name);
}