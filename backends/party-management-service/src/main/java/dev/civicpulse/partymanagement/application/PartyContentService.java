package dev.civicpulse.partymanagement.application;

import dev.civicpulse.partymanagement.application.port.in.ManagePartyContentUseCase;
import dev.civicpulse.partymanagement.application.port.out.PartyEventRepository;
import dev.civicpulse.partymanagement.application.port.out.PartyOfficeRepository;
import dev.civicpulse.partymanagement.domain.model.PartyEvent;
import dev.civicpulse.partymanagement.domain.model.PartyOffice;
import dev.civicpulse.partymanagement.domain.model.PartyOfficeScope;
import dev.civicpulse.partymanagement.domain.model.TagSeverity;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PartyContentService implements ManagePartyContentUseCase {

  private final PartyOfficeRepository officeRepository;
  private final PartyEventRepository eventRepository;

  public PartyContentService(PartyOfficeRepository officeRepository, PartyEventRepository eventRepository) {
    this.officeRepository = officeRepository;
    this.eventRepository = eventRepository;
  }

  @Override
  @Transactional
  public PartyOffice addOffice(UUID partyId, PartyOfficeScope scope, String location, String leaderName) {
    return officeRepository.save(PartyOffice.create(UUID.randomUUID(), partyId, scope, location, leaderName));
  }

  @Override
  @Transactional(readOnly = true)
  public List<PartyOffice> listOffices(UUID partyId) {
    return officeRepository.findByPartyId(partyId);
  }

  @Override
  @Transactional
  public PartyEvent addEvent(UUID partyId, String title, LocalDate eventDate, String location, String tagLabel, TagSeverity tagSeverity) {
    return eventRepository.save(PartyEvent.create(UUID.randomUUID(), partyId, title, eventDate, location, tagLabel, tagSeverity));
  }

  @Override
  @Transactional(readOnly = true)
  public List<PartyEvent> listEvents(UUID partyId) {
    return eventRepository.findByPartyId(partyId);
  }
}
