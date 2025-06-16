package com.javacraftacademy.userservice.model.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileResponse {
    
    private Long id;
    
    @JsonProperty("user_id")
    private Long userId;
    
    @JsonProperty("first_name")
    private String firstName;
    
    @JsonProperty("last_name")
    private String lastName;
    
    @JsonProperty("display_name")
    private String displayName;
    
    private String bio;
    
    @JsonProperty("profile_picture_url")
    private String profilePictureUrl;
    
    @JsonProperty("date_of_birth")
    private LocalDate dateOfBirth;
    
    private String phone;
    
    private String country;
    
    private String city;
    
    private String timezone;
    
    private String language;
    
    @JsonProperty("learning_goals")
    private String learningGoals;
    
    @JsonProperty("programming_experience")
    private String programmingExperience;
    
    @JsonProperty("preferred_learning_style")
    private String preferredLearningStyle;
    
    @JsonProperty("github_username")
    private String githubUsername;
    
    @JsonProperty("linkedin_url")
    private String linkedinUrl;
    
    @JsonProperty("portfolio_url")
    private String portfolioUrl;
    
    @JsonProperty("newsletter_subscribed")
    private boolean newsletterSubscribed;
    
    @JsonProperty("public_profile")
    private boolean publicProfile;
    
    @JsonProperty("created_at")
    private LocalDateTime createdAt;
    
    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
    
    @JsonProperty("completion_percentage")
    private int completionPercentage;
}