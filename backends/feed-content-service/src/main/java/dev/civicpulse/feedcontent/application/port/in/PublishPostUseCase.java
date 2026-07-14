package dev.civicpulse.feedcontent.application.port.in;

import dev.civicpulse.feedcontent.domain.model.Post;
import dev.civicpulse.feedcontent.domain.model.PostVisibility;
import dev.civicpulse.feedcontent.domain.model.TagSeverity;
import java.util.UUID;

public interface PublishPostUseCase {

  Post publishTextPost(UUID authorAccountId, String content, String imageUrl, PostVisibility visibility, String context);

  Post publishAgendaPost(
      UUID authorAccountId, String title, String eventDate, String location, PostVisibility visibility, String context);

  Post publishLivePost(UUID authorAccountId, UUID liveSessionId, PostVisibility visibility, String context);

  void addTag(UUID postId, String label, TagSeverity severity, String icon);
}
