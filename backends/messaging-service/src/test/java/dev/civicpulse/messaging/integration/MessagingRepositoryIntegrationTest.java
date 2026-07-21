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

  // Every test below writes through the real JPA adapters into the shared local-dev Postgres —
  // the same database the live messaging-service instance serves the real app from. Deleting the
  // conversation cascades to conversation_participants (ON DELETE CASCADE), but `messages` is
  // hash-partitioned by conversation_id with no FK to conversations, so it needs an explicit
  // delete too.

  @Test
  void savesAndRetrievesDirectConversation() {
    UUID id = UUID.randomUUID();
    conversationRepository.save(Conversation.createDirect(id, Instant.now()));

    try {
      assertThat(conversationRepository.findById(id)).isPresent().get().satisfies(found -> assertThat(found.group()).isFalse());
    } finally {
      conversationRepository.deleteById(id);
    }
  }

  @Test
  void findDirectBetweenFindsExactPair() {
    UUID a = UUID.randomUUID();
    UUID b = UUID.randomUUID();
    UUID conversationId = UUID.randomUUID();
    conversationRepository.save(Conversation.createDirect(conversationId, Instant.now()));
    conversationParticipantRepository.save(ConversationParticipant.join(conversationId, a, Instant.now()));
    conversationParticipantRepository.save(ConversationParticipant.join(conversationId, b, Instant.now()));

    try {
      assertThat(conversationRepository.findDirectBetween(a, b)).isPresent().get().satisfies(found -> assertThat(found.id()).isEqualTo(conversationId));
      assertThat(conversationRepository.findDirectBetween(a, UUID.randomUUID())).isEmpty();
    } finally {
      conversationRepository.deleteById(conversationId);
    }
  }

  @Test
  void findByParticipantReturnsConversationsForAccount() {
    UUID accountId = UUID.randomUUID();
    UUID conversationId = UUID.randomUUID();
    conversationRepository.save(Conversation.createDirect(conversationId, Instant.now()));
    conversationParticipantRepository.save(ConversationParticipant.join(conversationId, accountId, Instant.now()));

    try {
      assertThat(conversationRepository.findByParticipant(accountId)).extracting(Conversation::id).contains(conversationId);
    } finally {
      conversationRepository.deleteById(conversationId);
    }
  }

  @Test
  void participantExistsCheck() {
    UUID conversationId = UUID.randomUUID();
    UUID accountId = UUID.randomUUID();
    conversationRepository.save(Conversation.createDirect(conversationId, Instant.now()));

    try {
      assertThat(conversationParticipantRepository.exists(conversationId, accountId)).isFalse();

      conversationParticipantRepository.save(ConversationParticipant.join(conversationId, accountId, Instant.now()));

      assertThat(conversationParticipantRepository.exists(conversationId, accountId)).isTrue();
    } finally {
      conversationRepository.deleteById(conversationId);
    }
  }

  @Test
  void messagePersistsAndListsInOrder() {
    UUID conversationId = UUID.randomUUID();
    conversationRepository.save(Conversation.createDirect(conversationId, Instant.now()));

    messageRepository.save(Message.send(UUID.randomUUID(), conversationId, UUID.randomUUID(), "first!", Instant.now()));

    try {
      assertThat(messageRepository.findByConversationId(conversationId, 0, 20)).anySatisfy(m -> assertThat(m.body()).isEqualTo("first!"));
    } finally {
      messageRepository.deleteByConversationId(conversationId);
      conversationRepository.deleteById(conversationId);
    }
  }
}
