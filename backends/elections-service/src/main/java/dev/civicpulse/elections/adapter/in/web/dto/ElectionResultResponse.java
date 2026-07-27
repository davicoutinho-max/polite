package dev.civicpulse.elections.adapter.in.web.dto;

import dev.civicpulse.elections.domain.model.ElectionResult;
import java.util.UUID;

public record ElectionResultResponse(
    UUID id,
    String office,
    String candidateName,
    String partyAcronym,
    long votes,
    int rank,
    boolean elected,
    UUID politicianAccountId) {

  public static ElectionResultResponse from(ElectionResult result) {
    return new ElectionResultResponse(
        result.id(),
        result.office(),
        result.candidateName(),
        result.partyAcronym().orElse(null),
        result.votes(),
        result.rank(),
        result.elected(),
        result.politicianAccountId().orElse(null));
  }
}
