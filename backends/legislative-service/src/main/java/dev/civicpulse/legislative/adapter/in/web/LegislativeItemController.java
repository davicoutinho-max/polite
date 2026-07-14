package dev.civicpulse.legislative.adapter.in.web;

import dev.civicpulse.legislative.adapter.in.web.dto.AdvanceStatusRequest;
import dev.civicpulse.legislative.adapter.in.web.dto.FileLegislativeItemRequest;
import dev.civicpulse.legislative.adapter.in.web.dto.LegislativeItemResponse;
import dev.civicpulse.legislative.application.port.in.LegislativeItemUseCase;
import dev.civicpulse.legislative.domain.model.LegislativeItemCategory;
import dev.civicpulse.legislative.domain.model.LegislativeItemStatus;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/legislative-items")
public class LegislativeItemController {

  private final LegislativeItemUseCase legislativeItemUseCase;

  public LegislativeItemController(LegislativeItemUseCase legislativeItemUseCase) {
    this.legislativeItemUseCase = legislativeItemUseCase;
  }

  @PostMapping
  public ResponseEntity<LegislativeItemResponse> file(@Valid @RequestBody FileLegislativeItemRequest request) {
    var item =
        legislativeItemUseCase.fileItem(
            request.politicianAccountId(),
            request.reference(),
            request.title(),
            request.summary(),
            LegislativeItemCategory.fromCode(request.category()),
            request.itemDate(),
            request.cosponsorAccountIdsOrEmpty());
    LegislativeItemResponse body = LegislativeItemResponse.from(item);
    return ResponseEntity.created(URI.create("/legislative-items/" + body.id())).body(body);
  }

  @PatchMapping("/{id}/status")
  public LegislativeItemResponse advanceStatus(@PathVariable UUID id, @Valid @RequestBody AdvanceStatusRequest request) {
    return LegislativeItemResponse.from(legislativeItemUseCase.advanceStatus(id, LegislativeItemStatus.fromCode(request.status())));
  }

  @GetMapping("/{id}")
  public LegislativeItemResponse getItem(@PathVariable UUID id) {
    return LegislativeItemResponse.from(legislativeItemUseCase.getItem(id));
  }

  @GetMapping
  public List<LegislativeItemResponse> list(
      @RequestParam(required = false) UUID politicianAccountId,
      @RequestParam(required = false, defaultValue = "false") boolean recent,
      @RequestParam(required = false, defaultValue = "20") int limit) {
    if (politicianAccountId != null) {
      return legislativeItemUseCase.getItemsByPolitician(politicianAccountId).stream().map(LegislativeItemResponse::from).toList();
    }
    if (recent) {
      return legislativeItemUseCase.getRecentItems(limit).stream().map(LegislativeItemResponse::from).toList();
    }
    return List.of();
  }
}
