package dev.civicpulse.legislative.adapter.out.persistence;

import dev.civicpulse.legislative.application.port.out.TransparencyReportRepository;
import dev.civicpulse.legislative.domain.model.TransparencyReport;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class TransparencyReportRepositoryAdapter implements TransparencyReportRepository {

  private final TransparencyReportJpaRepository jpaRepository;
  private final TransparencyReportMapper mapper;

  TransparencyReportRepositoryAdapter(TransparencyReportJpaRepository jpaRepository, TransparencyReportMapper mapper) {
    this.jpaRepository = jpaRepository;
    this.mapper = mapper;
  }

  @Override
  public TransparencyReport save(TransparencyReport report) {
    return mapper.toDomain(jpaRepository.save(mapper.toEntity(report)));
  }

  @Override
  public Optional<TransparencyReport> findById(UUID politicianAccountId) {
    return jpaRepository.findById(politicianAccountId).map(mapper::toDomain);
  }
}
