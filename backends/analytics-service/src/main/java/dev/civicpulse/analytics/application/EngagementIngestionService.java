package dev.civicpulse.analytics.application;

import dev.civicpulse.analytics.application.port.in.EngagementIngestionUseCase;
import dev.civicpulse.analytics.application.port.out.EngagementEventRepository;
import dev.civicpulse.analytics.application.port.out.FeedContentLookupGateway;
import dev.civicpulse.analytics.application.port.out.IdentityLookupGateway;
import dev.civicpulse.analytics.domain.model.EngagementEvent;
import dev.civicpulse.analytics.domain.model.EngagementEventType;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EngagementIngestionService implements EngagementIngestionUseCase {

  private static final Logger log = LoggerFactory.getLogger(EngagementIngestionService.class);

  private final EngagementEventRepository engagementEventRepository;
  private final IdentityLookupGateway identityLookupGateway;
  private final FeedContentLookupGateway feedContentLookupGateway;
  private final Clock clock;

  public EngagementIngestionService(
      EngagementEventRepository engagementEventRepository,
      IdentityLookupGateway identityLookupGateway,
      FeedContentLookupGateway feedContentLookupGateway,
      Clock clock) {
    this.engagementEventRepository = engagementEventRepository;
    this.identityLookupGateway = identityLookupGateway;
    this.feedContentLookupGateway = feedContentLookupGateway;
    this.clock = clock;
  }

  @Override
  @Transactional
  public void onPostPublished(UUID postId, UUID authorId, String kind, Instant occurredAt) {
    ingest(authorId, authorId, EngagementEventType.POST_PUBLISHED, kind, occurredAt, "post-published:" + postId);
  }

  @Override
  @Transactional
  public void onPostLiked(UUID postId, UUID accountId, Instant occurredAt) {
    feedContentLookupGateway
        .lookupPost(postId)
        .ifPresentOrElse(
            post -> ingest(post.authorAccountId(), accountId, EngagementEventType.LIKE, post.kind(), occurredAt, "post-liked:" + postId + ":" + accountId),
            () -> log.debug("Skipping PostLiked for unknown post {}", postId));
  }

  @Override
  @Transactional
  public void onCommentAdded(UUID postId, UUID commentId, Instant occurredAt) {
    feedContentLookupGateway
        .lookupPost(postId)
        .ifPresentOrElse(
            post ->
                feedContentLookupGateway
                    .lookupCommentAuthor(postId, commentId)
                    .ifPresentOrElse(
                        commenterId ->
                            ingest(post.authorAccountId(), commenterId, EngagementEventType.COMMENT, post.kind(), occurredAt, "comment-added:" + commentId),
                        () -> log.debug("Skipping CommentAdded for unknown comment {}", commentId)),
            () -> log.debug("Skipping CommentAdded for unknown post {}", postId));
  }

  @Override
  @Transactional
  public void onFollowCreated(UUID followerAccountId, String targetType, UUID targetId, Instant occurredAt) {
    ingest(targetId, followerAccountId, EngagementEventType.FOLLOW_CREATED, null, occurredAt, "follow-created:" + followerAccountId + ":" + targetId);
  }

  @Override
  @Transactional
  public void onFollowRemoved(UUID followerAccountId, String targetType, UUID targetId, Instant occurredAt) {
    String sourceEventId = "follow-removed:" + followerAccountId + ":" + targetId + ":" + occurredAt.toEpochMilli();
    ingest(targetId, followerAccountId, EngagementEventType.FOLLOW_REMOVED, null, occurredAt, sourceEventId);
  }

  private void ingest(
      UUID authorAccountId, UUID actorAccountId, EngagementEventType type, String contentType, Instant occurredAt, String sourceEventId) {
    if (engagementEventRepository.existsByAuthorAndSourceEventId(authorAccountId, sourceEventId)) {
      return;
    }
    String actorAccountType = identityLookupGateway.lookupAccountType(actorAccountId).orElse(null);
    engagementEventRepository.save(
        EngagementEvent.record(authorAccountId, actorAccountId, actorAccountType, type, contentType, occurredAt, sourceEventId, clock.instant()));
  }
}
