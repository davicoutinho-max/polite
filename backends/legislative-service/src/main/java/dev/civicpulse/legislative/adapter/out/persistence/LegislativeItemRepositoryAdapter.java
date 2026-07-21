package dev.civicpulse.legislative.adapter.out.persistence;

import dev.civicpulse.legislative.application.port.out.LegislativeItemRepository;
import dev.civicpulse.legislative.domain.model.LegislativeItem;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
class LegislativeItemRepositoryAdapter implements LegislativeItemRepository {

  private final LegislativeItemJpaRepository jpaRepository;
  private final LegislativeItemMapper mapper;

  LegislativeItemRepositoryAdapter(LegislativeItemJpaRepository jpaRepository, LegislativeItemMapper mapper) {
    this.jpaRepository = jpaRepository;
    this.mapper = mapper;
  }

  @Override
  public LegislativeItem save(LegislativeItem item) {
    return mapper.toDomain(jpaRepository.save(mapper.toEntity(item)));
  }

  @Override
  public Optional<LegislativeItem> findById(UUID id) {
    return jpaRepository.findById(id).map(mapper::toDomain);
  }

  @Override
  public List<LegislativeItem> findByPolitician(UUID politicianAccountId) {
    return jpaRepository.findByPoliticianAccountIdOrderByItemDateDesc(politicianAccountId).stream().map(mapper::toDomain).toList();
  }

  @Override
  public List<LegislativeItem> findRecent(int limit) {
    return jpaRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, limit)).stream().map(mapper::toDomain).toList();
  }

  @Override
  public void deleteById(UUID id) {
    jpaRepository.deleteById(id);
  }
}
