package dev.civicpulse.assistant.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import dev.civicpulse.assistant.application.port.out.AssistantAnswerRepository;
import dev.civicpulse.assistant.application.port.out.AssistantTopicRepository;
import dev.civicpulse.assistant.domain.model.AssistantAnswer;
import dev.civicpulse.assistant.domain.model.AssistantPromptKind;
import dev.civicpulse.assistant.domain.model.AssistantTopic;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.Instant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/** Exercises the real JPA/Postgres adapters against the shared local-dev database (see
 * identity-service's equivalent test for the rationale on why this isn't Testcontainers). */
@SpringBootTest(
    properties = {
      "spring.datasource.url=jdbc:postgresql://localhost:5432/assistant_service",
      "spring.datasource.username=assistant_service_app",
      "spring.datasource.password=assistant_dev_pw"
    })
class AssistantRepositoryIntegrationTest {

  @BeforeAll
  static void requireLocalPostgres() {
    boolean reachable;
    try (Socket socket = new Socket()) {
      socket.connect(new InetSocketAddress("localhost", 5432), 500);
      reachable = true;
    } catch (Exception e) {
      reachable = false;
    }
    assumeTrue(reachable, "Shared dev Postgres (localhost:5432) is not running — start it with "
        + "'docker compose up -d postgres' in backends/ to run this test");
  }

  @Autowired private AssistantTopicRepository topicRepository;
  @Autowired private AssistantAnswerRepository answerRepository;

  @Test
  void savesAndRetrievesTopicWithAnswers() {
    AssistantTopic saved = topicRepository.save(AssistantTopic.create("PL 1/2026", "Test Bill", null, Instant.now()));
    var topicId = saved.id().orElseThrow();

    answerRepository.save(AssistantAnswer.write(topicId, AssistantPromptKind.SUMMARY, "A summary"));

    assertThat(topicRepository.findById(topicId)).isPresent().get().satisfies(t -> assertThat(t.reference()).isEqualTo("PL 1/2026"));
    assertThat(answerRepository.findByTopicId(topicId)).anySatisfy(a -> assertThat(a.answerText()).isEqualTo("A summary"));
  }

  @Test
  void findByTopicIdAndPromptKindSupportsUpsertLookup() {
    AssistantTopic saved = topicRepository.save(AssistantTopic.create("PL 2/2026", "Another Bill", null, Instant.now()));
    var topicId = saved.id().orElseThrow();
    answerRepository.save(AssistantAnswer.write(topicId, AssistantPromptKind.PLAIN, "Plain text"));

    assertThat(answerRepository.findByTopicIdAndPromptKind(topicId, AssistantPromptKind.PLAIN))
        .isPresent()
        .get()
        .satisfies(a -> assertThat(a.answerText()).isEqualTo("Plain text"));
    assertThat(answerRepository.findByTopicIdAndPromptKind(topicId, AssistantPromptKind.IMPACT)).isEmpty();
  }
}
