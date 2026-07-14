package dev.civicpulse.privacycompliance.application;

import dev.civicpulse.privacycompliance.application.port.in.ManageAccountDeletionUseCase;
import dev.civicpulse.privacycompliance.application.port.out.AccountDeletionRequestRepository;
import dev.civicpulse.privacycompliance.application.port.out.ErasureAuditLogRepository;
import dev.civicpulse.privacycompliance.application.port.out.EventPublisher;
import dev.civicpulse.privacycompliance.domain.event.AccountDeletionRequested;
import dev.civicpulse.privacycompliance.domain.exception.AccountDeletionRequestNotFoundException;
import dev.civicpulse.privacycompliance.domain.model.AccountDeletionRequest;
import dev.civicpulse.privacycompliance.domain.model.ErasureAuditEntry;
import dev.civicpulse.privacycompliance.domain.model.ExpectedErasureServices;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountDeletionService implements ManageAccountDeletionUseCase {

  private final AccountDeletionRequestRepository accountDeletionRequestRepository;
  private final ErasureAuditLogRepository erasureAuditLogRepository;
  private final EventPublisher eventPublisher;
  private final Clock clock;

  public AccountDeletionService(
      AccountDeletionRequestRepository accountDeletionRequestRepository,
      ErasureAuditLogRepository erasureAuditLogRepository,
      EventPublisher eventPublisher,
      Clock clock) {
    this.accountDeletionRequestRepository = accountDeletionRequestRepository;
    this.erasureAuditLogRepository = erasureAuditLogRepository;
    this.eventPublisher = eventPublisher;
    this.clock = clock;
  }

  @Override
  @Transactional
  public AccountDeletionRequest request(UUID accountId) {
    Instant now = clock.instant();
    AccountDeletionRequest saved = accountDeletionRequestRepository.save(AccountDeletionRequest.request(UUID.randomUUID(), accountId, now));
    eventPublisher.publish(new AccountDeletionRequested(saved.id(), accountId, now));
    return saved;
  }

  @Override
  @Transactional
  public AccountDeletionRequest confirm(UUID requestId) {
    AccountDeletionRequest request = findOrThrow(requestId);
    request.confirm();
    return accountDeletionRequestRepository.save(request);
  }

  @Override
  @Transactional
  public AccountDeletionRequest startProcessing(UUID requestId) {
    AccountDeletionRequest request = findOrThrow(requestId);
    request.startProcessing();
    return accountDeletionRequestRepository.save(request);
  }

  @Override
  @Transactional
  public AccountDeletionRequest cancel(UUID requestId) {
    AccountDeletionRequest request = findOrThrow(requestId);
    request.cancel();
    return accountDeletionRequestRepository.save(request);
  }

  @Override
  @Transactional
  public void onErasureReported(UUID deletionRequestId, String serviceName, Integer recordCount) {
    if (erasureAuditLogRepository.existsByDeletionRequestIdAndServiceName(deletionRequestId, serviceName)) {
      return; // idempotent — reprocessed message
    }
    AccountDeletionRequest request =
        accountDeletionRequestRepository.findById(deletionRequestId).orElseThrow(() -> new AccountDeletionRequestNotFoundException(deletionRequestId));

    erasureAuditLogRepository.save(ErasureAuditEntry.record(deletionRequestId, serviceName, recordCount, clock.instant()));

    Set<String> reported =
        erasureAuditLogRepository.findByDeletionRequestId(deletionRequestId).stream().map(ErasureAuditEntry::serviceName).collect(Collectors.toSet());
    if (reported.containsAll(ExpectedErasureServices.ALL)) {
      request.complete(clock.instant());
      accountDeletionRequestRepository.save(request);
    }
  }

  @Override
  @Transactional(readOnly = true)
  public AccountDeletionRequest getById(UUID requestId) {
    return findOrThrow(requestId);
  }

  @Override
  @Transactional(readOnly = true)
  public List<AccountDeletionRequest> listByAccount(UUID accountId) {
    return accountDeletionRequestRepository.findByAccountId(accountId);
  }

  @Override
  @Transactional(readOnly = true)
  public List<ErasureAuditEntry> listErasureAudit(UUID deletionRequestId) {
    return erasureAuditLogRepository.findByDeletionRequestId(deletionRequestId);
  }

  private AccountDeletionRequest findOrThrow(UUID requestId) {
    return accountDeletionRequestRepository.findById(requestId).orElseThrow(() -> new AccountDeletionRequestNotFoundException(requestId));
  }
}
