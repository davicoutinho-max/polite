package dev.civicpulse.elections.application;

import dev.civicpulse.elections.application.port.in.GetElectionUseCase;
import dev.civicpulse.elections.application.port.out.ElectionCandidacyRepository;
import dev.civicpulse.elections.application.port.out.ElectionRepository;
import dev.civicpulse.elections.application.port.out.PoliticianDirectoryGateway;
import dev.civicpulse.elections.application.port.out.PoliticianDirectoryGateway.PoliticianSummary;
import dev.civicpulse.elections.domain.exception.ElectionNotFoundException;
import dev.civicpulse.elections.domain.model.Election;
import dev.civicpulse.elections.domain.model.ElectionScope;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ElectionQueryService implements GetElectionUseCase {

  private final ElectionRepository electionRepository;
  private final ElectionCandidacyRepository electionCandidacyRepository;
  private final PoliticianDirectoryGateway politicianDirectoryGateway;
  private final Clock clock;

  public ElectionQueryService(
      ElectionRepository electionRepository,
      ElectionCandidacyRepository electionCandidacyRepository,
      PoliticianDirectoryGateway politicianDirectoryGateway,
      Clock clock) {
    this.electionRepository = electionRepository;
    this.electionCandidacyRepository = electionCandidacyRepository;
    this.politicianDirectoryGateway = politicianDirectoryGateway;
    this.clock = clock;
  }

  @Override
  @Transactional(readOnly = true)
  public Election getById(UUID id) {
    return electionRepository.findById(id).orElseThrow(() -> new ElectionNotFoundException(id));
  }

  @Override
  @Transactional(readOnly = true)
  public List<Election> list(ElectionScope scope, int page, int pageSize) {
    return scope == null ? electionRepository.findAll(page, pageSize) : electionRepository.findByScope(scope, page, pageSize);
  }

  @Override
  @Transactional(readOnly = true)
  public List<Election> listUpcoming(int page, int pageSize) {
    return electionRepository.findUpcoming(LocalDate.now(clock), page, pageSize);
  }

  @Override
  @Transactional(readOnly = true)
  public List<PoliticianSummary> listCandidates(UUID electionId) {
    if (electionRepository.findById(electionId).isEmpty()) {
      throw new ElectionNotFoundException(electionId);
    }
    return electionCandidacyRepository.findByElectionId(electionId).stream()
        .map(candidacy -> politicianDirectoryGateway.lookup(candidacy.politicianAccountId()))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .toList();
  }
}
