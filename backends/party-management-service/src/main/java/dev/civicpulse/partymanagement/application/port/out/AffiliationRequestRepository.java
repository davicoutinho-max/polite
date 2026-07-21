package dev.civicpulse.partymanagement.application.port.out;

import dev.civicpulse.partymanagement.domain.model.AffiliationRequest;
import dev.civicpulse.partymanagement.domain.model.AffiliationRequestStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AffiliationRequestRepository {

  AffiliationRequest save(AffiliationRequest request);

  Optional<AffiliationRequest> findById(UUID id);

  List<AffiliationRequest> findByPartyIdAndStatus(UUID partyId, AffiliationRequestStatus status);

  void deleteById(UUID id);
}
