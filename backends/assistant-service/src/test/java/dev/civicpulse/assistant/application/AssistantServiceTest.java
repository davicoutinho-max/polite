package dev.civicpulse.assistant.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import dev.civicpulse.assistant.application.port.out.AssistantAnswerRepository;
import dev.civicpulse.assistant.application.port.out.AssistantTopicRepository;
import dev.civicpulse.assistant.domain.model.AssistantAnswer;
import dev.civicpulse.assistant.domain.model.AssistantPromptKind;
import dev.civicpulse.assistant.domain.model.AssistantTopic;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AssistantServiceTest {

  private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

  @Mock private AssistantTopicRepository topicRepository;
  @Mock private AssistantAnswerRepository answerRepository;

  private AssistantService service;

  @BeforeEach
  void setUp() {
    service = new AssistantService(topicRepository, answerRepository, Clock.fixed(NOW, ZoneOffset.UTC));
  }

  @Test
  void getTopicCombinesTopicAndAnswersIntoAMap() {
    UUID topicId = UUID.randomUUID();
    AssistantTopic topic = AssistantTopic.reconstitute(topicId, "PEC 33/2024", "Title", null, NOW);
    when(topicRepository.findById(topicId)).thenReturn(Optional.of(topic));
    when(answerRepository.findByTopicId(topicId))
        .thenReturn(List.of(AssistantAnswer.reconstitute(1L, topicId, AssistantPromptKind.SUMMARY, "Short summary")));

    AssistantTopicView view = service.getTopic(topicId);

    assertThat(view.answers()).containsEntry("summary", "Short summary");
  }

  @Test
  void writeAnswerUpdatesExistingAnswerInPlace() {
    UUID topicId = UUID.randomUUID();
    AssistantTopic topic = AssistantTopic.reconstitute(topicId, "PEC 33/2024", "Title", null, NOW);
    when(topicRepository.findById(topicId)).thenReturn(Optional.of(topic));
    when(answerRepository.findByTopicIdAndPromptKind(topicId, AssistantPromptKind.SUMMARY))
        .thenReturn(Optional.of(AssistantAnswer.reconstitute(1L, topicId, AssistantPromptKind.SUMMARY, "Old text")));
    when(answerRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    when(answerRepository.findByTopicId(topicId))
        .thenReturn(List.of(AssistantAnswer.reconstitute(1L, topicId, AssistantPromptKind.SUMMARY, "New text")));

    AssistantTopicView view = service.writeAnswer(topicId, AssistantPromptKind.SUMMARY, "New text");

    assertThat(view.answers()).containsEntry("summary", "New text");
  }
}
