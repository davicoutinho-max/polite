package dev.civicpulse.legislative.adapter.out.persistence;

import dev.civicpulse.legislative.domain.model.ExpenseLine;
import org.springframework.stereotype.Component;

@Component
class ExpenseLineMapper {

  ExpenseLine toDomain(ExpenseLineJpaEntity entity) {
    return ExpenseLine.reconstitute(entity.getId(), entity.getPoliticianAccountId(), entity.getCategory(), entity.getAmountCents());
  }

  ExpenseLineJpaEntity toEntity(ExpenseLine expenseLine) {
    return new ExpenseLineJpaEntity(
        expenseLine.id().orElse(null), expenseLine.politicianAccountId(), expenseLine.category(), expenseLine.amountCents());
  }
}
