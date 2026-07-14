package dev.civicpulse.fundraising.adapter.out.persistence;

import dev.civicpulse.fundraising.application.port.out.FundraiserRepository;
import dev.civicpulse.fundraising.domain.model.Fundraiser;
import dev.civicpulse.fundraising.domain.model.FundraiserCategory;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
class FundraiserRepositoryAdapter implements FundraiserRepository {

  private final FundraiserJpaRepository jpaRepository;
  private final FundraiserMapper mapper;

  FundraiserRepositoryAdapter(FundraiserJpaRepository jpaRepository, FundraiserMapper mapper) {
    this.jpaRepository = jpaRepository;
    this.mapper = mapper;
  }

  @Override
  public Fundraiser save(Fundraiser fundraiser) {
    return mapper.toDomain(jpaRepository.save(mapper.toEntity(fundraiser)));
  }

  @Override
  public Optional<Fundraiser> findById(UUID id) {
    return jpaRepository.findById(id).map(mapper::toDomain);
  }

  @Override
  public List<Fundraiser> findAll(int page, int pageSize) {
    return jpaRepository.findAllOrderByCreatedAtDesc(PageRequest.of(page, pageSize)).stream().map(mapper::toDomain).toList();
  }

  @Override
  public List<Fundraiser> findByCategory(FundraiserCategory category, int page, int pageSize) {
    return jpaRepository.findByCategory(category, PageRequest.of(page, pageSize)).stream().map(mapper::toDomain).toList();
  }
}
