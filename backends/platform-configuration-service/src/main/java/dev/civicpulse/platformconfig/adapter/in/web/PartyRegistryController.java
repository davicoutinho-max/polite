package dev.civicpulse.platformconfig.adapter.in.web;

import dev.civicpulse.platformconfig.adapter.in.web.dto.PartyRegistryResponse;
import dev.civicpulse.platformconfig.adapter.in.web.dto.RegisterPartyRequest;
import dev.civicpulse.platformconfig.application.port.in.RegisterPartyUseCase;
import dev.civicpulse.platformconfig.application.port.out.PartyRegistryRepository;
import dev.civicpulse.platformconfig.domain.exception.PartyRegistryEntryNotFoundException;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/parties")
public class PartyRegistryController {

  private final RegisterPartyUseCase registerPartyUseCase;
  private final PartyRegistryRepository partyRegistryRepository;

  public PartyRegistryController(RegisterPartyUseCase registerPartyUseCase, PartyRegistryRepository partyRegistryRepository) {
    this.registerPartyUseCase = registerPartyUseCase;
    this.partyRegistryRepository = partyRegistryRepository;
  }

  @PostMapping
  public ResponseEntity<PartyRegistryResponse> register(@Valid @RequestBody RegisterPartyRequest request) {
    var entry = registerPartyUseCase.registerParty(request.name(), request.acronym(), request.number(), request.president(), request.ideology());
    PartyRegistryResponse body = PartyRegistryResponse.from(entry);
    return ResponseEntity.created(URI.create("/parties/" + body.id())).body(body);
  }

  @GetMapping("/{id}")
  public PartyRegistryResponse getById(@PathVariable UUID id) {
    return PartyRegistryResponse.from(partyRegistryRepository.findById(id).orElseThrow(() -> new PartyRegistryEntryNotFoundException(id)));
  }

  @GetMapping
  public List<PartyRegistryResponse> list() {
    return partyRegistryRepository.findAll().stream().map(PartyRegistryResponse::from).toList();
  }
}
