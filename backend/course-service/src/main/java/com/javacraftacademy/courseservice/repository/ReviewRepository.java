// Localisation: src/main/java/com/javacraftacademy/courseservice/repository/ReviewRepository.java
package com.javacraftacademy.courseservice.repository;

import com.javacraftacademy.courseservice.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    
    Optional<Review> findByStudentIdAndCourseId(Long studentId, Long courseId);
    
    List<Review> findByCourseId(Long courseId);
    
    Page<Review> findByCourseId(Long courseId, Pageable pageable);
    
    List<Review> findByStudentId(Long studentId);
    
    @Query("SELECT r FROM Review r WHERE r.course.id = :courseId AND r.published = true ORDER BY r.createdAt DESC")
    List<Review> findPublishedReviewsByCourseId(@Param("courseId") Long courseId);
    
    @Query("SELECT r FROM Review r WHERE r.course.id = :courseId AND r.published = true ORDER BY r.createdAt DESC")
    Page<Review> findPublishedReviewsByCourseId(@Param("courseId") Long courseId, Pageable pageable);
    
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.course.id = :courseId AND r.published = true")
    Double getAverageRatingByCourseId(@Param("courseId") Long courseId);
    
    @Query("SELECT COUNT(r) FROM Review r WHERE r.course.id = :courseId AND r.published = true")
    Long countPublishedReviewsByCourseId(@Param("courseId") Long courseId);
    
    @Query("SELECT r FROM Review r WHERE r.rating = :rating AND r.published = true")
    List<Review> findByRating(@Param("rating") Integer rating);
    
    @Query("SELECT r FROM Review r WHERE r.rating >= :minRating AND r.published = true")
    List<Review> findByMinimumRating(@Param("minRating") Integer minRating);
    
    @Query("SELECT COUNT(r) FROM Review r WHERE r.course.id = :courseId AND r.rating = :rating AND r.published = true")
    Long countReviewsByRating(@Param("courseId") Long courseId, @Param("rating") Integer rating);
    
    boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);
}