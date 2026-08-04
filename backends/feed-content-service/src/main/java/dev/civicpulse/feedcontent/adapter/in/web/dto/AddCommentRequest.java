package dev.civicpulse.feedcontent.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/** {@code parentCommentId} is null for a top-level comment, or the id of the comment being
 * replied to. */
public record AddCommentRequest(@NotNull UUID authorAccountId, UUID parentCommentId, @NotBlank String body) {}
