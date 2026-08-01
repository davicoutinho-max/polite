package dev.civicpulse.feedcontent.adapter.in.web;

import dev.civicpulse.feedcontent.adapter.in.web.dto.PublishToSocialNetworksRequest;
import dev.civicpulse.feedcontent.adapter.in.web.dto.SocialShareResponse;
import dev.civicpulse.feedcontent.application.port.in.PublishToSocialNetworksUseCase;
import dev.civicpulse.feedcontent.domain.model.SocialPlatform;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/posts")
public class SocialShareController {

  private final PublishToSocialNetworksUseCase useCase;

  public SocialShareController(PublishToSocialNetworksUseCase useCase) {
    this.useCase = useCase;
  }

  @PostMapping("/{postId}/social-shares")
  public List<SocialShareResponse> publish(
      @PathVariable UUID postId, @RequestHeader("X-Account-Id") UUID accountId, @Valid @RequestBody PublishToSocialNetworksRequest request) {
    Set<SocialPlatform> platforms = request.platforms().stream().map(SocialPlatform::fromCode).collect(Collectors.toSet());
    return useCase.publish(postId, accountId, platforms).stream().map(SocialShareResponse::from).toList();
  }

  @GetMapping("/{postId}/social-shares")
  public List<SocialShareResponse> list(@PathVariable UUID postId) {
    return useCase.listShares(postId).stream().map(SocialShareResponse::from).toList();
  }
}
