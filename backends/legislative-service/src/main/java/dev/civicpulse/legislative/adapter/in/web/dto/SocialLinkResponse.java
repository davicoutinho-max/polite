package dev.civicpulse.legislative.adapter.in.web.dto;

import dev.civicpulse.legislative.domain.model.SocialLink;
import java.util.UUID;

public record SocialLinkResponse(UUID id, String platform, String label, String handle, String url) {

  public static SocialLinkResponse from(SocialLink socialLink) {
    return new SocialLinkResponse(
        socialLink.id().orElse(null), socialLink.platform().code(), socialLink.label(), socialLink.handle(), socialLink.url());
  }
}
