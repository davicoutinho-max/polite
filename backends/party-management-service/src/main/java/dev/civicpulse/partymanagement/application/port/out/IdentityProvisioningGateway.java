package dev.civicpulse.partymanagement.application.port.out;

import java.util.UUID;

/** Anti-corruption-layer boundary: registering a brand-new politician means creating their
 * authenticatable identity, which only Identity Service owns (see
 * docs/architecture/system-architecture.html — this service never touches Identity's
 * database). This is a synchronous call because the caller (a party admin submitting a
 * registration form) needs the new account id back immediately to link it as a representative
 * in the same request; the resulting {@code AccountRegistered} event is still what Directory
 * Service consumes to build its own projection. */
public interface IdentityProvisioningGateway {

  ProvisionedAccount provisionPoliticianAccount(
      String name, String handle, String email, String rawPassword, String documentType, String rawDocumentNumber);

  record ProvisionedAccount(UUID accountId, String name, String handle) {}
}
