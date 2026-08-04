package dev.civicpulse.legislative.application.port.in;

import dev.civicpulse.legislative.domain.model.AccountabilityDisclosure;
import java.util.List;
import java.util.UUID;

public interface ManageAccountabilityDisclosureUseCase {

  /** Submits one accountability disclosure for AI review — always resolves synchronously to a
   * scored (approved or rejected) {@link AccountabilityDisclosure}, never a "pending" state,
   * since the AI call itself is synchronous. The politician may call this again with a new
   * document after a rejection; nothing here prevents or limits retries. {@code category} is a
   * free-text code validated against {@code accountability_category_options} at the database
   * level (FK constraint) — matches one of the compensation/CEAP/office-budget line items shown
   * on the transparency tab. {@code periodMonth}/{@code periodYear} identify which month this
   * declaration covers. {@code notes} is optional free-text context (why the expense was
   * necessary, what it covers) — not sent to the AI reviewer, purely for the politician's own
   * public record. */
  AccountabilityDisclosure submit(
      UUID politicianAccountId,
      String category,
      int periodMonth,
      int periodYear,
      long declaredAmountCents,
      String documentUrl,
      String notes);

  /** Full submission history, newest first. */
  List<AccountabilityDisclosure> listByPolitician(UUID politicianAccountId);
}
