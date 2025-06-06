package com.javacraftacademy.courseservice.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Properties;

/**
 * Configuration de la base de données pour le service Course de JavaCraft Academy.
 * 
 * Cette classe centralise toute la configuration liée à la persistance des données,
 * incluant la configuration du pool de connexions, de l'EntityManager et des transactions.
 * 
 * <h2>Fonctionnalités fournies :</h2>
 * <ul>
 *   <li>Configuration optimisée du pool de connexions avec HikariCP</li>
 *   <li>Configuration de l'EntityManagerFactory pour JPA/Hibernate</li>
 *   <li>Gestion des transactions avec PlatformTransactionManager</li>
 *   <li>Audit automatique des entités (création/modification)</li>
 *   <li>Configuration spécifique selon l'environnement (dev, prod, test)</li>
 * </ul>
 * 
 * <h2>Intégration dans l'écosystème :</h2>
 * <p>
 * Cette configuration est utilisée par tous les repositories du service pour :
 * - Stocker les données des cours, leçons, catégories et inscriptions
 * - Maintenir la cohérence transactionnelle lors des opérations complexes
 * - Optimiser les performances d'accès aux données
 * </p>
 * 
 * <h2>Optimisations incluses :</h2>
 * <ul>
 *   <li>Pool de connexions optimisé selon l'environnement</li>
 *   <li>Configuration Hibernate pour de meilleures performances</li>
 *   <li>Gestion automatique des audits (createdDate, lastModifiedDate)</li>
 *   <li>Configuration des timeouts et retry policies</li>
 * </ul>
 * 
 * <h2>Évolutions futures :</h2>
 * <ul>
 *   <li>Support multi-base de données (lecture/écriture séparées)</li>
 *   <li>Configuration pour la réplication de base de données</li>
 *   <li>Métriques avancées de performance de base de données</li>
 *   <li>Support pour les bases de données NoSQL complémentaires</li>
 * </ul>
 * 
 * @author JavaCraft Academy Team
 * @version 1.0
 * @since 2024
 */
@Configuration
@EnableTransactionManagement
@EnableJpaAuditing
public class DatabaseConfig {

    @Value("${spring.datasource.url}")
    private String jdbcUrl;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName;

    @Value("${spring.jpa.hibernate.ddl-auto:validate}")
    private String hibernateDdlAuto;

    @Value("${spring.jpa.show-sql:false}")
    private boolean showSql;

    @Value("${spring.jpa.properties.hibernate.format_sql:false}")
    private boolean formatSql;

    /**
     * Configuration du DataSource principal avec HikariCP.
     * 
     * HikariCP est le pool de connexions le plus performant pour Java.
     * Cette configuration optimise les performances selon l'environnement.
     * 
     * <h3>Avantages de HikariCP :</h3>
     * <ul>
     *   <li>Performance supérieure (jusqu'à 20x plus rapide que d'autres pools)</li>
     *   <li>Faible overhead mémoire</li>
     *   <li>Détection automatique des connexions fermées</li>
     *   <li>Métriques intégrées</li>
     * </ul>
     * 
     * @return DataSource configuré avec HikariCP
     */
    @Bean
    @Primary
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        
        // Configuration de base
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName(driverClassName);
        
        // Configuration du pool - Optimisée pour un service de cours
        config.setMaximumPoolSize(20); // Taille maximale du pool
        config.setMinimumIdle(5);      // Connexions minimales maintenues
        config.setConnectionTimeout(30000); // 30 secondes timeout
        config.setIdleTimeout(600000);      // 10 minutes idle timeout
        config.setMaxLifetime(1800000);     // 30 minutes max lifetime
        config.setLeakDetectionThreshold(60000); // Détection de fuites à 60s
        
        // Optimisations spécifiques
        config.setConnectionTestQuery("SELECT 1");
        config.setPoolName("CourseServicePool");
        
        // Configuration JMX pour monitoring
        config.setRegisterMbeans(true);
        
