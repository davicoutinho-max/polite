package dev.civicpulse.feedcontent.application.port.out;

import dev.civicpulse.feedcontent.domain.model.PostPollOption;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface PostPollRepository {

  List<PostPollOption> saveOptions(List<PostPollOption> options);

  List<PostPollOption> findOptionsByPostId(UUID postId);

  void deleteByPostId(UUID postId);

  /** Upserts the account's vote for this poll — changing an existing vote replaces it rather
   * than adding a second one, since at most one vote per (post, account) is ever kept. */
  void vote(UUID postId, UUID accountId, UUID optionId);

  /** Removes the account's vote entirely — a no-op if they hadn't voted. */
  void unvote(UUID postId, UUID accountId);

  Optional<UUID> findVotedOptionId(UUID postId, UUID accountId);

  /** Vote count per option id, for every option that has at least one vote. */
  Map<UUID, Long> countVotesByPostId(UUID postId);
}
