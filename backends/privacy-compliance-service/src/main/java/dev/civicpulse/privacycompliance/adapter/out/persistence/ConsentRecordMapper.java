package dev.civicpulse.privacycompliance.adapter.out.persistence;

import dev.civicpulse.privacycompliance.domain.model.ConsentPurpose;
import dev.civicpulse.privacycompliance.domain.model.ConsentRecord;
import org.springframework.stereotype.Component;

@Component
class ConsentRecordMapper {

  ConsentRecord toDomain(ConsentRecordJpaEntity entity) {
    return ConsentRecord.reconstitute(entity.getAccountId(), ConsentPurpose.fromCode(entity.getPurpose()), entity.isGranted(), entity.getUpdatedAt());
  }

  ConsentRecordJpaEntity toEntity(ConsentRecord record) {
    return new ConsentRecordJpaEntity(record.accountId(), record.purpose().code(), record.granted(), record.updatedAt());
  }
}
