package dev.civicpulse.membershipaffiliation.adapter.out.persistence;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface MembershipCardJpaRepository extends JpaRepository<MembershipCardJpaEntity, UUID> {

  boolean existsByMemberNumber(String memberNumber);
}
