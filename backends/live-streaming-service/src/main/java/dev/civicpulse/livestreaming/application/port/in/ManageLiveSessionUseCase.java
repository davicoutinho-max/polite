package dev.civicpulse.livestreaming.application.port.in;

import dev.civicpulse.livestreaming.domain.model.LiveSession;
import java.time.Instant;
import java.util.UUID;

public interface ManageLiveSessionUseCase {

  LiveSession schedule(UUID hostAccountId, String videoId, String channelId, Instant scheduledFor);

  LiveSession start(UUID sessionId);

  /** Ends the session and writes the {@code live_session_stats} rollup in the same transaction —
   * {@code totalUniqueViewers}/{@code avgWatchSeconds} are caller-supplied (this service has no
   * viewer-presence tracking of its own; see LiveStreamingServiceApplication's scope note), while
   * {@code totalChatMessages} is derived from whatever this service has archived itself. */
  LiveSession end(UUID sessionId, Integer totalUniqueViewers, Integer avgWatchSeconds);

  LiveSession attachPost(UUID sessionId, UUID postId);

  LiveSession recordViewerCount(UUID sessionId, int currentViewers);
}
