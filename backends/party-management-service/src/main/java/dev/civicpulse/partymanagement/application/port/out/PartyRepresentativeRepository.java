package dev.civicpulse.partymanagement.application.port.out;

import dev.civicpulse.partymanagement.domain.model.PartyRepresentative;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PartyRepresentativeRepository {

  PartyRepresentative save(PartyRepresentative representative);

  void delete(UUID id);

  Optional<PartyRepresentative> findByPartyIdAndPoliticianAccountId(UUID partyId, UUID politicianAccountId);

  /** Global lookup, regardless of party — used by the sync flow to detect a party switch (a
   * politician linked to a different party than the one the sync run is currently processing). */
  Optional<PartyRepresentative> findByPoliticianAccountId(UUID politicianAccountId);

  boolean existsByPartyIdAndPoliticianAccountId(UUID partyId, UUID politicianAccountId);

  List<PartyRepresentative> findByPartyId(UUID partyId);
}
