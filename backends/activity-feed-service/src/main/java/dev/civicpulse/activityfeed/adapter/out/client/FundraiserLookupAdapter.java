package dev.civicpulse.activityfeed.adapter.out.client;

import dev.civicpulse.activityfeed.application.port.out.FundraiserLookupGateway;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Component
class FundraiserLookupAdapter implements FundraiserLookupGateway {

  private static final Logger log = LoggerFactory.getLogger(FundraiserLookupAdapter.class);

  private final RestClient restClient;

  FundraiserLookupAdapter(RestClient.Builder restClientBuilder, FundraisingServiceProperties properties) {
    this.restClient = restClientBuilder.baseUrl(properties.baseUrl()).build();
  }

  @Override
  public Optional<FundraiserSummary> lookupFundraiser(UUID fundraiserId) {
    try {
      FundraiserResponse response = restClient.get().uri("/fundraisers/{id}", fundraiserId).retrieve().body(FundraiserResponse.class);
      return response == null ? Optional.empty() : Optional.of(new FundraiserSummary(response.organizerAccountId(), response.title()));
    } catch (RestClientResponseException e) {
      if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
        return Optional.empty();
      }
      log.warn("fundraising-service lookup failed for fundraiser {}: {}", fundraiserId, e.getMessage());
      return Optional.empty();
    } catch (RestClientException e) {
      log.warn("fundraising-service unreachable while resolving fundraiser {}: {}", fundraiserId, e.getMessage());
      return Optional.empty();
    }
  }

  private record FundraiserResponse(UUID id, UUID organizerAccountId, String title) {}
}
