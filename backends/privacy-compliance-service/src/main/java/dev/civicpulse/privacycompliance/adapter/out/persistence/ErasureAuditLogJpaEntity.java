package dev.civicpulse.privacycompliance.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "erasure_audit_log")
public class ErasureAuditLogJpaEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "deletion_request_id", nullable = false)
  private UUID deletionRequestId;

  @Column(name = "service_name", nullable = false)
  private String serviceName;

  @Column(name = "erased_at", nullable = false)
  private Instant erasedAt;

  @Column(name = "record_count")
  private Integer recordCount;

  protected ErasureAuditLogJpaEntity() {}

  public ErasureAuditLogJpaEntity(Long id, UUID deletionRequestId, String serviceName, Instant erasedAt, Integer recordCount) {
    this.id = id;
    this.deletionRequestId = deletionRequestId;
    this.serviceName = serviceName;
    this.erasedAt = erasedAt;
    this.recordCount = recordCount;
  }

  public Long getId() {
    return id;
  }

  public UUID getDeletionRequestId() {
    return deletionRequestId;
  }

  public String getServiceName() {
    return serviceName;
  }

  public Instant getErasedAt() {
    return erasedAt;
  }

  public Integer getRecordCount() {
    return recordCount;
  }
}
