package dev.civicpulse.analytics.adapter.in.web;

import dev.civicpulse.analytics.adapter.in.web.dto.DailyEngagementResponse;
import dev.civicpulse.analytics.adapter.in.web.dto.KpiSummaryResponse;
import dev.civicpulse.analytics.adapter.in.web.dto.TypeCountResponse;
import dev.civicpulse.analytics.application.port.in.GetAnalyticsUseCase;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/analytics/{authorAccountId}")
public class AnalyticsController {

  private final GetAnalyticsUseCase getAnalyticsUseCase;

  public AnalyticsController(GetAnalyticsUseCase getAnalyticsUseCase) {
    this.getAnalyticsUseCase = getAnalyticsUseCase;
  }

  @GetMapping("/kpis")
  public KpiSummaryResponse getKpis(@PathVariable UUID authorAccountId) {
    return KpiSummaryResponse.from(getAnalyticsUseCase.getKpis(authorAccountId));
  }

  @GetMapping("/engagement")
  public List<DailyEngagementResponse> getEngagement(@PathVariable UUID authorAccountId, @RequestParam(defaultValue = "30") int days) {
    return getAnalyticsUseCase.getEngagementTrend(authorAccountId, days).stream().map(DailyEngagementResponse::from).toList();
  }

  @GetMapping("/by-content-type")
  public List<TypeCountResponse> getByContentType(@PathVariable UUID authorAccountId) {
    return getAnalyticsUseCase.getByContentType(authorAccountId).stream().map(TypeCountResponse::from).toList();
  }

  @GetMapping("/by-account-type")
  public List<TypeCountResponse> getByAccountType(@PathVariable UUID authorAccountId) {
    return getAnalyticsUseCase.getByAccountType(authorAccountId).stream().map(TypeCountResponse::from).toList();
  }
}
