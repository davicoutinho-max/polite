package dev.civicpulse.directory.application.port.out;

import dev.civicpulse.directory.domain.model.Party;
import dev.civicpulse.directory.domain.model.PartySpectrum;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PartyRepository {

  Party save(Party party);

  Optional<Party> findById(UUID id);

  List<Party> search(PartySpectrum spectrum, int page, int pageSize);
}
