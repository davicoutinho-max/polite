package dev.civicpulse.platformconfig.application.port.out;

import dev.civicpulse.platformconfig.domain.model.PoliticalPosition;
import java.util.List;
import java.util.UUID;

public interface PoliticalPositionRepository {

  PoliticalPosition save(PoliticalPosition position);

  void delete(UUID id);

  List<PoliticalPosition> findAllOrderBySortOrder();
}
