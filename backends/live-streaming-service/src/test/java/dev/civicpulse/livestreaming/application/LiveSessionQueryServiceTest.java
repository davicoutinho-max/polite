package dev.civicpulse.livestreaming.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import dev.civicpulse.livestreaming.application.port.out.LiveSessionRepository;
import dev.civicpulse.livestreaming.application.port.out.LiveSessionStatsRepository;
import dev.civicpulse.livestreaming.domain.exception.LiveSessionNotFoundException;
import dev.civicpulse.livestreaming.domain.model.LiveSession;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LiveSessionQueryServiceTest {

  @Mock private LiveSessionRepository liveSessionRepository;
  @Mock private LiveSessionStatsRepository liveSessionStatsRepository;

  private LiveSessionQueryService service;

  @BeforeEach
  void setUp() {
    service = new LiveSessionQueryService(liveSessionRepository, liveSessionStatsRepository);
  }

  @Test
  void getByIdThrowsWhenMissing() {
    UUID sessionId = UUID.randomUUID();
    when(liveSessionRepository.findById(sessionId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.getById(sessionId)).isInstanceOf(LiveSessionNotFoundException.class);
  }

  @Test
  void getByIdReturnsSessionWhenPresent() {
    UUID sessionId = UUID.randomUUID();
    LiveSession session = LiveSession.schedule(sessionId, UUID.randomUUID(), null, null, null, Instant.now());
    when(liveSessionRepository.findById(sessionId)).thenReturn(Optional.of(session));

    assertThat(service.getById(sessionId)).isEqualTo(session);
  }

  @Test
  void listLiveDelegatesToRepository() {
    when(liveSessionRepository.findLive()).thenReturn(List.of());

    assertThat(service.listLive()).isEmpty();
  }
}
