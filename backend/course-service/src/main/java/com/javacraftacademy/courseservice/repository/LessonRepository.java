// Localisation: src/main/java/com/javacraftacademy/courseservice/repository/LessonRepository.java
package com.javacraftacademy.courseservice.repository;

import com.javacraftacademy.courseservice.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {
    
    List<Lesson> findByCourseIdOrderByOrderIndexAsc(Long courseId);
    
    Optional<Lesson> findByCourseIdAndOrderIndex(Long courseId, Integer orderIndex);
    
    @Query("SELECT l FROM Lesson l WHERE l.course.id = :courseId AND l.published = true ORDER BY l.orderIndex ASC")
    List<Lesson> findPublishedLessonsByCourseId(@Param("courseId") Long courseId);
    
    @Query("SELECT COUNT(l) FROM Lesson l WHERE l.course.id = :courseId")
    Long countLessonsByCourseId(@Param("courseId") Long courseId);
    
    @Query("SELECT l FROM Lesson l WHERE l.course.id = :courseId AND l.orderIndex > :currentIndex ORDER BY l.orderIndex ASC")
    Optional<Lesson> findNextLesson(@Param("courseId") Long courseId, @Param("currentIndex") Integer currentIndex);
    
    @Query("SELECT l FROM Lesson l WHERE l.course.id = :courseId AND l.orderIndex < :currentIndex ORDER BY l.orderIndex DESC")
    Optional<Lesson> findPreviousLesson(@Param("courseId") Long courseId, @Param("currentIndex") Integer currentIndex);
    
    @Query("SELECT MAX(l.orderIndex) FROM Lesson l WHERE l.course.id = :courseId")
    Integer findMaxOrderIndexByCourseId(@Param("courseId") Long courseId);
    
    List<Lesson> findByTitleContainingIgnoreCase(String title);
    
    boolean existsByCourseIdAndTitle(Long courseId, String title);
}
