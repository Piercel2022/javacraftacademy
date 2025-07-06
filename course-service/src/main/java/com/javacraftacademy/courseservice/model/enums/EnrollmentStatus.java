package com.javacraftacademy.courseservice.model.enums;

/**
 * Énumération représentant les différents statuts d'inscription d'un étudiant à un cours.
 * 
 * Cette énumération définit les états possibles d'une inscription tout au long de son cycle de vie,
 * depuis la demande initiale jusqu'à l'achèvement ou l'annulation du cours.
 * 
 * États du cycle de vie d'une inscription :
 * 1. PENDING - Inscription en attente de validation/paiement
 * 2. ACTIVE - Inscription active, étudiant peut accéder au cours
 * 3. COMPLETED - Cours terminé avec succès
 * 4. CANCELLED - Inscription annulée par l'étudiant ou l'administrateur
 * 5. EXPIRED - Inscription expirée (dépassement de la date limite)
 * 6. SUSPENDED - Inscription suspendue temporairement
 * 7. REFUNDED - Inscription remboursée et annulée
 * 
 * Relations avec l'application :
 * - Utilisée par l'entité Enrollment pour suivre l'état des inscriptions
 * - Référencée dans EnrollmentService pour la logique métier des transitions d'état
 * - Utilisée dans les contrôleurs pour filtrer les inscriptions par statut
 * - Intégrée dans les événements Kafka pour notifier les changements d'état
 * - Référencée dans les rapports et analytics pour les statistiques d'inscription
 * 
 * @author JavaCraft Academy
 * @version 1.0
 * @since 1.0
 */
public enum EnrollmentStatus {
    
    /**
     * Inscription en attente de validation ou de paiement.
     * 
     * Cet état indique que :
     * - L'étudiant a soumis une demande d'inscription
     * - Le paiement n'a pas encore été confirmé
     * - L'inscription nécessite une validation manuelle
     * - L'accès au cours n'est pas encore autorisé
     * 
     * Transitions possibles :
     * - Vers ACTIVE après confirmation du paiement
     * - Vers CANCELLED en cas d'annulation
     * - Vers EXPIRED si la période d'attente expire
     */
    PENDING("En attente", "Inscription en attente de validation ou de paiement"),
    
    /**
     * Inscription active et opérationnelle.
     * 
     * Cet état indique que :
     * - Le paiement a été confirmé
     * - L'étudiant a accès complet au cours
     * - Toutes les fonctionnalités sont disponibles
     * - L'étudiant peut progresser dans le cours
     * 
     * Transitions possibles :
     * - Vers COMPLETED à la fin du cours
     * - Vers CANCELLED en cas d'annulation
     * - Vers SUSPENDED en cas de suspension
     * - Vers REFUNDED en cas de remboursement
     */
    ACTIVE("Active", "Inscription active avec accès complet au cours"),
    
    /**
     * Cours terminé avec succès.
     * 
     * Cet état indique que :
     * - L'étudiant a terminé tous les modules requis
     * - Les évaluations ont été complétées
     * - Un certificat peut être généré
     * - L'inscription est considérée comme réussie
     * 
     * État final - aucune transition possible.
     */
    COMPLETED("Terminé", "Cours terminé avec succès"),
    
    /**
     * Inscription annulée.
     * 
     * Cet état indique que :
     * - L'inscription a été annulée par l'étudiant ou l'administrateur
     * - L'accès au cours est révoqué
     * - Aucun remboursement n'est impliqué
     * - Les données de progression sont conservées pour référence
     * 
     * État final - aucune transition possible.
     */
    CANCELLED("Annulée", "Inscription annulée par l'étudiant ou l'administrateur"),
    
    /**
     * Inscription expirée.
     * 
     * Cet état indique que :
     * - La période d'inscription ou d'accès a expiré
     * - L'étudiant n'a plus accès au cours
     * - L'expiration s'est produite automatiquement
     * - Une réactivation peut être possible selon la politique
     * 
     * Transitions possibles :
     * - Vers ACTIVE en cas de réactivation (selon politique)
     */
    EXPIRED("Expirée", "Inscription expirée, accès révoqué"),
    
