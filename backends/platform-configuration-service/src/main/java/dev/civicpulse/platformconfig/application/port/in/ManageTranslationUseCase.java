package dev.civicpulse.platformconfig.application.port.in;

import dev.civicpulse.platformconfig.domain.model.TranslationValue;
import java.util.List;

public interface ManageTranslationUseCase {

  /** Creates the {@code TranslationKey} if it doesn't already exist, then upserts the value
   * for {@code languageId}. */
  TranslationValue setTranslation(String key, String languageId, String value);

  List<TranslationValue> getTranslationsForLanguage(String languageId);
}
