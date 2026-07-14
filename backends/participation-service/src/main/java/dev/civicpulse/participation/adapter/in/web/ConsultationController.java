package dev.civicpulse.participation.adapter.in.web;

import dev.civicpulse.participation.adapter.in.web.dto.ConsultationResponseDto;
import dev.civicpulse.participation.adapter.in.web.dto.CreateConsultationRequest;
import dev.civicpulse.participation.adapter.in.web.dto.RespondToConsultationRequest;
import dev.civicpulse.participation.application.port.in.GetConsultationUseCase;
import dev.civicpulse.participation.application.port.in.ManageConsultationUseCase;
import dev.civicpulse.participation.domain.model.ConsultationStance;
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
@RequestMapping("/consultations")
public class ConsultationController {

  private final ManageConsultationUseCase manageConsultationUseCase;
  private final GetConsultationUseCase getConsultationUseCase;

  public ConsultationController(ManageConsultationUseCase manageConsultationUseCase, GetConsultationUseCase getConsultationUseCase) {
    this.manageConsultationUseCase = manageConsultationUseCase;
    this.getConsultationUseCase = getConsultationUseCase;
  }

  @PostMapping
  public ResponseEntity<ConsultationResponseDto> create(@Valid @RequestBody CreateConsultationRequest request) {
    var consultation = manageConsultationUseCase.create(request.title(), request.description(), request.deadline());
    ConsultationResponseDto body = ConsultationResponseDto.from(consultation);
    return ResponseEntity.created(URI.create("/consultations/" + body.id())).body(body);
  }

  @GetMapping("/{id}")
  public ConsultationResponseDto getById(@PathVariable UUID id) {
    return ConsultationResponseDto.from(getConsultationUseCase.getById(id));
  }

  @GetMapping
  public List<ConsultationResponseDto> list(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int pageSize) {
    return getConsultationUseCase.list(page, pageSize).stream().map(ConsultationResponseDto::from).toList();
  }

  @PostMapping("/{id}/responses")
  public ResponseEntity<Void> respond(@PathVariable UUID id, @Valid @RequestBody RespondToConsultationRequest request) {
    manageConsultationUseCase.respond(id, request.citizenAccountId(), ConsultationStance.fromCode(request.stance()));
    return ResponseEntity.noContent().build();
  }
}
