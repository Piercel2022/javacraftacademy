package com.javacraftacademy.courseservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Classe principale de l'application Course Service de JavaCraft Academy.
 * 
 * Cette classe est le point d'entrée de l'application Spring Boot qui gère
 * l'ensemble des fonctionnalités liées aux cours, leçons, catégories et inscriptions.
 * 
 * <h2>Fonctionnalités principales :</h2>
 * <ul>
 *   <li>Gestion complète des cours (création, modification, suppression, consultation)</li>
 *   <li>Gestion des leçons et contenus pédagogiques</li>
 *   <li>Système de catégorisation des cours</li>
 *   <li>Gestion des inscriptions et suivi des étudiants</li>
 *   <li>Intégration avec les services externes (User Service, Notification Service)</li>
 *   <li>Messagerie asynchrone avec Apache Kafka</li>
 *   <li>Stockage et gestion des fichiers multimédias</li>
 * </ul>
 * 
 * <h2>Architecture et intégrations :</h2>
 * <p>
 * Cette application fait partie d'une architecture microservices et communique avec :
 * - User Service : pour la gestion des utilisateurs et authentification
 * - Notification Service : pour l'envoi de notifications
 * - Payment Service : pour la gestion des paiements via Kafka
 * </p>
 * 
 * <h2>Technologies utilisées :</h2>
 * <ul>
 *   <li>Spring Boot : Framework principal</li>
 *   <li>Spring Data JPA : Couche de persistance</li>
 *   <li>Spring Security : Sécurité et authentification</li>
 *   <li>Apache Kafka : Messagerie asynchrone</li>
 *   <li>OpenFeign : Communication inter-services</li>
 *   <li>Swagger/OpenAPI : Documentation API</li>
 *   <li>Flyway : Migration de base de données</li>
 * </ul>
 * 
 * <h2>Évolutions futures possibles :</h2>
 * <ul>
 *   <li>Ajout de la fonctionnalité de quiz et évaluations</li>
 *   <li>Système de certification automatique</li>
 *   <li>Intégration avec des plateformes de vidéoconférence</li>
 *   <li>Analytics avancés et reporting</li>
 *   <li>Système de recommandation de cours</li>
 * </ul>
 * 
 * @author JavaCraft Academy Team
 * @version 1.0
 * @since 2024
 */
@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.javacraftacademy.courseservice.repository")
@EntityScan(basePackages = "com.javacraftacademy.courseservice.model.entity")
@EnableFeignClients(basePackages = "com.javacraftacademy.courseservice.client")
@EnableKafka
@EnableAsync
@EnableTransactionManagement
public class CourseServiceApplication {

    /**
     * Point d'entrée principal de l'application Course Service.
     * 
     * Cette méthode initialise le contexte Spring Boot et démarre l'application
     * avec toutes ses configurations et dépendances.
     * 
     * @param args Arguments de la ligne de commande passés à l'application
     */
    public static void main(String[] args) {
        SpringApplication.run(CourseServiceApplication.class, args);
    }
}

/**
 * <h2>Explication des annotations utilisées :</h2>
 * 
 * <h3>@SpringBootApplication :</h3>
 * Annotation composite qui combine :
 * - @Configuration : Indique que cette classe contient des beans de configuration
 * - @EnableAutoConfiguration : Active la configuration automatique de Spring Boot
 * - @ComponentScan : Active le scan automatique des composants dans le package courant
 * 
 * <h3>@EnableJpaRepositories :</h3>
 * Active la détection automatique des interfaces repository JPA dans le package spécifié.
 * Permet à Spring Data JPA de créer automatiquement les implémentations des repositories.
 * 
 * <h3>@EntityScan :</h3>
 * Indique à Spring Boot où chercher les entités JPA. Nécessaire quand les entités
 * ne sont pas dans le même package que la classe principale.
 * 
 * <h3>@EnableFeignClients :</h3>
 * Active la fonctionnalité Feign Client pour la communication avec les services externes.
 * Permet la création automatique d'implémentations des interfaces client.
 * 
 * <h3>@EnableKafka :</h3>
 * Active la configuration Kafka pour la messagerie asynchrone.
 * Permet l'utilisation des annotations @KafkaListener et la configuration des producteurs.
 * 
 * <h3>@EnableAsync :</h3>
 * Active le support pour l'exécution asynchrone de méthodes avec @Async.
 * Améliore les performances en permettant l'exécution non-bloquante.
 * 
 * <h3>@EnableTransactionManagement :</h3>
 * Active la gestion déclarative des transactions avec @Transactional.
 * Essentiel pour maintenir la cohérence des données dans les opérations complexes.
 */