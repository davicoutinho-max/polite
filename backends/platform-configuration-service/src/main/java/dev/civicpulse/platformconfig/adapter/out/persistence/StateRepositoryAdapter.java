package dev.civicpulse.platformconfig.adapter.out.persistence;

import dev.civicpulse.platformconfig.application.port.out.StateRepository;
import dev.civicpulse.platformconfig.domain.model.State;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class StateRepositoryAdapter implements StateRepository {

  private final StateJpaRepository jpaRepository;

  StateRepositoryAdapter(StateJpaRepository jpaRepository) {
    this.jpaRepository = jpaRepository;
  }

  @Override
  public State save(State state) {
    var saved = jpaRepository.save(new StateJpaEntity(state.id(), state.countryId(), state.name(), state.code()));
    return toDomain(saved);
  }

  @Override
  public List<State> findByCountryId(UUID countryId) {
    return jpaRepository.findByCountryId(countryId).stream().map(StateRepositoryAdapter::toDomain).toList();
  }

  private static State toDomain(StateJpaEntity entity) {
    return State.reconstitute(entity.getId(), entity.getCountryId(), entity.getName(), entity.getCode());
  }
}
