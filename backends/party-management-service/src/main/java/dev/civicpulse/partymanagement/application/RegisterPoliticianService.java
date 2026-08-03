package dev.civicpulse.partymanagement.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.civicpulse.partymanagement.application.port.in.ManagePoliticianInviteUseCase.PoliticianInvitePrefill;
import dev.civicpulse.partymanagement.application.port.in.RegisterPoliticianUseCase;
import dev.civicpulse.partymanagement.application.port.out.EventPublisher;
import dev.civicpulse.partymanagement.application.port.out.IdentityProvisioningGateway;
import dev.civicpulse.partymanagement.application.port.out.PartyRepresentativeRepository;
import dev.civicpulse.partymanagement.application.port.out.RegistrationTokenGateway;
import dev.civicpulse.partymanagement.domain.event.PoliticianRegistered;
import dev.civicpulse.partymanagement.domain.event.RepresentativeLinked;
import dev.civicpulse.partymanagement.domain.event.RepresentativeRemoved;
import dev.civicpulse.partymanagement.domain.exception.InvalidRegistrationTokenException;
import dev.civicpulse.partymanagement.domain.model.PartyRepresentative;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegisterPoliticianService implements RegisterPoliticianUseCase {

  private final IdentityProvisioningGateway identityProvisioningGateway;
  private final RegistrationTokenGateway registrationTokenGateway;
  private final PartyRepresentativeRepository representativeRepository;
  private final EventPublisher eventPublisher;
  private final ObjectMapper objectMapper;
  private final Clock clock;

  public RegisterPoliticianService(
      IdentityProvisioningGateway identityProvisioningGateway,
      RegistrationTokenGateway registrationTokenGateway,
      PartyRepresentativeRepository representativeRepository,
      EventPublisher eventPublisher,
      ObjectMapper objectMapper,
      Clock clock) {
    this.identityProvisioningGateway = identityProvisioningGateway;
    this.registrationTokenGateway = registrationTokenGateway;
    this.representativeRepository = representativeRepository;
    this.eventPublisher = eventPublisher;
    this.objectMapper = objectMapper;
    this.clock = clock;
  }

  @Override
  @Transactional
  public PartyRepresentative registerPolitician(UUID partyId, RegisterPoliticianCommand command) {
    // Validated (not consumed) before provisioning — a provisioning failure (e.g. duplicate CPF)
    // must never burn a one-time invite the politician can't get back; the token is only
    // actually consumed once the account is real, below.
    RegistrationTokenGateway.RedeemedToken redeemed = registrationTokenGateway.validate(command.registrationToken());
    PoliticianInvitePrefill prefill = readPrefill(redeemed.prefillDataJson());
    if (prefill.partyId() == null || !prefill.partyId().equals(partyId)) {
      throw new InvalidRegistrationTokenException();
    }

    // Not @Transactional-safe against a partial failure (account created in Identity but the
    // local link fails to commit) — acceptable for now since this mirrors the same
    // saga-less trade-off already documented for cross-service calls in
    // docs/architecture/system-architecture.html; a production build would use the outbox
    // pattern here the same way Payments does for its own cross-service consistency.
    IdentityProvisioningGateway.ProvisionedAccount account =
        identityProvisioningGateway.provisionPoliticianAccount(
            prefill.name(), command.handle(), command.email(), command.rawPassword(), command.documentType(), command.rawDocumentNumber());
    registrationTokenGateway.consume(command.registrationToken());

    Instant now = clock.instant();

    // Identity silently turns this into a claim (same CPF as an already-synced, unclaimed
    // profile — see identity-service's Account.claim) rather than always minting a fresh account,
    // so the account id returned above can already have a representative link from the earlier
    // government sync. Blindly inserting another row here would violate party_representatives'
    // (party_id, politician_account_id) unique index for a same-party re-registration, or create a
    // second live link for a different one — same party-switch handling SyncPoliticianService
    // already needs for the exact same reason, reused here rather than re-invented.
    var existingLink = representativeRepository.findByPoliticianAccountId(account.accountId());
    if (existingLink.isPresent() && existingLink.get().partyId().equals(partyId)) {
      eventPublisher.publish(new PoliticianRegistered(account.accountId(), partyId, null, null, now));
      return existingLink.get();
    }
    if (existingLink.isPresent()) {
      representativeRepository.delete(existingLink.get().id());
      eventPublisher.publish(new RepresentativeRemoved(existingLink.get().partyId(), account.accountId(), now));
    }

    PartyRepresentative representative =
        PartyRepresentative.link(UUID.randomUUID(), partyId, account.accountId(), prefill.roleTitle(), now);
    PartyRepresentative saved = representativeRepository.save(representative);

    eventPublisher.publish(new PoliticianRegistered(account.accountId(), partyId, null, null, now));
    eventPublisher.publish(new RepresentativeLinked(partyId, account.accountId(), prefill.roleTitle(), prefill.state(), null, now));

    return saved;
  }

  private PoliticianInvitePrefill readPrefill(String prefillDataJson) {
    try {
      return objectMapper.readValue(prefillDataJson, PoliticianInvitePrefill.class);
    } catch (Exception e) {
      throw new InvalidRegistrationTokenException();
    }
  }
}
