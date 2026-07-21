package dev.civicpulse.platformconfig.application;

import dev.civicpulse.platformconfig.application.port.in.ManageTranslationUseCase;
import dev.civicpulse.platformconfig.application.port.out.EventPublisher;
import dev.civicpulse.platformconfig.application.port.out.TranslationKeyRepository;
import dev.civicpulse.platformconfig.application.port.out.TranslationValueRepository;
import dev.civicpulse.platformconfig.domain.event.TranslationValueUpdated;
import dev.civicpulse.platformconfig.domain.model.TranslationKey;
import dev.civicpulse.platformconfig.domain.model.TranslationValue;
import java.time.Clock;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TranslationService implements ManageTranslationUseCase {

  private final TranslationKeyRepository keyRepository;
  private final TranslationValueRepository valueRepository;
  private final EventPublisher eventPublisher;
  private final Clock clock;

  public TranslationService(
      TranslationKeyRepository keyRepository, TranslationValueRepository valueRepository, EventPublisher eventPublisher, Clock clock) {
    this.keyRepository = keyRepository;
    this.valueRepository = valueRepository;
    this.eventPublisher = eventPublisher;
    this.clock = clock;
  }

  @Override
  @Transactional
  public TranslationValue setTranslation(String key, String languageId, String value) {
    TranslationKey translationKey = keyRepository.findByKey(key).orElseGet(() -> keyRepository.save(TranslationKey.create(UUID.randomUUID(), key)));

    TranslationValue translationValue =
        valueRepository
            .findByKeyAndLanguage(translationKey.id(), languageId)
            .map(existing -> {
              existing.update(value);
              return existing;
            })
            .orElseGet(() -> TranslationValue.set(translationKey.id(), languageId, value));

    TranslationValue saved = valueRepository.save(translationValue);
    eventPublisher.publish(new TranslationValueUpdated(translationKey.id(), languageId, clock.instant()));
    return saved;
  }

  @Override
  @Transactional(readOnly = true)
  public List<TranslationValue> getTranslationsForLanguage(String languageId) {
    return valueRepository.findByLanguage(languageId);
  }

  @Override
  @Transactional(readOnly = true)
  public List<TranslationKey> listKeys() {
    return keyRepository.findAll();
  }

  @Override
  @Transactional
  public void deleteKey(UUID keyId) {
    valueRepository.deleteByTranslationKeyId(keyId);
    keyRepository.delete(keyId);
  }
}
