package dev.civicpulse.messaging.adapter.in.web;

import dev.civicpulse.messaging.adapter.in.web.dto.ConversationResponse;
import dev.civicpulse.messaging.adapter.in.web.dto.ParticipantResponse;
import dev.civicpulse.messaging.adapter.in.web.dto.StartDirectConversationRequest;
import dev.civicpulse.messaging.adapter.in.web.dto.StartGroupConversationRequest;
import dev.civicpulse.messaging.adapter.in.web.dto.UpdateGroupAvatarRequest;
import dev.civicpulse.messaging.adapter.in.web.dto.UpdateGroupNameRequest;
import dev.civicpulse.messaging.application.port.in.GetConversationUseCase;
import dev.civicpulse.messaging.application.port.in.ManageConversationUseCase;
import dev.civicpulse.messaging.domain.model.Conversation;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/conversations")
public class ConversationController {

  private final ManageConversationUseCase manageConversationUseCase;
  private final GetConversationUseCase getConversationUseCase;

  public ConversationController(ManageConversationUseCase manageConversationUseCase, GetConversationUseCase getConversationUseCase) {
    this.manageConversationUseCase = manageConversationUseCase;
    this.getConversationUseCase = getConversationUseCase;
  }

  /** {@code X-Account-Id} is the caller's own id, forwarded by the Gateway after JWT
   * validation — see docs/architecture/system-architecture.html. */
  @PostMapping("/direct")
  public ResponseEntity<ConversationResponse> startDirect(
      @RequestHeader("X-Account-Id") UUID accountId, @Valid @RequestBody StartDirectConversationRequest request) {
    Conversation conversation = manageConversationUseCase.startDirect(accountId, request.otherAccountId());
    return created(conversation);
  }

  /** Group membership eligibility (politician/party only) is enforced by whatever picker the
   * caller used to gather {@code participantAccountIds} — see MessagingServiceApplication's
   * scope note. The caller itself is always added as a participant (like {@link #startDirect}
   * already does), even if the picker didn't include them — otherwise the group's own creator
   * can't send a message into the conversation they just started. */
  @PostMapping("/group")
  public ResponseEntity<ConversationResponse> startGroup(
      @RequestHeader("X-Account-Id") UUID accountId, @Valid @RequestBody StartGroupConversationRequest request) {
    List<UUID> participantAccountIds = new ArrayList<>(request.participantAccountIds());
    if (!participantAccountIds.contains(accountId)) {
      participantAccountIds.add(accountId);
    }
    Conversation conversation = manageConversationUseCase.startGroup(participantAccountIds, request.groupName(), request.groupAvatarUrl());
    return created(conversation);
  }

  @GetMapping("/{id}")
  public ConversationResponse getById(@PathVariable UUID id) {
    return ConversationResponse.from(getConversationUseCase.getById(id));
  }

  @GetMapping
  public List<ConversationResponse> listByParticipant(@RequestHeader("X-Account-Id") UUID accountId) {
    return getConversationUseCase.listByParticipant(accountId).stream().map(ConversationResponse::from).toList();
  }

  @GetMapping("/{id}/participants")
  public List<ParticipantResponse> listParticipants(@PathVariable UUID id) {
    return getConversationUseCase.listParticipants(id).stream().map(ParticipantResponse::from).toList();
  }

  @PostMapping("/{id}/read")
  public ResponseEntity<Void> markRead(@PathVariable UUID id, @RequestHeader("X-Account-Id") UUID accountId) {
    manageConversationUseCase.markRead(id, accountId);
    return ResponseEntity.noContent().build();
  }

  @PutMapping("/{id}/group-name")
  public ConversationResponse renameGroup(
      @PathVariable UUID id, @RequestHeader("X-Account-Id") UUID accountId, @Valid @RequestBody UpdateGroupNameRequest request) {
    return ConversationResponse.from(manageConversationUseCase.renameGroup(id, accountId, request.groupName()));
  }

  @PutMapping("/{id}/group-avatar")
  public ConversationResponse changeGroupAvatar(
      @PathVariable UUID id, @RequestHeader("X-Account-Id") UUID accountId, @RequestBody UpdateGroupAvatarRequest request) {
    return ConversationResponse.from(manageConversationUseCase.changeGroupAvatar(id, accountId, request.groupAvatarUrl()));
  }

  private static ResponseEntity<ConversationResponse> created(Conversation conversation) {
    ConversationResponse body = ConversationResponse.from(conversation);
    return ResponseEntity.created(URI.create("/conversations/" + body.id())).body(body);
  }
}
