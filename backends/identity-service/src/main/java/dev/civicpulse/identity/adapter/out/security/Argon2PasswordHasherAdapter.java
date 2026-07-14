package dev.civicpulse.identity.adapter.out.security;

import dev.civicpulse.identity.application.port.out.PasswordHasher;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Component;

/** Argon2id, as documented in the Identity &amp; Access Service's perf notes
 * (docs/architecture/data-architecture.html): "password_hash uses Argon2id at the application
 * layer". Parameters follow OWASP's current minimum recommendation for Argon2id. */
@Component
public class Argon2PasswordHasherAdapter implements PasswordHasher {

  private final Argon2PasswordEncoder encoder = new Argon2PasswordEncoder(16, 32, 1, 19 * 1024, 2);

  @Override
  public String hash(String rawPassword) {
    return encoder.encode(rawPassword);
  }

  @Override
  public boolean matches(String rawPassword, String hash) {
    return encoder.matches(rawPassword, hash);
  }
}
