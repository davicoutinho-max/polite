package dev.civicpulse.legislative.application.port.out;

import dev.civicpulse.legislative.domain.model.TeamMember;
import java.util.List;
import java.util.UUID;

public interface TeamMemberRepository {

  TeamMember save(TeamMember teamMember);

  List<TeamMember> findByPolitician(UUID politicianAccountId);

  void deleteById(UUID id);
}
