package dev.civicpulse.analytics.application.port.in;

import dev.civicpulse.analytics.application.KpiSummary;
import dev.civicpulse.analytics.application.port.out.EngagementEventRepository.DailyCount;
import dev.civicpulse.analytics.application.port.out.EngagementEventRepository.TypeCount;
import java.util.List;
import java.util.UUID;

public interface GetAnalyticsUseCase {

  KpiSummary getKpis(UUID authorAccountId);

  List<DailyCount> getEngagementTrend(UUID authorAccountId, int lookbackDays);

  List<TypeCount> getByContentType(UUID authorAccountId);

  List<TypeCount> getByAccountType(UUID authorAccountId);
}
