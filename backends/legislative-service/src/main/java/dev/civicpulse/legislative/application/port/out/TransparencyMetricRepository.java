package dev.civicpulse.legislative.application.port.out;

import dev.civicpulse.legislative.domain.model.TransparencyMetric;
import java.util.List;
import java.util.UUID;

public interface TransparencyMetricRepository {

  TransparencyMetric save(TransparencyMetric metric);

  List<TransparencyMetric> findByPolitician(UUID politicianAccountId);
}
