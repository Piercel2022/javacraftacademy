package com.javacraftacademy.courseservice.model.entity;

import com.javacraftacademy.courseservice.model.enums.EnrollmentStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entité JPA représentant une inscription d'un utilisateur à un cours dans JavaCraftAcademy.
 * 
 * <p>Cette classe centrale gère le lien entre les utilisateurs et les cours, permettant
 * de suivre l'état d'avancement, les paiements, et les métriques de performance de chaque
 * apprenant. Elle constitue le cœur du système de gestion des inscriptions et du suivi
 * pédagogique de la plateforme.</p>
 * 
 * <h3>Fonctionnalités principales :</h3>
 * <ul>
 *   <li>Gestion du cycle de vie des inscriptions (PENDING → ACTIVE → COMPLETED/CANCELLED)</li>
 *   <li>Suivi de progression avec pourcentage de complétion</li>
 *   <li>Gestion des paiements et des prix d'inscription</li>
 *   <li>Métriques de performance (note finale, temps passé)</li>
 *   <li>Gestion des certificats et des accomplissements</li>
 *   <li>Audit complet avec historique des modifications</li>
 * </ul>
 * 
 * <h3>Intégration dans l'écosystème JavaCraftAcademy :</h3>
 * <ul>
 *   <li><strong>User Service</strong> : Référence utilisateur via userId (microservice)</li>
 *   <li><strong>Course</strong> : Relation Many-to-One avec les cours</li>
 *   <li><strong>Payment Service</strong> : Intégration via Kafka pour les paiements</li>
 *   <li><strong>Notification Service</strong> : Alertes sur changements de statut</li>
 *   <li><strong>Analytics Service</strong> : Données pour tableaux de bord et rapports</li>
 *   <li><strong>Certificate Service</strong> : Génération automatique à la complétion</li>
 * </ul>
 * 
 * <h3>Flux de données typique :</h3>
 * <ol>
 *   <li>Création avec statut PENDING après sélection du cours</li>
 *   <li>Passage à ACTIVE après validation du paiement (via Kafka)</li>
 *   <li>Mise à jour du progrès pendant l'apprentissage</li>
 *   <li>Finalisation avec COMPLETED et génération du certificat</li>
 * </ol>
 * 
 * <h3>Extensions futures possibles :</h3>
 * <ul>
 *   <li>Support des inscriptions de groupe/entreprise</li>
 *   <li>Système de parrainage et codes promotionnels</li>
 *   <li>Intégration avec calendrier et rappels automatiques</li>
 *   <li>Gamification avec points et badges</li>
 *   <li>Analyse prédictive du risque d'abandon</li>
 *   <li>Support multidevice avec synchronisation de progression</li>
 * </ul>
 * 
 * @author JavaCraftAcademy Team
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(name = "enrollments", 
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_user_course", columnNames = {"user_id", "course_id"})
       },
       indexes = {
           @Index(name = "idx_enrollment_user", columnList = "user_id"),
           @Index(name = "idx_enrollment_course", columnList = "course_id"),
           @Index(name = "idx_enrollment_status", columnList = "status"),
           @Index(name = "idx_enrollment_created", columnList = "created_at"),
           @Index(name = "idx_enrollment_completion", columnList = "completion_percentage")
       })
public class Enrollment {

    /**
     * Identifiant unique de l'inscription.
     * Clé primaire auto-générée pour traçabilité complète.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Identifiant de l'utilisateur inscrit.
     * Référence vers le User Service (architecture microservices).
     * Permet de maintenir la séparation des concerns entre services.
     */
    @NotNull(message = "L'identifiant utilisateur est obligatoire")
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * Cours auquel l'utilisateur est inscrit.
     * Relation Many-to-One - plusieurs inscriptions par cours.
     * FetchType.LAZY pour optimiser les performances.
     */
    @NotNull(message = "Le cours est obligatoire")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    /**
     * Statut actuel de l'inscription.
     * Définit l'état dans le cycle de vie de l'inscription.
     * Utilise un enum pour garantir la cohérence des valeurs.
     */
    @NotNull(message = "Le statut de l'inscription est obligatoire")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private EnrollmentStatus status = EnrollmentStatus.PENDING;

