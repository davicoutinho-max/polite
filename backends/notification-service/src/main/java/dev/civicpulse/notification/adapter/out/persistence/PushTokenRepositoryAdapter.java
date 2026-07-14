package dev.civicpulse.notification.adapter.out.persistence;

import dev.civicpulse.notification.application.port.out.PushTokenRepository;
import dev.civicpulse.notification.domain.model.PushToken;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class PushTokenRepositoryAdapter implements PushTokenRepository {

  private final PushTokenJpaRepository jpaRepository;
  private final PushTokenMapper mapper;

  PushTokenRepositoryAdapter(PushTokenJpaRepository jpaRepository, PushTokenMapper mapper) {
    this.jpaRepository = jpaRepository;
    this.mapper = mapper;
  }

  @Override
  public PushToken save(PushToken pushToken) {
    return mapper.toDomain(jpaRepository.save(mapper.toEntity(pushToken)));
  }

  @Override
  public List<PushToken> findByAccountId(UUID accountId) {
    return jpaRepository.findByAccountId(accountId).stream().map(mapper::toDomain).toList();
  }

  @Override
  public void delete(UUID accountId, String platform, String token) {
    PushTokenId id = new PushTokenId(accountId, platform, token);
    if (jpaRepository.existsById(id)) {
      jpaRepository.deleteById(id);
    }
  }
}
