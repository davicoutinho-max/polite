package dev.civicpulse.legislative.adapter.in.web.dto;

import dev.civicpulse.legislative.application.TransparencyView;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record TransparencyResponse(
    UUID politicianAccountId,
    long totalExpenseCents,
    LocalDate lastUpdate,
    List<TransparencyMetricResponse> metrics,
    List<ExpenseLineResponse> expenseLines) {

  public static TransparencyResponse from(TransparencyView view) {
    return new TransparencyResponse(
        view.report().politicianAccountId(),
        view.report().totalExpenseCents(),
        view.report().lastUpdate(),
        view.metrics().stream().map(TransparencyMetricResponse::from).toList(),
        view.expenseLines().stream().map(ExpenseLineResponse::from).toList());
  }
}