    /**
     * Pourcentage de progression dans le cours (0-100).
     * Calculé automatiquement basé sur les leçons complétées.
     * Utilisé pour l'affichage de la barre de progression.
     */
    @Min(value = 0, message = "Le pourcentage de progression ne peut pas être négatif")
    @Max(value = 100, message = "Le pourcentage de progression ne peut pas dépasser 100")
    @Column(name = "completion_percentage", columnDefinition = "INTEGER DEFAULT 0")
    private Integer completionPercentage = 0;

    /**
     * Prix payé pour cette inscription.
     * Peut différer du prix actuel du cours (promotions, réductions).
     * Utilise BigDecimal pour la précision monétaire.
     */
    @DecimalMin(value = "0.0", message = "Le prix ne peut pas être négatif")
    @Column(name = "enrollment_price", precision = 10, scale = 2)
    private BigDecimal enrollmentPrice;

    /**
     * Devise utilisée pour le paiement.
     * Support international avec différentes devises.
     * Code ISO 4217 standard (EUR, USD, etc.).
     */
    @Column(name = "currency", length = 3, columnDefinition = "VARCHAR(3) DEFAULT 'EUR'")
    private String currency = "EUR";

    /**
     * Date de début d'accès au cours.
     * Permet de gérer les inscriptions avec démarrage différé.
     * Null = accès immédiat après activation.
     */
    @Column(name = "start_date")
    private LocalDateTime startDate;

    /**
     * Date de fin d'accès au cours.
     * Pour les cours avec durée limitée ou abonnements.
     * Null = accès illimité.
     */
    @Column(name = "end_date")
    private LocalDateTime endDate;

    /**
     * Date de complétion du cours.
     * Automatiquement définie quand completionPercentage atteint 100%.
     * Utilisée pour la génération des certificats.
     */
    @Column(name = "completion_date")
    private LocalDateTime completionDate;

    /**
     * Note finale obtenue dans le cours (0-20).
     * Calculée à partir des évaluations et quiz.
     * Peut être null si le cours n'inclut pas d'évaluations.
     */
    @DecimalMin(value = "0.0", message = "La note ne peut pas être négative")
    @Column(name = "final_grade", precision = 4, scale = 2)
    private BigDecimal finalGrade;

    /**
     * Temps total passé sur le cours en minutes.
     * Calculé à partir des sessions d'apprentissage.
     * Utilisé pour les statistiques et analytics.
     */
    @Min(value = 0, message = "Le temps passé ne peut pas être négatif")
    @Column(name = "time_spent_minutes", columnDefinition = "INTEGER DEFAULT 0")
    private Integer timeSpentMinutes = 0;

    /**
     * Identifiant du certificat généré.
     * Référence vers le Certificate Service.
     * Null si pas encore généré ou cours non complété.
     */
    @Column(name = "certificate_id")
    private String certificateId;

    /**
     * Dernière activité enregistrée.
     * Utilisée pour détecter les utilisateurs inactifs.
     * Mise à jour à chaque interaction avec le cours.
     */
    @Column(name = "last_activity_date")
    private LocalDateTime lastActivityDate;

    /**
     * Nombre de connexions au cours.
     * Métrique d'engagement de l'utilisateur.
     * Incrémentée à chaque session.
     */
    @Min(value = 0, message = "Le nombre d'accès ne peut pas être négatif")
    @Column(name = "access_count", columnDefinition = "INTEGER DEFAULT 0")
    private Integer accessCount = 0;

    /**
     * Indique si l'utilisateur a donné son avis sur le cours.
     * Utilisé pour solliciter les évaluations.
     */
    @Column(name = "has_reviewed", columnDefinition = "BOOLEAN DEFAULT false")
    private Boolean hasReviewed = false;

    /**
     * Notes personnelles de l'utilisateur.
     * Champ libre pour annotations et commentaires.
     */
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    /**
     * Identifiant de transaction de paiement.
     * Référence vers le Payment Service pour réconciliation.
     */
    @Column(name = "payment_transaction_id")
    private String paymentTransactionId;

