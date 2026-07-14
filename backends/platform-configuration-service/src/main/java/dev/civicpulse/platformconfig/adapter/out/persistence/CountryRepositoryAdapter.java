package dev.civicpulse.platformconfig.adapter.out.persistence;

import dev.civicpulse.platformconfig.application.port.out.CountryRepository;
import dev.civicpulse.platformconfig.domain.model.Country;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class CountryRepositoryAdapter implements CountryRepository {

  private final CountryJpaRepository jpaRepository;

  CountryRepositoryAdapter(CountryJpaRepository jpaRepository) {
    this.jpaRepository = jpaRepository;
  }

  @Override
  public Country save(Country country) {
    var saved = jpaRepository.save(new CountryJpaEntity(country.id(), country.name(), country.code()));
    return toDomain(saved);
  }

  @Override
  public void delete(UUID id) {
    jpaRepository.deleteById(id);
  }

  @Override
  public Optional<Country> findById(UUID id) {
    return jpaRepository.findById(id).map(CountryRepositoryAdapter::toDomain);
  }

  @Override
  public List<Country> findAll() {
    return jpaRepository.findAll().stream().map(CountryRepositoryAdapter::toDomain).toList();
  }

  private static Country toDomain(CountryJpaEntity entity) {
    return Country.reconstitute(entity.getId(), entity.getName(), entity.getCode());
  }
}
