package dev.civicpulse.legislative.application.port.in;

import dev.civicpulse.legislative.domain.model.VoteChoice;
import dev.civicpulse.legislative.domain.model.VoteRecord;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface VoteUseCase {

  VoteRecord castVote(UUID politicianAccountId, UUID legislativeItemId, String matter, LocalDate voteDate, VoteChoice choice);

  List<VoteRecord> getVotes(UUID politicianAccountId);
}
