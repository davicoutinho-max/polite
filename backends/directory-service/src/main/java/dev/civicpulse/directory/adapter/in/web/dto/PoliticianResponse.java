package dev.civicpulse.directory.adapter.in.web.dto;

import dev.civicpulse.directory.domain.model.Politician;
import java.time.Instant;
import java.util.UUID;

public record PoliticianResponse(
    UUID accountId,
    String name,
    String handle,
    String avatarUrl,
    boolean verified,
    String office,
    String level,
    UUID partyId,
    String partyAcronym,
    String state,
    int followersCount,
    int billsCount,
    Instant updatedAt) {

  public static PoliticianResponse from(Politician politician) {
    return new PoliticianResponse(
        politician.accountId(),
        politician.name(),
        politician.handle(),
        politician.avatarUrl().orElse(null),
        politician.verified(),
        politician.office().orElse(null),
        politician.level().map(l -> l.code()).orElse(null),
        politician.partyId().orElse(null),
        politician.partyAcronym().orElse(null),
        politician.state().orElse(null),
        politician.followersCount(),
        politician.billsCount(),
        politician.updatedAt());
  }
}
