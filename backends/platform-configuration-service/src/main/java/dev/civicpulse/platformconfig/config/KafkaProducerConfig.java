package dev.civicpulse.platformconfig.config;

import dev.civicpulse.platformconfig.domain.event.CountryAdded;
import dev.civicpulse.platformconfig.domain.event.CountryRemoved;
import dev.civicpulse.platformconfig.domain.event.DefaultLanguageChanged;
import dev.civicpulse.platformconfig.domain.event.LanguageAdded;
import dev.civicpulse.platformconfig.domain.event.LanguageRemoved;
import dev.civicpulse.platformconfig.domain.event.PartyRegistered;
import dev.civicpulse.platformconfig.domain.event.PoliticianReassigned;
import dev.civicpulse.platformconfig.domain.event.TranslationValueUpdated;
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
   * Must stay in sync with docs/db/platform-configuration-service/schema.sql's "Domain events
   * published" list. */
  private static final String TYPE_MAPPINGS =
      "PartyRegistered:" + PartyRegistered.class.getName() + "," //
          + "PoliticianReassigned:" + PoliticianReassigned.class.getName() + "," //
          + "LanguageAdded:" + LanguageAdded.class.getName() + "," //
          + "LanguageRemoved:" + LanguageRemoved.class.getName() + "," //
          + "DefaultLanguageChanged:" + DefaultLanguageChanged.class.getName() + "," //
          + "TranslationValueUpdated:" + TranslationValueUpdated.class.getName() + "," //
          + "CountryAdded:" + CountryAdded.class.getName() + "," //
          + "CountryRemoved:" + CountryRemoved.class.getName();

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
