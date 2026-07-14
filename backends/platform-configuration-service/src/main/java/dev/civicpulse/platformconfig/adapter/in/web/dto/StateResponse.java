package dev.civicpulse.platformconfig.adapter.in.web.dto;

import dev.civicpulse.platformconfig.domain.model.State;
import java.util.UUID;

public record StateResponse(UUID id, UUID countryId, String name, String code) {

  public static StateResponse from(State state) {
    return new StateResponse(state.id(), state.countryId(), state.name(), state.code());
  }
}
