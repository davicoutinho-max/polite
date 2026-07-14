package dev.civicpulse.platformconfig.adapter.in.web.dto;

import dev.civicpulse.platformconfig.domain.model.TranslationValue;
import java.util.UUID;

public record TranslationValueResponse(UUID translationKeyId, String languageId, String value) {

  public static TranslationValueResponse from(TranslationValue value) {
    return new TranslationValueResponse(value.translationKeyId(), value.languageId(), value.value());
  }
}
