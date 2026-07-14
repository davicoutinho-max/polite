package dev.civicpulse.identity.application.port.out;

import dev.civicpulse.identity.domain.model.DocumentStatus;
import dev.civicpulse.identity.domain.model.DocumentType;

/** Anti-corruption layer in front of a real KYC/Receita Federal provider — see "Security"
 * cross-cutting notes in docs/architecture/data-architecture.html. The shipped adapter is a
 * format-only stub; swapping in a real provider never touches application/domain code. */
public interface DocumentVerificationGateway {

  Result verify(DocumentType type, String rawDocumentNumber);

  record Result(DocumentStatus status, String providerName, String providerRef) {}
}
