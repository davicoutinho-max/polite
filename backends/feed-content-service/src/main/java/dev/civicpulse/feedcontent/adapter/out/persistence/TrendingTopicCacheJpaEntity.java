package dev.civicpulse.feedcontent.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "trending_topics_cache")
public class TrendingTopicCacheJpaEntity {

  @Id private String hashtag;

  @Column(name = "post_count_last_24h", nullable = false)
  private int postCountLast24h;

  @Column(nullable = false)
  private short rank;

  @Column(name = "computed_at", nullable = false)
  private Instant computedAt;

  protected TrendingTopicCacheJpaEntity() {}

  public TrendingTopicCacheJpaEntity(String hashtag, int postCountLast24h, short rank, Instant computedAt) {
    this.hashtag = hashtag;
    this.postCountLast24h = postCountLast24h;
    this.rank = rank;
    this.computedAt = computedAt;
  }

  public String getHashtag() {
    return hashtag;
  }

  public int getPostCountLast24h() {
    return postCountLast24h;
  }

  public short getRank() {
    return rank;
  }

  public Instant getComputedAt() {
    return computedAt;
  }
}
