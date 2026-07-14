package dev.civicpulse.legislative.adapter.out.persistence;

import dev.civicpulse.legislative.application.port.out.ExpenseLineRepository;
import dev.civicpulse.legislative.domain.model.ExpenseLine;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class ExpenseLineRepositoryAdapter implements ExpenseLineRepository {

  private final ExpenseLineJpaRepository jpaRepository;
  private final ExpenseLineMapper mapper;

  ExpenseLineRepositoryAdapter(ExpenseLineJpaRepository jpaRepository, ExpenseLineMapper mapper) {
    this.jpaRepository = jpaRepository;
    this.mapper = mapper;
  }

  @Override
  public ExpenseLine save(ExpenseLine expenseLine) {
    return mapper.toDomain(jpaRepository.save(mapper.toEntity(expenseLine)));
  }

  @Override
  public List<ExpenseLine> findByPolitician(UUID politicianAccountId) {
    return jpaRepository.findByPoliticianAccountId(politicianAccountId).stream().map(mapper::toDomain).toList();
  }

  @Override
  public long sumAmountCentsByPolitician(UUID politicianAccountId) {
    return jpaRepository.sumAmountCentsByPoliticianAccountId(politicianAccountId);
  }
}
