// Localisation: src/main/java/com/javacraftacademy/courseservice/repository/AnswerRepository.java
package com.javacraftacademy.courseservice.repository;

import com.javacraftacademy.courseservice.entity.Answer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, Long> {
    
    List<Answer> findByQuestionIdOrderByOrderIndexAsc(Long questionId);
    
    @Query("SELECT a FROM Answer a WHERE a.question.id = :questionId AND a.correct = true")
    List<Answer> findCorrectAnswersByQuestionId(@Param("questionId") Long questionId);
    
    @Query("SELECT COUNT(a) FROM Answer a WHERE a.question.id = :questionId")
    Long countAnswersByQuestionId(@Param("questionId") Long questionId);
    
    @Query("SELECT COUNT(a) FROM Answer a WHERE a.question.id = :questionId AND a.correct = true")
    Long countCorrectAnswersByQuestionId(@Param("questionId") Long questionId);
    
    @Query("SELECT MAX(a.orderIndex) FROM Answer a WHERE a.question.id = :questionId")
    Integer findMaxOrderIndexByQuestionId(@Param("questionId") Long questionId);
    
    @Query("SELECT a FROM Answer a WHERE a.question.quiz.id = :quizId")
    List<Answer> findAnswersByQuizId(@Param("quizId") Long quizId);
}
