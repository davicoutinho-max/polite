package dev.civicpulse.elections.adapter.out.client;

import dev.civicpulse.elections.application.port.out.PoliticianDirectoryGateway;
import dev.civicpulse.elections.domain.exception.PoliticianLookupException;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Component
class PoliticianDirectoryAdapter implements PoliticianDirectoryGateway {

  private final RestClient restClient;

  PoliticianDirectoryAdapter(RestClient.Builder restClientBuilder, DirectoryServiceProperties properties) {
    this.restClient = restClientBuilder.baseUrl(properties.baseUrl()).build();
  }

  @Override
  public Optional<PoliticianSummary> lookup(UUID politicianAccountId) {
    try {
      PoliticianResponse response =
          restClient.get().uri("/politicians/{accountId}", politicianAccountId).retrieve().body(PoliticianResponse.class);
      if (response == null) {
        return Optional.empty();
      }
      return Optional.of(
          new PoliticianSummary(
              response.accountId(), response.name(), response.handle(), response.avatarUrl(), response.verified(), response.office(),
              response.partyAcronym()));
    } catch (RestClientResponseException e) {
      if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
        return Optional.empty();
      }
      throw new PoliticianLookupException(
          "Directory Service rejected lookup of politician " + politicianAccountId + ": " + e.getResponseBodyAsString(), e);
    } catch (RestClientException e) {
      throw new PoliticianLookupException("Directory Service unreachable while looking up politician " + politicianAccountId, e);
    }
  }

  private record PoliticianResponse(
      UUID accountId, String name, String handle, String avatarUrl, boolean verified, String office, String partyAcronym) {}
}
