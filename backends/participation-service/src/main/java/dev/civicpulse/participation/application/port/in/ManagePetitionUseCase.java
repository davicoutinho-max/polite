package dev.civicpulse.participation.application.port.in;

import dev.civicpulse.participation.domain.model.Petition;
import dev.civicpulse.participation.domain.model.PetitionType;
import java.time.LocalDate;
import java.util.UUID;

public interface ManagePetitionUseCase {

  Petition create(
      String title,
      String summary,
      String category,
      int goal,
      LocalDate deadline,
      String imageUrl,
      String videoUrl,
      String fileUrl,
      String fileName,
      PetitionType petitionType);

  /** Step 1 of signing: validates the CPF and captures every tier-specific field, then issues a
   * verification code (SMS/email for Apoio Verificado, an identity-check pass for Iniciativa
   * Popular) — nothing is recorded as a real signature yet. */
  SignatureVerificationStarted startSignature(UUID petitionId, UUID citizenAccountId, StartSignatureCommand command);

  /** Step 2: confirms the code and, only then, materializes the actual signature — incrementing
   * the petition's count and publishing the domain event. */
  void confirmSignature(UUID petitionId, UUID citizenAccountId, UUID verificationId, String code);
}