    /**
     * Inscription suspendue temporairement.
     * 
     * Cet état indique que :
     * - L'accès au cours est temporairement suspendu
     * - La suspension peut être due à un problème de paiement
     * - L'inscription peut être réactivée
     * - Les données de progression sont préservées
     * 
     * Transitions possibles :
     * - Vers ACTIVE après résolution du problème
     * - Vers CANCELLED en cas d'annulation définitive
     */
    SUSPENDED("Suspendue", "Inscription suspendue temporairement"),
    
    /**
     * Inscription remboursée et annulée.
     * 
     * Cet état indique que :
     * - Un remboursement a été effectué
     * - L'inscription est définitivement annulée
     * - L'accès au cours est révoqué
     * - Le processus de remboursement est terminé
     * 
     * État final - aucune transition possible.
     */
    REFUNDED("Remboursée", "Inscription remboursée et annulée");
    
    /**
     * Libellé court du statut pour l'affichage utilisateur.
     */
    private final String label;
    
    /**
     * Description détaillée du statut.
     */
    private final String description;
    
    /**
     * Constructeur de l'énumération EnrollmentStatus.
     * 
     * @param label Libellé court du statut
     * @param description Description détaillée du statut
     */
    EnrollmentStatus(String label, String description) {
        this.label = label;
        this.description = description;
    }
    
    /**
     * Retourne le libellé court du statut.
     * 
     * @return Le libellé du statut
     */
    public String getLabel() {
        return label;
    }
    
    /**
     * Retourne la description détaillée du statut.
     * 
     * @return La description du statut
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Vérifie si le statut représente une inscription active.
     * 
     * @return true si l'inscription est active, false sinon
     */
    public boolean isActive() {
        return this == ACTIVE;
    }
    
    /**
     * Vérifie si le statut représente une inscription terminée.
     * 
     * @return true si l'inscription est terminée, false sinon
     */
    public boolean isCompleted() {
        return this == COMPLETED;
    }
    
    /**
     * Vérifie si le statut représente une inscription en attente.
     * 
     * @return true si l'inscription est en attente, false sinon
     */
    public boolean isPending() {
        return this == PENDING;
    }
    
    /**
     * Vérifie si le statut représente une inscription annulée ou remboursée.
     * 
     * @return true si l'inscription est annulée ou remboursée, false sinon
     */
    public boolean isCancelled() {
        return this == CANCELLED || this == REFUNDED;
    }
    
    /**
     * Vérifie si le statut permet l'accès au cours.
     * 
     * @return true si l'accès au cours est autorisé, false sinon
     */
    public boolean allowsCourseAccess() {
        return this == ACTIVE || this == COMPLETED;
    }
    
    /**
     * Vérifie si le statut peut être modifié (non final).
     * 
     * @return true si le statut peut évoluer, false si c'est un état final
     */
    public boolean isModifiable() {
        return this != COMPLETED && this != CANCELLED && this != REFUNDED;
    }
    
    /**
     * Retourne les statuts valides pour une transition depuis le statut actuel.
     * 
     * @return Un tableau des statuts de transition possibles
     */
    public EnrollmentStatus[] getValidTransitions() {
        switch (this) {
            case PENDING:
                return new EnrollmentStatus[]{ACTIVE, CANCELLED, EXPIRED};
            case ACTIVE:
                return new EnrollmentStatus[]{COMPLETED, CANCELLED, SUSPENDED, REFUNDED};
            case SUSPENDED:
                return new EnrollmentStatus[]{ACTIVE, CANCELLED};
            case EXPIRED:
                return new EnrollmentStatus[]{ACTIVE}; // Selon politique de réactivation
            case COMPLETED:
            case CANCELLED:
            case REFUNDED:
            default:
                return new EnrollmentStatus[0]; // États finaux
        }
    }
    
    /**
     * Vérifie si une transition vers un autre statut est valide.
     * 
     * @param targetStatus Le statut cible de la transition
     * @return true si la transition est valide, false sinon
     */
    public boolean canTransitionTo(EnrollmentStatus targetStatus) {
        if (targetStatus == null) {
            return false;
        }
        
        EnrollmentStatus[] validTransitions = getValidTransitions();
        for (EnrollmentStatus validStatus : validTransitions) {
            if (validStatus == targetStatus) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Retourne une représentation textuelle du statut avec son libellé.
     * 
     * @return Le libellé du statut
     */
    @Override
    public String toString() {
        return label;
    }
}