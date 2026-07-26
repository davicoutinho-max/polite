package dev.civicpulse.governmentsync.adapter.out.client;

import dev.civicpulse.governmentsync.application.port.out.PartySyncGateway;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/** Calls platform-configuration-service's internal {@code POST /parties/sync} — not routed
 * through the Gateway (see gateway-service's RouteConfig "platform-parties-sync-blocked" route),
 * reached directly service-to-service like every other internal sync/provisioning call. */
@Component
class PlatformConfigurationSyncAdapter implements PartySyncGateway {

  private final RestClient restClient;

  PlatformConfigurationSyncAdapter(RestClient.Builder restClientBuilder, PlatformConfigurationServiceProperties properties) {
    this.restClient = restClientBuilder.baseUrl(properties.baseUrl()).build();
  }

  @Override
  public UUID syncParty(SyncPartyCommand command) {
    PartyRegistryResponse response =
        restClient
            .post()
            .uri("/parties/sync")
            .body(
                new SyncPartyRequest(
                    command.name(),
                    command.acronym(),
                    command.number(),
                    command.logoUrl(),
                    "cnpj",
                    command.documentNumber(),
                    command.externalSource(),
                    command.externalId()))
            .retrieve()
            .body(PartyRegistryResponse.class);
    if (response == null) {
      throw new IllegalStateException("platform-configuration-service returned an empty response syncing party " + command.acronym());
    }
    return response.id();
  }

  private record SyncPartyRequest(
      String name,
      String acronym,
      int number,
      String logoUrl,
      String documentType,
      String documentNumber,
      String externalSource,
      String externalId) {}

  private record PartyRegistryResponse(UUID id) {}
}
