package dev.civicpulse.elections.adapter.in.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.UUID;

public record SyncElectionResultsRequest(@NotBlank String office, @NotEmpty @Valid List<ResultItem> results) {

  public record ResultItem(
      @NotBlank String externalId,
      @NotBlank String candidateName,
      String partyAcronym,
      long votes,
      int rank,
      boolean elected,
      UUID politicianAccountId) {}
}
