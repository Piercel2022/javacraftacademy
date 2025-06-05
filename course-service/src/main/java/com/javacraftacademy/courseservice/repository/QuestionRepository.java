// Localisation: src/main/java/com/javacraftacademy/courseservice/repository/QuestionRepository.java
package com.javacraftacademy.courseservice.repository;


import com.javacraftacademy.courseservice.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    
    List<Question> findByQuizIdOrderByOrderIndexAsc(Long quizId);
    
    @Query("SELECT q FROM Question q WHERE q.quiz.id = :quizId AND q.active = true ORDER BY q.orderIndex ASC")
    List<Question> findActiveQuestionsByQuizId(@Param("quizId") Long quizId);
    
    @Query("SELECT COUNT(q) FROM Question q WHERE q.quiz.id = :quizId")
    Long countQuestionsByQuizId(@Param("quizId") Long quizId);
    
    @Query("SELECT COUNT(q) FROM Question q WHERE q.quiz.id = :quizId AND q.active = true")
    Long countActiveQuestionsByQuizId(@Param("quizId") Long quizId);
    
    @Query("SELECT MAX(q.orderIndex) FROM Question q WHERE q.quiz.id = :quizId")
    Integer findMaxOrderIndexByQuizId(@Param("quizId") Long quizId);
    
    List<Question> findByQuestionType(String questionType);
    
    @Query("SELECT q FROM Question q WHERE q.quiz.lesson.course.id = :courseId")
    List<Question> findQuestionsByCourseId(@Param("courseId") Long courseId);
}