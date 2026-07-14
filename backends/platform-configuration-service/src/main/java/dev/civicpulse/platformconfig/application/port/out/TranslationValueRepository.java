package dev.civicpulse.platformconfig.application.port.out;

import dev.civicpulse.platformconfig.domain.model.TranslationValue;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TranslationValueRepository {

  TranslationValue save(TranslationValue value);

  Optional<TranslationValue> findByKeyAndLanguage(UUID translationKeyId, String languageId);

  List<TranslationValue> findByLanguage(String languageId);
}
