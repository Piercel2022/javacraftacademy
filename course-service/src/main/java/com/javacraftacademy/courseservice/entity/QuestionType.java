package com.javacraftacademy.courseservice.entity;

/**
 * Enumeration representing the different types of questions available in the system
 * Each type defines how the question should be presented and evaluated
 */
public enum QuestionType {
    
    /**
     * Multiple choice question with several options where one or more can be correct
     * Uses the 'options' and 'correctAnswers' fields from Question entity
     */
    MULTIPLE_CHOICE("Multiple Choice", "Question with multiple options where one or more answers can be selected"),
    
    /**
     * True/False question with only two possible answers
     * Uses the 'correctAnswer' field to store "true" or "false"
     */
    TRUE_FALSE("True/False", "Question with only True or False as possible answers"),
    
    /**
     * Open-ended question requiring a text response
     * Uses the 'correctAnswer' field for reference answer (optional for manual grading)
     */
    OPEN_ENDED("Open Ended", "Question requiring a free-text response"),
    
    /**
     * Single choice question with multiple options but only one correct answer
     * Uses the 'options' field for choices and 'correctAnswer' for the single correct option
     */
    SINGLE_CHOICE("Single Choice", "Question with multiple options but only one correct answer"),
    
    /**
     * Fill in the blank question where students complete missing parts
     * Uses the 'correctAnswer' or 'correctAnswers' for expected responses
     */
    FILL_IN_THE_BLANK("Fill in the Blank", "Question with missing parts to be completed"),
    
    /**
     * Matching question where students pair items from two lists
     * Uses the 'options' and 'correctAnswers' fields to define pairs
     */
    MATCHING("Matching", "Question requiring pairing of related items"),
    
    /**
     * Ordering/Sequencing question where items must be arranged in correct order
     * Uses the 'options' for items and 'correctAnswers' for correct sequence
     */
    ORDERING("Ordering", "Question requiring arrangement of items in correct sequence");

    private final String displayName;
    private final String description;

    QuestionType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Check if this question type supports multiple correct answers
     * @return true if multiple answers are possible
     */
    public boolean supportsMultipleAnswers() {
        return this == MULTIPLE_CHOICE || this == MATCHING || this == FILL_IN_THE_BLANK;
    }

    /**
     * Check if this question type requires predefined options
     * @return true if options are needed
     */
    public boolean requiresOptions() {
        return this == MULTIPLE_CHOICE || this == SINGLE_CHOICE || 
               this == MATCHING || this == ORDERING;
    }

    /**
     * Check if this question type can be auto-graded
     * @return true if automatic grading is possible
     */
    public boolean isAutoGradable() {
        return this != OPEN_ENDED;
    }

    /**
     * Get question types that are suitable for quick assessments
     * @return array of quick assessment question types
     */
    public static QuestionType[] getQuickAssessmentTypes() {
        return new QuestionType[]{MULTIPLE_CHOICE, SINGLE_CHOICE, TRUE_FALSE};
    }

    /**
     * Get question types that require manual grading
     * @return array of manually graded question types
     */
    public static QuestionType[] getManualGradingTypes() {
        return new QuestionType[]{OPEN_ENDED};
    }
}