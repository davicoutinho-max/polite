package dev.civicpulse.elections.adapter.in.web;

import dev.civicpulse.elections.adapter.in.web.dto.PersonalVoteResponse;
import dev.civicpulse.elections.adapter.in.web.dto.RegisterPersonalVoteRequest;
import dev.civicpulse.elections.application.port.in.PersonalVoteUseCase;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** A citizen's own personal/unofficial vote record for an election — see PersonalVote's javadoc
 * for why this is never the real ballot. Requires the caller to be authenticated: the gateway
 * injects {@code X-Account-Id} for any request bearing a valid token (see gateway-service's
 * JwtAuthenticationFilter), same convention as directory-service's FollowController. */
@RestController
@RequestMapping("/elections/{electionId}/my-votes")
public class PersonalVoteController {

  private final PersonalVoteUseCase personalVoteUseCase;

  public PersonalVoteController(PersonalVoteUseCase personalVoteUseCase) {
    this.personalVoteUseCase = personalVoteUseCase;
  }

  @PostMapping
  public PersonalVoteResponse register(
      @PathVariable UUID electionId, @RequestHeader("X-Account-Id") UUID citizenAccountId, @Valid @RequestBody RegisterPersonalVoteRequest request) {
    return PersonalVoteResponse.from(
        personalVoteUseCase.registerVote(
            citizenAccountId, electionId, request.office(), request.candidateName(), request.candidatePartyAcronym(), request.politicianAccountId()));
  }

  @GetMapping
  public List<PersonalVoteResponse> list(@PathVariable UUID electionId, @RequestHeader("X-Account-Id") UUID citizenAccountId) {
    return personalVoteUseCase.listVotes(citizenAccountId, electionId).stream().map(PersonalVoteResponse::from).toList();
  }
}
