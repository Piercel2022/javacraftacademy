// LessonType.java - Enum pour les types de leçons
package com.javacraftacademy.courseservice.entity;

public enum LessonType {
    TEXT("Texte"),
    VIDEO("Vidéo"),
    INTERACTIVE("Interactif"),
    AUDIO("Audio"),
    DOCUMENT("Document"),
    QUIZ("Quiz"),
    EXERCISE("Exercice");
    
    private final String displayName;
    
    LessonType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}