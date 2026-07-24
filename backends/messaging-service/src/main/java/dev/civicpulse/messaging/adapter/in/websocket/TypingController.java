package dev.civicpulse.messaging.adapter.in.websocket;

import java.security.Principal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

/** Purely ephemeral fan-out (no persistence, no domain invariant) — deliberately a thin
 * adapter-only relay rather than routed through a use-case port like every other write in this
 * service, since there's no application/domain concern here beyond "forward this to the topic".
 * Both "typing" and "recording audio" pings share the same topic and payload shape (just a
 * different {@code kind}) so the frontend can drive a single indicator off of one subscription. */
@Controller
public class TypingController {

  private final SimpMessagingTemplate template;

  public TypingController(SimpMessagingTemplate template) {
    this.template = template;
  }

  @MessageMapping("/conversations/{conversationId}/typing")
  public void typing(@DestinationVariable UUID conversationId, Principal principal) {
    broadcast(conversationId, principal, "typing");
  }

  @MessageMapping("/conversations/{conversationId}/recording")
  public void recording(@DestinationVariable UUID conversationId, Principal principal) {
    broadcast(conversationId, principal, "recording");
  }

  private void broadcast(UUID conversationId, Principal principal, String kind) {
    if (principal == null) {
      return;
    }
    template.convertAndSend(
        "/topic/conversations/" + conversationId + "/typing",
        Map.of("accountId", principal.getName(), "at", Instant.now(), "kind", kind));
  }
}
