package dev.civicpulse.directory.adapter.in.web.dto;

/** Either field may be omitted (null) to leave that image untouched — see
 * {@code Politician.updateProfileImages}/{@code Party.updateProfileImages}'s javadoc. Politicians
 * send {@code avatarUrl}; parties send it too but it's stored as their {@code logoUrl}. */
public record UpdateProfileImagesRequest(String avatarUrl, String coverImageUrl) {}
