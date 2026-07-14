package dev.civicpulse.platformconfig.adapter.out.persistence;

import dev.civicpulse.platformconfig.application.port.out.TranslationKeyRepository;
import dev.civicpulse.platformconfig.domain.model.TranslationKey;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class TranslationKeyRepositoryAdapter implements TranslationKeyRepository {

  private final TranslationKeyJpaRepository jpaRepository;

  TranslationKeyRepositoryAdapter(TranslationKeyJpaRepository jpaRepository) {
    this.jpaRepository = jpaRepository;
  }

  @Override
  public TranslationKey save(TranslationKey key) {
    var saved = jpaRepository.save(new TranslationKeyJpaEntity(key.id(), key.key()));
    return toDomain(saved);
  }

  @Override
  public Optional<TranslationKey> findById(UUID id) {
    return jpaRepository.findById(id).map(TranslationKeyRepositoryAdapter::toDomain);
  }

  @Override
  public Optional<TranslationKey> findByKey(String key) {
    return jpaRepository.findByKey(key).map(TranslationKeyRepositoryAdapter::toDomain);
  }

  private static TranslationKey toDomain(TranslationKeyJpaEntity entity) {
    return TranslationKey.reconstitute(entity.getId(), entity.getKey());
  }
}
