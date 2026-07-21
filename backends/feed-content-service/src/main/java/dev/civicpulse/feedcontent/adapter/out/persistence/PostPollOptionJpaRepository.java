package dev.civicpulse.feedcontent.adapter.out.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface PostPollOptionJpaRepository extends JpaRepository<PostPollOptionJpaEntity, UUID> {

  List<PostPollOptionJpaEntity> findByPostIdOrderBySortOrder(UUID postId);

  void deleteByPostId(UUID postId);
}
