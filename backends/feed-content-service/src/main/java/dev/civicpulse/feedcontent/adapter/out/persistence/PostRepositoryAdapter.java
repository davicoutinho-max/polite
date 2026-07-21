package dev.civicpulse.feedcontent.adapter.out.persistence;

import dev.civicpulse.feedcontent.application.port.out.PostRepository;
import dev.civicpulse.feedcontent.domain.model.Post;
import dev.civicpulse.feedcontent.domain.model.PostVisibility;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
class PostRepositoryAdapter implements PostRepository {

  private final PostJpaRepository jpaRepository;
  private final PostMapper mapper;

  PostRepositoryAdapter(PostJpaRepository jpaRepository, PostMapper mapper) {
    this.jpaRepository = jpaRepository;
    this.mapper = mapper;
  }

  @Override
  public Post save(Post post) {
    return mapper.toDomain(jpaRepository.save(mapper.toEntity(post)));
  }

  @Override
  public void deleteById(UUID id) {
    jpaRepository.deleteById(id);
  }

  @Override
  public Optional<Post> findById(UUID id) {
    return jpaRepository.findById(id).map(mapper::toDomain);
  }

  @Override
  public List<Post> findByAuthorAccountId(UUID authorAccountId, int page, int pageSize) {
    return jpaRepository.findByAuthorAccountId(authorAccountId, PageRequest.of(page, pageSize)).stream().map(mapper::toDomain).toList();
  }

  @Override
  public List<Post> findPublicFeed(int page, int pageSize) {
    return jpaRepository.findByVisibilityOrderByCreatedAtDesc(PostVisibility.PUBLIC, PageRequest.of(page, pageSize)).stream()
        .map(mapper::toDomain)
        .toList();
  }
}
