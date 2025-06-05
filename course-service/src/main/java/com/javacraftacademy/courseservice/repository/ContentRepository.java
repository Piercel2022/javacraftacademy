// Localisation: src/main/java/com/javacraftacademy/courseservice/repository/ContentRepository.java
package com.javacraftacademy.courseservice.repository;

import com.javacraftacademy.courseservice.entity.Content;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContentRepository extends JpaRepository<Content, Long> {
    
    List<Content> findByLessonIdOrderByOrderIndexAsc(Long lessonId);
    
    @Query("SELECT c FROM Content c WHERE c.lesson.id = :lessonId AND c.published = true ORDER BY c.orderIndex ASC")
    List<Content> findPublishedContentByLessonId(@Param("lessonId") Long lessonId);
    
    List<Content> findByContentType(String contentType);
    
    @Query("SELECT c FROM Content c WHERE c.lesson.course.id = :courseId")
    List<Content> findContentByCourseId(@Param("courseId") Long courseId);
    
    @Query("SELECT COUNT(c) FROM Content c WHERE c.lesson.id = :lessonId")
    Long countContentByLessonId(@Param("lessonId") Long lessonId);
    
    @Query("SELECT MAX(c.orderIndex) FROM Content c WHERE c.lesson.id = :lessonId")
    Integer findMaxOrderIndexByLessonId(@Param("lessonId") Long lessonId);
    
    Optional<Content> findByFilePath(String filePath);
    
    List<Content> findByFileUrl(String fileUrl);
    
    @Query("SELECT c FROM Content c WHERE c.fileSize > :minSize")
    List<Content> findLargeContent(@Param("minSize") Long minSize);
    
    @Query("SELECT SUM(c.fileSize) FROM Content c WHERE c.lesson.course.id = :courseId")
    Long getTotalContentSizeByCourseId(@Param("courseId") Long courseId);
    
    @Query("SELECT c FROM Content c WHERE c.title LIKE %:keyword% OR c.description LIKE %:keyword%")
    List<Content> searchByKeyword(@Param("keyword") String keyword);
}