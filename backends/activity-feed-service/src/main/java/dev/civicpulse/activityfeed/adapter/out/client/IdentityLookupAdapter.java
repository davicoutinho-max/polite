package dev.civicpulse.activityfeed.adapter.out.client;

import dev.civicpulse.activityfeed.application.port.out.IdentityLookupGateway;
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
class IdentityLookupAdapter implements IdentityLookupGateway {

  private static final Logger log = LoggerFactory.getLogger(IdentityLookupAdapter.class);

  private final RestClient restClient;

  IdentityLookupAdapter(RestClient.Builder restClientBuilder, IdentityServiceProperties properties) {
    this.restClient = restClientBuilder.baseUrl(properties.baseUrl()).build();
  }

  @Override
  public Optional<String> lookupDisplayName(UUID accountId) {
    try {
      AccountResponse response = restClient.get().uri("/accounts/{id}", accountId).retrieve().body(AccountResponse.class);
      return response == null ? Optional.empty() : Optional.ofNullable(response.name());
    } catch (RestClientResponseException e) {
      if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
        return Optional.empty();
      }
      log.warn("identity-service lookup failed for account {}: {}", accountId, e.getMessage());
      return Optional.empty();
    } catch (RestClientException e) {
      log.warn("identity-service unreachable while resolving account {}: {}", accountId, e.getMessage());
      return Optional.empty();
    }
  }

  private record AccountResponse(UUID id, String accountType, String name, String handle) {}
}
