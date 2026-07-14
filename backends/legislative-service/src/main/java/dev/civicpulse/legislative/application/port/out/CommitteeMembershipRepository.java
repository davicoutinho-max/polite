package dev.civicpulse.legislative.application.port.out;

import dev.civicpulse.legislative.domain.model.CommitteeMembership;
import java.util.List;
import java.util.UUID;

public interface CommitteeMembershipRepository {

  CommitteeMembership save(CommitteeMembership membership);

  List<CommitteeMembership> findByPolitician(UUID politicianAccountId);
}
