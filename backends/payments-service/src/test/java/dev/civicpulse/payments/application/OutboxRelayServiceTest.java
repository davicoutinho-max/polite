package dev.civicpulse.payments.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.civicpulse.payments.application.port.out.OutboxEventRepository;
import dev.civicpulse.payments.application.port.out.OutboxKafkaPublisher;
import dev.civicpulse.payments.domain.event.PaymentCaptured;
import dev.civicpulse.payments.domain.model.OutboxEvent;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OutboxRelayServiceTest {

  private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

  @Mock private OutboxEventRepository outboxEventRepository;
  @Mock private OutboxKafkaPublisher outboxKafkaPublisher;

  private OutboxRelayService relay;
  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    relay = new OutboxRelayService(outboxEventRepository, outboxKafkaPublisher, objectMapper, Clock.fixed(NOW, ZoneOffset.UTC));
  }

  @Test
  void relaysUnpublishedEventAndMarksItPublished() throws Exception {
    UUID intentId = UUID.randomUUID();
    UUID referenceId = UUID.randomUUID();
    PaymentCaptured event = new PaymentCaptured(intentId, referenceId, 5000, NOW);
    String payload = objectMapper.writeValueAsString(event);
    OutboxEvent row = OutboxEvent.record(UUID.randomUUID(), intentId, "PaymentCaptured", payload, NOW);
    when(outboxEventRepository.findUnpublished(50)).thenReturn(List.of(row));

    relay.relayUnpublishedEvents();

    ArgumentCaptor<PaymentCaptured> captor = ArgumentCaptor.forClass(PaymentCaptured.class);
    verify(outboxKafkaPublisher).publishAndWait(captor.capture());
    assertThat(captor.getValue().paymentIntentId()).isEqualTo(intentId);
    assertThat(captor.getValue().referenceId()).isEqualTo(referenceId);

    ArgumentCaptor<OutboxEvent> savedCaptor = ArgumentCaptor.forClass(OutboxEvent.class);
    verify(outboxEventRepository).save(savedCaptor.capture());
    assertThat(savedCaptor.getValue().publishedAt()).isPresent();
  }

  @Test
  void leavesEventUnpublishedIfKafkaSendFails() throws Exception {
    UUID intentId = UUID.randomUUID();
    PaymentCaptured event = new PaymentCaptured(intentId, UUID.randomUUID(), 5000, NOW);
    OutboxEvent row = OutboxEvent.record(UUID.randomUUID(), intentId, "PaymentCaptured", objectMapper.writeValueAsString(event), NOW);
    when(outboxEventRepository.findUnpublished(50)).thenReturn(List.of(row));
    org.mockito.Mockito.doThrow(new IllegalStateException("broker unreachable")).when(outboxKafkaPublisher).publishAndWait(any());

    relay.relayUnpublishedEvents();

    verify(outboxEventRepository, never()).save(any());
  }
}
