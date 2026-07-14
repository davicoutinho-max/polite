package dev.civicpulse.legislative.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

public record FileLegislativeItemRequest(
    @NotNull UUID politicianAccountId,
    @NotBlank String reference,
    @NotBlank String title,
    String summary,
    @NotBlank String category,
    @NotNull LocalDate itemDate,
    Set<UUID> cosponsorAccountIds) {

  public Set<UUID> cosponsorAccountIdsOrEmpty() {
    return cosponsorAccountIds == null ? Set.of() : cosponsorAccountIds;
  }
}
