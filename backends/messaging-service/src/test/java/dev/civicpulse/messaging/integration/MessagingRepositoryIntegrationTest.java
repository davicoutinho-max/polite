package dev.civicpulse.messaging.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import dev.civicpulse.messaging.application.port.out.ConversationParticipantRepository;
import dev.civicpulse.messaging.application.port.out.ConversationRepository;
import dev.civicpulse.messaging.application.port.out.MessageRepository;
import dev.civicpulse.messaging.domain.model.Conversation;
import dev.civicpulse.messaging.domain.model.ConversationParticipant;
import dev.civicpulse.messaging.domain.model.Message;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/** Exercises the real JPA/Postgres adapters against the shared local-dev database (see
 * identity-service's equivalent test for the rationale on why this isn't Testcontainers). */
@SpringBootTest(
    properties = {
      "spring.datasource.url=jdbc:postgresql://localhost:5432/messaging_service",
      "spring.datasource.username=messaging_service_app",
      "spring.datasource.password=messaging_dev_pw"
    })
class MessagingRepositoryIntegrationTest {

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

  @Autowired private ConversationRepository conversationRepository;
  @Autowired private ConversationParticipantRepository conversationParticipantRepository;
  @Autowired private MessageRepository messageRepository;

  @Test
  void savesAndRetrievesDirectConversation() {
    UUID id = UUID.randomUUID();
    conversationRepository.save(Conversation.createDirect(id, Instant.now()));

    assertThat(conversationRepository.findById(id)).isPresent().get().satisfies(found -> assertThat(found.group()).isFalse());
  }

  @Test
  void findDirectBetweenFindsExactPair() {
    UUID a = UUID.randomUUID();
    UUID b = UUID.randomUUID();
    UUID conversationId = UUID.randomUUID();
    conversationRepository.save(Conversation.createDirect(conversationId, Instant.now()));
    conversationParticipantRepository.save(ConversationParticipant.join(conversationId, a, Instant.now()));
    conversationParticipantRepository.save(ConversationParticipant.join(conversationId, b, Instant.now()));

    assertThat(conversationRepository.findDirectBetween(a, b)).isPresent().get().satisfies(found -> assertThat(found.id()).isEqualTo(conversationId));
    assertThat(conversationRepository.findDirectBetween(a, UUID.randomUUID())).isEmpty();
  }

  @Test
  void findByParticipantReturnsConversationsForAccount() {
    UUID accountId = UUID.randomUUID();
    UUID conversationId = UUID.randomUUID();
    conversationRepository.save(Conversation.createDirect(conversationId, Instant.now()));
    conversationParticipantRepository.save(ConversationParticipant.join(conversationId, accountId, Instant.now()));

    assertThat(conversationRepository.findByParticipant(accountId)).extracting(Conversation::id).contains(conversationId);
  }

  @Test
  void participantExistsCheck() {
    UUID conversationId = UUID.randomUUID();
    UUID accountId = UUID.randomUUID();
    conversationRepository.save(Conversation.createDirect(conversationId, Instant.now()));

    assertThat(conversationParticipantRepository.exists(conversationId, accountId)).isFalse();

    conversationParticipantRepository.save(ConversationParticipant.join(conversationId, accountId, Instant.now()));

    assertThat(conversationParticipantRepository.exists(conversationId, accountId)).isTrue();
  }

  @Test
  void messagePersistsAndListsInOrder() {
    UUID conversationId = UUID.randomUUID();
    conversationRepository.save(Conversation.createDirect(conversationId, Instant.now()));

    messageRepository.save(Message.send(UUID.randomUUID(), conversationId, UUID.randomUUID(), "first!", Instant.now()));

    assertThat(messageRepository.findByConversationId(conversationId, 0, 20)).anySatisfy(m -> assertThat(m.body()).isEqualTo("first!"));
  }
}
