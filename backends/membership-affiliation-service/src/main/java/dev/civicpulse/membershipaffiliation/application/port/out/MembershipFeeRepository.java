package dev.civicpulse.membershipaffiliation.application.port.out;

import dev.civicpulse.membershipaffiliation.domain.model.FeeStatus;
import dev.civicpulse.membershipaffiliation.domain.model.MembershipFee;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MembershipFeeRepository {

  MembershipFee save(MembershipFee fee);

  Optional<MembershipFee> findById(UUID id);

  List<MembershipFee> findByAffiliationId(UUID affiliationId);

  List<MembershipFee> findByStatusAndDueDateBefore(FeeStatus status, LocalDate date);
}
