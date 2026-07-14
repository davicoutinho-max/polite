package dev.civicpulse.payments.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.civicpulse.payments.application.port.out.LedgerEntryRepository;
import dev.civicpulse.payments.application.port.out.OutboxEventRepository;
import dev.civicpulse.payments.application.port.out.PaymentGateway;
import dev.civicpulse.payments.application.port.out.PaymentGateway.AuthorizationResult;
import dev.civicpulse.payments.application.port.out.PaymentIntentRepository;
import dev.civicpulse.payments.domain.model.OutboxEvent;
import dev.civicpulse.payments.domain.model.PaymentGatewayType;
import dev.civicpulse.payments.domain.model.PaymentIntent;
import dev.civicpulse.payments.domain.model.PaymentPurpose;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentIntentServiceTest {

  private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

  @Mock private PaymentIntentRepository paymentIntentRepository;
  @Mock private LedgerEntryRepository ledgerEntryRepository;
  @Mock private OutboxEventRepository outboxEventRepository;
  @Mock private PaymentGateway paymentGateway;

  private PaymentIntentService service;

  @BeforeEach
  void setUp() {
    ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    service =
        new PaymentIntentService(
            paymentIntentRepository, ledgerEntryRepository, outboxEventRepository, paymentGateway, objectMapper, Clock.fixed(NOW, ZoneOffset.UTC));
  }

  @Test
  void createAndAuthorizeIsIdempotentForSameKey() {
    String idempotencyKey = "key-1";
    PaymentIntent existing =
        PaymentIntent.create(
            UUID.randomUUID(), PaymentPurpose.MEMBERSHIP_FEE, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), 5000, "BRL",
            PaymentGatewayType.PIX, idempotencyKey, NOW);
    when(paymentIntentRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.of(existing));

    PaymentIntent result =
        service.createAndAuthorize(
            PaymentPurpose.MEMBERSHIP_FEE, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), 5000, PaymentGatewayType.PIX, idempotencyKey);

    assertThat(result).isEqualTo(existing);
    verify(paymentGateway, never()).authorize(any(), anyLong(), anyString());
  }

  @Test
  void createAndAuthorizeWritesOutboxEventOnApproval() {
    when(paymentIntentRepository.findByIdempotencyKey(anyString())).thenReturn(Optional.empty());
    when(paymentIntentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    when(paymentGateway.authorize(any(), anyLong(), anyString())).thenReturn(new AuthorizationResult(true, "gw-ref-1"));

    PaymentIntent result =
        service.createAndAuthorize(
            PaymentPurpose.MEMBERSHIP_FEE, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), 5000, PaymentGatewayType.PIX, "key-2");

    assertThat(result.status().code()).isEqualTo("authorized");
    ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
    verify(outboxEventRepository).save(captor.capture());
    assertThat(captor.getValue().eventType()).isEqualTo("PaymentAuthorized");
  }

  @Test
  void createAndAuthorizeWritesFailedOutboxEventOnDecline() {
    when(paymentIntentRepository.findByIdempotencyKey(anyString())).thenReturn(Optional.empty());
    when(paymentIntentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    when(paymentGateway.authorize(any(), anyLong(), anyString())).thenReturn(new AuthorizationResult(false, null));

    PaymentIntent result =
        service.createAndAuthorize(
            PaymentPurpose.MEMBERSHIP_FEE, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), 5000, PaymentGatewayType.PIX, "key-3");

    assertThat(result.status().code()).isEqualTo("failed");
    ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
    verify(outboxEventRepository).save(captor.capture());
    assertThat(captor.getValue().eventType()).isEqualTo("PaymentFailed");
  }

  @Test
  void captureWritesTwoLedgerEntriesAndOutboxEvent() {
    UUID intentId = UUID.randomUUID();
    UUID payer = UUID.randomUUID();
    UUID payee = UUID.randomUUID();
    PaymentIntent intent =
        PaymentIntent.create(intentId, PaymentPurpose.MEMBERSHIP_FEE, UUID.randomUUID(), payer, payee, 5000, "BRL", PaymentGatewayType.PIX, "key-4", NOW);
    intent.authorize("gw-ref-1", NOW);
    when(paymentIntentRepository.findById(intentId)).thenReturn(Optional.of(intent));
    when(paymentIntentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    when(ledgerEntryRepository.currentBalance(payer)).thenReturn(0L);
    when(ledgerEntryRepository.currentBalance(payee)).thenReturn(0L);

    PaymentIntent result = service.capture(intentId);

    assertThat(result.status().code()).isEqualTo("captured");
    verify(ledgerEntryRepository, times(2)).save(any());
    verify(outboxEventRepository).save(any());
  }
}
