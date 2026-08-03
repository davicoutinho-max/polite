package dev.civicpulse.partymanagement.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.civicpulse.partymanagement.application.port.in.ManagePoliticianInviteUseCase;
import dev.civicpulse.partymanagement.application.port.out.RegistrationTokenGateway;
import dev.civicpulse.partymanagement.application.port.out.RegistrationTokenGateway.IssuedToken;
import dev.civicpulse.partymanagement.domain.exception.InvalidRegistrationTokenException;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class PoliticianInviteService implements ManagePoliticianInviteUseCase {

  private final RegistrationTokenGateway registrationTokenGateway;
  private final ObjectMapper objectMapper;

  public PoliticianInviteService(RegistrationTokenGateway registrationTokenGateway, ObjectMapper objectMapper) {
    this.registrationTokenGateway = registrationTokenGateway;
    this.objectMapper = objectMapper;
  }

  @Override
  public IssuedToken issue(UUID partyId, String targetEmail, PoliticianInvitePrefill prefill) {
    return registrationTokenGateway.issueForPolitician(partyId, targetEmail, writePrefill(prefill));
  }

  @Override
  public IssuedToken resend(UUID partyId, UUID tokenId) {
    return registrationTokenGateway.resend(tokenId, partyId);
  }

  @Override
  public List<IssuedToken> listIssuedBy(UUID partyId) {
    return registrationTokenGateway.listIssuedBy(partyId);
  }

  private String writePrefill(PoliticianInvitePrefill prefill) {
    try {
      return objectMapper.writeValueAsString(prefill);
    } catch (JsonProcessingException e) {
      throw new InvalidRegistrationTokenException();
    }
  }
}
