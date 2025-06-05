package com.javacraftacademy.courseservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Version;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Classe abstraite de base pour toutes les entités de l'application JavaCraftAcademy.
 *
 * <p>
 * Cette classe fournit des champs communs pour l'audit, le suivi des états actifs/supprimés, 
 * la gestion des versions et l'identifiant unique. Toutes les entités métier doivent hériter 
 * de cette classe afin d'assurer une cohérence de structure et une auditabilité automatique.
 * </p>
 *
 * <h3>Fonctionnalités principales :</h3>
 * <ul>
 *   <li>Audit automatique : création, modification, utilisateurs associés.</li>
 *   <li>Versioning : gestion optimiste des modifications concurrentes.</li>
 *   <li>Statut logique (soft delete) via <code>isDeleted</code> et <code>isActive</code>.</li>
 *   <li>Méthodes utilitaires : suppression/restauration/logique, audit info, etc.</li>
 * </ul>
 *
 * <h3>Relations avec l'application JavaCraftAcademy :</h3>
 * <ul>
 *   <li>Chaque entité du service cours (Course, Module, Chapter, etc.) hérite de <code>BaseEntity</code>.</li>
 *   <li>Le système d’audit est basé sur Spring Data JPA Auditing et Hibernate.</li>
 *   <li>Assure une traçabilité globale dans l’ensemble du domaine métier.</li>
 * </ul>
 *
 * <h3>Extensions futures possibles :</h3>
 * <ul>
 *   <li>Ajouter un champ <code>organizationId</code> pour multi-tenancy.</li>
 *   <li>Ajouter une logique d’archivage automatique basée sur la date de suppression.</li>
 *   <li>Ajouter un champ <code>updatedFromIp</code> pour sécurité renforcée.</li>
 * </ul>
 *
 * @author 
 * @since 1.0
 */
@MappedSuperclass
@Data
@EqualsAndHashCode(of = "id")
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Identifiant primaire auto-généré. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    /** Date/heure de création. Gérée automatiquement par Hibernate. */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Date/heure de dernière modification. Mise à jour automatique. */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /** Utilisateur ayant créé l'entité. */
    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private String createdBy;

    /** Utilisateur ayant effectué la dernière modification. */
    @LastModifiedBy
    @Column(name = "updated_by")
    private String updatedBy;

    /** Version pour la gestion optimiste des accès concurrents. */
    @Version
    @Column(name = "version")
    private Long version;

    /** Indique si l'entité est active. */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    /** Indique si l'entité est supprimée logiquement. */
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    /** Date/heure de suppression logique. */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    /** Utilisateur ayant supprimé l'entité. */
    @Column(name = "deleted_by")
    private String deletedBy;

    /**
     * Initialisation automatique lors de la création.
     */
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (updatedAt == null) updatedAt = LocalDateTime.now();
        if (isActive == null) isActive = true;
        if (isDeleted == null) isDeleted = false;
    }

    /**
     * Mise à jour automatique de la date lors d'une modification.
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Vérifie si l'entité est nouvelle (non persistée).
     */
    public boolean isNew() {
        return id == null;
    }

    /**
     * Vérifie si l'entité est persistée.
     */
    public boolean isPersisted() {
        return id != null;
    }

    /**
     * Marque l'entité comme supprimée logiquement.
     */
    public void markAsDeleted() {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
        this.isActive = false;
    }

    /**
     * Marque l'entité comme supprimée avec un utilisateur.
     */
    public void markAsDeleted(String deletedBy) {
        markAsDeleted();
        this.deletedBy = deletedBy;
    }

    /**
     * Restaure une entité supprimée.
     */
    public void restore() {
        this.isDeleted = false;
        this.deletedAt = null;
        this.deletedBy = null;
        this.isActive = true;
    }

    /**
     * Active l'entité.
     */
    public void activate() {
        this.isActive = true;
    }

    /**
     * Désactive l'entité.
     */
    public void deactivate() {
        this.isActive = false;
    }

    /**
     * Vérifie si l'entité est supprimée.
     */
    public boolean isDeleted() {
        return Boolean.TRUE.equals(isDeleted);
    }

    /**
     * Vérifie si l'entité est active.
     */
    public boolean isActive() {
        return Boolean.TRUE.equals(isActive);
    }

    /**
     * Retourne un résumé des informations d'audit.
     */
    public String getAuditInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("Created: ").append(createdAt);
        if (createdBy != null) {
            sb.append(" by ").append(createdBy);
        }
        if (updatedAt != null && !updatedAt.equals(createdAt)) {
            sb.append(", Last updated: ").append(updatedAt);
            if (updatedBy != null) {
                sb.append(" by ").append(updatedBy);
            }
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return String.format("%s{id=%d, createdAt=%s, updatedAt=%s, isActive=%s, isDeleted=%s}", 
            getClass().getSimpleName(), id, createdAt, updatedAt, isActive, isDeleted);
    }
}
