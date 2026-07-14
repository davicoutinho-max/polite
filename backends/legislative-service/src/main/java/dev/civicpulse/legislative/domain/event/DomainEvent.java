package dev.civicpulse.legislative.domain.event;

import java.time.Instant;

public sealed interface DomainEvent
    permits LegislativeItemFiled, LegislativeItemStatusChanged, VoteCast, CommitteeMembershipChanged {

  String topic();

  Instant occurredAt();
}
