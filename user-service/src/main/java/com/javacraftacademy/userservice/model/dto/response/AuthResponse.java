package com.javacraftacademy.userservice.model.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    
    @JsonProperty("access_token")
    private String accessToken;
    
    @JsonProperty("refresh_token")
    private String refreshToken;
    
    @JsonProperty("token_type")
    private String tokenType;
    
    @JsonProperty("expires_in")
    private Long expiresIn;
    
    @JsonProperty("user_info")
    private UserInfo userInfo;
    
    @JsonProperty("timestamp")
    private LocalDateTime timestamp;

    private List<String> roles;
    
    // Nouveau champ pour identifier le fondateur/super admin
    @Builder.Default
    private Boolean isFounder = false;
    
    // Champs additionnels pour le super admin
    private String firstName;
    private String lastName;
    private String lastLoginAt;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private Long id;
        private String email;
        private String firstName;
        private String lastName;
        private String role;
        private boolean emailVerified;
    }
    
    public AuthResponse(String accessToken, String refreshToken, String tokenType, Long expiresIn, UserInfo userInfo) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenType = tokenType;
        this.expiresIn = expiresIn;
        this.userInfo = userInfo;
        this.timestamp = LocalDateTime.now();
    }
}
