package dev.civicpulse.feedcontent.adapter.in.web;

import dev.civicpulse.feedcontent.adapter.in.web.dto.AddTagRequest;
import dev.civicpulse.feedcontent.adapter.in.web.dto.PostMetricsResponse;
import dev.civicpulse.feedcontent.adapter.in.web.dto.PostResponse;
import dev.civicpulse.feedcontent.adapter.in.web.dto.PublishAgendaPostRequest;
import dev.civicpulse.feedcontent.adapter.in.web.dto.PublishLivePostRequest;
import dev.civicpulse.feedcontent.adapter.in.web.dto.PublishTextPostRequest;
import dev.civicpulse.feedcontent.application.port.in.GetFeedUseCase;
import dev.civicpulse.feedcontent.application.port.in.PublishPostUseCase;
import dev.civicpulse.feedcontent.application.port.out.PostAgendaDetailsRepository;
import dev.civicpulse.feedcontent.application.port.out.PostTagRepository;
import dev.civicpulse.feedcontent.domain.model.Post;
import dev.civicpulse.feedcontent.domain.model.PostVisibility;
import dev.civicpulse.feedcontent.domain.model.TagSeverity;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/posts")
public class PostController {

  private final PublishPostUseCase publishPostUseCase;
  private final GetFeedUseCase getFeedUseCase;
  private final PostTagRepository postTagRepository;
  private final PostAgendaDetailsRepository postAgendaDetailsRepository;

  public PostController(
      PublishPostUseCase publishPostUseCase,
      GetFeedUseCase getFeedUseCase,
      PostTagRepository postTagRepository,
      PostAgendaDetailsRepository postAgendaDetailsRepository) {
    this.publishPostUseCase = publishPostUseCase;
    this.getFeedUseCase = getFeedUseCase;
    this.postTagRepository = postTagRepository;
    this.postAgendaDetailsRepository = postAgendaDetailsRepository;
  }

  @PostMapping("/text")
  public ResponseEntity<PostResponse> publishText(
      @RequestHeader("X-Account-Id") UUID authorAccountId, @Valid @RequestBody PublishTextPostRequest request) {
    Post post =
        publishPostUseCase.publishTextPost(
            authorAccountId, request.content(), request.imageUrl(), visibilityOrDefault(request.visibility()), request.context());
    return created(post);
  }

  @PostMapping("/agenda")
  public ResponseEntity<PostResponse> publishAgenda(
      @RequestHeader("X-Account-Id") UUID authorAccountId, @Valid @RequestBody PublishAgendaPostRequest request) {
    Post post =
        publishPostUseCase.publishAgendaPost(
            authorAccountId,
            request.title(),
            request.eventDate(),
            request.location(),
            visibilityOrDefault(request.visibility()),
            request.context());
    return created(post);
  }

  @PostMapping("/live")
  public ResponseEntity<PostResponse> publishLive(
      @RequestHeader("X-Account-Id") UUID authorAccountId, @Valid @RequestBody PublishLivePostRequest request) {
    Post post =
        publishPostUseCase.publishLivePost(
            authorAccountId, request.liveSessionId(), visibilityOrDefault(request.visibility()), request.context());
    return created(post);
  }

  @PostMapping("/{postId}/tags")
  public ResponseEntity<Void> addTag(@PathVariable UUID postId, @Valid @RequestBody AddTagRequest request) {
    TagSeverity severity = request.severity() == null ? null : TagSeverity.fromCode(request.severity());
    publishPostUseCase.addTag(postId, request.label(), severity, request.icon());
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{postId}")
  public PostResponse getById(@PathVariable UUID postId) {
    return toResponse(getFeedUseCase.getById(postId));
  }

  @GetMapping("/{postId}/metrics")
  public PostMetricsResponse getMetrics(@PathVariable UUID postId) {
    return PostMetricsResponse.from(getFeedUseCase.getMetrics(postId));
  }

  @GetMapping
  public List<PostResponse> getPublicFeed(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int pageSize) {
    return getFeedUseCase.getPublicFeed(page, pageSize).stream().map(this::toResponse).toList();
  }

  @GetMapping("/by-author/{authorAccountId}")
  public List<PostResponse> getByAuthor(
      @PathVariable UUID authorAccountId, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int pageSize) {
    return getFeedUseCase.getByAuthor(authorAccountId, page, pageSize).stream().map(this::toResponse).toList();
  }

  private static PostVisibility visibilityOrDefault(String visibility) {
    return visibility == null ? PostVisibility.PUBLIC : PostVisibility.fromCode(visibility);
  }

  private PostResponse toResponse(Post post) {
    return PostResponse.from(post, postTagRepository.findByPostId(post.id()), postAgendaDetailsRepository.findByPostId(post.id()).orElse(null));
  }

  private ResponseEntity<PostResponse> created(Post post) {
    PostResponse body = toResponse(post);
    return ResponseEntity.created(URI.create("/posts/" + body.id())).body(body);
  }
}
