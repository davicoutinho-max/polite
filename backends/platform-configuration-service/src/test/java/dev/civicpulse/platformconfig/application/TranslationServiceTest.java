package dev.civicpulse.platformconfig.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.civicpulse.platformconfig.application.port.out.EventPublisher;
import dev.civicpulse.platformconfig.application.port.out.TranslationKeyRepository;
import dev.civicpulse.platformconfig.application.port.out.TranslationValueRepository;
import dev.civicpulse.platformconfig.domain.model.TranslationKey;
import dev.civicpulse.platformconfig.domain.model.TranslationValue;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TranslationServiceTest {

  private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

  @Mock private TranslationKeyRepository keyRepository;
  @Mock private TranslationValueRepository valueRepository;
  @Mock private EventPublisher eventPublisher;

  private TranslationService service;

  @BeforeEach
  void setUp() {
    service = new TranslationService(keyRepository, valueRepository, eventPublisher, Clock.fixed(NOW, ZoneOffset.UTC));
  }

  @Test
  void setTranslationCreatesKeyWhenItDoesNotExist() {
    UUID keyId = UUID.randomUUID();
    when(keyRepository.findByKey("nav.feed")).thenReturn(Optional.empty());
    when(keyRepository.save(any())).thenAnswer(invocation -> {
      TranslationKey candidate = invocation.getArgument(0);
      return TranslationKey.reconstitute(keyId, candidate.key());
    });
    when(valueRepository.findByKeyAndLanguage(keyId, "en-us")).thenReturn(Optional.empty());
    when(valueRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    TranslationValue result = service.setTranslation("nav.feed", "en-us", "Feed");

    assertThat(result.translationKeyId()).isEqualTo(keyId);
    assertThat(result.value()).isEqualTo("Feed");
    verify(eventPublisher).publish(any());
  }

  @Test
  void setTranslationReusesExistingKey() {
    UUID keyId = UUID.randomUUID();
    when(keyRepository.findByKey("nav.feed")).thenReturn(Optional.of(TranslationKey.reconstitute(keyId, "nav.feed")));
    when(valueRepository.findByKeyAndLanguage(keyId, "en-us")).thenReturn(Optional.empty());
    when(valueRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    service.setTranslation("nav.feed", "en-us", "Feed");

    verify(keyRepository, never()).save(any());
  }

  @Test
  void listKeysDelegatesToRepository() {
    TranslationKey key = TranslationKey.reconstitute(UUID.randomUUID(), "nav.feed");
    when(keyRepository.findAll()).thenReturn(List.of(key));

    assertThat(service.listKeys()).containsExactly(key);
  }

  @Test
  void deleteKeyRemovesValuesThenTheKeyItself() {
    UUID keyId = UUID.randomUUID();

    service.deleteKey(keyId);

    verify(valueRepository).deleteByTranslationKeyId(keyId);
    verify(keyRepository).delete(keyId);
  }
}
