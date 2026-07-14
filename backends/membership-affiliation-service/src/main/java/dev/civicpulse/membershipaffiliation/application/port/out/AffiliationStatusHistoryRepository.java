package dev.civicpulse.membershipaffiliation.application.port.out;

import dev.civicpulse.membershipaffiliation.domain.model.AffiliationStatusHistoryEntry;
import java.util.List;
import java.util.UUID;

public interface AffiliationStatusHistoryRepository {

  AffiliationStatusHistoryEntry save(AffiliationStatusHistoryEntry entry);

  List<AffiliationStatusHistoryEntry> findByAffiliationId(UUID affiliationId);
}
