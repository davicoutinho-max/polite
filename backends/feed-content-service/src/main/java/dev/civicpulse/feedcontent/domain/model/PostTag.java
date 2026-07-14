package dev.civicpulse.feedcontent.domain.model;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class PostTag {

  private final Long id;
  private final UUID postId;
  private final String label;
  private final TagSeverity severity;
  private final String icon;

  private PostTag(Long id, UUID postId, String label, TagSeverity severity, String icon) {
    this.id = id;
    this.postId = Objects.requireNonNull(postId);
    this.label = requireNonBlank(label);
    this.severity = severity;
    this.icon = icon;
  }

  public static PostTag add(UUID postId, String label, TagSeverity severity, String icon) {
    return new PostTag(null, postId, label, severity, icon);
  }

  public static PostTag reconstitute(Long id, UUID postId, String label, TagSeverity severity, String icon) {
    return new PostTag(id, postId, label, severity, icon);
  }

  private static String requireNonBlank(String value) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("label must not be blank");
    }
    return value;
  }

  public Optional<Long> id() {
    return Optional.ofNullable(id);
  }

  public UUID postId() {
    return postId;
  }

  public String label() {
    return label;
  }

  public Optional<TagSeverity> severity() {
    return Optional.ofNullable(severity);
  }

  public Optional<String> icon() {
    return Optional.ofNullable(icon);
  }
}
