package dev.civicpulse.notification.adapter.out.client;

import dev.civicpulse.notification.application.port.out.FundraiserLookupGateway;
import dev.civicpulse.notification.domain.exception.RecipientLookupException;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Component
class FundraiserLookupAdapter implements FundraiserLookupGateway {

  private final RestClient restClient;

  FundraiserLookupAdapter(RestClient.Builder restClientBuilder, FundraisingServiceProperties properties) {
    this.restClient = restClientBuilder.baseUrl(properties.baseUrl()).build();
  }

  @Override
  public Optional<UUID> lookupOrganizerAccountId(UUID fundraiserId) {
    try {
      FundraiserResponse response = restClient.get().uri("/fundraisers/{id}", fundraiserId).retrieve().body(FundraiserResponse.class);
      return response == null ? Optional.empty() : Optional.of(response.organizerAccountId());
    } catch (RestClientResponseException e) {
      if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
        return Optional.empty();
      }
      throw new RecipientLookupException(
          "Fundraising Service rejected lookup of fundraiser " + fundraiserId + ": " + e.getResponseBodyAsString(), e);
    } catch (RestClientException e) {
      throw new RecipientLookupException("Fundraising Service unreachable while looking up fundraiser " + fundraiserId, e);
    }
  }

  private record FundraiserResponse(UUID id, UUID organizerAccountId) {}
}
