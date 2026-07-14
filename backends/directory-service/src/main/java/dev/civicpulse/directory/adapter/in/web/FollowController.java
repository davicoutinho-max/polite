package dev.civicpulse.directory.adapter.in.web;

import dev.civicpulse.directory.adapter.in.web.dto.FollowRequest;
import dev.civicpulse.directory.application.port.in.FollowUseCase;
import dev.civicpulse.directory.domain.model.FollowTargetType;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/follows")
public class FollowController {

  private final FollowUseCase followUseCase;

  public FollowController(FollowUseCase followUseCase) {
    this.followUseCase = followUseCase;
  }

  @PostMapping
  public ResponseEntity<Void> follow(@RequestHeader("X-Account-Id") UUID followerAccountId, @Valid @RequestBody FollowRequest request) {
    followUseCase.follow(followerAccountId, FollowTargetType.fromCode(request.targetType()), request.targetId());
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping
  public ResponseEntity<Void> unfollow(@RequestHeader("X-Account-Id") UUID followerAccountId, @Valid @RequestBody FollowRequest request) {
    followUseCase.unfollow(followerAccountId, FollowTargetType.fromCode(request.targetType()), request.targetId());
    return ResponseEntity.noContent().build();
  }
}
