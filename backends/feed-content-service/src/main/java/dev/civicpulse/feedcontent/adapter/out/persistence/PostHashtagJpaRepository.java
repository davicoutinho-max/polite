package dev.civicpulse.feedcontent.adapter.out.persistence;

import java.time.Instant;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

interface PostHashtagJpaRepository extends JpaRepository<PostHashtagJpaEntity, Long> {

  @Query(
      "select new dev.civicpulse.feedcontent.adapter.out.persistence.HashtagCountProjection(h.hashtag, count(h)) "
          + "from PostHashtagJpaEntity h where h.createdAt >= :since group by h.hashtag order by count(h) desc")
  java.util.List<HashtagCountProjection> countByHashtagSince(Instant since, Pageable pageable);

  // See CommentJpaRepository's deleteByPostId for why @Transactional is required here.
  @Transactional
  void deleteByPostId(UUID postId);
}
