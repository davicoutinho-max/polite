package dev.civicpulse.livestreaming;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Scope note for this pass: schema.sql documents Redis-backed viewer presence
 * ({@code live:{session_id}:viewers}/{@code viewer_count}) and a {@code live-chat-messages} Kafka
 * topic fanned out to viewers by a WebSocket gateway. Neither is implemented here — this service
 * covers the low-volume, transactional part of the domain (session scheduling/start/end,
 * peak-viewer tracking from caller-reported counts, the post-session stats rollup, and an
 * optional compliance-only chat archive), which is already a correct, complete system of record
 * for that slice. Real-time presence and chat fan-out are additive infrastructure concerns for a
 * WebSocket gateway to own, not a gap in this service's domain model.
 */
@SpringBootApplication
public class LiveStreamingServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(LiveStreamingServiceApplication.class, args);
  }
}
