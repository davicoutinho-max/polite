package dev.civicpulse.feedcontent.adapter.out.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface PostTagJpaRepository extends JpaRepository<PostTagJpaEntity, Long> {

  List<PostTagJpaEntity> findByPostId(UUID postId);
}
