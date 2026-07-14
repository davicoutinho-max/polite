package dev.civicpulse.platformconfig.adapter.in.web;

import dev.civicpulse.platformconfig.adapter.in.web.dto.AddCountryRequest;
import dev.civicpulse.platformconfig.adapter.in.web.dto.AddStateRequest;
import dev.civicpulse.platformconfig.adapter.in.web.dto.CountryResponse;
import dev.civicpulse.platformconfig.adapter.in.web.dto.StateResponse;
import dev.civicpulse.platformconfig.application.port.in.ManageGeographyUseCase;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GeographyController {

  private final ManageGeographyUseCase manageGeographyUseCase;

  public GeographyController(ManageGeographyUseCase manageGeographyUseCase) {
    this.manageGeographyUseCase = manageGeographyUseCase;
  }

  @PostMapping("/countries")
  public CountryResponse addCountry(@Valid @RequestBody AddCountryRequest request) {
    return CountryResponse.from(manageGeographyUseCase.addCountry(request.name(), request.code()));
  }

  @DeleteMapping("/countries/{id}")
  public ResponseEntity<Void> removeCountry(@PathVariable UUID id) {
    manageGeographyUseCase.removeCountry(id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/countries")
  public List<CountryResponse> listCountries() {
    return manageGeographyUseCase.listCountries().stream().map(CountryResponse::from).toList();
  }

  @PostMapping("/countries/{countryId}/states")
  public StateResponse addState(@PathVariable UUID countryId, @Valid @RequestBody AddStateRequest request) {
    return StateResponse.from(manageGeographyUseCase.addState(countryId, request.name(), request.code()));
  }

  @GetMapping("/countries/{countryId}/states")
  public List<StateResponse> listStates(@PathVariable UUID countryId) {
    return manageGeographyUseCase.listStates(countryId).stream().map(StateResponse::from).toList();
  }
}
