package dev.civicpulse.directory.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/** The follower's own account id is never trusted from the request body — the Gateway is the
 * JWT trust boundary (see docs/architecture/system-architecture.html) and forwards it as the
 * {@code X-Account-Id} header after validating the token. */
public record FollowRequest(@NotBlank String targetType, @NotNull UUID targetId) {}
