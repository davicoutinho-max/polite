package dev.civicpulse.feedcontent.application.port.in;

import dev.civicpulse.feedcontent.domain.model.Post;
import dev.civicpulse.feedcontent.domain.model.PostVisibility;
import dev.civicpulse.feedcontent.domain.model.TagSeverity;
import java.util.UUID;

public interface PublishPostUseCase {

  Post publishTextPost(UUID authorAccountId, String content, PostAttachments attachments, PostVisibility visibility, String context);

  Post publishAgendaPost(
      UUID authorAccountId,
      String title,
      String eventDate,
      String location,
      PostAttachments attachments,
      PostVisibility visibility,
      String context);

  Post publishLivePost(UUID authorAccountId, UUID liveSessionId, PostAttachments attachments, PostVisibility visibility, String context);

  void addTag(UUID postId, String label, TagSeverity severity, String icon);

  /** Only the post's own author may delete it — throws NotPostOwnerException otherwise. */
  void deletePost(UUID postId, UUID requesterAccountId);

  /** Casts (or changes) the caller's vote on the post's poll — at most one vote per account is
   * ever kept, so voting again just moves it to the new option. */
  void vote(UUID postId, UUID accountId, UUID optionId);
}
