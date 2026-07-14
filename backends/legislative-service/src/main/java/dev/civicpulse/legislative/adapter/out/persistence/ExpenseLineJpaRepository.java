package dev.civicpulse.legislative.adapter.out.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

interface ExpenseLineJpaRepository extends JpaRepository<ExpenseLineJpaEntity, UUID> {

  List<ExpenseLineJpaEntity> findByPoliticianAccountId(UUID politicianAccountId);

  @Query("select coalesce(sum(e.amountCents), 0) from ExpenseLineJpaEntity e where e.politicianAccountId = :politicianAccountId")
  long sumAmountCentsByPoliticianAccountId(UUID politicianAccountId);
}
