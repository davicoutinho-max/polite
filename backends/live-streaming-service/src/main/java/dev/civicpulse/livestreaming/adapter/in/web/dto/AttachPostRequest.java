package dev.civicpulse.livestreaming.adapter.in.web.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AttachPostRequest(@NotNull UUID postId) {}
