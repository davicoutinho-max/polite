package dev.civicpulse.assistant.domain.model;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class AssistantAnswer {

  private final Long id;
  private final UUID topicId;
  private final AssistantPromptKind promptKind;
  private final String answerText;

  private AssistantAnswer(Long id, UUID topicId, AssistantPromptKind promptKind, String answerText) {
    this.id = id;
    this.topicId = Objects.requireNonNull(topicId);
    this.promptKind = Objects.requireNonNull(promptKind);
    this.answerText = requireNonBlank(answerText);
  }

  public static AssistantAnswer write(UUID topicId, AssistantPromptKind promptKind, String answerText) {
    return new AssistantAnswer(null, topicId, promptKind, answerText);
  }

  public static AssistantAnswer reconstitute(Long id, UUID topicId, AssistantPromptKind promptKind, String answerText) {
    return new AssistantAnswer(id, topicId, promptKind, answerText);
  }

  private static String requireNonBlank(String value) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("answerText must not be blank");
    }
    return value;
  }

  public Optional<Long> id() {
    return Optional.ofNullable(id);
  }

  public UUID topicId() {
    return topicId;
  }

  public AssistantPromptKind promptKind() {
    return promptKind;
  }

  public String answerText() {
    return answerText;
  }
}
