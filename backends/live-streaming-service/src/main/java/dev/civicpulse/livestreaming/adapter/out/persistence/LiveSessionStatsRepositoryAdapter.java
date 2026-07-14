package dev.civicpulse.livestreaming.adapter.out.persistence;

import dev.civicpulse.livestreaming.application.port.out.LiveSessionStatsRepository;
import dev.civicpulse.livestreaming.domain.model.LiveSessionStats;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class LiveSessionStatsRepositoryAdapter implements LiveSessionStatsRepository {

  private final LiveSessionStatsJpaRepository jpaRepository;
  private final LiveSessionStatsMapper mapper;

  LiveSessionStatsRepositoryAdapter(LiveSessionStatsJpaRepository jpaRepository, LiveSessionStatsMapper mapper) {
    this.jpaRepository = jpaRepository;
    this.mapper = mapper;
  }

  @Override
  public LiveSessionStats save(LiveSessionStats stats) {
    return mapper.toDomain(jpaRepository.save(mapper.toEntity(stats)));
  }

  @Override
  public Optional<LiveSessionStats> findBySessionId(UUID liveSessionId) {
    return jpaRepository.findById(liveSessionId).map(mapper::toDomain);
  }
}
