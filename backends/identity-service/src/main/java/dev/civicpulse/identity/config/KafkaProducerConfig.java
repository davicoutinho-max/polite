package dev.civicpulse.identity.config;

import dev.civicpulse.identity.domain.event.AccountRegistered;
import dev.civicpulse.identity.domain.event.AccountVerified;
import dev.civicpulse.identity.domain.event.SessionIssued;
import dev.civicpulse.identity.domain.event.SessionRevoked;
import java.util.Map;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

@Configuration
public class KafkaProducerConfig {

  /** Short, symbolic type ids carried in the {@code __TypeId__} header instead of this
   * service's internal class names — consumers in other services (which can't have
   * identity-service's classes on their classpath) map these same ids to their own local DTOs.
   * See docs/db/identity-service/schema.sql's "Domain events published" list — this mapping
   * must stay in sync with it. */
  private static final String TYPE_MAPPINGS =
      "AccountRegistered:" + AccountRegistered.class.getName() + "," //
          + "SessionIssued:" + SessionIssued.class.getName() + "," //
          + "SessionRevoked:" + SessionRevoked.class.getName() + "," //
          + "AccountVerified:" + AccountVerified.class.getName();

  @Bean
  public ProducerFactory<String, Object> producerFactory(
      KafkaProperties kafkaProperties, @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers) {
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
