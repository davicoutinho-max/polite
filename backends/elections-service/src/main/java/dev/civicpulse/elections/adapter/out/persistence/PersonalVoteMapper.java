package dev.civicpulse.elections.adapter.out.persistence;

import dev.civicpulse.elections.domain.model.PersonalVote;

final class PersonalVoteMapper {

  private PersonalVoteMapper() {}

  static PersonalVoteJpaEntity toEntity(PersonalVote vote) {
    return new PersonalVoteJpaEntity(
        vote.id(),
        vote.citizenAccountId(),
        vote.electionId(),
        vote.office(),
        vote.candidateName(),
        vote.candidatePartyAcronym().orElse(null),
        vote.politicianAccountId().orElse(null),
        vote.castAt());
  }

  static PersonalVote toDomain(PersonalVoteJpaEntity entity) {
    return PersonalVote.reconstitute(
        entity.getId(),
        entity.getCitizenAccountId(),
        entity.getElectionId(),
        entity.getOffice(),
        entity.getCandidateName(),
        entity.getCandidatePartyAcronym(),
        entity.getPoliticianAccountId(),
        entity.getCastAt());
  }
}
