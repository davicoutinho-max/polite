package dev.civicpulse.livestreaming.adapter.in.web.dto;

public record EndLiveSessionRequest(Integer totalUniqueViewers, Integer avgWatchSeconds) {}
