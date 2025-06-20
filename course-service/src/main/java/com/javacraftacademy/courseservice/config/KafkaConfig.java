package com.javacraftacademy.courseservice.config;

// Imports Spring Framework - Framework principal pour l'injection de dépendances et la configuration
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Imports Kafka Core - Classes principales pour la configuration Kafka
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

// Imports Spring Kafka - Intégration Spring avec Apache Kafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;

// Imports pour la gestion des erreurs et retry
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

// Imports pour logging
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Imports Java standard
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration Apache Kafka pour l'application JavaCraftAcademy
 * 
 * <p>Cette classe centralise toute la configuration Kafka nécessaire au bon fonctionnement
 * de l'application JavaCraftAcademy. Elle gère :</p>
 * 
 * <h3>Fonctionnalités principales :</h3>
 * <ul>
 *   <li><strong>Configuration des Producers</strong> : Pour l'envoi de messages vers les topics Kafka</li>
 *   <li><strong>Configuration des Consumers</strong> : Pour la consommation de messages depuis les topics</li>
 *   <li><strong>Sérialisation/Désérialisation</strong> : Support JSON et String pour les messages</li>
 *   <li><strong>Gestion des erreurs</strong> : Mécanisme de retry et de gestion d'erreurs robuste</li>
 *   <li><strong>Configuration des listeners</strong> : Container factory pour les consumers annotés @KafkaListener</li>
 * </ul>
 * 
 * <h3>Relations avec l'application JavaCraftAcademy :</h3>
 * <ul>
 *   <li><strong>Services métier</strong> : Les services utilisent KafkaTemplate pour publier des événements</li>
 *   <li><strong>Event-driven architecture</strong> : Support des événements asynchrones (inscriptions, notifications, etc.)</li>
 *   <li><strong>Microservices communication</strong> : Communication inter-services via Kafka</li>
 *   <li><strong>Audit et logging</strong> : Traçabilité des actions utilisateur via événements Kafka</li>
 *   <li><strong>Notifications</strong> : Système de notifications push basé sur Kafka</li>
 * </ul>
 * 
 * <h3>Topics principaux utilisés :</h3>
 * <ul>
 *   <li><code>user-registration</code> : Événements d'inscription des utilisateurs</li>
 *   <li><code>course-enrollment</code> : Événements d'inscription aux cours</li>
 *   <li><code>notifications</code> : Messages de notification</li>
 *   <li><code>audit-events</code> : Événements d'audit système</li>
 * </ul>
 * 
 * <h3>Extension future :</h3>
 * <p>Pour ajouter de nouvelles fonctionnalités, vous pouvez :</p>
 * <ul>
 *   <li>Ajouter de nouveaux beans Producer/Consumer avec des configurations spécifiques</li>
 *   <li>Implémenter des deserializers personnalisés pour de nouveaux types de messages</li>
 *   <li>Configurer des partitioning strategies personnalisées</li>
 *   <li>Ajouter des interceptors pour le monitoring et la sécurité</li>
 * </ul>
 * 
 * @author JavaCraftAcademy Team
 * @version 1.0
 * @since 2025
 */
