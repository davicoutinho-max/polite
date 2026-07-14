package dev.civicpulse.platformconfig.adapter.out.client;

import dev.civicpulse.platformconfig.application.port.out.IdentityProvisioningGateway;
import dev.civicpulse.platformconfig.domain.exception.IdentityProvisioningException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Component
class IdentityProvisioningAdapter implements IdentityProvisioningGateway {

  private final RestClient restClient;

  IdentityProvisioningAdapter(RestClient.Builder restClientBuilder, IdentityServiceProperties properties) {
    this.restClient = restClientBuilder.baseUrl(properties.baseUrl()).build();
  }

  @Override
  public ProvisionedAccount provisionPartyAccount(
      String name, String handle, String email, String rawPassword, String documentType, String rawDocumentNumber) {
    try {
      IdentityAccountResponse response =
          restClient
              .post()
              .uri("/accounts/provision")
              .body(new ProvisionAccountRequest("party", name, handle, email, rawPassword, documentType, rawDocumentNumber))
              .retrieve()
              .body(IdentityAccountResponse.class);
      if (response == null) {
        throw new IdentityProvisioningException("Identity Service returned an empty response provisioning " + handle);
      }
      return new ProvisionedAccount(response.id(), response.name(), response.handle());
    } catch (RestClientResponseException e) {
      throw new IdentityProvisioningException("Identity Service rejected provisioning " + handle + ": " + e.getResponseBodyAsString(), e);
    } catch (RestClientException e) {
      throw new IdentityProvisioningException("Identity Service unreachable while provisioning " + handle, e);
    }
  }

  private record ProvisionAccountRequest(
      String accountType, String name, String handle, String email, String password, String documentType, String documentNumber) {}

  private record IdentityAccountResponse(java.util.UUID id, String name, String handle) {}
}
