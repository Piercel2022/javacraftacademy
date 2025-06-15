package com.javacraftacademy.userservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.jwt")
public class JwtConfig {
    
    private String secret = "javaCraftAcademySecretKeyForJWTTokenGeneration2024";
    private int accessTokenExpirationInMs = 86400000; // 24 hours
    private int refreshTokenExpirationInMs = 604800000; // 7 days
    private String tokenPrefix = "Bearer ";
    private String headerString = "Authorization";
    private String issuer = "JavaCraft Academy";
    
    // Getters and setters
    public String getSecret() {
        return secret;
    }
    
    public void setSecret(String secret) {
        this.secret = secret;
    }
    
    public int getAccessTokenExpirationInMs() {
        return accessTokenExpirationInMs;
    }
    
    public void setAccessTokenExpirationInMs(int accessTokenExpirationInMs) {
        this.accessTokenExpirationInMs = accessTokenExpirationInMs;
    }
    
    public int getRefreshTokenExpirationInMs() {
        return refreshTokenExpirationInMs;
    }
    
    public void setRefreshTokenExpirationInMs(int refreshTokenExpirationInMs) {
        this.refreshTokenExpirationInMs = refreshTokenExpirationInMs;
    }
    
    public String getTokenPrefix() {
        return tokenPrefix;
    }
    
    public void setTokenPrefix(String tokenPrefix) {
        this.tokenPrefix = tokenPrefix;
    }
    
    public String getHeaderString() {
        return headerString;
    }
    
    public void setHeaderString(String headerString) {
        this.headerString = headerString;
    }
    
    public String getIssuer() {
        return issuer;
    }
    
    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }
    
    // Utility methods
    public long getAccessTokenExpirationInSeconds() {
        return accessTokenExpirationInMs / 1000;
    }
    
    public long getRefreshTokenExpirationInSeconds() {
        return refreshTokenExpirationInMs / 1000;
    }
    
    public boolean isTokenPrefixValid(String token) {
        return token != null && token.startsWith(tokenPrefix);
    }
    
    public String extractTokenFromHeader(String authHeader) {
        if (isTokenPrefixValid(authHeader)) {
            return authHeader.substring(tokenPrefix.length());
        }
        return null;
    }
}
