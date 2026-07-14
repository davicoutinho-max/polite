package dev.civicpulse.analytics.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.civicpulse.analytics.application.port.out.EngagementEventRepository;
import dev.civicpulse.analytics.application.port.out.FeedContentLookupGateway;
import dev.civicpulse.analytics.application.port.out.FeedContentLookupGateway.PostSummary;
import dev.civicpulse.analytics.application.port.out.IdentityLookupGateway;
import dev.civicpulse.analytics.domain.model.EngagementEvent;
import dev.civicpulse.analytics.domain.model.EngagementEventType;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EngagementIngestionServiceTest {

  private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

  @Mock private EngagementEventRepository engagementEventRepository;
  @Mock private IdentityLookupGateway identityLookupGateway;
  @Mock private FeedContentLookupGateway feedContentLookupGateway;

  private EngagementIngestionService service;

  @BeforeEach
  void setUp() {
    service =
        new EngagementIngestionService(
            engagementEventRepository, identityLookupGateway, feedContentLookupGateway, Clock.fixed(NOW, ZoneOffset.UTC));
  }

  @Test
  void postPublishedIngestsWithoutAnyLookup() {
    UUID authorId = UUID.randomUUID();
    UUID postId = UUID.randomUUID();
    when(engagementEventRepository.existsByAuthorAndSourceEventId(authorId, "post-published:" + postId)).thenReturn(false);
    when(identityLookupGateway.lookupAccountType(authorId)).thenReturn(Optional.of("politician"));

    service.onPostPublished(postId, authorId, "text", NOW);

    ArgumentCaptor<EngagementEvent> captor = ArgumentCaptor.forClass(EngagementEvent.class);
    verify(engagementEventRepository).save(captor.capture());
    assertThat(captor.getValue().eventType()).isEqualTo(EngagementEventType.POST_PUBLISHED);
    assertThat(captor.getValue().contentType()).contains("text");
  }

  @Test
  void postLikedResolvesAuthorAndKindViaFeedContentLookup() {
    UUID postId = UUID.randomUUID();
    UUID authorId = UUID.randomUUID();
    UUID likerId = UUID.randomUUID();
    when(feedContentLookupGateway.lookupPost(postId)).thenReturn(Optional.of(new PostSummary(authorId, "video")));
    when(engagementEventRepository.existsByAuthorAndSourceEventId(authorId, "post-liked:" + postId + ":" + likerId)).thenReturn(false);
    when(identityLookupGateway.lookupAccountType(likerId)).thenReturn(Optional.of("citizen"));

    service.onPostLiked(postId, likerId, NOW);

    ArgumentCaptor<EngagementEvent> captor = ArgumentCaptor.forClass(EngagementEvent.class);
    verify(engagementEventRepository).save(captor.capture());
    assertThat(captor.getValue().authorAccountId()).isEqualTo(authorId);
    assertThat(captor.getValue().contentType()).contains("video");
  }

  @Test
  void postLikedSkipsWhenPostUnknown() {
    UUID postId = UUID.randomUUID();
    when(feedContentLookupGateway.lookupPost(postId)).thenReturn(Optional.empty());

    service.onPostLiked(postId, UUID.randomUUID(), NOW);

    verify(engagementEventRepository, never()).save(any());
  }

  @Test
  void followCreatedAttributesToTargetAsAuthor() {
    UUID follower = UUID.randomUUID();
    UUID target = UUID.randomUUID();
    when(engagementEventRepository.existsByAuthorAndSourceEventId(target, "follow-created:" + follower + ":" + target)).thenReturn(false);
    when(identityLookupGateway.lookupAccountType(follower)).thenReturn(Optional.of("citizen"));

    service.onFollowCreated(follower, "politician", target, NOW);

    ArgumentCaptor<EngagementEvent> captor = ArgumentCaptor.forClass(EngagementEvent.class);
    verify(engagementEventRepository).save(captor.capture());
    assertThat(captor.getValue().authorAccountId()).isEqualTo(target);
    assertThat(captor.getValue().actorAccountId()).isEqualTo(follower);
    assertThat(captor.getValue().eventType()).isEqualTo(EngagementEventType.FOLLOW_CREATED);
  }

  @Test
  void skipsIngestionWhenSourceEventAlreadySeen() {
    UUID authorId = UUID.randomUUID();
    UUID postId = UUID.randomUUID();
    when(engagementEventRepository.existsByAuthorAndSourceEventId(authorId, "post-published:" + postId)).thenReturn(true);

    service.onPostPublished(postId, authorId, "text", NOW);

    verify(engagementEventRepository, never()).save(any());
  }
}
