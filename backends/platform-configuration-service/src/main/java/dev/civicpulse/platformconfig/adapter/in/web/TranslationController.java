package dev.civicpulse.platformconfig.adapter.in.web;

import dev.civicpulse.platformconfig.adapter.in.web.dto.SetTranslationRequest;
import dev.civicpulse.platformconfig.adapter.in.web.dto.TranslationKeyResponse;
import dev.civicpulse.platformconfig.adapter.in.web.dto.TranslationValueResponse;
import dev.civicpulse.platformconfig.application.port.in.ManageTranslationUseCase;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TranslationController {

  private final ManageTranslationUseCase manageTranslationUseCase;

  public TranslationController(ManageTranslationUseCase manageTranslationUseCase) {
    this.manageTranslationUseCase = manageTranslationUseCase;
  }

  @PutMapping("/translations/{key}/{languageId}")
  public TranslationValueResponse set(
      @PathVariable String key, @PathVariable String languageId, @Valid @RequestBody SetTranslationRequest request) {
    return TranslationValueResponse.from(manageTranslationUseCase.setTranslation(key, languageId, request.value()));
  }

  @GetMapping("/translations")
  public List<TranslationValueResponse> getForLanguage(@RequestParam String languageId) {
    return manageTranslationUseCase.getTranslationsForLanguage(languageId).stream().map(TranslationValueResponse::from).toList();
  }

  @GetMapping("/translation-keys")
  public List<TranslationKeyResponse> listKeys() {
    return manageTranslationUseCase.listKeys().stream().map(TranslationKeyResponse::from).toList();
  }

  @DeleteMapping("/translation-keys/{id}")
  public ResponseEntity<Void> deleteKey(@PathVariable UUID id) {
    manageTranslationUseCase.deleteKey(id);
    return ResponseEntity.noContent().build();
  }
}
