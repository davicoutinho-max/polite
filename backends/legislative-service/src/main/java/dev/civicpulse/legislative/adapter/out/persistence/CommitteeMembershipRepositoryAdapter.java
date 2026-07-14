package dev.civicpulse.legislative.adapter.out.persistence;

import dev.civicpulse.legislative.application.port.out.CommitteeMembershipRepository;
import dev.civicpulse.legislative.domain.model.CommitteeMembership;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class CommitteeMembershipRepositoryAdapter implements CommitteeMembershipRepository {

  private final CommitteeMembershipJpaRepository jpaRepository;
  private final CommitteeMembershipMapper mapper;

  CommitteeMembershipRepositoryAdapter(CommitteeMembershipJpaRepository jpaRepository, CommitteeMembershipMapper mapper) {
    this.jpaRepository = jpaRepository;
    this.mapper = mapper;
  }

  @Override
  public CommitteeMembership save(CommitteeMembership membership) {
    return mapper.toDomain(jpaRepository.save(mapper.toEntity(membership)));
  }

  @Override
  public List<CommitteeMembership> findByPolitician(UUID politicianAccountId) {
    return jpaRepository.findByPoliticianAccountId(politicianAccountId).stream().map(mapper::toDomain).toList();
  }
}
