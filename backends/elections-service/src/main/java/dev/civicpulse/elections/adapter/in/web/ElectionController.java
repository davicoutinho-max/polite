package dev.civicpulse.elections.adapter.in.web;

import dev.civicpulse.elections.adapter.in.web.dto.CandidateResponse;
import dev.civicpulse.elections.adapter.in.web.dto.CreateElectionRequest;
import dev.civicpulse.elections.adapter.in.web.dto.ElectionResponse;
import dev.civicpulse.elections.adapter.in.web.dto.NominateCandidateRequest;
import dev.civicpulse.elections.application.port.in.GetElectionUseCase;
import dev.civicpulse.elections.application.port.in.ManageElectionUseCase;
import dev.civicpulse.elections.domain.model.Election;
import dev.civicpulse.elections.domain.model.ElectionScope;
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

/** No {@code X-Account-Id}/authorization checks in this controller — this is a public,
 * visitor-accessible read surface (see schema.sql's header); election creation/nomination are
 * administrative writes trusted to whatever gateway-level policy fronts this service, same as
 * every other admin-style endpoint elsewhere in this system. */
@RestController
@RequestMapping("/elections")
public class ElectionController {

  private final ManageElectionUseCase manageElectionUseCase;
  private final GetElectionUseCase getElectionUseCase;

  public ElectionController(ManageElectionUseCase manageElectionUseCase, GetElectionUseCase getElectionUseCase) {
    this.manageElectionUseCase = manageElectionUseCase;
    this.getElectionUseCase = getElectionUseCase;
  }

  @PostMapping
  public ResponseEntity<ElectionResponse> create(@Valid @RequestBody CreateElectionRequest request) {
    Election election =
        manageElectionUseCase.create(request.title(), ElectionScope.fromCode(request.scope()), request.electionDate(), request.description());
    ElectionResponse body = ElectionResponse.from(election);
    return ResponseEntity.created(URI.create("/elections/" + body.id())).body(body);
  }

  @GetMapping("/{id}")
  public ElectionResponse getById(@PathVariable UUID id) {
    return ElectionResponse.from(getElectionUseCase.getById(id));
  }

  @GetMapping
  public List<ElectionResponse> list(
      @RequestParam(required = false) String scope, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int pageSize) {
    ElectionScope parsedScope = scope == null ? null : ElectionScope.fromCode(scope);
    return getElectionUseCase.list(parsedScope, page, pageSize).stream().map(ElectionResponse::from).toList();
  }

  @GetMapping("/upcoming")
  public List<ElectionResponse> listUpcoming(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int pageSize) {
    return getElectionUseCase.listUpcoming(page, pageSize).stream().map(ElectionResponse::from).toList();
  }

  @PostMapping("/{id}/candidacies")
  public ResponseEntity<Void> nominateCandidate(@PathVariable UUID id, @Valid @RequestBody NominateCandidateRequest request) {
    manageElectionUseCase.nominateCandidate(id, request.politicianAccountId());
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{id}/candidacies")
  public List<CandidateResponse> listCandidates(@PathVariable UUID id) {
    return getElectionUseCase.listCandidates(id).stream().map(CandidateResponse::from).toList();
  }
}
