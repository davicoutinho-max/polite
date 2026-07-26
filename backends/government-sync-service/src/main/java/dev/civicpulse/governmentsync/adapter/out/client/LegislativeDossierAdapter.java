package dev.civicpulse.governmentsync.adapter.out.client;

import dev.civicpulse.governmentsync.application.port.out.LegislativeDossierGateway;
import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/** legislative-service's dossier is the only home for the education/email/social-link fields
 * Câmara's per-deputy detail endpoint provides — see LegislativeDossierGateway's javadoc for why
 * failures here are swallowed (logged, not thrown): a brand-new politician's dossier stub is
 * created asynchronously off a Kafka event this same sync run just published, and callers must
 * not fail — or retry-loop — the whole politician sync over that ordinary race. */
@Component
class LegislativeDossierAdapter implements LegislativeDossierGateway {

  private static final Logger log = LoggerFactory.getLogger(LegislativeDossierAdapter.class);

  private final RestClient restClient;

  LegislativeDossierAdapter(RestClient.Builder restClientBuilder, LegislativeServiceProperties properties) {
    this.restClient = restClientBuilder.baseUrl(properties.baseUrl()).build();
  }

  @Override
  public void enrichDossier(UUID politicianAccountId, String education, String email) {
    if ((education == null || education.isBlank()) && (email == null || email.isBlank())) {
      return;
    }
    try {
      restClient
          .put()
          .uri("/politicians/{id}/dossier", politicianAccountId)
          .body(new UpdateDossierRequest(education, null, null, email, null, null))
          .retrieve()
          .toBodilessEntity();
    } catch (RestClientException e) {
      log.debug("Dossier enrichment skipped for {} (likely not projected yet): {}", politicianAccountId, e.getMessage());
    }
  }

  @Override
  public void addSocialLinks(UUID politicianAccountId, List<String> urls) {
    if (urls.isEmpty()) {
      return;
    }
    // POST .../social-links always inserts a new row — no upsert semantics on legislative-
    // service's side — so re-running this daily against the same URLs would otherwise duplicate
    // a row per day forever; fetching what's already on file first is this adapter's own
    // idempotency guard.
    List<String> existingUrls;
    try {
      SocialLinkResponse[] existing = restClient.get().uri("/politicians/{id}/social-links", politicianAccountId).retrieve().body(SocialLinkResponse[].class);
      existingUrls = existing == null ? List.of() : java.util.Arrays.stream(existing).map(SocialLinkResponse::url).toList();
    } catch (RestClientException e) {
      log.debug("Could not read existing social links for {}, skipping social sync this run: {}", politicianAccountId, e.getMessage());
      return;
    }
    for (String url : urls) {
      if (existingUrls.contains(url)) {
        continue;
      }
      try {
        String platform = detectPlatform(url);
        String handle = extractHandle(url);
        restClient
            .post()
            .uri("/politicians/{id}/social-links", politicianAccountId)
            .body(new AddSocialLinkRequest(platform, capitalize(platform), handle, url))
            .retrieve()
            .toBodilessEntity();
      } catch (RestClientException e) {
        log.debug("Social link sync skipped for {} ({}): {}", politicianAccountId, url, e.getMessage());
      }
    }
  }

  /** Câmara's {@code redeSocial} is a bare list of URLs with no platform label — inferred from
   * the host, falling back to the generic "website" bucket {@code SocialPlatform} already has for
   * anything that isn't one of the named social networks. */
  private static String detectPlatform(String url) {
    String host = hostOf(url);
    if (host.contains("twitter.com") || host.contains("x.com")) return "x";
    if (host.contains("instagram.com")) return "instagram";
    if (host.contains("facebook.com")) return "facebook";
    if (host.contains("youtube.com") || host.contains("youtu.be")) return "youtube";
    if (host.contains("linkedin.com")) return "linkedin";
    if (host.contains("tiktok.com")) return "tiktok";
    return "website";
  }

  private static String extractHandle(String url) {
    String path = URI.create(url).getPath();
    if (path == null || path.isBlank() || path.equals("/")) {
      return hostOf(url);
    }
    String[] segments = path.split("/");
    return segments.length > 0 ? segments[segments.length - 1] : hostOf(url);
  }

  private static String hostOf(String url) {
    try {
      String host = URI.create(url).getHost();
      return host == null ? url : host;
    } catch (IllegalArgumentException e) {
      return url;
    }
  }

  private static String capitalize(String value) {
    return value.isEmpty() ? value : Character.toUpperCase(value.charAt(0)) + value.substring(1).toLowerCase(Locale.ROOT);
  }

  private record UpdateDossierRequest(String education, String profession, String patrimony, String email, String phone, String officeDetail) {}

  private record AddSocialLinkRequest(String platform, String label, String handle, String url) {}

  private record SocialLinkResponse(UUID id, String platform, String label, String handle, String url) {}
}
