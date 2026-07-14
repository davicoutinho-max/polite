package dev.civicpulse.privacycompliance.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.civicpulse.privacycompliance.application.port.out.ConsentRecordRepository;
import dev.civicpulse.privacycompliance.application.port.out.EventPublisher;
import dev.civicpulse.privacycompliance.domain.event.ConsentUpdated;
import dev.civicpulse.privacycompliance.domain.model.ConsentPurpose;
import dev.civicpulse.privacycompliance.domain.model.ConsentRecord;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ConsentServiceTest {

  private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

  @Mock private ConsentRecordRepository consentRecordRepository;
  @Mock private EventPublisher eventPublisher;

  private ConsentService service;

  @BeforeEach
  void setUp() {
    service = new ConsentService(consentRecordRepository, eventPublisher, Clock.fixed(NOW, ZoneOffset.UTC));
  }

  @Test
  void updateConsentCreatesNewRecordWhenNoneExists() {
    UUID accountId = UUID.randomUUID();
    when(consentRecordRepository.findByAccountAndPurpose(accountId, ConsentPurpose.MARKETING)).thenReturn(Optional.empty());
    when(consentRecordRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    ConsentRecord result = service.updateConsent(accountId, ConsentPurpose.MARKETING, true);

    assertThat(result.granted()).isTrue();
    ArgumentCaptor<ConsentUpdated> captor = ArgumentCaptor.forClass(ConsentUpdated.class);
    verify(eventPublisher).publish(captor.capture());
    assertThat(captor.getValue().granted()).isTrue();
  }

  @Test
  void updateConsentUpdatesExistingRecordInPlace() {
    UUID accountId = UUID.randomUUID();
    ConsentRecord existing = ConsentRecord.record(accountId, ConsentPurpose.ANALYTICS, true, NOW.minusSeconds(3600));
    when(consentRecordRepository.findByAccountAndPurpose(accountId, ConsentPurpose.ANALYTICS)).thenReturn(Optional.of(existing));
    when(consentRecordRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    ConsentRecord result = service.updateConsent(accountId, ConsentPurpose.ANALYTICS, false);

    assertThat(result.granted()).isFalse();
    assertThat(result).isSameAs(existing);
  }
}
