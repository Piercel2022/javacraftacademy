package com.javacraftacademy.courseservice.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

/**
 * Configuration Swagger/OpenAPI pour le service Course de JavaCraft Academy.
 * 
 * Cette classe configure la documentation API automatique en utilisant OpenAPI 3.0,
 * permettant aux développeurs et aux équipes de comprendre et tester facilement l'API.
 * 
 * <h2>Fonctionnalités de documentation :</h2>
 * <ul>
 *   <li>Documentation interactive via Swagger UI</li>
 *   <li>Authentification JWT intégrée dans l'interface</li>
 *   <li>Organisation par tags (Courses, Lessons, Categories, etc.)</li>
 *   <li>Exemples de requêtes et réponses</li>
 *   <li>Modèles de données détaillés</li>
 *   <li>Support multi-environnements (dev, staging, prod)</li>
 * </ul>
 * 
 * <h2>Architecture de documentation :</h2>
 * <p>
 * La documentation est générée automatiquement à partir des annotations
 * Spring et OpenAPI dans les contrôleurs et modèles de données.
 * L'interface Swagger UI permet de tester directement les endpoints
 * avec authentification JWT.
 * </p>
 * 
 * <h2>URLs d'accès :</h2>
 * <ul>
 *   <li><strong>Swagger UI :</strong> /swagger-ui/index.html</li>
 *   <li><strong>OpenAPI JSON :</strong> /v3/api-docs</li>
 *   <li><strong>OpenAPI YAML :</strong> /v3/api-docs.yaml</li>
 * </ul>
 * 
 * <h2>Bonnes pratiques appliquées :</h2>
 * <ul>
 *   <li>Sécurité JWT configurée pour les tests</li>
 *   <li>Tags organisés par domaine fonctionnel</li>
 *   <li>Informations de contact et licence</li>
 *   <li>Versioning API claire</li>
 *   <li>Description détaillée des endpoints</li>
 * </ul>
 * 
 * @author JavaCraft Academy Team
 * @version 1.0
 * @since 2024
 */
@Configuration
public class SwaggerConfig {

    @Value("${app.version:1.0}")
    private String appVersion;

    @Value("${app.title:Course Service API}")
    private String appTitle;

    @Value("${app.description:API de gestion des cours pour JavaCraft Academy}")
    private String appDescription;

    @Value("${server.servlet.context-path:/}")
    private String contextPath;

    @Value("${swagger.server.url:http://localhost:8082}")
    private String serverUrl;

    /**
     * Configuration principale de l'OpenAPI.
     * 
     * Cette méthode configure tous les aspects de la documentation API,
     * incluant les informations générales, la sécurité, et l'organisation.
     * 
     * <h3>Éléments configurés :</h3>
     * <ul>
     *   <li>Métadonnées de l'API (titre, description, version)</li>
     *   <li>Informations de contact et licence</li>
     *   <li>Schémas de sécurité JWT</li>
     *   <li>Serveurs et environnements</li>
     *   <li>Tags pour l'organisation</li>
     * </ul>
     * 
     * @return Configuration OpenAPI complète
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(serverList())
                .components(securityComponents())
                .security(securityRequirements())
                .tags(apiTags());
    }

    /**
     * Informations générales de l'API.
     * 
     * Configure les métadonnées principales de l'API comme le titre,
     * la description, la version, et les informations de contact.
     * 
     * @return Informations de l'API
     */
    private Info apiInfo() {
        return new Info()
                .title(appTitle)
                .version(appVersion)
                .description(buildDescription())
                .contact(contactInfo())
                .license(licenseInfo());
    }

    /**
     * Construction de la description détaillée de l'API.
     * 
     * @return Description complète avec fonctionnalités et utilisation
     */
    private String buildDescription() {
        return appDescription + "\n\n" +
                "## Fonctionnalités principales\n" +
                "- **Gestion des cours** : Création, modification, consultation des cours\n" +
                "- **Gestion des leçons** : Organisation du contenu pédagogique\n" +
                "- **Système d'inscription** : Inscription et suivi des étudiants\n" +
                "- **Catégorisation** : Classification et recherche des cours\n" +
                "- **Authentification JWT** : Sécurité basée sur les rôles\n\n" +
                "## Authentification\n" +
                "Cette API utilise l'authentification JWT. Pour tester les endpoints protégés :\n" +
                "1. Obtenez un token JWT depuis le User Service\n" +
                "2. Cliquez sur 'Authorize' et saisissez : `Bearer {votre-token}`\n" +
                "3. Vous pouvez maintenant tester tous les endpoints\n\n" +
                "## Rôles utilisateur\n" +
                "- **STUDENT** : Consultation et inscription aux cours\n" +
                "- **INSTRUCTOR** : Création et gestion des cours\n" +
                "- **ADMIN** : Accès complet au système\n\n" +
                "## Support\n" +
                "Pour toute question technique, contactez l'équipe de développement.";
    }

