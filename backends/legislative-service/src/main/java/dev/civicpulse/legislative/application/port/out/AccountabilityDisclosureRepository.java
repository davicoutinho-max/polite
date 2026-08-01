package dev.civicpulse.legislative.application.port.out;

import dev.civicpulse.legislative.domain.model.AccountabilityDisclosure;
import java.util.List;
import java.util.UUID;

public interface AccountabilityDisclosureRepository {

  AccountabilityDisclosure save(AccountabilityDisclosure disclosure);

  /** Newest first — the section's current status is simply the first row per category. */
  List<AccountabilityDisclosure> findByPolitician(UUID politicianAccountId);
}
