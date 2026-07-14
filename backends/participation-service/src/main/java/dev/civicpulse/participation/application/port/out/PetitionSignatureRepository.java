package dev.civicpulse.participation.application.port.out;

import dev.civicpulse.participation.domain.model.PetitionSignature;
import java.util.UUID;

public interface PetitionSignatureRepository {

  PetitionSignature save(PetitionSignature signature);

  boolean exists(UUID petitionId, UUID citizenAccountId);
}
