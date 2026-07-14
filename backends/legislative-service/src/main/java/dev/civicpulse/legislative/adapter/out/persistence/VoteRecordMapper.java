package dev.civicpulse.legislative.adapter.out.persistence;

import dev.civicpulse.legislative.domain.model.VoteRecord;
import org.springframework.stereotype.Component;

@Component
class VoteRecordMapper {

  VoteRecord toDomain(VoteRecordJpaEntity entity) {
    return VoteRecord.reconstitute(
        entity.getId(), entity.getPoliticianAccountId(), entity.getLegislativeItemId(), entity.getMatter(), entity.getVoteDate(), entity.getChoice());
  }

  VoteRecordJpaEntity toEntity(VoteRecord voteRecord) {
    return new VoteRecordJpaEntity(
        voteRecord.id().orElse(null),
        voteRecord.politicianAccountId(),
        voteRecord.legislativeItemId().orElse(null),
        voteRecord.matter(),
        voteRecord.voteDate(),
        voteRecord.choice());
  }
}
