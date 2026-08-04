package dev.civicpulse.legislative.adapter.out.persistence;

import dev.civicpulse.legislative.domain.model.DisclosureStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "accountability_disclosures")
public class AccountabilityDisclosureJpaEntity {

  @Id private UUID id;

  @Column(name = "politician_account_id", nullable = false)
  private UUID politicianAccountId;

  @Column(nullable = false)
  private String category;

  @Column(name = "period_month", nullable = false)
  private int periodMonth;

  @Column(name = "period_year", nullable = false)
  private int periodYear;

  @Column(name = "declared_amount_cents", nullable = false)
  private long declaredAmountCents;

  @Column(name = "document_url", nullable = false)
  private String documentUrl;

  @Column(nullable = false)
  private DisclosureStatus status;

  @Column(name = "extracted_amount_cents")
  private Long extractedAmountCents;

  @Column(name = "ai_feedback", nullable = false)
  private String aiFeedback;

  @Column(name = "notes")
  private String notes;

  @Column(name = "submitted_at", nullable = false)
  private Instant submittedAt;

  protected AccountabilityDisclosureJpaEntity() {}

  public AccountabilityDisclosureJpaEntity(
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
    this.id = id;
    this.politicianAccountId = politicianAccountId;
    this.category = category;
    this.periodMonth = periodMonth;
    this.periodYear = periodYear;
    this.declaredAmountCents = declaredAmountCents;
    this.documentUrl = documentUrl;
    this.status = status;
    this.extractedAmountCents = extractedAmountCents;
    this.aiFeedback = aiFeedback;
    this.notes = notes;
    this.submittedAt = submittedAt;
  }

  public UUID getId() {
    return id;
  }

  public UUID getPoliticianAccountId() {
    return politicianAccountId;
  }

  public String getCategory() {
    return category;
  }

  public int getPeriodMonth() {
    return periodMonth;
  }

  public int getPeriodYear() {
    return periodYear;
  }

  public long getDeclaredAmountCents() {
    return declaredAmountCents;
  }

  public String getDocumentUrl() {
    return documentUrl;
  }

  public DisclosureStatus getStatus() {
    return status;
  }

  public Long getExtractedAmountCents() {
    return extractedAmountCents;
  }

  public String getAiFeedback() {
    return aiFeedback;
  }

  public String getNotes() {
    return notes;
  }

  public Instant getSubmittedAt() {
    return submittedAt;
  }
}
