package dev.civicpulse.privacycompliance.adapter.out.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface ErasureAuditLogJpaRepository extends JpaRepository<ErasureAuditLogJpaEntity, Long> {

  List<ErasureAuditLogJpaEntity> findByDeletionRequestId(UUID deletionRequestId);

  boolean existsByDeletionRequestIdAndServiceName(UUID deletionRequestId, String serviceName);
}
