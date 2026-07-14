package dev.civicpulse.fundraising.application.port.out;

import dev.civicpulse.fundraising.domain.model.Contribution;
import java.util.List;
import java.util.UUID;

public interface ContributionRepository {

  Contribution save(Contribution contribution);

  List<Contribution> findByFundraiserId(UUID fundraiserId);

  boolean existsByPaymentIntentId(UUID paymentIntentId);
}
