package dev.civicpulse.legislative.adapter.out.persistence;

import dev.civicpulse.legislative.application.port.out.TransparencyMetricRepository;
import dev.civicpulse.legislative.domain.model.TransparencyMetric;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class TransparencyMetricRepositoryAdapter implements TransparencyMetricRepository {

  private final TransparencyMetricJpaRepository jpaRepository;
  private final TransparencyMetricMapper mapper;

  TransparencyMetricRepositoryAdapter(TransparencyMetricJpaRepository jpaRepository, TransparencyMetricMapper mapper) {
    this.jpaRepository = jpaRepository;
    this.mapper = mapper;
  }

  @Override
  public TransparencyMetric save(TransparencyMetric metric) {
    return mapper.toDomain(jpaRepository.save(mapper.toEntity(metric)));
  }

  @Override
  public List<TransparencyMetric> findByPolitician(UUID politicianAccountId) {
    return jpaRepository.findByPoliticianAccountId(politicianAccountId).stream().map(mapper::toDomain).toList();
  }
}
