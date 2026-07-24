package dev.civicpulse.messaging.adapter.in.web.dto;

import java.util.UUID;

/** {@code body} is optional when an attachment is present (a bare audio/video/file/image message
 * with no caption) — enforced instead by the domain ({@code Message}'s constructor requires one
 * of the two), not by bean validation here, since which one is required is conditional.
 * {@code replyToMessageId} is optional on every send — quoting another message in the same
 * conversation. */
public record SendMessageRequest(
    String body, String attachmentUrl, String attachmentType, String attachmentFileName, UUID replyToMessageId) {}
