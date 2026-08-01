package dev.civicpulse.feedcontent.domain.exception;

import dev.civicpulse.feedcontent.domain.model.SocialPlatform;
import java.util.UUID;

public class SocialConnectionNotFoundException extends RuntimeException {

  public SocialConnectionNotFoundException(UUID accountId, SocialPlatform platform) {
    super("Account " + accountId + " has no connected " + platform.code() + " account");
  }
}
