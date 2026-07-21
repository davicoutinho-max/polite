package dev.civicpulse.feedcontent.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "post_poll_votes")
@IdClass(PostPollVoteId.class)
public class PostPollVoteJpaEntity {

  @Id
  @Column(name = "post_id")
  private UUID postId;

  @Id
  @Column(name = "account_id")
  private UUID accountId;

  @Column(name = "option_id", nullable = false)
  private UUID optionId;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  protected PostPollVoteJpaEntity() {}

  public PostPollVoteJpaEntity(UUID postId, UUID accountId, UUID optionId, Instant createdAt) {
    this.postId = postId;
    this.accountId = accountId;
    this.optionId = optionId;
    this.createdAt = createdAt;
  }

  public UUID getPostId() {
    return postId;
  }

  public UUID getAccountId() {
    return accountId;
  }

  public UUID getOptionId() {
    return optionId;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
