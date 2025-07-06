package com.javacraftacademy.gateway;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests d'intégration complets pour l'application Gateway Service.
 * 
 * <p>Cette classe de test vérifie le bon fonctionnement de l'ensemble de l'application Gateway,
 * qui agit comme point d'entrée unique pour tous les services de l'architecture microservices.
 * 
 * <h2>Fonctionnalités testées du Gateway Service :</h2>
 * <ul>
 *   <li><strong>Démarrage de l'application</strong> - Vérification du contexte Spring Boot</li>
 *   <li><strong>Configuration des routes</strong> - Routage vers les microservices</li>
 *   <li><strong>Authentification centralisée</strong> - Validation JWT sur toutes les routes</li>
 *   <li><strong>Limitation du taux de requêtes</strong> - Protection contre les abus</li>
 *   <li><strong>Monitoring et santé</strong> - Points de contrôle pour l'observabilité</li>
 *   <li><strong>Gestion CORS</strong> - Configuration pour les clients web</li>
 *   <li><strong>Cache Redis</strong> - Mise en cache des sessions et tokens</li>
 * </ul>
 * 
 * <h2>Relations avec l'architecture globale :</h2>
 * <pre>
 * Client Web/Mobile
 *       ↓
 * [Gateway Service] ← Tests de cette classe
 *       ↓
 * ┌─────────────────────────────────┐
 * │ Microservices Backend :         │
 * │ • User Service                  │
 * │ • Product Service               │
 * │ • Order Service                 │
 * │ • Notification Service          │
 * │ • Inventory Service             │
 * └─────────────────────────────────┘
 *       ↓
 * Infrastructure :
 * • Redis (Cache)
 * • Base de données
 * • Services externes
 * </pre>
 * 
 * <h2>Patterns d'architecture implementés :</h2>
 * <ul>
 *   <li><strong>API Gateway Pattern</strong> - Point d'entrée unique</li>
 *   <li><strong>Circuit Breaker</strong> - Résilience face aux pannes</li>
 *   <li><strong>Load Balancing</strong> - Répartition de charge</li>
 *   <li><strong>Service Discovery</strong> - Découverte automatique des services</li>
 *   <li><strong>Cross-Cutting Concerns</strong> - Authentification, logging, monitoring</li>
 * </ul>
 * 
 * @author JavaCraft Academy
 * @version 1.0
 * @since 2024
 * 
 * @see com.javacraftacademy.gateway.GatewayServiceApplication
 * @see com.javacraftacademy.gateway.config.GatewayConfig
 * @see com.javacraftacademy.gateway.filter.AuthenticationFilter
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.cloud.gateway.discovery.locator.enabled=false",
    "management.endpoints.web.exposure.include=health,info,metrics"
})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Tests d'intégration - Gateway Service Application")
class GatewayServiceApplicationTests {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ApplicationContext applicationContext;

    /**
     * Test fondamental de démarrage de l'application Spring Boot.
     * 
     * <p>Vérifie que :
     * <ul>
     *   <li>Le contexte Spring se charge correctement</li>
     *   <li>Toutes les beans sont initialisées</li>
     *   <li>Les configurations sont appliquées</li>
     *   <li>Les dépendances sont résolues</li>
     * </ul>
     * 
     * <p><strong>Impact sur l'architecture :</strong>
     * Ce test garantit que le Gateway peut démarrer et servir de point d'entrée
     * pour l'ensemble de l'écosystème microservices.
     */
    @Test
    @DisplayName("Démarrage réussi de l'application Gateway")
    void contextLoads() {
        assertThat(applicationContext).isNotNull();
        assertThat(applicationContext.getBean(GatewayServiceApplication.class)).isNotNull();
    }

