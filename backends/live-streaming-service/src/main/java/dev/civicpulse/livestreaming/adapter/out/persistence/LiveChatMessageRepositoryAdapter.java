package dev.civicpulse.livestreaming.adapter.out.persistence;

import dev.civicpulse.livestreaming.application.port.out.LiveChatMessageRepository;
import dev.civicpulse.livestreaming.domain.model.LiveChatMessage;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class LiveChatMessageRepositoryAdapter implements LiveChatMessageRepository {

  private final LiveChatMessageJpaRepository jpaRepository;
  private final LiveChatMessageMapper mapper;

  LiveChatMessageRepositoryAdapter(LiveChatMessageJpaRepository jpaRepository, LiveChatMessageMapper mapper) {
    this.jpaRepository = jpaRepository;
    this.mapper = mapper;
  }

  @Override
  public LiveChatMessage save(LiveChatMessage message) {
    return mapper.toDomain(jpaRepository.save(mapper.toEntity(message)));
  }

  @Override
  public List<LiveChatMessage> findBySessionId(UUID liveSessionId) {
    return jpaRepository.findByLiveSessionIdOrderBySentAtAsc(liveSessionId).stream().map(mapper::toDomain).toList();
  }

  @Override
  public long countBySessionId(UUID liveSessionId) {
    return jpaRepository.countByLiveSessionId(liveSessionId);
  }
}
