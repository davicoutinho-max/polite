package dev.civicpulse.legislative.application.port.out;

import dev.civicpulse.legislative.domain.model.LegislativeItem;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LegislativeItemRepository {

  LegislativeItem save(LegislativeItem item);

  Optional<LegislativeItem> findById(UUID id);

  List<LegislativeItem> findByPolitician(UUID politicianAccountId);

  List<LegislativeItem> findRecent(int limit);

  void deleteById(UUID id);
}
