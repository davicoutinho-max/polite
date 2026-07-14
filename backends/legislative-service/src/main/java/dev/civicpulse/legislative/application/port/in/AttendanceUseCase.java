package dev.civicpulse.legislative.application.port.in;

import dev.civicpulse.legislative.domain.model.AttendanceRecord;
import java.util.UUID;

public interface AttendanceUseCase {

  AttendanceRecord recordAttendance(UUID politicianAccountId, boolean wasPresent);

  AttendanceRecord getAttendance(UUID politicianAccountId);
}
