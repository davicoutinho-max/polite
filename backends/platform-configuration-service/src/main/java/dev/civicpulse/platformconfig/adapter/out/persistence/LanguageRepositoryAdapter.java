package dev.civicpulse.platformconfig.adapter.out.persistence;

import dev.civicpulse.platformconfig.application.port.out.LanguageRepository;
import dev.civicpulse.platformconfig.domain.model.Language;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
class LanguageRepositoryAdapter implements LanguageRepository {

  private final LanguageJpaRepository jpaRepository;

  LanguageRepositoryAdapter(LanguageJpaRepository jpaRepository) {
    this.jpaRepository = jpaRepository;
  }

  @Override
  public Language save(Language language) {
    var saved = jpaRepository.save(new LanguageJpaEntity(language.id(), language.name(), language.code(), language.isDefault()));
    return toDomain(saved);
  }

  @Override
  public void delete(String id) {
    jpaRepository.deleteById(id);
  }

  @Override
  public Optional<Language> findById(String id) {
    return jpaRepository.findById(id).map(LanguageRepositoryAdapter::toDomain);
  }

  @Override
  public List<Language> findAll() {
    return jpaRepository.findAll().stream().map(LanguageRepositoryAdapter::toDomain).toList();
  }

  @Override
  public Optional<Language> findDefault() {
    return jpaRepository.findByIsDefaultTrue().map(LanguageRepositoryAdapter::toDomain);
  }

  @Override
  @Transactional
  public void clearDefaultFlag() {
    jpaRepository.clearDefaultFlag();
  }

  @Override
  @Transactional
  public void markAsDefault(String id) {
    jpaRepository.markAsDefault(id);
  }

  private static Language toDomain(LanguageJpaEntity entity) {
    return Language.reconstitute(entity.getId(), entity.getName(), entity.getCode(), entity.isDefault());
  }
}
