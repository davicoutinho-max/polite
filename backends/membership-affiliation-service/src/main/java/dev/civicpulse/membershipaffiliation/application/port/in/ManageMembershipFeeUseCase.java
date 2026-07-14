package dev.civicpulse.membershipaffiliation.application.port.in;

import dev.civicpulse.membershipaffiliation.domain.model.MembershipFee;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ManageMembershipFeeUseCase {

  MembershipFee generateFee(UUID affiliationId, String referencePeriod, long amountCents, LocalDate dueDate);

  /** Sweeps all still-pending fees past their due date to OVERDUE — called on a schedule (see
   * config.FeeOverdueScheduler). */
  void markOverdueFees();

  /** Consumes {@code PaymentCaptured}. */
  void onPaymentCaptured(UUID feeId, UUID paymentIntentId);

  List<MembershipFee> listByAffiliation(UUID affiliationId);
}
