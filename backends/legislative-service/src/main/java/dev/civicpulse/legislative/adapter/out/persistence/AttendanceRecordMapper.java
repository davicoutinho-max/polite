package dev.civicpulse.legislative.adapter.out.persistence;

import dev.civicpulse.legislative.domain.model.AttendanceRecord;
import org.springframework.stereotype.Component;

@Component
class AttendanceRecordMapper {

  AttendanceRecord toDomain(AttendanceRecordJpaEntity entity) {
    return AttendanceRecord.reconstitute(
        entity.getPoliticianAccountId(), entity.getPresent(), entity.getAbsent(), entity.getUpdatedAt());
  }

  AttendanceRecordJpaEntity toEntity(AttendanceRecord record) {
    return new AttendanceRecordJpaEntity(record.politicianAccountId(), record.present(), record.absent(), record.updatedAt());
  }
}
