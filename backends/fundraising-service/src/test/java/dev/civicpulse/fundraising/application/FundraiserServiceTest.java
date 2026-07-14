package dev.civicpulse.fundraising.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.civicpulse.fundraising.application.port.out.EventPublisher;
import dev.civicpulse.fundraising.application.port.out.FundraiserRepository;
import dev.civicpulse.fundraising.domain.event.FundraiserCreated;
import dev.civicpulse.fundraising.domain.exception.FundraiserNotFoundException;
import dev.civicpulse.fundraising.domain.model.Fundraiser;
import dev.civicpulse.fundraising.domain.model.FundraiserCategory;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FundraiserServiceTest {

  private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

  @Mock private FundraiserRepository fundraiserRepository;
  @Mock private EventPublisher eventPublisher;

  private FundraiserService service;

  @BeforeEach
  void setUp() {
    service = new FundraiserService(fundraiserRepository, eventPublisher, Clock.fixed(NOW, ZoneOffset.UTC));
  }

  @Test
  void createSavesFundraiserAndPublishesEvent() {
    when(fundraiserRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    UUID organizerId = UUID.randomUUID();

    Fundraiser fundraiser = service.create(organizerId, "Help rebuild", "desc", FundraiserCategory.SOCIAL, 100_000, null, true);

    assertThat(fundraiser.organizerAccountId()).isEqualTo(organizerId);
    ArgumentCaptor<FundraiserCreated> captor = ArgumentCaptor.forClass(FundraiserCreated.class);
    verify(eventPublisher).publish(captor.capture());
    assertThat(captor.getValue().fundraiserId()).isEqualTo(fundraiser.id());
    assertThat(captor.getValue().goalCents()).isEqualTo(100_000);
  }

  @Test
  void getByIdThrowsWhenMissing() {
    UUID id = UUID.randomUUID();
    when(fundraiserRepository.findById(id)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.getById(id)).isInstanceOf(FundraiserNotFoundException.class);
  }

  @Test
  void listWithNullCategoryDelegatesToFindAll() {
    service.list(null, 0, 20);

    verify(fundraiserRepository).findAll(0, 20);
  }

  @Test
  void listWithCategoryDelegatesToFindByCategory() {
    service.list(FundraiserCategory.PARTY, 1, 10);

    verify(fundraiserRepository).findByCategory(FundraiserCategory.PARTY, 1, 10);
  }
}
