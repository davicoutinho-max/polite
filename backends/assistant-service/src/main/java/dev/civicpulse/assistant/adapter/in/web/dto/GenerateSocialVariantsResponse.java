package dev.civicpulse.assistant.adapter.in.web.dto;

import dev.civicpulse.assistant.application.port.in.GenerateSocialVariantsUseCase.Result;

public record GenerateSocialVariantsResponse(String instagram, String facebook, String x, String linkedin, String simpleSummary) {

  public static GenerateSocialVariantsResponse from(Result result) {
    return new GenerateSocialVariantsResponse(result.instagram(), result.facebook(), result.x(), result.linkedin(), result.simpleSummary());
  }
}
