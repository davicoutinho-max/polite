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
 * service, since there's no application/domain concern here beyond "forward this to the topic". */
@Controller
public class TypingController {

  private final SimpMessagingTemplate template;

  public TypingController(SimpMessagingTemplate template) {
    this.template = template;
  }

  @MessageMapping("/conversations/{conversationId}/typing")
  public void typing(@DestinationVariable UUID conversationId, Principal principal) {
    if (principal == null) {
      return;
    }
    template.convertAndSend(
        "/topic/conversations/" + conversationId + "/typing", Map.of("accountId", principal.getName(), "at", Instant.now()));
  }
}
