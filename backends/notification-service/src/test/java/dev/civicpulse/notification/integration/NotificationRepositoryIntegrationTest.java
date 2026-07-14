package dev.civicpulse.notification.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import dev.civicpulse.notification.application.port.out.NotificationRepository;
import dev.civicpulse.notification.application.port.out.PushTokenRepository;
import dev.civicpulse.notification.domain.model.Notification;
import dev.civicpulse.notification.domain.model.NotificationCategory;
import dev.civicpulse.notification.domain.model.NotificationPlatform;
import dev.civicpulse.notification.domain.model.PushToken;
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
      "spring.datasource.url=jdbc:postgresql://localhost:5432/notification_service",
      "spring.datasource.username=notification_service_app",
      "spring.datasource.password=notification_dev_pw"
    })
class NotificationRepositoryIntegrationTest {

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

  @Autowired private NotificationRepository notificationRepository;
  @Autowired private PushTokenRepository pushTokenRepository;

  @Test
  void savesAndRetrievesNotification() {
    UUID id = UUID.randomUUID();
    UUID recipientId = UUID.randomUUID();
    notificationRepository.save(
        Notification.create(id, recipientId, NotificationCategory.CAMPAIGN, "icon", "title", "message", "/link", "src-" + id, Instant.now()));

    assertThat(notificationRepository.findById(id)).isPresent().get().satisfies(found -> assertThat(found.title()).isEqualTo("title"));
  }

  @Test
  void existsByRecipientAndSourceEventIdDetectsDuplicates() {
    UUID recipientId = UUID.randomUUID();
    String sourceEventId = "dedupe-test-" + UUID.randomUUID();

    assertThat(notificationRepository.existsByRecipientAndSourceEventId(recipientId, sourceEventId)).isFalse();

    notificationRepository.save(
        Notification.create(
            UUID.randomUUID(), recipientId, NotificationCategory.PARTY, null, "title", "message", null, sourceEventId, Instant.now()));

    assertThat(notificationRepository.existsByRecipientAndSourceEventId(recipientId, sourceEventId)).isTrue();
  }

  @Test
  void countUnreadReflectsReadState() {
    UUID recipientId = UUID.randomUUID();
    Notification notification =
        Notification.create(
            UUID.randomUUID(), recipientId, NotificationCategory.VOTE, null, "title", "message", null, "src-" + UUID.randomUUID(), Instant.now());
    notificationRepository.save(notification);

    assertThat(notificationRepository.countUnread(recipientId)).isEqualTo(1);

    notification.markRead();
    notificationRepository.save(notification);

    assertThat(notificationRepository.countUnread(recipientId)).isZero();
  }

  @Test
  void pushTokenRoundTrip() {
    UUID accountId = UUID.randomUUID();
    pushTokenRepository.save(PushToken.register(accountId, NotificationPlatform.ANDROID, "tok-abc", Instant.now()));

    assertThat(pushTokenRepository.findByAccountId(accountId)).anySatisfy(t -> assertThat(t.token()).isEqualTo("tok-abc"));

    pushTokenRepository.delete(accountId, "android", "tok-abc");

    assertThat(pushTokenRepository.findByAccountId(accountId)).isEmpty();
  }
}
