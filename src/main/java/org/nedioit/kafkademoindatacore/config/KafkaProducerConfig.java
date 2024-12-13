package org.nedioit.kafkademoindatacore.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.security.plain.PlainLoginModule;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.*;

@Configuration
@PropertySource("${spring.config.name}")
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${ACKS_CONFIG}")
    private String acks_config;

    @Value("${topic}")
    private String topic;

    @Value("${username}")
    private String username;

    @Value("${password}")
    private String password;

    public Map<String, Object> producerConfig(){
        Map<String,Object> props = new HashMap<>();
        // Spécification du serveur bootstrap
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.ACKS_CONFIG, acks_config);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        // Configuration SASL SCRAM-SHA-512
        props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_PLAINTEXT");
        props.put(SaslConfigs.SASL_MECHANISM, "SCRAM-SHA-512");
        props.put(SaslConfigs.SASL_JAAS_CONFIG,
                "org.apache.kafka.common.security.scram.ScramLoginModule required username='" + username + "' password='" + password + "';");
        return props;
    }

    // ProducerFactory => fabriquant de producer
    @Bean
    public ProducerFactory<String, String> producerFactory(){
        return new DefaultKafkaProducerFactory<>(producerConfig());
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate(
            ProducerFactory<String, String> producerFactory
    ){
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public CommandLineRunner kafkaMessageSender(KafkaTemplate<String, String> kafkaTemplate) {
        return args -> {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                // Déclaration du JSON directement dans le code
                String jsonString = "[\n" +
                        "  {\n" +
                        "    \"id\": 1,\n" +
                        "    \"name\": \"John Doe\",\n" +
                        "    \"age\": 30,\n" +
                        "    \"city\": \"New York\"\n" +
                        "  }\n" +
                        "]"; // Remplacer le JSON de pushNotification

                // Convertir la chaîne JSON en un JsonNode
                JsonNode jsonNode = objectMapper.readTree(jsonString);

                jsonNode.forEach(messageNode -> {
                    String key = UUID.randomUUID().toString();
                    String message = messageNode.toString();

                    // Envoyer le message avec un log simple
                    kafkaTemplate.send(topic, key, message);
                    System.out.println("Message envoyé : " + message);
                });
            } catch (Exception e) {
                System.err.println("Erreur lors de la conversion ou de l'envoi du message : " + e.getMessage());
            }
        };
    }
}
