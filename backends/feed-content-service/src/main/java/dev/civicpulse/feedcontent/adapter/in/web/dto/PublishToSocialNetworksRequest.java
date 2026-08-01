package dev.civicpulse.feedcontent.adapter.in.web.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.Set;

public record PublishToSocialNetworksRequest(@NotEmpty Set<String> platforms) {}
