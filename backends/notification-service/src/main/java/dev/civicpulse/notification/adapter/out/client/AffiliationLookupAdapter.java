package dev.civicpulse.notification.adapter.out.client;

import dev.civicpulse.notification.application.port.out.AffiliationLookupGateway;
import dev.civicpulse.notification.domain.exception.RecipientLookupException;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Component
class AffiliationLookupAdapter implements AffiliationLookupGateway {

  private final RestClient restClient;

  AffiliationLookupAdapter(RestClient.Builder restClientBuilder, MembershipAffiliationServiceProperties properties) {
    this.restClient = restClientBuilder.baseUrl(properties.baseUrl()).build();
  }

  @Override
  public Optional<UUID> lookupCitizenAccountId(UUID affiliationId) {
    try {
      AffiliationResponse response = restClient.get().uri("/affiliations/{id}", affiliationId).retrieve().body(AffiliationResponse.class);
      return response == null ? Optional.empty() : Optional.of(response.citizenAccountId());
    } catch (RestClientResponseException e) {
      if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
        return Optional.empty();
      }
      throw new RecipientLookupException(
          "Membership & Affiliation Service rejected lookup of affiliation " + affiliationId + ": " + e.getResponseBodyAsString(), e);
    } catch (RestClientException e) {
      throw new RecipientLookupException("Membership & Affiliation Service unreachable while looking up affiliation " + affiliationId, e);
    }
  }

  private record AffiliationResponse(UUID id, UUID citizenAccountId, UUID partyId, String status) {}
}
