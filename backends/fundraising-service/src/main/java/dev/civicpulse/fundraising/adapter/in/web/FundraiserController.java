package dev.civicpulse.fundraising.adapter.in.web;

import dev.civicpulse.fundraising.adapter.in.web.dto.CreateFundraiserRequest;
import dev.civicpulse.fundraising.adapter.in.web.dto.FundraiserResponse;
import dev.civicpulse.fundraising.application.port.in.GetFundraiserUseCase;
import dev.civicpulse.fundraising.application.port.in.ManageFundraiserUseCase;
import dev.civicpulse.fundraising.domain.model.Fundraiser;
import dev.civicpulse.fundraising.domain.model.FundraiserCategory;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fundraisers")
public class FundraiserController {

  private final ManageFundraiserUseCase manageFundraiserUseCase;
  private final GetFundraiserUseCase getFundraiserUseCase;

  public FundraiserController(ManageFundraiserUseCase manageFundraiserUseCase, GetFundraiserUseCase getFundraiserUseCase) {
    this.manageFundraiserUseCase = manageFundraiserUseCase;
    this.getFundraiserUseCase = getFundraiserUseCase;
  }

  /** {@code X-Account-Id} is the organizer's own id, forwarded by the Gateway after JWT
   * validation — see docs/architecture/system-architecture.html. Both politicians/parties and
   * citizens may organize a fundraiser; only Payments-confirmed contributions ever move money. */
  @PostMapping
  public ResponseEntity<FundraiserResponse> create(
      @RequestHeader("X-Account-Id") UUID organizerAccountId, @Valid @RequestBody CreateFundraiserRequest request) {
    Fundraiser fundraiser =
        manageFundraiserUseCase.create(
            organizerAccountId,
            request.title(),
            request.description(),
            FundraiserCategory.fromCode(request.category()),
            request.goalCents(),
            request.deadline(),
            request.ledgerPublic() == null || request.ledgerPublic());
    FundraiserResponse body = FundraiserResponse.from(fundraiser);
    return ResponseEntity.created(URI.create("/fundraisers/" + body.id())).body(body);
  }

  @GetMapping("/{id}")
  public FundraiserResponse getById(@PathVariable UUID id) {
    return FundraiserResponse.from(getFundraiserUseCase.getById(id));
  }

  @GetMapping
  public List<FundraiserResponse> list(
      @RequestParam(required = false) String category, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int pageSize) {
    FundraiserCategory parsedCategory = category == null ? null : FundraiserCategory.fromCode(category);
    return getFundraiserUseCase.list(parsedCategory, page, pageSize).stream().map(FundraiserResponse::from).toList();
  }
}
