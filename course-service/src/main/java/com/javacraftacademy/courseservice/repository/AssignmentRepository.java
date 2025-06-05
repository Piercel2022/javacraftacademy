// Localisation: src/main/java/com/javacraftacademy/courseservice/repository/AssignmentRepository.java
package com.javacraftacademy.courseservice.repository;

import com.javacraftacademy.courseservice.entity.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    
    List<Assignment> findByLessonId(Long lessonId);
    
    List<Assignment> findByCourseId(Long courseId);
    
    @Query("SELECT a FROM Assignment a WHERE a.lesson.course.id = :courseId")
    List<Assignment> findAssignmentsByCourseId(@Param("courseId") Long courseId);
    
    @Query("SELECT a FROM Assignment a WHERE a.lesson.id = :lessonId AND a.published = true")
    List<Assignment> findPublishedAssignmentsByLessonId(@Param("lessonId") Long lessonId);
    
    @Query("SELECT a FROM Assignment a WHERE a.dueDate <= :dueDate AND a.published = true")
    List<Assignment> findAssignmentsDueBefore(@Param("dueDate") LocalDateTime dueDate);
    
    @Query("SELECT a FROM Assignment a WHERE a.dueDate BETWEEN :startDate AND :endDate")
    List<Assignment> findAssignmentsDueBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(a) FROM Assignment a WHERE a.lesson.id = :lessonId")
    Long countAssignmentsByLessonId(@Param("lessonId") Long lessonId);
    
    @Query("SELECT COUNT(a) FROM Assignment a WHERE a.lesson.course.id = :courseId")
    Long countAssignmentsByCourseId(@Param("courseId") Long courseId);
    
    Optional<Assignment> findByLessonIdAndTitle(Long lessonId, String title);
    
    boolean existsByLessonIdAndTitle(Long lessonId, String title);
}
