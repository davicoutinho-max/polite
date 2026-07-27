package dev.civicpulse.governmentsync.adapter.out.client;

import dev.civicpulse.governmentsync.application.port.out.ElectionSyncGateway;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/** See ElectionSyncGateway's javadoc for why failures here are logged, not thrown. */
@Component
class ElectionSyncAdapter implements ElectionSyncGateway {

  private static final Logger log = LoggerFactory.getLogger(ElectionSyncAdapter.class);

  private final RestClient restClient;

  ElectionSyncAdapter(RestClient.Builder restClientBuilder, ElectionsServiceProperties properties) {
    this.restClient = restClientBuilder.baseUrl(properties.baseUrl()).build();
  }

  @Override
  public void syncElectionCandidacy(String electionTitle, String scope, LocalDate electionDate, String location, UUID politicianAccountId) {
    try {
      ElectionResponse election =
          restClient
              .post()
              .uri("/elections/sync")
              .body(new SyncElectionRequest(electionTitle, scope, electionDate, location, null))
              .retrieve()
              .body(ElectionResponse.class);
      if (election == null) {
        log.debug("elections-service returned an empty response syncing election {}", electionTitle);
        return;
      }
      restClient
          .post()
          .uri("/elections/{id}/candidacies", election.id())
          .body(new NominateCandidateRequest(politicianAccountId))
          .retrieve()
          .toBodilessEntity();
    } catch (RestClientException e) {
      log.debug("Election sync skipped for {} / {}: {}", electionTitle, politicianAccountId, e.getMessage());
    }
  }

  @Override
  public void syncElectionResults(
      String electionTitle, String scope, LocalDate electionDate, String location, String office, List<ResultCandidate> results) {
    try {
      ElectionResponse election =
          restClient
              .post()
              .uri("/elections/sync")
              .body(new SyncElectionRequest(electionTitle, scope, electionDate, location, null))
              .retrieve()
              .body(ElectionResponse.class);
      if (election == null) {
        log.debug("elections-service returned an empty response syncing election {}", electionTitle);
        return;
      }
      List<ResultItem> items =
          results.stream()
              .map(r -> new ResultItem(r.externalId(), r.candidateName(), r.partyAcronym(), r.votes(), r.rank(), r.elected(), r.politicianAccountId()))
              .toList();
      restClient.post().uri("/elections/{id}/results/sync", election.id()).body(new SyncResultsRequest(office, items)).retrieve().toBodilessEntity();
    } catch (RestClientException e) {
      log.debug("Election results sync skipped for {} / {}: {}", electionTitle, office, e.getMessage());
    }
  }

  private record SyncElectionRequest(String title, String scope, LocalDate electionDate, String location, String description) {}

  private record NominateCandidateRequest(UUID politicianAccountId) {}

  private record ElectionResponse(UUID id) {}

  private record SyncResultsRequest(String office, List<ResultItem> results) {}

  private record ResultItem(
      String externalId, String candidateName, String partyAcronym, long votes, int rank, boolean elected, UUID politicianAccountId) {}
}
