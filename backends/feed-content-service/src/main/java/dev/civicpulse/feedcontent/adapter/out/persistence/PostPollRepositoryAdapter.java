package dev.civicpulse.feedcontent.adapter.out.persistence;

import dev.civicpulse.feedcontent.application.port.out.PostPollRepository;
import dev.civicpulse.feedcontent.domain.model.PostPollOption;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
class PostPollRepositoryAdapter implements PostPollRepository {

  private final PostPollOptionJpaRepository optionJpaRepository;
  private final PostPollVoteJpaRepository voteJpaRepository;

  PostPollRepositoryAdapter(PostPollOptionJpaRepository optionJpaRepository, PostPollVoteJpaRepository voteJpaRepository) {
    this.optionJpaRepository = optionJpaRepository;
    this.voteJpaRepository = voteJpaRepository;
  }

  @Override
  public List<PostPollOption> saveOptions(List<PostPollOption> options) {
    var entities =
        options.stream()
            .map(o -> new PostPollOptionJpaEntity(o.id(), o.postId(), o.label(), (short) o.sortOrder()))
            .toList();
    return optionJpaRepository.saveAll(entities).stream().map(PostPollRepositoryAdapter::toDomain).toList();
  }

  @Override
  public List<PostPollOption> findOptionsByPostId(UUID postId) {
    return optionJpaRepository.findByPostIdOrderBySortOrder(postId).stream().map(PostPollRepositoryAdapter::toDomain).toList();
  }

  @Override
  public void deleteByPostId(UUID postId) {
    voteJpaRepository.deleteByPostId(postId);
    optionJpaRepository.deleteByPostId(postId);
  }

  @Override
  public void vote(UUID postId, UUID accountId, UUID optionId) {
    voteJpaRepository.save(new PostPollVoteJpaEntity(postId, accountId, optionId, Instant.now()));
  }

  @Override
  public Optional<UUID> findVotedOptionId(UUID postId, UUID accountId) {
    return voteJpaRepository.findById(new PostPollVoteId(postId, accountId)).map(PostPollVoteJpaEntity::getOptionId);
  }

  @Override
  public Map<UUID, Long> countVotesByPostId(UUID postId) {
    return voteJpaRepository.findByPostId(postId).stream()
        .collect(Collectors.groupingBy(PostPollVoteJpaEntity::getOptionId, Collectors.counting()));
  }

  private static PostPollOption toDomain(PostPollOptionJpaEntity entity) {
    return PostPollOption.reconstitute(entity.getId(), entity.getPostId(), entity.getLabel(), entity.getSortOrder());
  }
}
