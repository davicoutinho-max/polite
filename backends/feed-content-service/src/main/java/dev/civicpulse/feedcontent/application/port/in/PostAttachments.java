package dev.civicpulse.feedcontent.application.port.in;

import java.util.List;

/** Bundles the optional extras a post of any kind (text/agenda/live) may carry — an image, a
 * generic file, or poll options (2+ labels means "attach a poll", using the post's own content as
 * the question). Kept together since every {@code PublishPostUseCase} method needs all three. */
public record PostAttachments(String imageUrl, String fileUrl, String fileName, List<String> pollOptions) {

  public static final PostAttachments NONE = new PostAttachments(null, null, null, null);

  public boolean hasPoll() {
    return pollOptions != null && pollOptions.size() >= 2;
  }
}
