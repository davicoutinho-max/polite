package dev.civicpulse.participation.adapter.in.web;

import dev.civicpulse.participation.adapter.in.web.dto.CreatePetitionRequest;
import dev.civicpulse.participation.adapter.in.web.dto.PetitionResponse;
import dev.civicpulse.participation.adapter.in.web.dto.SignPetitionRequest;
import dev.civicpulse.participation.application.port.in.GetPetitionUseCase;
import dev.civicpulse.participation.application.port.in.ManagePetitionUseCase;
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
@RequestMapping("/petitions")
public class PetitionController {

  private final ManagePetitionUseCase managePetitionUseCase;
  private final GetPetitionUseCase getPetitionUseCase;

  public PetitionController(ManagePetitionUseCase managePetitionUseCase, GetPetitionUseCase getPetitionUseCase) {
    this.managePetitionUseCase = managePetitionUseCase;
    this.getPetitionUseCase = getPetitionUseCase;
  }

  @PostMapping
  public ResponseEntity<PetitionResponse> create(@Valid @RequestBody CreatePetitionRequest request) {
    var petition = managePetitionUseCase.create(request.title(), request.summary(), request.category(), request.goal(), request.deadline());
    PetitionResponse body = PetitionResponse.from(petition);
    return ResponseEntity.created(URI.create("/petitions/" + body.id())).body(body);
  }

  @GetMapping("/{id}")
  public PetitionResponse getById(@PathVariable UUID id) {
    return PetitionResponse.from(getPetitionUseCase.getById(id));
  }

  @GetMapping
  public List<PetitionResponse> list(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int pageSize) {
    return getPetitionUseCase.list(page, pageSize).stream().map(PetitionResponse::from).toList();
  }

  @PostMapping("/{id}/signatures")
  public ResponseEntity<Void> sign(@PathVariable UUID id, @Valid @RequestBody SignPetitionRequest request) {
    managePetitionUseCase.sign(id, request.citizenAccountId());
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{id}/signatures/{citizenAccountId}")
  public boolean hasSigned(@PathVariable UUID id, @PathVariable UUID citizenAccountId) {
    return getPetitionUseCase.hasSigned(id, citizenAccountId);
  }
}
