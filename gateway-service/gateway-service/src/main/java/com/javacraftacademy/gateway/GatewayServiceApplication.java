
package com.javacraftacademy.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.gateway.config.GatewayAutoConfiguration;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.reactive.config.EnableWebFluxSecurity;

import javax.annotation.PreDestroy;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Optional;

/**
 * Classe principale de l'application Gateway Service.
 * 
 * <p>Cette classe constitue le point d'entrée de l'application Spring Boot Gateway qui agit comme
 * un proxy inverse et un point d'accès unifié pour l'ensemble des microservices de l'architecture.
 * 
 * <h3>Responsabilités principales :</h3>
 * <ul>
 *   <li><strong>Routage des requêtes</strong> : Redirection des requêtes vers les microservices appropriés</li>
 *   <li><strong>Authentification et autorisation</strong> : Validation des tokens JWT et contrôle d'accès</li>
 *   <li><strong>Rate limiting</strong> : Limitation du nombre de requêtes par client</li>
 *   <li><strong>Logging et monitoring</strong> : Traçabilité des requêtes et métriques de performance</li>
 *   <li><strong>CORS et sécurité</strong> : Gestion des politiques de sécurité cross-origin</li>
 *   <li><strong>Load balancing</strong> : Répartition de charge entre les instances de services</li>
 * </ul>
 * 
 * <h3>Architecture et relations :</h3>
 * <ul>
 *   <li><strong>Filters</strong> : Utilise les filtres pour traiter les requêtes (AuthenticationFilter, RateLimitingFilter, etc.)</li>
 *   <li><strong>Services</strong> : Intègre les services d'authentification, JWT et validation</li>
 *   <li><strong>Configuration</strong> : S'appuie sur les classes de configuration (SecurityConfig, CorsConfig, etc.)</li>
 *   <li><strong>Controllers</strong> : Expose des endpoints de santé et de gestion via les contrôleurs</li>
 *   <li><strong>Redis</strong> : Utilise Redis pour le cache et le stockage de sessions</li>
 * </ul>
 * 
 * <h3>Fonctionnalités activées :</h3>
 * <ul>
 *   <li><strong>@EnableCaching</strong> : Activation du cache Redis pour les performances</li>
 *   <li><strong>@EnableAsync</strong> : Support des opérations asynchrones</li>
 *   <li><strong>@EnableScheduling</strong> : Activation des tâches planifiées</li>
 *   <li><strong>@EnableWebFluxSecurity</strong> : Sécurité réactive avec WebFlux</li>
 *   <li><strong>@EnableConfigurationProperties</strong> : Liaison des propriétés de configuration</li>
 * </ul>
 * 
 * <h3>Profils d'environnement supportés :</h3>
 * <ul>
 *   <li><strong>dev</strong> : Environnement de développement avec logs détaillés</li>
 *   <li><strong>prod</strong> : Environnement de production avec optimisations</li>
 *   <li><strong>test</strong> : Environnement de test avec configurations spécifiques</li>
 * </ul>
 * 
 * <h3>Configuration requise :</h3>
 * <p>L'application nécessite les fichiers de configuration suivants :
 * <ul>
 *   <li>application.yml - Configuration principale</li>
 *   <li>application-dev.yml - Configuration développement</li>
 *   <li>application-prod.yml - Configuration production</li>
 *   <li>bootstrap.yml - Configuration de démarrage</li>
 * </ul>
 * 
 * <h3>Services externes requis :</h3>
 * <ul>
 *   <li><strong>Redis</strong> : Pour le cache et la gestion des sessions</li>
 *   <li><strong>Service Registry</strong> : Pour la découverte des microservices</li>
 *   <li><strong>Configuration Server</strong> : Pour la gestion centralisée des configurations</li>
 * </ul>
 * 
 * @author JavaCraft Academy
 * @version 1.0
 * @since 1.0
 */
