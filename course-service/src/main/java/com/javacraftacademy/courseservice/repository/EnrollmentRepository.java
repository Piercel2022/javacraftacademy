// Localisation: src/main/java/com/javacraftacademy/courseservice/repository/EnrollmentRepository.java
package com.javacraftacademy.courseservice.repository;

import com.javacraftacademy.courseservice.entity.Enrollment;
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
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    
    Optional<Enrollment> findByStudentIdAndCourseId(Long studentId, Long courseId);
    
    List<Enrollment> findByStudentId(Long studentId);
    
    Page<Enrollment> findByStudentId(Long studentId, Pageable pageable);
    
    List<Enrollment> findByCourseId(Long courseId);
    
    Page<Enrollment> findByCourseId(Long courseId, Pageable pageable);
    
    @Query("SELECT e FROM Enrollment e WHERE e.studentId = :studentId AND e.active = true")
    List<Enrollment> findActiveEnrollmentsByStudent(@Param("studentId") Long studentId);
    
    @Query("SELECT e FROM Enrollment e WHERE e.course.id = :courseId AND e.active = true")
    List<Enrollment> findActiveEnrollmentsByCourse(@Param("courseId") Long courseId);
    
    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.course.id = :courseId AND e.active = true")
    Long countActiveEnrollmentsByCourseId(@Param("courseId") Long courseId);
    
    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.studentId = :studentId AND e.active = true")
    Long countActiveEnrollmentsByStudentId(@Param("studentId") Long studentId);
    
    @Query("SELECT e FROM Enrollment e WHERE e.enrolledAt >= :startDate")
    List<Enrollment> findRecentEnrollments(@Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT e FROM Enrollment e WHERE e.enrolledAt BETWEEN :startDate AND :endDate")
    List<Enrollment> findEnrollmentsBetweenDates(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);
    
    @Query("SELECT e FROM Enrollment e WHERE e.studentId = :studentId AND e.completedAt IS NOT NULL")
    List<Enrollment> findCompletedEnrollmentsByStudent(@Param("studentId") Long studentId);
}