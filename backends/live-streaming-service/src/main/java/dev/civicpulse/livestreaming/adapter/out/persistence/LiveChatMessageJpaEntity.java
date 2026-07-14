package dev.civicpulse.livestreaming.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "live_chat_archive")
public class LiveChatMessageJpaEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "live_session_id", nullable = false)
  private UUID liveSessionId;

  @Column(name = "account_id", nullable = false)
  private UUID accountId;

  @Column(nullable = false)
  private String body;

  @Column(name = "sent_at", nullable = false)
  private Instant sentAt;

  protected LiveChatMessageJpaEntity() {}

  public LiveChatMessageJpaEntity(Long id, UUID liveSessionId, UUID accountId, String body, Instant sentAt) {
    this.id = id;
    this.liveSessionId = liveSessionId;
    this.accountId = accountId;
    this.body = body;
    this.sentAt = sentAt;
  }

  public Long getId() {
    return id;
  }

  public UUID getLiveSessionId() {
    return liveSessionId;
  }

  public UUID getAccountId() {
    return accountId;
  }

  public String getBody() {
    return body;
  }

  public Instant getSentAt() {
    return sentAt;
  }
}
