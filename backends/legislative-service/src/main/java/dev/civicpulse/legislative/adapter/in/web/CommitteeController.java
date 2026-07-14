package dev.civicpulse.legislative.adapter.in.web;

import dev.civicpulse.legislative.adapter.in.web.dto.CommitteeMembershipResponse;
import dev.civicpulse.legislative.adapter.in.web.dto.JoinCommitteeRequest;
import dev.civicpulse.legislative.application.port.in.CommitteeUseCase;
import dev.civicpulse.legislative.domain.model.CommitteeKind;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/politicians/{politicianAccountId}/committees")
public class CommitteeController {

  private final CommitteeUseCase committeeUseCase;

  public CommitteeController(CommitteeUseCase committeeUseCase) {
    this.committeeUseCase = committeeUseCase;
  }

  @GetMapping
  public List<CommitteeMembershipResponse> list(@PathVariable UUID politicianAccountId) {
    return committeeUseCase.getCommittees(politicianAccountId).stream().map(CommitteeMembershipResponse::from).toList();
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public CommitteeMembershipResponse join(@PathVariable UUID politicianAccountId, @Valid @RequestBody JoinCommitteeRequest request) {
    return CommitteeMembershipResponse.from(
        committeeUseCase.joinCommittee(politicianAccountId, request.name(), request.role(), CommitteeKind.fromCode(request.kind())));
  }
}
