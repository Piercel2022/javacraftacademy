package com.javacraftacademy.userservice.model.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    
    private Long id;
    
    private String email;
    
    @JsonProperty("first_name")
    private String firstName;
    
    @JsonProperty("last_name")
    private String lastName;
    
    private String username;
    
    @JsonProperty("email_verified")
    private boolean emailVerified;
    
    private boolean active;
    
    private Set<String> roles;
    
    @JsonProperty("created_at")
    private LocalDateTime createdAt;
    
    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
    
    @JsonProperty("last_login")
    private LocalDateTime lastLogin;
    
    @JsonProperty("profile_completed")
    private boolean profileCompleted;
    
    @JsonProperty("account_locked")
    private boolean accountLocked;
    
    @JsonProperty("failed_login_attempts")
    private int failedLoginAttempts;
}