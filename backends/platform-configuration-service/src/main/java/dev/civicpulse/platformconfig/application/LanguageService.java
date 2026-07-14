package dev.civicpulse.platformconfig.application;

import dev.civicpulse.platformconfig.application.port.in.ManageLanguageUseCase;
import dev.civicpulse.platformconfig.application.port.out.EventPublisher;
import dev.civicpulse.platformconfig.application.port.out.LanguageRepository;
import dev.civicpulse.platformconfig.domain.event.DefaultLanguageChanged;
import dev.civicpulse.platformconfig.domain.event.LanguageAdded;
import dev.civicpulse.platformconfig.domain.event.LanguageRemoved;
import dev.civicpulse.platformconfig.domain.exception.CannotRemoveDefaultLanguageException;
import dev.civicpulse.platformconfig.domain.exception.LanguageNotFoundException;
import dev.civicpulse.platformconfig.domain.model.Language;
import java.time.Clock;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LanguageService implements ManageLanguageUseCase {

  private final LanguageRepository languageRepository;
  private final EventPublisher eventPublisher;
  private final Clock clock;

  public LanguageService(LanguageRepository languageRepository, EventPublisher eventPublisher, Clock clock) {
    this.languageRepository = languageRepository;
    this.eventPublisher = eventPublisher;
    this.clock = clock;
  }

  @Override
  @Transactional
  public Language addLanguage(String id, String name, String code, boolean isDefault) {
    if (isDefault) {
      languageRepository.clearDefaultFlag();
    }
    Language language = languageRepository.save(Language.create(id, name, code, isDefault));
    eventPublisher.publish(new LanguageAdded(id, clock.instant()));
    if (isDefault) {
      eventPublisher.publish(new DefaultLanguageChanged(id, clock.instant()));
    }
    return language;
  }

  @Override
  @Transactional
  public void removeLanguage(String id) {
    Language language = languageRepository.findById(id).orElseThrow(() -> new LanguageNotFoundException(id));
    if (language.isDefault()) {
      throw new CannotRemoveDefaultLanguageException(id);
    }
    languageRepository.delete(id);
    eventPublisher.publish(new LanguageRemoved(id, clock.instant()));
  }

  @Override
  @Transactional
  public Language setDefaultLanguage(String id) {
    if (languageRepository.findById(id).isEmpty()) {
      throw new LanguageNotFoundException(id);
    }
    languageRepository.clearDefaultFlag();
    languageRepository.markAsDefault(id);
    eventPublisher.publish(new DefaultLanguageChanged(id, clock.instant()));
    return languageRepository.findById(id).orElseThrow(() -> new LanguageNotFoundException(id));
  }

  @Override
  @Transactional(readOnly = true)
  public List<Language> listLanguages() {
    return languageRepository.findAll();
  }
}
