package dev.civicpulse.fundraising.application;

import dev.civicpulse.fundraising.application.port.in.GetFundraiserUseCase;
import dev.civicpulse.fundraising.application.port.in.ManageFundraiserUseCase;
import dev.civicpulse.fundraising.application.port.out.EventPublisher;
import dev.civicpulse.fundraising.application.port.out.FundraiserRepository;
import dev.civicpulse.fundraising.domain.event.FundraiserCreated;
import dev.civicpulse.fundraising.domain.exception.FundraiserNotFoundException;
import dev.civicpulse.fundraising.domain.model.Fundraiser;
import dev.civicpulse.fundraising.domain.model.FundraiserCategory;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FundraiserService implements ManageFundraiserUseCase, GetFundraiserUseCase {

  private final FundraiserRepository fundraiserRepository;
  private final EventPublisher eventPublisher;
  private final Clock clock;

  public FundraiserService(FundraiserRepository fundraiserRepository, EventPublisher eventPublisher, Clock clock) {
    this.fundraiserRepository = fundraiserRepository;
    this.eventPublisher = eventPublisher;
    this.clock = clock;
  }

  @Override
  @Transactional
  public Fundraiser create(
      UUID organizerAccountId, String title, String description, FundraiserCategory category, long goalCents, LocalDate deadline, boolean ledgerPublic) {
    Fundraiser fundraiser =
        Fundraiser.create(UUID.randomUUID(), organizerAccountId, title, description, category, goalCents, deadline, ledgerPublic, clock.instant());
    Fundraiser saved = fundraiserRepository.save(fundraiser);
    eventPublisher.publish(new FundraiserCreated(saved.id(), organizerAccountId, category.code(), goalCents, clock.instant()));
    return saved;
  }

  @Override
  @Transactional(readOnly = true)
  public Fundraiser getById(UUID id) {
    return fundraiserRepository.findById(id).orElseThrow(() -> new FundraiserNotFoundException(id));
  }

  @Override
  @Transactional(readOnly = true)
  public List<Fundraiser> list(FundraiserCategory category, int page, int pageSize) {
    return category == null ? fundraiserRepository.findAll(page, pageSize) : fundraiserRepository.findByCategory(category, page, pageSize);
  }
}