        return new HikariDataSource(config);
    }

    /**
     * Configuration de l'EntityManagerFactory pour JPA/Hibernate.
     * 
     * Cette configuration définit comment les entités JPA sont gérées
     * et comment Hibernate interagit avec la base de données.
     * 
     * @param dataSource Le DataSource à utiliser
     * @return EntityManagerFactory configuré
     */
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("com.javacraftacademy.courseservice.model.entity");
        
        // Configuration du vendor adapter Hibernate
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setGenerateDdl(false); // Flyway gère le DDL
        vendorAdapter.setShowSql(showSql);
        em.setJpaVendorAdapter(vendorAdapter);
        
        // Propriétés Hibernate spécifiques
        em.setJpaProperties(hibernateProperties());
        
        return em;
    }

    /**
     * Configuration du gestionnaire de transactions.
     * 
     * Le PlatformTransactionManager gère les transactions JPA et assure
     * la cohérence des données lors des opérations complexes.
     * 
     * @param entityManagerFactory Factory pour les EntityManager
     * @return Gestionnaire de transactions configuré
     */
    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory);
        
        // Configuration pour de meilleures performances
        transactionManager.setDefaultTimeout(30); // 30 secondes par défaut
        transactionManager.setRollbackOnCommitFailure(true);
        
        return transactionManager;
    }

    /**
     * Configuration des propriétés Hibernate pour optimiser les performances.
     * 
     * Ces propriétés configurent le comportement d'Hibernate selon
     * les besoins spécifiques du service de cours.
     * 
     * @return Properties configurées pour Hibernate
     */
    private Properties hibernateProperties() {
        Properties properties = new Properties();
        
        // Configuration de base
        properties.setProperty("hibernate.hbm2ddl.auto", hibernateDdlAuto);
        properties.setProperty("hibernate.show_sql", String.valueOf(showSql));
        properties.setProperty("hibernate.format_sql", String.valueOf(formatSql));
        
        // Optimisations de performance
        properties.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQL95Dialect");
        properties.setProperty("hibernate.jdbc.batch_size", "25");
        properties.setProperty("hibernate.order_inserts", "true");
        properties.setProperty("hibernate.order_updates", "true");
        properties.setProperty("hibernate.jdbc.batch_versioned_data", "true");
        
        // Configuration du cache de second niveau (si activé)
        properties.setProperty("hibernate.cache.use_second_level_cache", "false");
        properties.setProperty("hibernate.cache.use_query_cache", "false");
        
        // Configuration de la génération des statistiques
        properties.setProperty("hibernate.generate_statistics", "false");
        
        // Configuration spécifique pour les LOB (Large Objects)
        properties.setProperty("hibernate.jdbc.lob.non_contextual_creation", "true");
        
        return properties;
    }

    /**
     * Configuration spécifique pour l'environnement de développement.
     * 
     * En développement, on active des fonctionnalités de debug
     * et on utilise des paramètres moins stricts.
     */
    @Configuration
    @Profile("dev")
    static class DevelopmentDatabaseConfig {
        
        @Bean
        @Primary
        public DataSource devDataSource(@Value("${spring.datasource.url}") String jdbcUrl,
                                       @Value("${spring.datasource.username}") String username,
                                       @Value("${spring.datasource.password}") String password,
                                       @Value("${spring.datasource.driver-class-name}") String driverClassName) {
            
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(jdbcUrl);
            config.setUsername(username);
            config.setPassword(password);
            config.setDriverClassName(driverClassName);
            
            // Configuration optimisée pour le développement
            config.setMaximumPoolSize(5);
            config.setMinimumIdle(2);
            config.setConnectionTimeout(20000);
            config.setLeakDetectionThreshold(30000);
            config.setPoolName("CourseServiceDevPool");
            
            return new HikariDataSource(config);
        }
    }

    /**
     * Configuration spécifique pour l'environnement de production.
     * 
     * En production, on optimise pour la performance et la robustesse
     * avec des paramètres plus stricts et du monitoring avancé.
     */
    @Configuration
    @Profile("prod")
    static class ProductionDatabaseConfig {
        
        @Bean
        @Primary
        public DataSource prodDataSource(@Value("${spring.datasource.url}") String jdbcUrl,
                                        @Value("${spring.datasource.username}") String username,
                                        @Value("${spring.datasource.password}") String password,
                                        @Value("${spring.datasource.driver-class-name}") String driverClassName) {
            
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(jdbcUrl);
            config.setUsername(username);
            config.setPassword(password);
            config.setDriverClassName(driverClassName);
            
            // Configuration optimisée pour la production
            config.setMaximumPoolSize(50);
            config.setMinimumIdle(10);
            config.setConnectionTimeout(30000);
            config.setIdleTimeout(300000);
            config.setMaxLifetime(1200000);
            config.setLeakDetectionThreshold(60000);
            config.setPoolName("CourseServiceProdPool");
            
            // Activation du monitoring JMX
            config.setRegisterMbeans(true);
            
            return new HikariDataSource(config);
        }
    }
}

/**
 * <h2>Classes importées et leur utilité :</h2>
 * 
 * <h3>HikariConfig & HikariDataSource :</h3>
 * Pool de connexions haute performance. Le plus rapide disponible pour Java.
 * Utilisé pour gérer efficacement les connexions à la base de données.
 * 
 * <h3>LocalContainerEntityManagerFactoryBean :</h3>
 * Factory Spring pour créer des EntityManager JPA. Intègre JPA dans le contexte Spring.
 * 
 * <h3>HibernateJpaVendorAdapter :</h3>
 * Adaptateur pour configurer Hibernate comme implémentation JPA.
 * 
 * <h3>JpaTransactionManager :</h3>
 * Gestionnaire de transactions spécialisé pour JPA. Assure la cohérence des données.
 * 
 * <h3>@EnableJpaAuditing :</h3>
 * Active l'audit automatique des entités (dates de création/modification).
 * 
 * <h3>@Value :</h3>
 * Injecte les valeurs de configuration depuis application.yml.
 * 
 * <h3>@Profile :</h3>
 * Permet une configuration conditionnelle selon l'environnement.
 */