@Configuration
public class KafkaConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(KafkaConfig.class);
    
    // Configuration depuis application.properties/yml
    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;
    
    @Value("${spring.kafka.consumer.group-id:javacraftacademy-group}")
    private String groupId;
    
    @Value("${spring.kafka.consumer.auto-offset-reset:earliest}")
    private String autoOffsetReset;
    
    @Value("${spring.kafka.consumer.enable-auto-commit:false}")
    private boolean enableAutoCommit;

    /**
     * Configuration du Producer Kafka
     * 
     * <p>Le Producer est responsable de l'envoi des messages vers les topics Kafka.
     * Cette configuration optimise les performances et la fiabilité pour l'application.</p>
     * 
     * <h4>Implémentation actuelle :</h4>
     * <ul>
     *   <li>Sérialisation String pour les clés</li>
     *   <li>Sérialisation JSON pour les valeurs</li>
     *   <li>Configuration optimisée pour la latence et la fiabilité</li>
     * </ul>
     * 
     * @return Map contenant la configuration du producer
     */
    @Bean
    public Map<String, Object> producerConfigs() {
        Map<String, Object> props = new HashMap<>();
        
        // Configuration de base
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        
        // Configuration pour la performance et la fiabilité
        props.put(ProducerConfig.ACKS_CONFIG, "all"); // Attendre confirmation de tous les replicas
        props.put(ProducerConfig.RETRIES_CONFIG, 3); // Nombre de tentatives en cas d'échec
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384); // Taille du batch pour optimiser le débit
        props.put(ProducerConfig.LINGER_MS_CONFIG, 5); // Délai d'attente pour grouper les messages
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432); // 32MB de buffer
        
        // Configuration de compression pour optimiser le réseau
        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");
        
        // Configuration pour l'idempotence (éviter les doublons)
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        
        logger.info("Configuration Producer Kafka initialisée avec bootstrap-servers: {}", bootstrapServers);
        return props;
    }

    /**
     * Factory pour créer des Producer Kafka
     * 
     * <p>ProducerFactory utilise la configuration définie dans producerConfigs()
     * pour créer des instances de Producer Kafka optimisées.</p>
     * 
     * @return ProducerFactory configurée
     */
    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    /**
     * Template Kafka pour l'envoi de messages
     * 
     * <p>KafkaTemplate est l'interface principale utilisée par les services
     * de l'application pour envoyer des messages vers Kafka.</p>
     * 
     * <h4>Utilisation dans l'application :</h4>
     * <pre>{@code
     * @Autowired
     * private KafkaTemplate<String, Object> kafkaTemplate;
     * 
     * public void sendUserRegistrationEvent(UserRegistrationEvent event) {
     *     kafkaTemplate.send("user-registration", event);
     * }
     * }</pre>
     * 
     * @return KafkaTemplate configuré
     */
    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        KafkaTemplate<String, Object> template = new KafkaTemplate<>(producerFactory());
        
        // Configuration des callbacks pour le monitoring
        template.setDefaultTopic("default-topic");
        
        logger.info("KafkaTemplate initialisé et prêt pour l'envoi de messages");
        return template;
    }

    /**
     * Configuration du Consumer Kafka
     * 
     * <p>Le Consumer est responsable de la consommation des messages depuis les topics Kafka.
     * Cette configuration garantit une consommation fiable et performante.</p>
     * 
     * <h4>Implémentation actuelle :</h4>
     * <ul>
     *   <li>Désérialisation String pour les clés</li>
     *   <li>Désérialisation JSON pour les valeurs avec gestion d'erreurs</li>
     *   <li>Commit manuel pour un contrôle précis</li>
     * </ul>
     * 
     * @return Map contenant la configuration du consumer
     */
    @Bean
    public Map<String, Object> consumerConfigs() {
        Map<String, Object> props = new HashMap<>();
        
        // Configuration de base
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, enableAutoCommit);
        
        // Configuration des deserializers avec gestion d'erreurs
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class.getName());
        
        // Configuration JsonDeserializer
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.javacraftacademy.*");
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "com.javacraftacademy.dto.GenericMessage");
        
        // Configuration pour la performance
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 500); // Nombre max de records par poll
        props.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, 1); // Taille minimum à fetcher
        props.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, 500); // Délai max d'attente pour fetch
        
        logger.info("Configuration Consumer Kafka initialisée pour le groupe: {}", groupId);
        return props;
    }

    /**
     * Factory pour créer des Consumer Kafka
     * 
     * <p>ConsumerFactory utilise la configuration définie dans consumerConfigs()
     * pour créer des instances de Consumer Kafka optimisées.</p>
     * 
     * @return ConsumerFactory configurée
     */
    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        return new DefaultKafkaConsumerFactory<>(consumerConfigs());
    }

    /**
     * Container Factory pour les Kafka Listeners
     * 
     * <p>Cette factory configure les containers qui hébergent les méthodes
     * annotées avec @KafkaListener dans l'application.</p>
     * 
     * <h4>Fonctionnalités configurées :</h4>
     * <ul>
     *   <li>Gestion des erreurs avec retry automatique</li>
     *   <li>Commit manuel pour un contrôle précis</li>
     *   <li>Concurrence configurable</li>
     * </ul>
     * 
     * <h4>Utilisation dans l'application :</h4>
     * <pre>{@code
     * @KafkaListener(topics = "user-registration", groupId = "javacraftacademy-group")
     * public void handleUserRegistration(UserRegistrationEvent event) {
     *     // Traitement de l'événement
     * }
     * }</pre>
     * 
     * @return ConcurrentKafkaListenerContainerFactory configurée
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = 
            new ConcurrentKafkaListenerContainerFactory<>();
        
        factory.setConsumerFactory(consumerFactory());
        
        // Configuration du container
        factory.setConcurrency(3); // Nombre de threads consumers
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        
        // Configuration de la gestion d'erreurs avec retry
        factory.setCommonErrorHandler(new DefaultErrorHandler(
            new FixedBackOff(1000L, 3L))); // 3 tentatives avec 1 seconde d'intervalle
        
        logger.info("KafkaListenerContainerFactory initialisée avec concurrence: 3");
        return factory;
    }

    /**
     * Container Factory spécialisée pour les messages JSON
     * 
     * <p>Factory dédiée au traitement des messages JSON complexes avec
     * une configuration spécialisée pour les DTOs métier.</p>
     * 
     * <h4>Extensions futures possibles :</h4>
     * <ul>
     *   <li>Validation automatique des DTOs</li>
     *   <li>Transformation des messages</li>
     *   <li>Routage conditionnel</li>
     * </ul>
     * 
     * @return ConcurrentKafkaListenerContainerFactory pour JSON
     */
    @Bean("jsonKafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, Object> jsonKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = 
            new ConcurrentKafkaListenerContainerFactory<>();
        
        // Configuration spécialisée pour JSON
        Map<String, Object> jsonConsumerProps = new HashMap<>(consumerConfigs());
        jsonConsumerProps.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        jsonConsumerProps.put(JsonDeserializer.REMOVE_TYPE_INFO_HEADERS, false);
        
        ConsumerFactory<String, Object> jsonConsumerFactory = 
            new DefaultKafkaConsumerFactory<>(jsonConsumerProps);
        
        factory.setConsumerFactory(jsonConsumerFactory);
        factory.setConcurrency(2);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        
        logger.info("JsonKafkaListenerContainerFactory initialisée pour les messages JSON complexes");
        return factory;
    }

    /*
     * =================================================================================
     * MÉTHODES D'EXTENSION POUR FONCTIONNALITÉS FUTURES
     * =================================================================================
     * 
     * Les méthodes ci-dessous montrent comment étendre cette configuration
     * pour ajouter de nouvelles fonctionnalités à l'avenir.
     */

    /**
     * EXEMPLE : Configuration Producer pour messages à haute fréquence
     * 
     * <p>Cette méthode montre comment créer un Producer optimisé pour
     * l'envoi de messages à haute fréquence (analytics, logs, etc.)</p>
     * 
     * Décommentez et adaptez selon vos besoins futurs.
     */
    /*
    @Bean("highThroughputKafkaTemplate")
    public KafkaTemplate<String, Object> highThroughputKafkaTemplate() {
        Map<String, Object> props = new HashMap<>(producerConfigs());
        
        // Optimisations pour haut débit
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 65536); // Batch plus grand
        props.put(ProducerConfig.LINGER_MS_CONFIG, 100); // Attente plus longue
        props.put(ProducerConfig.ACKS_CONFIG, "1"); // Moins de garanties mais plus rapide
        
        ProducerFactory<String, Object> factory = new DefaultKafkaProducerFactory<>(props);
        return new KafkaTemplate<>(factory);
    }
    */

    /**
     * EXEMPLE : Configuration Consumer pour traitement par batch
     * 
     * <p>Cette méthode montre comment créer un Consumer configuré pour
     * traiter les messages par lot (batch processing)</p>
     */
    /*
    @Bean("batchKafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, Object> batchKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = 
            new ConcurrentKafkaListenerContainerFactory<>();
        
        Map<String, Object> batchConsumerProps = new HashMap<>(consumerConfigs());
        batchConsumerProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 1000);
        
        ConsumerFactory<String, Object> batchConsumerFactory = 
            new DefaultKafkaConsumerFactory<>(batchConsumerProps);
        
        factory.setConsumerFactory(batchConsumerFactory);
        factory.setBatchListener(true); // Active le mode batch
        factory.setConcurrency(1);
        
        return factory;
    }
    */
}