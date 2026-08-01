package dev.civicpulse.directory.application.port.in;

import java.util.UUID;

/** Self-service avatar/cover-photo updates — a politician or party changing how their own
 * profile looks. Unlike the rest of the directory catalog (a read projection built from
 * Kafka events, see {@link ProjectDirectoryUseCase}'s javadoc), these fields are owned locally
 * by this service, the same way {@code followers_count}/{@code member_count} are. */
public interface UpdateProfileImagesUseCase {

  void updatePoliticianProfileImages(UUID accountId, String avatarUrl, String coverImageUrl);

  void updatePartyLogo(UUID partyId, String logoUrl);
}
