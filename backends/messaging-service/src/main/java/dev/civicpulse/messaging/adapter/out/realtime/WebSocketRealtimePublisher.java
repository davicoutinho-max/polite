package dev.civicpulse.messaging.adapter.out.realtime;

import dev.civicpulse.messaging.application.port.out.RealtimeMessagePublisher;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
class WebSocketRealtimePublisher implements RealtimeMessagePublisher {

  private final SimpMessagingTemplate template;

  WebSocketRealtimePublisher(SimpMessagingTemplate template) {
    this.template = template;
  }

  @Override
  public void messageSent(UUID conversationId, UUID messageId, UUID senderAccountId, String body, Instant createdAt) {
    messageUpdated(conversationId, messageId, senderAccountId, body, createdAt, null, false);
  }

  @Override
  public void messageUpdated(
      UUID conversationId, UUID messageId, UUID senderAccountId, String body, Instant createdAt, Instant editedAt, boolean deleted) {
    Map<String, Object> payload = new HashMap<>();
    payload.put("type", "message");
    payload.put("id", messageId);
    payload.put("conversationId", conversationId);
    payload.put("senderAccountId", senderAccountId);
    payload.put("body", body);
    payload.put("createdAt", createdAt);
    payload.put("editedAt", editedAt);
    payload.put("deleted", deleted);
    send(conversationId, payload);
  }

  @Override
  public void conversationRead(UUID conversationId, UUID accountId, Instant readAt) {
    send(conversationId, Map.of("type", "read", "accountId", accountId, "readAt", readAt));
  }

  private void send(UUID conversationId, Map<String, Object> payload) {
    template.convertAndSend("/topic/conversations/" + conversationId, payload);
  }
}
