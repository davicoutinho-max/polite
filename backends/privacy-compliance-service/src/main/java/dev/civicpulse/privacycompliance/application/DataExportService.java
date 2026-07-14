package dev.civicpulse.privacycompliance.application;

import dev.civicpulse.privacycompliance.application.port.in.ManageDataExportUseCase;
import dev.civicpulse.privacycompliance.application.port.out.DataExportRequestRepository;
import dev.civicpulse.privacycompliance.application.port.out.EventPublisher;
import dev.civicpulse.privacycompliance.domain.event.DataExportRequested;
import dev.civicpulse.privacycompliance.domain.exception.DataExportRequestNotFoundException;
import dev.civicpulse.privacycompliance.domain.model.DataExportRequest;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DataExportService implements ManageDataExportUseCase {

  private final DataExportRequestRepository dataExportRequestRepository;
  private final EventPublisher eventPublisher;
  private final Clock clock;

  public DataExportService(DataExportRequestRepository dataExportRequestRepository, EventPublisher eventPublisher, Clock clock) {
    this.dataExportRequestRepository = dataExportRequestRepository;
    this.eventPublisher = eventPublisher;
    this.clock = clock;
  }

  @Override
  @Transactional
  public DataExportRequest request(UUID accountId) {
    Instant now = clock.instant();
    DataExportRequest saved = dataExportRequestRepository.save(DataExportRequest.request(UUID.randomUUID(), accountId, now));
    eventPublisher.publish(new DataExportRequested(saved.id(), accountId, now));
    return saved;
  }

  @Override
  @Transactional
  public DataExportRequest startProcessing(UUID requestId) {
    DataExportRequest request = findOrThrow(requestId);
    request.startProcessing();
    return dataExportRequestRepository.save(request);
  }

  @Override
  @Transactional
  public DataExportRequest markReady(UUID requestId, String downloadUrl, Instant expiresAt) {
    DataExportRequest request = findOrThrow(requestId);
    request.markReady(downloadUrl, expiresAt, clock.instant());
    return dataExportRequestRepository.save(request);
  }

  @Override
  @Transactional
  public DataExportRequest markFailed(UUID requestId) {
    DataExportRequest request = findOrThrow(requestId);
    request.markFailed(clock.instant());
    return dataExportRequestRepository.save(request);
  }

  @Override
  @Transactional(readOnly = true)
  public DataExportRequest getById(UUID requestId) {
    return findOrThrow(requestId);
  }

  @Override
  @Transactional(readOnly = true)
  public List<DataExportRequest> listByAccount(UUID accountId) {
    return dataExportRequestRepository.findByAccountId(accountId);
  }

  private DataExportRequest findOrThrow(UUID requestId) {
    return dataExportRequestRepository.findById(requestId).orElseThrow(() -> new DataExportRequestNotFoundException(requestId));
  }
}
