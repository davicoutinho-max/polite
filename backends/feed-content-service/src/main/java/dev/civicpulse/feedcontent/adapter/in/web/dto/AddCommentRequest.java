package dev.civicpulse.feedcontent.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AddCommentRequest(@NotNull UUID authorAccountId, @NotBlank String body) {}