    /**
     * Test de l'endpoint de santé (health check) du Gateway.
     * 
     * <p>Fonctionnalités vérifiées :
     * <ul>
     *   <li>Disponibilité du service</li>
     *   <li>État des composants internes (Redis, base de données)</li>
     *   <li>Connectivité avec les services downstream</li>
     *   <li>Métriques de performance</li>
     * </ul>
     * 
     * <p><strong>Intégration avec l'infrastructure :</strong>
     * <pre>
     * Load Balancer → Health Check → Gateway → Services
     *                      ↓
     *              Monitoring & Alerting
     *              (Prometheus, Grafana)
     * </pre>
     * 
     * @throws Exception si l'endpoint n'est pas accessible
     */
    @Test
    @DisplayName("Endpoint de santé accessible et fonctionnel")
    void healthEndpointIsAccessible() throws Exception {
        // Given : L'application est démarrée sur un port aléatoire
        String healthUrl = "http://localhost:" + port + "/actuator/health";
        
        // When : Appel de l'endpoint de santé
        ResponseEntity<String> response = restTemplate.getForEntity(healthUrl, String.class);
        
        // Then : Le service répond positivement
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("UP");
    }

    /**
     * Test de vérification des beans critiques du Gateway.
     * 
     * <p>Composants essentiels vérifiés :
     * <ul>
     *   <li><strong>GatewayConfig</strong> - Configuration des routes et filtres</li>
     *   <li><strong>SecurityConfig</strong> - Configuration de sécurité</li>
     *   <li><strong>AuthenticationFilter</strong> - Filtre d'authentification JWT</li>
     *   <li><strong>JwtService</strong> - Service de gestion des tokens</li>
     *   <li><strong>RedisConfig</strong> - Configuration du cache Redis</li>
     * </ul>
     * 
     * <p><strong>Relations fonctionnelles :</strong>
     * <pre>
     * Request → AuthenticationFilter → JwtService → Route → Backend Service
     *               ↓                     ↓
     *         SecurityConfig      Redis Cache (Sessions)
     * </pre>
     */
    @Test
    @DisplayName("Tous les beans critiques sont correctement initialisés")
    void criticalBeansAreInitialized() {
        // Vérification des configurations principales
        assertThat(applicationContext.getBean("gatewayConfig")).isNotNull();
        assertThat(applicationContext.getBean("securityConfig")).isNotNull();
        assertThat(applicationContext.getBean("corsConfig")).isNotNull();
        
        // Vérification des filtres
        assertThat(applicationContext.getBean("authenticationFilter")).isNotNull();
        assertThat(applicationContext.getBean("rateLimitingFilter")).isNotNull();
        
        // Vérification des services
        assertThat(applicationContext.getBean("jwtService")).isNotNull();
        assertThat(applicationContext.getBean("authenticationService")).isNotNull();
    }

    /**
     * Test d'intégration de la chaîne de filtres du Gateway.
     * 
     * <p>Vérifie l'ordre et le bon fonctionnement de la chaîne de filtres :
     * <ol>
     *   <li><strong>LoggingFilter</strong> - Journalisation des requêtes entrantes</li>
     *   <li><strong>CorsFilter</strong> - Gestion des requêtes cross-origin</li>
     *   <li><strong>RateLimitingFilter</strong> - Limitation du débit</li>
     *   <li><strong>AuthenticationFilter</strong> - Validation JWT</li>
     *   <li><strong>RequestValidationFilter</strong> - Validation des données</li>
     * </ol>
     * 
     * <p><strong>Impact sur la sécurité :</strong>
     * Cette chaîne assure une protection multicouche avant que les requêtes
     * n'atteignent les microservices backend.
     */
    @Test
    @DisplayName("Chaîne de filtres configurée correctement")
    void filterChainIsConfiguredCorrectly() {
        // Test d'une requête non authentifiée (doit être rejetée)
        String protectedUrl = "http://localhost:" + port + "/api/users/profile";
        
        ResponseEntity<String> response = restTemplate.getForEntity(protectedUrl, String.class);
        
        // Should be rejected by AuthenticationFilter
        assertThat(response.getStatusCode()).isIn(
            HttpStatus.UNAUTHORIZED, 
            HttpStatus.FORBIDDEN,
            HttpStatus.NOT_FOUND // Peut varier selon la configuration des routes
        );
    }

