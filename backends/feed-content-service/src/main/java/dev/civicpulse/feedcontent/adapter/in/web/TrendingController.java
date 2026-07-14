package dev.civicpulse.feedcontent.adapter.in.web;

import dev.civicpulse.feedcontent.adapter.in.web.dto.TrendingTopicResponse;
import dev.civicpulse.feedcontent.application.port.in.GetTrendingTopicsUseCase;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/trending")
public class TrendingController {

  private final GetTrendingTopicsUseCase getTrendingTopicsUseCase;

  public TrendingController(GetTrendingTopicsUseCase getTrendingTopicsUseCase) {
    this.getTrendingTopicsUseCase = getTrendingTopicsUseCase;
  }

  @GetMapping
  public List<TrendingTopicResponse> list(@RequestParam(defaultValue = "10") int limit) {
    return getTrendingTopicsUseCase.getTrending(limit).stream().map(TrendingTopicResponse::from).toList();
  }
}
