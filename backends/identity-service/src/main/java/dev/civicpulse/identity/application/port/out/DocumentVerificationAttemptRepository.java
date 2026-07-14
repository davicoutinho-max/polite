package dev.civicpulse.identity.application.port.out;

import dev.civicpulse.identity.domain.model.DocumentVerificationAttempt;

public interface DocumentVerificationAttemptRepository {

  DocumentVerificationAttempt save(DocumentVerificationAttempt attempt);
}
