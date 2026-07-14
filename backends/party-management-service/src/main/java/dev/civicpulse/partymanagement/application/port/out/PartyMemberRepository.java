package dev.civicpulse.partymanagement.application.port.out;

import dev.civicpulse.partymanagement.domain.model.PartyMember;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PartyMemberRepository {

  PartyMember save(PartyMember member);

  Optional<PartyMember> findByPartyIdAndCitizenAccountId(UUID partyId, UUID citizenAccountId);

  List<PartyMember> findByPartyId(UUID partyId);
}
