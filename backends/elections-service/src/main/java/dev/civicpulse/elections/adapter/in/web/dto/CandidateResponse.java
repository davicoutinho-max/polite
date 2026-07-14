package dev.civicpulse.elections.adapter.in.web.dto;

import dev.civicpulse.elections.application.port.out.PoliticianDirectoryGateway.PoliticianSummary;
import java.util.UUID;

public record CandidateResponse(UUID accountId, String name, String handle, String avatarUrl, boolean verified, String office, String partyAcronym) {

  public static CandidateResponse from(PoliticianSummary summary) {
    return new CandidateResponse(
        summary.accountId(), summary.name(), summary.handle(), summary.avatarUrl(), summary.verified(), summary.office(), summary.partyAcronym());
  }
}
