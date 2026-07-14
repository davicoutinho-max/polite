package dev.civicpulse.identity.application.port.out;

/** Adapter wraps Argon2id (see docs/architecture/system-architecture.html's security notes —
 * "PII stays hashed/encrypted", same principle applied to credentials). */
public interface PasswordHasher {

  String hash(String rawPassword);

  boolean matches(String rawPassword, String hash);
}
