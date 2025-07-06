
package com.javacraftacademy.gateway.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import com.javacraftacademy.gateway.filter.AuthenticationFilter;

import java.util.Arrays;
import java.util.List;

/**
 * Configuration de sécurité pour le service Gateway.
 * 
 * Cette classe configure la sécurité Spring WebFlux pour la passerelle API, incluant :
 * - L'authentification JWT personnalisée
 * - La configuration CORS
 * - Les règles d'autorisation des endpoints
 * - La gestion des sessions (stateless)
 * 
 * Relations avec l'application :
 * - Utilise AuthenticationFilter pour la validation des tokens JWT
 * - Intègre avec CorsConfig pour la configuration cross-origin
 * - Protège les routes vers les microservices backend
 * - Fournit un point central de sécurité pour l'ensemble de l'architecture
 * 
 * Fonctionnalités principales :
 * - Protection des endpoints sensibles
 * - Authentification basée sur JWT
 * - Configuration CORS flexible
 * - Gestion des erreurs d'authentification/autorisation
 * 
 * @author JavaCraft Academy
 * @version 1.0
 * @since 2024
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Autowired
    private AuthenticationFilter authenticationFilter;

    /**
     * Configure la chaîne de filtres de sécurité pour WebFlux.
     * 
     * Cette méthode définit les règles de sécurité pour l'application :
     * - Désactive CSRF (non nécessaire pour une API REST)
     * - Configure les endpoints publics et protégés
     * - Intègre le filtre d'authentification JWT
     * - Configure la gestion des sessions en mode stateless
     * 
     * @param http L'objet ServerHttpSecurity pour configurer la sécurité
     * @return SecurityWebFilterChain La chaîne de filtres de sécurité configurée
     */
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
            // Désactive CSRF car nous utilisons JWT
            .csrf().disable()
            
            // Configure les autorisations des endpoints
            .authorizeExchange(exchanges -> exchanges
                // Endpoints publics - pas d'authentification requise
                .pathMatchers("/api/auth/**").permitAll()
                .pathMatchers("/api/health/**").permitAll()
                .pathMatchers("/actuator/**").permitAll()
                .pathMatchers("/swagger-ui/**").permitAll()
                .pathMatchers("/v3/api-docs/**").permitAll()
                
                // Tous les autres endpoints nécessitent une authentification
                .anyExchange().authenticated()
            )
            
            // Configure CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // Ajoute le filtre d'authentification JWT
            .addFilterBefore(authenticationFilter, 
                org.springframework.security.web.server.authentication.AuthenticationWebFilter.class)
            
            // Configure la gestion des sessions (stateless pour JWT)
            .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
            
            .build();
    }

    /**
     * Configure la source de configuration CORS.
     * 
     * Cette méthode définit les règles CORS pour permettre les requêtes
     * cross-origin depuis les applications frontend.
     * 
     * @return CorsConfigurationSource La source de configuration CORS
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Origines autorisées (à configurer selon l'environnement)
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        
        // Méthodes HTTP autorisées
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        
        // Headers autorisés
        configuration.setAllowedHeaders(Arrays.asList("*"));
        
        // Permet les credentials (cookies, headers d'autorisation)
        configuration.setAllowCredentials(true);
        
        // Headers exposés au client
        configuration.setExposedHeaders(Arrays.asList("Authorization", "X-Total-Count"));
        
        // Durée de cache pour les requêtes preflight
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }

    /**
     * Bean pour l'encodage des mots de passe.
     * 
     * Utilise BCrypt pour l'encodage sécurisé des mots de passe.
     * Bien que principalement utilisé dans les services d'authentification,
     * ce bean peut être nécessaire pour certaines validations au niveau gateway.
     * 
     * @return PasswordEncoder L'encodeur de mots de passe BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /**
     * Configure les endpoints de santé publics.
     * 
     * Cette méthode retourne la liste des patterns d'URL qui doivent
     * être accessibles sans authentification pour les vérifications de santé.
     * 
     * @return List<String> Liste des patterns d'endpoints publics
     */
    public List<String> getPublicEndpoints() {
        return Arrays.asList(
            "/api/auth/**",
            "/api/health/**",
            "/actuator/health",
            "/actuator/info",
            "/swagger-ui/**",
            "/v3/api-docs/**"
        );
    }

    /**
     * Configure les endpoints nécessitant des rôles spécifiques.
     * 
     * Cette méthode peut être étendue pour définir des règles d'autorisation
     * basées sur les rôles utilisateur extraits du JWT.
     * 
     * @return List<String> Liste des patterns d'endpoints admin
     */
    public List<String> getAdminEndpoints() {
        return Arrays.asList(
            "/api/admin/**",
            "/actuator/**"
        );
    }
}