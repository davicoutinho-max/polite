package dev.civicpulse.directory.adapter.out.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface FollowJpaRepository extends JpaRepository<FollowJpaEntity, FollowId> {

  List<FollowJpaEntity> findByFollowerAccountId(UUID followerAccountId);
}
