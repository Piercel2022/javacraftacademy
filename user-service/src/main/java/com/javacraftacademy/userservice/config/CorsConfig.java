package com.javacraftacademy.userservice.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Value("${app.cors.allowed-origins:http://localhost:3000,http://localhost:3001,http://127.0.0.1:3000}")
    private List<String> allowedOrigins;

    @Value("${app.cors.allowed-methods:GET,POST,PUT,DELETE,OPTIONS,PATCH}")
    private List<String> allowedMethods;

    @Value("${app.cors.allowed-headers:*}")
    private List<String> allowedHeaders;

    @Value("${app.cors.exposed-headers:Authorization,Content-Type}")
    private List<String> exposedHeaders;

    @Value("${app.cors.allow-credentials:true}")
    private boolean allowCredentials;

    @Value("${app.cors.max-age:3600}")
    private long maxAge;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns(allowedOrigins.toArray(new String[0]))
                .allowedMethods(allowedMethods.toArray(new String[0]))
                .allowedHeaders(allowedHeaders.toArray(new String[0]))
                .exposedHeaders(exposedHeaders.toArray(new String[0]))
                .allowCredentials(allowCredentials)
                .maxAge(maxAge);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Set allowed origins
        configuration.setAllowedOriginPatterns(allowedOrigins);
        
        // Set allowed methods
        configuration.setAllowedMethods(allowedMethods);
        
        // Set allowed headers
        if (allowedHeaders.contains("*")) {
            configuration.addAllowedHeader("*");
        } else {
            configuration.setAllowedHeaders(allowedHeaders);
        }
        
        // Set exposed headers
        configuration.setExposedHeaders(exposedHeaders);
        
        // Allow credentials
        configuration.setAllowCredentials(allowCredentials);
        
        // Set max age for preflight requests
        configuration.setMaxAge(maxAge);

        // Apply CORS configuration to all paths
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        
        return source;
    }

    /**
     * Development CORS configuration - more permissive
     */
    @Bean
    public CorsConfiguration developmentCorsConfiguration() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(Arrays.asList(
            "http://localhost:*",
            "http://127.0.0.1:*",
            "https://localhost:*"
        ));
        config.setAllowedMethods(Arrays.asList("*"));
        config.setAllowedHeaders(Arrays.asList("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);
        return config;
    }

    /**
     * Production CORS configuration - more restrictive
     */
    @Bean
    public CorsConfiguration productionCorsConfiguration() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(Arrays.asList(
            "https://javacraftacademy.com",
            "https://www.javacraftacademy.com",
            "https://app.javacraftacademy.com"
        ));
        config.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        ));
        config.setAllowedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "Accept",
            "Origin",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers",
            "X-Requested-With",
            "X-Request-ID"
        ));
        config.setExposedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "X-Total-Count"
        ));
        config.setAllowCredentials(true);
        config.setMaxAge(1800L); // 30 minutes
        return config;
    }

    /**
     * Custom CORS configuration for specific endpoints
     */
    public void addCustomCorsMappings(CorsRegistry registry) {
        // Public API endpoints - more permissive
        registry.addMapping("/api/public/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(false)
                .maxAge(3600);

        // Authentication endpoints
        registry.addMapping("/api/auth/**")
                .allowedOriginPatterns(allowedOrigins.toArray(new String[0]))
                .allowedMethods("POST", "OPTIONS")
                .allowedHeaders("Content-Type", "Accept")
                .allowCredentials(true)
                .maxAge(300); // 5 minutes for auth endpoints

        // Admin endpoints - most restrictive
        registry.addMapping("/api/admin/**")
                .allowedOriginPatterns("https://admin.javacraftacademy.com")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("Authorization", "Content-Type")
                .allowCredentials(true)
                .maxAge(600); // 10 minutes
    }
}