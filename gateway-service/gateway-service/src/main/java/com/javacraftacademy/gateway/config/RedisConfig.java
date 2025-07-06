package com.javacraftacademy.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.SocketOptions;

import java.time.Duration;

/**
 * Configuration Redis pour le service Gateway.
 * 
 * Cette classe configure la connexion et les opérations Redis pour le service de passerelle.
 * Redis est utilisé pour :
 * - Le cache des tokens JWT et des sessions utilisateur
 * - La limitation de débit (rate limiting) par utilisateur/IP
 * - Le stockage temporaire des données de session
 * - Le cache des réponses API fréquemment utilisées
 * - La synchronisation entre instances de gateway (mode cluster)
 * 
 * Relations avec l'application :
 * - Utilisé par RateLimitingFilter pour la limitation de débit
 * - Intégré avec AuthenticationService pour le cache des tokens
 * - Support du cache distributed pour la scalabilité horizontale
 * - Stockage des métriques et statistiques d'utilisation
 * 
 * Fonctionnalités principales :
 * - Configuration de pool de connexions optimisée
 * - Sérialisation JSON pour les objets complexes
 * - Gestion des timeouts et reconnexions automatiques
 * - Support des transactions Redis
 * - Configuration différente par environnement
 * 
 * @author JavaCraft Academy
 * @version 1.0
 * @since 2025
 */
@Configuration
public class RedisConfig {

    /**
     * Adresse du serveur Redis.
     * Par défaut : localhost
     */
    @Value("${spring.redis.host:localhost}")
    private String redisHost;

    /**
     * Port du serveur Redis.
     * Par défaut : 6379
     */
    @Value("${spring.redis.port:6379}")
    private int redisPort;

    /**
     * Mot de passe Redis (optionnel).
     * Utilisé en production pour sécuriser l'accès
     */
    @Value("${spring.redis.password:}")
    private String redisPassword;

    /**
     * Base de données Redis à utiliser.
     * Par défaut : 0
     */
    @Value("${spring.redis.database:0}")
    private int redisDatabase;

    /**
     * Timeout de connexion Redis (en millisecondes).
     * Par défaut : 2000ms
     */
    @Value("${spring.redis.timeout:2000}")
    private int connectionTimeout;

    /**
     * Timeout de commande Redis (en millisecondes).
     * Par défaut : 1000ms
     */
    @Value("${spring.redis.command-timeout:1000}")
    private int commandTimeout;

    /**
     * Taille maximale du pool de connexions.
     * Par défaut : 20
     */
    @Value("${spring.redis.lettuce.pool.max-active:20}")
    private int maxActive;

    /**
     * Nombre maximum de connexions inactives dans le pool.
     * Par défaut : 10
     */
    @Value("${spring.redis.lettuce.pool.max-idle:10}")
    private int maxIdle;

    /**
     * Nombre minimum de connexions inactives dans le pool.
     * Par défaut : 2
     */
    @Value("${spring.redis.lettuce.pool.min-idle:2}")
    private int minIdle;

    /**
     * Temps d'attente maximum pour obtenir une connexion (en millisecondes).
     * Par défaut : 3000ms
     */
    @Value("${spring.redis.lettuce.pool.max-wait:3000}")
    private long maxWait;

    /**
     * Configure la factory de connexion Redis avec Lettuce.
     * 
     * Cette méthode configure la connexion Redis avec :
     * - Pool de connexions optimisé
     * - Gestion des timeouts
     * - Reconnexion automatique
     * - Configuration SSL si nécessaire
     * 
     * @return RedisConnectionFactory Factory de connexion Redis configurée
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        // Configuration de base Redis
        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration();
        redisConfig.setHostName(redisHost);
        redisConfig.setPort(redisPort);
        redisConfig.setDatabase(redisDatabase);
        
        // Configuration du mot de passe si fourni
        if (redisPassword != null && !redisPassword.trim().isEmpty()) {
            redisConfig.setPassword(redisPassword);
        }

        // Configuration des options client
        ClientOptions clientOptions = ClientOptions.builder()
            .socketOptions(SocketOptions.builder()
                .connectTimeout(Duration.ofMillis(connectionTimeout))
                .build())
            .build();

        // Configuration du pool de connexions Lettuce
        LettucePoolingClientConfiguration poolConfig = LettucePoolingClientConfiguration.builder()
            .poolConfig(jedisPoolConfig())
            .clientOptions(clientOptions)
            .commandTimeout(Duration.ofMillis(commandTimeout))
            .shutdownTimeout(Duration.ofMillis(200))
            .build();

        return new LettuceConnectionFactory(redisConfig, poolConfig);
    }

    /**
     * Configure le pool de connexions Jedis.
     * 
     * @return GenericObjectPoolConfig Configuration du pool
     */
    private org.apache.commons.pool2.impl.GenericObjectPoolConfig jedisPoolConfig() {
        org.apache.commons.pool2.impl.GenericObjectPoolConfig poolConfig = 
            new org.apache.commons.pool2.impl.GenericObjectPoolConfig();
        
        poolConfig.setMaxTotal(maxActive);
        poolConfig.setMaxIdle(maxIdle);
        poolConfig.setMinIdle(minIdle);
        poolConfig.setMaxWaitMillis(maxWait);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);
        poolConfig.setTestWhileIdle(true);
        poolConfig.setBlockWhenExhausted(true);
        
