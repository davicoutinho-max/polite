package dev.civicpulse.elections.adapter.out.persistence;

import dev.civicpulse.elections.domain.model.Election;
import org.springframework.stereotype.Component;

@Component
class ElectionMapper {

  Election toDomain(ElectionJpaEntity entity) {
    return Election.reconstitute(
        entity.getId(), entity.getTitle(), entity.getScope(), entity.getElectionDate(), entity.getLocation(), entity.getDescription());
  }

  ElectionJpaEntity toEntity(Election election) {
    return new ElectionJpaEntity(
        election.id(),
        election.title(),
        election.scope(),
        election.electionDate(),
        election.location().orElse(null),
        election.description().orElse(null));
  }
}
