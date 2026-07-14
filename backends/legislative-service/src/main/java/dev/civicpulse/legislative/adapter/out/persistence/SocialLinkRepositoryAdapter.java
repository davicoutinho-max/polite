package dev.civicpulse.legislative.adapter.out.persistence;

import dev.civicpulse.legislative.application.port.out.SocialLinkRepository;
import dev.civicpulse.legislative.domain.model.SocialLink;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class SocialLinkRepositoryAdapter implements SocialLinkRepository {

  private final SocialLinkJpaRepository jpaRepository;
  private final SocialLinkMapper mapper;

  SocialLinkRepositoryAdapter(SocialLinkJpaRepository jpaRepository, SocialLinkMapper mapper) {
    this.jpaRepository = jpaRepository;
    this.mapper = mapper;
  }

  @Override
  public SocialLink save(SocialLink socialLink) {
    return mapper.toDomain(jpaRepository.save(mapper.toEntity(socialLink)));
  }

  @Override
  public List<SocialLink> findByPolitician(UUID politicianAccountId) {
    return jpaRepository.findByPoliticianAccountId(politicianAccountId).stream().map(mapper::toDomain).toList();
  }

  @Override
  public void deleteById(UUID id) {
    jpaRepository.deleteById(id);
  }
}
