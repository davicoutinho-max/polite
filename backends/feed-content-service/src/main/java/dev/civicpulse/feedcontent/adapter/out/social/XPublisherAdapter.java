package dev.civicpulse.feedcontent.adapter.out.social;

import dev.civicpulse.feedcontent.application.port.out.SocialPublisher;
import dev.civicpulse.feedcontent.domain.model.SocialConnection;
import dev.civicpulse.feedcontent.domain.model.SocialPlatform;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/** X API v2 tweet creation — text only. X's media upload lives on the older v1.1 endpoint under a
 * different auth scheme (OAuth 1.0a); wiring that up is a real gap deliberately left for a later
 * pass, so this adapter always posts text-only and ignores any image on the source post. */
@Component
class XPublisherAdapter implements SocialPublisher {

  private final RestClient restClient;

  XPublisherAdapter(RestClient.Builder restClientBuilder) {
    this.restClient = restClientBuilder.baseUrl("https://api.twitter.com/2").build();
  }

  @Override
  public SocialPlatform platform() {
    return SocialPlatform.X;
  }

  @Override
  public PublishResult publish(SocialConnection connection, PostToPublish post) {
    try {
      TweetResponse response =
          restClient
              .post()
              .uri("/tweets")
              .headers(headers -> headers.setBearerAuth(connection.accessToken()))
              .body(new TweetRequest(post.text()))
              .retrieve()
              .body(TweetResponse.class);
      if (response == null || response.data() == null) {
        return PublishResult.failure("X returned no tweet id");
      }
      return PublishResult.success(response.data().id());
    } catch (RestClientException e) {
      return PublishResult.failure("X publish failed: " + e.getMessage());
    }
  }

  private record TweetRequest(String text) {}

  private record TweetResponse(TweetData data) {}

  private record TweetData(String id) {}
}
