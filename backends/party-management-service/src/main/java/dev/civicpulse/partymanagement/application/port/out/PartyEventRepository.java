package dev.civicpulse.partymanagement.application.port.out;

import dev.civicpulse.partymanagement.domain.model.PartyEvent;
import java.util.List;
import java.util.UUID;

public interface PartyEventRepository {

  PartyEvent save(PartyEvent event);

  List<PartyEvent> findByPartyId(UUID partyId);
}
