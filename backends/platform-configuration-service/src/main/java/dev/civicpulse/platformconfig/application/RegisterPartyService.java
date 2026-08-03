package dev.civicpulse.platformconfig.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.civicpulse.platformconfig.application.port.in.ManagePartyInviteUseCase.PartyInvitePrefill;
import dev.civicpulse.platformconfig.application.port.in.RegisterPartyUseCase;
import dev.civicpulse.platformconfig.application.port.out.EventPublisher;
import dev.civicpulse.platformconfig.application.port.out.IdentityProvisioningGateway;
import dev.civicpulse.platformconfig.application.port.out.PartyRegistryRepository;
import dev.civicpulse.platformconfig.application.port.out.RegistrationTokenGateway;
import dev.civicpulse.platformconfig.domain.event.PartyRegistered;
import dev.civicpulse.platformconfig.domain.exception.DuplicatePartyRegistrationException;
import dev.civicpulse.platformconfig.domain.exception.InvalidRegistrationTokenException;
import dev.civicpulse.platformconfig.domain.model.PartyRegistryEntry;
import java.time.Clock;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegisterPartyService implements RegisterPartyUseCase {

  private final PartyRegistryRepository partyRegistryRepository;
  private final IdentityProvisioningGateway identityProvisioningGateway;
  private final RegistrationTokenGateway registrationTokenGateway;
  private final EventPublisher eventPublisher;
  private final ObjectMapper objectMapper;
  private final Clock clock;

  public RegisterPartyService(
      PartyRegistryRepository partyRegistryRepository,
      IdentityProvisioningGateway identityProvisioningGateway,
      RegistrationTokenGateway registrationTokenGateway,
      EventPublisher eventPublisher,
      ObjectMapper objectMapper,
      Clock clock) {
    this.partyRegistryRepository = partyRegistryRepository;
    this.identityProvisioningGateway = identityProvisioningGateway;
    this.registrationTokenGateway = registrationTokenGateway;
    this.eventPublisher = eventPublisher;
    this.objectMapper = objectMapper;
    this.clock = clock;
  }

  @Override
  @Transactional
  public PartyRegistryEntry registerParty(String registrationToken, String handle, String email, String rawPassword) {
    // Validated (not consumed) before provisioning — a provisioning failure (e.g. duplicate
    // CNPJ) must never burn a one-time invite the citizen can't get back; the token is only
    // actually consumed once the account is real, below.
    RegistrationTokenGateway.RedeemedToken redeemed = registrationTokenGateway.validate(registrationToken);
    PartyInvitePrefill prefill = readPrefill(redeemed.prefillDataJson());
    String name = prefill.name();
    String acronym = prefill.acronym();
    int number = prefill.number() == null ? 0 : prefill.number();
    String president = prefill.president();
    String ideology = prefill.ideology();

    // Not @Transactional-safe against a partial failure (account created in Identity but the
    // local registry row fails to commit) — same accepted trade-off already documented in
    // party-management-service's RegisterPoliticianService for the identical pattern. CNPJ comes
    // from the admin-vetted invite, never from the redeeming citizen — see PartyInvitePrefill's
    // javadoc for why that one field is treated differently than the politician flow's CPF.
    IdentityProvisioningGateway.ProvisionedAccount account =
        identityProvisioningGateway.provisionPartyAccount(name, handle, email, rawPassword, "cnpj", prefill.cnpj());
    registrationTokenGateway.consume(registrationToken);

    // Identity silently turns this into a claim (same CNPJ as an already-synced, unclaimed party
    // account — see identity-service's Account.claim) when it recognizes the document number, in
    // which case government-sync-service's registry row already exists for this exact account id
    // and stands as-is: claiming grants login, it doesn't get to silently rename/renumber a party
    // that's already on file. The acronym/number uniqueness checks below only apply to the
    // genuinely-new-account path.
    var existingEntry = partyRegistryRepository.findById(account.accountId());
    if (existingEntry.isPresent()) {
      return existingEntry.get();
    }

    if (partyRegistryRepository.existsByAcronym(acronym)) {
      throw new DuplicatePartyRegistrationException("acronym");
    }
    if (partyRegistryRepository.existsByNumber(number)) {
      throw new DuplicatePartyRegistrationException("number");
    }

    var now = clock.instant();
    PartyRegistryEntry entry = PartyRegistryEntry.register(account.accountId(), name, acronym, number, president, ideology, now);
    PartyRegistryEntry saved = partyRegistryRepository.save(entry);
    eventPublisher.publish(new PartyRegistered(saved.id(), saved.name(), saved.acronym(), saved.number(), president, ideology, now));
    return saved;
  }

  private PartyInvitePrefill readPrefill(String prefillDataJson) {
    try {
      return objectMapper.readValue(prefillDataJson, PartyInvitePrefill.class);
    } catch (Exception e) {
      throw new InvalidRegistrationTokenException();
    }
  }
}
