package dev.civicpulse.notification.adapter.in.web;

import dev.civicpulse.notification.adapter.in.web.dto.NotificationResponse;
import dev.civicpulse.notification.adapter.in.web.dto.UnreadCountResponse;
import dev.civicpulse.notification.application.port.in.GetNotificationUseCase;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** {@code X-Account-Id} is the caller's own id, forwarded by the Gateway after JWT validation —
 * see docs/architecture/system-architecture.html. There is no write endpoint for creating
 * notifications — they are only ever produced by consuming upstream domain events (see
 * NotificationProjectionListener), matching this service's "pure consumer" framing in
 * schema.sql. */
@RestController
@RequestMapping("/notifications")
public class NotificationController {

  private final GetNotificationUseCase getNotificationUseCase;

  public NotificationController(GetNotificationUseCase getNotificationUseCase) {
    this.getNotificationUseCase = getNotificationUseCase;
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
}
