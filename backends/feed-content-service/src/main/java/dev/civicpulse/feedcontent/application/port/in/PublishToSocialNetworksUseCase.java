package dev.civicpulse.feedcontent.application.port.in;

import dev.civicpulse.feedcontent.domain.model.SocialPlatform;
import dev.civicpulse.feedcontent.domain.model.SocialShare;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface PublishToSocialNetworksUseCase {

  /** One requested platform failing (no connection, rejected by the platform) never stops the
   * others — every platform gets its own {@link SocialShare} result, success or failure, see
   * SocialPublisher's javadoc. */
  List<SocialShare> publish(UUID postId, UUID accountId, Set<SocialPlatform> platforms);

  List<SocialShare> listShares(UUID postId);
}
