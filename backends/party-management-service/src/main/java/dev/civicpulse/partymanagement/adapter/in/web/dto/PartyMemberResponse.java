package dev.civicpulse.partymanagement.adapter.in.web.dto;

import dev.civicpulse.partymanagement.domain.model.PartyMember;
import java.time.Instant;
import java.util.UUID;

public record PartyMemberResponse(UUID id, UUID partyId, UUID citizenAccountId, String city, String status, Instant joinedAt) {

  public static PartyMemberResponse from(PartyMember member) {
    return new PartyMemberResponse(
        member.id(), member.partyId(), member.citizenAccountId(), member.city().orElse(null), member.status().code(), member.joinedAt());
  }
}
