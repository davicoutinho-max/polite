package dev.civicpulse.platformconfig.adapter.in.web;

import dev.civicpulse.platformconfig.adapter.in.web.dto.AddLanguageRequest;
import dev.civicpulse.platformconfig.adapter.in.web.dto.LanguageResponse;
import dev.civicpulse.platformconfig.application.port.in.ManageLanguageUseCase;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/languages")
public class LanguageController {

  private final ManageLanguageUseCase manageLanguageUseCase;

  public LanguageController(ManageLanguageUseCase manageLanguageUseCase) {
    this.manageLanguageUseCase = manageLanguageUseCase;
  }

  @PostMapping
  public LanguageResponse add(@Valid @RequestBody AddLanguageRequest request) {
    return LanguageResponse.from(manageLanguageUseCase.addLanguage(request.id(), request.name(), request.code(), request.isDefault()));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> remove(@PathVariable String id) {
    manageLanguageUseCase.removeLanguage(id);
    return ResponseEntity.noContent().build();
  }

  @PutMapping("/{id}/default")
  public LanguageResponse setDefault(@PathVariable String id) {
    return LanguageResponse.from(manageLanguageUseCase.setDefaultLanguage(id));
  }

  @GetMapping
  public List<LanguageResponse> list() {
    return manageLanguageUseCase.listLanguages().stream().map(LanguageResponse::from).toList();
  }
}
