package dev.civicpulse.platformconfig.application.port.out;

import dev.civicpulse.platformconfig.domain.model.Language;
import java.util.List;
import java.util.Optional;

public interface LanguageRepository {

  Language save(Language language);

  void delete(String id);

  Optional<Language> findById(String id);

  List<Language> findAll();

  Optional<Language> findDefault();

  /** Targeted update, not a read-modify-save: exactly one row may have {@code is_default =
   * true} (a partial unique index enforces it), so clearing the old default and setting the
   * new one must be two separate, narrow statements — never a whole-entity save that could
   * carry stale field values for anything else on the row. */
  void clearDefaultFlag();

  void markAsDefault(String id);
}
