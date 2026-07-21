package dev.civicpulse.messaging.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/** No framework imports — the domain core of the hexagonal architecture (see
 * docs/architecture/system-architecture.html). Group membership eligibility (politician/party
 * only) is enforced by the caller building the participant picker, not re-validated here — see
 * MessagingServiceApplication's scope note. */
public final class Conversation {

  private final UUID id;
  private final boolean group;
  private String groupName;
  private String groupAvatarUrl;
  private final Instant createdAt;
  private Instant lastMessageAt;

  private Conversation(UUID id, boolean group, String groupName, String groupAvatarUrl, Instant createdAt, Instant lastMessageAt) {
    this.id = Objects.requireNonNull(id);
    this.group = group;
    this.groupName = groupName;
    this.groupAvatarUrl = groupAvatarUrl;
    this.createdAt = Objects.requireNonNull(createdAt);
    this.lastMessageAt = lastMessageAt;
  }

  public static Conversation createDirect(UUID id, Instant now) {
    return new Conversation(id, false, null, null, now, null);
  }

  public static Conversation createGroup(UUID id, String groupName, String groupAvatarUrl, Instant now) {
    return new Conversation(id, true, requireNonBlank(groupName), groupAvatarUrl, now, null);
  }

  public static Conversation reconstitute(
      UUID id, boolean group, String groupName, String groupAvatarUrl, Instant createdAt, Instant lastMessageAt) {
    return new Conversation(id, group, groupName, groupAvatarUrl, createdAt, lastMessageAt);
  }

  public void recordMessageSent(Instant now) {
    this.lastMessageAt = now;
  }

  /** Only group conversations have a name/avatar to edit — direct conversations derive their
   * display identity from the other participant instead (see the frontend's peerName/peerAvatar
   * resolution), so there's nothing here to rename. */
  public void rename(String newName) {
    if (!group) {
      throw new IllegalStateException("Only group conversations can be renamed");
    }
    this.groupName = requireNonBlank(newName);
  }

  public void changeAvatar(String newAvatarUrl) {
    if (!group) {
      throw new IllegalStateException("Only group conversations have an avatar to change");
    }
    this.groupAvatarUrl = newAvatarUrl;
  }

  private static String requireNonBlank(String value) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("groupName must not be blank");
    }
    return value;
  }

  public UUID id() {
    return id;
  }

  public boolean group() {
    return group;
  }

  public Optional<String> groupName() {
    return Optional.ofNullable(groupName);
  }

  public Optional<String> groupAvatarUrl() {
    return Optional.ofNullable(groupAvatarUrl);
  }

  public Instant createdAt() {
    return createdAt;
  }

  public Optional<Instant> lastMessageAt() {
    return Optional.ofNullable(lastMessageAt);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Conversation other)) return false;
    return id.equals(other.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}
