package dev.civicpulse.participation.application.port.in;

import dev.civicpulse.participation.domain.model.Petition;
import java.util.List;
import java.util.UUID;

public interface GetPetitionUseCase {

  Petition getById(UUID id);

  List<Petition> list(int page, int pageSize);
}
