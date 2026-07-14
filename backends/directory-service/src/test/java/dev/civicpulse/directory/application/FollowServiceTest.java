package dev.civicpulse.directory.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.civicpulse.directory.application.port.out.EventPublisher;
import dev.civicpulse.directory.application.port.out.FollowRepository;
import dev.civicpulse.directory.application.port.out.PartyRepository;
import dev.civicpulse.directory.application.port.out.PoliticianRepository;
import dev.civicpulse.directory.domain.event.FollowCreated;
import dev.civicpulse.directory.domain.exception.AlreadyFollowingException;
import dev.civicpulse.directory.domain.exception.PoliticianNotFoundException;
import dev.civicpulse.directory.domain.model.FollowTargetType;
import dev.civicpulse.directory.domain.model.Politician;
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
class FollowServiceTest {

  private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

  @Mock private FollowRepository followRepository;
  @Mock private PoliticianRepository politicianRepository;
  @Mock private PartyRepository partyRepository;
  @Mock private EventPublisher eventPublisher;

  private FollowService service;

  @BeforeEach
  void setUp() {
    service = new FollowService(followRepository, politicianRepository, partyRepository, eventPublisher, Clock.fixed(NOW, ZoneOffset.UTC));
  }

  @Test
  void followIncrementsFollowersAndPublishesEvent() {
    UUID follower = UUID.randomUUID();
    UUID politicianId = UUID.randomUUID();
    Politician politician = Politician.project(politicianId, "Jane Doe", "janedoe", null, null, null, null, null, null, NOW);
    when(followRepository.exists(follower, FollowTargetType.POLITICIAN, politicianId)).thenReturn(false);
    when(politicianRepository.findById(politicianId)).thenReturn(Optional.of(politician));

    service.follow(follower, FollowTargetType.POLITICIAN, politicianId);

    assertThat(politician.followersCount()).isEqualTo(1);
    verify(politicianRepository).save(politician);
    verify(followRepository).save(any());
    ArgumentCaptor<FollowCreated> captor = ArgumentCaptor.forClass(FollowCreated.class);
    verify(eventPublisher).publish(captor.capture());
    assertThat(captor.getValue().followerAccountId()).isEqualTo(follower);
    assertThat(captor.getValue().targetId()).isEqualTo(politicianId);
  }

  @Test
  void followingTwiceThrows() {
    UUID follower = UUID.randomUUID();
    UUID politicianId = UUID.randomUUID();
    when(followRepository.exists(follower, FollowTargetType.POLITICIAN, politicianId)).thenReturn(true);

    assertThatThrownBy(() -> service.follow(follower, FollowTargetType.POLITICIAN, politicianId)).isInstanceOf(AlreadyFollowingException.class);

    verify(politicianRepository, never()).save(any());
  }

  @Test
  void followingUnknownPoliticianThrows() {
    UUID follower = UUID.randomUUID();
    UUID politicianId = UUID.randomUUID();
    when(followRepository.exists(follower, FollowTargetType.POLITICIAN, politicianId)).thenReturn(false);
    when(politicianRepository.findById(politicianId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.follow(follower, FollowTargetType.POLITICIAN, politicianId)).isInstanceOf(PoliticianNotFoundException.class);
  }

  @Test
  void unfollowingSomethingNotFollowedIsANoOp() {
    UUID follower = UUID.randomUUID();
    UUID politicianId = UUID.randomUUID();
    when(followRepository.exists(follower, FollowTargetType.POLITICIAN, politicianId)).thenReturn(false);

    service.unfollow(follower, FollowTargetType.POLITICIAN, politicianId);

    verify(followRepository, never()).delete(any(), any(), any());
    verify(eventPublisher, never()).publish(any());
  }

  @Test
  void unfollowDecrementsFollowersAndPublishesEvent() {
    UUID follower = UUID.randomUUID();
    UUID politicianId = UUID.randomUUID();
    Politician politician = Politician.project(politicianId, "Jane Doe", "janedoe", null, null, null, null, null, null, NOW);
    politician.incrementFollowers(NOW);
    when(followRepository.exists(follower, FollowTargetType.POLITICIAN, politicianId)).thenReturn(true);
    when(politicianRepository.findById(politicianId)).thenReturn(Optional.of(politician));

    service.unfollow(follower, FollowTargetType.POLITICIAN, politicianId);

    assertThat(politician.followersCount()).isZero();
    verify(followRepository, times(1)).delete(follower, FollowTargetType.POLITICIAN, politicianId);
    verify(eventPublisher).publish(any());
  }
}
