package dev.civicpulse.analytics.application;

/** {@code reach} is the count of distinct accounts that liked/commented/followed in-period — the
 * real computable proxy for "audience size" (no impressions/views concept exists in this
 * platform). {@code engagementRatePercent} = (likes+comments) * 100 / reach, computed here, never
 * stored. */
public record KpiSummary(long totalPosts, long totalLikes, long totalComments, long netFollows, long reach, double engagementRatePercent) {

  public static KpiSummary of(long totalPosts, long totalLikes, long totalComments, long netFollows, long reach) {
    double engagementRate = reach == 0 ? 0.0 : ((totalLikes + totalComments) * 100.0) / reach;
    return new KpiSummary(totalPosts, totalLikes, totalComments, netFollows, reach, engagementRate);
  }
}
