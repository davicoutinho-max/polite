package dev.civicpulse.legislative.application.port.out;

import dev.civicpulse.legislative.domain.model.VoteRecord;
import java.util.List;
import java.util.UUID;

public interface VoteRecordRepository {

  VoteRecord save(VoteRecord voteRecord);

  List<VoteRecord> findByPolitician(UUID politicianAccountId);
}
