package dev.civicpulse.legislative.adapter.in.web;

import dev.civicpulse.legislative.adapter.in.web.dto.CastVoteRequest;
import dev.civicpulse.legislative.adapter.in.web.dto.VoteRecordResponse;
import dev.civicpulse.legislative.application.port.in.VoteUseCase;
import dev.civicpulse.legislative.domain.model.VoteChoice;
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
@RequestMapping("/politicians/{politicianAccountId}/votes")
public class VoteController {

  private final VoteUseCase voteUseCase;

  public VoteController(VoteUseCase voteUseCase) {
    this.voteUseCase = voteUseCase;
  }

  @GetMapping
  public List<VoteRecordResponse> list(@PathVariable UUID politicianAccountId) {
    return voteUseCase.getVotes(politicianAccountId).stream().map(VoteRecordResponse::from).toList();
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public VoteRecordResponse cast(@PathVariable UUID politicianAccountId, @Valid @RequestBody CastVoteRequest request) {
    return VoteRecordResponse.from(
        voteUseCase.castVote(
            politicianAccountId, request.legislativeItemId(), request.matter(), request.voteDate(), VoteChoice.fromCode(request.choice())));
  }
}
