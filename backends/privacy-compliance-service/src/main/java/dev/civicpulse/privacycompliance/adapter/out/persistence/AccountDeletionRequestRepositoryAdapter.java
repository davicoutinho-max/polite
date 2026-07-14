package dev.civicpulse.privacycompliance.adapter.out.persistence;

import dev.civicpulse.privacycompliance.application.port.out.AccountDeletionRequestRepository;
import dev.civicpulse.privacycompliance.domain.model.AccountDeletionRequest;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class AccountDeletionRequestRepositoryAdapter implements AccountDeletionRequestRepository {

  private final AccountDeletionRequestJpaRepository jpaRepository;
  private final AccountDeletionRequestMapper mapper;

  AccountDeletionRequestRepositoryAdapter(AccountDeletionRequestJpaRepository jpaRepository, AccountDeletionRequestMapper mapper) {
    this.jpaRepository = jpaRepository;
    this.mapper = mapper;
  }

  @Override
  public AccountDeletionRequest save(AccountDeletionRequest request) {
    return mapper.toDomain(jpaRepository.save(mapper.toEntity(request)));
  }

  @Override
  public Optional<AccountDeletionRequest> findById(UUID id) {
    return jpaRepository.findById(id).map(mapper::toDomain);
  }

  @Override
  public List<AccountDeletionRequest> findByAccountId(UUID accountId) {
    return jpaRepository.findByAccountIdOrderByRequestedAtDesc(accountId).stream().map(mapper::toDomain).toList();
  }
}
