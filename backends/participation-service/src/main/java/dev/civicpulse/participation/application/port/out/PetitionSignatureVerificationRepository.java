package dev.civicpulse.participation.application.port.out;

import dev.civicpulse.participation.domain.model.PendingSignatureVerification;
import java.util.Optional;
import java.util.UUID;

public interface PetitionSignatureVerificationRepository {

  PendingSignatureVerification save(PendingSignatureVerification verification);

  Optional<PendingSignatureVerification> findById(UUID id);
}
