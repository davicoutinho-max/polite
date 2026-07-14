package dev.civicpulse.legislative.adapter.in.web.dto;

import dev.civicpulse.legislative.domain.model.Mandate;
import java.util.UUID;

public record MandateResponse(UUID id, String role, String period, boolean current) {

  public static MandateResponse from(Mandate mandate) {
    return new MandateResponse(mandate.id().orElse(null), mandate.role(), mandate.period(), mandate.current());
  }
}
