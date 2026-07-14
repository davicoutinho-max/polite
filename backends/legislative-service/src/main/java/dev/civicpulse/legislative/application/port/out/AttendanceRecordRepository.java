package dev.civicpulse.legislative.application.port.out;

import dev.civicpulse.legislative.domain.model.AttendanceRecord;
import java.util.Optional;
import java.util.UUID;

public interface AttendanceRecordRepository {

  AttendanceRecord save(AttendanceRecord record);

  Optional<AttendanceRecord> findById(UUID politicianAccountId);
}
