package dev.civicpulse.feedcontent.adapter.out.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

interface PostTagJpaRepository extends JpaRepository<PostTagJpaEntity, Long> {

  List<PostTagJpaEntity> findByPostId(UUID postId);

  // See CommentJpaRepository's deleteByPostId for why @Transactional is required here.
  @Transactional
  void deleteByPostId(UUID postId);
}
