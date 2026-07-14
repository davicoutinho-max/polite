package dev.civicpulse.membershipaffiliation.adapter.in.web;

import dev.civicpulse.membershipaffiliation.adapter.in.web.dto.GenerateFeeRequest;
import dev.civicpulse.membershipaffiliation.adapter.in.web.dto.MembershipFeeResponse;
import dev.civicpulse.membershipaffiliation.application.port.in.ManageMembershipFeeUseCase;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/affiliations/{affiliationId}/fees")
public class MembershipFeeController {

  private final ManageMembershipFeeUseCase manageMembershipFeeUseCase;

  public MembershipFeeController(ManageMembershipFeeUseCase manageMembershipFeeUseCase) {
    this.manageMembershipFeeUseCase = manageMembershipFeeUseCase;
  }

  @PostMapping
  public MembershipFeeResponse generate(@PathVariable UUID affiliationId, @Valid @RequestBody GenerateFeeRequest request) {
    return MembershipFeeResponse.from(
        manageMembershipFeeUseCase.generateFee(affiliationId, request.referencePeriod(), request.amountCents(), request.dueDate()));
  }

  @GetMapping
  public List<MembershipFeeResponse> list(@PathVariable UUID affiliationId) {
    return manageMembershipFeeUseCase.listByAffiliation(affiliationId).stream().map(MembershipFeeResponse::from).toList();
  }
}
