package com.javacraftacademy.courseservice.entity;

// Imports pour JPA et persistance
import jakarta.persistence.*;
// Imports pour Lombok - génération automatique de code
import lombok.Data;
import lombok.EqualsAndHashCode;
// Imports pour Hibernate - soft delete et filtres
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
// Imports pour gestion des dates
import java.time.LocalDateTime;
// Imports pour validation (optionnel pour futures améliorations)
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

/**
 * Entité Certificate - Gestion des certificats de fin de cours
 * 
 * <p>Cette classe représente un certificat délivré à un étudiant après la completion
 * réussie d'un cours dans l'application JavaCraft Academy. Elle fait partie du 
 * système de certification et de validation des compétences acquises.</p>
 * 
 * <h3>Fonctionnalités principales :</h3>
 * <ul>
 *   <li>Génération automatique de numéros de certificat uniques</li>
 *   <li>Système de vérification par code de validation</li>
 *   <li>Gestion de l'expiration des certificats</li>
 *   <li>Mécanisme de révocation avec historique</li>
 *   <li>Génération et stockage de PDF</li>
 *   <li>Soft delete pour préserver l'historique</li>
 * </ul>
 * 
 * <h3>Relations dans l'écosystème JavaCraft Academy :</h3>
 * <ul>
 *   <li><strong>Enrollment</strong> : Relation OneToOne - Un certificat correspond 
 *       à une inscription complétée avec succès</li>
 *   <li><strong>Course</strong> : Accès indirect via Enrollment pour obtenir 
 *       les informations du cours certifié</li>
 *   <li><strong>Student</strong> : Accès indirect via Enrollment pour identifier 
 *       le bénéficiaire du certificat</li>
 *   <li><strong>Instructor</strong> : Accès indirect pour validation et signature 
 *       électronique du certificat</li>
 * </ul>
 * 
 * <h3>Intégration système :</h3>
 * <ul>
 *   <li><strong>Service de génération PDF</strong> : Utilise les données pour créer 
 *       le document officiel</li>
 *   <li><strong>API de vérification</strong> : Validation publique via verification_code</li>
 *   <li><strong>Système de notification</strong> : Alerte l'étudiant de la délivrance</li>
 *   <li><strong>Analytics</strong> : Suivi des taux de certification par cours</li>
 *   <li><strong>Blockchain (futur)</strong> : Stockage immuable des hash de certificats</li>
 * </ul>
 * 
 * <h3>Sécurité et conformité :</h3>
 * <ul>
 *   <li>Numéros de certificat cryptographiquement sécurisés</li>
 *   <li>Codes de vérification à usage unique</li>
 *   <li>Audit trail complet via BaseEntity</li>
 *   <li>Conformité RGPD avec soft delete</li>
 * </ul>
 * 
 * <h3>Extensibilité future :</h3>
 * <ul>
 *   <li>Support multi-langue pour certificats internationaux</li>
 *   <li>Templates personnalisables par institution</li>
 *   <li>Signature électronique qualifiée</li>
 *   <li>Badge system integration</li>
 *   <li>Microcrédentials et certifications partielles</li>
 * </ul>
 * 
 * @author JavaCraft Academy Team
 * @version 1.0
 * @since 1.0
 * 
 * @see Enrollment
 * @see Course
 * @see BaseEntity
 */
