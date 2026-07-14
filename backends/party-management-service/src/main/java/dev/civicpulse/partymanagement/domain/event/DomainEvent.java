package dev.civicpulse.partymanagement.domain.event;

import java.time.Instant;

public sealed interface DomainEvent
    permits PartyProfileUpdated,
        PoliticianRegistered,
        RepresentativeLinked,
        RepresentativeRemoved,
        AffiliationRequestApproved,
        AffiliationRequestRejected,
        PartyMemberStatusChanged {

  String topic();

  Instant occurredAt();
}
