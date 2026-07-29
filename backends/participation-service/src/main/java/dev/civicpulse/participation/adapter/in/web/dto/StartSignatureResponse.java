package dev.civicpulse.participation.adapter.in.web.dto;

import dev.civicpulse.participation.application.port.in.SignatureVerificationStarted;
import java.util.UUID;

public record StartSignatureResponse(UUID verificationId, String contact, String method) {

  public static StartSignatureResponse from(SignatureVerificationStarted started) {
    return new StartSignatureResponse(started.verificationId(), started.contact(), started.method());
  }
}
