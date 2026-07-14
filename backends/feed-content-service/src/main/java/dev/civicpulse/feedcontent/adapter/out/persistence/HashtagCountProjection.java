package dev.civicpulse.feedcontent.adapter.out.persistence;

/** JPQL constructor-expression projection for {@code PostHashtagJpaRepository.countByHashtagSince}. */
public class HashtagCountProjection {

  private final String hashtag;
  private final long count;

  public HashtagCountProjection(String hashtag, long count) {
    this.hashtag = hashtag;
    this.count = count;
  }

  public String getHashtag() {
    return hashtag;
  }

  public long getCount() {
    return count;
  }
}
