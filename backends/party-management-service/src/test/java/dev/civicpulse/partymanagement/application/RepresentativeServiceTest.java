package dev.civicpulse.partymanagement.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.civicpulse.partymanagement.application.port.out.EventPublisher;
import dev.civicpulse.partymanagement.application.port.out.PartyRepresentativeRepository;
import dev.civicpulse.partymanagement.domain.exception.AlreadyRepresentativeException;
import dev.civicpulse.partymanagement.domain.exception.RepresentativeNotFoundException;
import dev.civicpulse.partymanagement.domain.model.PartyRepresentative;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RepresentativeServiceTest {

  private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

  @Mock private PartyRepresentativeRepository representativeRepository;
  @Mock private EventPublisher eventPublisher;

  private RepresentativeService service;

  @BeforeEach
  void setUp() {
    service = new RepresentativeService(representativeRepository, eventPublisher, Clock.fixed(NOW, ZoneOffset.UTC));
  }

  @Test
  void linkExistingRejectsDuplicateLink() {
    UUID partyId = UUID.randomUUID();
    UUID politicianId = UUID.randomUUID();
    when(representativeRepository.existsByPartyIdAndPoliticianAccountId(partyId, politicianId)).thenReturn(true);

    assertThatThrownBy(() -> service.linkExisting(partyId, politicianId, "Deputy")).isInstanceOf(AlreadyRepresentativeException.class);

    verify(representativeRepository, never()).save(any());
  }

  @Test
  void linkExistingSucceedsAndPublishesEvent() {
    UUID partyId = UUID.randomUUID();
    UUID politicianId = UUID.randomUUID();
    when(representativeRepository.existsByPartyIdAndPoliticianAccountId(partyId, politicianId)).thenReturn(false);
    when(representativeRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    PartyRepresentative result = service.linkExisting(partyId, politicianId, "Senator");

    assertThat(result.partyId()).isEqualTo(partyId);
    assertThat(result.politicianAccountId()).isEqualTo(politicianId);
    verify(eventPublisher).publish(any());
  }

  @Test
  void unlinkingUnknownRepresentativeThrows() {
    UUID partyId = UUID.randomUUID();
    UUID politicianId = UUID.randomUUID();
    when(representativeRepository.findByPartyIdAndPoliticianAccountId(partyId, politicianId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.unlink(partyId, politicianId)).isInstanceOf(RepresentativeNotFoundException.class);
  }
}
