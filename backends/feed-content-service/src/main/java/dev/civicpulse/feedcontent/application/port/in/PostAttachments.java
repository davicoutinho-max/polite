package dev.civicpulse.feedcontent.application.port.in;

import java.time.Instant;
import java.util.List;

/** Bundles the optional extras a post of any kind (text/agenda/live) may carry — an image, a
 * generic file, or poll options (2+ labels means "attach a poll", using the post's own content as
 * the question). {@code pollClosesAt} is only meaningful alongside poll options — null means the
 * poll never closes on its own. Kept together since every {@code PublishPostUseCase} method needs
 * all of these. */
public record PostAttachments(String imageUrl, String fileUrl, String fileName, List<String> pollOptions, Instant pollClosesAt) {

  public static final PostAttachments NONE = new PostAttachments(null, null, null, null, null);

  public boolean hasPoll() {
    return pollOptions != null && pollOptions.size() >= 2;
  }
}
