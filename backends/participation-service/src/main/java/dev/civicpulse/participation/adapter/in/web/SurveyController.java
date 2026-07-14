package dev.civicpulse.participation.adapter.in.web;

import dev.civicpulse.participation.adapter.in.web.dto.CastVoteRequest;
import dev.civicpulse.participation.adapter.in.web.dto.CreateSurveyRequest;
import dev.civicpulse.participation.adapter.in.web.dto.SurveyOptionResponse;
import dev.civicpulse.participation.adapter.in.web.dto.SurveyResponse;
import dev.civicpulse.participation.application.port.in.GetSurveyUseCase;
import dev.civicpulse.participation.application.port.in.ManageSurveyUseCase;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/surveys")
public class SurveyController {

  private final ManageSurveyUseCase manageSurveyUseCase;
  private final GetSurveyUseCase getSurveyUseCase;

  public SurveyController(ManageSurveyUseCase manageSurveyUseCase, GetSurveyUseCase getSurveyUseCase) {
    this.manageSurveyUseCase = manageSurveyUseCase;
    this.getSurveyUseCase = getSurveyUseCase;
  }

  @PostMapping
  public ResponseEntity<SurveyResponse> create(@Valid @RequestBody CreateSurveyRequest request) {
    var survey = manageSurveyUseCase.create(request.question(), request.context(), request.options());
    SurveyResponse body = SurveyResponse.from(survey);
    return ResponseEntity.created(URI.create("/surveys/" + body.id())).body(body);
  }

  @GetMapping("/{id}")
  public SurveyResponse getById(@PathVariable UUID id) {
    return SurveyResponse.from(getSurveyUseCase.getById(id));
  }

  @GetMapping
  public List<SurveyResponse> list(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int pageSize) {
    return getSurveyUseCase.list(page, pageSize).stream().map(SurveyResponse::from).toList();
  }

  @GetMapping("/{id}/options")
  public List<SurveyOptionResponse> listOptions(@PathVariable UUID id) {
    return getSurveyUseCase.listOptions(id).stream().map(SurveyOptionResponse::from).toList();
  }

  @PostMapping("/{id}/votes")
  public ResponseEntity<Void> vote(@PathVariable UUID id, @Valid @RequestBody CastVoteRequest request) {
    manageSurveyUseCase.vote(id, request.citizenAccountId(), request.optionId());
    return ResponseEntity.noContent().build();
  }
}
