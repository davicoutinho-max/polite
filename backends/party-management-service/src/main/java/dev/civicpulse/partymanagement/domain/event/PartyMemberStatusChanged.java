package dev.civicpulse.partymanagement.domain.event;

import java.time.Instant;
import java.util.UUID;

public record PartyMemberStatusChanged(UUID partyId, UUID citizenAccountId, String status, Instant occurredAt)
    implements DomainEvent {

  @Override
  public String topic() {
    return "party-member-status-changed";
  }
}
