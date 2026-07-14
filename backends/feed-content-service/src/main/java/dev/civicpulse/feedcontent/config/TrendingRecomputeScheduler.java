package dev.civicpulse.feedcontent.config;

import dev.civicpulse.feedcontent.application.port.in.RecomputeTrendingTopicsUseCase;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
class TrendingRecomputeScheduler {

  private final RecomputeTrendingTopicsUseCase recomputeTrendingTopicsUseCase;

  TrendingRecomputeScheduler(RecomputeTrendingTopicsUseCase recomputeTrendingTopicsUseCase) {
    this.recomputeTrendingTopicsUseCase = recomputeTrendingTopicsUseCase;
  }

  @Scheduled(fixedRate = 15, initialDelay = 1, timeUnit = java.util.concurrent.TimeUnit.MINUTES)
  void recomputeTrendingTopics() {
    recomputeTrendingTopicsUseCase.recompute();
  }
}
