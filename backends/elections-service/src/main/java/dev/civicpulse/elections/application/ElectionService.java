package dev.civicpulse.elections.application;

import dev.civicpulse.elections.application.port.in.ManageElectionUseCase;
import dev.civicpulse.elections.application.port.out.ElectionCandidacyRepository;
import dev.civicpulse.elections.application.port.out.ElectionRepository;
import dev.civicpulse.elections.application.port.out.ElectionResultRepository;
import dev.civicpulse.elections.domain.exception.ElectionNotFoundException;
import dev.civicpulse.elections.domain.model.Election;
import dev.civicpulse.elections.domain.model.ElectionCandidacy;
import dev.civicpulse.elections.domain.model.ElectionResult;
import dev.civicpulse.elections.domain.model.ElectionScope;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ElectionService implements ManageElectionUseCase {

  private final ElectionRepository electionRepository;
  private final ElectionCandidacyRepository electionCandidacyRepository;
  private final ElectionResultRepository electionResultRepository;

  public ElectionService(
      ElectionRepository electionRepository,
      ElectionCandidacyRepository electionCandidacyRepository,
      ElectionResultRepository electionResultRepository) {
    this.electionRepository = electionRepository;
    this.electionCandidacyRepository = electionCandidacyRepository;
    this.electionResultRepository = electionResultRepository;
  }

  @Override
  @Transactional
  public Election create(String title, ElectionScope scope, LocalDate electionDate, String description) {
    return electionRepository.save(Election.create(UUID.randomUUID(), title, scope, electionDate, null, description));
  }

  @Override
  @Transactional
  public Election syncElection(String title, ElectionScope scope, LocalDate electionDate, String location, String description) {
    return electionRepository
        .findByScopeAndElectionDateAndLocation(scope, electionDate, location)
        .orElseGet(() -> electionRepository.save(Election.create(UUID.randomUUID(), title, scope, electionDate, location, description)));
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

  @Override
  @Transactional
  public void syncElectionResults(UUID electionId, String office, List<ResultInput> results) {
    if (electionRepository.findById(electionId).isEmpty()) {
      throw new ElectionNotFoundException(electionId);
    }
    List<ElectionResult> domainResults =
        results.stream()
            .map(
                r ->
                    ElectionResult.create(
                        UUID.randomUUID(),
                        electionId,
                        office,
                        r.externalId(),
                        r.candidateName(),
                        r.partyAcronym(),
                        r.votes(),
                        r.rank(),
                        r.elected(),
                        r.politicianAccountId()))
            .toList();
    electionResultRepository.replaceForOffice(electionId, office, domainResults);
  }
}
