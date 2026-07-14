package dev.civicpulse.legislative.adapter.in.web.dto;

import dev.civicpulse.legislative.domain.model.LegislativeItem;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

public record LegislativeItemResponse(
    UUID id,
    UUID politicianAccountId,
    String reference,
    String title,
    String summary,
    String category,
    String status,
    LocalDate itemDate,
    Set<UUID> cosponsorAccountIds,
    Instant createdAt) {

  public static LegislativeItemResponse from(LegislativeItem item) {
    return new LegislativeItemResponse(
        item.id().orElse(null),
        item.politicianAccountId(),
        item.reference(),
        item.title(),
        item.summary().orElse(null),
        item.category().code(),
        item.status().code(),
        item.itemDate(),
        item.cosponsorAccountIds(),
        item.createdAt());
  }
}
