package dev.civicpulse.elections.adapter.in.web.dto;

import dev.civicpulse.elections.domain.model.PersonalVote;
import java.time.Instant;
import java.util.UUID;

public record PersonalVoteResponse(
    UUID id, String office, String candidateName, String candidatePartyAcronym, UUID politicianAccountId, Instant castAt) {

  public static PersonalVoteResponse from(PersonalVote vote) {
    return new PersonalVoteResponse(
        vote.id(),
        vote.office(),
        vote.candidateName(),
        vote.candidatePartyAcronym().orElse(null),
        vote.politicianAccountId().orElse(null),
        vote.castAt());
  }
}
