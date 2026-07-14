package dev.civicpulse.privacycompliance.config;

import dev.civicpulse.privacycompliance.domain.event.AccountDeletionRequested;
import dev.civicpulse.privacycompliance.domain.event.ConsentUpdated;
import dev.civicpulse.privacycompliance.domain.event.DataExportRequested;
import java.util.Map;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

@Configuration
public class KafkaProducerConfig {

  /** Symbolic type ids — see identity-service's KafkaProducerConfig for the full rationale.
   * Must stay in sync with docs/db/privacy-compliance-service/schema.sql's "Domain events
   * published" list. */
  private static final String TYPE_MAPPINGS =
      "ConsentUpdated:" + ConsentUpdated.class.getName() + "," //
          + "DataExportRequested:" + DataExportRequested.class.getName() + "," //
          + "AccountDeletionRequested:" + AccountDeletionRequested.class.getName();

  @Bean
  public ProducerFactory<String, Object> producerFactory(@Value("${spring.kafka.bootstrap-servers}") String bootstrapServers) {
    Map<String, Object> configProps =
        Map.of(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class,
            ProducerConfig.ACKS_CONFIG, "all",
            ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true,
            JsonSerializer.TYPE_MAPPINGS, TYPE_MAPPINGS);
    return new DefaultKafkaProducerFactory<>(configProps);
  }

  @Bean
  public KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> producerFactory) {
    return new KafkaTemplate<>(producerFactory);
  }
}
