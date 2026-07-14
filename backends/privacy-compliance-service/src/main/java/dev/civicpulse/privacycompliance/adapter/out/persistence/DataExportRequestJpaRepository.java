package dev.civicpulse.privacycompliance.adapter.out.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface DataExportRequestJpaRepository extends JpaRepository<DataExportRequestJpaEntity, UUID> {

  List<DataExportRequestJpaEntity> findByAccountIdOrderByRequestedAtDesc(UUID accountId);
}
