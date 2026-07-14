package dev.civicpulse.elections.adapter.in.web.dto;

import dev.civicpulse.elections.domain.model.Election;
import java.time.LocalDate;
import java.util.UUID;

public record ElectionResponse(UUID id, String title, String scope, LocalDate electionDate, String description) {

  public static ElectionResponse from(Election election) {
    return new ElectionResponse(election.id(), election.title(), election.scope().code(), election.electionDate(), election.description().orElse(null));
  }
}
