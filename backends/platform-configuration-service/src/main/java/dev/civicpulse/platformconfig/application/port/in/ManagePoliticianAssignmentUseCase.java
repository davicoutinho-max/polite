package dev.civicpulse.platformconfig.application.port.in;

import dev.civicpulse.platformconfig.domain.model.PoliticianAssignment;
import java.util.UUID;

public interface ManagePoliticianAssignmentUseCase {

  /** Consumes {@code PoliticianRegistered} — auto-creates the initial assignment. */
  void onPoliticianRegistered(UUID politicianAccountId, UUID partyId);

  /** Platform Admin reassignment — independent of how the politician was originally
   * registered. */
  PoliticianAssignment reassign(UUID politicianAccountId, UUID newPartyId);

  PoliticianAssignment getAssignment(UUID politicianAccountId);
}