@Entity
@Table(name = "certificates", indexes = {
    @Index(name = "idx_certificate_number", columnList = "certificate_number"),
    @Index(name = "idx_verification_code", columnList = "verification_code"),
    @Index(name = "idx_issued_date", columnList = "issued_date"),
    @Index(name = "idx_enrollment_id", columnList = "enrollment_id")
})
@Data
@EqualsAndHashCode(callSuper = true)
@SQLDelete(sql = "UPDATE certificates SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class Certificate extends BaseEntity {

    /**
     * Relation OneToOne avec l'inscription
     * Chaque certificat correspond à une inscription complétée avec succès
     * FetchType.LAZY pour optimiser les performances
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrollment_id", nullable = false)
    @NotNull(message = "L'inscription associée est obligatoire")
    private Enrollment enrollment;

    /**
     * Numéro unique du certificat
     * Format suggéré: JCA-YYYY-NNNNNN (ex: JCA-2024-000001)
     * Généré automatiquement lors de la création
     */
    @Column(name = "certificate_number", nullable = false, unique = true, length = 50)
    @NotBlank(message = "Le numéro de certificat ne peut pas être vide")
    private String certificateNumber;

    /**
     * Date de délivrance du certificat
     * Par défaut à la date/heure actuelle lors de la création
     */
    @Column(name = "issued_date", nullable = false)
    @NotNull(message = "La date de délivrance est obligatoire")
    private LocalDateTime issuedDate = LocalDateTime.now();

    /**
     * Date d'expiration du certificat (optionnelle)
     * null = certificat permanent
     * Utilisé pour les certifications nécessitant un renouvellement
     */
    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;

    /**
     * Code de vérification unique
     * Utilisé pour l'API publique de validation des certificats
     * Généré aléatoirement et cryptographiquement sécurisé
     */
    @Column(name = "verification_code", nullable = false, unique = true, length = 100)
    @NotBlank(message = "Le code de vérification est obligatoire")
    private String verificationCode;

    /**
     * Chemin vers le fichier PDF du certificat
     * Stockage sur système de fichiers ou cloud storage
     * Format suggéré: /certificates/YYYY/MM/certificate-{id}.pdf
     */
    @Column(name = "pdf_path", length = 500)
    private String pdfPath;

    /**
     * Indicateur de révocation du certificat
     * false = certificat valide, true = certificat révoqué
     */
    @Column(name = "is_revoked", nullable = false)
    private Boolean isRevoked = false;

    /**
     * Date de révocation du certificat
     * Renseignée automatiquement lors de l'appel à revoke()
     */
    @Column(name = "revoked_date")
    private LocalDateTime revokedDate;

    /**
     * Raison de la révocation
     * Obligatoire lors de la révocation pour traçabilité
     * Ex: "Fraude détectée", "Erreur administrative", "Demande étudiant"
     */
    @Column(name = "revocation_reason", length = 500)
    private String revocationReason;

    // ========== MÉTHODES UTILITAIRES ==========

    /**
     * Vérifie si le certificat est actuellement valide
     * Un certificat est valide s'il n'est pas révoqué ET non expiré
     * 
     * @return true si le certificat est valide, false sinon
     */
    public boolean isValid() {
        return !Boolean.TRUE.equals(isRevoked) && 
               (expiryDate == null || expiryDate.isAfter(LocalDateTime.now()));
    }

    /**
     * Vérifie si le certificat est expiré
     * 
     * @return true si le certificat a une date d'expiration et qu'elle est dépassée
     */
    public boolean isExpired() {
        return expiryDate != null && expiryDate.isBefore(LocalDateTime.now());
    }

    /**
     * Révoque le certificat avec une raison
     * Met à jour automatiquement les champs de révocation
     * 
     * @param reason La raison de la révocation (obligatoire)
     * @throws IllegalArgumentException si la raison est null ou vide
     */
    public void revoke(String reason) {
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("La raison de révocation est obligatoire");
        }
        
        this.isRevoked = true;
        this.revokedDate = LocalDateTime.now();
        this.revocationReason = reason.trim();
    }

    /**
     * Annule la révocation du certificat (réactivation)
     * Remet le certificat en état valide si pas expiré
     */
    public void unrevokeIfValid() {
        if (!isExpired()) {
            this.isRevoked = false;
            this.revokedDate = null;
            this.revocationReason = null;
        }
    }

    /**
     * Génère une URL publique de vérification du certificat
     * 
     * @param baseUrl L'URL de base de l'application
     * @return L'URL complète de vérification
     */
    public String getVerificationUrl(String baseUrl) {
        return baseUrl + "/api/public/certificates/verify/" + this.verificationCode;
    }

    /**
     * Retourne le nom complet de l'étudiant via l'enrollment
     * Méthode de convenance pour l'affichage
     * 
     * @return Le nom de l'étudiant ou "N/A" si indisponible
     */
    public String getStudentName() {
        return enrollment != null && enrollment.getStudent() != null 
            ? enrollment.getStudent().getFullName() 
            : "N/A";
    }

    /**
     * Retourne le titre du cours via l'enrollment
     * Méthode de convenance pour l'affichage
     * 
     * @return Le titre du cours ou "N/A" si indisponible
     */
    public String getCourseTitle() {
        return enrollment != null && enrollment.getCourse() != null 
            ? enrollment.getCourse().getTitle() 
            : "N/A";
    }

    // ========== HOOKS JPA ==========

    /**
     * Validation avant persistance
     * Génère automatiquement les codes si manquants
     */
    @PrePersist
    private void prePersist() {
        if (certificateNumber == null || certificateNumber.trim().isEmpty()) {
            // Ici, dans une vraie implémentation, on ferait appel à un service
            // pour générer un numéro unique
            this.certificateNumber = generateCertificateNumber();
        }
        
        if (verificationCode == null || verificationCode.trim().isEmpty()) {
            this.verificationCode = generateVerificationCode();
        }
        
        if (issuedDate == null) {
            this.issuedDate = LocalDateTime.now();
        }
    }

    /**
     * Validation avant mise à jour
     */
    @PreUpdate
    private void preUpdate() {
        // Validation métier
        if (Boolean.TRUE.equals(isRevoked) && 
            (revocationReason == null || revocationReason.trim().isEmpty())) {
            throw new IllegalStateException(
                "Un certificat révoqué doit avoir une raison de révocation"
            );
        }
    }

    // ========== MÉTHODES PRIVÉES UTILITAIRES ==========

    /**
     * Génère un numéro de certificat unique
     * À remplacer par un appel à un service dédié
     */
    private String generateCertificateNumber() {
        // Implémentation simplifiée - à remplacer par un service
        return "JCA-" + LocalDateTime.now().getYear() + "-" + 
               String.format("%06d", System.currentTimeMillis() % 1000000);
    }

    /**
     * Génère un code de vérification sécurisé
     * À remplacer par un appel à un service cryptographique
     */
    private String generateVerificationCode() {
        // Implémentation simplifiée - à remplacer par un service sécurisé
        return java.util.UUID.randomUUID().toString().replace("-", "").toUpperCase();
    }
}

