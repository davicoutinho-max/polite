package dev.civicpulse.feedcontent.adapter.in.web.dto;

import dev.civicpulse.feedcontent.domain.model.SocialShare;
import java.time.Instant;

public record SocialShareResponse(String platform, String status, String externalPostId, String errorMessage, Instant sharedAt) {

  public static SocialShareResponse from(SocialShare share) {
    return new SocialShareResponse(
        share.platform().code(), share.status().code(), share.externalPostId().orElse(null), share.errorMessage().orElse(null), share.sharedAt());
  }
}
