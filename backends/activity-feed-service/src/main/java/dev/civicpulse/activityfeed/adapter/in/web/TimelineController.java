package dev.civicpulse.activityfeed.adapter.in.web;

import dev.civicpulse.activityfeed.adapter.in.web.dto.TimelineEventResponse;
import dev.civicpulse.activityfeed.application.port.in.GetTimelineUseCase;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/timeline")
public class TimelineController {

  private final GetTimelineUseCase getTimelineUseCase;

  public TimelineController(GetTimelineUseCase getTimelineUseCase) {
    this.getTimelineUseCase = getTimelineUseCase;
  }

  @GetMapping
  public List<TimelineEventResponse> list(@RequestParam UUID subjectAccountId, @RequestParam(defaultValue = "20") int limit) {
    return getTimelineUseCase.getTimeline(subjectAccountId, limit).stream().map(TimelineEventResponse::from).toList();
  }
}
