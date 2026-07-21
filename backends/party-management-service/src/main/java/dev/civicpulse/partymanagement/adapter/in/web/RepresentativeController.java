package dev.civicpulse.partymanagement.adapter.in.web;

import dev.civicpulse.partymanagement.adapter.in.web.dto.LinkRepresentativeRequest;
import dev.civicpulse.partymanagement.adapter.in.web.dto.RegisterPoliticianRequest;
import dev.civicpulse.partymanagement.adapter.in.web.dto.RepresentativeResponse;
import dev.civicpulse.partymanagement.application.port.in.ManageRepresentativeUseCase;
import dev.civicpulse.partymanagement.application.port.in.RegisterPoliticianUseCase;
import dev.civicpulse.partymanagement.application.port.in.RegisterPoliticianUseCase.RegisterPoliticianCommand;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/parties/{partyId}/representatives")
public class RepresentativeController {

  private final RegisterPoliticianUseCase registerPoliticianUseCase;
  private final ManageRepresentativeUseCase manageRepresentativeUseCase;

  public RepresentativeController(RegisterPoliticianUseCase registerPoliticianUseCase, ManageRepresentativeUseCase manageRepresentativeUseCase) {
    this.registerPoliticianUseCase = registerPoliticianUseCase;
    this.manageRepresentativeUseCase = manageRepresentativeUseCase;
  }

  /** Party-initiated politician registration (flow 02) — provisions a brand-new identity. */
  @PostMapping("/register")
  public ResponseEntity<RepresentativeResponse> registerPolitician(
      @PathVariable UUID partyId, @Valid @RequestBody RegisterPoliticianRequest request) {
    var representative =
        registerPoliticianUseCase.registerPolitician(
            partyId,
            new RegisterPoliticianCommand(
                request.name(),
                request.handle(),
                request.email(),
                request.password(),
                request.documentType(),
                request.documentNumber(),
                request.roleTitle(),
                request.state()));
    return created(representative);
  }

  /** Links an already-existing politician account (Platform Admin reassignment path, flow 03). */
  @PostMapping
  public ResponseEntity<RepresentativeResponse> link(@PathVariable UUID partyId, @Valid @RequestBody LinkRepresentativeRequest request) {
    var representative = manageRepresentativeUseCase.linkExisting(partyId, request.politicianAccountId(), request.roleTitle());
    return created(representative);
  }

  @DeleteMapping("/{politicianAccountId}")
  public ResponseEntity<Void> unlink(@PathVariable UUID partyId, @PathVariable UUID politicianAccountId) {
    manageRepresentativeUseCase.unlink(partyId, politicianAccountId);
    return ResponseEntity.noContent().build();
  }

  @GetMapping
  public List<RepresentativeResponse> list(@PathVariable UUID partyId) {
    return manageRepresentativeUseCase.listByParty(partyId).stream().map(RepresentativeResponse::from).toList();
  }

  private ResponseEntity<RepresentativeResponse> created(dev.civicpulse.partymanagement.domain.model.PartyRepresentative representative) {
    RepresentativeResponse body = RepresentativeResponse.from(representative);
    return ResponseEntity.created(URI.create("/parties/" + body.partyId() + "/representatives/" + body.politicianAccountId())).body(body);
  }
}
