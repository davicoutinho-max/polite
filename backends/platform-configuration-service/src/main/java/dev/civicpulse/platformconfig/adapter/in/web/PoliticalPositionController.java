package dev.civicpulse.platformconfig.adapter.in.web;

import dev.civicpulse.platformconfig.adapter.in.web.dto.AddPoliticalPositionRequest;
import dev.civicpulse.platformconfig.adapter.in.web.dto.PoliticalPositionResponse;
import dev.civicpulse.platformconfig.application.port.in.ManagePoliticalPositionUseCase;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/political-positions")
public class PoliticalPositionController {

  private final ManagePoliticalPositionUseCase managePoliticalPositionUseCase;

  public PoliticalPositionController(ManagePoliticalPositionUseCase managePoliticalPositionUseCase) {
    this.managePoliticalPositionUseCase = managePoliticalPositionUseCase;
  }

  @GetMapping
  public List<PoliticalPositionResponse> list() {
    return managePoliticalPositionUseCase.listPositions().stream().map(PoliticalPositionResponse::from).toList();
  }

  @PostMapping
  public PoliticalPositionResponse add(@Valid @RequestBody AddPoliticalPositionRequest request) {
    return PoliticalPositionResponse.from(managePoliticalPositionUseCase.addPosition(request.name()));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> remove(@PathVariable UUID id) {
    managePoliticalPositionUseCase.removePosition(id);
    return ResponseEntity.noContent().build();
  }
}
