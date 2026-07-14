package dev.civicpulse.directory.config;

import dev.civicpulse.directory.adapter.in.messaging.dto.AccountRegisteredMessage;
import dev.civicpulse.directory.adapter.in.messaging.dto.PartyRegisteredMessage;
import dev.civicpulse.directory.adapter.in.messaging.dto.PoliticianReassignedMessage;
import dev.civicpulse.directory.adapter.in.messaging.dto.PoliticianRegisteredMessage;
import dev.civicpulse.directory.adapter.in.messaging.dto.RepresentativeLinkedMessage;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

@Configuration
public class KafkaConsumerConfig {

  /** Maps the symbolic {@code __TypeId__} header (set by each publishing service's own
   * JsonSerializer.TYPE_MAPPINGS — see e.g. identity-service's KafkaProducerConfig) to this
   * service's own local DTOs. Must stay in sync with
   * docs/db/directory-service/schema.sql's "Domain events consumed" list. */
  private static final String TYPE_MAPPINGS =
      "AccountRegistered:" + AccountRegisteredMessage.class.getName() + "," //
          + "RepresentativeLinked:" + RepresentativeLinkedMessage.class.getName() + "," //
          + "PoliticianRegistered:" + PoliticianRegisteredMessage.class.getName() + "," //
          + "PoliticianReassigned:" + PoliticianReassignedMessage.class.getName() + "," //
          + "PartyRegistered:" + PartyRegisteredMessage.class.getName();

  @Bean
  public ConsumerFactory<String, Object> consumerFactory(@Value("${spring.kafka.bootstrap-servers}") String bootstrapServers) {
    Map<String, Object> props =
        Map.of(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
            ConsumerConfig.GROUP_ID_CONFIG, "directory-service",
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class,
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
            JsonDeserializer.TRUSTED_PACKAGES, "dev.civicpulse.directory.adapter.in.messaging.dto",
            JsonDeserializer.TYPE_MAPPINGS, TYPE_MAPPINGS,
            JsonDeserializer.USE_TYPE_INFO_HEADERS, true);
    return new DefaultKafkaConsumerFactory<>(props);
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory(
      ConsumerFactory<String, Object> consumerFactory) {
    var factory = new ConcurrentKafkaListenerContainerFactory<String, Object>();
    factory.setConsumerFactory(consumerFactory);
    return factory;
  }
}
