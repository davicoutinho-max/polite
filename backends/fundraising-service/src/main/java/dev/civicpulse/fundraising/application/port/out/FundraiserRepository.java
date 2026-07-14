package dev.civicpulse.fundraising.application.port.out;

import dev.civicpulse.fundraising.domain.model.Fundraiser;
import dev.civicpulse.fundraising.domain.model.FundraiserCategory;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FundraiserRepository {

  Fundraiser save(Fundraiser fundraiser);

  Optional<Fundraiser> findById(UUID id);

  List<Fundraiser> findAll(int page, int pageSize);

  List<Fundraiser> findByCategory(FundraiserCategory category, int page, int pageSize);
}
