package dev.civicpulse.legislative.adapter.in.web.dto;

import dev.civicpulse.legislative.domain.model.TransparencyMetric;
import java.util.UUID;

public record TransparencyMetricResponse(UUID id, String icon, String label, long valueCents, String caption, String period) {

  public static TransparencyMetricResponse from(TransparencyMetric metric) {
    return new TransparencyMetricResponse(
        metric.id().orElse(null), metric.icon().orElse(null), metric.label(), metric.valueCents(), metric.caption().orElse(null), metric.period());
  }
}
