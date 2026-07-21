package dev.civicpulse.feedcontent.adapter.out.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface PostPollVoteJpaRepository extends JpaRepository<PostPollVoteJpaEntity, PostPollVoteId> {

  List<PostPollVoteJpaEntity> findByPostId(UUID postId);

  void deleteByPostId(UUID postId);
}
