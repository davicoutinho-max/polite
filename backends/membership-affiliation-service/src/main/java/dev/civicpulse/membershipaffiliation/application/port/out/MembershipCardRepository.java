package dev.civicpulse.membershipaffiliation.application.port.out;

import dev.civicpulse.membershipaffiliation.domain.model.MembershipCard;
import java.util.Optional;
import java.util.UUID;

public interface MembershipCardRepository {

  MembershipCard save(MembershipCard card);

  Optional<MembershipCard> findByAffiliationId(UUID affiliationId);

  boolean existsByMemberNumber(String memberNumber);
}
