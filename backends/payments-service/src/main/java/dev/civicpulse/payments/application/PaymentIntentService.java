package dev.civicpulse.payments.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.civicpulse.payments.application.port.in.ManagePaymentIntentUseCase;
import dev.civicpulse.payments.application.port.out.LedgerEntryRepository;
import dev.civicpulse.payments.application.port.out.OutboxEventRepository;
import dev.civicpulse.payments.application.port.out.PaymentGateway;
import dev.civicpulse.payments.application.port.out.PaymentIntentRepository;
import dev.civicpulse.payments.domain.event.DomainEvent;
import dev.civicpulse.payments.domain.event.PaymentAuthorized;
import dev.civicpulse.payments.domain.event.PaymentCaptured;
import dev.civicpulse.payments.domain.event.PaymentFailed;
import dev.civicpulse.payments.domain.event.PaymentRefunded;
import dev.civicpulse.payments.domain.exception.PaymentIntentNotFoundException;
import dev.civicpulse.payments.domain.model.LedgerDirection;
import dev.civicpulse.payments.domain.model.LedgerEntry;
import dev.civicpulse.payments.domain.model.OutboxEvent;
import dev.civicpulse.payments.domain.model.PaymentGatewayType;
import dev.civicpulse.payments.domain.model.PaymentIntent;
import dev.civicpulse.payments.domain.model.PaymentPurpose;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentIntentService implements ManagePaymentIntentUseCase {

  private final PaymentIntentRepository paymentIntentRepository;
  private final LedgerEntryRepository ledgerEntryRepository;
  private final OutboxEventRepository outboxEventRepository;
  private final PaymentGateway paymentGateway;
  private final ObjectMapper objectMapper;
  private final Clock clock;

  public PaymentIntentService(
      PaymentIntentRepository paymentIntentRepository,
      LedgerEntryRepository ledgerEntryRepository,
      OutboxEventRepository outboxEventRepository,
      PaymentGateway paymentGateway,
      ObjectMapper objectMapper,
      Clock clock) {
    this.paymentIntentRepository = paymentIntentRepository;
    this.ledgerEntryRepository = ledgerEntryRepository;
    this.outboxEventRepository = outboxEventRepository;
    this.paymentGateway = paymentGateway;
    this.objectMapper = objectMapper;
    this.clock = clock;
  }

  @Override
  @Transactional
  public PaymentIntent createAndAuthorize(
      PaymentPurpose purpose, UUID referenceId, UUID payerAccountId, UUID payeeId, long amountCents, PaymentGatewayType gateway, String idempotencyKey) {
    var existing = paymentIntentRepository.findByIdempotencyKey(idempotencyKey);
    if (existing.isPresent()) {
      return existing.get();
    }

    Instant now = clock.instant();
    PaymentIntent intent =
        PaymentIntent.create(UUID.randomUUID(), purpose, referenceId, payerAccountId, payeeId, amountCents, "BRL", gateway, idempotencyKey, now);
    paymentIntentRepository.save(intent);

    var authorization = paymentGateway.authorize(gateway, amountCents, idempotencyKey);
    if (authorization.approved()) {
      intent.authorize(authorization.gatewayRef(), now);
      paymentIntentRepository.save(intent);
      writeOutboxEvent(intent.id(), new PaymentAuthorized(intent.id(), referenceId, purpose.code(), now));
    } else {
      intent.fail(now);
      paymentIntentRepository.save(intent);
      writeOutboxEvent(intent.id(), new PaymentFailed(intent.id(), referenceId, purpose.code(), now));
    }
    return intent;
  }

  @Override
  @Transactional
  public PaymentIntent capture(UUID intentId) {
    PaymentIntent intent = paymentIntentRepository.findById(intentId).orElseThrow(() -> new PaymentIntentNotFoundException(intentId));
    Instant now = clock.instant();
    intent.capture(now);
    paymentIntentRepository.save(intent);

    long payerBalance = ledgerEntryRepository.currentBalance(intent.payerAccountId()) - intent.amountCents();
    ledgerEntryRepository.save(
        LedgerEntry.record(intent.id(), intent.payerAccountId(), LedgerDirection.DEBIT, intent.amountCents(), payerBalance, now));
    long payeeBalance = ledgerEntryRepository.currentBalance(intent.payeeId()) + intent.amountCents();
    ledgerEntryRepository.save(LedgerEntry.record(intent.id(), intent.payeeId(), LedgerDirection.CREDIT, intent.amountCents(), payeeBalance, now));

    writeOutboxEvent(intent.id(), new PaymentCaptured(intent.id(), intent.referenceId(), intent.amountCents(), now));
    return intent;
  }

  @Override
  @Transactional
  public PaymentIntent refund(UUID intentId) {
    PaymentIntent intent = paymentIntentRepository.findById(intentId).orElseThrow(() -> new PaymentIntentNotFoundException(intentId));
    Instant now = clock.instant();
    intent.refund(now);
    paymentIntentRepository.save(intent);

    long payerBalance = ledgerEntryRepository.currentBalance(intent.payerAccountId()) + intent.amountCents();
    ledgerEntryRepository.save(
        LedgerEntry.record(intent.id(), intent.payerAccountId(), LedgerDirection.CREDIT, intent.amountCents(), payerBalance, now));
    long payeeBalance = ledgerEntryRepository.currentBalance(intent.payeeId()) - intent.amountCents();
    ledgerEntryRepository.save(LedgerEntry.record(intent.id(), intent.payeeId(), LedgerDirection.DEBIT, intent.amountCents(), payeeBalance, now));

    writeOutboxEvent(intent.id(), new PaymentRefunded(intent.id(), intent.referenceId(), intent.amountCents(), now));
    return intent;
  }

  @Override
  @Transactional(readOnly = true)
  public PaymentIntent getById(UUID intentId) {
    return paymentIntentRepository.findById(intentId).orElseThrow(() -> new PaymentIntentNotFoundException(intentId));
  }

  private void writeOutboxEvent(UUID aggregateId, DomainEvent event) {
    try {
      String payload = objectMapper.writeValueAsString(event);
      outboxEventRepository.save(OutboxEvent.record(UUID.randomUUID(), aggregateId, event.getClass().getSimpleName(), payload, event.occurredAt()));
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("Failed to serialize outbox event " + event.getClass().getSimpleName(), e);
    }
  }
}
