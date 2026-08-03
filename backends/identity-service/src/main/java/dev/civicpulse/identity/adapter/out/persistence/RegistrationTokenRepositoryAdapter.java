package dev.civicpulse.identity.adapter.out.persistence;

import dev.civicpulse.identity.application.port.out.RegistrationTokenRepository;
import dev.civicpulse.identity.domain.model.RegistrationToken;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class RegistrationTokenRepositoryAdapter implements RegistrationTokenRepository {

  private final RegistrationTokenJpaRepository jpaRepository;
  private final RegistrationTokenMapper mapper;

  RegistrationTokenRepositoryAdapter(RegistrationTokenJpaRepository jpaRepository, RegistrationTokenMapper mapper) {
    this.jpaRepository = jpaRepository;
    this.mapper = mapper;
  }

  @Override
  public RegistrationToken save(RegistrationToken token) {
    return mapper.toDomain(jpaRepository.save(mapper.toEntity(token)));
  }

  @Override
  public Optional<RegistrationToken> findById(UUID id) {
    return jpaRepository.findById(id).map(mapper::toDomain);
  }

  @Override
  public Optional<RegistrationToken> findByToken(String token) {
    return jpaRepository.findByToken(token).map(mapper::toDomain);
  }

  @Override
  public List<RegistrationToken> findByIssuedByAccountId(UUID issuedByAccountId) {
    return jpaRepository.findByIssuedByAccountIdOrderByCreatedAtDesc(issuedByAccountId).stream().map(mapper::toDomain).toList();
  }
}
