
package com.javacraftacademy.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Configuration CORS (Cross-Origin Resource Sharing) pour le service Gateway.
 * 
 * Cette classe configure les politiques CORS pour permettre aux applications frontend
 * d'accéder aux APIs backend via la passerelle. Elle gère :
 * - Les origines autorisées (domaines frontend)
 * - Les méthodes HTTP permises
 * - Les headers autorisés et exposés
 * - La gestion des credentials
 * - Les configurations spécifiques par environnement
 * 
 * Relations avec l'application :
 * - Intégrée avec SecurityConfig pour la sécurité globale
 * - Utilisée par tous les controllers et filtres
 * - Impact sur toutes les requêtes cross-origin
 * - Configuration centralisée pour l'ensemble des microservices
 * 
 * Fonctionnalités principales :
 * - Configuration flexible par environnement
 * - Support des requêtes preflight OPTIONS
 * - Gestion sécurisée des credentials
 * - Optimisation des performances avec mise en cache
 * 
 * @author JavaCraft Academy
 * @version 1.0
 * @since 2024
 */
@Configuration
public class CorsConfig {

    /**
     * Origines autorisées pour l'environnement de développement.
     * Configurées via application.yml
     */
    @Value("${cors.allowed-origins.dev:http://localhost:3000,http://localhost:4200,http://localhost:8080}")
    private String devAllowedOrigins;

    /**
     * Origines autorisées pour l'environnement de production.
     * Configurées via application.yml
     */
    @Value("${cors.allowed-origins.prod:https://app.javacraftacademy.com}")
    private String prodAllowedOrigins;

    /**
     * Méthodes HTTP autorisées.
     * Par défaut : GET, POST, PUT, DELETE, OPTIONS, PATCH
     */
    @Value("${cors.allowed-methods:GET,POST,PUT,DELETE,OPTIONS,PATCH}")
    private String allowedMethods;

    /**
     * Headers autorisés dans les requêtes.
     * Par défaut : tous les headers (*)
     */
    @Value("${cors.allowed-headers:*}")
    private String allowedHeaders;

    /**
     * Headers exposés dans les réponses.
     * Utile pour exposer des headers personnalisés au frontend
     */
    @Value("${cors.exposed-headers:Authorization,X-Total-Count,X-Request-Id}")
    private String exposedHeaders;

    /**
     * Durée de cache pour les requêtes preflight (en secondes).
     * Par défaut : 1 heure (3600 secondes)
     */
    @Value("${cors.max-age:3600}")
    private Long maxAge;

    /**
     * Indique si les credentials doivent être autorisés.
     * Nécessaire pour les cookies et headers d'authentification
     */
    @Value("${cors.allow-credentials:true}")
    private Boolean allowCredentials;

    /**
     * Profil actif de l'application.
     * Utilisé pour déterminer les origines autorisées
     */
    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    /**
     * Configure la source de configuration CORS principale.
     * 
     * Cette méthode crée une configuration CORS basée sur l'environnement actuel
     * et les propriétés définies dans les fichiers de configuration.
     * 
     * @return CorsConfigurationSource La source de configuration CORS
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Configure les origines autorisées selon l'environnement
        List<String> allowedOrigins = getAllowedOrigins();
        if (allowedOrigins.contains("*")) {
            configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        } else {
            configuration.setAllowedOrigins(allowedOrigins);
        }
        
        // Configure les méthodes HTTP autorisées
        configuration.setAllowedMethods(Arrays.asList(allowedMethods.split(",")));
        
        // Configure les headers autorisés
        if ("*".equals(allowedHeaders)) {
            configuration.setAllowedHeaders(Arrays.asList("*"));
        } else {
            configuration.setAllowedHeaders(Arrays.asList(allowedHeaders.split(",")));
        }
        
        // Configure les headers exposés
        configuration.setExposedHeaders(Arrays.asList(exposedHeaders.split(",")));
        
        // Configure les credentials
        configuration.setAllowCredentials(allowCredentials);
        
        // Configure la durée de cache
        configuration.setMaxAge(maxAge);
        
        // Applique la configuration à toutes les routes
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }

    /**
     * Configure une source CORS spécifique pour les APIs publiques.
     * 
     * Cette configuration peut être plus permissive pour les endpoints publics
     * comme les APIs de santé ou la documentation.
     * 
     * @return CorsConfigurationSource Configuration CORS pour les APIs publiques
     */
    @Bean("publicCorsConfigurationSource")
    public CorsConfigurationSource publicCorsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Plus permissif pour les endpoints publics
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(false); // Pas de credentials pour les APIs publiques
        configuration.setMaxAge(7200L); // Cache plus long pour les APIs publiques
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/health/**", configuration);
        source.registerCorsConfiguration("/api/public/**", configuration);
        source.registerCorsConfiguration("/swagger-ui/**", configuration);
        source.registerCorsConfiguration("/v3/api-docs/**", configuration);
        
        return source;
    }

    /**
     * Détermine les origines autorisées basées sur le profil actif.
     * 
     * Cette méthode sélectionne les origines appropriées selon l'environnement :
     * - Développement : localhost avec différents ports
     * - Production : domaines de production sécurisés
     * - Test : configuration spécifique pour les tests
     * 
     * @return List<String> Liste des origines autorisées
     */
    private List<String> getAllowedOrigins() {
        switch (activeProfile.toLowerCase()) {
            case "prod":
            case "production":
                return Arrays.asList(prodAllowedOrigins.split(","));
            
            case "test":
                return Arrays.asList("http://localhost:3000", "http://localhost:8080");
            
            case "dev":
            case "development":
            default:
                return Arrays.asList(devAllowedOrigins.split(","));
        }
    }

    /**
     * Vérifie si une origine est autorisée.
     * 
     * Méthode utilitaire pour valider les origines dans les filtres personnalisés.
     * 
     * @param origin L'origine à vérifier
     * @return boolean true si l'origine est autorisée, false sinon
     */
    public boolean isOriginAllowed(String origin) {
        if (origin == null) {
            return false;
        }
        
        List<String> allowedOrigins = getAllowedOrigins();
        return allowedOrigins.contains("*") || 
               allowedOrigins.contains(origin) ||
               allowedOrigins.stream().anyMatch(allowed -> 
                   allowed.endsWith("*") && origin.startsWith(allowed.substring(0, allowed.length() - 1))
               );
    }

    /**
     * Retourne la configuration CORS pour le debugging.
     * 
     * Méthode utilitaire pour le debugging et le monitoring des configurations CORS.
     * 
     * @return String Représentation textuelle de la configuration CORS
     */
    public String getCorsConfigurationInfo() {
        return String.format(
            "CORS Configuration [Profile: %s] - " +
            "Allowed Origins: %s, " +
            "Allowed Methods: %s, " +
            "Allow Credentials: %s, " +
            "Max Age: %d seconds",
            activeProfile,
            getAllowedOrigins(),
            allowedMethods,
            allowCredentials,
            maxAge
        );
    }

    /**
     * Configure les headers de sécurité supplémentaires.
     * 
     * Cette méthode peut être utilisée pour ajouter des headers de sécurité
     * spécifiques aux réponses CORS.
     * 
     * @return List<String> Liste des headers de sécurité
     */
    public List<String> getSecurityHeaders() {
        return Arrays.asList(
            "X-Content-Type-Options: nosniff",
            "X-Frame-Options: DENY",
            "X-XSS-Protection: 1; mode=block",
            "Referrer-Policy: strict-origin-when-cross-origin"
        );
    }
}