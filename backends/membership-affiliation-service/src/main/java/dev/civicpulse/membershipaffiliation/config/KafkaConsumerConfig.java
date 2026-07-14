package dev.civicpulse.membershipaffiliation.config;

import dev.civicpulse.membershipaffiliation.adapter.in.messaging.dto.AffiliationRequestApprovedMessage;
import dev.civicpulse.membershipaffiliation.adapter.in.messaging.dto.AffiliationRequestRejectedMessage;
import dev.civicpulse.membershipaffiliation.adapter.in.messaging.dto.PaymentCapturedMessage;
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

  private static final String TYPE_MAPPINGS =
      "AffiliationRequestApproved:" + AffiliationRequestApprovedMessage.class.getName() + "," //
          + "AffiliationRequestRejected:" + AffiliationRequestRejectedMessage.class.getName() + "," //
          + "PaymentCaptured:" + PaymentCapturedMessage.class.getName();

  @Bean
  public ConsumerFactory<String, Object> consumerFactory(@Value("${spring.kafka.bootstrap-servers}") String bootstrapServers) {
    Map<String, Object> props =
        Map.of(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
            ConsumerConfig.GROUP_ID_CONFIG, "membership-affiliation-service",
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class,
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
            JsonDeserializer.TRUSTED_PACKAGES, "dev.civicpulse.membershipaffiliation.adapter.in.messaging.dto",
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
