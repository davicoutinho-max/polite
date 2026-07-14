package dev.civicpulse.legislative.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import dev.civicpulse.legislative.application.port.out.ExpenseLineRepository;
import dev.civicpulse.legislative.application.port.out.TransparencyMetricRepository;
import dev.civicpulse.legislative.application.port.out.TransparencyReportRepository;
import dev.civicpulse.legislative.domain.model.ExpenseLine;
import dev.civicpulse.legislative.domain.model.TransparencyReport;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TransparencyServiceTest {

  private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

  @Mock private TransparencyReportRepository reportRepository;
  @Mock private TransparencyMetricRepository metricRepository;
  @Mock private ExpenseLineRepository expenseLineRepository;

  private TransparencyService service;

  @BeforeEach
  void setUp() {
    service = new TransparencyService(reportRepository, metricRepository, expenseLineRepository, Clock.fixed(NOW, ZoneOffset.UTC));
  }

  @Test
  void addingAnExpenseLineRecomputesTheReportTotal() {
    UUID politicianAccountId = UUID.randomUUID();
    when(expenseLineRepository.save(any(ExpenseLine.class))).thenAnswer(invocation -> invocation.getArgument(0));
    when(expenseLineRepository.sumAmountCentsByPolitician(politicianAccountId)).thenReturn(15_000L);
    when(reportRepository.findById(politicianAccountId)).thenReturn(Optional.empty());
    when(reportRepository.save(any(TransparencyReport.class))).thenAnswer(invocation -> invocation.getArgument(0));

    service.addExpenseLine(politicianAccountId, "Travel", 15_000L);

    ArgumentCaptor<TransparencyReport> captor = ArgumentCaptor.forClass(TransparencyReport.class);
    org.mockito.Mockito.verify(reportRepository).save(captor.capture());
    assertThat(captor.getValue().totalExpenseCents()).isEqualTo(15_000L);
    assertThat(captor.getValue().lastUpdate()).isEqualTo(LocalDate.now(Clock.fixed(NOW, ZoneOffset.UTC)));
  }
}
