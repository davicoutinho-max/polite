package dev.civicpulse.privacycompliance.application.port.in;

import dev.civicpulse.privacycompliance.domain.model.ConsentPurpose;
import dev.civicpulse.privacycompliance.domain.model.ConsentRecord;
import java.util.List;
import java.util.UUID;

public interface ManageConsentUseCase {

  ConsentRecord updateConsent(UUID accountId, ConsentPurpose purpose, boolean granted);

  List<ConsentRecord> listByAccount(UUID accountId);
}
