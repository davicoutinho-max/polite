package dev.civicpulse.livestreaming.adapter.in.web;

import dev.civicpulse.livestreaming.adapter.in.web.dto.ArchiveChatMessageRequest;
import dev.civicpulse.livestreaming.adapter.in.web.dto.ChatMessageResponse;
import dev.civicpulse.livestreaming.application.port.in.ArchiveChatUseCase;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Compliance/moderation-only archival — see LiveStreamingServiceApplication's scope note. Real-
 * time chat fan-out itself happens over Kafka + a WebSocket gateway, neither of which is part of
 * this pass. */
@RestController
@RequestMapping("/live-sessions/{sessionId}/chat-archive")
public class ChatArchiveController {

  private final ArchiveChatUseCase archiveChatUseCase;

  public ChatArchiveController(ArchiveChatUseCase archiveChatUseCase) {
    this.archiveChatUseCase = archiveChatUseCase;
  }

  @PostMapping
  public ResponseEntity<ChatMessageResponse> archive(@PathVariable UUID sessionId, @Valid @RequestBody ArchiveChatMessageRequest request) {
    var message = archiveChatUseCase.archiveMessage(sessionId, request.accountId(), request.body());
    return ResponseEntity.status(201).body(ChatMessageResponse.from(message));
  }

  @GetMapping
  public List<ChatMessageResponse> list(@PathVariable UUID sessionId) {
    return archiveChatUseCase.listArchivedMessages(sessionId).stream().map(ChatMessageResponse::from).toList();
  }
}
