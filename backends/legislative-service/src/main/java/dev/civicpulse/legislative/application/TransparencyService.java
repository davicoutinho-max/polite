package dev.civicpulse.legislative.application;

import dev.civicpulse.legislative.application.port.in.TransparencyUseCase;
import dev.civicpulse.legislative.application.port.out.ExpenseLineRepository;
import dev.civicpulse.legislative.application.port.out.TransparencyMetricRepository;
import dev.civicpulse.legislative.application.port.out.TransparencyReportRepository;
import dev.civicpulse.legislative.domain.model.ExpenseLine;
import dev.civicpulse.legislative.domain.model.TransparencyMetric;
import dev.civicpulse.legislative.domain.model.TransparencyReport;
import java.time.Clock;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransparencyService implements TransparencyUseCase {

  private final TransparencyReportRepository reportRepository;
  private final TransparencyMetricRepository metricRepository;
  private final ExpenseLineRepository expenseLineRepository;
  private final Clock clock;

  public TransparencyService(
      TransparencyReportRepository reportRepository,
      TransparencyMetricRepository metricRepository,
      ExpenseLineRepository expenseLineRepository,
      Clock clock) {
    this.reportRepository = reportRepository;
    this.metricRepository = metricRepository;
    this.expenseLineRepository = expenseLineRepository;
    this.clock = clock;
  }

  @Override
  public TransparencyView getTransparency(UUID politicianAccountId) {
    TransparencyReport report = reportRepository
        .findById(politicianAccountId)
        .orElseGet(() -> TransparencyReport.initialize(politicianAccountId, LocalDate.now(clock)));
    return TransparencyView.of(report, metricRepository.findByPolitician(politicianAccountId), expenseLineRepository.findByPolitician(politicianAccountId));
  }

  @Override
  @Transactional
  public TransparencyMetric addMetric(UUID politicianAccountId, String icon, String label, long valueCents, String caption, String period) {
    return metricRepository.save(TransparencyMetric.add(politicianAccountId, icon, label, valueCents, caption, period));
  }

  @Override
  @Transactional
  public void addExpenseLine(UUID politicianAccountId, String category, long amountCents) {
    expenseLineRepository.save(ExpenseLine.record(politicianAccountId, category, amountCents));
    long total = expenseLineRepository.sumAmountCentsByPolitician(politicianAccountId);
    TransparencyReport report = reportRepository
        .findById(politicianAccountId)
        .orElseGet(() -> TransparencyReport.initialize(politicianAccountId, LocalDate.now(clock)));
    report.recomputeTotal(total, LocalDate.now(clock));
    reportRepository.save(report);
  }
}
