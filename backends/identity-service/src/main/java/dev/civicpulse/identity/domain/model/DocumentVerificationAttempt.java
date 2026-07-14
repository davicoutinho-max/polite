package dev.civicpulse.identity.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/** Audit trail of one call through the anti-corruption layer in front of a real KYC/Receita
 * Federal provider. See {@link dev.civicpulse.identity.application.port.out.DocumentVerificationGateway}. */
public final class DocumentVerificationAttempt {

  private final UUID id;
  private final AccountId accountId;
  private final DocumentType documentType;
  private final DocumentStatus status;
  private final String provider;
  private final String providerRef;
  private final Instant checkedAt;

  private DocumentVerificationAttempt(
      UUID id,
      AccountId accountId,
      DocumentType documentType,
      DocumentStatus status,
      String provider,
      String providerRef,
      Instant checkedAt) {
    this.id = Objects.requireNonNull(id);
    this.accountId = Objects.requireNonNull(accountId);
    this.documentType = Objects.requireNonNull(documentType);
    this.status = Objects.requireNonNull(status);
    this.provider = Objects.requireNonNull(provider);
    this.providerRef = providerRef;
    this.checkedAt = Objects.requireNonNull(checkedAt);
  }

  public static DocumentVerificationAttempt record(
      AccountId accountId,
      DocumentType documentType,
      DocumentStatus status,
      String provider,
      String providerRef,
      Instant now) {
    return new DocumentVerificationAttempt(UUID.randomUUID(), accountId, documentType, status, provider, providerRef, now);
  }

  public static DocumentVerificationAttempt reconstitute(
      UUID id,
      AccountId accountId,
      DocumentType documentType,
      DocumentStatus status,
      String provider,
      String providerRef,
      Instant checkedAt) {
    return new DocumentVerificationAttempt(id, accountId, documentType, status, provider, providerRef, checkedAt);
  }

  public UUID id() {
    return id;
  }

  public AccountId accountId() {
    return accountId;
  }

  public DocumentType documentType() {
    return documentType;
  }

  public DocumentStatus status() {
    return status;
  }

  public String provider() {
    return provider;
  }

  public Optional<String> providerRef() {
    return Optional.ofNullable(providerRef);
  }

  public Instant checkedAt() {
    return checkedAt;
  }
}
