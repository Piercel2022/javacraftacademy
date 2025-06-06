package com.javacraftacademy.courseservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Configuration de sécurité pour le service Course de JavaCraft Academy.
 * 
 * Cette classe configure tous les aspects de sécurité de l'application,
 * incluant l'authentification JWT, l'autorisation basée sur les rôles,
 * et la protection contre les attaques courantes.
 * 
 * <h2>Fonctionnalités de sécurité :</h2>
 * <ul>
 *   <li>Authentification JWT stateless</li>
 *   <li>Autorisation granulaire basée sur les rôles (STUDENT, INSTRUCTOR, ADMIN)</li>
 *   <li>Protection CORS pour les appels cross-origin</li>
 *   <li>Protection CSRF désactivée (API REST stateless)</li>
 *   <li>Sécurisation des endpoints selon les rôles utilisateur</li>
 *   <li>Configuration des endpoints publics (santé, documentation)</li>
 * </ul>
 * 
 * <h2>Architecture de sécurité :</h2>
 * <p>
 * Le service Course utilise une authentification distribuée où :
 * - Le User Service génère et valide les tokens JWT
 * - Le Course Service valide les tokens et extrait les informations utilisateur
 * - Les autorisations sont basées sur les rôles contenus dans le JWT
 * </p>
 * 
 * <h2>Niveaux d'accès :</h2>
 * <ul>
 *   <li><strong>PUBLIC :</strong> Consultation des cours publics, catégories</li>
 *   <li><strong>STUDENT :</strong> Inscription aux cours, accès aux contenus</li>
 *   <li><strong>INSTRUCTOR :</strong> Création/modification de cours et leçons</li>
 *   <li><strong>ADMIN :</strong> Gestion complète du système</li>
 * </ul>
 * 
 * <h2>Évolutions futures :</h2>
 * <ul>
 *   <li>Authentification multi-facteur (MFA)</li>
 *   <li>Rate limiting par utilisateur</li>
 *   <li>Audit trail des actions sensibles</li>
 *   <li>Chiffrement des données sensibles</li>
 *   <li>Support OAuth2 avec providers externes (Google, GitHub)</li>
 * </ul>
 * 
 * @author JavaCraft Academy Team
 * @version 1.0
 * @since 2024
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.issuer:javacraftacademy}")
    private String jwtIssuer;

    @Value("${cors.allowed-origins}")
    private List<String> allowedOrigins;

    /**
     * Configuration principale de la chaîne de filtres de sécurité.
     * 
     * Cette méthode définit toutes les règles de sécurité pour l'application,
     * incluant quels endpoints sont accessibles à qui et comment l'authentification fonctionne.
     * 
     * <h3>Stratégie de sécurité :</h3>
     * <ul>
     *   <li>Session stateless (pas de sessions HTTP)</li>
     *   <li>Authentification JWT obligatoire sauf endpoints publics</li>
     *   <li>Autorisation granulaire par endpoint et méthode HTTP</li>
     * </ul>
     * 
     * @param http Configuration HTTP Security
     * @return Chaîne de filtres de sécurité configurée
     * @throws Exception En cas d'erreur de configuration
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Désactivation des fonctionnalités non nécessaires pour une API REST
            .csrf().disable()
            .formLogin().disable()
            .httpBasic().disable()
            .logout().disable()
            
            // Configuration de la politique de session (stateless)
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            
            // Configuration CORS
            .cors().configurationSource(corsConfigurationSource())
            .and()
            
            // Configuration des autorisations par endpoint
            .authorizeHttpRequests(authz -> authz
                // Endpoints publics - accessibles sans authentification
                .requestMatchers(HttpMethod.GET, "/api/v1/courses/public/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/categories/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/courses/search/**").permitAll()
                
                // Endpoints de monitoring et documentation
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                
                // Endpoints pour les étudiants
                .requestMatchers(HttpMethod.GET, "/api/v1/courses/**").hasAnyRole("STUDENT", "INSTRUCTOR", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/v1/enrollments/**").hasAnyRole("STUDENT", "INSTRUCTOR", "ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/v1/enrollments/**").hasAnyRole("STUDENT", "INSTRUCTOR", "ADMIN")
                
                // Endpoints pour les instructeurs
                .requestMatchers(HttpMethod.POST, "/api/v1/courses/**").hasAnyRole("INSTRUCTOR", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/v1/courses/**").hasAnyRole("INSTRUCTOR", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/v1/lessons/**").hasAnyRole("INSTRUCTOR", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/v1/lessons/**").hasAnyRole("INSTRUCTOR", "ADMIN")
                
                // Endpoints pour les administrateurs
                .requestMatchers(HttpMethod.DELETE, "/api/v1/courses/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/lessons/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/v1/categories/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/v1/categories/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/categories/**").hasRole("ADMIN")
                
                // Tous les autres endpoints nécessitent une authentification
                .anyRequest().authenticated()
            )
            
            // Configuration de l'authentification JWT
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .decoder(jwtDecoder())
                    .jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
            );

        return http.build();
    }

    /**
     * Configuration du décodeur JWT.
     * 
     * Le décodeur JWT valide la signature des tokens et extrait les informations
     * contenues dans le payload pour l'authentification.
     * 
     * @return Décodeur JWT configuré avec la clé secrète
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        // Configuration du décodeur avec la clé secrète HMAC
        return NimbusJwtDecoder.withSecretKey(jwtSecret.getBytes())
                .build();
    }

    /**
     * Convertisseur d'authentification JWT personnalisé.
     * 
     * Ce convertisseur extrait les rôles du token JWT et les transforme
     * en authorities Spring Security avec le préfixe "ROLE_".
     * 
     * @return Convertisseur d'authentification JWT
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
        
        // Configuration pour extraire les rôles du claim "roles"
        authoritiesConverter.setAuthorityPrefix("ROLE_");
        authoritiesConverter.setAuthoritiesClaimName("roles");
        
        JwtAuthenticationConverter authenticationConverter = new JwtAuthenticationConverter();
        authenticationConverter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
        
        return authenticationConverter;
    }

    /**
     * Configuration CORS pour permettre les appels cross-origin.
     * 
     * Nécessaire pour permettre aux applications frontend (React, Angular, etc.)
     * hébergées sur des domaines différents d'accéder à l'API.
     * 
     * <h3>Politiques CORS configurées :</h3>
     * <ul>
     *   <li>Origins autorisées : configurables via application.properties</li>
     *   <li>Méthodes HTTP : GET, POST, PUT, DELETE, OPTIONS</li>
     *   <li>Headers : Authorization, Content-Type, Accept</li>
     *   <li>Credentials : autorisées pour l'authentification</li>
     * </ul>
     * 
     * @return Source de configuration CORS
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Origins autorisées (configurables via properties)
        configuration.setAllowedOrigins(allowedOrigins);
        
        // Méthodes HTTP autorisées
        configuration.setAllowedMethods(Arrays.asList(
            HttpMethod.GET.name(),
            HttpMethod.POST.name(),
            HttpMethod.PUT.name(),
            HttpMethod.DELETE.name(),
            HttpMethod.OPTIONS.name()
        ));
        
        // Headers autorisés
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "Accept",
            "X-Requested-With",
            "Cache-Control"
        ));
        
        // Headers exposés au client
        configuration.setExposedHeaders(Arrays.asList(
            "Authorization",
            "X-Total-Count",
            "X-Page-Number"
        ));
        
        // Autoriser les credentials (cookies, headers d'auth)
        configuration.setAllowCredentials(true);
        
        // Durée de cache pour les requêtes préflight
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        
        return source;
    }

    /**
     * Encodeur de mots de passe BCrypt.
     * 
     * Bien que le service Course ne gère pas directement l'authentification,
     * cet encodeur peut être utile pour des fonctionnalités futures comme
     * la validation de mots de passe temporaires ou l'audit.
     * 
     * @return Encodeur BCrypt avec une force de 12
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /**
     * Filtre d'authentification JWT personnalisé (optionnel).
     * 
     * Ce filtre peut être utilisé pour des traitements supplémentaires
     * lors de l'authentification JWT, comme la validation de tokens blacklistés
     * ou l'enrichissement du contexte de sécurité.
     * 
     * Note : Actuellement commenté car la configuration OAuth2 Resource Server
     * gère déjà l'authentification JWT de base.
     */
    /*
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtDecoder(), jwtAuthenticationConverter());
    }
    */

    /**
     * Gestionnaire d'exceptions d'authentification personnalisé.
     * 
     * Permet de personnaliser les réponses d'erreur en cas d'échec d'authentification,
     * par exemple pour retourner des messages d'erreur standardisés en JSON.
     */
    /*
    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return new CustomAuthenticationEntryPoint();
    }
    */

    /**
     * Gestionnaire d'exceptions d'autorisation personnalisé.
     * 
     * Permet de personnaliser les réponses d'erreur en cas d'accès refusé,
     * par exemple pour retourner des codes d'erreur spécifiques à l'application.
     */
    /*
    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return new CustomAccessDeniedHandler();
    }
    */
}