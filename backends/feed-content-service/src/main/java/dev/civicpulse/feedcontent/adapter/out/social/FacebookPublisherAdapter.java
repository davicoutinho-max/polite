package dev.civicpulse.feedcontent.adapter.out.social;

import dev.civicpulse.feedcontent.application.port.out.SocialPublisher;
import dev.civicpulse.feedcontent.domain.model.SocialConnection;
import dev.civicpulse.feedcontent.domain.model.SocialPlatform;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/** Real Meta Graph API Page publishing — a post with an image goes to {@code /{page-id}/photos}
 * (image + caption), a text-only post goes to {@code /{page-id}/feed} (message only). */
@Component
class FacebookPublisherAdapter implements SocialPublisher {

  private final RestClient restClient;
  private final MetaProperties properties;

  FacebookPublisherAdapter(RestClient.Builder restClientBuilder, MetaProperties properties) {
    this.restClient = restClientBuilder.baseUrl("https://graph.facebook.com/" + properties.graphApiVersion()).build();
    this.properties = properties;
  }

  @Override
  public SocialPlatform platform() {
    return SocialPlatform.FACEBOOK;
  }

  @Override
  public PublishResult publish(SocialConnection connection, PostToPublish post) {
    boolean hasImage = post.imageUrl() != null && !post.imageUrl().isBlank();
    MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
    if (hasImage) {
      form.add("url", post.imageUrl());
      form.add("caption", post.text());
    } else {
      form.add("message", post.text());
    }
    form.add("access_token", connection.accessToken());
    try {
      IdResponse response =
          restClient
              .post()
              .uri("/{pageId}/" + (hasImage ? "photos" : "feed"), connection.externalAccountId())
              .headers(headers -> headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED))
              .body(form)
              .retrieve()
              .body(IdResponse.class);
      if (response == null || response.id() == null) {
        return PublishResult.failure("Facebook returned no post id");
      }
      return PublishResult.success(response.id());
    } catch (RestClientException e) {
      return PublishResult.failure("Facebook publish failed: " + e.getMessage());
    }
  }

  private record IdResponse(String id) {}
}
