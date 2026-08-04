package dev.civicpulse.feedcontent.adapter.in.web;

import dev.civicpulse.feedcontent.adapter.in.web.dto.LikeRequest;
import dev.civicpulse.feedcontent.application.port.in.ManageCommentLikeUseCase;
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
@RequestMapping("/comments/{commentId}/likes")
public class CommentLikeController {

  private final ManageCommentLikeUseCase manageCommentLikeUseCase;

  public CommentLikeController(ManageCommentLikeUseCase manageCommentLikeUseCase) {
    this.manageCommentLikeUseCase = manageCommentLikeUseCase;
  }

  @PostMapping
  public ResponseEntity<Void> like(@PathVariable UUID commentId, @Valid @RequestBody LikeRequest request) {
    manageCommentLikeUseCase.like(commentId, request.accountId());
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping
  public ResponseEntity<Void> unlike(@PathVariable UUID commentId, @RequestParam UUID accountId) {
    manageCommentLikeUseCase.unlike(commentId, accountId);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{accountId}")
  public boolean isLiked(@PathVariable UUID commentId, @PathVariable UUID accountId) {
    return manageCommentLikeUseCase.isLiked(commentId, accountId);
  }
}