    /**
     * Informations de contact de l'équipe de développement.
     * 
     * @return Détails de contact
     */
    private Contact contactInfo() {
        return new Contact()
                .name("JavaCraft Academy - Équipe Technique")
                .email("dev-team@javacraftacademy.com")
                .url("https://javacraftacademy.com/support");
    }

    /**
     * Informations de licence de l'API.
     * 
     * @return Détails de la licence
     */
    private License licenseInfo() {
        return new License()
                .name("MIT License")
                .url("https://opensource.org/licenses/MIT");
    }

    /**
     * Configuration des serveurs pour différents environnements.
     * 
     * Permet de tester l'API sur différents environnements
     * directement depuis l'interface Swagger.
     * 
     * @return Liste des serveurs configurés
     */
    private List<Server> serverList() {
        return Arrays.asList(
                new Server()
                        .url(serverUrl + contextPath)
                        .description("Serveur de développement local"),
                new Server()
                        .url("https://api-dev.javacraftacademy.com/course-service")
                        .description("Environnement de développement"),
                new Server()
                        .url("https://api-staging.javacraftacademy.com/course-service")
                        .description("Environnement de staging"),
                new Server()
                        .url("https://api.javacraftacademy.com/course-service")
                        .description("Environnement de production")
        );
    }

    /**
     * Configuration des composants de sécurité.
     * 
     * Définit le schéma d'authentification JWT pour permettre
     * les tests depuis l'interface Swagger UI.
     * 
     * @return Composants de sécurité configurés
     */
    private Components securityComponents() {
        return new Components()
                .addSecuritySchemes("bearerAuth", jwtSecurityScheme())
                .addSecuritySchemes("apiKey", apiKeySecurityScheme());
    }

    /**
     * Schéma de sécurité JWT Bearer.
     * 
     * @return Configuration du schéma JWT
     */
    private SecurityScheme jwtSecurityScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Authentification JWT - Saisissez votre token JWT sans le préfixe 'Bearer'");
    }

    /**
     * Schéma de sécurité par clé API (pour usage futur).
     * 
     * @return Configuration du schéma API Key
     */
    private SecurityScheme apiKeySecurityScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.HEADER)
                .name("X-API-Key")
                .description("Clé API pour l'authentification de service à service");
    }

    /**
     * Exigences de sécurité globales.
     * 
     * Définit quels schémas de sécurité sont requis
     * par défaut pour l'API.
     * 
     * @return Liste des exigences de sécurité
     */
    private List<SecurityRequirement> securityRequirements() {
        return Arrays.asList(
                new SecurityRequirement().addList("bearerAuth"),
                new SecurityRequirement().addList("apiKey")
        );
    }

    /**
     * Tags pour l'organisation de la documentation.
     * 
     * Les tags permettent de regrouper les endpoints
     * par domaine fonctionnel dans l'interface Swagger.
     * 
     * @return Liste des tags organisationnels
     */
    private List<Tag> apiTags() {
        return Arrays.asList(
                new Tag()
                        .name("Courses")
                        .description("Gestion des cours - Création, modification, consultation et recherche des cours disponibles"),
                        
                new Tag()
                        .name("Lessons")
                        .description("Gestion des leçons - Organisation du contenu pédagogique au sein des cours"),
                        
                new Tag()
                        .name("Categories")
                        .description("Gestion des catégories - Classification et organisation thématique des cours"),
                        
                new Tag()
                        .name("Enrollments")
                        .description("Gestion des inscriptions - Inscription des étudiants aux cours et suivi des progressions"),
                        
                new Tag()
                        .name("Public")
                        .description("Endpoints publics - Accessible sans authentification pour la consultation générale"),
                        
                new Tag()
                        .name("Admin")
                        .description("Administration - Fonctionnalités réservées aux administrateurs système"),
                        
                new Tag()
                        .name("Health")
                        .description("Monitoring - Endpoints de surveillance et de santé du service")
        );
    }
}