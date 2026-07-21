package dev.civicpulse.platformconfig.application.port.in;

import dev.civicpulse.platformconfig.domain.model.TranslationKey;
import dev.civicpulse.platformconfig.domain.model.TranslationValue;
import java.util.List;
import java.util.UUID;

public interface ManageTranslationUseCase {

  /** Creates the {@code TranslationKey} if it doesn't already exist, then upserts the value
   * for {@code languageId}. */
  TranslationValue setTranslation(String key, String languageId, String value);

  List<TranslationValue> getTranslationsForLanguage(String languageId);

  List<TranslationKey> listKeys();

  void deleteKey(UUID keyId);
}
