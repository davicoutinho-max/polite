package dev.civicpulse.legislative.application;

import dev.civicpulse.legislative.domain.model.ExpenseLine;
import dev.civicpulse.legislative.domain.model.TransparencyMetric;
import dev.civicpulse.legislative.domain.model.TransparencyReport;
import java.util.List;

/** Read-model combining {@link TransparencyReport}, its {@link TransparencyMetric}s, and its
 * {@link ExpenseLine}s with each line's share (% of total) computed here rather than stored — see
 * expense_lines' table comment in schema.sql. */
public record TransparencyView(
    TransparencyReport report, List<TransparencyMetric> metrics, List<ExpenseLineShare> expenseLines) {

  public record ExpenseLineShare(ExpenseLine line, double sharePercent) {}

  public static TransparencyView of(TransparencyReport report, List<TransparencyMetric> metrics, List<ExpenseLine> lines) {
    List<ExpenseLineShare> shares = lines.stream()
        .map(line -> new ExpenseLineShare(line, line.shareOf(report.totalExpenseCents())))
        .toList();
    return new TransparencyView(report, metrics, shares);
  }
}
