package dev.civicpulse.privacycompliance.adapter.out.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface ConsentRecordJpaRepository extends JpaRepository<ConsentRecordJpaEntity, ConsentRecordId> {

  Optional<ConsentRecordJpaEntity> findByAccountIdAndPurpose(UUID accountId, String purpose);

  List<ConsentRecordJpaEntity> findByAccountId(UUID accountId);
}
