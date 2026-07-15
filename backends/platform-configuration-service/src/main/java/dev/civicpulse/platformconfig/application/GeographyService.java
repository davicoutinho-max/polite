package dev.civicpulse.platformconfig.application;

import dev.civicpulse.platformconfig.application.port.in.ManageGeographyUseCase;
import dev.civicpulse.platformconfig.application.port.out.CountryRepository;
import dev.civicpulse.platformconfig.application.port.out.EventPublisher;
import dev.civicpulse.platformconfig.application.port.out.StateRepository;
import dev.civicpulse.platformconfig.domain.event.CountryAdded;
import dev.civicpulse.platformconfig.domain.event.CountryRemoved;
import dev.civicpulse.platformconfig.domain.model.Country;
import dev.civicpulse.platformconfig.domain.model.State;
import java.time.Clock;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GeographyService implements ManageGeographyUseCase {

  private final CountryRepository countryRepository;
  private final StateRepository stateRepository;
  private final EventPublisher eventPublisher;
  private final Clock clock;

  public GeographyService(CountryRepository countryRepository, StateRepository stateRepository, EventPublisher eventPublisher, Clock clock) {
    this.countryRepository = countryRepository;
    this.stateRepository = stateRepository;
    this.eventPublisher = eventPublisher;
    this.clock = clock;
  }

  @Override
  @Transactional
  public Country addCountry(String name, String code) {
    Country country = countryRepository.save(Country.create(UUID.randomUUID(), name, code));
    eventPublisher.publish(new CountryAdded(country.id(), clock.instant()));
    return country;
  }

  @Override
  @Transactional
  public void removeCountry(UUID id) {
    countryRepository.delete(id);
    eventPublisher.publish(new CountryRemoved(id, clock.instant()));
  }

  @Override
  @Transactional(readOnly = true)
  public List<Country> listCountries() {
    return countryRepository.findAll();
  }

  @Override
  @Transactional
  public State addState(UUID countryId, String name, String code) {
    return stateRepository.save(State.create(UUID.randomUUID(), countryId, name, code));
  }

  @Override
  @Transactional
  public void removeState(UUID id) {
    stateRepository.delete(id);
  }

  @Override
  @Transactional(readOnly = true)
  public List<State> listStates(UUID countryId) {
    return stateRepository.findByCountryId(countryId);
  }
}
