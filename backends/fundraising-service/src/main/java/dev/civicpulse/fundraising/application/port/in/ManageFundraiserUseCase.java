package dev.civicpulse.fundraising.application.port.in;

import dev.civicpulse.fundraising.domain.model.Fundraiser;
import dev.civicpulse.fundraising.domain.model.FundraiserCategory;
import java.time.LocalDate;
import java.util.UUID;

public interface ManageFundraiserUseCase {

  Fundraiser create(
      UUID organizerAccountId, String title, String description, FundraiserCategory category, long goalCents, LocalDate deadline, boolean ledgerPublic);
}
