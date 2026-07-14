package dev.civicpulse.legislative.adapter.out.persistence;

import dev.civicpulse.legislative.domain.model.CommitteeMembership;
import org.springframework.stereotype.Component;

@Component
class CommitteeMembershipMapper {

  CommitteeMembership toDomain(CommitteeMembershipJpaEntity entity) {
    return CommitteeMembership.reconstitute(
        entity.getId(), entity.getPoliticianAccountId(), entity.getName(), entity.getRole(), entity.getKind());
  }

  CommitteeMembershipJpaEntity toEntity(CommitteeMembership membership) {
    return new CommitteeMembershipJpaEntity(
        membership.id().orElse(null), membership.politicianAccountId(), membership.name(), membership.role(), membership.kind());
  }
}
