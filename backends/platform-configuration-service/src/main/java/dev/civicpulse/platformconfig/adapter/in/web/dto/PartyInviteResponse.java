package dev.civicpulse.platformconfig.adapter.in.web.dto;

import dev.civicpulse.platformconfig.application.port.out.RegistrationTokenGateway.IssuedToken;
import java.util.UUID;

public record PartyInviteResponse(UUID id, String token, String targetEmail, String status) {

  public static PartyInviteResponse from(IssuedToken t) {
    return new PartyInviteResponse(t.id(), t.token(), t.targetEmail(), t.status());
  }
}
