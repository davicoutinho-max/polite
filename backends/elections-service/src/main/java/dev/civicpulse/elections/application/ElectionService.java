package dev.civicpulse.elections.application;

import dev.civicpulse.elections.application.port.in.ManageElectionUseCase;
import dev.civicpulse.elections.application.port.out.ElectionCandidacyRepository;
import dev.civicpulse.elections.application.port.out.ElectionRepository;
import dev.civicpulse.elections.domain.exception.ElectionNotFoundException;
import dev.civicpulse.elections.domain.model.Election;
import dev.civicpulse.elections.domain.model.ElectionCandidacy;
import dev.civicpulse.elections.domain.model.ElectionScope;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ElectionService implements ManageElectionUseCase {

  private final ElectionRepository electionRepository;
  private final ElectionCandidacyRepository electionCandidacyRepository;

  public ElectionService(ElectionRepository electionRepository, ElectionCandidacyRepository electionCandidacyRepository) {
    this.electionRepository = electionRepository;
    this.electionCandidacyRepository = electionCandidacyRepository;
  }

  @Override
  @Transactional
  public Election create(String title, ElectionScope scope, LocalDate electionDate, String description) {
    return electionRepository.save(Election.create(UUID.randomUUID(), title, scope, electionDate, description));
  }

  @Override
  @Transactional
  public void nominateCandidate(UUID electionId, UUID politicianAccountId) {
    if (electionRepository.findById(electionId).isEmpty()) {
      throw new ElectionNotFoundException(electionId);
    }
    if (electionCandidacyRepository.exists(electionId, politicianAccountId)) {
      return; // idempotent — already nominated
    }
    electionCandidacyRepository.save(ElectionCandidacy.nominate(electionId, politicianAccountId));
  }
}
