package dev.civicpulse.privacycompliance.domain.event;

import java.time.Instant;

public sealed interface DomainEvent permits ConsentUpdated, DataExportRequested, AccountDeletionRequested {

  String topic();

  Instant occurredAt();
}
