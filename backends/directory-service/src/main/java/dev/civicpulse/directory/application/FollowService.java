package dev.civicpulse.directory.application;

import dev.civicpulse.directory.application.port.in.FollowUseCase;
import dev.civicpulse.directory.application.port.out.EventPublisher;
import dev.civicpulse.directory.application.port.out.FollowRepository;
import dev.civicpulse.directory.application.port.out.PartyRepository;
import dev.civicpulse.directory.application.port.out.PoliticianRepository;
import dev.civicpulse.directory.domain.event.FollowCreated;
import dev.civicpulse.directory.domain.event.FollowRemoved;
import dev.civicpulse.directory.domain.exception.AlreadyFollowingException;
import dev.civicpulse.directory.domain.exception.PartyNotFoundException;
import dev.civicpulse.directory.domain.exception.PoliticianNotFoundException;
import dev.civicpulse.directory.domain.model.Follow;
import dev.civicpulse.directory.domain.model.FollowTargetType;
import dev.civicpulse.directory.domain.model.Party;
import dev.civicpulse.directory.domain.model.Politician;
import java.time.Clock;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FollowService implements FollowUseCase {

  private final FollowRepository followRepository;
  private final PoliticianRepository politicianRepository;
  private final PartyRepository partyRepository;
  private final EventPublisher eventPublisher;
  private final Clock clock;

  public FollowService(
      FollowRepository followRepository,
      PoliticianRepository politicianRepository,
      PartyRepository partyRepository,
      EventPublisher eventPublisher,
      Clock clock) {
    this.followRepository = followRepository;
    this.politicianRepository = politicianRepository;
    this.partyRepository = partyRepository;
    this.eventPublisher = eventPublisher;
    this.clock = clock;
  }

  @Override
  @Transactional
  public void follow(UUID followerAccountId, FollowTargetType targetType, UUID targetId) {
    if (followRepository.exists(followerAccountId, targetType, targetId)) {
      throw new AlreadyFollowingException();
    }
    var now = clock.instant();
    if (targetType == FollowTargetType.POLITICIAN) {
      Politician politician = politicianRepository.findById(targetId).orElseThrow(() -> new PoliticianNotFoundException(targetId));
      politician.incrementFollowers(now);
      politicianRepository.save(politician);
    } else {
      Party party = partyRepository.findById(targetId).orElseThrow(() -> new PartyNotFoundException(targetId));
      party.incrementMembers(now);
      partyRepository.save(party);
    }
    followRepository.save(Follow.create(followerAccountId, targetType, targetId, now));
    eventPublisher.publish(new FollowCreated(followerAccountId, targetType.code(), targetId, now));
  }

  @Override
  @Transactional
  public void unfollow(UUID followerAccountId, FollowTargetType targetType, UUID targetId) {
    if (!followRepository.exists(followerAccountId, targetType, targetId)) {
      return; // idempotent — unfollowing something you don't follow is a no-op, not an error
    }
    var now = clock.instant();
    followRepository.delete(followerAccountId, targetType, targetId);
    if (targetType == FollowTargetType.POLITICIAN) {
      politicianRepository
          .findById(targetId)
          .ifPresent(
              politician -> {
                politician.decrementFollowers(now);
                politicianRepository.save(politician);
              });
    } else {
      partyRepository
          .findById(targetId)
          .ifPresent(
              party -> {
                party.decrementMembers(now);
                partyRepository.save(party);
              });
    }
    eventPublisher.publish(new FollowRemoved(followerAccountId, targetType.code(), targetId, now));
  }

  @Override
  @Transactional(readOnly = true)
  public boolean isFollowing(UUID followerAccountId, FollowTargetType targetType, UUID targetId) {
    return followRepository.exists(followerAccountId, targetType, targetId);
  }

  @Override
  @Transactional(readOnly = true)
  public List<UUID> listFollowingTargets(UUID followerAccountId, FollowTargetType targetType) {
    return followRepository.findByFollower(followerAccountId).stream()
        .filter(follow -> follow.targetType() == targetType)
        .map(Follow::targetId)
        .collect(Collectors.toList());
  }
}
