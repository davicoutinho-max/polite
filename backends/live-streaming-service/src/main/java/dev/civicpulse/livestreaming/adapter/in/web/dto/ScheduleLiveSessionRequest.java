package dev.civicpulse.livestreaming.adapter.in.web.dto;

import java.time.Instant;

public record ScheduleLiveSessionRequest(String videoId, String channelId, Instant scheduledFor) {}
