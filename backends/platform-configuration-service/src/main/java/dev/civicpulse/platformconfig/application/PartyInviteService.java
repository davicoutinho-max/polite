package dev.civicpulse.platformconfig.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.civicpulse.platformconfig.application.port.in.ManagePartyInviteUseCase;
import dev.civicpulse.platformconfig.application.port.out.RegistrationTokenGateway;
import dev.civicpulse.platformconfig.application.port.out.RegistrationTokenGateway.IssuedToken;
import dev.civicpulse.platformconfig.domain.exception.InvalidRegistrationTokenException;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class PartyInviteService implements ManagePartyInviteUseCase {

  private final RegistrationTokenGateway registrationTokenGateway;
  private final ObjectMapper objectMapper;

  public PartyInviteService(RegistrationTokenGateway registrationTokenGateway, ObjectMapper objectMapper) {
    this.registrationTokenGateway = registrationTokenGateway;
    this.objectMapper = objectMapper;
  }

  @Override
  public IssuedToken issue(UUID issuedByAdminAccountId, String targetEmail, PartyInvitePrefill prefill) {
    return registrationTokenGateway.issueForParty(issuedByAdminAccountId, targetEmail, writePrefill(prefill));
  }

  @Override
  public IssuedToken resend(UUID tokenId, UUID issuedByAdminAccountId) {
    return registrationTokenGateway.resend(tokenId, issuedByAdminAccountId);
  }

  @Override
  public List<IssuedToken> listIssuedBy(UUID issuedByAdminAccountId) {
    return registrationTokenGateway.listIssuedBy(issuedByAdminAccountId);
  }

  private String writePrefill(PartyInvitePrefill prefill) {
    try {
      return objectMapper.writeValueAsString(prefill);
    } catch (JsonProcessingException e) {
      throw new InvalidRegistrationTokenException();
    }
  }
}
