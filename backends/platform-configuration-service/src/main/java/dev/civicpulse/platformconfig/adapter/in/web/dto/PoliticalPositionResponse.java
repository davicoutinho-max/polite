package dev.civicpulse.platformconfig.adapter.in.web.dto;

import dev.civicpulse.platformconfig.domain.model.PoliticalPosition;
import java.util.UUID;

public record PoliticalPositionResponse(UUID id, String name) {

  public static PoliticalPositionResponse from(PoliticalPosition position) {
    return new PoliticalPositionResponse(position.id(), position.name());
  }
}
