package dev.civicpulse.membershipaffiliation.config;

import dev.civicpulse.membershipaffiliation.application.port.in.ManageMembershipFeeUseCase;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
class FeeOverdueScheduler {

  private final ManageMembershipFeeUseCase manageMembershipFeeUseCase;

  FeeOverdueScheduler(ManageMembershipFeeUseCase manageMembershipFeeUseCase) {
    this.manageMembershipFeeUseCase = manageMembershipFeeUseCase;
  }

  /** Once a day is plenty for a due-date sweep — see docs/db's
   * {@code idx_fees_status_due} index, sized for this exact scan. */
  @Scheduled(cron = "0 0 3 * * *")
  void sweepOverdueFees() {
    manageMembershipFeeUseCase.markOverdueFees();
  }
}
