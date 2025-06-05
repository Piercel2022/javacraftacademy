package com.javacraftacademy.courseservice.entity;

import java.util.Arrays;
import java.util.Optional;

/**
 * Représente les différents statuts possibles d'une inscription à un cours.
 *
 * <p>Chaque statut a une signification précise dans le cycle de vie d’un étudiant inscrit :</p>
 * <ul>
 *   <li>{@code ACTIVE} : L'étudiant suit activement le cours.</li>
 *   <li>{@code COMPLETED} : Le cours a été terminé avec succès.</li>
 *   <li>{@code DROPPED} : L'étudiant a abandonné le cours volontairement.</li>
 *   <li>{@code SUSPENDED} : L’accès a été suspendu (raison administrative, technique, etc.).</li>
 *   <li>{@code PENDING} : En attente de validation (paiement, approbation, etc.).</li>
 *   <li>{@code EXPIRED} : Délai expiré, le cours n’est plus accessible.</li>
 * </ul>
 *
 * <p>Chaque statut est associé à une étiquette lisible en français (via {@link #getDisplayName()}).</p>
 *
 * <p>Fonctionnalités supplémentaires :</p>
 * <ul>
 *   <li>Méthode {@code fromDisplayName()} pour retrouver un statut via son libellé affiché</li>
 *   <li>Support pour future internationalisation (i18n)</li>
 * </ul>
 *
 * @author JavaCraft
 * @since 1.0
 */
public enum EnrollmentStatus {

    /** L'étudiant suit activement le cours */
    ACTIVE("Actif"),

    /** Le cours a été terminé avec succès */
    COMPLETED("Terminé"),

    /** L'étudiant a abandonné le cours */
    DROPPED("Abandonné"),

    /** L'accès a été suspendu temporairement ou définitivement */
    SUSPENDED("Suspendu"),

    /** En attente de validation (paiement, approbation, etc.) */
    PENDING("En attente"),

    /** Le cours n’est plus accessible (délai dépassé ou contrat terminé) */
    EXPIRED("Expiré");

    /**
     * Libellé affichable, utilisé dans l’interface utilisateur ou les exports.
     */
    private final String displayName;

    EnrollmentStatus(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Retourne l’étiquette en français associée à ce statut.
     *
     * @return Libellé affichable (ex: "Terminé", "Actif")
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Permet de retrouver un statut à partir de son libellé affiché.
     * Utile pour des conversions dans les formulaires ou fichiers CSV.
     *
     * @param displayName Le nom affiché du statut (sensible à la casse)
     * @return Un {@link Optional} du statut correspondant
     */
    public static Optional<EnrollmentStatus> fromDisplayName(String displayName) {
        return Arrays.stream(values())
                .filter(status -> status.displayName.equalsIgnoreCase(displayName))
                .findFirst();
    }

    /**
     * Vérifie si le statut est considéré comme terminal (cours terminé ou quitté).
     *
     * @return true si le statut est COMPLETED, DROPPED ou EXPIRED
     */
    public boolean isFinalStatus() {
        return this == COMPLETED || this == DROPPED || this == EXPIRED;
    }
}
