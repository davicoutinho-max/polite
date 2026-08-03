package dev.civicpulse.platformconfig.adapter.out.client;

import dev.civicpulse.platformconfig.application.port.out.RegistrationTokenGateway;
import dev.civicpulse.platformconfig.domain.exception.InvalidRegistrationTokenException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Component
class RegistrationTokenAdapter implements RegistrationTokenGateway {

  private final RestClient restClient;

  RegistrationTokenAdapter(RestClient.Builder restClientBuilder, IdentityServiceProperties properties) {
    this.restClient = restClientBuilder.baseUrl(properties.baseUrl()).build();
  }

  @Override
  public IssuedToken issueForParty(UUID issuedByAdminAccountId, String targetEmail, String prefillDataJson) {
    TokenResponse response =
        restClient
            .post()
            .uri("/registration-tokens")
            .body(new IssueRequest("party", issuedByAdminAccountId, targetEmail, prefillDataJson))
            .retrieve()
            .body(TokenResponse.class);
    return toIssuedToken(response);
  }

  @Override
  public IssuedToken resend(UUID tokenId, UUID issuedByAdminAccountId) {
    try {
      TokenResponse response =
          restClient.post().uri("/registration-tokens/{id}/resend?issuedByAccountId={issuedBy}", tokenId, issuedByAdminAccountId).retrieve().body(TokenResponse.class);
      return toIssuedToken(response);
    } catch (RestClientResponseException e) {
      if (e.getStatusCode() == HttpStatusCode.valueOf(410)) {
        throw new InvalidRegistrationTokenException();
      }
      throw e;
    }
  }

  @Override
  public List<IssuedToken> listIssuedBy(UUID issuedByAdminAccountId) {
    TokenResponse[] responses =
        restClient.get().uri("/registration-tokens?issuedByAccountId={id}", issuedByAdminAccountId).retrieve().body(TokenResponse[].class);
    return responses == null ? List.of() : java.util.Arrays.stream(responses).map(this::toIssuedToken).toList();
  }

  @Override
  public RedeemedToken validate(String rawToken) {
    try {
      TokenResponse response = restClient.get().uri("/registration-tokens/validate?token={token}", rawToken).retrieve().body(TokenResponse.class);
      if (response == null || !"party".equals(response.accountType())) {
        throw new InvalidRegistrationTokenException();
      }
      return new RedeemedToken(response.prefillData());
    } catch (RestClientResponseException e) {
      if (e.getStatusCode() == HttpStatusCode.valueOf(410)) {
        throw new InvalidRegistrationTokenException();
      }
      throw e;
    } catch (RestClientException e) {
      throw new InvalidRegistrationTokenException();
    }
  }

  @Override
  public void consume(String rawToken) {
    try {
      restClient.post().uri("/registration-tokens/redeem?token={token}", rawToken).retrieve().toBodilessEntity();
    } catch (RestClientResponseException e) {
      if (e.getStatusCode() == HttpStatusCode.valueOf(410)) {
        throw new InvalidRegistrationTokenException();
      }
      throw e;
    } catch (RestClientException e) {
      throw new InvalidRegistrationTokenException();
    }
  }

  private IssuedToken toIssuedToken(TokenResponse r) {
    if (r == null) {
      throw new InvalidRegistrationTokenException();
    }
    return new IssuedToken(r.id(), r.token(), r.targetEmail(), r.prefillData(), r.status());
  }

  private record IssueRequest(String accountType, UUID issuedByAccountId, String targetEmail, String prefillData) {}

  private record TokenResponse(
      UUID id, String token, String accountType, String targetEmail, String prefillData, String status, Instant createdAt, Instant expiresAt) {}
}
