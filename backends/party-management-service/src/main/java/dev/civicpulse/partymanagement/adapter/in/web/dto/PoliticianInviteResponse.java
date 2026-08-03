package dev.civicpulse.partymanagement.adapter.in.web.dto;

import dev.civicpulse.partymanagement.application.port.out.RegistrationTokenGateway.IssuedToken;
import java.util.UUID;

public record PoliticianInviteResponse(UUID id, String token, String targetEmail, String status) {

  public static PoliticianInviteResponse from(IssuedToken t) {
    return new PoliticianInviteResponse(t.id(), t.token(), t.targetEmail(), t.status());
  }
}
