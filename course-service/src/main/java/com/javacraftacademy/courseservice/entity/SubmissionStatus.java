// SubmissionStatus.java - Enum pour les statuts de soumission
package com.javacraftacademy.courseservice.entity;

public enum SubmissionStatus {
    DRAFT("Brouillon"),
    SUBMITTED("Soumis"),
    UNDER_REVIEW("En cours de révision"),
    GRADED("Noté"),
    RETURNED("Retourné"),
    LATE("En retard");
    
    private final String displayName;
    
    SubmissionStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}