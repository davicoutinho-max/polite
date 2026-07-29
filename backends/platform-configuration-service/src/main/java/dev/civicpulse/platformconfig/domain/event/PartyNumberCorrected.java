package dev.civicpulse.platformconfig.domain.event;

import java.time.Instant;
import java.util.UUID;

/** Published when a government-data sync discovers a party's real TSE electoral number after it
 * was first registered with a synthetic placeholder (see SyncPartyService — a party can be
 * created by a source that doesn't carry the real number, like Câmara's often-null
 * numeroEleitoral, before a later, more authoritative source, like TSE's own candidate data,
 * supplies the real one). Consumed by Directory Service to correct its own read-shadow, the only
 * place the number is user-visible. */
public record PartyNumberCorrected(UUID partyId, int number, Instant occurredAt) implements DomainEvent {

  @Override
  public String topic() {
    return "party-number-corrected";
  }
}
