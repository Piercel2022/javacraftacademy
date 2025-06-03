// AssignmentType.java - Enum pour les types d'assignments
package com.javacraftacademy.courseservice.entity;

public enum AssignmentType {
    PROJECT("Projet"),
    ESSAY("Dissertation"),
    CODE_CHALLENGE("Défi de Code"),
    PRESENTATION("Présentation"),
    RESEARCH("Recherche"),
    LAB("Laboratoire"),
    HOMEWORK("Devoir"),
    EXAM("Examen");
    
    private final String displayName;
    
    AssignmentType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}