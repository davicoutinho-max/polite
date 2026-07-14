package dev.civicpulse.feedcontent.adapter.out.persistence;

import dev.civicpulse.feedcontent.application.port.out.TrendingTopicCacheRepository;
import dev.civicpulse.feedcontent.domain.model.TrendingTopic;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
class TrendingTopicCacheRepositoryAdapter implements TrendingTopicCacheRepository {

  private final TrendingTopicCacheJpaRepository jpaRepository;

  TrendingTopicCacheRepositoryAdapter(TrendingTopicCacheJpaRepository jpaRepository) {
    this.jpaRepository = jpaRepository;
  }

  @Override
  @Transactional
  public void replaceAll(List<TrendingTopic> topics) {
    jpaRepository.deleteAllInBatch();
    List<TrendingTopicCacheJpaEntity> entities =
        topics.stream()
            .map(topic -> new TrendingTopicCacheJpaEntity(topic.hashtag(), (int) topic.postCountLast24h(), topic.rank(), topic.computedAt()))
            .toList();
    jpaRepository.saveAll(entities);
  }

  @Override
  public List<TrendingTopic> findTopRanked(int limit) {
    return jpaRepository.findAllByOrderByRankAsc(PageRequest.of(0, limit)).stream()
        .map(entity -> new TrendingTopic(entity.getHashtag(), entity.getPostCountLast24h(), entity.getRank(), entity.getComputedAt()))
        .toList();
  }
}
