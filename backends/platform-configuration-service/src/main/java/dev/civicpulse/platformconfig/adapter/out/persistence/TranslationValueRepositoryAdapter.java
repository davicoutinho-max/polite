package dev.civicpulse.platformconfig.adapter.out.persistence;

import dev.civicpulse.platformconfig.application.port.out.TranslationValueRepository;
import dev.civicpulse.platformconfig.domain.model.TranslationValue;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class TranslationValueRepositoryAdapter implements TranslationValueRepository {

  private final TranslationValueJpaRepository jpaRepository;

  TranslationValueRepositoryAdapter(TranslationValueJpaRepository jpaRepository) {
    this.jpaRepository = jpaRepository;
  }

  @Override
  public TranslationValue save(TranslationValue value) {
    var saved =
        jpaRepository.save(new TranslationValueJpaEntity(value.translationKeyId(), value.languageId(), value.value()));
    return toDomain(saved);
  }

  @Override
  public Optional<TranslationValue> findByKeyAndLanguage(UUID translationKeyId, String languageId) {
    return jpaRepository.findByTranslationKeyIdAndLanguageId(translationKeyId, languageId).map(TranslationValueRepositoryAdapter::toDomain);
  }

  @Override
  public List<TranslationValue> findByLanguage(String languageId) {
    return jpaRepository.findByLanguageId(languageId).stream().map(TranslationValueRepositoryAdapter::toDomain).toList();
  }

  private static TranslationValue toDomain(TranslationValueJpaEntity entity) {
    return TranslationValue.reconstitute(entity.getTranslationKeyId(), entity.getLanguageId(), entity.getValue());
  }
}
