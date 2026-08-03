package dev.civicpulse.identity.application.port.out;

import dev.civicpulse.identity.domain.model.RegistrationToken;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RegistrationTokenRepository {

  RegistrationToken save(RegistrationToken token);

  Optional<RegistrationToken> findById(UUID id);

  Optional<RegistrationToken> findByToken(String token);

  List<RegistrationToken> findByIssuedByAccountId(UUID issuedByAccountId);
}
