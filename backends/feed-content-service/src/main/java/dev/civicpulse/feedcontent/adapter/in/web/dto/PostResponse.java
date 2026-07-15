package dev.civicpulse.feedcontent.adapter.in.web.dto;

import dev.civicpulse.feedcontent.domain.model.Post;
import dev.civicpulse.feedcontent.domain.model.PostAgendaDetails;
import dev.civicpulse.feedcontent.domain.model.PostTag;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PostResponse(
    UUID id,
    UUID authorAccountId,
    String kind,
    String content,
    String imageUrl,
    String visibility,
    String context,
    UUID liveSessionId,
    Instant createdAt,
    List<TagResponse> tags,
    String agendaTitle,
    String agendaEventDate,
    String agendaLocation) {

  public record TagResponse(String label, String severity, String icon) {
    public static TagResponse from(PostTag tag) {
      return new TagResponse(tag.label(), tag.severity().map(s -> s.code()).orElse(null), tag.icon().orElse(null));
    }
  }

  public static PostResponse from(Post post, List<PostTag> tags, PostAgendaDetails agendaDetails) {
    return new PostResponse(
        post.id(),
        post.authorAccountId(),
        post.kind().code(),
        post.content().orElse(null),
        post.imageUrl().orElse(null),
        post.visibility().code(),
        post.context().orElse(null),
        post.liveSessionId().orElse(null),
        post.createdAt(),
        tags.stream().map(TagResponse::from).toList(),
        agendaDetails == null ? null : agendaDetails.title(),
        agendaDetails == null ? null : agendaDetails.eventDate(),
        agendaDetails == null ? null : agendaDetails.location());
  }
}
