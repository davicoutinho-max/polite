package dev.civicpulse.legislative.application.port.in;

import dev.civicpulse.legislative.domain.model.AccountabilityCategory;
import dev.civicpulse.legislative.domain.model.AccountabilityDisclosure;
import java.util.List;
import java.util.UUID;

public interface ManageAccountabilityDisclosureUseCase {

  /** Submits one accountability disclosure for AI review — always resolves synchronously to a
   * scored (approved or rejected) {@link AccountabilityDisclosure}, never a "pending" state,
   * since the AI call itself is synchronous. The politician may call this again with a new
   * document after a rejection; nothing here prevents or limits retries. */
  AccountabilityDisclosure submit(UUID politicianAccountId, AccountabilityCategory category, long declaredAmountCents, String documentUrl);

  /** Full submission history, newest first. */
  List<AccountabilityDisclosure> listByPolitician(UUID politicianAccountId);
}
