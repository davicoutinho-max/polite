package dev.civicpulse.legislative.adapter.out.persistence;

import dev.civicpulse.legislative.domain.model.AccountabilityCategory;
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
  private AccountabilityCategory category;

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

  @Column(name = "submitted_at", nullable = false)
  private Instant submittedAt;

  protected AccountabilityDisclosureJpaEntity() {}

  public AccountabilityDisclosureJpaEntity(
      UUID id,
      UUID politicianAccountId,
      AccountabilityCategory category,
      long declaredAmountCents,
      String documentUrl,
      DisclosureStatus status,
      Long extractedAmountCents,
      String aiFeedback,
      Instant submittedAt) {
    this.id = id;
    this.politicianAccountId = politicianAccountId;
    this.category = category;
    this.declaredAmountCents = declaredAmountCents;
    this.documentUrl = documentUrl;
    this.status = status;
    this.extractedAmountCents = extractedAmountCents;
    this.aiFeedback = aiFeedback;
    this.submittedAt = submittedAt;
  }

  public UUID getId() {
    return id;
  }

  public UUID getPoliticianAccountId() {
    return politicianAccountId;
  }

  public AccountabilityCategory getCategory() {
    return category;
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

  public Instant getSubmittedAt() {
    return submittedAt;
  }
}
