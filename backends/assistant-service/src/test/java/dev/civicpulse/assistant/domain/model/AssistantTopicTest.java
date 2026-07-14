package dev.civicpulse.assistant.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AssistantTopicTest {

  private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

  @Test
  void createStartsWithNoId() {
    AssistantTopic topic = AssistantTopic.create("PEC 33/2024", "Fiscal Transparency Amendment", null, NOW);

    assertThat(topic.id()).isEmpty();
    assertThat(topic.reference()).isEqualTo("PEC 33/2024");
    assertThat(topic.legislativeItemId()).isEmpty();
  }

  @Test
  void createAllowsAnOptionalLegislativeItemLink() {
    UUID itemId = UUID.randomUUID();
    AssistantTopic topic = AssistantTopic.create("PEC 33/2024", "Title", itemId, NOW);

    assertThat(topic.legislativeItemId()).contains(itemId);
  }

  @Test
  void blankTitleIsRejected() {
    assertThatThrownBy(() -> AssistantTopic.create("PEC 33/2024", "  ", null, NOW)).isInstanceOf(IllegalArgumentException.class);
  }
}
