package dev.civicpulse.fundraising.adapter.in.web;

import dev.civicpulse.fundraising.adapter.in.web.dto.ContributionResponse;
import dev.civicpulse.fundraising.application.port.in.ManageContributionUseCase;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Contributions are only ever created reactively, upon consuming payments-service's {@code
 * PaymentCaptured} (see FundraisingProjectionListener) — there is no direct "create contribution"
 * endpoint, since a contribution without a captured payment behind it would be exactly the drift
 * this schema is designed to prevent. */
@RestController
@RequestMapping("/fundraisers/{fundraiserId}/contributions")
public class ContributionController {

  private final ManageContributionUseCase manageContributionUseCase;

  public ContributionController(ManageContributionUseCase manageContributionUseCase) {
    this.manageContributionUseCase = manageContributionUseCase;
  }

  @GetMapping
  public List<ContributionResponse> list(@PathVariable UUID fundraiserId) {
    return manageContributionUseCase.listByFundraiser(fundraiserId).stream().map(ContributionResponse::from).toList();
  }
}
