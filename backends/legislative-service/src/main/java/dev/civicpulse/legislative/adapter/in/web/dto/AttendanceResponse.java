package dev.civicpulse.legislative.adapter.in.web.dto;

import dev.civicpulse.legislative.domain.model.AttendanceRecord;
import java.time.Instant;

public record AttendanceResponse(int present, int absent, double presenceRate, Instant updatedAt) {

  public static AttendanceResponse from(AttendanceRecord record) {
    return new AttendanceResponse(record.present(), record.absent(), record.presenceRate(), record.updatedAt());
  }
}
