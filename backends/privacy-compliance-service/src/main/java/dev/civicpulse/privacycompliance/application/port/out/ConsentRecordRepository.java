package dev.civicpulse.privacycompliance.application.port.out;

import dev.civicpulse.privacycompliance.domain.model.ConsentPurpose;
import dev.civicpulse.privacycompliance.domain.model.ConsentRecord;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConsentRecordRepository {

  ConsentRecord save(ConsentRecord consentRecord);

  Optional<ConsentRecord> findByAccountAndPurpose(UUID accountId, ConsentPurpose purpose);

  List<ConsentRecord> findByAccount(UUID accountId);
}
