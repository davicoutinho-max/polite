package dev.civicpulse.legislative.application;

import dev.civicpulse.legislative.application.port.in.AttendanceUseCase;
import dev.civicpulse.legislative.application.port.out.AttendanceRecordRepository;
import dev.civicpulse.legislative.domain.model.AttendanceRecord;
import java.time.Clock;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AttendanceService implements AttendanceUseCase {

  private final AttendanceRecordRepository attendanceRepository;
  private final Clock clock;

  public AttendanceService(AttendanceRecordRepository attendanceRepository, Clock clock) {
    this.attendanceRepository = attendanceRepository;
    this.clock = clock;
  }

  @Override
  @Transactional
  public AttendanceRecord recordAttendance(UUID politicianAccountId, boolean wasPresent) {
    AttendanceRecord record = attendanceRepository
        .findById(politicianAccountId)
        .orElseGet(() -> AttendanceRecord.initialize(politicianAccountId, clock.instant()));
    record.recordPresence(wasPresent, clock.instant());
    return attendanceRepository.save(record);
  }

  @Override
  public AttendanceRecord getAttendance(UUID politicianAccountId) {
    return attendanceRepository.findById(politicianAccountId).orElseGet(() -> AttendanceRecord.initialize(politicianAccountId, clock.instant()));
  }
}
