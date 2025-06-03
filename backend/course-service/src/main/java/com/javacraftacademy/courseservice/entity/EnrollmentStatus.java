// EnrollmentStatus.java - Enum pour les statuts d'inscription
package com.javacraftacademy.courseservice.entity;

public enum EnrollmentStatus {
    ACTIVE("Actif"),
    COMPLETED("Terminé"),
    DROPPED("Abandonné"),
    SUSPENDED("Suspendu"),
    PENDING("En attente"),
    EXPIRED("Expiré");
    
    private final String displayName;
    
    EnrollmentStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
