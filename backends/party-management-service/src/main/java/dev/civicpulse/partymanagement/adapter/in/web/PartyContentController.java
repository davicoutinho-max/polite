package dev.civicpulse.partymanagement.adapter.in.web;

import dev.civicpulse.partymanagement.adapter.in.web.dto.AddEventRequest;
import dev.civicpulse.partymanagement.adapter.in.web.dto.AddOfficeRequest;
import dev.civicpulse.partymanagement.adapter.in.web.dto.EventResponse;
import dev.civicpulse.partymanagement.adapter.in.web.dto.OfficeResponse;
import dev.civicpulse.partymanagement.application.port.in.ManagePartyContentUseCase;
import dev.civicpulse.partymanagement.domain.model.PartyOfficeScope;
import dev.civicpulse.partymanagement.domain.model.TagSeverity;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/parties/{partyId}")
public class PartyContentController {

  private final ManagePartyContentUseCase managePartyContentUseCase;

  public PartyContentController(ManagePartyContentUseCase managePartyContentUseCase) {
    this.managePartyContentUseCase = managePartyContentUseCase;
  }

  @PostMapping("/offices")
  public OfficeResponse addOffice(@PathVariable UUID partyId, @Valid @RequestBody AddOfficeRequest request) {
    return OfficeResponse.from(
        managePartyContentUseCase.addOffice(partyId, PartyOfficeScope.fromCode(request.scope()), request.location(), request.leaderName()));
  }

  @GetMapping("/offices")
  public List<OfficeResponse> listOffices(@PathVariable UUID partyId) {
    return managePartyContentUseCase.listOffices(partyId).stream().map(OfficeResponse::from).toList();
  }

  @PostMapping("/events")
  public EventResponse addEvent(@PathVariable UUID partyId, @Valid @RequestBody AddEventRequest request) {
    TagSeverity severity = request.tagSeverity() == null ? null : TagSeverity.fromCode(request.tagSeverity());
    return EventResponse.from(
        managePartyContentUseCase.addEvent(partyId, request.title(), request.eventDate(), request.location(), request.tagLabel(), severity));
  }

  @GetMapping("/events")
  public List<EventResponse> listEvents(@PathVariable UUID partyId) {
    return managePartyContentUseCase.listEvents(partyId).stream().map(EventResponse::from).toList();
  }
}
