package dev.civicpulse.identity.adapter.out.verification;

import dev.civicpulse.identity.application.port.out.DocumentVerificationGateway;
import dev.civicpulse.identity.domain.model.DocumentStatus;
import dev.civicpulse.identity.domain.model.DocumentType;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Format-only stand-in for a real Receita Federal / KYC provider integration. Every call
 * "verifies" successfully — this is the anti-corruption-layer boundary documented in
 * docs/architecture/data-architecture.html ("CpfVerificationGateway anti-corruption layer");
 * a real implementation of {@link DocumentVerificationGateway} plugs in here without any
 * change to {@link dev.civicpulse.identity.application.DocumentVerificationService} or the
 * domain model.
 */
@Component
public class StubDocumentVerificationGatewayAdapter implements DocumentVerificationGateway {

  private static final String PROVIDER_NAME = "stub-kyc-provider";

  @Override
  public Result verify(DocumentType type, String documentNumberHash) {
    // A real adapter would call out to an external provider with the raw document number
    // captured at registration time (never re-derived from the one-way hash stored here) and
    // map its response to DocumentStatus. The stub always succeeds so the rest of the flow —
    // event publication, account.markVerified() — is exercised end-to-end in every environment.
    return new Result(DocumentStatus.VERIFIED, PROVIDER_NAME, UUID.randomUUID().toString());
  }
}
