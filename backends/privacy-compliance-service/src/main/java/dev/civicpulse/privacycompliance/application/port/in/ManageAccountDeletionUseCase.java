package dev.civicpulse.privacycompliance.application.port.in;

import dev.civicpulse.privacycompliance.domain.model.AccountDeletionRequest;
import dev.civicpulse.privacycompliance.domain.model.ErasureAuditEntry;
import java.util.List;
import java.util.UUID;

public interface ManageAccountDeletionUseCase {

  AccountDeletionRequest request(UUID accountId);

  AccountDeletionRequest confirm(UUID requestId);

  AccountDeletionRequest startProcessing(UUID requestId);

  AccountDeletionRequest cancel(UUID requestId);

  /** Idempotent under Kafka redelivery — records the report, then auto-completes the saga once
   * every {@link dev.civicpulse.privacycompliance.domain.model.ExpectedErasureServices} entry has
   * reported. */
  void onErasureReported(UUID deletionRequestId, String serviceName, Integer recordCount);

  AccountDeletionRequest getById(UUID requestId);

  List<AccountDeletionRequest> listByAccount(UUID accountId);

  List<ErasureAuditEntry> listErasureAudit(UUID deletionRequestId);
}
