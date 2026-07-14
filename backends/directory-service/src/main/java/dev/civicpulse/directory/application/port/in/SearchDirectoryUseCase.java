package dev.civicpulse.directory.application.port.in;

import dev.civicpulse.directory.domain.model.GovLevel;
import dev.civicpulse.directory.domain.model.Party;
import dev.civicpulse.directory.domain.model.PartySpectrum;
import dev.civicpulse.directory.domain.model.Politician;
import java.util.List;
import java.util.UUID;

public interface SearchDirectoryUseCase {

  Politician getPolitician(UUID accountId);

  List<Politician> searchPoliticians(String state, GovLevel level, UUID partyId, int page, int pageSize);

  Party getParty(UUID id);

  List<Party> searchParties(PartySpectrum spectrum, int page, int pageSize);
}
