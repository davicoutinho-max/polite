package dev.civicpulse.legislative.adapter.out.persistence;

import dev.civicpulse.legislative.domain.model.TeamMember;
import org.springframework.stereotype.Component;

@Component
class TeamMemberMapper {

  TeamMember toDomain(TeamMemberJpaEntity entity) {
    return TeamMember.reconstitute(entity.getId(), entity.getPoliticianAccountId(), entity.getName(), entity.getRole(), entity.getAvatarUrl());
  }

  TeamMemberJpaEntity toEntity(TeamMember teamMember) {
    return new TeamMemberJpaEntity(
        teamMember.id().orElse(null),
        teamMember.politicianAccountId(),
        teamMember.name(),
        teamMember.role(),
        teamMember.avatarUrl().orElse(null));
  }
}
