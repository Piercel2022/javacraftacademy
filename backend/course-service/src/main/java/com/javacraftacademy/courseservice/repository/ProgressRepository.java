// Localisation: src/main/java/com/javacraftacademy/courseservice/repository/ProgressRepository.java
package com.javacraftacademy.courseservice.repository;

import com.javacraftacademy.courseservice.entity.Progress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProgressRepository extends JpaRepository<Progress, Long> {
    
    Optional<Progress> findByStudentIdAndCourseId(Long studentId, Long courseId);
    
    List<Progress> findByStudentId(Long studentId);
    
    List<Progress> findByCourseId(Long courseId);
    
    @Query("SELECT p FROM Progress p WHERE p.studentId = :studentId AND p.course.id = :courseId")
    Optional<Progress> findStudentCourseProgress(@Param("studentId") Long studentId, @Param("courseId") Long courseId);
    
    @Query("SELECT p FROM Progress p WHERE p.studentId = :studentId AND p.completed = true")
    List<Progress> findCompletedCoursesByStudent(@Param("studentId") Long studentId);
    
    @Query("SELECT p FROM Progress p WHERE p.studentId = :studentId AND p.completed = false")
    List<Progress> findInProgressCoursesByStudent(@Param("studentId") Long studentId);
    
    @Query("SELECT COUNT(p) FROM Progress p WHERE p.course.id = :courseId AND p.completed = true")
    Long countCompletedStudentsByCourseId(@Param("courseId") Long courseId);
    
    @Query("SELECT COUNT(p) FROM Progress p WHERE p.course.id = :courseId")
    Long countEnrolledStudentsByCourseId(@Param("courseId") Long courseId);
    
    @Query("SELECT AVG(p.completionPercentage) FROM Progress p WHERE p.course.id = :courseId")
    Double getAverageProgressByCourseId(@Param("courseId") Long courseId);
    
    @Query("SELECT p FROM Progress p WHERE p.completionPercentage >= :percentage")
    List<Progress> findProgressByMinimumPercentage(@Param("percentage") Double percentage);
    
    @Query("SELECT p FROM Progress p WHERE p.studentId = :studentId ORDER BY p.lastAccessedAt DESC")
    List<Progress> findRecentProgressByStudent(@Param("studentId") Long studentId);
}