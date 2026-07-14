package dev.civicpulse.partymanagement.application.port.in;

import dev.civicpulse.partymanagement.domain.model.PartyEvent;
import dev.civicpulse.partymanagement.domain.model.PartyOffice;
import dev.civicpulse.partymanagement.domain.model.PartyOfficeScope;
import dev.civicpulse.partymanagement.domain.model.TagSeverity;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ManagePartyContentUseCase {

  PartyOffice addOffice(UUID partyId, PartyOfficeScope scope, String location, String leaderName);

  List<PartyOffice> listOffices(UUID partyId);

  PartyEvent addEvent(UUID partyId, String title, LocalDate eventDate, String location, String tagLabel, TagSeverity tagSeverity);

  List<PartyEvent> listEvents(UUID partyId);
}
