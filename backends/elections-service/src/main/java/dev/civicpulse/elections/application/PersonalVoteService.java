package dev.civicpulse.elections.application;

import dev.civicpulse.elections.application.port.in.PersonalVoteUseCase;
import dev.civicpulse.elections.application.port.out.ElectionRepository;
import dev.civicpulse.elections.application.port.out.PersonalVoteRepository;
import dev.civicpulse.elections.domain.exception.ElectionNotFoundException;
import dev.civicpulse.elections.domain.model.PersonalVote;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PersonalVoteService implements PersonalVoteUseCase {

  private final ElectionRepository electionRepository;
  private final PersonalVoteRepository personalVoteRepository;
  private final Clock clock;

  public PersonalVoteService(ElectionRepository electionRepository, PersonalVoteRepository personalVoteRepository, Clock clock) {
    this.electionRepository = electionRepository;
    this.personalVoteRepository = personalVoteRepository;
    this.clock = clock;
  }

  @Override
  @Transactional
  public PersonalVote registerVote(
      UUID citizenAccountId, UUID electionId, String office, String candidateName, String candidatePartyAcronym, UUID politicianAccountId) {
    if (electionRepository.findById(electionId).isEmpty()) {
      throw new ElectionNotFoundException(electionId);
    }
    UUID id =
        personalVoteRepository
            .findByCitizenElectionAndOffice(citizenAccountId, electionId, office)
            .map(PersonalVote::id)
            .orElseGet(UUID::randomUUID);
    PersonalVote vote =
        PersonalVote.cast(id, citizenAccountId, electionId, office, candidateName, candidatePartyAcronym, politicianAccountId, Instant.now(clock));
    return personalVoteRepository.save(vote);
  }

  @Override
  @Transactional(readOnly = true)
  public List<PersonalVote> listVotes(UUID citizenAccountId, UUID electionId) {
    return personalVoteRepository.findByCitizenAndElection(citizenAccountId, electionId);
  }
}
