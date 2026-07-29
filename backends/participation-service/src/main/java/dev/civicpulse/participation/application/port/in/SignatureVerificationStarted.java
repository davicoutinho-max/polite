package dev.civicpulse.participation.application.port.in;

import java.util.UUID;

/** The verification code itself is deliberately NOT part of this record — it's sent for real by
 * email (see EmailGateway/ResendEmailClient) and never returned in the API response, unlike this
 * flow's old demo-only behavior. */
public record SignatureVerificationStarted(UUID verificationId, String contact, String method) {}
