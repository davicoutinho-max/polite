package dev.civicpulse.elections.application.port.out;

import dev.civicpulse.elections.domain.model.Election;
import dev.civicpulse.elections.domain.model.ElectionScope;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ElectionRepository {

  Election save(Election election);

  Optional<Election> findById(UUID id);

  List<Election> findAll(int page, int pageSize);

  List<Election> findByScope(ElectionScope scope, int page, int pageSize);

  List<Election> findUpcoming(LocalDate from, int page, int pageSize);
}
