package dev.civicpulse.legislative.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/** One accountability ("prestação de contas") submission: a politician declares an amount spent
 * under one real public-money line item (matching the compensation/CEAP/office-budget structure
 * shown on the transparency tab — see accountability_category_options), for a specific
 * month/year, and attaches a supporting document. An AI reviewer (assistant-service, backed by
 * Gemini's document understanding — see DocumentVerificationGateway) checks whether the document
 * actually supports the declared amount. Immutable once scored — a rejected submission is never
 * edited, only superseded by a new one for the same category+period, so the full attempt history
 * (and each attempt's AI feedback) stays visible. */
public final class AccountabilityDisclosure {

  private final UUID id;
  private final UUID politicianAccountId;
  private final String category;
  private final int periodMonth;
  private final int periodYear;
  private final long declaredAmountCents;
  private final String documentUrl;
  private final DisclosureStatus status;
  private final Long extractedAmountCents;
  private final String aiFeedback;
  private final String notes;
  private final Instant submittedAt;

  private AccountabilityDisclosure(
      UUID id,
      UUID politicianAccountId,
      String category,
      int periodMonth,
      int periodYear,
      long declaredAmountCents,
      String documentUrl,
      DisclosureStatus status,
      Long extractedAmountCents,
      String aiFeedback,
      String notes,
      Instant submittedAt) {
    this.id = Objects.requireNonNull(id);
    this.politicianAccountId = Objects.requireNonNull(politicianAccountId);
    this.category = requireNonBlank(category, "category");
    if (periodMonth < 1 || periodMonth > 12) {
      throw new IllegalArgumentException("periodMonth must be between 1 and 12");
    }
    this.periodMonth = periodMonth;
    this.periodYear = periodYear;
    if (declaredAmountCents < 0) {
      throw new IllegalArgumentException("declaredAmountCents must not be negative");
    }
    this.declaredAmountCents = declaredAmountCents;
    this.documentUrl = requireNonBlank(documentUrl, "documentUrl");
    this.status = Objects.requireNonNull(status);
    this.extractedAmountCents = extractedAmountCents;
    this.aiFeedback = requireNonBlank(aiFeedback, "aiFeedback");
    this.notes = notes == null || notes.isBlank() ? null : notes;
    this.submittedAt = Objects.requireNonNull(submittedAt);
  }

  public static AccountabilityDisclosure score(
      UUID politicianAccountId,
      String category,
      int periodMonth,
      int periodYear,
      long declaredAmountCents,
      String documentUrl,
      DisclosureStatus status,
      Long extractedAmountCents,
      String aiFeedback,
      String notes,
      Instant now) {
    return new AccountabilityDisclosure(
        UUID.randomUUID(),
        politicianAccountId,
        category,
        periodMonth,
        periodYear,
        declaredAmountCents,
        documentUrl,
        status,
        extractedAmountCents,
        aiFeedback,
        notes,
        now);
  }

  public static AccountabilityDisclosure reconstitute(
      UUID id,
      UUID politicianAccountId,
      String category,
      int periodMonth,
      int periodYear,
      long declaredAmountCents,
      String documentUrl,
      DisclosureStatus status,
      Long extractedAmountCents,
      String aiFeedback,
      String notes,
      Instant submittedAt) {
    return new AccountabilityDisclosure(
        id,
        politicianAccountId,
        category,
        periodMonth,
        periodYear,
        declaredAmountCents,
        documentUrl,
        status,
        extractedAmountCents,
        aiFeedback,
        notes,
        submittedAt);
  }

  private static String requireNonBlank(String value, String field) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(field + " must not be blank");
    }
    return value;
  }

  public UUID id() {
    return id;
  }

  public UUID politicianAccountId() {
    return politicianAccountId;
  }

  public String category() {
    return category;
  }

  public int periodMonth() {
    return periodMonth;
  }

  public int periodYear() {
    return periodYear;
  }

  public long declaredAmountCents() {
    return declaredAmountCents;
  }

  public String documentUrl() {
    return documentUrl;
  }

  public DisclosureStatus status() {
    return status;
  }

  public Optional<Long> extractedAmountCents() {
    return Optional.ofNullable(extractedAmountCents);
  }

  public String aiFeedback() {
    return aiFeedback;
  }

  public Optional<String> notes() {
    return Optional.ofNullable(notes);
  }

  public Instant submittedAt() {
    return submittedAt;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof AccountabilityDisclosure other)) return false;
    return id.equals(other.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}