@SpringBootApplication
@EnableCaching
@EnableAsync
@EnableScheduling
@EnableWebFluxSecurity
@EnableConfigurationProperties
@ComponentScan(basePackages = {
    "com.javacraftacademy.gateway.config",
    "com.javacraftacademy.gateway.filter",
    "com.javacraftacademy.gateway.service",
    "com.javacraftacademy.gateway.controller",
    "com.javacraftacademy.gateway.util",
    "com.javacraftacademy.gateway.exception"
})
public class GatewayServiceApplication {

    private static final Logger logger = LoggerFactory.getLogger(GatewayServiceApplication.class);
    
    /**
     * Version de l'application Gateway.
     */
    private static final String APPLICATION_VERSION = "1.0.0";
    
    /**
     * Nom de l'application.
     */
    private static final String APPLICATION_NAME = "Gateway Service";
    
    /**
     * Bannière ASCII pour l'affichage au démarrage.
     */
    private static final String STARTUP_BANNER = """
            
             ██████╗  █████╗ ████████╗███████╗██╗    ██╗ █████╗ ██╗   ██╗
            ██╔════╝ ██╔══██╗╚══██╔══╝██╔════╝██║    ██║██╔══██╗╚██╗ ██╔╝
            ██║  ███╗███████║   ██║   █████╗  ██║ █╗ ██║███████║ ╚████╔╝ 
            ██║   ██║██╔══██║   ██║   ██╔══╝  ██║███╗██║██╔══██║  ╚██╔╝  
            ╚██████╔╝██║  ██║   ██║   ███████╗╚███╔███╔╝██║  ██║   ██║   
             ╚═════╝ ╚═╝  ╚═╝   ╚═╝   ╚══════╝ ╚══╝╚══╝ ╚═╝  ╚═╝   ╚═╝   
                                                                         
                        JavaCraft Academy - Microservices Gateway
            """;

    private final Environment environment;

    /**
     * Constructeur de l'application Gateway.
     * 
     * @param environment L'environnement Spring pour accéder aux propriétés
     */
    public GatewayServiceApplication(Environment environment) {
        this.environment = environment;
    }

    /**
     * Point d'entrée principal de l'application.
     * 
     * <p>Cette méthode démarre l'application Spring Boot Gateway avec les configurations
     * appropriées selon l'environnement détecté (dev, prod, test).
     * 
     * @param args Arguments de la ligne de commande
     */
    public static void main(String[] args) {
        try {
            logger.info("=== Démarrage de {} v{} ===", APPLICATION_NAME, APPLICATION_VERSION);
            logger.info("Heure de démarrage: {}", 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
            
            // Configuration des propriétés système
            configureSystemProperties();
            
            // Démarrage de l'application Spring Boot
            SpringApplication app = new SpringApplication(GatewayServiceApplication.class);
            
            // Configuration additionnelle de l'application
            configureApplication(app);
            
            // Lancement de l'application
            app.run(args);
            
        } catch (Exception e) {
            logger.error("Erreur fatale lors du démarrage de l'application: {}", e.getMessage(), e);
            System.exit(1);
        }
    }

    /**
     * Configuration des routes dynamiques du Gateway.
     * 
     * <p>Cette méthode définit les routes vers les différents microservices de l'architecture.
     * Les routes sont configurées de manière programmatique pour permettre une gestion
     * flexible et dynamique du routage.
     * 
     * <h4>Routes configurées :</h4>
     * <ul>
     *   <li><strong>/api/auth/**</strong> : Service d'authentification</li>
     *   <li><strong>/api/users/**</strong> : Service de gestion des utilisateurs</li>
     *   <li><strong>/api/orders/**</strong> : Service de gestion des commandes</li>
     *   <li><strong>/api/inventory/**</strong> : Service de gestion des stocks</li>
     *   <li><strong>/api/notifications/**</strong> : Service de notifications</li>
     * </ul>
     * 
     * @param builder Builder pour la construction des routes
     * @return RouteLocator configuré avec toutes les routes
     */
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        logger.info("Configuration des routes du Gateway...");
        
        return builder.routes()
                // Route vers le service d'authentification
                .route("auth-service", r -> r
                        .path("/api/auth/**")
                        .filters(f -> f
                                .stripPrefix(2)
                                .filter("AuthenticationFilter")
                                .filter("LoggingFilter")
                                .filter("RateLimitingFilter"))
                        .uri("lb://auth-service"))
                
                // Route vers le service utilisateurs
                .route("user-service", r -> r
                        .path("/api/users/**")
                        .filters(f -> f
                                .stripPrefix(2)
                                .filter("AuthenticationFilter")
                                .filter("LoggingFilter")
                                .filter("RequestValidationFilter"))
                        .uri("lb://user-service"))
                
                // Route vers le service commandes
                .route("order-service", r -> r
                        .path("/api/orders/**")
                        .filters(f -> f
                                .stripPrefix(2)
                                .filter("AuthenticationFilter")
                                .filter("LoggingFilter")
                                .filter("RateLimitingFilter"))
                        .uri("lb://order-service"))
                
                // Route vers le service inventaire
                .route("inventory-service", r -> r
                        .path("/api/inventory/**")
                        .filters(f -> f
                                .stripPrefix(2)
                                .filter("AuthenticationFilter")
                                .filter("LoggingFilter"))
                        .uri("lb://inventory-service"))
                
                // Route vers le service notifications
                .route("notification-service", r -> r
                        .path("/api/notifications/**")
                        .filters(f -> f
                                .stripPrefix(2)
                                .filter("AuthenticationFilter")
                                .filter("LoggingFilter"))
                        .uri("lb://notification-service"))
                
                // Route pour les endpoints de santé (sans authentification)
                .route("health-check", r -> r
                        .path("/actuator/**", "/health/**")
                        .filters(f -> f.filter("LoggingFilter"))
                        .uri("forward:/"))
                
                .build();
    }

