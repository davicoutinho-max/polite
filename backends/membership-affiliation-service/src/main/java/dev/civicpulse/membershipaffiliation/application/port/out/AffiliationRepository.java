package dev.civicpulse.membershipaffiliation.application.port.out;

import dev.civicpulse.membershipaffiliation.domain.model.Affiliation;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AffiliationRepository {

  Affiliation save(Affiliation affiliation);

  Optional<Affiliation> findById(UUID id);

  boolean existsActiveByCitizenAndParty(UUID citizenAccountId, UUID partyId);

  List<Affiliation> findByCitizenAccountId(UUID citizenAccountId);

  void deleteById(UUID id);
}
