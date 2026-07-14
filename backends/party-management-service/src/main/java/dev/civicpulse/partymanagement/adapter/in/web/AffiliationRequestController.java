package dev.civicpulse.partymanagement.adapter.in.web;

import dev.civicpulse.partymanagement.adapter.in.web.dto.AffiliationRequestResponse;
import dev.civicpulse.partymanagement.application.port.in.ReviewAffiliationRequestUseCase;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AffiliationRequestController {

  private final ReviewAffiliationRequestUseCase reviewAffiliationRequestUseCase;

  public AffiliationRequestController(ReviewAffiliationRequestUseCase reviewAffiliationRequestUseCase) {
    this.reviewAffiliationRequestUseCase = reviewAffiliationRequestUseCase;
  }

  @GetMapping("/parties/{partyId}/affiliation-requests")
  public List<AffiliationRequestResponse> listPending(@PathVariable UUID partyId) {
    return reviewAffiliationRequestUseCase.listPending(partyId).stream().map(AffiliationRequestResponse::from).toList();
  }

  @PostMapping("/affiliation-requests/{requestId}/approve")
  public AffiliationRequestResponse approve(@PathVariable UUID requestId) {
    return AffiliationRequestResponse.from(reviewAffiliationRequestUseCase.approve(requestId));
  }

  @PostMapping("/affiliation-requests/{requestId}/reject")
  public AffiliationRequestResponse reject(@PathVariable UUID requestId) {
    return AffiliationRequestResponse.from(reviewAffiliationRequestUseCase.reject(requestId));
  }
}
