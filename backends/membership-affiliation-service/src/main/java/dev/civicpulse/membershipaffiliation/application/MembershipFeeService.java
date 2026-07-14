package dev.civicpulse.membershipaffiliation.application;

import dev.civicpulse.membershipaffiliation.application.port.in.ManageMembershipFeeUseCase;
import dev.civicpulse.membershipaffiliation.application.port.out.EventPublisher;
import dev.civicpulse.membershipaffiliation.application.port.out.MembershipFeeRepository;
import dev.civicpulse.membershipaffiliation.domain.event.MembershipFeeGenerated;
import dev.civicpulse.membershipaffiliation.domain.event.MembershipFeeOverdue;
import dev.civicpulse.membershipaffiliation.domain.exception.MembershipFeeNotFoundException;
import dev.civicpulse.membershipaffiliation.domain.model.FeeStatus;
import dev.civicpulse.membershipaffiliation.domain.model.MembershipFee;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MembershipFeeService implements ManageMembershipFeeUseCase {

  private final MembershipFeeRepository feeRepository;
  private final EventPublisher eventPublisher;
  private final Clock clock;

  public MembershipFeeService(MembershipFeeRepository feeRepository, EventPublisher eventPublisher, Clock clock) {
    this.feeRepository = feeRepository;
    this.eventPublisher = eventPublisher;
    this.clock = clock;
  }

  @Override
  @Transactional
  public MembershipFee generateFee(UUID affiliationId, String referencePeriod, long amountCents, LocalDate dueDate) {
    MembershipFee fee = feeRepository.save(MembershipFee.generate(UUID.randomUUID(), affiliationId, referencePeriod, amountCents, dueDate));
    eventPublisher.publish(new MembershipFeeGenerated(fee.id(), affiliationId, amountCents, clock.instant()));
    return fee;
  }

  @Override
  @Transactional
  public void markOverdueFees() {
    LocalDate today = LocalDate.now(clock);
    for (MembershipFee fee : feeRepository.findByStatusAndDueDateBefore(FeeStatus.PENDING, today)) {
      fee.markOverdue();
      feeRepository.save(fee);
      eventPublisher.publish(new MembershipFeeOverdue(fee.id(), fee.affiliationId(), clock.instant()));
    }
  }

  @Override
  @Transactional
  public void onPaymentCaptured(UUID feeId, UUID paymentIntentId) {
    MembershipFee fee = feeRepository.findById(feeId).orElseThrow(() -> new MembershipFeeNotFoundException(feeId));
    if (fee.status() == FeeStatus.PAID) {
      return; // idempotent — reprocessed message
    }
    fee.markPaid(paymentIntentId, clock.instant());
    feeRepository.save(fee);
  }

  @Override
  @Transactional(readOnly = true)
  public List<MembershipFee> listByAffiliation(UUID affiliationId) {
    return feeRepository.findByAffiliationId(affiliationId);
  }
}
