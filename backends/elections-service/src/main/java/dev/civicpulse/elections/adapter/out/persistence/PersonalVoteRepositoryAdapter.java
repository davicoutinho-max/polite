package dev.civicpulse.elections.adapter.out.persistence;

import dev.civicpulse.elections.application.port.out.PersonalVoteRepository;
import dev.civicpulse.elections.domain.model.PersonalVote;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class PersonalVoteRepositoryAdapter implements PersonalVoteRepository {

  private final PersonalVoteJpaRepository jpaRepository;

  PersonalVoteRepositoryAdapter(PersonalVoteJpaRepository jpaRepository) {
    this.jpaRepository = jpaRepository;
  }

  @Override
  public Optional<PersonalVote> findByCitizenElectionAndOffice(UUID citizenAccountId, UUID electionId, String office) {
    return jpaRepository.findByCitizenAccountIdAndElectionIdAndOffice(citizenAccountId, electionId, office).map(PersonalVoteMapper::toDomain);
  }

  @Override
  public List<PersonalVote> findByCitizenAndElection(UUID citizenAccountId, UUID electionId) {
    return jpaRepository.findByCitizenAccountIdAndElectionId(citizenAccountId, electionId).stream().map(PersonalVoteMapper::toDomain).toList();
  }

  @Override
  public PersonalVote save(PersonalVote vote) {
    return PersonalVoteMapper.toDomain(jpaRepository.save(PersonalVoteMapper.toEntity(vote)));
  }
}
