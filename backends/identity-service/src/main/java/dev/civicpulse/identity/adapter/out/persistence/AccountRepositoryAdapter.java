package dev.civicpulse.identity.adapter.out.persistence;

import dev.civicpulse.identity.application.port.out.AccountRepository;
import dev.civicpulse.identity.domain.model.Account;
import dev.civicpulse.identity.domain.model.AccountId;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
class AccountRepositoryAdapter implements AccountRepository {

  private final AccountJpaRepository jpaRepository;
  private final AccountMapper mapper;

  AccountRepositoryAdapter(AccountJpaRepository jpaRepository, AccountMapper mapper) {
    this.jpaRepository = jpaRepository;
    this.mapper = mapper;
  }

  @Override
  public Account save(Account account) {
    AccountJpaEntity saved = jpaRepository.save(mapper.toEntity(account));
    return mapper.toDomain(saved);
  }

  @Override
  public Optional<Account> findById(AccountId id) {
    return jpaRepository.findById(id.value()).map(mapper::toDomain);
  }

  @Override
  public Optional<Account> findByEmail(String email) {
    return jpaRepository.findByEmail(email).map(mapper::toDomain);
  }

  @Override
  public Optional<Account> findByHandle(String handle) {
    return jpaRepository.findByHandle(handle).map(mapper::toDomain);
  }

  @Override
  public boolean existsByEmail(String email) {
    return jpaRepository.existsByEmail(email);
  }

  @Override
  public boolean existsByHandle(String handle) {
    return jpaRepository.existsByHandle(handle);
  }

  @Override
  public boolean existsByDocumentNumberHash(String documentNumberHash) {
    return jpaRepository.existsByDocumentNumberHash(documentNumberHash);
  }
}
