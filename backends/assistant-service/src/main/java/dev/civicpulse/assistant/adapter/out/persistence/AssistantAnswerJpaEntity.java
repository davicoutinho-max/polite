package dev.civicpulse.assistant.adapter.out.persistence;

import dev.civicpulse.assistant.domain.model.AssistantPromptKind;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "assistant_answers")
public class AssistantAnswerJpaEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "topic_id", nullable = false)
  private UUID topicId;

  @Column(name = "prompt_kind", nullable = false)
  private AssistantPromptKind promptKind;

  @Column(name = "answer_text", nullable = false)
  private String answerText;

  protected AssistantAnswerJpaEntity() {}

  public AssistantAnswerJpaEntity(Long id, UUID topicId, AssistantPromptKind promptKind, String answerText) {
    this.id = id;
    this.topicId = topicId;
    this.promptKind = promptKind;
    this.answerText = answerText;
  }

  public Long getId() {
    return id;
  }

  public UUID getTopicId() {
    return topicId;
  }

  public AssistantPromptKind getPromptKind() {
    return promptKind;
  }

  public String getAnswerText() {
    return answerText;
  }
}
