package dev.civicpulse.directory.application.port.out;

import dev.civicpulse.directory.domain.model.GovLevel;
import dev.civicpulse.directory.domain.model.Politician;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PoliticianRepository {

  Politician save(Politician politician);

  Optional<Politician> findById(UUID accountId);

  List<Politician> search(String state, GovLevel level, UUID partyId, int page, int pageSize);

  /** Targeted partial update — {@code PoliticianRegistered} and {@code PoliticianReassigned}
   * only ever carry the party linkage, never office/level/state. A read-modify-save round trip
   * here would race with {@link #assignOffice} (whichever of the two events lands last would
   * silently overwrite the other's field with a stale in-memory copy) since both events fire
   * within milliseconds of each other from the same registration action. A direct column
   * update sidesteps that entirely — no in-memory snapshot to go stale. */
  void assignParty(UUID accountId, UUID partyId, String partyAcronym, Instant now);

  /** Targeted partial update — see {@link #assignParty} for why this isn't a read-modify-save.
   * Office and state are bundled here since both only ever arrive together, on
   * {@code RepresentativeLinked}. */
  void assignOffice(UUID accountId, String office, String state, Instant now);

  /** Atomic {@code INSERT ... ON CONFLICT DO NOTHING}, not {@link #save}. {@code save()} on an
   * entity with a manually-assigned (non-generated) {@code @Id} is treated by Spring Data JPA
   * as "not new" and routed through {@code EntityManager.merge()}, which — if a concurrent
   * handler has already created the row and applied {@link #assignParty}/{@link #assignOffice}
   * by the time this runs — performs a full-row overwrite back to whatever blank state this
   * caller constructed, silently erasing the other handler's write. Every consumer that might
   * be racing to create the initial projection row (AccountRegistered vs. the
   * RepresentativeLinked/PoliticianRegistered enrichment fallback) must go through this instead. */
  void createIfAbsent(UUID accountId, String name, String handle, String avatarUrl, Instant now);
}