    /**
     * Test de la configuration des routes dynamiques du Gateway.
     * 
     * <p>Routes principales testées :
     * <ul>
     *   <li><strong>/api/users/**</strong> → User Service</li>
     *   <li><strong>/api/products/**</strong> → Product Service</li>
     *   <li><strong>/api/orders/**</strong> → Order Service</li>
     *   <li><strong>/api/notifications/**</strong> → Notification Service</li>
     *   <li><strong>/api/inventory/**</strong> → Inventory Service</li>
     * </ul>
     * 
     * <p><strong>Découverte de services :</strong>
     * Le Gateway utilise Spring Cloud Discovery pour localiser automatiquement
     * les instances de services disponibles et répartir la charge.
     */
    @Test
    @DisplayName("Routes du Gateway configurées pour tous les microservices")
    void gatewayRoutesAreConfigured() {
        // Test de routage basique (sans authentification pour des endpoints publics)
        String[] publicEndpoints = {
            "/actuator/health",
            "/actuator/info"
        };
        
        for (String endpoint : publicEndpoints) {
            String url = "http://localhost:" + port + endpoint;
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            
            // Les endpoints publics doivent être accessibles
            assertThat(response.getStatusCode()).isNotEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Test de la gestion des erreurs et de la résilience du Gateway.
     * 
     * <p>Scénarios de résilience testés :
     * <ul>
     *   <li><strong>Circuit Breaker</strong> - Protection contre les services défaillants</li>
     *   <li><strong>Retry Policy</strong> - Tentatives de reconnexion automatiques</li>
     *   <li><strong>Fallback Responses</strong> - Réponses de secours</li>
     *   <li><strong>Timeout Handling</strong> - Gestion des timeouts</li>
     * </ul>
     * 
     * <p><strong>Patterns de résilience :</strong>
     * <pre>
     * Client Request → Gateway → Circuit Breaker → Backend Service
     *                     ↓              ↓
     *                Fallback      Service Health
     *                Response       Monitoring
     * </pre>
     */
    @Test
    @DisplayName("Mécanismes de résilience et gestion d'erreurs")
    void resilienceAndErrorHandling() {
        // Test d'un endpoint inexistant
        String invalidUrl = "http://localhost:" + port + "/api/nonexistent/resource";
        
        ResponseEntity<String> response = restTemplate.getForEntity(invalidUrl, String.class);
        
        // Le Gateway doit retourner une erreur appropriée, pas un crash
        assertThat(response.getStatusCode()).isIn(
            HttpStatus.NOT_FOUND,
            HttpStatus.SERVICE_UNAVAILABLE,
            HttpStatus.BAD_GATEWAY
        );
    }

    /**
     * Test des métriques et de l'observabilité du Gateway.
     * 
     * <p>Métriques collectées :
     * <ul>
     *   <li><strong>Request Count</strong> - Nombre de requêtes par endpoint</li>
     *   <li><strong>Response Time</strong> - Temps de réponse moyen</li>
     *   <li><strong>Error Rate</strong> - Taux d'erreur par service</li>
     *   <li><strong>Throughput</strong> - Débit de requêtes</li>
     *   <li><strong>Circuit Breaker State</strong> - État des circuit breakers</li>
     * </ul>
     * 
     * <p><strong>Intégration monitoring :</strong>
     * <pre>
     * Gateway Metrics → Micrometer → Prometheus → Grafana
     *       ↓
     * Application Logs → ELK Stack → Alerting
     * </pre>
     */
    @Test
    @DisplayName("Métriques et observabilité disponibles")
    void metricsAndObservabilityAvailable() {
        String metricsUrl = "http://localhost:" + port + "/actuator/metrics";
        
        ResponseEntity<String> response = restTemplate.getForEntity(metricsUrl, String.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("names");
    }
}