package dev.civicpulse.analytics.adapter.in.web.dto;

import dev.civicpulse.analytics.application.KpiSummary;

public record KpiSummaryResponse(long totalPosts, long totalLikes, long totalComments, long netFollows, long reach, double engagementRatePercent) {

  public static KpiSummaryResponse from(KpiSummary summary) {
    return new KpiSummaryResponse(
        summary.totalPosts(), summary.totalLikes(), summary.totalComments(), summary.netFollows(), summary.reach(), summary.engagementRatePercent());
  }
}
