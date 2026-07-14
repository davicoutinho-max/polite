package dev.civicpulse.platformconfig.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.civicpulse.platformconfig.application.port.out.EventPublisher;
import dev.civicpulse.platformconfig.application.port.out.LanguageRepository;
import dev.civicpulse.platformconfig.domain.exception.CannotRemoveDefaultLanguageException;
import dev.civicpulse.platformconfig.domain.exception.LanguageNotFoundException;
import dev.civicpulse.platformconfig.domain.model.Language;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LanguageServiceTest {

  private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

  @Mock private LanguageRepository languageRepository;
  @Mock private EventPublisher eventPublisher;

  private LanguageService service;

  @BeforeEach
  void setUp() {
    service = new LanguageService(languageRepository, eventPublisher, Clock.fixed(NOW, ZoneOffset.UTC));
  }

  @Test
  void addingANewDefaultLanguageClearsThePreviousDefaultFirst() {
    when(languageRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    service.addLanguage("en-us", "English (US)", "en", true);

    InOrder inOrder = Mockito.inOrder(languageRepository);
    inOrder.verify(languageRepository).clearDefaultFlag();
    inOrder.verify(languageRepository).save(any());
  }

  @Test
  void addingANonDefaultLanguageNeverTouchesDefaultFlag() {
    when(languageRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    service.addLanguage("es-es", "Spanish", "es", false);

    verify(languageRepository, never()).clearDefaultFlag();
  }

  @Test
  void setDefaultLanguageClearsThenMarksNewDefault() {
    when(languageRepository.findById("es-es")).thenReturn(Optional.of(Language.reconstitute("es-es", "Spanish", "es", false)));

    service.setDefaultLanguage("es-es");

    InOrder inOrder = Mockito.inOrder(languageRepository);
    inOrder.verify(languageRepository).clearDefaultFlag();
    inOrder.verify(languageRepository).markAsDefault("es-es");
    verify(eventPublisher, times(1)).publish(any());
  }

  @Test
  void setDefaultLanguageThrowsForUnknownLanguage() {
    when(languageRepository.findById("xx")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.setDefaultLanguage("xx")).isInstanceOf(LanguageNotFoundException.class);

    verify(languageRepository, never()).clearDefaultFlag();
  }

  @Test
  void cannotRemoveTheDefaultLanguage() {
    when(languageRepository.findById("pt-br")).thenReturn(Optional.of(Language.reconstitute("pt-br", "Portuguese (BR)", "pt", true)));

    assertThatThrownBy(() -> service.removeLanguage("pt-br")).isInstanceOf(CannotRemoveDefaultLanguageException.class);

    verify(languageRepository, never()).delete(any());
  }

  @Test
  void removingANonDefaultLanguageSucceeds() {
    when(languageRepository.findById("es-es")).thenReturn(Optional.of(Language.reconstitute("es-es", "Spanish", "es", false)));

    service.removeLanguage("es-es");

    verify(languageRepository).delete("es-es");
  }
}
