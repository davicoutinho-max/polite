package dev.civicpulse.elections.adapter.out.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface PersonalVoteJpaRepository extends JpaRepository<PersonalVoteJpaEntity, UUID> {

  Optional<PersonalVoteJpaEntity> findByCitizenAccountIdAndElectionIdAndOffice(UUID citizenAccountId, UUID electionId, String office);

  List<PersonalVoteJpaEntity> findByCitizenAccountIdAndElectionId(UUID citizenAccountId, UUID electionId);
}
