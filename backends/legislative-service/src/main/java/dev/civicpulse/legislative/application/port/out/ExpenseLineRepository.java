package dev.civicpulse.legislative.application.port.out;

import dev.civicpulse.legislative.domain.model.ExpenseLine;
import java.util.List;
import java.util.UUID;

public interface ExpenseLineRepository {

  ExpenseLine save(ExpenseLine expenseLine);

  List<ExpenseLine> findByPolitician(UUID politicianAccountId);

  long sumAmountCentsByPolitician(UUID politicianAccountId);
}
