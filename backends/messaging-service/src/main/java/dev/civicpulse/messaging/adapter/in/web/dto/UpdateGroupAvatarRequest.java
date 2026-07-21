package dev.civicpulse.messaging.adapter.in.web.dto;

/** {@code groupAvatarUrl} may be null/blank to remove the group's photo. */
public record UpdateGroupAvatarRequest(String groupAvatarUrl) {}
