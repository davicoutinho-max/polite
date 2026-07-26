package dev.civicpulse.participation.application.port.in;

import java.util.UUID;

/** {@code demoCode} exists only because this platform has no real SMS/email gateway anywhere
 * (every external provider in this system is a stub — see identity-service's document
 * verification) — a real deployment would send {@code demoCode} out-of-band and never return it
 * in the API response. */
public record SignatureVerificationStarted(UUID verificationId, String demoCode, String contact, String method) {}
