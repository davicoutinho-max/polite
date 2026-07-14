package dev.civicpulse.legislative.adapter.in.web.dto;

import dev.civicpulse.legislative.domain.model.TeamMember;
import java.util.UUID;

public record TeamMemberResponse(UUID id, String name, String role, String avatarUrl) {

  public static TeamMemberResponse from(TeamMember teamMember) {
    return new TeamMemberResponse(teamMember.id().orElse(null), teamMember.name(), teamMember.role(), teamMember.avatarUrl().orElse(null));
  }
}
