package dev.civicpulse.directory.application;

import dev.civicpulse.directory.application.port.in.SearchDirectoryUseCase;
import dev.civicpulse.directory.application.port.out.PartyRepository;
import dev.civicpulse.directory.application.port.out.PoliticianRepository;
import dev.civicpulse.directory.domain.exception.PartyNotFoundException;
import dev.civicpulse.directory.domain.exception.PoliticianNotFoundException;
import dev.civicpulse.directory.domain.model.GovLevel;
import dev.civicpulse.directory.domain.model.Party;
import dev.civicpulse.directory.domain.model.PartySpectrum;
import dev.civicpulse.directory.domain.model.Politician;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SearchDirectoryService implements SearchDirectoryUseCase {

  private final PoliticianRepository politicianRepository;
  private final PartyRepository partyRepository;

  public SearchDirectoryService(PoliticianRepository politicianRepository, PartyRepository partyRepository) {
    this.politicianRepository = politicianRepository;
    this.partyRepository = partyRepository;
  }

  @Override
  @Transactional(readOnly = true)
  public Politician getPolitician(UUID accountId) {
    return politicianRepository.findById(accountId).orElseThrow(() -> new PoliticianNotFoundException(accountId));
  }

  @Override
  @Transactional(readOnly = true)
  public List<Politician> searchPoliticians(String state, GovLevel level, UUID partyId, int page, int pageSize) {
    return politicianRepository.search(state, level, partyId, page, pageSize);
  }

  @Override
  @Transactional(readOnly = true)
  public Party getParty(UUID id) {
    return partyRepository.findById(id).orElseThrow(() -> new PartyNotFoundException(id));
  }

  @Override
  @Transactional(readOnly = true)
  public List<Party> searchParties(PartySpectrum spectrum, int page, int pageSize) {
    return partyRepository.search(spectrum, page, pageSize);
  }
}
