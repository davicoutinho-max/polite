package dev.civicpulse.legislative.adapter.out.persistence;

import dev.civicpulse.legislative.application.port.out.AccountabilityDisclosureRepository;
import dev.civicpulse.legislative.domain.model.AccountabilityDisclosure;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class AccountabilityDisclosureRepositoryAdapter implements AccountabilityDisclosureRepository {

  private final AccountabilityDisclosureJpaRepository jpaRepository;
  private final AccountabilityDisclosureMapper mapper;

  AccountabilityDisclosureRepositoryAdapter(AccountabilityDisclosureJpaRepository jpaRepository, AccountabilityDisclosureMapper mapper) {
    this.jpaRepository = jpaRepository;
    this.mapper = mapper;
  }

  @Override
  public AccountabilityDisclosure save(AccountabilityDisclosure disclosure) {
    return mapper.toDomain(jpaRepository.save(mapper.toEntity(disclosure)));
  }

  @Override
  public List<AccountabilityDisclosure> findByPolitician(UUID politicianAccountId) {
    return jpaRepository.findByPoliticianAccountIdOrderBySubmittedAtDesc(politicianAccountId).stream().map(mapper::toDomain).toList();
  }
}
