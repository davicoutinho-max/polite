package dev.civicpulse.partymanagement.application.port.in;

import dev.civicpulse.partymanagement.domain.model.PartyMember;
import dev.civicpulse.partymanagement.domain.model.PartyMemberStatus;
import java.util.List;
import java.util.UUID;

public interface ManagePartyMembershipUseCase {

  PartyMember changeStatus(UUID partyId, UUID citizenAccountId, PartyMemberStatus newStatus);

  List<PartyMember> listByParty(UUID partyId);
}
