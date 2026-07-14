package dev.civicpulse.platformconfig.domain.event;

import java.time.Instant;

public sealed interface DomainEvent
    permits PartyRegistered,
        PoliticianReassigned,
        LanguageAdded,
        LanguageRemoved,
        DefaultLanguageChanged,
        TranslationValueUpdated,
        CountryAdded,
        CountryRemoved {

  String topic();

  Instant occurredAt();
}
