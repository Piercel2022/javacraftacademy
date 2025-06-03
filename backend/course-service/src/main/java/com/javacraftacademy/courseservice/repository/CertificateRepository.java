// Localisation: src/main/java/com/javacraftacademy/courseservice/repository/CertificateRepository.java
package com.javacraftacademy.courseservice.repository;

import com.javacraftacademy.courseservice.entity.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CertificateRepository extends JpaRepository<Certificate, Long> {
    
    Optional<Certificate> findByStudentIdAndCourseId(Long studentId, Long courseId);
    
    List<Certificate> findByStudentId(Long studentId);
    
    List<Certificate> findByCourseId(Long courseId);
    
    Optional<Certificate> findByCertificateNumber(String certificateNumber);
    
    @Query("SELECT c FROM Certificate c WHERE c.studentId = :studentId AND c.active = true")
    List<Certificate> findActiveCertificatesByStudent(@Param("studentId") Long studentId);
    
    @Query("SELECT c FROM Certificate c WHERE c.course.id = :courseId AND c.active = true")
    List<Certificate> findActiveCertificatesByCourse(@Param("courseId") Long courseId);
    
    @Query("SELECT COUNT(c) FROM Certificate c WHERE c.course.id = :courseId AND c.active = true")
    Long countActiveCertificatesByCourseId(@Param("courseId") Long courseId);
    
    @Query("SELECT c FROM Certificate c WHERE c.issuedAt >= :startDate")
    List<Certificate> findCertificatesIssuedAfter(@Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT c FROM Certificate c WHERE c.issuedAt BETWEEN :startDate AND :endDate")
    List<Certificate> findCertificatesIssuedBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT c FROM Certificate c WHERE c.expiresAt IS NOT NULL AND c.expiresAt < :currentDate AND c.active = true")
    List<Certificate> findExpiredCertificates(@Param("currentDate") LocalDateTime currentDate);
    
    boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);
    
    boolean existsByCertificateNumber(String certificateNumber);
}