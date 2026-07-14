package dev.civicpulse.directory.adapter.in.web;

import dev.civicpulse.directory.adapter.in.web.dto.PoliticianResponse;
import dev.civicpulse.directory.application.port.in.SearchDirectoryUseCase;
import dev.civicpulse.directory.domain.model.GovLevel;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/politicians")
public class PoliticianController {

  private final SearchDirectoryUseCase searchDirectoryUseCase;

  public PoliticianController(SearchDirectoryUseCase searchDirectoryUseCase) {
    this.searchDirectoryUseCase = searchDirectoryUseCase;
  }

  @GetMapping("/{accountId}")
  public PoliticianResponse getById(@PathVariable UUID accountId) {
    return PoliticianResponse.from(searchDirectoryUseCase.getPolitician(accountId));
  }

  @GetMapping
  public List<PoliticianResponse> search(
      @RequestParam(required = false) String state,
      @RequestParam(required = false) String level,
      @RequestParam(required = false) UUID partyId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int pageSize) {
    GovLevel govLevel = level == null ? null : GovLevel.fromCode(level);
    return searchDirectoryUseCase.searchPoliticians(state, govLevel, partyId, page, pageSize).stream()
        .map(PoliticianResponse::from)
        .toList();
  }
}
