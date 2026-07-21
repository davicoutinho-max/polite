package dev.civicpulse.platformconfig.application.port.in;

import dev.civicpulse.platformconfig.domain.model.PoliticalPosition;
import java.util.List;
import java.util.UUID;

public interface ManagePoliticalPositionUseCase {

  PoliticalPosition addPosition(String name);

  void removePosition(UUID id);

  List<PoliticalPosition> listPositions();
}
