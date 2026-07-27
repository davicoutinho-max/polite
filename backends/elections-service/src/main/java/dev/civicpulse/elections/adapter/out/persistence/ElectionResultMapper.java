package dev.civicpulse.elections.adapter.out.persistence;

import dev.civicpulse.elections.domain.model.ElectionResult;

final class ElectionResultMapper {

  private ElectionResultMapper() {}

  static ElectionResultJpaEntity toEntity(ElectionResult result) {
    return new ElectionResultJpaEntity(
        result.id(),
        result.electionId(),
        result.office(),
        result.externalId(),
        result.candidateName(),
        result.partyAcronym().orElse(null),
        result.votes(),
        result.rank(),
        result.elected(),
        result.politicianAccountId().orElse(null));
  }

  static ElectionResult toDomain(ElectionResultJpaEntity entity) {
    return ElectionResult.reconstitute(
        entity.getId(),
        entity.getElectionId(),
        entity.getOffice(),
        entity.getExternalId(),
        entity.getCandidateName(),
        entity.getPartyAcronym(),
        entity.getVotes(),
        entity.getRank(),
        entity.isElected(),
        entity.getPoliticianAccountId());
  }
}
