package dev.civicpulse.legislative.adapter.in.web;

import dev.civicpulse.legislative.adapter.in.web.dto.AddExpenseLineRequest;
import dev.civicpulse.legislative.adapter.in.web.dto.AddMetricRequest;
import dev.civicpulse.legislative.adapter.in.web.dto.TransparencyMetricResponse;
import dev.civicpulse.legislative.adapter.in.web.dto.TransparencyResponse;
import dev.civicpulse.legislative.application.port.in.TransparencyUseCase;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/politicians/{politicianAccountId}/transparency")
public class TransparencyController {

  private final TransparencyUseCase transparencyUseCase;

  public TransparencyController(TransparencyUseCase transparencyUseCase) {
    this.transparencyUseCase = transparencyUseCase;
  }

  @GetMapping
  public TransparencyResponse get(@PathVariable UUID politicianAccountId) {
    return TransparencyResponse.from(transparencyUseCase.getTransparency(politicianAccountId));
  }

  @PostMapping("/metrics")
  @ResponseStatus(HttpStatus.CREATED)
  public TransparencyMetricResponse addMetric(@PathVariable UUID politicianAccountId, @Valid @RequestBody AddMetricRequest request) {
    return TransparencyMetricResponse.from(
        transparencyUseCase.addMetric(
            politicianAccountId, request.icon(), request.label(), request.valueCents(), request.caption(), request.period()));
  }

  @PostMapping("/expense-lines")
  @ResponseStatus(HttpStatus.CREATED)
  public TransparencyResponse addExpenseLine(@PathVariable UUID politicianAccountId, @Valid @RequestBody AddExpenseLineRequest request) {
    transparencyUseCase.addExpenseLine(politicianAccountId, request.category(), request.amountCents());
    return TransparencyResponse.from(transparencyUseCase.getTransparency(politicianAccountId));
  }
}
