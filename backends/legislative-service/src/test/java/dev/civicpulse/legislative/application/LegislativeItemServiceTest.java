package dev.civicpulse.legislative.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.civicpulse.legislative.application.port.out.LegislativeEventPublisher;
import dev.civicpulse.legislative.application.port.out.LegislativeItemRepository;
import dev.civicpulse.legislative.domain.event.DomainEvent;
import dev.civicpulse.legislative.domain.event.LegislativeItemFiled;
import dev.civicpulse.legislative.domain.event.LegislativeItemStatusChanged;
import dev.civicpulse.legislative.domain.model.LegislativeItem;
import dev.civicpulse.legislative.domain.model.LegislativeItemCategory;
import dev.civicpulse.legislative.domain.model.LegislativeItemStatus;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LegislativeItemServiceTest {

  private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

  @Mock private LegislativeItemRepository itemRepository;
  @Mock private LegislativeEventPublisher eventPublisher;

  private LegislativeItemService service;

  @BeforeEach
  void setUp() {
    service = new LegislativeItemService(itemRepository, eventPublisher, Clock.fixed(NOW, ZoneOffset.UTC));
  }

  @Test
  void filingAnItemPublishesLegislativeItemFiled() {
    UUID politicianAccountId = UUID.randomUUID();
    UUID itemId = UUID.randomUUID();
    when(itemRepository.save(any(LegislativeItem.class)))
        .thenAnswer(
            invocation -> {
              LegislativeItem arg = invocation.getArgument(0);
              return LegislativeItem.reconstitute(
                  itemId,
                  arg.politicianAccountId(),
                  arg.reference(),
                  arg.title(),
                  arg.summary().orElse(null),
                  arg.category(),
                  arg.status(),
                  arg.itemDate(),
                  arg.cosponsorAccountIds(),
                  arg.createdAt());
            });

    LegislativeItem result =
        service.fileItem(politicianAccountId, "PL 1/2026", "Title", "Summary", LegislativeItemCategory.PROJECT, LocalDate.now(), Set.of());

    assertThat(result.id()).contains(itemId);

    ArgumentCaptor<DomainEvent> captor = ArgumentCaptor.forClass(DomainEvent.class);
    verify(eventPublisher).publish(captor.capture());
    assertThat(captor.getValue()).isInstanceOf(LegislativeItemFiled.class);
  }

  @Test
  void advancingStatusPublishesLegislativeItemStatusChanged() {
    UUID politicianAccountId = UUID.randomUUID();
    UUID itemId = UUID.randomUUID();
    LegislativeItem existing =
        LegislativeItem.reconstitute(
            itemId, politicianAccountId, "PL 1/2026", "Title", null, LegislativeItemCategory.PROJECT, LegislativeItemStatus.FILED, LocalDate.now(), Set.of(), NOW);
    when(itemRepository.findById(itemId)).thenReturn(java.util.Optional.of(existing));
    when(itemRepository.save(any(LegislativeItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

    LegislativeItem result = service.advanceStatus(itemId, LegislativeItemStatus.IN_COMMITTEE);

    assertThat(result.status()).isEqualTo(LegislativeItemStatus.IN_COMMITTEE);
    ArgumentCaptor<DomainEvent> captor = ArgumentCaptor.forClass(DomainEvent.class);
    verify(eventPublisher).publish(captor.capture());
    assertThat(captor.getValue()).isInstanceOf(LegislativeItemStatusChanged.class);
  }
}
