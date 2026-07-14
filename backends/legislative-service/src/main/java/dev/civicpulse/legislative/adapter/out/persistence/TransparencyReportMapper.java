package dev.civicpulse.legislative.adapter.out.persistence;

import dev.civicpulse.legislative.domain.model.TransparencyReport;
import org.springframework.stereotype.Component;

@Component
class TransparencyReportMapper {

  TransparencyReport toDomain(TransparencyReportJpaEntity entity) {
    return TransparencyReport.reconstitute(entity.getPoliticianAccountId(), entity.getTotalExpenseCents(), entity.getLastUpdate());
  }

  TransparencyReportJpaEntity toEntity(TransparencyReport report) {
    return new TransparencyReportJpaEntity(report.politicianAccountId(), report.totalExpenseCents(), report.lastUpdate());
  }
}
