package dev.civicpulse.legislative.application;

import dev.civicpulse.legislative.application.port.in.LegislativeItemUseCase;
import dev.civicpulse.legislative.application.port.out.LegislativeEventPublisher;
import dev.civicpulse.legislative.application.port.out.LegislativeItemRepository;
import dev.civicpulse.legislative.domain.event.LegislativeItemFiled;
import dev.civicpulse.legislative.domain.event.LegislativeItemStatusChanged;
import dev.civicpulse.legislative.domain.exception.LegislativeItemNotFoundException;
import dev.civicpulse.legislative.domain.model.LegislativeItem;
import dev.civicpulse.legislative.domain.model.LegislativeItemCategory;
import dev.civicpulse.legislative.domain.model.LegislativeItemStatus;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LegislativeItemService implements LegislativeItemUseCase {

  private final LegislativeItemRepository itemRepository;
  private final LegislativeEventPublisher eventPublisher;
  private final Clock clock;

  public LegislativeItemService(LegislativeItemRepository itemRepository, LegislativeEventPublisher eventPublisher, Clock clock) {
    this.itemRepository = itemRepository;
    this.eventPublisher = eventPublisher;
    this.clock = clock;
  }

  @Override
  @Transactional
  public LegislativeItem fileItem(
      UUID politicianAccountId,
      String reference,
      String title,
      String summary,
      LegislativeItemCategory category,
      LocalDate itemDate,
      Set<UUID> cosponsorAccountIds) {
    Instant now = clock.instant();
    LegislativeItem item = LegislativeItem.file(politicianAccountId, reference, title, summary, category, itemDate, cosponsorAccountIds, now);
    LegislativeItem saved = itemRepository.save(item);
    eventPublisher.publish(new LegislativeItemFiled(saved.id().orElseThrow(), politicianAccountId, category.code(), reference, now));
    return saved;
  }

  @Override
  @Transactional
  public LegislativeItem advanceStatus(UUID legislativeItemId, LegislativeItemStatus target) {
    LegislativeItem item = getItem(legislativeItemId);
    item.advanceStatus(target);
    LegislativeItem saved = itemRepository.save(item);
    eventPublisher.publish(
        new LegislativeItemStatusChanged(legislativeItemId, item.politicianAccountId(), target.code(), clock.instant()));
    return saved;
  }

  @Override
  public LegislativeItem getItem(UUID id) {
    return itemRepository.findById(id).orElseThrow(() -> new LegislativeItemNotFoundException(id));
  }

  @Override
  public List<LegislativeItem> getItemsByPolitician(UUID politicianAccountId) {
    return itemRepository.findByPolitician(politicianAccountId);
  }

  @Override
  public List<LegislativeItem> getRecentItems(int limit) {
    return itemRepository.findRecent(limit);
  }
}
