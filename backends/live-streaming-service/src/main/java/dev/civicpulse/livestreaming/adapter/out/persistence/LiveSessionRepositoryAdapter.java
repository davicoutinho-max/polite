package dev.civicpulse.livestreaming.adapter.out.persistence;

import dev.civicpulse.livestreaming.application.port.out.LiveSessionRepository;
import dev.civicpulse.livestreaming.domain.model.LiveSession;
import dev.civicpulse.livestreaming.domain.model.LiveSessionStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class LiveSessionRepositoryAdapter implements LiveSessionRepository {

  private final LiveSessionJpaRepository jpaRepository;
  private final LiveSessionMapper mapper;

  LiveSessionRepositoryAdapter(LiveSessionJpaRepository jpaRepository, LiveSessionMapper mapper) {
    this.jpaRepository = jpaRepository;
    this.mapper = mapper;
  }

  @Override
  public LiveSession save(LiveSession session) {
    return mapper.toDomain(jpaRepository.save(mapper.toEntity(session)));
  }

  @Override
  public Optional<LiveSession> findById(UUID id) {
    return jpaRepository.findById(id).map(mapper::toDomain);
  }

  @Override
  public List<LiveSession> findLive() {
    return jpaRepository.findByStatus(LiveSessionStatus.LIVE).stream().map(mapper::toDomain).toList();
  }
}
