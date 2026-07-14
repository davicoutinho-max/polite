package dev.civicpulse.privacycompliance.adapter.out.persistence;

import dev.civicpulse.privacycompliance.application.port.out.DataExportRequestRepository;
import dev.civicpulse.privacycompliance.domain.model.DataExportRequest;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class DataExportRequestRepositoryAdapter implements DataExportRequestRepository {

  private final DataExportRequestJpaRepository jpaRepository;
  private final DataExportRequestMapper mapper;

  DataExportRequestRepositoryAdapter(DataExportRequestJpaRepository jpaRepository, DataExportRequestMapper mapper) {
    this.jpaRepository = jpaRepository;
    this.mapper = mapper;
  }

  @Override
  public DataExportRequest save(DataExportRequest request) {
    return mapper.toDomain(jpaRepository.save(mapper.toEntity(request)));
  }

  @Override
  public Optional<DataExportRequest> findById(UUID id) {
    return jpaRepository.findById(id).map(mapper::toDomain);
  }

  @Override
  public List<DataExportRequest> findByAccountId(UUID accountId) {
    return jpaRepository.findByAccountIdOrderByRequestedAtDesc(accountId).stream().map(mapper::toDomain).toList();
  }
}
