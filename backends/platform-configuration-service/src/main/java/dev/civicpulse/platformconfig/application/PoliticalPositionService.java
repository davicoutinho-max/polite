package dev.civicpulse.platformconfig.application;

import dev.civicpulse.platformconfig.application.port.in.ManagePoliticalPositionUseCase;
import dev.civicpulse.platformconfig.application.port.out.PoliticalPositionRepository;
import dev.civicpulse.platformconfig.domain.model.PoliticalPosition;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PoliticalPositionService implements ManagePoliticalPositionUseCase {

  private final PoliticalPositionRepository politicalPositionRepository;

  public PoliticalPositionService(PoliticalPositionRepository politicalPositionRepository) {
    this.politicalPositionRepository = politicalPositionRepository;
  }

  @Override
  @Transactional
  public PoliticalPosition addPosition(String name) {
    int nextSortOrder =
        politicalPositionRepository.findAllOrderBySortOrder().stream().mapToInt(PoliticalPosition::sortOrder).max().orElse(0) + 1;
    return politicalPositionRepository.save(PoliticalPosition.create(UUID.randomUUID(), name, nextSortOrder));
  }

  @Override
  @Transactional
  public void removePosition(UUID id) {
    politicalPositionRepository.delete(id);
  }

  @Override
  @Transactional(readOnly = true)
  public List<PoliticalPosition> listPositions() {
    return politicalPositionRepository.findAllOrderBySortOrder();
  }
}
