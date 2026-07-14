package dev.civicpulse.fundraising.application.port.in;

import dev.civicpulse.fundraising.domain.model.Fundraiser;
import dev.civicpulse.fundraising.domain.model.FundraiserCategory;
import java.util.List;
import java.util.UUID;

public interface GetFundraiserUseCase {

  Fundraiser getById(UUID id);

  List<Fundraiser> list(FundraiserCategory category, int page, int pageSize);
}
