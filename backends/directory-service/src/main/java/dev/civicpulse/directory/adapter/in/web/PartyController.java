package dev.civicpulse.directory.adapter.in.web;

import dev.civicpulse.directory.adapter.in.web.dto.PartyResponse;
import dev.civicpulse.directory.application.port.in.SearchDirectoryUseCase;
import dev.civicpulse.directory.domain.model.PartySpectrum;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/parties")
public class PartyController {

  private final SearchDirectoryUseCase searchDirectoryUseCase;

  public PartyController(SearchDirectoryUseCase searchDirectoryUseCase) {
    this.searchDirectoryUseCase = searchDirectoryUseCase;
  }

  @GetMapping("/{id}")
  public PartyResponse getById(@PathVariable UUID id) {
    return PartyResponse.from(searchDirectoryUseCase.getParty(id));
  }

  @GetMapping
  public List<PartyResponse> search(
      @RequestParam(required = false) String spectrum,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int pageSize) {
    PartySpectrum partySpectrum = spectrum == null ? null : PartySpectrum.fromCode(spectrum);
    return searchDirectoryUseCase.searchParties(partySpectrum, page, pageSize).stream().map(PartyResponse::from).toList();
  }
}
