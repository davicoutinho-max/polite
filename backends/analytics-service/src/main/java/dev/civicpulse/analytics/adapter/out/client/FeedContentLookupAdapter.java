package dev.civicpulse.analytics.adapter.out.client;

import dev.civicpulse.analytics.application.port.out.FeedContentLookupGateway;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Component
class FeedContentLookupAdapter implements FeedContentLookupGateway {

  private static final Logger log = LoggerFactory.getLogger(FeedContentLookupAdapter.class);

  private final RestClient restClient;

  FeedContentLookupAdapter(RestClient.Builder restClientBuilder, FeedContentServiceProperties properties) {
    this.restClient = restClientBuilder.baseUrl(properties.baseUrl()).build();
  }

  @Override
  public Optional<PostSummary> lookupPost(UUID postId) {
    try {
      PostResponse response = restClient.get().uri("/posts/{id}", postId).retrieve().body(PostResponse.class);
      return response == null ? Optional.empty() : Optional.of(new PostSummary(response.authorAccountId(), response.kind()));
    } catch (RestClientResponseException e) {
      if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
        return Optional.empty();
      }
      log.warn("feed-content-service lookup failed for post {}: {}", postId, e.getMessage());
      return Optional.empty();
    } catch (RestClientException e) {
      log.warn("feed-content-service unreachable while resolving post {}: {}", postId, e.getMessage());
      return Optional.empty();
    }
  }

  @Override
  public Optional<UUID> lookupCommentAuthor(UUID postId, UUID commentId) {
    try {
      List<CommentResponse> comments =
          restClient.get().uri("/posts/{postId}/comments", postId).retrieve().body(new org.springframework.core.ParameterizedTypeReference<>() {});
      return comments == null
          ? Optional.empty()
          : comments.stream().filter(c -> c.id().equals(commentId)).map(CommentResponse::authorAccountId).findFirst();
    } catch (RestClientResponseException e) {
      if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
        return Optional.empty();
      }
      log.warn("feed-content-service comment lookup failed for post {} comment {}: {}", postId, commentId, e.getMessage());
      return Optional.empty();
    } catch (RestClientException e) {
      log.warn("feed-content-service unreachable while resolving comment {}: {}", commentId, e.getMessage());
      return Optional.empty();
    }
  }

  private record PostResponse(
      UUID id, UUID authorAccountId, String kind, String content, String imageUrl, String visibility, String context, UUID liveSessionId, Instant createdAt) {}

  private record CommentResponse(UUID id, UUID postId, UUID authorAccountId, String body, Instant createdAt) {}
}