    /**
     * Indicateur de remboursement.
     * true si l'inscription a été remboursée.
     */
    @Column(name = "is_refunded", columnDefinition = "BOOLEAN DEFAULT false")
    private Boolean isRefunded = false;

    /**
     * Date et heure de création de l'inscription.
     * Audit automatique géré par Hibernate.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Date et heure de dernière modification.
     * Audit automatique pour traçabilité des changements.
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Constructeurs

    /**
     * Constructeur par défaut requis par JPA.
     */
    public Enrollment() {}

    /**
     * Constructeur pour création d'inscription simple.
     * 
     * @param userId L'identifiant de l'utilisateur
     * @param course Le cours à associer
     */
    public Enrollment(Long userId, Course course) {
        this.userId = userId;
        this.course = course;
        this.status = EnrollmentStatus.PENDING;
        this.lastActivityDate = LocalDateTime.now();
    }

    /**
     * Constructeur complet avec prix.
     * 
     * @param userId L'identifiant de l'utilisateur
     * @param course Le cours à associer
     * @param enrollmentPrice Le prix d'inscription
     * @param currency La devise
     */
    public Enrollment(Long userId, Course course, BigDecimal enrollmentPrice, String currency) {
        this(userId, course);
        this.enrollmentPrice = enrollmentPrice;
        this.currency = currency;
    }

    // Méthodes métier

    /**
     * Active l'inscription après validation du paiement.
     * Transition PENDING → ACTIVE.
     */
    public void activate() {
        if (this.status == EnrollmentStatus.PENDING) {
            this.status = EnrollmentStatus.ACTIVE;
            this.startDate = LocalDateTime.now();
            this.lastActivityDate = LocalDateTime.now();
        }
    }

    /**
     * Marque l'inscription comme complétée.
     * Déclenche la génération du certificat.
     */
    public void complete() {
        if (this.status == EnrollmentStatus.ACTIVE) {
            this.status = EnrollmentStatus.COMPLETED;
            this.completionDate = LocalDateTime.now();
            this.completionPercentage = 100;
            this.lastActivityDate = LocalDateTime.now();
        }
    }

    /**
     * Suspend l'inscription (violation de règles, non-paiement).
     */
    public void suspend() {
        if (this.status == EnrollmentStatus.ACTIVE) {
            this.status = EnrollmentStatus.SUSPENDED;
            this.lastActivityDate = LocalDateTime.now();
        }
    }

    /**
     * Annule l'inscription.
     * État final - pas de retour possible.
     */
    public void cancel() {
        this.status = EnrollmentStatus.CANCELLED;
        this.lastActivityDate = LocalDateTime.now();
    }

    /**
     * Met à jour la progression dans le cours.
     * 
     * @param newPercentage Le nouveau pourcentage de progression
     */
    public void updateProgress(Integer newPercentage) {
        if (newPercentage != null && newPercentage >= 0 && newPercentage <= 100) {
            this.completionPercentage = newPercentage;
            this.lastActivityDate = LocalDateTime.now();
            
            // Auto-complétion si 100%
            if (newPercentage == 100 && this.status == EnrollmentStatus.ACTIVE) {
                complete();
            }
        }
    }

    /**
     * Enregistre une session d'apprentissage.
     * 
     * @param durationMinutes Durée de la session en minutes
     */
    public void recordLearningSession(Integer durationMinutes) {
        if (durationMinutes != null && durationMinutes > 0) {
            this.timeSpentMinutes += durationMinutes;
            this.accessCount++;
            this.lastActivityDate = LocalDateTime.now();
        }
    }

    /**
     * Vérifie si l'inscription est active.
     * 
     * @return true si l'inscription permet l'accès au cours
     */
    public boolean isActive() {
        return this.status == EnrollmentStatus.ACTIVE && 
               (this.endDate == null || this.endDate.isAfter(LocalDateTime.now()));
    }

    /**
     * Vérifie si l'inscription est complétée avec succès.
     * 
     * @return true si le cours est terminé
     */
    public boolean isCompleted() {
        return this.status == EnrollmentStatus.COMPLETED && 
               this.completionPercentage == 100;
    }

