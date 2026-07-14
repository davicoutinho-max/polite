package dev.civicpulse.privacycompliance.adapter.in.web;

import dev.civicpulse.privacycompliance.adapter.in.web.dto.DataExportRequestResponse;
import dev.civicpulse.privacycompliance.adapter.in.web.dto.MarkExportReadyRequest;
import dev.civicpulse.privacycompliance.application.port.in.ManageDataExportUseCase;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/data-export-requests")
public class DataExportController {

  private final ManageDataExportUseCase manageDataExportUseCase;

  public DataExportController(ManageDataExportUseCase manageDataExportUseCase) {
    this.manageDataExportUseCase = manageDataExportUseCase;
  }

  /** {@code X-Account-Id} is the caller's own id, forwarded by the Gateway after JWT
   * validation — see docs/architecture/system-architecture.html. */
  @PostMapping
  public ResponseEntity<DataExportRequestResponse> request(@RequestHeader("X-Account-Id") UUID accountId) {
    DataExportRequestResponse body = DataExportRequestResponse.from(manageDataExportUseCase.request(accountId));
    return ResponseEntity.created(URI.create("/data-export-requests/" + body.id())).body(body);
  }

  @GetMapping("/{id}")
  public DataExportRequestResponse getById(@PathVariable UUID id) {
    return DataExportRequestResponse.from(manageDataExportUseCase.getById(id));
  }

  @GetMapping
  public List<DataExportRequestResponse> listByAccount(@RequestHeader("X-Account-Id") UUID accountId) {
    return manageDataExportUseCase.listByAccount(accountId).stream().map(DataExportRequestResponse::from).toList();
  }

  /** Simulates the background worker that would actually generate the export file — see
   * PrivacyComplianceServiceApplication's scope note. */
  @PostMapping("/{id}/start-processing")
  public DataExportRequestResponse startProcessing(@PathVariable UUID id) {
    return DataExportRequestResponse.from(manageDataExportUseCase.startProcessing(id));
  }

  @PostMapping("/{id}/ready")
  public DataExportRequestResponse markReady(@PathVariable UUID id, @Valid @RequestBody MarkExportReadyRequest request) {
    return DataExportRequestResponse.from(manageDataExportUseCase.markReady(id, request.downloadUrl(), request.expiresAt()));
  }

  @PostMapping("/{id}/failed")
  public DataExportRequestResponse markFailed(@PathVariable UUID id) {
    return DataExportRequestResponse.from(manageDataExportUseCase.markFailed(id));
  }
}
