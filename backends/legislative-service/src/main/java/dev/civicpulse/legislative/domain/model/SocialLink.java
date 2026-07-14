package dev.civicpulse.legislative.domain.model;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class SocialLink {

  private final UUID id;
  private final UUID politicianAccountId;
  private final SocialPlatform platform;
  private final String label;
  private final String handle;
  private final String url;

  private SocialLink(UUID id, UUID politicianAccountId, SocialPlatform platform, String label, String handle, String url) {
    this.id = id;
    this.politicianAccountId = Objects.requireNonNull(politicianAccountId);
    this.platform = Objects.requireNonNull(platform);
    this.label = requireNonBlank(label, "label");
    this.handle = requireNonBlank(handle, "handle");
    this.url = requireNonBlank(url, "url");
  }

  public static SocialLink add(UUID politicianAccountId, SocialPlatform platform, String label, String handle, String url) {
    return new SocialLink(null, politicianAccountId, platform, label, handle, url);
  }

  public static SocialLink reconstitute(UUID id, UUID politicianAccountId, SocialPlatform platform, String label, String handle, String url) {
    return new SocialLink(id, politicianAccountId, platform, label, handle, url);
  }

  private static String requireNonBlank(String value, String field) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(field + " must not be blank");
    }
    return value;
  }

  public Optional<UUID> id() {
    return Optional.ofNullable(id);
  }

  public UUID politicianAccountId() {
    return politicianAccountId;
  }

  public SocialPlatform platform() {
    return platform;
  }

  public String label() {
    return label;
  }

  public String handle() {
    return handle;
  }

  public String url() {
    return url;
  }
}
