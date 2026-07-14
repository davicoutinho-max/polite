package dev.civicpulse.feedcontent.adapter.in.web;

import dev.civicpulse.feedcontent.adapter.in.web.dto.AddCommentRequest;
import dev.civicpulse.feedcontent.adapter.in.web.dto.CommentResponse;
import dev.civicpulse.feedcontent.application.port.in.ManageCommentUseCase;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/posts/{postId}/comments")
public class CommentController {

  private final ManageCommentUseCase manageCommentUseCase;

  public CommentController(ManageCommentUseCase manageCommentUseCase) {
    this.manageCommentUseCase = manageCommentUseCase;
  }

  @PostMapping
  public ResponseEntity<CommentResponse> add(@PathVariable UUID postId, @Valid @RequestBody AddCommentRequest request) {
    var comment = manageCommentUseCase.addComment(postId, request.authorAccountId(), request.body());
    CommentResponse body = CommentResponse.from(comment);
    return ResponseEntity.created(URI.create("/posts/" + postId + "/comments/" + body.id())).body(body);
  }

  @GetMapping
  public List<CommentResponse> list(@PathVariable UUID postId) {
    return manageCommentUseCase.listByPost(postId).stream().map(CommentResponse::from).toList();
  }
}