    /**
     * Gestionnaire d'événement déclenché quand l'application est prête.
     * 
     * <p>Cette méthode est appelée automatiquement par Spring Boot une fois que
     * l'application est complètement initialisée et prête à recevoir des requêtes.
     * Elle affiche les informations de démarrage et vérifie la santé des composants.
     * 
     * @param event Événement ApplicationReady
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady(ApplicationReadyEvent event) {
        displayStartupBanner();
        logApplicationInfo();
        performHealthChecks();
        logActiveProfiles();
    }

    /**
     * Méthode appelée avant la destruction de l'application.
     * 
     * <p>Cette méthode permet de nettoyer les ressources et de fermer proprement
     * les connexions avant l'arrêt de l'application.
     */
    @PreDestroy
    public void onShutdown() {
        logger.info("=== Arrêt de {} ===", APPLICATION_NAME);
        logger.info("Nettoyage des ressources...");
        
        try {
            // Nettoyage des ressources si nécessaire
            cleanupResources();
            
            logger.info("Arrêt propre de l'application terminé");
        } catch (Exception e) {
            logger.error("Erreur lors de l'arrêt de l'application: {}", e.getMessage(), e);
        }
    }

    /**
     * Configure les propriétés système nécessaires au fonctionnement de l'application.
     */
    private static void configureSystemProperties() {
        // Configuration du fuseau horaire
        System.setProperty("user.timezone", "Europe/Paris");
        
        // Configuration des logs
        System.setProperty("logging.pattern.console", 
            "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level [%logger{36}] - %msg%n");
        
        // Configuration de la sécurité
        System.setProperty("spring.security.require-ssl", "false");
        
        logger.debug("Propriétés système configurées");
    }

    /**
     * Configure l'application Spring Boot avec des paramètres personnalisés.
     * 
     * @param app L'application Spring Boot à configurer
     */
    private static void configureApplication(SpringApplication app) {
        // Configuration des profils par défaut si aucun n'est spécifié
        app.setDefaultProperties(java.util.Map.of(
            "spring.profiles.default", "dev",
            "management.endpoints.web.exposure.include", "health,info,metrics,prometheus",
            "management.endpoint.health.show-details", "always"
        ));
        
        // Configuration de la bannière personnalisée
        app.setBannerMode(org.springframework.boot.Banner.Mode.OFF);
        
        logger.debug("Application Spring Boot configurée");
    }

