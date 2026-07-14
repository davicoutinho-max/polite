package dev.civicpulse.elections.adapter.out.persistence;

import dev.civicpulse.elections.domain.model.ElectionCandidacy;
import org.springframework.stereotype.Component;

@Component
class ElectionCandidacyMapper {

  ElectionCandidacy toDomain(ElectionCandidacyJpaEntity entity) {
    return ElectionCandidacy.nominate(entity.getElectionId(), entity.getPoliticianAccountId());
  }

  ElectionCandidacyJpaEntity toEntity(ElectionCandidacy candidacy) {
    return new ElectionCandidacyJpaEntity(candidacy.electionId(), candidacy.politicianAccountId());
  }
}
