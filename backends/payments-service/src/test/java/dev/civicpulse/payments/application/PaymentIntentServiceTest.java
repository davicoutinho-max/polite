package dev.civicpulse.payments.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.civicpulse.payments.application.port.out.CheckoutGateway;
import dev.civicpulse.payments.application.port.out.CheckoutGateway.CheckoutResult;
import dev.civicpulse.payments.application.port.out.LedgerEntryRepository;
import dev.civicpulse.payments.application.port.out.OutboxEventRepository;
import dev.civicpulse.payments.application.port.out.PayerLookupGateway;
import dev.civicpulse.payments.application.port.out.PayerLookupGateway.PayerInfo;
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
  @Mock private PayerLookupGateway payerLookupGateway;
  @Mock private CheckoutGateway checkoutGateway;

  private PaymentIntentService service;

  @BeforeEach
  void setUp() {
    ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    service =
        new PaymentIntentService(
            paymentIntentRepository, ledgerEntryRepository, outboxEventRepository, payerLookupGateway, checkoutGateway, objectMapper,
            Clock.fixed(NOW, ZoneOffset.UTC));
  }

  @Test
  void createPendingPaymentIsIdempotentForSameKey() {
    String idempotencyKey = "key-1";
    PaymentIntent existing =
        PaymentIntent.create(
            UUID.randomUUID(), PaymentPurpose.MEMBERSHIP_FEE, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), 5000, "BRL",
            PaymentGatewayType.PIX, idempotencyKey, NOW);
    when(paymentIntentRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.of(existing));

    PaymentIntent result =
        service.createPendingPayment(
            PaymentPurpose.MEMBERSHIP_FEE, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), 5000, PaymentGatewayType.PIX, idempotencyKey);

    assertThat(result).isEqualTo(existing);
    verify(paymentIntentRepository, never()).save(any());
  }

  @Test
  void createPendingPaymentCreatesIntentWithoutCallingTheGatewayYet() {
    when(paymentIntentRepository.findByIdempotencyKey(anyString())).thenReturn(Optional.empty());
    when(paymentIntentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    PaymentIntent result =
        service.createPendingPayment(
            PaymentPurpose.FUNDRAISING_CONTRIBUTION, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), 12345, PaymentGatewayType.PIX,
            "key-2");

    assertThat(result.status().code()).isEqualTo("created");
    assertThat(result.gateway().code()).isEqualTo("pix");
    verify(checkoutGateway, never()).createPayment(any(), any());
  }

  @Test
  void createCheckoutUrlResolvesPayerThenAsksTheGatewayForAnInvoice() {
    UUID intentId = UUID.randomUUID();
    UUID payerAccountId = UUID.randomUUID();
    PaymentIntent intent =
        PaymentIntent.create(
            intentId, PaymentPurpose.FUNDRAISING_CONTRIBUTION, UUID.randomUUID(), payerAccountId, UUID.randomUUID(), 5000, "BRL",
            PaymentGatewayType.CARD, "key-3", NOW);
    PayerInfo payer = new PayerInfo("Jane Doe", "52998224725");
    when(paymentIntentRepository.findById(intentId)).thenReturn(Optional.of(intent));
    when(payerLookupGateway.getPaymentProfile(payerAccountId)).thenReturn(payer);
    when(checkoutGateway.createPayment(intent, payer)).thenReturn(new CheckoutResult("https://asaas.example/i/abc", "pay_abc"));

    String url = service.createCheckoutUrl(intentId);

    assertThat(url).isEqualTo("https://asaas.example/i/abc");
  }

  @Test
  void confirmPaymentIsIdempotentWhenAlreadyCaptured() {
    UUID intentId = UUID.randomUUID();
    PaymentIntent intent =
        PaymentIntent.create(
            intentId, PaymentPurpose.FUNDRAISING_CONTRIBUTION, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), 5000, "BRL",
            PaymentGatewayType.CARD, "key-4", NOW);
    intent.authorize("pay_abc", NOW);
    intent.capture(NOW);
    when(paymentIntentRepository.findById(intentId)).thenReturn(Optional.of(intent));

    PaymentIntent result = service.confirmPayment(intentId, "pay_abc");

    assertThat(result.status().code()).isEqualTo("captured");
    verify(paymentIntentRepository, never()).save(any());
    verify(ledgerEntryRepository, never()).save(any());
  }

  @Test
  void confirmPaymentAuthorizesThenCapturesAndWritesBothOutboxEvents() {
    UUID intentId = UUID.randomUUID();
    UUID payer = UUID.randomUUID();
    UUID payee = UUID.randomUUID();
    PaymentIntent intent =
        PaymentIntent.create(intentId, PaymentPurpose.MEMBERSHIP_FEE, UUID.randomUUID(), payer, payee, 5000, "BRL", PaymentGatewayType.PIX, "key-5", NOW);
    when(paymentIntentRepository.findById(intentId)).thenReturn(Optional.of(intent));
    when(paymentIntentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    when(ledgerEntryRepository.currentBalance(payer)).thenReturn(0L);
    when(ledgerEntryRepository.currentBalance(payee)).thenReturn(0L);

    PaymentIntent result = service.confirmPayment(intentId, "pay_xyz");

    assertThat(result.status().code()).isEqualTo("captured");
    ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
    verify(outboxEventRepository, times(2)).save(captor.capture());
    assertThat(captor.getAllValues()).extracting(OutboxEvent::eventType).containsExactly("PaymentAuthorized", "PaymentCaptured");
  }

  @Test
  void captureWritesTwoLedgerEntriesAndOutboxEvent() {
    UUID intentId = UUID.randomUUID();
    UUID payer = UUID.randomUUID();
    UUID payee = UUID.randomUUID();
    PaymentIntent intent =
        PaymentIntent.create(intentId, PaymentPurpose.MEMBERSHIP_FEE, UUID.randomUUID(), payer, payee, 5000, "BRL", PaymentGatewayType.PIX, "key-6", NOW);
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
