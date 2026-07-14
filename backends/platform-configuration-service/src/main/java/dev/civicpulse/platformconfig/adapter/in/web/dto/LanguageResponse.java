package dev.civicpulse.platformconfig.adapter.in.web.dto;

import dev.civicpulse.platformconfig.domain.model.Language;

public record LanguageResponse(String id, String name, String code, boolean isDefault) {

  public static LanguageResponse from(Language language) {
    return new LanguageResponse(language.id(), language.name(), language.code(), language.isDefault());
  }
}
