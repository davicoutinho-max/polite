package dev.civicpulse.feedcontent.adapter.out.persistence;

import dev.civicpulse.feedcontent.application.port.out.SocialShareRepository;
import dev.civicpulse.feedcontent.domain.model.SocialShare;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class SocialShareRepositoryAdapter implements SocialShareRepository {

  private final SocialShareJpaRepository jpaRepository;
  private final SocialShareMapper mapper;

  SocialShareRepositoryAdapter(SocialShareJpaRepository jpaRepository, SocialShareMapper mapper) {
    this.jpaRepository = jpaRepository;
    this.mapper = mapper;
  }

  @Override
  public SocialShare save(SocialShare share) {
    return mapper.toDomain(jpaRepository.save(mapper.toEntity(share)));
  }

  @Override
  public List<SocialShare> findByPost(UUID postId) {
    return jpaRepository.findByPostId(postId).stream().map(mapper::toDomain).toList();
  }
}
