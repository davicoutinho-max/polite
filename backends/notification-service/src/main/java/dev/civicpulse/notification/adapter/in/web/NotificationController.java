package dev.civicpulse.notification.adapter.in.web;

import dev.civicpulse.notification.adapter.in.web.dto.BroadcastNotificationRequest;
import dev.civicpulse.notification.adapter.in.web.dto.NotificationResponse;
import dev.civicpulse.notification.adapter.in.web.dto.UnreadCountResponse;
import dev.civicpulse.notification.application.port.in.GetNotificationUseCase;
import dev.civicpulse.notification.application.port.in.IngestNotificationUseCase;
import dev.civicpulse.notification.domain.model.NotificationCategory;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** {@code X-Account-Id} is the caller's own id, forwarded by the Gateway after JWT validation —
 * see docs/architecture/system-architecture.html. Most notifications are only ever produced by
 * consuming upstream domain events (see NotificationProjectionListener) — {@code /broadcast} is
 * the one direct-write exception, for a party/platform admin manually announcing something to a
 * chosen list of recipients (e.g. "send notification to all members"). */
@RestController
@RequestMapping("/notifications")
public class NotificationController {

  private final GetNotificationUseCase getNotificationUseCase;
  private final IngestNotificationUseCase ingestNotificationUseCase;

  public NotificationController(GetNotificationUseCase getNotificationUseCase, IngestNotificationUseCase ingestNotificationUseCase) {
    this.getNotificationUseCase = getNotificationUseCase;
    this.ingestNotificationUseCase = ingestNotificationUseCase;
  }

  @GetMapping
  public List<NotificationResponse> list(
      @RequestHeader("X-Account-Id") UUID accountId, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int pageSize) {
    return getNotificationUseCase.listByRecipient(accountId, page, pageSize).stream().map(NotificationResponse::from).toList();
  }

  @GetMapping("/unread-count")
  public UnreadCountResponse unreadCount(@RequestHeader("X-Account-Id") UUID accountId) {
    return new UnreadCountResponse(getNotificationUseCase.countUnread(accountId));
  }

  @PostMapping("/{id}/read")
  public ResponseEntity<Void> markRead(@PathVariable UUID id) {
    getNotificationUseCase.markRead(id);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/read-all")
  public ResponseEntity<Void> markAllRead(@RequestHeader("X-Account-Id") UUID accountId) {
    getNotificationUseCase.markAllRead(accountId);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/broadcast")
  public ResponseEntity<Void> broadcast(@RequestHeader("X-Account-Id") UUID senderAccountId, @Valid @RequestBody BroadcastNotificationRequest request) {
    NotificationCategory category = NotificationCategory.fromCode(request.category());
    String broadcastId = "broadcast-" + senderAccountId + "-" + UUID.randomUUID();
    for (UUID recipientAccountId : request.recipientAccountIds()) {
      ingestNotificationUseCase.ingest(
          recipientAccountId, category, request.icon(), request.title(), request.message(), request.link(), broadcastId + "-" + recipientAccountId);
    }
    return ResponseEntity.noContent().build();
  }
}
