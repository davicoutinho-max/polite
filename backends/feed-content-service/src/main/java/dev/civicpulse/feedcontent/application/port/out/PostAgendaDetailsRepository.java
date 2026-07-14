package dev.civicpulse.feedcontent.application.port.out;

import dev.civicpulse.feedcontent.domain.model.PostAgendaDetails;
import java.util.Optional;
import java.util.UUID;

public interface PostAgendaDetailsRepository {

  PostAgendaDetails save(PostAgendaDetails details);

  Optional<PostAgendaDetails> findByPostId(UUID postId);
}
