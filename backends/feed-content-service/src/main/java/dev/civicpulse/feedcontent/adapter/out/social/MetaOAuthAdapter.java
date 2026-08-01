package dev.civicpulse.feedcontent.adapter.out.social;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.civicpulse.feedcontent.application.port.out.MetaOAuthGateway;
import dev.civicpulse.feedcontent.domain.exception.SocialOAuthException;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

/** Real Meta Graph API OAuth (see MetaProperties for the app credentials this needs). {@link
 * #exchangeCode} walks three real Graph API calls: short-lived user token, then a long-lived user
 * token, then the Pages the user manages (each Page's own access token comes back long-lived once
 * derived from a long-lived user token) — and for each Page, a follow-up call for whether it has a
 * linked Instagram Business account. */
@Component
class MetaOAuthAdapter implements MetaOAuthGateway {

  private static final String SCOPES =
      "pages_show_list,pages_manage_posts,pages_read_engagement,instagram_basic,instagram_content_publish";

  private final RestClient restClient;
  private final MetaProperties properties;

  MetaOAuthAdapter(RestClient.Builder restClientBuilder, MetaProperties properties) {
    this.restClient = restClientBuilder.baseUrl("https://graph.facebook.com/" + properties.graphApiVersion()).build();
    this.properties = properties;
  }

  @Override
  public String buildAuthorizeUrl(String state) {
    return UriComponentsBuilder.fromUriString("https://www.facebook.com/" + properties.graphApiVersion() + "/dialog/oauth")
        .queryParam("client_id", properties.appId())
        .queryParam("redirect_uri", properties.redirectUri())
        .queryParam("state", state)
        .queryParam("scope", SCOPES)
        .queryParam("response_type", "code")
        .build()
        .toUriString();
  }

  @Override
  public MetaAuthResult exchangeCode(String code) {
    try {
      String shortLivedToken = fetchShortLivedUserToken(code);
      String longLivedToken = fetchLongLivedUserToken(shortLivedToken);
      return new MetaAuthResult(fetchPages(longLivedToken));
    } catch (RestClientException e) {
      throw new SocialOAuthException("Meta OAuth exchange failed: " + e.getMessage(), e);
    }
  }

  private String fetchShortLivedUserToken(String code) {
    TokenResponse response =
        restClient
            .get()
            .uri(
                uriBuilder ->
                    uriBuilder
                        .path("/oauth/access_token")
                        .queryParam("client_id", properties.appId())
                        .queryParam("client_secret", properties.appSecret())
                        .queryParam("redirect_uri", properties.redirectUri())
                        .queryParam("code", code)
                        .build())
            .retrieve()
            .body(TokenResponse.class);
    if (response == null || response.accessToken() == null) {
      throw new SocialOAuthException("Meta returned no access token for the authorization code");
    }
    return response.accessToken();
  }

  private String fetchLongLivedUserToken(String shortLivedToken) {
    TokenResponse response =
        restClient
            .get()
            .uri(
                uriBuilder ->
                    uriBuilder
                        .path("/oauth/access_token")
                        .queryParam("grant_type", "fb_exchange_token")
                        .queryParam("client_id", properties.appId())
                        .queryParam("client_secret", properties.appSecret())
                        .queryParam("fb_exchange_token", shortLivedToken)
                        .build())
            .retrieve()
            .body(TokenResponse.class);
    if (response == null || response.accessToken() == null) {
      throw new SocialOAuthException("Meta returned no long-lived token during exchange");
    }
    return response.accessToken();
  }

  private List<MetaPage> fetchPages(String userAccessToken) {
    PagesResponse response =
        restClient
            .get()
            .uri(uriBuilder -> uriBuilder.path("/me/accounts").queryParam("access_token", userAccessToken).build())
            .retrieve()
            .body(PagesResponse.class);
    if (response == null || response.data() == null) {
      return List.of();
    }
    return response.data().stream()
        .map(page -> new MetaPage(page.id(), page.name(), page.accessToken(), fetchInstagramAccountId(page.id(), page.accessToken())))
        .toList();
  }

  private Optional<String> fetchInstagramAccountId(String pageId, String pageAccessToken) {
    InstagramLinkResponse response =
        restClient
            .get()
            .uri(
                uriBuilder ->
                    uriBuilder
                        .path("/{pageId}")
                        .queryParam("fields", "instagram_business_account")
                        .queryParam("access_token", pageAccessToken)
                        .build(pageId))
            .retrieve()
            .body(InstagramLinkResponse.class);
    if (response == null || response.instagramBusinessAccount() == null) {
      return Optional.empty();
    }
    return Optional.ofNullable(response.instagramBusinessAccount().id());
  }

  private record TokenResponse(@JsonProperty("access_token") String accessToken) {}

  private record PagesResponse(List<PageEntry> data) {}

  private record PageEntry(String id, String name, @JsonProperty("access_token") String accessToken) {}

  private record InstagramLinkResponse(@JsonProperty("instagram_business_account") InstagramAccount instagramBusinessAccount) {}

  private record InstagramAccount(String id) {}
}
