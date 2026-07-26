package dev.civicpulse.participation.adapter.in.web.dto;

import dev.civicpulse.participation.application.port.in.SignatureVerificationStarted;
import java.util.UUID;

/** {@code demoCode} stands in for a real SMS/email send — see {@link SignatureVerificationStarted}. */
public record StartSignatureResponse(UUID verificationId, String demoCode, String contact, String method) {

  public static StartSignatureResponse from(SignatureVerificationStarted started) {
    return new StartSignatureResponse(started.verificationId(), started.demoCode(), started.contact(), started.method());
  }
}
