package dev.civicpulse.activityfeed.config;

import dev.civicpulse.activityfeed.adapter.in.messaging.dto.CommitteeMembershipChangedMessage;
import dev.civicpulse.activityfeed.adapter.in.messaging.dto.FundraiserGoalReachedMessage;
import dev.civicpulse.activityfeed.adapter.in.messaging.dto.LegislativeItemFiledMessage;
import dev.civicpulse.activityfeed.adapter.in.messaging.dto.LegislativeItemStatusChangedMessage;
import dev.civicpulse.activityfeed.adapter.in.messaging.dto.PoliticianReassignedMessage;
import dev.civicpulse.activityfeed.adapter.in.messaging.dto.PostPublishedMessage;
import dev.civicpulse.activityfeed.adapter.in.messaging.dto.VoteCastMessage;
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
      "PostPublished:" + PostPublishedMessage.class.getName() + "," //
          + "VoteCast:" + VoteCastMessage.class.getName() + "," //
          + "LegislativeItemFiled:" + LegislativeItemFiledMessage.class.getName() + "," //
          + "LegislativeItemStatusChanged:" + LegislativeItemStatusChangedMessage.class.getName() + "," //
          + "CommitteeMembershipChanged:" + CommitteeMembershipChangedMessage.class.getName() + "," //
          + "PoliticianReassigned:" + PoliticianReassignedMessage.class.getName() + "," //
          + "FundraiserGoalReached:" + FundraiserGoalReachedMessage.class.getName();

  @Bean
  public ConsumerFactory<String, Object> consumerFactory(@Value("${spring.kafka.bootstrap-servers}") String bootstrapServers) {
    Map<String, Object> props =
        Map.of(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
            ConsumerConfig.GROUP_ID_CONFIG, "activity-feed-service",
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class,
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
            JsonDeserializer.TRUSTED_PACKAGES, "dev.civicpulse.activityfeed.adapter.in.messaging.dto",
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
