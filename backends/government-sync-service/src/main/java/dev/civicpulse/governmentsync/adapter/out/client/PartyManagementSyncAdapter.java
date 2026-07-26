package dev.civicpulse.governmentsync.adapter.out.client;

import dev.civicpulse.governmentsync.application.port.out.PoliticianSyncGateway;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/** Calls party-management-service's internal {@code POST /politicians/sync} — not routed
 * through the Gateway (see gateway-service's RouteConfig "party-management-sync-blocked" route),
 * reached directly service-to-service like every other internal sync/provisioning call. */
@Component
class PartyManagementSyncAdapter implements PoliticianSyncGateway {

  private final RestClient restClient;

  PartyManagementSyncAdapter(RestClient.Builder restClientBuilder, PartyManagementServiceProperties properties) {
    this.restClient = restClientBuilder.baseUrl(properties.baseUrl()).build();
  }

  @Override
  public UUID syncPolitician(UUID partyId, SyncPoliticianCommand command) {
    RepresentativeResponse response =
        restClient
            .post()
            .uri("/politicians/sync")
            .body(
                new SyncPoliticianRequest(
                    partyId,
                    command.name(),
                    command.handle(),
                    command.email(),
                    command.avatarUrl(),
                    "cpf",
                    command.documentNumber(),
                    command.externalSource(),
                    command.externalId(),
                    command.roleTitle(),
                    command.state(),
                    command.govLevel()))
            .retrieve()
            .body(RepresentativeResponse.class);
    if (response == null) {
      throw new IllegalStateException("party-management-service returned an empty response syncing politician " + command.externalId());
    }
    return response.politicianAccountId();
  }

  private record SyncPoliticianRequest(
      UUID partyId,
      String name,
      String handle,
      String email,
      String avatarUrl,
      String documentType,
      String documentNumber,
      String externalSource,
      String externalId,
      String roleTitle,
      String state,
      String govLevel) {}

  private record RepresentativeResponse(UUID politicianAccountId) {}
}
