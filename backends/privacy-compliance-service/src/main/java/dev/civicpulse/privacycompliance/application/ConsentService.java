package dev.civicpulse.privacycompliance.application;

import dev.civicpulse.privacycompliance.application.port.in.ManageConsentUseCase;
import dev.civicpulse.privacycompliance.application.port.out.ConsentRecordRepository;
import dev.civicpulse.privacycompliance.application.port.out.EventPublisher;
import dev.civicpulse.privacycompliance.domain.event.ConsentUpdated;
import dev.civicpulse.privacycompliance.domain.model.ConsentPurpose;
import dev.civicpulse.privacycompliance.domain.model.ConsentRecord;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConsentService implements ManageConsentUseCase {

  private final ConsentRecordRepository consentRecordRepository;
  private final EventPublisher eventPublisher;
  private final Clock clock;

  public ConsentService(ConsentRecordRepository consentRecordRepository, EventPublisher eventPublisher, Clock clock) {
    this.consentRecordRepository = consentRecordRepository;
    this.eventPublisher = eventPublisher;
    this.clock = clock;
  }

  @Override
  @Transactional
  public ConsentRecord updateConsent(UUID accountId, ConsentPurpose purpose, boolean granted) {
    Instant now = clock.instant();
    ConsentRecord record =
        consentRecordRepository
            .findByAccountAndPurpose(accountId, purpose)
            .map(
                existing -> {
                  existing.update(granted, now);
                  return existing;
                })
            .orElseGet(() -> ConsentRecord.record(accountId, purpose, granted, now));
    ConsentRecord saved = consentRecordRepository.save(record);
    eventPublisher.publish(new ConsentUpdated(accountId, purpose.code(), granted, now));
    return saved;
  }

  @Override
  @Transactional(readOnly = true)
  public List<ConsentRecord> listByAccount(UUID accountId) {
    return consentRecordRepository.findByAccount(accountId);
  }
}
