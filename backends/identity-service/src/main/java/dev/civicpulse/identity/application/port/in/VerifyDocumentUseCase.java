package dev.civicpulse.identity.application.port.in;

import dev.civicpulse.identity.domain.model.AccountId;
import dev.civicpulse.identity.domain.model.DocumentVerificationAttempt;

public interface VerifyDocumentUseCase {

  DocumentVerificationAttempt verify(AccountId accountId);
}
