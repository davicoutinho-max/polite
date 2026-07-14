package dev.civicpulse.platformconfig.application.port.out;

import dev.civicpulse.platformconfig.domain.model.TranslationKey;
import java.util.Optional;
import java.util.UUID;

public interface TranslationKeyRepository {

  TranslationKey save(TranslationKey key);

  Optional<TranslationKey> findById(UUID id);

  Optional<TranslationKey> findByKey(String key);
}
