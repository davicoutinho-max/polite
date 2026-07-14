package dev.civicpulse.platformconfig.adapter.out.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface TranslationValueJpaRepository extends JpaRepository<TranslationValueJpaEntity, TranslationValueId> {

  Optional<TranslationValueJpaEntity> findByTranslationKeyIdAndLanguageId(UUID translationKeyId, String languageId);

  List<TranslationValueJpaEntity> findByLanguageId(String languageId);
}
