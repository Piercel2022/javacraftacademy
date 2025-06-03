// Certificate.java - Certificats de cours
package com.javacraftacademy.courseservice.entity;

import com.javacraftacademy.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;

@Entity
@Table(name = "certificates")
@Data
@EqualsAndHashCode(callSuper = true)
@SQLDelete(sql = "UPDATE certificates SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class Certificate extends BaseEntity {
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrollment_id", nullable = false)
    private Enrollment enrollment;
    
    @Column(name = "certificate_number", nullable = false, unique = true)
    private String certificateNumber;
    
    @Column(name = "issued_date", nullable = false)
    private LocalDateTime issuedDate = LocalDateTime.now();
    
    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;
    
    @Column(name = "verification_code", nullable = false)
    private String verificationCode;
    
    @Column(name = "pdf_path")
    private String pdfPath;
    
    @Column(name = "is_revoked")
    private Boolean isRevoked = false;
    
    @Column(name = "revoked_date")
    private LocalDateTime revokedDate;
    
    @Column(name = "revocation_reason")
    private String revocationReason;
    
    // Méthodes utilitaires
    public boolean isValid() {
        return !Boolean.TRUE.equals(isRevoked) && 
               (expiryDate == null || expiryDate.isAfter(LocalDateTime.now()));
    }
    
    public void revoke(String reason) {
        this.isRevoked = true;
        this.revokedDate = LocalDateTime.now();
        this.revocationReason = reason;
    }
}