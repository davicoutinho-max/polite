package dev.civicpulse.feedcontent.application.port.out;

import dev.civicpulse.feedcontent.domain.model.SocialConnection;
import dev.civicpulse.feedcontent.domain.model.SocialPlatform;

/** One implementation per {@link SocialPlatform} (strategy pattern) — injected as a {@code
 * List<SocialPublisher>} into PublishToSocialNetworksService, which picks the one matching the
 * requested platform. A failure to publish (rejected by the platform, missing required media,
 * expired token) is a normal {@link PublishResult} with {@code success=false}, never an
 * exception — only call errors (network, malformed request) are exceptional, and even those are
 * caught by each adapter and turned into a failed result so one platform failing doesn't stop
 * the others in the same publish request. */
public interface SocialPublisher {

  SocialPlatform platform();

  PublishResult publish(SocialConnection connection, PostToPublish post);

  /** {@code imageUrl} is required for Instagram (the platform has no text-only post type) — the
   * Instagram adapter returns a failed result with a clear reason when it's absent, rather than
   * silently skipping or throwing. */
  record PostToPublish(String text, String imageUrl) {}

  record PublishResult(boolean success, String externalPostId, String errorMessage) {
    public static PublishResult success(String externalPostId) {
      return new PublishResult(true, externalPostId, null);
    }

    public static PublishResult failure(String errorMessage) {
      return new PublishResult(false, null, errorMessage);
    }
  }
}
