// Localisation: src/main/java/com/javacraftacademy/courseservice/repository/QuizRepository.java
package com.javacraftacademy.courseservice.repository;

import com.javacraftacademy.courseservice.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {
    
    List<Quiz> findByLessonId(Long lessonId);
    
    List<Quiz> findByCourseId(Long courseId);
    
    @Query("SELECT q FROM Quiz q WHERE q.lesson.course.id = :courseId")
    List<Quiz> findQuizzesByCourseId(@Param("courseId") Long courseId);
    
    @Query("SELECT q FROM Quiz q WHERE q.lesson.id = :lessonId AND q.published = true")
    List<Quiz> findPublishedQuizzesByLessonId(@Param("lessonId") Long lessonId);
    
    @Query("SELECT COUNT(q) FROM Quiz q WHERE q.lesson.id = :lessonId")
    Long countQuizzesByLessonId(@Param("lessonId") Long lessonId);
    
    @Query("SELECT COUNT(q) FROM Quiz q WHERE q.lesson.course.id = :courseId")
    Long countQuizzesByCourseId(@Param("courseId") Long courseId);
    
    Optional<Quiz> findByLessonIdAndTitle(Long lessonId, String title);
    
    @Query("SELECT q FROM Quiz q WHERE q.timeLimit IS NOT NULL AND q.timeLimit > 0")
    List<Quiz> findTimedQuizzes();
    
    boolean existsByLessonIdAndTitle(Long lessonId, String title);
}