    /**
     * Calcule le temps d'apprentissage moyen par session.
     * 
     * @return Temps moyen en minutes, ou 0 si pas de sessions
     */
    public Double getAverageSessionDuration() {
        return this.accessCount > 0 ? 
               (double) this.timeSpentMinutes / this.accessCount : 0.0;
    }

    /**
     * Vérifie si l'utilisateur est inactif.
     * 
     * @param daysThreshold Nombre de jours sans activité
     * @return true si l'utilisateur est considéré comme inactif
     */
    public boolean isInactive(int daysThreshold) {
        return this.lastActivityDate != null && 
               this.lastActivityDate.isBefore(LocalDateTime.now().minusDays(daysThreshold));
    }

    /**
     * Génère un résumé de l'inscription pour les rapports.
     * 
     * @return Résumé formaté de l'inscription
     */
    public String getSummary() {
        return String.format("Inscription #%d - Utilisateur %d - Cours: %s - Statut: %s - Progression: %d%%",
                this.id, this.userId, 
                this.course != null ? this.course.getTitle() : "N/A",
                this.status, this.completionPercentage);
    }

    // Getters et Setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }

    public EnrollmentStatus getStatus() { return status; }
    public void setStatus(EnrollmentStatus status) { this.status = status; }

    public Integer getCompletionPercentage() { return completionPercentage; }
    public void setCompletionPercentage(Integer completionPercentage) { 
        this.completionPercentage = completionPercentage; 
    }

    public BigDecimal getEnrollmentPrice() { return enrollmentPrice; }
    public void setEnrollmentPrice(BigDecimal enrollmentPrice) { 
        this.enrollmentPrice = enrollmentPrice; 
    }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }

    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }

    public LocalDateTime getCompletionDate() { return completionDate; }
    public void setCompletionDate(LocalDateTime completionDate) { 
        this.completionDate = completionDate; 
    }

    public BigDecimal getFinalGrade() { return finalGrade; }
    public void setFinalGrade(BigDecimal finalGrade) { this.finalGrade = finalGrade; }

    public Integer getTimeSpentMinutes() { return timeSpentMinutes; }
    public void setTimeSpentMinutes(Integer timeSpentMinutes) { 
        this.timeSpentMinutes = timeSpentMinutes; 
    }

    public String getCertificateId() { return certificateId; }
    public void setCertificateId(String certificateId) { this.certificateId = certificateId; }

    public LocalDateTime getLastActivityDate() { return lastActivityDate; }
    public void setLastActivityDate(LocalDateTime lastActivityDate) { 
        this.lastActivityDate = lastActivityDate; 
    }

    public Integer getAccessCount() { return accessCount; }
    public void setAccessCount(Integer accessCount) { this.accessCount = accessCount; }

    public Boolean getHasReviewed() { return hasReviewed; }
    public void setHasReviewed(Boolean hasReviewed) { this.hasReviewed = hasReviewed; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getPaymentTransactionId() { return paymentTransactionId; }
    public void setPaymentTransactionId(String paymentTransactionId) { 
        this.paymentTransactionId = paymentTransactionId; 
    }

    public Boolean getIsRefunded() { return isRefunded; }
    public void setIsRefunded(Boolean isRefunded) { this.isRefunded = isRefunded; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // equals, hashCode et toString

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Enrollment that = (Enrollment) o;
        return Objects.equals(id, that.id) && 
               Objects.equals(userId, that.userId) && 
               Objects.equals(course, that.course);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId, course);
    }

    @Override
    public String toString() {
        return "Enrollment{" +
                "id=" + id +
                ", userId=" + userId +
                ", courseId=" + (course != null ? course.getId() : null) +
                ", status=" + status +
                ", completionPercentage=" + completionPercentage +
                ", enrollmentPrice=" + enrollmentPrice +
                ", currency='" + currency + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", completionDate=" + completionDate +
                ", finalGrade=" + finalGrade +
                ", timeSpentMinutes=" + timeSpentMinutes +
                ", certificateId='" + certificateId + '\'' +
                ", lastActivityDate=" + lastActivityDate +
                ", accessCount=" + accessCount +
                ", hasReviewed=" + hasReviewed +
                ", isRefunded=" + isRefunded +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}