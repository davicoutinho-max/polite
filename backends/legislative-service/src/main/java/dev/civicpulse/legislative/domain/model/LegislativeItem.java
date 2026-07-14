package dev.civicpulse.legislative.domain.model;

import dev.civicpulse.legislative.domain.exception.InvalidStatusTransitionException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/** Unifies the frontend mock's 5 separate arrays (projects/approvedLaws/rejectedLaws/pecs/cpis)
 * into one aggregate — see schema.sql's table comment. Status is forward-only
 * (filed-&gt;in_committee-&gt;floor_vote-&gt;passed) with a one-way exit to rejected from any
 * earlier status, mirroring PaymentStatus/AffiliationStatus elsewhere in the platform. */
public final class LegislativeItem {

  private final UUID id;
  private final UUID politicianAccountId;
  private final String reference;
  private final String title;
  private final String summary;
  private final LegislativeItemCategory category;
  private LegislativeItemStatus status;
  private final LocalDate itemDate;
  private final Set<UUID> cosponsorAccountIds;
  private final Instant createdAt;

  private LegislativeItem(
      UUID id,
      UUID politicianAccountId,
      String reference,
      String title,
      String summary,
      LegislativeItemCategory category,
      LegislativeItemStatus status,
      LocalDate itemDate,
      Set<UUID> cosponsorAccountIds,
      Instant createdAt) {
    this.id = id;
    this.politicianAccountId = Objects.requireNonNull(politicianAccountId);
    this.reference = requireNonBlank(reference, "reference");
    this.title = requireNonBlank(title, "title");
    this.summary = summary;
    this.category = Objects.requireNonNull(category);
    this.status = Objects.requireNonNull(status);
    this.itemDate = Objects.requireNonNull(itemDate);
    this.cosponsorAccountIds = new HashSet<>(cosponsorAccountIds);
    this.createdAt = Objects.requireNonNull(createdAt);
  }

  public static LegislativeItem file(
      UUID politicianAccountId,
      String reference,
      String title,
      String summary,
      LegislativeItemCategory category,
      LocalDate itemDate,
      Set<UUID> cosponsorAccountIds,
      Instant now) {
    return new LegislativeItem(
        null, politicianAccountId, reference, title, summary, category, LegislativeItemStatus.FILED, itemDate, cosponsorAccountIds, now);
  }

  public static LegislativeItem reconstitute(
      UUID id,
      UUID politicianAccountId,
      String reference,
      String title,
      String summary,
      LegislativeItemCategory category,
      LegislativeItemStatus status,
      LocalDate itemDate,
      Set<UUID> cosponsorAccountIds,
      Instant createdAt) {
    return new LegislativeItem(id, politicianAccountId, reference, title, summary, category, status, itemDate, cosponsorAccountIds, createdAt);
  }

  private static String requireNonBlank(String value, String field) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(field + " must not be blank");
    }
    return value;
  }

  public void advanceStatus(LegislativeItemStatus target) {
    Objects.requireNonNull(target);
    if (target == status) {
      return;
    }
    boolean isForwardStep = target != LegislativeItemStatus.REJECTED && target.sortOrder() == status.sortOrder() + 1;
    boolean isRejectionFromOpenState = target == LegislativeItemStatus.REJECTED && status != LegislativeItemStatus.PASSED && status != LegislativeItemStatus.REJECTED;
    if (!isForwardStep && !isRejectionFromOpenState) {
      throw new InvalidStatusTransitionException(status, target);
    }
    this.status = target;
  }

  public Optional<UUID> id() {
    return Optional.ofNullable(id);
  }

  public UUID politicianAccountId() {
    return politicianAccountId;
  }

  public String reference() {
    return reference;
  }

  public String title() {
    return title;
  }

  public Optional<String> summary() {
    return Optional.ofNullable(summary);
  }

  public LegislativeItemCategory category() {
    return category;
  }

  public LegislativeItemStatus status() {
    return status;
  }

  public LocalDate itemDate() {
    return itemDate;
  }

  public Set<UUID> cosponsorAccountIds() {
    return Set.copyOf(cosponsorAccountIds);
  }

  public Instant createdAt() {
    return createdAt;
  }
}
