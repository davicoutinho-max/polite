package dev.civicpulse.feedcontent.adapter.out.social;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.civicpulse.feedcontent.application.port.out.XOAuthGateway;
import dev.civicpulse.feedcontent.domain.exception.SocialOAuthException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

/** Real X (Twitter) API v2 OAuth 2.0 with PKCE (see XProperties for the app credentials and
 * posting-tier requirement this needs). */
@Component
class XOAuthAdapter implements XOAuthGateway {

  private static final String SCOPES = "tweet.read tweet.write users.read offline.access";
  private static final SecureRandom RANDOM = new SecureRandom();

  private final RestClient restClient;
  private final XProperties properties;

  XOAuthAdapter(RestClient.Builder restClientBuilder, XProperties properties) {
    this.restClient = restClientBuilder.build();
    this.properties = properties;
  }

  @Override
  public AuthorizeRequest buildAuthorizeRequest(String state) {
    String codeVerifier = generateCodeVerifier();
    String url =
        UriComponentsBuilder.fromUriString("https://twitter.com/i/oauth2/authorize")
            .queryParam("response_type", "code")
            .queryParam("client_id", properties.clientId())
            .queryParam("redirect_uri", properties.redirectUri())
            .queryParam("scope", SCOPES)
            .queryParam("state", state)
            .queryParam("code_challenge", deriveCodeChallenge(codeVerifier))
            .queryParam("code_challenge_method", "S256")
            .build()
            .toUriString();
    return new AuthorizeRequest(url, codeVerifier);
  }

  @Override
  public XAuthResult exchangeCode(String code, String codeVerifier) {
    try {
      String accessToken = fetchAccessToken(code, codeVerifier);
      MeResponse me =
          restClient
              .get()
              .uri("https://api.twitter.com/2/users/me")
              .headers(headers -> headers.setBearerAuth(accessToken))
              .retrieve()
              .body(MeResponse.class);
      if (me == null || me.data() == null) {
        throw new SocialOAuthException("X returned no user profile for the new connection");
      }
      return new XAuthResult(accessToken, me.data().id(), me.data().username());
    } catch (RestClientException e) {
      throw new SocialOAuthException("X OAuth exchange failed: " + e.getMessage(), e);
    }
  }

  private String fetchAccessToken(String code, String codeVerifier) {
    MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
    form.add("grant_type", "authorization_code");
    form.add("code", code);
    form.add("redirect_uri", properties.redirectUri());
    form.add("code_verifier", codeVerifier);
    form.add("client_id", properties.clientId());
    TokenResponse response =
        restClient
            .post()
            .uri("https://api.twitter.com/2/oauth2/token")
            .headers(
                headers -> {
                  headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                  headers.setBasicAuth(properties.clientId(), properties.clientSecret());
                })
            .body(form)
            .retrieve()
            .body(TokenResponse.class);
    if (response == null || response.accessToken() == null) {
      throw new SocialOAuthException("X returned no access token for the authorization code");
    }
    return response.accessToken();
  }

  private static String generateCodeVerifier() {
    byte[] bytes = new byte[32];
    RANDOM.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }

  private static String deriveCodeChallenge(String codeVerifier) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(codeVerifier.getBytes(StandardCharsets.US_ASCII));
      return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 not available", e);
    }
  }

  private record TokenResponse(@JsonProperty("access_token") String accessToken) {}

  private record MeResponse(MeData data) {}

  private record MeData(String id, String username) {}
}
