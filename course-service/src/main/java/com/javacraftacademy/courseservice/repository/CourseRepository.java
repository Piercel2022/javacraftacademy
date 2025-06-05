// Localisation: src/main/java/com/javacraftacademy/courseservice/repository/CourseRepository.java
package com.javacraftacademy.courseservice.repository;

import com.javacraftacademy.courseservice.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    
    Optional<Course> findBySlug(String slug);
    
    List<Course> findByInstructorId(Long instructorId);
    
    Page<Course> findByInstructorId(Long instructorId, Pageable pageable);
    
    List<Course> findByCategoryId(Long categoryId);
    
    Page<Course> findByCategoryId(Long categoryId, Pageable pageable);
    
    @Query("SELECT c FROM Course c WHERE c.published = true")
    Page<Course> findPublishedCourses(Pageable pageable);
    
    @Query("SELECT c FROM Course c WHERE c.published = true AND c.featured = true")
    List<Course> findFeaturedCourses();
    
    @Query("SELECT c FROM Course c WHERE c.title LIKE %:keyword% OR c.description LIKE %:keyword%")
    Page<Course> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
    
    @Query("SELECT c FROM Course c WHERE c.price BETWEEN :minPrice AND :maxPrice")
    Page<Course> findByPriceRange(@Param("minPrice") Double minPrice, @Param("maxPrice") Double maxPrice, Pageable pageable);
    
    @Query("SELECT c FROM Course c WHERE c.difficulty = :difficulty")
    Page<Course> findByDifficulty(@Param("difficulty") String difficulty, Pageable pageable);
    
    @Query("SELECT c FROM Course c WHERE c.createdAt >= :startDate")
    List<Course> findRecentCourses(@Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.course.id = :courseId")
    Long countEnrollmentsByCourseId(@Param("courseId") Long courseId);
    
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.course.id = :courseId")
    Double getAverageRating(@Param("courseId") Long courseId);
    
    boolean existsBySlug(String slug);
    
    boolean existsByTitleAndInstructorId(String title, Long instructorId);
}
