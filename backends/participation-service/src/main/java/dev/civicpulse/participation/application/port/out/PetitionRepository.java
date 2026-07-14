package dev.civicpulse.participation.application.port.out;

import dev.civicpulse.participation.domain.model.Petition;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PetitionRepository {

  Petition save(Petition petition);

  Optional<Petition> findById(UUID id);

  List<Petition> findAll(int page, int pageSize);
}
