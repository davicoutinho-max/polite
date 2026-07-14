package dev.civicpulse.feedcontent.domain.event;

import java.time.Instant;

public sealed interface DomainEvent permits PostPublished, PostLiked, CommentAdded {

  String topic();

  Instant occurredAt();
}
