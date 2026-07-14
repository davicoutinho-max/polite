package dev.civicpulse.partymanagement.adapter.in.web.dto;

import dev.civicpulse.partymanagement.domain.model.PartyOffice;
import java.util.UUID;

public record OfficeResponse(UUID id, UUID partyId, String scope, String location, String leaderName, int memberCount) {

  public static OfficeResponse from(PartyOffice office) {
    return new OfficeResponse(
        office.id(), office.partyId(), office.scope().code(), office.location(), office.leaderName().orElse(null), office.memberCount());
  }
}
