package dev.civicpulse.feedcontent.adapter.out.persistence;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

interface TrendingTopicCacheJpaRepository extends JpaRepository<TrendingTopicCacheJpaEntity, String> {

  java.util.List<TrendingTopicCacheJpaEntity> findAllByOrderByRankAsc(Pageable pageable);
}
