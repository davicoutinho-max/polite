package dev.civicpulse.legislative.application.port.out;

import dev.civicpulse.legislative.domain.model.SocialLink;
import java.util.List;
import java.util.UUID;

public interface SocialLinkRepository {

  SocialLink save(SocialLink socialLink);

  List<SocialLink> findByPolitician(UUID politicianAccountId);

  void deleteById(UUID id);
}
