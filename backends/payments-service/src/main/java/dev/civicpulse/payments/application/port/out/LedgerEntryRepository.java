package dev.civicpulse.payments.application.port.out;

import dev.civicpulse.payments.domain.model.LedgerEntry;
import java.util.List;
import java.util.UUID;

public interface LedgerEntryRepository {

  LedgerEntry save(LedgerEntry entry);

  /** The account's current balance, i.e. the running balance on its most recent ledger entry
   * (0 if it has none yet) — used to compute the next entry's running balance. */
  long currentBalance(UUID accountId);

  List<LedgerEntry> findByAccountId(UUID accountId);
}