        return poolConfig;
    }

    /**
     * Configure le RedisTemplate principal pour les opérations génériques.
     * 
     * Ce template utilise une sérialisation JSON pour les valeurs et
     * une sérialisation String pour les clés, optimisé pour le cache
     * d'objets complexes comme les tokens JWT et les données utilisateur.
     * 
     * @param connectionFactory Factory de connexion Redis
     * @return RedisTemplate Template Redis configuré
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Configuration de la sérialisation
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer();

        // Sérialisation des clés
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);

        // Sérialisation des valeurs
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        // Active les transactions
        template.setEnableTransactionSupport(true);

        template.afterPropertiesSet();
        return template;
    }

    /**
     * Configure un StringRedisTemplate pour les opérations simples.
     * 
     * Optimisé pour les opérations de rate limiting et de cache simple
     * où seules des chaînes de caractères sont manipulées.
     * 
     * @param connectionFactory Factory de connexion Redis
     * @return StringRedisTemplate Template Redis pour les strings
     */
    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(connectionFactory);
        template.setEnableTransactionSupport(true);
        return template;
    }

    /**
     * Configure un RedisTemplate spécialisé pour le cache des tokens JWT.
     * 
     * Ce template est optimisé pour le stockage et la récupération rapide
     * des tokens JWT avec une sérialisation adaptée.
     * 
     * @param connectionFactory Factory de connexion Redis
     * @return RedisTemplate Template spécialisé pour les JWT
     */
    @Bean("jwtRedisTemplate")
    public RedisTemplate<String, String> jwtRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setValueSerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setHashValueSerializer(stringSerializer);

        template.setEnableTransactionSupport(true);
        template.afterPropertiesSet();
        return template;
    }

    /**
     * Configure un RedisTemplate pour les données de session utilisateur.
     * 
     * Utilise une sérialisation JSON optimisée pour les objets utilisateur
     * et les données de session complexes.
     * 
     * @param connectionFactory Factory de connexion Redis
     * @return RedisTemplate Template pour les sessions
     */
    @Bean("sessionRedisTemplate")
    public RedisTemplate<String, Object> sessionRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Configuration d'un ObjectMapper personnalisé pour les sessions
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.activateDefaultTyping(
            LaissezFaireSubTypeValidator.instance, 
            ObjectMapper.DefaultTyping.NON_FINAL
        );

        Jackson2JsonRedisSerializer<Object> jsonSerializer = 
            new Jackson2JsonRedisSerializer<>(Object.class);
        jsonSerializer.setObjectMapper(objectMapper);

        StringRedisSerializer stringSerializer = new StringRedisSerializer();

        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.setEnableTransactionSupport(true);
        template.afterPropertiesSet();
        return template;
    }

    /**
     * Retourne les informations de configuration Redis pour le monitoring.
     * 
     * Méthode utilitaire pour obtenir les détails de configuration Redis
     * sans exposer les informations sensibles comme les mots de passe.
     * 
     * @return String Informations de configuration Redis
     */
    public String getRedisConfigurationInfo() {
        return String.format(
            "Redis Configuration - Host: %s:%d, Database: %d, " +
            "Pool[Max: %d, MaxIdle: %d, MinIdle: %d], " +
            "Timeouts[Connection: %dms, Command: %dms]",
            redisHost, redisPort, redisDatabase,
            maxActive, maxIdle, minIdle,
            connectionTimeout, commandTimeout
        );
    }

    /**
     * Vérifie la connectivité Redis.
     * 
     * Méthode utilitaire pour vérifier la santé de la connexion Redis.
     * Utilisée par les health checks et le monitoring.
     * 
     * @param redisTemplate Template Redis à tester
     * @return boolean true si Redis est accessible, false sinon
     */
    public boolean isRedisAvailable(RedisTemplate<String, Object> redisTemplate) {
        try {
            redisTemplate.getConnectionFactory().getConnection().ping();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Configure les préfixes de clés Redis pour l'organisation des données.
     * 
     * Définit les préfixes utilisés pour organiser les différents types de données
     * dans Redis, facilitant la maintenance et évitant les collisions de clés.
     * 
     * @return RedisKeyPrefixConfig Configuration des préfixes de clés
     */
    @Bean
    public RedisKeyPrefixConfig redisKeyPrefixConfig() {
        RedisKeyPrefixConfig config = new RedisKeyPrefixConfig();
        config.setJwtPrefix("gateway:jwt:");
        config.setSessionPrefix("gateway:session:");
        config.setRateLimitPrefix("gateway:rate:");
        config.setCachePrefix("gateway:cache:");
        config.setMetricsPrefix("gateway:metrics:");
        return config;
    }

    /**
     * Configuration des préfixes de clés Redis.
     * 
     * Classe interne pour définir les préfixes utilisés dans Redis.
     */
    public static class RedisKeyPrefixConfig {
        private String jwtPrefix;
        private String sessionPrefix;
        private String rateLimitPrefix;
        private String cachePrefix;
        private String metricsPrefix;

        public String getJwtPrefix() { return jwtPrefix; }
        public void setJwtPrefix(String jwtPrefix) { this.jwtPrefix = jwtPrefix; }

        public String getSessionPrefix() { return sessionPrefix; }
        public void setSessionPrefix(String sessionPrefix) { this.sessionPrefix = sessionPrefix; }

        public String getRateLimitPrefix() { return rateLimitPrefix; }
        public void setRateLimitPrefix(String rateLimitPrefix) { this.rateLimitPrefix = rateLimitPrefix; }

        public String getCachePrefix() { return cachePrefix; }
        public void setCachePrefix(String cachePrefix) { this.cachePrefix = cachePrefix; }

        public String getMetricsPrefix() { return metricsPrefix; }
        public void setMetricsPrefix(String metricsPrefix) { this.metricsPrefix = metricsPrefix; }
    }
}