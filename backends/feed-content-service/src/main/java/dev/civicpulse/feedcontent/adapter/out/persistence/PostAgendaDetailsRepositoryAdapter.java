package dev.civicpulse.feedcontent.adapter.out.persistence;

import dev.civicpulse.feedcontent.application.port.out.PostAgendaDetailsRepository;
import dev.civicpulse.feedcontent.domain.model.PostAgendaDetails;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class PostAgendaDetailsRepositoryAdapter implements PostAgendaDetailsRepository {

  private final PostAgendaDetailsJpaRepository jpaRepository;

  PostAgendaDetailsRepositoryAdapter(PostAgendaDetailsJpaRepository jpaRepository) {
    this.jpaRepository = jpaRepository;
  }

  @Override
  public PostAgendaDetails save(PostAgendaDetails details) {
    var saved =
        jpaRepository.save(new PostAgendaDetailsJpaEntity(details.postId(), details.title(), details.eventDate(), details.location()));
    return toDomain(saved);
  }

  @Override
  public Optional<PostAgendaDetails> findByPostId(UUID postId) {
    return jpaRepository.findById(postId).map(PostAgendaDetailsRepositoryAdapter::toDomain);
  }

  private static PostAgendaDetails toDomain(PostAgendaDetailsJpaEntity entity) {
    return PostAgendaDetails.reconstitute(entity.getPostId(), entity.getTitle(), entity.getEventDate(), entity.getLocation());
  }
}
