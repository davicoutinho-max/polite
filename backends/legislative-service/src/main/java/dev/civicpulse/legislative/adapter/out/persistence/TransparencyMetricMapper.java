package dev.civicpulse.legislative.adapter.out.persistence;

import dev.civicpulse.legislative.domain.model.TransparencyMetric;
import org.springframework.stereotype.Component;

@Component
class TransparencyMetricMapper {

  TransparencyMetric toDomain(TransparencyMetricJpaEntity entity) {
    return TransparencyMetric.reconstitute(
        entity.getId(), entity.getPoliticianAccountId(), entity.getIcon(), entity.getLabel(), entity.getValueCents(), entity.getCaption(), entity.getPeriod());
  }

  TransparencyMetricJpaEntity toEntity(TransparencyMetric metric) {
    return new TransparencyMetricJpaEntity(
        metric.id().orElse(null),
        metric.politicianAccountId(),
        metric.icon().orElse(null),
        metric.label(),
        metric.valueCents(),
        metric.caption().orElse(null),
        metric.period());
  }
}
