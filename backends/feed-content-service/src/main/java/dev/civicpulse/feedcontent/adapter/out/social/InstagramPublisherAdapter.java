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

/** Instagram's Graph API has no text-only post type — every post needs a media container created
 * first, then published in a second call. Both calls are authenticated with the linked Facebook
 * Page's access token (see ManageSocialConnectionService — the Instagram connection stores the
 * Page's token, not a separate Instagram-specific one). */
@Component
class InstagramPublisherAdapter implements SocialPublisher {

  private final RestClient restClient;
  private final MetaProperties properties;

  InstagramPublisherAdapter(RestClient.Builder restClientBuilder, MetaProperties properties) {
    this.restClient = restClientBuilder.baseUrl("https://graph.facebook.com/" + properties.graphApiVersion()).build();
    this.properties = properties;
  }

  @Override
  public SocialPlatform platform() {
    return SocialPlatform.INSTAGRAM;
  }

  @Override
  public PublishResult publish(SocialConnection connection, PostToPublish post) {
    if (post.imageUrl() == null || post.imageUrl().isBlank()) {
      return PublishResult.failure("Instagram requires an image — this post has none.");
    }
    try {
      String containerId = createMediaContainer(connection, post);
      return PublishResult.success(publishContainer(connection, containerId));
    } catch (RestClientException e) {
      return PublishResult.failure("Instagram publish failed: " + e.getMessage());
    } catch (MissingIdException e) {
      return PublishResult.failure(e.getMessage());
    }
  }

  private String createMediaContainer(SocialConnection connection, PostToPublish post) {
    MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
    form.add("image_url", post.imageUrl());
    form.add("caption", post.text());
    form.add("access_token", connection.accessToken());
    IdResponse response =
        restClient
            .post()
            .uri("/{igUserId}/media", connection.externalAccountId())
            .headers(headers -> headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED))
            .body(form)
            .retrieve()
            .body(IdResponse.class);
    if (response == null || response.id() == null) {
      throw new MissingIdException("Instagram returned no media container id");
    }
    return response.id();
  }

  private String publishContainer(SocialConnection connection, String containerId) {
    MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
    form.add("creation_id", containerId);
    form.add("access_token", connection.accessToken());
    IdResponse response =
        restClient
            .post()
            .uri("/{igUserId}/media_publish", connection.externalAccountId())
            .headers(headers -> headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED))
            .body(form)
            .retrieve()
            .body(IdResponse.class);
    if (response == null || response.id() == null) {
      throw new MissingIdException("Instagram returned no published post id");
    }
    return response.id();
  }

  private record IdResponse(String id) {}

  private static final class MissingIdException extends RuntimeException {
    MissingIdException(String message) {
      super(message);
    }
  }
}
