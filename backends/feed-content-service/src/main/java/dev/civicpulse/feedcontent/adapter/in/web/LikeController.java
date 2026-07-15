package dev.civicpulse.feedcontent.adapter.in.web;

import dev.civicpulse.feedcontent.adapter.in.web.dto.LikeRequest;
import dev.civicpulse.feedcontent.application.port.in.ManageLikeUseCase;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/posts/{postId}/likes")
public class LikeController {

  private final ManageLikeUseCase manageLikeUseCase;

  public LikeController(ManageLikeUseCase manageLikeUseCase) {
    this.manageLikeUseCase = manageLikeUseCase;
  }

  @PostMapping
  public ResponseEntity<Void> like(@PathVariable UUID postId, @Valid @RequestBody LikeRequest request) {
    manageLikeUseCase.like(postId, request.accountId());
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping
  public ResponseEntity<Void> unlike(@PathVariable UUID postId, @RequestParam UUID accountId) {
    manageLikeUseCase.unlike(postId, accountId);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{accountId}")
  public boolean isLiked(@PathVariable UUID postId, @PathVariable UUID accountId) {
    return manageLikeUseCase.isLiked(postId, accountId);
  }
}
