package dev.civicpulse.legislative.application.port.in;

import dev.civicpulse.legislative.domain.model.CommitteeKind;
import dev.civicpulse.legislative.domain.model.CommitteeMembership;
import java.util.List;
import java.util.UUID;

public interface CommitteeUseCase {

  CommitteeMembership joinCommittee(UUID politicianAccountId, String name, String role, CommitteeKind kind);

  List<CommitteeMembership> getCommittees(UUID politicianAccountId);
}
