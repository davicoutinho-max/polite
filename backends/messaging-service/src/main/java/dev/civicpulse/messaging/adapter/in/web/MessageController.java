package dev.civicpulse.messaging.adapter.in.web;

import dev.civicpulse.messaging.adapter.in.web.dto.EditMessageRequest;
import dev.civicpulse.messaging.adapter.in.web.dto.MessageResponse;
import dev.civicpulse.messaging.adapter.in.web.dto.SendMessageRequest;
import dev.civicpulse.messaging.application.port.in.EditMessageUseCase;
import dev.civicpulse.messaging.application.port.in.GetMessageUseCase;
import dev.civicpulse.messaging.application.port.in.SendMessageUseCase;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/conversations/{conversationId}/messages")
public class MessageController {

  private final SendMessageUseCase sendMessageUseCase;
  private final GetMessageUseCase getMessageUseCase;
  private final EditMessageUseCase editMessageUseCase;

  public MessageController(SendMessageUseCase sendMessageUseCase, GetMessageUseCase getMessageUseCase, EditMessageUseCase editMessageUseCase) {
    this.sendMessageUseCase = sendMessageUseCase;
    this.getMessageUseCase = getMessageUseCase;
    this.editMessageUseCase = editMessageUseCase;
  }

  @PostMapping
  public ResponseEntity<MessageResponse> send(
      @PathVariable UUID conversationId, @RequestHeader("X-Account-Id") UUID senderAccountId, @Valid @RequestBody SendMessageRequest request) {
    var message = sendMessageUseCase.send(conversationId, senderAccountId, request.body());
    MessageResponse body = MessageResponse.from(message);
    return ResponseEntity.created(URI.create("/conversations/" + conversationId + "/messages/" + body.id())).body(body);
  }

  @GetMapping
  public List<MessageResponse> list(
      @PathVariable UUID conversationId, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "50") int pageSize) {
    return getMessageUseCase.listByConversation(conversationId, page, pageSize).stream().map(MessageResponse::from).toList();
  }

  @PutMapping("/{messageId}")
  public MessageResponse edit(
      @PathVariable UUID conversationId,
      @PathVariable UUID messageId,
      @RequestHeader("X-Account-Id") UUID requesterAccountId,
      @Valid @RequestBody EditMessageRequest request) {
    return MessageResponse.from(editMessageUseCase.edit(messageId, requesterAccountId, request.body()));
  }

  @DeleteMapping("/{messageId}")
  public MessageResponse delete(
      @PathVariable UUID conversationId, @PathVariable UUID messageId, @RequestHeader("X-Account-Id") UUID requesterAccountId) {
    return MessageResponse.from(editMessageUseCase.delete(messageId, requesterAccountId));
  }
}
