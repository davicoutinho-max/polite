package dev.civicpulse.platformconfig.application.port.in;

import dev.civicpulse.platformconfig.domain.model.Language;
import java.util.List;

public interface ManageLanguageUseCase {

  Language addLanguage(String id, String name, String code, boolean isDefault);

  void removeLanguage(String id);

  Language setDefaultLanguage(String id);

  List<Language> listLanguages();
}
