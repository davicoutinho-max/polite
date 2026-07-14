package dev.civicpulse.legislative.adapter.out.persistence;

import dev.civicpulse.legislative.application.port.out.TeamMemberRepository;
import dev.civicpulse.legislative.domain.model.TeamMember;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class TeamMemberRepositoryAdapter implements TeamMemberRepository {

  private final TeamMemberJpaRepository jpaRepository;
  private final TeamMemberMapper mapper;

  TeamMemberRepositoryAdapter(TeamMemberJpaRepository jpaRepository, TeamMemberMapper mapper) {
    this.jpaRepository = jpaRepository;
    this.mapper = mapper;
  }

  @Override
  public TeamMember save(TeamMember teamMember) {
    return mapper.toDomain(jpaRepository.save(mapper.toEntity(teamMember)));
  }

  @Override
  public List<TeamMember> findByPolitician(UUID politicianAccountId) {
    return jpaRepository.findByPoliticianAccountId(politicianAccountId).stream().map(mapper::toDomain).toList();
  }

  @Override
  public void deleteById(UUID id) {
    jpaRepository.deleteById(id);
  }
}
