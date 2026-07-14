package dev.civicpulse.legislative.application.port.out;

import dev.civicpulse.legislative.domain.model.TransparencyReport;
import java.util.Optional;
import java.util.UUID;

public interface TransparencyReportRepository {

  TransparencyReport save(TransparencyReport report);

  Optional<TransparencyReport> findById(UUID politicianAccountId);
}
