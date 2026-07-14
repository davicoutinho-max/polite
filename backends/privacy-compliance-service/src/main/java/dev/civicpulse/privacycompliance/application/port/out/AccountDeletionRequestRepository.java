package dev.civicpulse.privacycompliance.application.port.out;

import dev.civicpulse.privacycompliance.domain.model.AccountDeletionRequest;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountDeletionRequestRepository {

  AccountDeletionRequest save(AccountDeletionRequest request);

  Optional<AccountDeletionRequest> findById(UUID id);

  List<AccountDeletionRequest> findByAccountId(UUID accountId);
}
