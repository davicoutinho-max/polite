package dev.civicpulse.feedcontent.application.port.out;

import dev.civicpulse.feedcontent.domain.model.SocialConnection;
import dev.civicpulse.feedcontent.domain.model.SocialPlatform;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SocialConnectionRepository {

  SocialConnection save(SocialConnection connection);

  Optional<SocialConnection> findByAccountAndPlatform(UUID accountId, SocialPlatform platform);

  List<SocialConnection> findByAccount(UUID accountId);

  void deleteByAccountAndPlatform(UUID accountId, SocialPlatform platform);
}