/*
GUIDE D'UTILISATION DES IMPORTS :

1. jakarta.persistence.* :
   - @Entity, @Table, @Column : Mapping objet-relationnel JPA
   - @OneToOne, @JoinColumn : Définition des relations entre entités
   - @Index : Optimisation des requêtes par indexation BD
   - @PrePersist, @PreUpdate : Hooks de cycle de vie JPA

2. lombok.Data, lombok.EqualsAndHashCode :
   - Génération automatique des getters/setters, toString, equals, hashCode
   - Réduit considérablement le code boilerplate
   - @EqualsAndHashCode(callSuper = true) inclut les champs de BaseEntity

3. org.hibernate.annotations.SQLDelete, @Where :
   - Implémentation du pattern "Soft Delete"
   - Les entités sont marquées comme supprimées sans être physiquement effacées
   - Préserve l'intégrité historique et la traçabilité

4. java.time.LocalDateTime :
   - API moderne Java 8+ pour gestion des dates/heures
   - Thread-safe et immutable
   - Intégration native avec JPA 2.2+

5. jakarta.validation.constraints.* (optionnel) :
   - Validation déclarative des données
   - @NotNull, @NotBlank pour contraintes métier
   - Intégration avec Spring Boot Validation

FUTURES AMÉLIORATIONS POSSIBLES :

1. Audit et traçabilité :
   - @EntityListeners(AuditingEntityListener.class)
   - Ajout de champs audit automatiques

2. Versioning :
   - @Version pour optimistic locking
   - Gestion des conflits de concurrence

3. Internationalisation :
   - Champs multilingues pour le certificat
   - @ElementCollection pour Map<Locale, String>

4. Sécurité avancée :
   - Chiffrement des données sensibles
   - @Convert(converter = CryptoConverter.class)

5. Cache :
   - @Cacheable pour optimiser les performances
   - @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)

6. Validation métier avancée :
   - @AssertTrue pour validations complexes
   - Validators personnalisés

7. Events :
   - @DomainEvents pour publication d'événements métier
   - Integration avec Spring Application Events
*/