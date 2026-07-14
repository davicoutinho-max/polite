package dev.civicpulse.legislative.application.port.in;

import dev.civicpulse.legislative.domain.model.LegislativeItem;
import dev.civicpulse.legislative.domain.model.LegislativeItemCategory;
import dev.civicpulse.legislative.domain.model.LegislativeItemStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface LegislativeItemUseCase {

  LegislativeItem fileItem(
      UUID politicianAccountId,
      String reference,
      String title,
      String summary,
      LegislativeItemCategory category,
      LocalDate itemDate,
      Set<UUID> cosponsorAccountIds);

  LegislativeItem advanceStatus(UUID legislativeItemId, LegislativeItemStatus target);

  LegislativeItem getItem(UUID id);

  List<LegislativeItem> getItemsByPolitician(UUID politicianAccountId);

  List<LegislativeItem> getRecentItems(int limit);
}
