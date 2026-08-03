package dev.civicpulse.identity.adapter.in.web.dto;

import dev.civicpulse.identity.application.port.in.CheckDocumentUseCase.SyncedAccountPreview;
import java.util.Optional;
import java.util.UUID;

public record CheckDocumentResponse(boolean matched, UUID accountId, String name, String avatarUrl, String accountType) {

  public static CheckDocumentResponse from(Optional<SyncedAccountPreview> preview) {
    return preview
        .map(p -> new CheckDocumentResponse(true, p.accountId(), p.name(), p.avatarUrl(), p.accountType()))
        .orElseGet(() -> new CheckDocumentResponse(false, null, null, null, null));
  }
}
