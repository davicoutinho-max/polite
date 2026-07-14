package dev.civicpulse.membershipaffiliation.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.civicpulse.membershipaffiliation.application.port.out.EventPublisher;
import dev.civicpulse.membershipaffiliation.application.port.out.MembershipFeeRepository;
import dev.civicpulse.membershipaffiliation.domain.model.FeeStatus;
import dev.civicpulse.membershipaffiliation.domain.model.MembershipFee;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MembershipFeeServiceTest {

  private static final Instant NOW = Instant.parse("2026-07-15T00:00:00Z");

  @Mock private MembershipFeeRepository feeRepository;
  @Mock private EventPublisher eventPublisher;

  private MembershipFeeService service;

  @BeforeEach
  void setUp() {
    service = new MembershipFeeService(feeRepository, eventPublisher, Clock.fixed(NOW, ZoneOffset.UTC));
  }

  @Test
  void generateFeePublishesEvent() {
    UUID affiliationId = UUID.randomUUID();
    when(feeRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    MembershipFee fee = service.generateFee(affiliationId, "2026-07", 5000, LocalDate.of(2026, 7, 20));

    assertThat(fee.status()).isEqualTo(FeeStatus.PENDING);
    verify(eventPublisher).publish(any());
  }

  @Test
  void markOverdueFeesTransitionsAndPublishesPerFee() {
    MembershipFee fee1 = MembershipFee.generate(UUID.randomUUID(), UUID.randomUUID(), "2026-06", 5000, LocalDate.of(2026, 6, 20));
    MembershipFee fee2 = MembershipFee.generate(UUID.randomUUID(), UUID.randomUUID(), "2026-05", 5000, LocalDate.of(2026, 5, 20));
    when(feeRepository.findByStatusAndDueDateBefore(FeeStatus.PENDING, LocalDate.of(2026, 7, 15))).thenReturn(List.of(fee1, fee2));
    when(feeRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    service.markOverdueFees();

    assertThat(fee1.status()).isEqualTo(FeeStatus.OVERDUE);
    assertThat(fee2.status()).isEqualTo(FeeStatus.OVERDUE);
    verify(eventPublisher, org.mockito.Mockito.times(2)).publish(any());
  }

  @Test
  void onPaymentCapturedIsIdempotentForAlreadyPaidFee() {
    UUID feeId = UUID.randomUUID();
    MembershipFee fee = MembershipFee.generate(feeId, UUID.randomUUID(), "2026-07", 5000, LocalDate.of(2026, 7, 20));
    fee.markPaid(UUID.randomUUID(), NOW);
    when(feeRepository.findById(feeId)).thenReturn(Optional.of(fee));

    service.onPaymentCaptured(feeId, UUID.randomUUID());

    verify(feeRepository, never()).save(any());
  }

  @Test
  void onPaymentCapturedMarksFeePaid() {
    UUID feeId = UUID.randomUUID();
    UUID paymentIntentId = UUID.randomUUID();
    MembershipFee fee = MembershipFee.generate(feeId, UUID.randomUUID(), "2026-07", 5000, LocalDate.of(2026, 7, 20));
    when(feeRepository.findById(feeId)).thenReturn(Optional.of(fee));
    when(feeRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    service.onPaymentCaptured(feeId, paymentIntentId);

    assertThat(fee.status()).isEqualTo(FeeStatus.PAID);
    assertThat(fee.paymentIntentId()).contains(paymentIntentId);
  }
}
