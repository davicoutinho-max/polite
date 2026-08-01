package dev.civicpulse.feedcontent.application.port.out;

import dev.civicpulse.feedcontent.domain.model.SocialShare;
import java.util.List;
import java.util.UUID;

public interface SocialShareRepository {

  SocialShare save(SocialShare share);

  List<SocialShare> findByPost(UUID postId);
}
