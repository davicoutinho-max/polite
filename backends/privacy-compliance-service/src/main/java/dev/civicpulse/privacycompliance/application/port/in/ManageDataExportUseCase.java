package dev.civicpulse.privacycompliance.application.port.in;

import dev.civicpulse.privacycompliance.domain.model.DataExportRequest;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface ManageDataExportUseCase {

  DataExportRequest request(UUID accountId);

  DataExportRequest startProcessing(UUID requestId);

  DataExportRequest markReady(UUID requestId, String downloadUrl, Instant expiresAt);

  DataExportRequest markFailed(UUID requestId);

  DataExportRequest getById(UUID requestId);

  List<DataExportRequest> listByAccount(UUID accountId);
}
