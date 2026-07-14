package dev.civicpulse.legislative.adapter.in.web.dto;

import dev.civicpulse.legislative.domain.model.CommitteeMembership;
import java.util.UUID;

public record CommitteeMembershipResponse(UUID id, String name, String role, String kind) {

  public static CommitteeMembershipResponse from(CommitteeMembership membership) {
    return new CommitteeMembershipResponse(membership.id().orElse(null), membership.name(), membership.role(), membership.kind().code());
  }
}
