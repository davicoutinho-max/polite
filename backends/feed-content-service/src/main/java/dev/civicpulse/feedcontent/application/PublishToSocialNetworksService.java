package dev.civicpulse.feedcontent.application;

import dev.civicpulse.feedcontent.application.port.in.PublishToSocialNetworksUseCase;
import dev.civicpulse.feedcontent.application.port.out.PostRepository;
import dev.civicpulse.feedcontent.application.port.out.SocialConnectionRepository;
import dev.civicpulse.feedcontent.application.port.out.SocialPublisher;
import dev.civicpulse.feedcontent.application.port.out.SocialShareRepository;
import dev.civicpulse.feedcontent.domain.exception.PostNotFoundException;
import dev.civicpulse.feedcontent.domain.model.Post;
import dev.civicpulse.feedcontent.domain.model.SocialPlatform;
import dev.civicpulse.feedcontent.domain.model.SocialShare;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PublishToSocialNetworksService implements PublishToSocialNetworksUseCase {

  private final PostRepository postRepository;
  private final SocialConnectionRepository connectionRepository;
  private final SocialShareRepository shareRepository;
  private final Map<SocialPlatform, SocialPublisher> publishersByPlatform;
  private final Clock clock;

  public PublishToSocialNetworksService(
      PostRepository postRepository,
      SocialConnectionRepository connectionRepository,
      SocialShareRepository shareRepository,
      List<SocialPublisher> publishers,
      Clock clock) {
    this.postRepository = postRepository;
    this.connectionRepository = connectionRepository;
    this.shareRepository = shareRepository;
    this.publishersByPlatform = publishers.stream().collect(Collectors.toMap(SocialPublisher::platform, Function.identity()));
    this.clock = clock;
  }

  @Override
  @Transactional
  public List<SocialShare> publish(UUID postId, UUID accountId, Set<SocialPlatform> platforms) {
    Post post = postRepository.findById(postId).orElseThrow(() -> new PostNotFoundException(postId));
    var toPublish = new SocialPublisher.PostToPublish(post.content().orElse(""), post.imageUrl().orElse(null));
    Instant now = clock.instant();
    List<SocialShare> results = new ArrayList<>();
    for (SocialPlatform platform : platforms) {
      SocialShare share = attemptPublish(accountId, postId, platform, toPublish, now);
      results.add(shareRepository.save(share));
    }
    return results;
  }

  private SocialShare attemptPublish(UUID accountId, UUID postId, SocialPlatform platform, SocialPublisher.PostToPublish toPublish, Instant now) {
    var connection = connectionRepository.findByAccountAndPlatform(accountId, platform);
    if (connection.isEmpty()) {
      return SocialShare.failed(postId, platform, "No connected " + platform.code() + " account.", now);
    }
    var publisher = publishersByPlatform.get(platform);
    if (publisher == null) {
      return SocialShare.failed(postId, platform, "Publishing to " + platform.code() + " is not supported yet.", now);
    }
    var result = publisher.publish(connection.get(), toPublish);
    return result.success()
        ? SocialShare.published(postId, platform, result.externalPostId(), now)
        : SocialShare.failed(postId, platform, result.errorMessage(), now);
  }

  @Override
  @Transactional(readOnly = true)
  public List<SocialShare> listShares(UUID postId) {
    return shareRepository.findByPost(postId);
  }
}
