package dev.civicpulse.livestreaming.adapter.in.web;

import dev.civicpulse.livestreaming.adapter.in.web.dto.AttachPostRequest;
import dev.civicpulse.livestreaming.adapter.in.web.dto.EndLiveSessionRequest;
import dev.civicpulse.livestreaming.adapter.in.web.dto.LiveSessionResponse;
import dev.civicpulse.livestreaming.adapter.in.web.dto.LiveSessionStatsResponse;
import dev.civicpulse.livestreaming.adapter.in.web.dto.RecordViewerCountRequest;
import dev.civicpulse.livestreaming.adapter.in.web.dto.ScheduleLiveSessionRequest;
import dev.civicpulse.livestreaming.application.port.in.GetLiveSessionUseCase;
import dev.civicpulse.livestreaming.application.port.in.ManageLiveSessionUseCase;
import dev.civicpulse.livestreaming.domain.model.LiveSession;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/live-sessions")
public class LiveSessionController {

  private final ManageLiveSessionUseCase manageLiveSessionUseCase;
  private final GetLiveSessionUseCase getLiveSessionUseCase;

  public LiveSessionController(ManageLiveSessionUseCase manageLiveSessionUseCase, GetLiveSessionUseCase getLiveSessionUseCase) {
    this.manageLiveSessionUseCase = manageLiveSessionUseCase;
    this.getLiveSessionUseCase = getLiveSessionUseCase;
  }

  @PostMapping
  public ResponseEntity<LiveSessionResponse> schedule(
      @RequestHeader("X-Account-Id") UUID hostAccountId, @RequestBody ScheduleLiveSessionRequest request) {
    LiveSession session = manageLiveSessionUseCase.schedule(hostAccountId, request.videoId(), request.channelId(), request.scheduledFor());
    return created(session);
  }

  @PostMapping("/{sessionId}/start")
  public LiveSessionResponse start(@PathVariable UUID sessionId) {
    return LiveSessionResponse.from(manageLiveSessionUseCase.start(sessionId));
  }

  @PostMapping("/{sessionId}/end")
  public LiveSessionResponse end(@PathVariable UUID sessionId, @RequestBody(required = false) EndLiveSessionRequest request) {
    EndLiveSessionRequest body = request == null ? new EndLiveSessionRequest(null, null) : request;
    return LiveSessionResponse.from(manageLiveSessionUseCase.end(sessionId, body.totalUniqueViewers(), body.avgWatchSeconds()));
  }

  @PostMapping("/{sessionId}/post")
  public LiveSessionResponse attachPost(@PathVariable UUID sessionId, @Valid @RequestBody AttachPostRequest request) {
    return LiveSessionResponse.from(manageLiveSessionUseCase.attachPost(sessionId, request.postId()));
  }

  @PostMapping("/{sessionId}/viewer-count")
  public LiveSessionResponse recordViewerCount(@PathVariable UUID sessionId, @Valid @RequestBody RecordViewerCountRequest request) {
    return LiveSessionResponse.from(manageLiveSessionUseCase.recordViewerCount(sessionId, request.currentViewers()));
  }

  @GetMapping("/{sessionId}")
  public LiveSessionResponse getById(@PathVariable UUID sessionId) {
    return LiveSessionResponse.from(getLiveSessionUseCase.getById(sessionId));
  }

  @GetMapping("/{sessionId}/stats")
  public ResponseEntity<LiveSessionStatsResponse> getStats(@PathVariable UUID sessionId) {
    return getLiveSessionUseCase
        .getStats(sessionId)
        .map(stats -> ResponseEntity.ok(LiveSessionStatsResponse.from(stats)))
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @GetMapping("/live")
  public List<LiveSessionResponse> listLive() {
    return getLiveSessionUseCase.listLive().stream().map(LiveSessionResponse::from).toList();
  }

  private static ResponseEntity<LiveSessionResponse> created(LiveSession session) {
    LiveSessionResponse body = LiveSessionResponse.from(session);
    return ResponseEntity.created(URI.create("/live-sessions/" + body.id())).body(body);
  }
}
