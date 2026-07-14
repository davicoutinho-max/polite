package dev.civicpulse.privacycompliance.application.port.out;

import dev.civicpulse.privacycompliance.domain.model.DataExportRequest;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DataExportRequestRepository {

  DataExportRequest save(DataExportRequest request);

  Optional<DataExportRequest> findById(UUID id);

  List<DataExportRequest> findByAccountId(UUID accountId);
}
