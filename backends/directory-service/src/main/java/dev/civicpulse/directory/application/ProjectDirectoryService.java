package dev.civicpulse.directory.application;

import dev.civicpulse.directory.application.port.in.ProjectDirectoryUseCase;
import dev.civicpulse.directory.application.port.out.AccountLookupGateway;
import dev.civicpulse.directory.application.port.out.PartyRepository;
import dev.civicpulse.directory.application.port.out.PoliticianRepository;
import dev.civicpulse.directory.domain.model.Party;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProjectDirectoryService implements ProjectDirectoryUseCase {

  private static final Logger log = LoggerFactory.getLogger(ProjectDirectoryService.class);
  private static final String POLITICIAN_ACCOUNT_TYPE = "politician";

  private final PoliticianRepository politicianRepository;
  private final PartyRepository partyRepository;
  private final AccountLookupGateway accountLookupGateway;
  private final Clock clock;

  public ProjectDirectoryService(
      PoliticianRepository politicianRepository,
      PartyRepository partyRepository,
      AccountLookupGateway accountLookupGateway,
      Clock clock) {
    this.politicianRepository = politicianRepository;
    this.partyRepository = partyRepository;
    this.accountLookupGateway = accountLookupGateway;
    this.clock = clock;
  }

  @Override
  @Transactional
  public void onAccountRegistered(UUID accountId, String accountType) {
    if (!POLITICIAN_ACCOUNT_TYPE.equals(accountType)) {
      return; // parties/citizens/admins aren't projected into the politicians catalog
    }
    if (politicianRepository.findById(accountId).isPresent()) {
      return; // already projected (e.g. RepresentativeLinked arrived first)
    }
    var accountSummary = accountLookupGateway.findAccount(accountId);
    if (accountSummary.isEmpty()) {
      log.warn("AccountRegistered received for {} but Identity lookup returned nothing — skipping projection", accountId);
      return;
    }
    var account = accountSummary.get();
    politicianRepository.createIfAbsent(accountId, account.name(), account.handle(), account.avatarUrl(), clock.instant());
  }

  @Override
  @Transactional
  public void onRepresentativeLinked(UUID politicianAccountId, UUID partyId, String roleTitle, String state, Instant linkedAt) {
    // Targeted column updates, not a read-modify-save round trip: RepresentativeLinked and
    // PoliticianRegistered both fire within milliseconds of the same registration action, and
    // both carry a party linkage. A fetch-mutate-save cycle here would race with the other
    // handler's — whichever commits last wins with a stale in-memory copy of fields it never
    // meant to touch (this cost the office field its value in an earlier version). See
    // PoliticianRepository.assignParty/assignOffice's javadoc.
    if (!ensurePoliticianProjected(politicianAccountId)) {
      return;
    }
    String partyAcronym = partyRepository.findById(partyId).map(Party::acronym).orElse(null);
    politicianRepository.assignParty(politicianAccountId, partyId, partyAcronym, linkedAt);
    politicianRepository.assignOffice(politicianAccountId, roleTitle, state, linkedAt);
  }

  @Override
  @Transactional
  public void onPoliticianRegistered(UUID politicianAccountId, UUID partyId, Instant occurredAt) {
    if (!ensurePoliticianProjected(politicianAccountId)) {
      return;
    }
    String partyAcronym = partyRepository.findById(partyId).map(Party::acronym).orElse(null);
    politicianRepository.assignParty(politicianAccountId, partyId, partyAcronym, occurredAt);
  }

  @Override
  @Transactional
  public void onPoliticianReassigned(UUID politicianAccountId, UUID partyId, Instant updatedAt) {
    if (!ensurePoliticianProjected(politicianAccountId)) {
      return;
    }
    String partyAcronym = partyRepository.findById(partyId).map(Party::acronym).orElse(null);
    politicianRepository.assignParty(politicianAccountId, partyId, partyAcronym, updatedAt);
  }

  @Override
  @Transactional
  public void onPartyRegistered(
      UUID partyId, String name, String acronym, int number, String president, String ideology, Instant occurredAt) {
    if (partyRepository.findById(partyId).isPresent()) {
      return;
    }
    Party party = Party.project(partyId, name, acronym, number, ideology, null, null, president, null, occurredAt);
    partyRepository.save(party);
  }

  /** Handles out-of-order delivery: if the projection doesn't exist yet (this event beat
   * {@code AccountRegistered} to the topic), enrich right away via the same lookup path.
   * Returns false if enrichment failed and the caller should skip its update. */
  private boolean ensurePoliticianProjected(UUID politicianAccountId) {
    if (politicianRepository.findById(politicianAccountId).isPresent()) {
      return true;
    }
    var accountSummary = accountLookupGateway.findAccount(politicianAccountId);
    if (accountSummary.isEmpty()) {
      log.warn("Party-linkage event received for {} before a resolvable account — skipping", politicianAccountId);
      return false;
    }
    var account = accountSummary.get();
    politicianRepository.createIfAbsent(politicianAccountId, account.name(), account.handle(), account.avatarUrl(), clock.instant());
    return true;
  }
}
