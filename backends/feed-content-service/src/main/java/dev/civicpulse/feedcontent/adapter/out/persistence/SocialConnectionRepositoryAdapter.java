package dev.civicpulse.feedcontent.adapter.out.persistence;

import dev.civicpulse.feedcontent.application.port.out.SocialConnectionRepository;
import dev.civicpulse.feedcontent.domain.model.SocialConnection;
import dev.civicpulse.feedcontent.domain.model.SocialPlatform;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class SocialConnectionRepositoryAdapter implements SocialConnectionRepository {

  private final SocialConnectionJpaRepository jpaRepository;
  private final SocialConnectionMapper mapper;

  SocialConnectionRepositoryAdapter(SocialConnectionJpaRepository jpaRepository, SocialConnectionMapper mapper) {
    this.jpaRepository = jpaRepository;
    this.mapper = mapper;
  }

  @Override
  public SocialConnection save(SocialConnection connection) {
    return mapper.toDomain(jpaRepository.save(mapper.toEntity(connection)));
  }

  @Override
  public Optional<SocialConnection> findByAccountAndPlatform(UUID accountId, SocialPlatform platform) {
    return jpaRepository.findByAccountIdAndPlatform(accountId, platform).map(mapper::toDomain);
  }

  @Override
  public List<SocialConnection> findByAccount(UUID accountId) {
    return jpaRepository.findByAccountId(accountId).stream().map(mapper::toDomain).toList();
  }

  @Override
  public void deleteByAccountAndPlatform(UUID accountId, SocialPlatform platform) {
    jpaRepository.deleteByAccountIdAndPlatform(accountId, platform);
  }
}
