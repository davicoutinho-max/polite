package dev.civicpulse.legislative.adapter.in.web.dto;

import dev.civicpulse.legislative.application.TransparencyView.ExpenseLineShare;
import java.util.UUID;

public record ExpenseLineResponse(UUID id, String category, long amountCents, double sharePercent) {

  public static ExpenseLineResponse from(ExpenseLineShare share) {
    return new ExpenseLineResponse(
        share.line().id().orElse(null), share.line().category(), share.line().amountCents(), share.sharePercent());
  }
}
