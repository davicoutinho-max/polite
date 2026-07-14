package dev.civicpulse.directory.adapter.out.client;

import dev.civicpulse.directory.application.port.out.AccountLookupGateway;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
class IdentityAccountLookupAdapter implements AccountLookupGateway {

  private static final Logger log = LoggerFactory.getLogger(IdentityAccountLookupAdapter.class);

  private final RestClient restClient;

  IdentityAccountLookupAdapter(RestClient.Builder restClientBuilder, IdentityServiceProperties properties) {
    this.restClient = restClientBuilder.baseUrl(properties.baseUrl()).build();
  }

  @Override
  public Optional<AccountSummary> findAccount(UUID accountId) {
    try {
      IdentityAccountResponse response =
          restClient.get().uri("/accounts/{id}", accountId).retrieve().body(IdentityAccountResponse.class);
      if (response == null) {
        return Optional.empty();
      }
      return Optional.of(new AccountSummary(response.id(), response.name(), response.handle(), response.avatarUrl()));
    } catch (RestClientException e) {
      log.warn("Identity lookup failed for account {}: {}", accountId, e.getMessage());
      return Optional.empty();
    }
  }

  /** Local shape of identity-service's AccountResponse — only the fields Directory needs. */
  private record IdentityAccountResponse(UUID id, String name, String handle, String avatarUrl) {}
}
