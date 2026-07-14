package dev.civicpulse.legislative.application;

import dev.civicpulse.legislative.application.port.in.VoteUseCase;
import dev.civicpulse.legislative.application.port.out.LegislativeEventPublisher;
import dev.civicpulse.legislative.application.port.out.VoteRecordRepository;
import dev.civicpulse.legislative.domain.event.VoteCast;
import dev.civicpulse.legislative.domain.model.VoteChoice;
import dev.civicpulse.legislative.domain.model.VoteRecord;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VoteService implements VoteUseCase {

  private final VoteRecordRepository voteRecordRepository;
  private final LegislativeEventPublisher eventPublisher;
  private final Clock clock;

  public VoteService(VoteRecordRepository voteRecordRepository, LegislativeEventPublisher eventPublisher, Clock clock) {
    this.voteRecordRepository = voteRecordRepository;
    this.eventPublisher = eventPublisher;
    this.clock = clock;
  }

  @Override
  @Transactional
  public VoteRecord castVote(UUID politicianAccountId, UUID legislativeItemId, String matter, LocalDate voteDate, VoteChoice choice) {
    VoteRecord saved = voteRecordRepository.save(VoteRecord.cast(politicianAccountId, legislativeItemId, matter, voteDate, choice));
    eventPublisher.publish(
        new VoteCast(saved.id().orElseThrow(), politicianAccountId, matter, choice.code(), clock.instant()));
    return saved;
  }

  @Override
  public List<VoteRecord> getVotes(UUID politicianAccountId) {
    return voteRecordRepository.findByPolitician(politicianAccountId);
  }
}
