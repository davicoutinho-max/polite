package dev.civicpulse.feedcontent.adapter.in.web.dto;

import dev.civicpulse.feedcontent.domain.model.SocialConnection;
import java.time.Instant;

public record SocialConnectionResponse(String platform, String externalAccountId, String externalAccountName, Instant connectedAt) {

  public static SocialConnectionResponse from(SocialConnection connection) {
    return new SocialConnectionResponse(
        connection.platform().code(), connection.externalAccountId(), connection.externalAccountName().orElse(null), connection.connectedAt());
  }
}
