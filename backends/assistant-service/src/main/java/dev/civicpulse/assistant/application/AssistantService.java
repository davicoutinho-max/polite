package dev.civicpulse.assistant.application;

import dev.civicpulse.assistant.application.port.in.AssistantUseCase;
import dev.civicpulse.assistant.application.port.out.AssistantAnswerRepository;
import dev.civicpulse.assistant.application.port.out.AssistantTopicRepository;
import dev.civicpulse.assistant.domain.exception.AssistantTopicNotFoundException;
import dev.civicpulse.assistant.domain.model.AssistantAnswer;
import dev.civicpulse.assistant.domain.model.AssistantPromptKind;
import dev.civicpulse.assistant.domain.model.AssistantTopic;
import java.time.Clock;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AssistantService implements AssistantUseCase {

  private final AssistantTopicRepository topicRepository;
  private final AssistantAnswerRepository answerRepository;
  private final Clock clock;

  public AssistantService(AssistantTopicRepository topicRepository, AssistantAnswerRepository answerRepository, Clock clock) {
    this.topicRepository = topicRepository;
    this.answerRepository = answerRepository;
    this.clock = clock;
  }

  @Override
  public List<AssistantTopicView> listTopics() {
    return topicRepository.findAll().stream().map(topic -> AssistantTopicView.of(topic, answerRepository.findByTopicId(requireId(topic)))).toList();
  }

  @Override
  public AssistantTopicView getTopic(UUID id) {
    AssistantTopic topic = topicRepository.findById(id).orElseThrow(() -> new AssistantTopicNotFoundException(id));
    return AssistantTopicView.of(topic, answerRepository.findByTopicId(id));
  }

  @Override
  @Transactional
  public AssistantTopicView createTopic(String reference, String title, UUID legislativeItemId) {
    AssistantTopic saved = topicRepository.save(AssistantTopic.create(reference, title, legislativeItemId, clock.instant()));
    return AssistantTopicView.of(saved, List.of());
  }

  @Override
  @Transactional
  public AssistantTopicView writeAnswer(UUID topicId, AssistantPromptKind promptKind, String answerText) {
    AssistantTopic topic = topicRepository.findById(topicId).orElseThrow(() -> new AssistantTopicNotFoundException(topicId));
    AssistantAnswer answer =
        answerRepository
            .findByTopicIdAndPromptKind(topicId, promptKind)
            .map(existing -> AssistantAnswer.reconstitute(existing.id().orElseThrow(), topicId, promptKind, answerText))
            .orElseGet(() -> AssistantAnswer.write(topicId, promptKind, answerText));
    answerRepository.save(answer);
    return AssistantTopicView.of(topic, answerRepository.findByTopicId(topicId));
  }

  private static UUID requireId(AssistantTopic topic) {
    return topic.id().orElseThrow();
  }
}
