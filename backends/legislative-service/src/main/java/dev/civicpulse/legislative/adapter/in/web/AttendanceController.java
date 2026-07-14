package dev.civicpulse.legislative.adapter.in.web;

import dev.civicpulse.legislative.adapter.in.web.dto.AttendanceResponse;
import dev.civicpulse.legislative.adapter.in.web.dto.RecordAttendanceRequest;
import dev.civicpulse.legislative.application.port.in.AttendanceUseCase;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/politicians/{politicianAccountId}/attendance")
public class AttendanceController {

  private final AttendanceUseCase attendanceUseCase;

  public AttendanceController(AttendanceUseCase attendanceUseCase) {
    this.attendanceUseCase = attendanceUseCase;
  }

  @GetMapping
  public AttendanceResponse get(@PathVariable UUID politicianAccountId) {
    return AttendanceResponse.from(attendanceUseCase.getAttendance(politicianAccountId));
  }

  @PostMapping
  public AttendanceResponse record(@PathVariable UUID politicianAccountId, @RequestBody RecordAttendanceRequest request) {
    return AttendanceResponse.from(attendanceUseCase.recordAttendance(politicianAccountId, request.present()));
  }
}
