package dev.civicpulse.feedcontent.adapter.in.web.dto;

import dev.civicpulse.feedcontent.domain.model.Post;
import dev.civicpulse.feedcontent.domain.model.PostAgendaDetails;
import dev.civicpulse.feedcontent.domain.model.PostPollOption;
import dev.civicpulse.feedcontent.domain.model.PostTag;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record PostResponse(
    UUID id,
    UUID authorAccountId,
    String kind,
    String content,
    String imageUrl,
    String fileUrl,
    String fileName,
    String visibility,
    String context,
    UUID liveSessionId,
    Instant createdAt,
    List<TagResponse> tags,
    String agendaTitle,
    String agendaEventDate,
    String agendaLocation,
    List<PollOptionResponse> pollOptions) {

  public record TagResponse(String label, String severity, String icon) {
    public static TagResponse from(PostTag tag) {
      return new TagResponse(tag.label(), tag.severity().map(s -> s.code()).orElse(null), tag.icon().orElse(null));
    }
  }

  public record PollOptionResponse(UUID id, String label, long votes) {
    public static PollOptionResponse from(PostPollOption option, Map<UUID, Long> voteCounts) {
      return new PollOptionResponse(option.id(), option.label(), voteCounts.getOrDefault(option.id(), 0L));
    }
  }

  public static PostResponse from(
      Post post, List<PostTag> tags, PostAgendaDetails agendaDetails, List<PostPollOption> pollOptions, Map<UUID, Long> pollVoteCounts) {
    return new PostResponse(
        post.id(),
        post.authorAccountId(),
        post.kind().code(),
        post.content().orElse(null),
        post.imageUrl().orElse(null),
        post.fileUrl().orElse(null),
        post.fileName().orElse(null),
        post.visibility().code(),
        post.context().orElse(null),
        post.liveSessionId().orElse(null),
        post.createdAt(),
        tags.stream().map(TagResponse::from).toList(),
        agendaDetails == null ? null : agendaDetails.title(),
        agendaDetails == null ? null : agendaDetails.eventDate(),
        agendaDetails == null ? null : agendaDetails.location(),
        pollOptions.stream().map(o -> PollOptionResponse.from(o, pollVoteCounts)).toList());
  }
}