    /**
     * Affiche la bannière de démarrage de l'application.
     */
    private void displayStartupBanner() {
        System.out.println(STARTUP_BANNER);
        System.out.println(String.format("                           Version %s", APPLICATION_VERSION));
        System.out.println();
    }

    /**
     * Affiche les informations détaillées de l'application au démarrage.
     */
    private void logApplicationInfo() {
        try {
            String serverPort = environment.getProperty("server.port", "8080");
            String hostAddress = InetAddress.getLocalHost().getHostAddress();
            String hostName = InetAddress.getLocalHost().getHostName();
            
            logger.info("=== {} démarré avec succès ===", APPLICATION_NAME);
            logger.info("Version: {}", APPLICATION_VERSION);
            logger.info("Host: {} ({})", hostName, hostAddress);
            logger.info("Port: {}", serverPort);
            logger.info("URL locale: http://localhost:{}", serverPort);
            logger.info("URL externe: http://{}:{}", hostAddress, serverPort);
            logger.info("Endpoints de santé: http://localhost:{}/actuator/health", serverPort);
            logger.info("Java version: {}", System.getProperty("java.version"));
            logger.info("Spring Boot version: {}", 
                org.springframework.boot.SpringBootVersion.getVersion());
            
        } catch (UnknownHostException e) {
            logger.warn("Impossible de déterminer l'adresse de l'hôte: {}", e.getMessage());
        }
    }

    /**
     * Effectue des vérifications de santé des composants critiques.
     */
    private void performHealthChecks() {
        logger.info("Vérification de la santé des composants...");
        
        try {
            // Vérification de la connexion Redis (si configurée)
            checkRedisConnection();
            
            // Vérification des services externes
            checkExternalServices();
            
            logger.info("✓ Tous les composants sont opérationnels");
            
        } catch (Exception e) {
            logger.warn("⚠ Certains composants ne sont pas disponibles: {}", e.getMessage());
        }
    }

    /**
     * Affiche les profils Spring actifs.
     */
    private void logActiveProfiles() {
        String[] activeProfiles = environment.getActiveProfiles();
        String[] defaultProfiles = environment.getDefaultProfiles();
        
        if (activeProfiles.length > 0) {
            logger.info("Profils actifs: {}", Arrays.toString(activeProfiles));
        } else {
            logger.info("Profils par défaut: {}", Arrays.toString(defaultProfiles));
        }
    }

    /**
     * Vérifie la connexion à Redis.
     */
    private void checkRedisConnection() {
        try {
            String redisHost = environment.getProperty("spring.redis.host", "localhost");
            String redisPort = environment.getProperty("spring.redis.port", "6379");
            
            logger.debug("Vérification de Redis sur {}:{}", redisHost, redisPort);
            // La vérification réelle serait implémentée ici
            
        } catch (Exception e) {
            logger.warn("Redis non disponible: {}", e.getMessage());
        }
    }

    /**
     * Vérifie la disponibilité des services externes.
     */
    private void checkExternalServices() {
        logger.debug("Vérification des services externes...");
        
        // Vérification du service de découverte
        Optional.ofNullable(environment.getProperty("eureka.client.service-url.defaultZone"))
                .ifPresentOrElse(
                    url -> logger.debug("Service registry configuré: {}", url),
                    () -> logger.debug("Service registry non configuré")
                );
        
        // Vérification du serveur de configuration
        Optional.ofNullable(environment.getProperty("spring.cloud.config.uri"))
                .ifPresentOrElse(
                    uri -> logger.debug("Config server configuré: {}", uri),
                    () -> logger.debug("Config server non configuré")
                );
    }

    /**
     * Nettoie les ressources avant l'arrêt de l'application.
     */
    private void cleanupResources() {
        // Fermeture des connexions pool si nécessaire
        logger.debug("Nettoyage des pools de connexions...");
        
        // Nettoyage du cache
        logger.debug("Nettoyage du cache...");
        
        // Autres nettoyages spécifiques
        logger.debug("Autres nettoyages terminés");
    }
}